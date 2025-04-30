/// <reference types="cypress" />
// ***********************************************
// This example commands.ts shows you how to
// create various custom commands and overwrite
// existing commands.
//
// For more comprehensive examples of custom
// commands please read more here:
// https://on.cypress.io/custom-commands
// ***********************************************
//
//
// -- This is a parent command --
// Cypress.Commands.add('login', (email, password) => { ... })
//
//
// -- This is a child command --
// Cypress.Commands.add('drag', { prevSubject: 'element'}, (subject, options) => { ... })
//
//
// -- This is a dual command --
// Cypress.Commands.add('dismiss', { prevSubject: 'optional'}, (subject, options) => { ... })
//
//
// -- This will overwrite an existing command --
// Cypress.Commands.overwrite('visit', (originalFn, url, options) => { ... })
//
// declare global {
//   namespace Cypress {
//     interface Chainable {
//       login(email: string, password: string): Chainable<void>
//       drag(subject: string, options?: Partial<TypeOptions>): Chainable<Element>
//       dismiss(subject: string, options?: Partial<TypeOptions>): Chainable<Element>
//       visit(originalFn: CommandOriginalFn, url: string, options: Partial<VisitOptions>): Chainable<Element>
//     }
//   }
// }

import { User } from "../fixtures/users";
import { getAccount } from "../utils/account";
import {getActiveOrganization, getRealmOrganizations} from "../utils/organizations";
import { loginUriAccountHint } from "../fixtures/uri";

declare global {
  namespace Cypress {
    interface Chainable {
      /**
       * Custom command to select DOM element by data-cy attribute.
       * @example cy.dataCy('greeting')
       */
      login(user: User): Chainable<JQuery<HTMLElement>>
      validateActiveOrgAttributeNotVisible(user: User): Chainable<JQuery<HTMLElement>>
      validateActiveOrganization(user: User, orgName: string): Chainable<JQuery<HTMLElement>>
      accountHint(orgName: string): Chainable<JQuery<HTMLElement>>
    }
  }
}

Cypress.Commands.add('login', (user: User) => {
  cy.get('#username').type(user.username);
  cy.get('#password').type(user.password);
  cy.get('#kc-login').click();
});

Cypress.Commands.add('validateActiveOrgAttributeNotVisible', (user: User) => {
  getAccount(user).then((account) => {
    let hasActiveOrgAttribute = false;
    for (let attribute of account.userProfileMetadata.attributes) {
      if (attribute.name == 'org.ro.active') {
        hasActiveOrgAttribute = true;
      }
    }
    expect(hasActiveOrgAttribute).eq(false);
  });
});

Cypress.Commands.add('validateActiveOrganization', (user: User, orgName: string) => {
  getActiveOrganization(user).then((org) => {
    expect(org.name).eq(orgName);
  });
});

Cypress.Commands.add('accountHint', (orgName: string) => {
  getRealmOrganizations().then((orgs) => {
    const org = orgs.filter(function (el) {
      return el.name == orgName;
    })[0];

    cy.visit(loginUriAccountHint.concat(org.id));
  })
});


Cypress.Commands.add('updatePersonalInformation', (firstName: string, lastName:string, email:string) => {
  cy.get('#firstName').type(firstName);
  cy.get('#lastName').type(lastName);
  cy.get('#email').type(email);
  cy.get('#kc-form-buttons input[type="submit"]').click()
});

