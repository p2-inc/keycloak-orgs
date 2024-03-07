package io.phasetwo.service.auth;

import static io.phasetwo.service.Orgs.ORG_OWNER_CONFIG_KEY;
import static org.keycloak.events.EventType.IDENTITY_PROVIDER_POST_LOGIN;

import com.google.auto.service.AutoService;
import io.phasetwo.service.model.InvitationModel;
import io.phasetwo.service.model.OrganizationModel;
import io.phasetwo.service.model.OrganizationProvider;
import io.phasetwo.service.model.OrganizationRoleModel;
import java.util.Map;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.provider.ProviderEvent;

/** */
@JBossLog
@AutoService(AuthenticatorFactory.class)
public class OrgAddUserAuthenticatorFactory extends BaseAuthenticatorFactory
    implements DefaultAuthenticator {

  public static final String PROVIDER_ID = "ext-auth-org-add-user";

  public OrgAddUserAuthenticatorFactory() {
    super(PROVIDER_ID);
  }

  @Override
  public void authenticate(AuthenticationFlowContext context) {
    log.info("OrgAddUserAuthenticatorFactory.authenticate");
    addUser(context);
  }

  @Override
  public void action(AuthenticationFlowContext context) {
    log.info("OrgAddUserAuthenticatorFactory.authenticate");
  }

  private void addUser(AuthenticationFlowContext context) {
    PostOrgAuthFlow.setStatus(context);
    BrokeredIdentityContext brokerContext = PostOrgAuthFlow.getBrokeredIdentityContext(context);
    if (!PostOrgAuthFlow.brokeredIdpEnabled(context, brokerContext)) return;

    Map<String, String> idpConfig = brokerContext.getIdpConfig().getConfig();
    if (idpConfig != null && idpConfig.containsKey(ORG_OWNER_CONFIG_KEY)) {
      OrganizationProvider orgs = context.getSession().getProvider(OrganizationProvider.class);
      OrganizationModel org =
          orgs.getOrganizationById(context.getRealm(), idpConfig.get(ORG_OWNER_CONFIG_KEY));
      if (org == null) {
        log.infof(
            "idpConfig contained %s = %s, but org not found",
            ORG_OWNER_CONFIG_KEY, idpConfig.get(ORG_OWNER_CONFIG_KEY));
        return;
      }
      if (!org.hasMembership(context.getUser())) {
        log.infof(
            "granting membership to %s for user %s",
            org.getName(), context.getUser().getUsername());
        org.grantMembership(context.getUser());
        context
            .getEvent()
            .user(context.getUser())
            .detail("joined_organization", org.getId())
            .success();
        orgs.getUserInvitationsStream(context.getRealm(), context.getUser())
            .filter(
                invitationModel -> invitationModel.getOrganization().getId().equals(org.getId()))
            .forEach(
                invitationModel -> {
                  addRolesFromInvitation(invitationModel, context.getUser());

                  invitationModel.getOrganization().revokeInvitation(invitationModel.getId());
                  context
                      .getEvent()
                      .clone()
                      .event(IDENTITY_PROVIDER_POST_LOGIN)
                      .detail("org_id", invitationModel.getOrganization().getId())
                      .detail("invitation_id", invitationModel.getId())
                      .user(context.getUser())
                      .error("User invitation revoked.");
                });
      }
    } else {
      log.infof("No organization owns IdP %s", brokerContext.getIdpConfig().getAlias());
    }
  }

  void addRolesFromInvitation(InvitationModel invitation, UserModel user) {
    invitation
        .getRoles()
        .forEach(
            r -> {
              OrganizationRoleModel role = invitation.getOrganization().getRoleByName(r);
              if (role == null) {
                log.debugf("No org role found for invitation role %s. Skipping...", r);
              } else {
                role.grantRole(user);
              }
            });
  }

  @Override
  public boolean requiresUser() {
    return true;
  }

  @Override
  public Authenticator create(KeycloakSession session) {
    return this;
  }

  @Override
  public boolean isUserSetupAllowed() {
    return false;
  }

  @Override
  public String getHelpText() {
    return "Adds a user to an organization if an organization-owned IdP was used to log in. Use only in Post Login Flows.";
  }

  @Override
  public String getDisplayType() {
    return "Add User to Org";
  }

  @Override
  public String getReferenceCategory() {
    return "Post Broker";
  }

  @Override
  public void postInit(KeycloakSessionFactory factory) {
    factory.register(
        (ProviderEvent ev) -> {
          if (ev instanceof RealmModel.RealmPostCreateEvent) {
            PostOrgAuthFlow.realmPostCreate((RealmModel.RealmPostCreateEvent) ev, PROVIDER_ID);
          }
        });
  }
}
