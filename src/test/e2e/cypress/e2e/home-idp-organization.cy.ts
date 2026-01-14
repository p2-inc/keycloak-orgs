import { loginUriSelectAccount, tokenUri, testRealmLoginUri } from "../fixtures/uri";
import { user1, user2, user3, idpUser } from "../fixtures/users";
import { getRealmOrganizations, org1Name, org2Name } from "../utils/organizations";

describe('user login via home idp provider', () => {
   it('user which doesnt exists in the realm should receive an error ', () => {
      cy.visit(testRealmLoginUri);
      cy.get('#username').type(user2.username);
      cy.get('#kc-login').click();

      cy.get('#kc-error-message').should('exist');
      cy.contains('Invalid');
   })

  it('user which doesnt have a domain associated to an organization but exists in the realm should login via username + password ', () => {
    cy.visit(testRealmLoginUri);
    cy.get('#username').type(user1.username);
    cy.get('#kc-login').click();
    cy.get('#password').type(user1.password);
    cy.get('#kc-login').click();

    cy.contains('Personal');
  })

   it('user which has a domain associated to an organization should login via idp SSO ', () => {
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
