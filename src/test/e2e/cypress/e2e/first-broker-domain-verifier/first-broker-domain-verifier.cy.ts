/**
 * E2E: GHSA-gvcc-4vw2-6wvr — an organization IdP must not create (or let a member create)
 * accounts for email addresses outside the organization's domains.
 *
 * Setup (CypressFirstBrokerDomainVerifierTest.java):
 *   - test-realm, org "acme" owns domain phasetwo.io
 *   - external-idp realm is the upstream OIDC IdP: test@phasetwo.io (in-domain) and
 *     test@auth.it (out-of-domain), both test123
 *   - org-idp links external-idp to acme, syncMode=FORCE, realm default "first broker login"
 *     flow (ext-auth-org-domain-fbl-verifier auto-wired in), Review Profile forced on
 *
 * The verifier runs after idp-review-profile and before idp-create-user-if-unique, so it blocks
 * both an IdP-supplied out-of-domain email and an email edited to an out-of-domain value on the
 * Review Profile page, while leaving in-domain first broker logins working.
 */
import { testRealmUri } from "../../fixtures/uri";

const authBase = Cypress.config("baseUrl");
const loginViaOrgIdp =
  testRealmUri +
  "/protocol/openid-connect/auth" +
  "?response_type=code&client_id=account&scope=openid" +
  "&redirect_uri=" +
  testRealmUri +
  "/account" +
  "&kc_idp_hint=org-idp";

const masterTokenUri =
  authBase + "realms/master/protocol/openid-connect/token";
const testRealmUsersUri = authBase + "admin/realms/test-realm/users";

const adminToken = () =>
  cy
    .request({
      method: "POST",
      url: masterTokenUri,
      form: true,
      body: {
        client_id: "admin-cli",
        grant_type: "password",
        username: "admin",
        password: "admin",
      },
    })
    .then((res) => {
      expect(res.status).to.eq(200);
      return res.body.access_token as string;
    });

const usersWithEmail = (email: string) =>
  adminToken().then((token) =>
    cy
      .request({
        method: "GET",
        url: `${testRealmUsersUri}?email=${encodeURIComponent(email)}&exact=true`,
        headers: { Authorization: `Bearer ${token}` },
      })
      .then((res) => {
        expect(res.status).to.eq(200);
        return res.body as Array<{ id: string; email: string }>;
      })
  );

// Log in at the upstream IdP, then (because Review Profile is forced on) land on the
// "Update Account Information" page. Optionally overwrite the email before submitting.
const brokerLogin = (upstreamUser: string, reviewEmail?: string) => {
  cy.visit(loginViaOrgIdp);
  // redirected to the external-idp login page
  cy.url().should("include", "external-idp");
  cy.get("#username").type(upstreamUser);
  cy.get("#password").type("test123");
  cy.get("#kc-login").click();
  // Review Profile ("Update Account Information") page — #email is present here
  cy.get("#email", { timeout: 10000 }).should("exist");
  if (reviewEmail !== undefined) {
    cy.get("#email").clear().type(reviewEmail);
  }
  cy.get("#kc-form-buttons input[type='submit']").click();
};

beforeEach(() => {
  cy.clearCookies();
});

describe("org IdP first-broker domain verification (GHSA-gvcc-4vw2-6wvr)", () => {
  it("blocks an IdP-supplied out-of-domain email and creates no account", () => {
    brokerLogin("test@auth.it");

    // login was rejected with an error page ...
    cy.get("#kc-error-message").should("exist");
    // ... and no user was created in the target realm
    usersWithEmail("test@auth.it").should("have.length", 0);
  });

  it("blocks editing the email to an out-of-domain address on Review Profile", () => {
    brokerLogin("test@phasetwo.io", "attacker@auth.it");

    cy.get("#kc-error-message").should("exist");
    usersWithEmail("attacker@auth.it").should("have.length", 0);
    // the in-domain identity was not created either — the whole login was rejected
    usersWithEmail("test@phasetwo.io").should("have.length", 0);
  });

  it("allows an in-domain first broker login to create the account", () => {
    brokerLogin("test@phasetwo.io");

    // the account was created in the target realm
    usersWithEmail("test@phasetwo.io").should("have.length", 1);
  });
});
