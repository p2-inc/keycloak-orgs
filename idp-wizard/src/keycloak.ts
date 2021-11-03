import Keycloak from 'keycloak-js';

// Setup Keycloak instance as needed
// Pass initialization options as required or leave blank to load from 'keycloak.json'
const keycloak = Keycloak({
  "realm": process.env.REALM || "wizard",
  "url": process.env.KEYCLOAK_URL || "https://app.phasetwo.io/auth/",
  "clientId": process.env.CLIENT_ID || "idp-wizard",
});

export default keycloak;

