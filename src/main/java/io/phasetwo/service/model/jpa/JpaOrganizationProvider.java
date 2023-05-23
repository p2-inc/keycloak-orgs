package io.phasetwo.service.model.jpa;

import com.google.common.base.Strings;
import com.google.common.net.InternetDomainName;
import io.phasetwo.service.model.InvitationModel;
import io.phasetwo.service.model.OrganizationModel;
import io.phasetwo.service.model.OrganizationProvider;
import io.phasetwo.service.model.jpa.entity.DomainEntity;
import io.phasetwo.service.model.jpa.entity.InvitationEntity;
import io.phasetwo.service.model.jpa.entity.OrganizationEntity;
import io.phasetwo.service.model.jpa.entity.OrganizationMemberEntity;
import io.phasetwo.service.resource.OrganizationAdminAuth;
import java.util.stream.Stream;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
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
    OrganizationEntity e = new OrganizationEntity();
    e.setId(KeycloakModelUtils.generateId());
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
    OrganizationEntity org = em.find(OrganizationEntity.class, id);
    if (org != null && org.getRealmId().equals(realm.getId())) {
      return new OrganizationAdapter(session, realm, em, org);
    } else {
      return null;
    }
  }

  @Override
  public Stream<OrganizationModel> getOrganizationsStream(
      RealmModel realm, Integer firstResult, Integer maxResults) {
    return searchForOrganizationByNameStream(realm, "%", firstResult, maxResults);
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

  @Override
  public Stream<OrganizationModel> searchForOrganizationByNameStream(
      RealmModel realm, String search, Integer firstResult, Integer maxResults) {
    TypedQuery<OrganizationEntity> query =
        em.createNamedQuery("getOrganizationsByRealmIdAndName", OrganizationEntity.class);
    query.setParameter("realmId", realm.getId());
    search = createSearchString(search);
    query.setParameter("search", search);
    if (firstResult != null) query.setFirstResult(firstResult);
    if (maxResults != null) query.setMaxResults(maxResults);
    return query.getResultStream().map(e -> new OrganizationAdapter(session, realm, em, e));
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
    query.setParameter("id", user.getId());
    return query
        .getResultStream()
        .map(e -> new OrganizationAdapter(session, realm, em, e.getOrganization()));
  }

  @Override
  public Long getOrganizationsCount(RealmModel realm, String search) {
    TypedQuery<Long> query = em.createNamedQuery("countOrganizationsByRealmIdAndName", Long.class);
    query.setParameter("realmId", realm.getId());
    search = createSearchString(search);
    query.setParameter("search", search);
    return query.getSingleResult();
  }

  @Override
  public boolean removeOrganization(RealmModel realm, String id) {
    OrganizationModel org = getOrganizationById(realm, id);
    OrganizationEntity e = em.find(OrganizationEntity.class, id);
    em.remove(e);
    session.getKeycloakSessionFactory().publish(orgRemovedEvent(realm, org));
    em.flush();
    return true;
  }

  @Override
  public void removeOrganizations(RealmModel realm) {
    getOrganizationsStream(realm).forEach(o -> removeOrganization(realm, o.getId()));
  }

  @Override
  public Stream<InvitationModel> getUserInvitationsStream(RealmModel realm, UserModel user) {
    TypedQuery<InvitationEntity> query =
        em.createNamedQuery("getInvitationsByRealmAndEmail", InvitationEntity.class);
    query.setParameter("realmId", realm.getId());
    query.setParameter("search", user.getEmail());
    return query.getResultStream().map(i -> new InvitationAdapter(session, realm, em, i));
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
}
