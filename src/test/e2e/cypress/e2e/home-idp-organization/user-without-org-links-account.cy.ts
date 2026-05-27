import {testRealmLoginUri, testRealmUri} from "../../fixtures/uri";
import { organizationlessAuthItUser } from "../../fixtures/users";

describe('Organiationless IDP Linked user login', () => {
    // using force: true, cause flakyness https://github.com/cypress-io/cypress/issues/5830
    it('a user registers their own idp & logs in with that', () => {
        cy.visit(testRealmLoginUri);
        cy.task('log', `Visited the login URL 1st time`)
        cy.get('#username').type(organizationlessAuthItUser.username, {force: true});
        cy.task('log', `Username typed in`)
        cy.get('#kc-login').click();
        cy.get('#password').type(organizationlessAuthItUser.password, {force: true});
        cy.task('log', `Password typed in`)
        cy.get('#kc-login').click();
        cy.contains('Personal');
        cy.get("#nav-toggle").click();
        cy.task('log', `Nav-toggle clicked`)

        cy.get('[data-testid="accountSecurity"]').click();
        cy.get('[data-testid="account-security/linked-accounts"]').click();
        cy.task('log', `Navigated to linked accounts page`)
        cy.url().should('contain', 'account-security/linked-accounts');

        cy.get('[data-testid="linked-accounts/oidc-idp"]')
            .find('button')
            .click()
        cy.task('log', `OIDC-IDP linking initiated, button clicked`)

        cy.contains('Linking oidc-idp')
        cy.get('#kc-continue').click();

        cy.url().should('contain', 'external-idp');

        cy.get('#username').should('be.visible').type(organizationlessAuthItUser.username, {force: true});
        cy.task('log', `external-idp username typed in`)
        cy.get('#password').should('be.visible').type(organizationlessAuthItUser.password, {force: true});
        cy.task('log', `external-idp password typed in`)
        cy.get('#kc-login').click();

        cy.url().should('contain', 'test-realm/account');

        cy.get('[data-testid="options-toggle"]').click();
        cy.get('[data-testid="options"]')
            .contains('button', 'Sign out')
            .click()

        cy.clearCookies()

        cy.visit(testRealmLoginUri);
        cy.task('log', `Verifying user can log in with external IDP`)
        cy.get('#username').type(organizationlessAuthItUser.username, {force: true});
        cy.task('log', `Username typed in`)
        cy.get('#kc-login').click();
        // after linking the account to the user on the next log-in we are automatically redirected to the external idp's login page
        cy.url().should('contain', 'external-idp');
    })
});
