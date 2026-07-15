/**
 * E2E: account_hint with post-broker login
 *
 * Verifies that account_hint=<org-id> passed in the original OIDC auth request
 * survives the IdP round-trip and auto-selects the correct org during post-broker
 * flow execution, skipping the org-picker UI.
 *
 * Setup (done by CypressAccountHintPostBrokerTest.java):
 *   - idp-test-user (test@phasetwo.io) is member of org-1 and org-2
 *   - oidc-idp is linked to org-1 with postBrokerLoginFlowAlias = "custom-post-broker-login",
 *     a flow that adds ext-select-org after the default org bookkeeping steps -- without it,
 *     ext-select-org never runs post-broker and account_hint is never evaluated
 */

import { testRealmLoginUri, loginUriAccountHint } from "../../fixtures/uri";
import { getRealmOrganizations, org1Name, org2Name } from "../../utils/organizations";

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

describe("account_hint with post-broker login — org auto-selection", () => {
  it("account_hint=org1 skips org picker and activates org-1", () => {
    getRealmOrganizations().then((orgs) => {
      const org1 = orgs.find((o) => o.name === org1Name);

      loginViaIdp(loginUriAccountHint + org1.id);

      // post-broker flow ran: account_hint matched → no picker shown
      cy.get('[data-cy="select-org-label"]').should("not.exist");
      cy.contains("Personal");

      // verify org-1 is now the active org
      cy.validateActiveOrganization(idpUser, org1Name);
    });
  });

  it("account_hint=org2 skips org picker and activates org-2", () => {
    getRealmOrganizations().then((orgs) => {
      const org2 = orgs.find((o) => o.name === org2Name);

      loginViaIdp(loginUriAccountHint + org2.id);

      cy.get('[data-cy="select-org-label"]').should("not.exist");
      cy.contains("Personal");

      cy.validateActiveOrganization(idpUser, org2Name);
    });
  });
});

describe("account_hint with post-broker login — fallback behaviour", () => {
  it("no account_hint and user in 2 orgs shows org picker after IdP login", () => {
    loginViaIdp(testRealmLoginUri);

    // post-broker flow ran: no hint → picker shown because user has 2 orgs
    cy.get('[data-cy="select-org-label"]').should("exist");
    cy.get('[data-cy="select-org-options"]').should("have.length", 2);
  });

  it("invalid account_hint returns invalidOrganizationError after IdP login", () => {
    loginViaIdp(loginUriAccountHint + "non-existent-org-id");

    cy.get("#kc-error-message").should("exist");
    cy.contains("Invalid");
  });
});
