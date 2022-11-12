package io.phasetwo.service.auth;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.authenticators.broker.AbstractIdpAuthenticator;
import org.keycloak.authentication.authenticators.broker.util.SerializedBrokeredIdentityContext;
import org.keycloak.broker.provider.BrokeredIdentityContext;

public class IdpAuthenticator extends AbstractIdpAuthenticator implements DefaultAuthenticator {

  @Override
  public void authenticateImpl(
      AuthenticationFlowContext context,
      SerializedBrokeredIdentityContext serializedCtx,
      BrokeredIdentityContext brokerContext) {}

  @Override
  public void actionImpl(
      AuthenticationFlowContext context,
      SerializedBrokeredIdentityContext serializedCtx,
      BrokeredIdentityContext brokerContext) {}
}
