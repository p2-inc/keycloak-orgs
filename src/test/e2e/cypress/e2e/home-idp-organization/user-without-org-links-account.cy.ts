import {testRealmLoginUri, testRealmUri} from "../../fixtures/uri";
import { organizationlessAuthItUser } from "../../fixtures/users";

describe('Organiationless IDP Linked user login', () => {
    it('a user registers their own idp & logs in with that', () => {
        cy.visit(testRealmLoginUri);
        cy.get('#username').type(organizationlessAuthItUser.username);
        cy.get('#kc-login').click();
        cy.get('#password').type(organizationlessAuthItUser.password);
        cy.get('#kc-login').click();
        cy.contains('Personal');
        cy.get("#nav-toggle").click();

        cy.get('[data-testid="accountSecurity"]').click();
        cy.get('[data-testid="account-security/linked-accounts"]').click();
        cy.url().should('contain', 'account-security/linked-accounts');

        cy.get('[data-testid="linked-accounts/oidc-idp"]')
            .find('button')
            .click()

        cy.contains('Linking oidc-idp')
        cy.get('#kc-continue').click();

        cy.url().should('contain', 'external-idp');

        cy.get('#username').type(organizationlessAuthItUser.username);
        cy.get('#password').type(organizationlessAuthItUser.password);
        cy.get('#kc-login').click();

        cy.url().should('contain', 'test-realm/account');

        cy.get('[data-testid="options-toggle"]').click();
        cy.get('[data-testid="options"]')
            .contains('button', 'Sign out')
            .click()

        cy.clearCookies()

        cy.visit(testRealmLoginUri);
        cy.get('#username').type(organizationlessAuthItUser.username);
        cy.get('#kc-login').click();
        // after linking the account to the user on the next log-in we are automatically redirected to the external idp's login page
        cy.url().should('contain', 'external-idp');
    })
});
