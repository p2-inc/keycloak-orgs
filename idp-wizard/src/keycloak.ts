import Keycloak from 'keycloak-js';

// Setup Keycloak instance as needed
// Pass initialization options as required or leave blank to load from 'keycloak.json'
const keycloak = Keycloak({
  "realm": process.env.REALM || "wizard",
  "url": process.env.KEYCLOAK_URL || "https://app.phasetwo.io/auth/",
  "clientId": process.env.CLIENT_ID || "idp-wizard",
  // "realm": "wizard",
  // "auth-server-url": "https://app.phasetwo.io/auth/",
  // "ssl-required": "external",
  // "resource": "idp-wizard",
  // "credentials": {
  //   "secret": "094e7246-8291-4a8b-bea1-d97b5eac732b"
  // },
  // "confidential-port": 0
});

export default keycloak;

