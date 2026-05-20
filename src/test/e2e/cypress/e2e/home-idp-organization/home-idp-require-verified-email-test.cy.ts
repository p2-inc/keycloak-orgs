import { testRealmLoginUri } from "../../fixtures/uri";
import { idpUser, unverifiedIdpUser, internetDomainNameError } from "../../fixtures/users";

describe('Logging in into the Account client when the HomeIdpDiscovery requires verified email', () => {
    it('user with verified email tries to log in, and redirected to the relevant realm', () => {
        cy.visit(testRealmLoginUri);
        cy.get('#username').type(idpUser.username);
        cy.get('#kc-login').click();

        cy.url().should('contain', 'external-idp');
        cy.get('#username').type(idpUser.username);
        cy.get('#password').type(idpUser.password);
        cy.get('#kc-login').click();

        // the user exists in the realm, but no IDP link is set up for them;
        // But at this point they are done with the HomeIDP authentication flow, we won't test it any further
        cy.contains('Account already exists');
        cy.url().should('contain', 'test-realm');
    })

    it('user with unverified email tries to log in, and prompted for username & password', () => {
        cy.visit(testRealmLoginUri);
        cy.get('#username').type(unverifiedIdpUser.username);
        cy.get('#kc-login').click();

        cy.url().should('contain', 'test-realm');

        cy.get('#password').type(unverifiedIdpUser.password);
        cy.get('#kc-login').click();

        cy.contains('Personal');
    })

    it('user with internetDomainName error email tries to log in, and prompted for username & password', () => {
          cy.visit(testRealmLoginUri);
          cy.get('#username').type(internetDomainNameError.username);
          cy.get('#kc-login').click();

          cy.url().should('contain', 'test-realm');

          cy.get('#password').type(internetDomainNameError.password);
          cy.get('#kc-login').click();

          cy.contains('Personal');
      })
});
