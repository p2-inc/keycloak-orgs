import Keycloak from "keycloak-js";
import { KeycloakService } from "@/services/keycloak.service";

const kc = new Keycloak("keycloak.json");

setInterval(async () => {
  if (kc.isTokenExpired(10)) {
    kc.updateToken(10);
  }
}, 5 * 1000); // 5 seconds

export const keycloak = kc;
export const keycloakService = new KeycloakService(kc);
