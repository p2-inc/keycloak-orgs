import KcAdminClient from "@keycloak/keycloak-admin-client";
import { useKeycloak } from "@react-keycloak/web";

const settings = {
  baseUrl: process.env.KEYCLOAK_URL,
  realmName: process.env.REALM,
  // requestConfig: {
  //   /* Axios request config options https://github.com/axios/axios#request-config */
  // },
};

export const useKeycloakAdminApi = () => {
  const { keycloak } = useKeycloak();
  const kcAdminClient = new KcAdminClient(settings);

  const setKcAdminClientAccessToken = async () => {
    kcAdminClient.setAccessToken(keycloak.token!);
  };

  return [kcAdminClient, setKcAdminClientAccessToken] as const;
};
