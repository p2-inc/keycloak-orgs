package io.phasetwo.service.model.jpa;

import static org.keycloak.models.jpa.PaginationUtils.paginateQuery;
import static org.keycloak.utils.StreamsUtil.closing;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.net.InternetDomainName;
import io.phasetwo.service.model.InvitationModel;
import io.phasetwo.service.model.OrganizationModel;
import io.phasetwo.service.model.OrganizationProvider;
import io.phasetwo.service.model.jpa.entity.DomainEntity;
import io.phasetwo.service.model.jpa.entity.ExtOrganizationEntity;
import io.phasetwo.service.model.jpa.entity.InvitationEntity;
import io.phasetwo.service.model.jpa.entity.OrganizationAttributeEntity;
import io.phasetwo.service.model.jpa.entity.OrganizationMemberEntity;
import io.phasetwo.service.resource.OrganizationAdminAuth;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.KeycloakModelUtils;

public class JpaOrganizationProvider implements OrganizationProvider {

  protected final KeycloakSession session;
  protected final EntityManager em;

  public JpaOrganizationProvider(KeycloakSession session, EntityManager em) {
    this.session = session;
    this.em = em;
  }

  @Override
  public OrganizationModel createOrganization(
      RealmModel realm, String name, UserModel createdBy, boolean admin) {
    return createOrganization(realm, KeycloakModelUtils.generateId(), name, createdBy, admin);
  }

  @Override
  public OrganizationModel createOrganization(
      RealmModel realm, String id, String name, UserModel createdBy, boolean admin) {
    ExtOrganizationEntity e = new ExtOrganizationEntity();
    if (Strings.isNullOrEmpty(id)) {
      throw new IllegalArgumentException("id must be not null or empty");
    }
    e.setId(id);

    e.setRealmId(realm.getId());
    e.setName(name);
    e.setCreatedBy(createdBy.getId());
    em.persist(e);
    em.flush();
    OrganizationModel org = new OrganizationAdapter(session, realm, em, e);
    session.getKeycloakSessionFactory().publish(orgCreationEvent(realm, org));

    // creator if admin, but not a service account
    if (admin && createdBy.getServiceAccountClientLink() == null) {
      org.grantMembership(createdBy);
      for (String role : OrganizationAdminAuth.DEFAULT_ORG_ROLES) {
        org.getRoleByName(role).grantRole(createdBy);
      }
    }

    return org;
  }

  @Override
  public OrganizationModel getOrganizationById(RealmModel realm, String id) {
    ExtOrganizationEntity org = em.find(ExtOrganizationEntity.class, id);
    if (org != null && org.getRealmId().equals(realm.getId())) {
      return new OrganizationAdapter(session, realm, em, org);
    } else {
      return null;
    }
  }

  @Override
  public OrganizationModel getOrganizationByName(RealmModel realm, String name) {
    TypedQuery<ExtOrganizationEntity> query =
        em.createNamedQuery("getOrganizationsByRealmIdAndNameExact", ExtOrganizationEntity.class);
    query.setParameter("realmId", realm.getId());
    query.setParameter("name", name);
    try {
      ExtOrganizationEntity org = query.getSingleResult();
      return new OrganizationAdapter(session, realm, em, org);
    } catch (Exception e) {
      return null;
    }
  }

  @Override
  public Stream<OrganizationModel> getOrganizationsStreamForDomain(
      RealmModel realm, String domain, boolean verified) {
    domain = InternetDomainName.from(domain).toString();
    TypedQuery<DomainEntity> query =
        em.createNamedQuery(
            verified ? "getVerifiedDomainsByName" : "getDomainsByName", DomainEntity.class);
    query.setParameter("domain", domain);
    query.setParameter("realmId", realm.getId());
    if (verified) {
      query.setParameter("verified", verified);
    }
    return query
        .getResultStream()
        .map(de -> new OrganizationAdapter(session, realm, em, de.getOrganization()));
  }

  public static String createSearchString(String search) {
    if (Strings.isNullOrEmpty(search)) return "%";
    if (!search.startsWith("%")) search = "%" + search;
    if (!search.endsWith("%")) search = search + "%";
    return search;
  }

  @Override
  public Stream<OrganizationModel> getUserOrganizationsStream(RealmModel realm, UserModel user) {
    TypedQuery<OrganizationMemberEntity> query =
        em.createNamedQuery("getOrganizationMembershipsByUserId", OrganizationMemberEntity.class);
    query.setParameter("userId", user.getId());
    return query
        .getResultStream()
        .map(e -> new OrganizationAdapter(session, realm, em, e.getOrganization()));
  }

  @Override
  public Stream<OrganizationModel> searchForOrganizationStream(
      RealmModel realm,
      Map<String, String> attributes,
      Integer firstResult,
      Integer maxResults,
      Optional<UserModel> member) {
    if (attributes == null) {
      attributes = ImmutableMap.of();
    }
    CriteriaBuilder builder = em.getCriteriaBuilder();
    CriteriaQuery<ExtOrganizationEntity> queryBuilder =
        builder.createQuery(ExtOrganizationEntity.class);
    Root<ExtOrganizationEntity> root = queryBuilder.from(ExtOrganizationEntity.class);

    List<Predicate> predicates = attributePredicates(attributes, root);

    predicates.add(builder.equal(root.get("realmId"), realm.getId()));

    member.ifPresent(u -> predicates.add(memberPredicate(u, root)));

    queryBuilder.where(predicates.toArray(new Predicate[0])).orderBy(builder.asc(root.get("name")));

    TypedQuery<ExtOrganizationEntity> query = em.createQuery(queryBuilder);

    return closing(paginateQuery(query, firstResult, maxResults).getResultStream())
        .map(orgEntity -> getOrganizationById(realm, orgEntity.getId()))
        .filter(Objects::nonNull);
  }

  @Override
  public Long getOrganizationsCount(
      RealmModel realm, String search, Map<String, String> attributes) {
    if (attributes == null) {
      attributes = ImmutableMap.of();
    }

    CriteriaBuilder builder = em.getCriteriaBuilder();
    CriteriaQuery<Long> queryBuilder = builder.createQuery(Long.class);
    Root<ExtOrganizationEntity> root = queryBuilder.from(ExtOrganizationEntity.class);

    List<Predicate> predicates = attributePredicates(attributes, root);
    predicates.add(builder.equal(root.get("realmId"), realm.getId()));

    if (search != null && !search.trim().isEmpty()) {
      String searchPattern = "%" + search.toLowerCase() + "%";
      predicates.add(builder.like(builder.lower(root.get("name")), searchPattern));
    }

    queryBuilder.select(builder.count(root)).where(predicates.toArray(new Predicate[0]));
    TypedQuery<Long> query = em.createQuery(queryBuilder);
    return query.getSingleResult();
  }

  @Override
  public boolean removeOrganization(RealmModel realm, String id) {
    OrganizationModel org = getOrganizationById(realm, id);
    ExtOrganizationEntity e = em.find(ExtOrganizationEntity.class, id);
    em.remove(e);
    session.getKeycloakSessionFactory().publish(orgRemovedEvent(realm, org));
    em.flush();
    return true;
  }

  @Override
  public void removeOrganizations(RealmModel realm) {
    searchForOrganizationStream(realm, null, null, null, Optional.empty())
        .forEach(o -> removeOrganization(realm, o.getId()));
  }

  @Override
  public Stream<InvitationModel> getUserInvitationsStream(RealmModel realm, UserModel user) {
    return getUserInvitationsStream(realm, user.getEmail());
  }

  @Override
  public Stream<InvitationModel> getUserInvitationsStream(RealmModel realm, String email) {
    TypedQuery<InvitationEntity> query =
        em.createNamedQuery("getInvitationsByRealmAndEmail", InvitationEntity.class);
    query.setParameter("realmId", realm.getId());
    query.setParameter("search", email);

    return query.getResultStream().map(i -> new InvitationAdapter(session, realm, em, i));
  }

  @Override
  public InvitationModel getInvitationById(RealmModel realm, String id) {
    TypedQuery<InvitationEntity> query =
        em.createNamedQuery("getInvitationById", InvitationEntity.class);
    query.setParameter("realmId", realm.getId());
    query.setParameter("id", id);

    try {
      var entity = query.getSingleResult();
      if (entity != null) {
        return new InvitationAdapter(session, realm, em, entity);
      }
    } catch (PersistenceException ignore) {
    }
    return null;
  }

  @Override
  public void close() {}

  public OrganizationModel.OrganizationCreationEvent orgCreationEvent(
      RealmModel realm, OrganizationModel org) {
    return new OrganizationModel.OrganizationCreationEvent() {
      @Override
      public OrganizationModel getOrganization() {
        return org;
      }

      @Override
      public KeycloakSession getKeycloakSession() {
        return session;
      }

      @Override
      public RealmModel getRealm() {
        return realm;
      }
    };
  }

  public OrganizationModel.OrganizationRemovedEvent orgRemovedEvent(
      RealmModel realm, OrganizationModel org) {
    return new OrganizationModel.OrganizationRemovedEvent() {
      @Override
      public OrganizationModel getOrganization() {
        return org;
      }

      @Override
      public KeycloakSession getKeycloakSession() {
        return session;
      }

      @Override
      public RealmModel getRealm() {
        return realm;
      }
    };
  }

  private List<Predicate> attributePredicates(
      Map<String, String> attributes, Root<ExtOrganizationEntity> root) {
    CriteriaBuilder builder = em.getCriteriaBuilder();

    List<Predicate> predicates = new ArrayList<>();
    List<Predicate> attributePredicates = new ArrayList<>();

    for (Map.Entry<String, String> entry : attributes.entrySet()) {
      String key = entry.getKey();
      String value = entry.getValue();

      if (value == null) {
        continue;
      }

      if (key.equals("name")) {
        predicates.add(
            builder.or(
                builder.like(builder.lower(root.get("name")), "%" + value.toLowerCase() + "%"),
                builder.like(
                    builder.lower(root.get("displayName")), "%" + value.toLowerCase() + "%")));
      } else {
        Join<ExtOrganizationEntity, OrganizationAttributeEntity> attributesJoin =
            root.join("attributes", JoinType.LEFT);

        attributePredicates.add(
            builder.and(
                builder.equal(builder.lower(attributesJoin.get("name")), key.toLowerCase()),
                builder.equal(builder.lower(attributesJoin.get("value")), value.toLowerCase())));
      }
    }

    if (!attributePredicates.isEmpty()) {
      predicates.add(builder.and(attributePredicates.toArray(new Predicate[0])));
    }

    return predicates;
  }

  private Predicate memberPredicate(UserModel member, Root<ExtOrganizationEntity> root) {
    CriteriaBuilder builder = em.getCriteriaBuilder();

    Join<ExtOrganizationEntity, OrganizationMemberEntity> membersJoin =
        root.join("members", JoinType.LEFT);

    return builder.equal(membersJoin.get("userId"), member.getId());
  }
}
