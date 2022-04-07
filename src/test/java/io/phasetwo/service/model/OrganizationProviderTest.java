package io.phasetwo.service.model;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.MoreCollectors;
import io.phasetwo.service.KeycloakSuite;
import lombok.extern.jbosslog.JBossLog;
import org.junit.ClassRule;
import org.junit.Test;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

@JBossLog
public class OrganizationProviderTest {

  @ClassRule public static KeycloakSuite server = KeycloakSuite.SERVER;

  @Test
  public void testCreateOrganization() throws Exception {
    KeycloakSessionFactory factory = server.getKeycloak().getSessionFactory();
    KeycloakSession session = factory.create();
    String id = null;

    session.getTransactionManager().begin();
    try {
      RealmModel realm = session.realms().getRealmByName("master");
      UserModel user = session.users().getUserByUsername(realm, "admin");

      OrganizationProvider provider = session.getProvider(OrganizationProvider.class);
      OrganizationModel org = provider.createOrganization(realm, "foo", user);
      id = org.getId();
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
  }
}
