import KcAdminClient from "@keycloak/keycloak-admin-client";
import keycloak from "src/keycloak";

const settings = {
  baseUrl: process.env.KEYCLOAK_URL,
  realmName: process.env.REALM,
  // requestConfig: {
  //   /* Axios request config options https://github.com/axios/axios#request-config */
  // },
};

export const useKeycloakAdminApi = () => {
  const kcAdminClient = new KcAdminClient(settings);

  const setKcAdminClientAccessToken = async () => {
    kcAdminClient.setAccessToken(keycloak.token!);
  };

  // Should be able to initiate off the bat and still provide as a callback
  setKcAdminClientAccessToken();

  return [kcAdminClient, setKcAdminClientAccessToken] as const;
};
