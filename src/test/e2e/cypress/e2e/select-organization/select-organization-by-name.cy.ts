import { loginUriSelectAccount, testRealmTokenUri } from "../../fixtures/uri";
import { user1, user2, user3 } from "../../fixtures/users";
import { getRealmOrganizations, org1Name, org2Name } from "../../utils/organizations";

describe('account_hint to org1 name', () => {

  it('account_hint to org1 should succeed', () => {
    cy.accountHintOrgName(org1Name);
    cy.login(user1);
    //cy.get('.pf-v5-c-page__header-tools').should('exist');
//     cy.validateActiveOrgAttributeNotVisible(user1);
    cy.validateActiveOrganization(user1, org1Name);
  });
});
