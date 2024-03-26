import { accountUri, tokenUri } from "../fixtures/uri";
import {User } from "../fixtures/users";

type Attribute = {
  name: string,
  displayName: string,
  required: boolean,
  readOnly: boolean,
  validators: object,
  multivalued: boolean
}

type UserProfileMetadata = {
  attributes: Attribute[],
  groups: object[]
}

type Account = {
  "id": string,
  "username": string,
  "userProfileMetadata": UserProfileMetadata
}

const getAccount = (user: User) => {
  return cy.request({
    method: 'POST',
    url: tokenUri,
    form: true,
    body: {
      client_id: 'public-client',
      grant_type: 'password',
      username: user.username,
      password: user.password
    }
  })
  .then(res => cy.request(
      {
        method: 'GET',
        url: accountUri,
        headers: {
          'Authorization': 'Bearer '.concat(res.body.access_token),
          'Content-Type': 'application/json'
        }
      })
  )
  .then((res) => {
    expect(res.status).to.eq(200);
    return <Account> res.body;
  })
}

export { getAccount }
