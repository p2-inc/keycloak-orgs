import { loginUriSelectAccount, tokenUri } from "../fixtures/uri";
import { user1, user2, user3 } from "../fixtures/users";
import { getRealmOrganizations, org1Name, org2Name } from "../utils/organizations";

describe('user having no organizations', () => {
  it('select_account should get error message', () => {
    cy.visit(loginUriSelectAccount);
    cy.login(user3);
    cy.get('#kc-error-message').should('exist');
    cy.contains('Contact an Administrator');
  })

  it('account_hint should get error message', () => {
    cy.accountHint(org1Name);
    cy.login(user3);
    cy.get('#kc-error-message').should('exist');
    cy.contains('Invalid');
  })
});

describe('user having 1 organization', () => {
  it('select_account should get to account', () => {
    cy.visit(loginUriSelectAccount);
    cy.login(user2);
    cy.get('.pf-c-page__header-tools').should('exist');
    cy.contains('Personal');
    cy.validateActiveOrgAttributeNotVisible(user2);
    cy.validateActiveOrganization(user2, org1Name);
  })

  it('account_hint to org2 should get error message', () => {
    cy.accountHint(org2Name);
    cy.login(user2);
    cy.get('#kc-error-message').should('exist');
    cy.contains('Invalid');
  })

  it('account_hint to org1 should succeed', () => {
    cy.accountHint(org1Name);
    cy.login(user2);
    cy.get('.pf-c-page__header-tools').should('exist');
  })
});

describe('user having more than 1 organization', () => {
  it('select_account should get select org form', () => {
    cy.visit(loginUriSelectAccount);
    cy.login(user1);
    cy.get('[data-cy="select-org-label"]').should('exist');
    cy.contains('Select');
  });

  it('select_account should select org 2', () => {
    cy.visit(loginUriSelectAccount);
    cy.login(user1);
    cy.get('[data-cy="select-org-options"]').should('have.length', 2);
    cy.get('[data-cy="select-org-input"]').select(org2Name);
    cy.get('[data-cy="submit"]').click();
    cy.validateActiveOrgAttributeNotVisible(user1);
    cy.validateActiveOrganization(user1, org2Name);
  });

  it('account_hint to org1 should succeed', () => {
    cy.accountHint(org1Name);
    cy.login(user1);
    cy.get('.pf-c-page__header-tools').should('exist');
    cy.validateActiveOrgAttributeNotVisible(user1);
    cy.validateActiveOrganization(user1, org1Name);
  });

  it('account switch', () => {
    cy.visit(loginUriSelectAccount);
    cy.login(user1);
    cy.get('[data-cy="select-org-options"]').should('have.length', 2);
    cy.get('[data-cy="select-org-input"]').select(org2Name);
    cy.get('[data-cy="submit"]').click();
    cy.get('.pf-c-page__header-tools').should('exist');
    cy.contains('Personal');
    cy.validateActiveOrgAttributeNotVisible(user1);
    cy.validateActiveOrganization(user1, org2Name);

    cy.accountHint(org1Name);
    cy.get('.pf-c-page__header-tools').should('exist');
    cy.validateActiveOrgAttributeNotVisible(user1);
    cy.validateActiveOrganization(user1, org1Name);
  })
});

describe('direct grant test', () => {
  it('account_hint should succeed', () => {
    cy.validateActiveOrgAttributeNotVisible(user1);
    cy.validateActiveOrganization(user1, org1Name);

    getRealmOrganizations().then((orgs) => {
      const org = orgs.filter(function (el) {
        return el.name == org2Name;
      })[0];

      cy.request({
        method: 'POST',
        url: tokenUri + "?account_hint=".concat(org.id),
        form: true,
        body: {
          client_id: 'public-client',
          grant_type: 'password',
          username: user1.username,
          password: user1.password
        }
      }).then((res) => {
        expect(res.status).to.eq(200);
      })
    })

    cy.validateActiveOrgAttributeNotVisible(user1);
    cy.validateActiveOrganization(user1, org2Name);
  })
});
