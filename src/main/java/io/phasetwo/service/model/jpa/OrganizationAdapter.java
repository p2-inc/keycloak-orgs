package io.phasetwo.service.model.jpa;

import static io.phasetwo.service.Orgs.*;
import static org.keycloak.models.UserModel.EMAIL;
import static org.keycloak.models.UserModel.FIRST_NAME;
import static org.keycloak.models.UserModel.LAST_NAME;
import static org.keycloak.models.UserModel.USERNAME;
import static org.keycloak.models.jpa.PaginationUtils.paginateQuery;
import static org.keycloak.utils.StreamsUtil.closing;

import io.phasetwo.service.model.DomainModel;
import io.phasetwo.service.model.InvitationModel;
import io.phasetwo.service.model.OrganizationMemberModel;
import io.phasetwo.service.model.OrganizationModel;
import io.phasetwo.service.model.OrganizationRoleModel;
import io.phasetwo.service.model.jpa.entity.DomainEntity;
import io.phasetwo.service.model.jpa.entity.ExtOrganizationEntity;
import io.phasetwo.service.model.jpa.entity.InvitationEntity;
import io.phasetwo.service.model.jpa.entity.OrganizationAttributeEntity;
import io.phasetwo.service.model.jpa.entity.OrganizationMemberEntity;
import io.phasetwo.service.model.jpa.entity.OrganizationRoleEntity;
import io.phasetwo.service.model.jpa.entity.UserOrganizationRoleMappingEntity;
import io.phasetwo.service.util.IdentityProviders;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.persistence.criteria.*;
import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.jpa.JpaModel;
import org.keycloak.models.jpa.entities.UserEntity;
import org.keycloak.models.utils.KeycloakModelUtils;

public class OrganizationAdapter implements OrganizationModel, JpaModel<ExtOrganizationEntity> {

  protected final KeycloakSession session;
  protected final ExtOrganizationEntity org;
  protected final EntityManager em;
  protected final RealmModel realm;

  private static final char ESCAPE_BACKSLASH = '\\';

  public OrganizationAdapter(
      KeycloakSession session, RealmModel realm, EntityManager em, ExtOrganizationEntity org) {
    this.session = session;
    this.em = em;
    this.org = org;
    this.realm = realm;
  }

  @Override
  public ExtOrganizationEntity getEntity() {
    return org;
  }

  @Override
  public String getId() {
    return org.getId();
  }

  @Override
  public String getName() {
    return org.getName();
  }

  @Override
  public void setName(String name) {
    org.setName(name);
  }

  @Override
  public String getDisplayName() {
    return org.getDisplayName();
  }

  @Override
  public void setDisplayName(String displayName) {
    org.setDisplayName(displayName);
  }

  @Override
  public Set<String> getDomains() {
    return org.getDomains().stream().map(DomainEntity::getDomain).collect(Collectors.toSet());
  }

  @Override
  public void setDomains(Set<String> domains) {
    //  org.setDomains(domains);
    Set<String> lower = domains.stream().map(String::toLowerCase).collect(Collectors.toSet());
    org.getDomains().removeIf(e -> !lower.contains(e.getDomain()));
    lower.removeIf(d -> org.getDomains().stream().filter(e -> d.equals(e.getDomain())).count() > 0);
    lower.forEach(
        d -> {
          DomainEntity de = new DomainEntity();
          de.setId(KeycloakModelUtils.generateId());
          de.setDomain(d);
          de.setVerified(false);
          de.setOrganization(org);
          org.getDomains().add(de);
        });
  }

  @Override
  public DomainModel getDomain(String domainName) {
    TypedQuery<DomainEntity> query =
        em.createNamedQuery("getDomainByOrganizationAndDomainName", DomainEntity.class);
    query.setParameter("organization", org);
    query.setParameter("search", domainName);
    query.setMaxResults(1);
    try {
      return new DomainAdapter(session, realm, em, query.getSingleResult());
    } catch (Exception e) {
    }
    return null;
  }

  @Override
  public String getUrl() {
    return org.getUrl();
  }

  @Override
  public void setUrl(String url) {
    org.setUrl(url);
  }

  @Override
  public RealmModel getRealm() {
    return session.realms().getRealm(org.getRealmId());
  }

  @Override
  public UserModel getCreatedBy() {
    return session.users().getUserById(realm, org.getCreatedBy());
  }

  @Override
  public Map<String, List<String>> getAttributes() {
    MultivaluedHashMap<String, String> result = new MultivaluedHashMap<>();
    for (OrganizationAttributeEntity attr : org.getAttributes()) {
      result.add(attr.getName(), attr.getValue());
    }
    return result;
  }

  @Override
  public void removeAttribute(String name) {
    org.getAttributes().removeIf(attribute -> attribute.getName().equals(name));
  }

  @Override
  public void removeAttributes() {
    org.getAttributes().clear();
  }

  @Override
  public void setAttribute(String name, List<String> values) {
    removeAttribute(name);
    for (String value : values) {
      OrganizationAttributeEntity a = new OrganizationAttributeEntity();
      a.setId(KeycloakModelUtils.generateId());
      a.setName(name);
      a.setValue(value);
      a.setOrganization(org);
      em.persist(a);
      org.getAttributes().add(a);
    }
  }

  private TypedQuery<OrganizationMemberEntity> membersQuery(String search, boolean excludeAdmin) {
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<OrganizationMemberEntity> criteriaQuery =
        cb.createQuery(OrganizationMemberEntity.class);

    Root<OrganizationMemberEntity> root = criteriaQuery.from(OrganizationMemberEntity.class);

    List<Predicate> predicates = new ArrayList<>();
    // defining the organization search clause
    predicates.add(cb.equal(root.get("organization"), org));

    // search filter
    if (search != null && !search.isEmpty()) {
      List<Predicate> searchTermsPredicates = new ArrayList<>();
      Subquery<String> subquery = criteriaQuery.subquery(String.class);
      Root<UserEntity> subRoot = subquery.from(UserEntity.class);

      // Add your search predicate
      for (String stringToSearch : search.trim().split(",")) {
        searchTermsPredicates.add(
                cb.or(getSearchOptionPredicateArray(stringToSearch, cb, subRoot)));
      }

      subquery.select(subRoot.get("id"));
      subquery.where(cb.or(searchTermsPredicates.toArray(Predicate[]::new)));

      // Now add the subquery-based predicate
      predicates.add(root.get("userId").in(subquery));
    }

    if (excludeAdmin) {
      List<Predicate> excludeAdminsPredicates = new ArrayList<>();
      Subquery<String> subquery = criteriaQuery.subquery(String.class);
      Root<UserEntity> subRoot = subquery.from(UserEntity.class);
      excludeAdminsPredicates.add(cb.like(subRoot.get(USERNAME), "org-admin-%", ESCAPE_BACKSLASH));
      excludeAdminsPredicates.add(cb.equal(cb.length(subRoot.get(USERNAME)), "46"));

      subquery.select(subRoot.get("id"));
      subquery.where(cb.not(cb.and(excludeAdminsPredicates.toArray(Predicate[]::new))));

      predicates.add(root.get("userId").in(subquery));
    }

    criteriaQuery
        .where(predicates.toArray(Predicate[]::new))
        .orderBy(cb.asc(root.get("createdAt")));
    return em.createQuery(criteriaQuery);
  }

  private Predicate[] getSearchOptionPredicateArray(
          String value, CriteriaBuilder builder, Root<UserEntity> from) {
    value = value.trim().toLowerCase();
    List<Predicate> orPredicates = new ArrayList<>();
    if (!value.isEmpty()) {
      value = "%" + value + "%"; // contains in SQL query manner
      orPredicates.add(builder.like(from.get(USERNAME), value, ESCAPE_BACKSLASH));
      orPredicates.add(builder.like(from.get(EMAIL), value, ESCAPE_BACKSLASH));
      orPredicates.add(builder.like(builder.lower(from.get(FIRST_NAME)), value, ESCAPE_BACKSLASH));
      orPredicates.add(builder.like(builder.lower(from.get(LAST_NAME)), value, ESCAPE_BACKSLASH));
    }
    return orPredicates.toArray(Predicate[]::new);
  }

  @Override
  public Stream<UserModel> searchForMembersStream(
      String search, Integer firstResult, Integer maxResults, boolean excludeAdmin) {
    var query = membersQuery(search, excludeAdmin);
    return closing(paginateQuery(query, firstResult, maxResults).getResultStream())
        .filter(Objects::nonNull)
        .map(OrganizationMemberEntity::getUserId)
        .map(userId -> session.users().getUserById(realm, userId))
        .filter(u -> u.getServiceAccountClientLink() == null);
  }

  @Override
  public Stream<OrganizationMemberModel> getOrganizationMembersStream() {
    TypedQuery<OrganizationMemberEntity> query =
        em.createNamedQuery("getOrganizationMembers", OrganizationMemberEntity.class);
    query.setParameter("organization", org);

    return query
        .getResultStream()
        .map(
            organizationMemberEntity ->
                new OrganizationMemberAdapter(session, realm, em, organizationMemberEntity));
  }

  @Override
  public Stream<OrganizationMemberModel> searchForOrganizationMembersStream(
      String search, Integer firstResult, Integer maxResults) {
    CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
    CriteriaQuery<OrganizationMemberEntity> criteriaQuery =
        criteriaBuilder.createQuery(OrganizationMemberEntity.class);

    Root<OrganizationMemberEntity> root = criteriaQuery.from(OrganizationMemberEntity.class);

    List<Predicate> predicates = new ArrayList<>();
    // defining the organization search clause
    predicates.add(criteriaBuilder.equal(root.get("organization"), org));
    if (search != null && !search.isEmpty()) {
      var userIds = userIdsSubquery(criteriaQuery, search);
      predicates.add(root.get("userId").in(userIds));
    }

    criteriaQuery
        .where(predicates.toArray(Predicate[]::new))
        .orderBy(criteriaBuilder.asc(root.get("createdAt")));

    TypedQuery<OrganizationMemberEntity> query = em.createQuery(criteriaQuery);

    return closing(paginateQuery(query, firstResult, maxResults).getResultStream())
        .filter(Objects::nonNull)
        .map(
            organizationMemberEntity ->
                new OrganizationMemberAdapter(session, realm, em, organizationMemberEntity));
  }

  private Subquery<String> userIdsSubquery(CriteriaQuery<?> query, String search) {
    CriteriaBuilder cb = em.getCriteriaBuilder();
    Subquery<String> subquery = query.subquery(String.class);
    Root<UserEntity> subRoot = subquery.from(UserEntity.class);

    subquery.select(subRoot.get("id"));
    List<Predicate> subqueryPredicates = new ArrayList<>();

    subqueryPredicates.add(cb.equal(subRoot.get("realmId"), realm.getId()));

    List<Predicate> searchTermsPredicates = new ArrayList<>();
    // define search terms
    for (String stringToSearch : search.trim().split(",")) {
      searchTermsPredicates.add(cb.or(getSearchOptionPredicateArray(stringToSearch, cb, subRoot)));
    }
    Predicate searchPredicate = cb.or(searchTermsPredicates.toArray(Predicate[]::new));
    subqueryPredicates.add(searchPredicate);

    subquery.where(subqueryPredicates.toArray(Predicate[]::new));

    return subquery;
  }

  @Override
  public Long getMembersCount(boolean excludeAdmin) {
    TypedQuery<Long> query =
        em.createNamedQuery(
            excludeAdmin
                ? "getOrganizationMembersCountExcludeAdmin"
                : "getOrganizationMembersCount",
            Long.class);
    query.setParameter("organization", org);
    return query.getSingleResult();
  }

  @Override
  public Stream<UserModel> getMembersStream(boolean excludeAdmin) {
    var query = membersQuery(null, excludeAdmin);
    return query
        .getResultStream()
        .filter(Objects::nonNull)
        .map(OrganizationMemberEntity::getUserId)
        .map(userId -> session.users().getUserById(realm, userId))
        .filter(u -> u.getServiceAccountClientLink() == null);
  }

  @Override
  public boolean hasMembership(UserModel user) {
    return org.getMembers().stream().anyMatch(m -> m.getUserId().equals(user.getId()));
  }

  @Override
  public void grantMembership(UserModel user) {
    if (hasMembership(user)) return;
    OrganizationMemberEntity m = new OrganizationMemberEntity();
    m.setId(KeycloakModelUtils.generateId());
    m.setUserId(user.getId());
    m.setOrganization(org);
    em.persist(m);
    org.getMembers().add(m);
  }

  @Override
  public void revokeMembership(UserModel user) {
    if (!hasMembership(user)) return;
    org.getMembers().removeIf(m -> m.getUserId().equals(user.getId()));
    getRolesStream().forEach(r -> r.revokeRole(user));
    if (user.getEmail() != null) revokeInvitations(user.getEmail());
  }

  @Override
  public Long getInvitationsCount() {
    TypedQuery<Long> query = em.createNamedQuery("getInvitationCount", Long.class);
    query.setParameter("organization", org);
    return query.getSingleResult();
  }

  @Override
  public Stream<InvitationModel> getInvitationsStream() {
    /*
          public List<InvitationEntity> getInvitationsByRealmAndEmail(String realmName, String email) {
      TypedQuery<InvitationEntity> query =
          em.createNamedQuery("getInvitationsByRealmAndEmail", InvitationEntity.class);
      query.setParameter("realmId", realmName);
      query.setParameter("search", email);
      return query.getResultList();
    }
      */
    return org.getInvitations().stream().map(i -> new InvitationAdapter(session, realm, em, i));
  }

  @Override
  public InvitationModel getInvitation(String id) {
    InvitationEntity ie = em.find(InvitationEntity.class, id);
    if (ie != null && ie.getOrganization().equals(org)) {
      return new InvitationAdapter(session, realm, em, ie);
    } else {
      return null;
    }
  }

  @Override
  public void revokeInvitation(String id) {
    org.getInvitations().removeIf(inv -> inv.getId().equals(id));
  }

  @Override
  public void revokeInvitations(String email) {
    org.getInvitations().removeIf(inv -> inv.getEmail().equals(email.toLowerCase()));
  }

  @Override
  public InvitationModel addInvitation(String email, UserModel inviter) {
    InvitationEntity inv = new InvitationEntity();
    inv.setId(KeycloakModelUtils.generateId());
    inv.setOrganization(org);
    inv.setEmail(email.toLowerCase());
    inv.setInviterId(inviter.getId());
    em.persist(inv);
    org.getInvitations().add(inv);
    return new InvitationAdapter(session, realm, em, inv);
  }

  @Override
  public Stream<OrganizationRoleModel> getRolesStream() {
    return org.getRoles().stream()
        .map(r -> new OrganizationRoleAdapter(session, realm, em, this, r));
  }

  @Override
  public Stream<OrganizationRoleModel> getRolesByUserStream(UserModel user) {
    TypedQuery<UserOrganizationRoleMappingEntity> query =
        em.createNamedQuery("getMappingsByUser", UserOrganizationRoleMappingEntity.class);
    query.setParameter("userId", user.getId());
    query.setParameter("orgId", org.getId());
    try {
      return query
          .getResultStream()
          .map(r -> new OrganizationRoleAdapter(session, realm, em, this, r.getRole()));
    } catch (Exception ignore) {
      return null;
    }
  }

  @Override
  public OrganizationMemberModel getMembershipDetails(UserModel user) {
    TypedQuery<OrganizationMemberEntity> query =
        em.createNamedQuery("getOrganizationMemberByUserId", OrganizationMemberEntity.class);
    query.setParameter("organization", org);
    query.setParameter("userId", user.getId());
    try {
      OrganizationMemberEntity organizationMemberEntity = query.getSingleResult();
      return new OrganizationMemberAdapter(session, realm, em, organizationMemberEntity);
    } catch (Exception e) {
      return null;
    }
  }

  @Override
  public void removeRole(String name) {
    org.getRoles().removeIf(r -> r.getName().equals(name));
  }

  @Override
  public OrganizationRoleModel addRole(String name) {
    OrganizationRoleEntity r = new OrganizationRoleEntity();
    r.setId(KeycloakModelUtils.generateId());
    r.setName(name);
    r.setOrganization(org);
    em.persist(r);
    org.getRoles().add(r);
    return new OrganizationRoleAdapter(session, realm, em, this, r);
  }

  @Override
  public Stream<IdentityProviderModel> getIdentityProvidersStream() {
    return session
        .identityProviders()
        .getAllStream()
        // Todo: do we need to apply here a role filter? I believe not since its part of the
        // HomeIdpDiscoverer
        .filter(
            i -> {
              Map<String, String> config = i.getConfig();
              var orgs = IdentityProviders.getAttributeMultivalued(config, ORG_OWNER_CONFIG_KEY);
              return orgs.contains(getId());
            });
  }

  private Predicate[] getSearchOptionPredicateArray(
      String value, CriteriaBuilder builder, From<?, UserEntity> from) {
    value = value.trim().toLowerCase();
    List<Predicate> orPredicates = new ArrayList<>();
    if (!value.isEmpty()) {
      value = "%" + value + "%"; // contains in SQL query manner
      orPredicates.add(builder.like(from.get(USERNAME), value, ESCAPE_BACKSLASH));
      orPredicates.add(builder.like(from.get(EMAIL), value, ESCAPE_BACKSLASH));
      orPredicates.add(builder.like(builder.lower(from.get(FIRST_NAME)), value, ESCAPE_BACKSLASH));
      orPredicates.add(builder.like(builder.lower(from.get(LAST_NAME)), value, ESCAPE_BACKSLASH));
    }
    return orPredicates.toArray(Predicate[]::new);
  }
}
