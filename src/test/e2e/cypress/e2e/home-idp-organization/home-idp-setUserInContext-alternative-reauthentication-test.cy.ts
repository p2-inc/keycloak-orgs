import { testRealmLoginUri } from "../../fixtures/uri";
import { unverifiedIdpUser } from "../../fixtures/users";

describe('Reauthenticating a locally-authenticated session when setUserInContext is off (alternative topology)', () => {
    it('keeps the username locked and lets the user log in with just the password', () => {
        cy.visit(testRealmLoginUri);
        cy.get('#username').type(unverifiedIdpUser.username);
        cy.get('#kc-login').click();
        cy.get('#password').type(unverifiedIdpUser.password);
        cy.get('#kc-login').click();
        cy.contains('Personal');

        cy.visit(testRealmLoginUri.concat('&prompt=login'));
        cy.get('#username').should('not.exist');
        cy.get('#kc-login').click();

        cy.get('#username').should('not.exist');
        cy.get('#password').type(unverifiedIdpUser.password);
        cy.get('#kc-login').click();

        cy.contains('Personal');
    })
});
