package io.phasetwo.service.model;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.MoreCollectors;
import io.phasetwo.service.KeycloakSuite;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.extern.jbosslog.JBossLog;
import org.junit.ClassRule;
import org.junit.Test;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.representations.idm.RealmRepresentation;

@JBossLog
public class OrganizationProviderTest {

  @ClassRule public static KeycloakSuite server = KeycloakSuite.SERVER;

  void createRealm(String name) {
    RealmRepresentation realm = new RealmRepresentation();
    realm.setRealm(name);
    realm.setEnabled(true);
    server.client().realms().create(realm);
  }

  @Test
  public void testCreateOrganization() throws Exception {
    KeycloakSessionFactory factory = server.getKeycloak().getSessionFactory();
    KeycloakSession session = factory.create();
    createRealm("test");
    String id = null;
    String barid = null;

    // org foo in master
    session = factory.create();
    session.getTransactionManager().begin();
    try {
      RealmModel realm = session.realms().getRealmByName("master");
      UserModel user = session.users().getUserByUsername(realm, "admin");

      OrganizationProvider provider = session.getProvider(OrganizationProvider.class);
      OrganizationModel org = provider.createOrganization(realm, "foo", user, false);
      id = org.getId();
      org.setDisplayName("Foo Corp.");
      org.setDomains(ImmutableSet.of("foo.com"));
      org.setUrl("https://www.foo.com/bar");
      org.setSingleAttribute("single", "one");
      org.setAttribute("multiple", ImmutableList.of("one", "two", "three"));

      org.addInvitation("bar@foo.com", user);
      OrganizationRoleModel role = org.addRole("admins");
      role.grantRole(user);

      session.getTransactionManager().commit();
    } finally {
      session.close();
    }

    // org bar in test
    session = factory.create();
    session.getTransactionManager().begin();
    try {
      RealmModel realm = session.realms().getRealmByName("test");
      UserModel user = session.users().addUser(realm, "admin");

      OrganizationProvider provider = session.getProvider(OrganizationProvider.class);
      OrganizationModel org = provider.createOrganization(realm, "bar", user, false);
      barid = org.getId();
      org.setDomains(ImmutableSet.of("foo.com"));
      org.setUrl("https://www.foo.com/bar");
      org.setSingleAttribute("single", "one");
      org.setAttribute("multiple", ImmutableList.of("one", "two", "three"));

      org.addInvitation("bar@foo.com", user);
      OrganizationRoleModel role = org.addRole("admins");
      role.grantRole(user);

      session.getTransactionManager().commit();
    } finally {
      session.close();
    }

    // check org foo in master
    session = factory.create();
    session.getTransactionManager().begin();
    try {
      RealmModel realm = session.realms().getRealmByName("master");
      UserModel user = session.users().getUserByUsername(realm, "admin");
      OrganizationProvider provider = session.getProvider(OrganizationProvider.class);
      OrganizationModel org = provider.getOrganizationById(realm, id);
      assertNotNull(org);
      assertThat(org.getId(), is(id));
      assertThat(org.getName(), is("foo"));
      assertThat(org.getDomains().iterator().next(), is("foo.com"));
      assertThat(org.getUrl(), is("https://www.foo.com/bar"));

      assertThat(org.getFirstAttribute("single"), is("one"));
      assertThat(org.getFirstAttribute("multiple"), is("one"));
      assertTrue(org.getAttributes().get("multiple").contains("two"));
      assertTrue(org.getAttributes().get("multiple").contains("three"));

      assertThat(
          org.getInvitationsStream().collect(MoreCollectors.onlyElement()).getEmail(),
          is("bar@foo.com"));

      OrganizationRoleModel role = org.getRoleByName("admins");
      assertThat(role.getName(), is("admins"));
      assertTrue(role.hasRole(user));

      session.getTransactionManager().commit();
    } finally {
      session.close();
    }

    //  search with predicates
    session = factory.create();
    session.getTransactionManager().begin();
    try {
      RealmModel realm = session.realms().getRealmByName("master");
      UserModel user = session.users().getUserByUsername(realm, "admin");
      OrganizationProvider provider = session.getProvider(OrganizationProvider.class);
      Stream<OrganizationModel> orgs =
          provider.searchForOrganizationStream(
              realm, ImmutableMap.of("name", "FOO"), 0, 50, Optional.empty());
      OrganizationModel org = orgs.findFirst().get();
      assertNotNull(org);
      assertThat(org.getId(), is(id));

      orgs =
          provider.searchForOrganizationStream(
              realm, ImmutableMap.of("name", "fO"), 0, 50, Optional.empty());
      org = orgs.findFirst().get();
      assertNotNull(org);
      assertThat(org.getId(), is(id));

      orgs =
          provider.searchForOrganizationStream(
              realm, ImmutableMap.of("name", "Oo cORp"), 0, 50, Optional.empty());
      org = orgs.findFirst().get();
      assertNotNull(org);
      assertThat(org.getId(), is(id));

      orgs =
          provider.searchForOrganizationStream(
              realm, ImmutableMap.of("name", "foo"), 0, 50, Optional.empty());
      org = orgs.findFirst().get();
      assertNotNull(org);
      assertThat(org.getId(), is(id));
      session.getTransactionManager().commit();
    } finally {
      session.close();
    }

    // check no crossover realm domains
    session = factory.create();
    session.getTransactionManager().begin();
    try {
      RealmModel realm = session.realms().getRealmByName("master");
      OrganizationProvider provider = session.getProvider(OrganizationProvider.class);
      Stream<OrganizationModel> orgs =
          provider.getOrganizationsStreamForDomain(realm, "foo.com", false);
      assertThat(orgs.count(), is(1l));
      orgs = provider.getOrganizationsStreamForDomain(realm, "foo.com", false);
      assertThat(orgs.collect(MoreCollectors.onlyElement()).getName(), is("foo"));

      realm = session.realms().getRealmByName("test");
      orgs = provider.getOrganizationsStreamForDomain(realm, "foo.com", false);
      assertThat(orgs.count(), is(1l));
      orgs = provider.getOrganizationsStreamForDomain(realm, "foo.com", false);
      assertThat(orgs.collect(MoreCollectors.onlyElement()).getName(), is("bar"));

      session.getTransactionManager().commit();
    } finally {
      session.close();
    }

    // remove
    session = factory.create();
    session.getTransactionManager().begin();
    try {
      OrganizationProvider provider = session.getProvider(OrganizationProvider.class);

      RealmModel realm = session.realms().getRealmByName("master");
      boolean removed = provider.removeOrganization(realm, id);
      assertTrue(removed);

      realm = session.realms().getRealmByName("test");
      removed = provider.removeOrganization(realm, barid);
      assertTrue(removed);

      session.getTransactionManager().commit();
    } finally {
      session.close();
    }
  }
}
