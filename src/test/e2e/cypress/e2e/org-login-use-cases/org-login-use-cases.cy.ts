/**
 * E2E: organization-aware login for a multi-tenant store platform.
 *
 * Every store is an organization (globex, initech, acme). The app sends the store it is opening
 * as account_hint, and a user gets in only as a member of that store or with a pending
 * invitation to it, whatever the login method.
 *
 * Setup is done by CypressOrgLoginUseCasesTest.java. Passwords are "test123" in both realms.
 *
 * The active organization is read from the user's org.ro.active attribute through the admin
 * API, so the check works for identity-provider users that have no password.
 */

import { testRealmUri, testRealmLoginUri, tokenUri } from "../../fixtures/uri";

const PASSWORD = "test123";
const INVALID = "Invalid Organization";
const NO_ORG = "not part of any organization";
const adminUsersUri = testRealmUri.replace("/realms/test-realm", "/admin/realms/test-realm/users");

const authUri = (params: Record<string, string>) =>
  testRealmLoginUri + Object.entries(params).map(([k, v]) => `&${k}=${encodeURIComponent(v)}`).join("");

const adminToken = () =>
  cy
    .request({
      method: "POST",
      url: tokenUri,
      form: true,
      body: { client_id: "admin-cli", grant_type: "password", username: "admin", password: "admin" },
    })
    .then((res) => res.body.access_token as string);

const orgId = (name: string) =>
  adminToken().then((token) =>
    cy
      .request({ url: `${testRealmUri}/orgs?search=${name}`, headers: { Authorization: `Bearer ${token}` } })
      .then((res) => res.body.find((o: { name: string }) => o.name === name).id as string),
  );

const activeOrgOf = (email: string) =>
  adminToken().then((token) =>
    cy
      .request({
        url: `${adminUsersUri}?email=${encodeURIComponent(email)}&exact=true`,
        headers: { Authorization: `Bearer ${token}` },
      })
      .then((res) => (res.body[0]?.attributes?.["org.ro.active"] || [])[0] as string | undefined),
  );

const passwordLogin = (email: string) => {
  cy.get("#username").type(email);
  cy.get("#kc-login").click();
  cy.get("#password").type(PASSWORD);
  cy.get("#kc-login").click();
};

const upstreamLogin = (email: string) => {
  cy.url().should("include", "/realms/shophost-upstream/");
  cy.get("#username").type(email);
  cy.get("#password").type(PASSWORD);
  cy.get("#kc-login").click();
};

const companySsoLogin = (email: string) => {
  cy.get("#username").type(email);
  cy.get("#kc-login").click();
  upstreamLogin(email);
};

const expectSignedIn = (email: string, store: string) => {
  cy.url().should("include", "/realms/test-realm/account");
  orgId(store).then((id) => activeOrgOf(email).should("eq", id));
};

const expectRefused = (message: string) => {
  cy.get("#kc-error-message").should("contain", message);
  // the error is rendered at the authorize URL, whose redirect_uri names the account page,
  // so only the path tells whether the user reached the app
  cy.location("pathname").should("not.include", "/realms/test-realm/account");
};

const expectNoSessionLeft = () => {
  cy.visit(testRealmLoginUri);
  cy.get("#username").should("exist");
};

beforeEach(() => {
  cy.clearCookies();
});

describe("use case 1: account_hint=globex across login methods", () => {
  it("password: member gets in", () => {
    cy.visit(authUri({ account_hint: "globex" }));
    passwordLogin("paula@mail.test");
    expectSignedIn("paula@mail.test", "globex");
  });

  it("password: invited user gets in and the invitation becomes a membership", () => {
    cy.visit(authUri({ account_hint: "globex" }));
    passwordLogin("ivan@mail.test");
    expectSignedIn("ivan@mail.test", "globex");
    adminToken().then((token) =>
      orgId("globex").then((id) =>
        cy
          .request({ url: `${testRealmUri}/orgs/${id}/invitations`, headers: { Authorization: `Bearer ${token}` } })
          .then((res) => {
            const pending = res.body.filter((i: { email: string }) => i.email === "ivan@mail.test");
            expect(pending).to.have.length(0);
          }),
      ),
    );
  });

  it("password: neither member nor invited is refused, and no session is left", () => {
    cy.visit(authUri({ account_hint: "globex" }));
    passwordLogin("nina@mail.test");
    expectRefused(INVALID);
    expectNoSessionLeft();
  });

  it("social: invited user gets in", () => {
    cy.visit(authUri({ account_hint: "globex", kc_idp_hint: "google" }));
    upstreamLogin("sam@gmail.test");
    expectSignedIn("sam@gmail.test", "globex");
  });

  it("social: uninvited user is refused, and no session is left", () => {
    cy.visit(authUri({ account_hint: "globex", kc_idp_hint: "google" }));
    upstreamLogin("zoe@gmail.test");
    expectRefused(INVALID);
    expectNoSessionLeft();
  });

  it("company SSO: Globex employee is routed to Globex SSO and joins globex", () => {
    cy.visit(authUri({ account_hint: "globex" }));
    companySsoLogin("erin@globex.test");
    expectSignedIn("erin@globex.test", "globex");
  });

  it("company SSO: Acme employee asking for globex is refused", () => {
    cy.visit(authUri({ account_hint: "globex" }));
    companySsoLogin("ann@acme.test");
    expectRefused(INVALID);
    expectNoSessionLeft();
  });
});

describe("use case 2: contractor switching stores", () => {
  it("picks a store, switches on the session, and is refused for a store he is not in", () => {
    cy.visit(authUri({ prompt: "select_account" }));
    passwordLogin("carl@contractor.test");
    cy.get('[data-cy="select-org-options"]').should("have.length", 2);
    cy.get('[data-cy="select-org-input"]').select("initech");
    cy.get('[data-cy="submit"]').click();
    expectSignedIn("carl@contractor.test", "initech");

    // same session, no password prompt
    cy.visit(authUri({ account_hint: "globex" }));
    expectSignedIn("carl@contractor.test", "globex");

    cy.visit(authUri({ prompt: "select_account" }));
    cy.get('[data-cy="select-org-options"]').should("have.length", 2);
    cy.get("#password").should("not.exist");

    cy.visit(authUri({ account_hint: "acme" }));
    expectRefused(INVALID);
  });

  it("switch-organization API switches to his stores only", () => {
    cy.request({
      method: "POST",
      url: `${testRealmUri}/protocol/openid-connect/token`,
      form: true,
      body: { client_id: "store-admin-api", grant_type: "password", username: "carl@contractor.test", password: PASSWORD },
    }).then((res) => {
      const token = res.body.access_token as string;
      orgId("initech").then((id) =>
        cy
          .request({
            method: "PUT",
            url: `${testRealmUri}/users/switch-organization`,
            headers: { Authorization: `Bearer ${token}` },
            body: { id },
          })
          .its("status")
          .should("eq", 200),
      );
      orgId("acme").then((id) =>
        cy
          .request({
            method: "PUT",
            url: `${testRealmUri}/users/switch-organization`,
            headers: { Authorization: `Bearer ${token}` },
            body: { id },
            failOnStatusCode: false,
          })
          .its("status")
          .should("eq", 401),
      );
    });
  });
});

describe("use case 3: kc_idp_hint plus account_hint", () => {
  it("partner member of globex gets in", () => {
    cy.visit(authUri({ account_hint: "globex", kc_idp_hint: "partner" }));
    upstreamLogin("pete@partner.test");
    expectSignedIn("pete@partner.test", "globex");
  });

  it("partner user who is not a member is refused", () => {
    cy.visit(authUri({ account_hint: "globex", kc_idp_hint: "partner" }));
    upstreamLogin("quinn@partner.test");
    expectRefused(INVALID);
    expectNoSessionLeft();
  });

  it("globex member asking for initech is refused", () => {
    cy.visit(authUri({ account_hint: "initech", kc_idp_hint: "partner" }));
    upstreamLogin("pete@partner.test");
    expectRefused(INVALID);
  });
});

describe("use case 4: refusals", () => {
  it("a store that does not exist is refused", () => {
    cy.visit(authUri({ account_hint: "no-such-store" }));
    passwordLogin("paula@mail.test");
    expectRefused(INVALID);
    expectNoSessionLeft();
  });

  it("a user with no store asked to pick is told so", () => {
    cy.visit(authUri({ prompt: "select_account" }));
    passwordLogin("nina@mail.test");
    expectRefused(NO_ORG);
    expectNoSessionLeft();
  });
});
