import { loginUriSelectAccount, tokenUri, testRealmLoginUri } from "../../fixtures/uri";
import { idpUser, authItIdpUser } from "../../fixtures/users";
import { getRealmOrganizations, org1Name, org2Name } from "../../utils/organizations";

describe('When the IDPLink has domain set, then only those IDPs should be offered for the user', () => {
   it('auth.it user should be redirected to the relevant realm', () => {
      cy.visit(testRealmLoginUri);
      cy.get('#username').type(authItIdpUser.email);
      cy.get('#kc-login').click();
      cy.url().should('contain', 'second-external-idp');
      cy.get('#username').type(authItIdpUser.username);
      cy.get('#password').type(authItIdpUser.password);
      cy.get('#kc-login').click();
      cy.contains('Personal');
   })
   it('phasetwo.io user should be redirected to the relevant realm', () => {
      cy.visit(testRealmLoginUri);
      cy.get('#username').type(idpUser.email);
      cy.get('#kc-login').click();
      cy.url().should('contain', 'external-idp');
      cy.get('#username').type(idpUser.username);
      cy.get('#password').type(idpUser.password);
      cy.get('#kc-login').click();
      cy.contains('Personal');
   })
});
