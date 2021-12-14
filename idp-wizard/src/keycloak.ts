import Keycloak from 'keycloak-js';

// Setup Keycloak instance as needed
// Pass initialization options as required or leave blank to load from 'keycloak.json'
const keycloak = Keycloak({
  "realm": process.env.REALM || "wizard",
  "url": process.env.KEYCLOAK_URL || "https://app.phasetwo.io/auth/",
  "clientId": process.env.CLIENT_ID || "idp-wizard"
});

/*
keycloak.onTokenExpired = () => {
  console.log('token expired');
  keycloak.updateToken(30).then(() => {
    console.log('successfully get a new token');
  }).catch(() => {
    console.log('error getting a new token');
  });
}
*/

export default keycloak;

