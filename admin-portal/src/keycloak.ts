import Keycloak from "keycloak-js";

const keycloak = new Keycloak('/keycloak.json');

setInterval(async () => {
    if (keycloak.isTokenExpired(10)) {
      keycloak.updateToken(10);
    }
  }, 5 * 1000); // 5 seconds

export default keycloak;