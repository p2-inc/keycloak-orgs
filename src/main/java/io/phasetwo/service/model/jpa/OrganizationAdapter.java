package io.phasetwo.service.model.jpa;

import static io.phasetwo.service.Orgs.*;

import com.google.common.base.Strings;
import io.phasetwo.service.model.DomainModel;
import io.phasetwo.service.model.InvitationModel;
import io.phasetwo.service.model.OrganizationModel;
import io.phasetwo.service.model.OrganizationRoleModel;
import io.phasetwo.service.model.jpa.entity.DomainEntity;
import io.phasetwo.service.model.jpa.entity.InvitationEntity;
import io.phasetwo.service.model.jpa.entity.OrganizationAttributeEntity;
import io.phasetwo.service.model.jpa.entity.OrganizationEntity;
import io.phasetwo.service.model.jpa.entity.OrganizationMemberEntity;
import io.phasetwo.service.model.jpa.entity.OrganizationRoleEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.jpa.JpaModel;
import org.keycloak.models.utils.KeycloakModelUtils;

public class OrganizationAdapter implements OrganizationModel, JpaModel<OrganizationEntity> {

  protected final KeycloakSession session;
  protected final OrganizationEntity org;
  protected final EntityManager em;
  protected final RealmModel realm;

  public OrganizationAdapter(
      KeycloakSession session, RealmModel realm, EntityManager em, OrganizationEntity org) {
    this.session = session;
    this.em = em;
    this.org = org;
    this.realm = realm;
  }

  @Override
  public OrganizationEntity getEntity() {
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
    Set<String> lower = domains.stream().map(d -> d.toLowerCase()).collect(Collectors.toSet());
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

  @Override
  public Stream<UserModel> searchForMembersStream(
      String search, Integer firstResult, Integer maxResults) {
    // TODO this could be optimized for large member lists with a query
    return getMembersStream()
        .filter(
            (m) -> {
              if (Strings.isNullOrEmpty(search)) return true;
              return (m.getEmail() != null && m.getEmail().toLowerCase().contains(search))
                  || (m.getUsername() != null && m.getUsername().toLowerCase().contains(search))
                  || (m.getFirstName() != null && m.getFirstName().toLowerCase().contains(search))
                  || (m.getLastName() != null && m.getLastName().toLowerCase().contains(search));
            })
        .skip(firstResult)
        .limit(maxResults);
  }

  @Override
  public Long getMembersCount() {
    TypedQuery<Long> query = em.createNamedQuery("getOrganizationMembersCount", Long.class);
    query.setParameter("organization", org);
    return query.getSingleResult();
  }

  @Override
  public Stream<UserModel> getMembersStream() {
    return org.getMembers().stream()
        .map(m -> m.getUserId())
        .map(uid -> session.users().getUserById(realm, uid))
        .filter(u -> u != null && u.getServiceAccountClientLink() == null);
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
    return org.getRoles().stream().map(r -> new OrganizationRoleAdapter(session, realm, em, r));
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
    return new OrganizationRoleAdapter(session, realm, em, r);
  }

  @Override
  public Stream<IdentityProviderModel> getIdentityProvidersStream() {
    return getRealm()
        .getIdentityProvidersStream()
        .filter(
            i -> {
              Map<String, String> config = i.getConfig();
              return config != null
                  && config.containsKey(ORG_OWNER_CONFIG_KEY)
                  && getId().equals(config.get(ORG_OWNER_CONFIG_KEY));
            });
  }
}
