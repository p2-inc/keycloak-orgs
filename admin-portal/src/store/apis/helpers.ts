import { keycloak } from "keycloak";
import config from "config";

export const getAccessToken = () => {
  if (keycloak.isTokenExpired(10)) {
    keycloak.updateToken(10);
  }
  return keycloak.token || "";
};

export const apiRealm = config.realm;
