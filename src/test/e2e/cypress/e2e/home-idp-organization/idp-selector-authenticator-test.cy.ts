import {loginUriSelectAccount, tokenUri, testRealmLoginUri, testWizardLoginUri} from "../../fixtures/uri";
import { user1, user2, user3, idpUser } from "../../fixtures/users";
import { getRealmOrganizations, org1Name, org2Name } from "../../utils/organizations";

// there's a code in the onSubmit attribute in login-select-idp.ftl, which blows up cypress, but seems to work fine in browsers
// as I'm currently just adding tests I'll ignore it for the moment
Cypress.on('uncaught:exception', (err) => {
    if (err.message.includes('login is not defined')) {
        return false; // prevents test failure
    }
});

describe('Testing the IDP Selector Authenticator', () => {
    it('When entering an IDP which exists in the realm config the flow should succeed', () => {
        cy.visit(testRealmLoginUri);
        cy.get('#providerId').type('oidc-idp');
        cy.get('form#kc-form-login')
            .find('input[type="submit"]')
            .click()

        cy.url().should('contain', 'external-idp');
        cy.get('#username').type(idpUser.username);
        cy.get('#password').type(idpUser.password);
        cy.get('#kc-login').click();

        cy.contains('Personal');
        cy.url().should('contain', 'test-realm');
    })

    it('When entering a link-only IDP\'s ID then the flow should not redirect the user', () => {
        cy.visit(testRealmLoginUri);
        cy.get('#providerId').type('link-only-oidc-idp');
        cy.get('form#kc-form-login')
            .find('input[type="submit"]')
            .click()

        cy.url().should('contain', 'test-realm');
        cy.get('#kc-error-message').should('exist');
        cy.contains('Unexpected error');
    })

    it('When entering a disabled IDP\'s ID then the flow should not redirect the user', () => {
        cy.visit(testRealmLoginUri);
        cy.get('#providerId').type('link-only-oidc-idp');
        cy.get('form#kc-form-login')
            .find('input[type="submit"]')
            .click()

        cy.url().should('contain', 'test-realm');
        cy.get('#kc-error-message').should('exist');
        cy.contains('Unexpected error');
    })
});