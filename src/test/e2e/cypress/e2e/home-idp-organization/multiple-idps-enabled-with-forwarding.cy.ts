import { testRealmLoginUri, testRealmUri } from "../../fixtures/uri";
import { idpUser } from "../../fixtures/users";

describe('an organization user logs in', () => {
   it('Trying to log in with a user who belongs to an organization with multiple IDPs should forward them to the first match', () => {
      cy.visit(testRealmLoginUri);
      cy.get('#username').type(idpUser.email);
      cy.get('#kc-login').click();

      cy.url().should('contain', 'external-idp');
      cy.get('#username').type(idpUser.username);
      cy.get('#password').type(idpUser.password);
      cy.get('#kc-login').click();

      cy.contains('Personal');
      cy.url().should('contain', testRealmUri);
   })
});
