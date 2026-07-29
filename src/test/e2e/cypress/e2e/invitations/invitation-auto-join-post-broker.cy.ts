/**
 * E2E: invitation auto-join on post-broker login
 *
 * Verifies that ext-inv-auto-join, when added to a post-broker-login flow, silently
 * grants membership for any pending organization invitation matching the federated
 * user's email and revokes the invitation -- without showing the "accept invitations"
 * challenge screen (invitations.ftl) that the InvitationRequiredAction flow uses.
 *
 * Setup (done by CypressInvitationAutoJoinPostBrokerTest.java):
 *   - idp-test-user (test@phasetwo.io) has a pending invitation to org-2 and is a member of
 *     no orgs before login
 *   - oidc-idp is linked to org-1 with postBrokerLoginFlowAlias =
 *     "custom-post-broker-login-inv-auto-join", a flow that adds ext-inv-auto-join after the
 *     default org bookkeeping steps -- without it, ext-inv-auto-join never runs post-broker
 *   - org-2 is not linked to oidc-idp, so its membership can only come from ext-inv-auto-join
 *
 * Membership grant + invitation revocation are verified via REST by the JUnit test after this
 * spec runs -- this spec only asserts on the browser-visible behaviour of the login itself.
 */

import { testRealmLoginUri } from "../../fixtures/uri";

const idpUser = {
  username: "test@phasetwo.io",
  password: "test123",
  email: "test@phasetwo.io",
};

const loginViaIdp = (loginUrl: string) => {
  cy.visit(loginUrl);
  // home-IdP discovery: type email → discovered → redirect to external IdP
  cy.get("#username").type(idpUser.email);
  cy.get("#kc-login").click();
  // now on external-idp login page
  cy.url().should("include", "external-idp");
  cy.get("#username").type(idpUser.username);
  cy.get("#password").type(idpUser.password);
  cy.get("#kc-login").click();
};

beforeEach(() => {
  cy.clearCookies();
});

describe("invitation auto-join on post-broker login", () => {
  it("silently joins the invited org and lands on the account page", () => {
    loginViaIdp(testRealmLoginUri);

    // no "accept invitations" challenge -- that's InvitationRequiredAction's UI, not this flow
    cy.get("#kc-accept").should("not.exist");
    cy.contains("You have been invited").should("not.exist");

    // post-broker flow ran to completion, landing directly on the account page
    cy.url().should("include", "/account");
    cy.contains("Personal");
  });
});