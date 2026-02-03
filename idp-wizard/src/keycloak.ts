import Keycloak from "keycloak-js";

// Setup Keycloak instance as needed
const keycloak = new Keycloak("./keycloak.json");

setInterval(async () => {
  if (keycloak.isTokenExpired(10)) {
    keycloak.updateToken(10);
  }
}, 60 * 1000); // 60 seconds

export default keycloak;
