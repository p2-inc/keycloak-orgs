import { loginUriSelectAccount, tokenUri, testRealmLoginUri } from "../../fixtures/uri";
import { user1, user2, user3, idpUser } from "../../fixtures/users";
import { getRealmOrganizations, org1Name, org2Name } from "../../utils/organizations";

describe('user login via home idp provider when bypassLogin is enabled', () => {
   it('providing login_hint should automatically redirect the user to the upstream idp', () => {
      cy.visit(testRealmLoginUri + "&login_hint=" + idpUser.email);
      cy.url().should('contain', 'external-idp');
   })
});
