import {loginUriSelectAccount, tokenUri, testRealmLoginUri, testWizardLoginUri} from "../../fixtures/uri";
import { user1, user2, user3, idpUser } from "../../fixtures/users";
import { getRealmOrganizations, org1Name, org2Name } from "../../utils/organizations";

describe('Logging in into the Account client in which the login is enabled by the flow', () => {
    it('providing login_hint should fill out the username on the authentication form', () => {
        cy.visit(testRealmLoginUri + "&login_hint=" + idpUser.email);
        cy.get('#username').should('have.value', idpUser.username);
    })

    it('logging in to the account (which is enabled by the client scope) page with a non-idp user is successful', () => {
        cy.visit(testRealmLoginUri);
        cy.get('#username').type(user1.username);
        cy.get('#kc-login').click();
        cy.get('#password').type(user1.password);
        cy.get('#kc-login').click();

        cy.contains('Personal');
    })
    it('logging in to the idp-wizard (which is disabled by the client scope) page with a non-idp user is successful', () => {
        cy.visit(testWizardLoginUri);
        cy.get('#username').type(user1.username);
        cy.get('#kc-login').click();
        cy.contains("Access denied")
    })
});
