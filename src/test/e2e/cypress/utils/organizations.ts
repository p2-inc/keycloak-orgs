import {tokenUri, orgsUri, activeOrganizationUri} from "../fixtures/uri";
import {User} from "../fixtures/users";

const org1Name = 'org-1';
const org2Name = 'org-2';

type TokenResponse = {
  "access_token": string,
  "expires_in": number,
  "refresh_expires_in": number,
  "refresh_token": string,
  "token_type": string,
  "not-before-policy": number,
  "session_state": string,
  "scope": string
}

type OrganizationModel = {
  "id": string,
  "name": string,
  "displayName": string,
  "url": string,
  "realm": string,
  "domain": string[],
  "attributes": object
}

const getAdminToken = () => {
  return cy.request({
    method: 'POST',
    url: tokenUri,
    form: true,
    body: {
      client_id: 'admin-cli',
      grant_type: 'password',
      username: 'admin',
      password: 'admin'
    }
  }).then(res => {
    expect(res.status).to.eq(200);
    return <TokenResponse> res.body;
  })
}

const getRealmOrganizations = () => {
  return getAdminToken()
  .then(res => cy.request(
      {
        method: 'GET',
        url: orgsUri,
        headers: {
          'Authorization': 'Bearer '.concat(res.access_token),
        }
      })
  ).then((res) => {
    expect(res.status).to.eq(200);
    expect(res.body).to.have.length(2)
    return <OrganizationModel[]> res.body;
  });
}

const getActiveOrganization = (user: User) => {
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
        url: activeOrganizationUri,
        headers: {
          'Authorization': 'Bearer '.concat(res.body.access_token),
          'Content-Type': 'application/json'
        }
      })
  )
  .then((res) => {
    expect(res.status).to.eq(200);
    return <OrganizationModel> res.body;
  });
}

export { getRealmOrganizations, getActiveOrganization, org1Name, org2Name }
