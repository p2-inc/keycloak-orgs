import keycloak from "keycloak";
import config from "config";
import { Configuration } from "@p2-inc/js-sdk";

const getAccessToken = () => {
  return keycloak.token || "";
};

export const apiConfig = new Configuration({
  basePath: config.baseUrl,
  accessToken: getAccessToken,
  headers: { Accept: "application/json" },
});

export const apiRealm = config.realm;
