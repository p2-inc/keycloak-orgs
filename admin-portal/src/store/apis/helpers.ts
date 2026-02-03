import { keycloak } from "@/keycloak";

export const getAccessToken = () => {
  if (keycloak.isTokenExpired(10)) {
    keycloak.updateToken(10);
  }
  return keycloak.token || "";
};
