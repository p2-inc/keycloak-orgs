import Keycloak from "keycloak-js";

const keycloak = new Keycloak('keycloak.json');
keycloak.init({}).then(function(authenticated) {
  //console.log("", keycloak);
}).catch(function() {
  alert('failed to initialize');
});

setInterval(async () => {
    if (keycloak.isTokenExpired(10)) {
      keycloak.updateToken(10);
    }
  }, 5 * 1000); // 5 seconds

export default keycloak;