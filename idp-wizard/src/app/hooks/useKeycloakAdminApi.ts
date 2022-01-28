import KcAdminClient from "@keycloak/keycloak-admin-client";
import keycloak from "src/keycloak";

export const useKeycloakAdminApi = () => {
  const settings = {
    baseUrl: keycloak.authServerUrl,
    realmName: keycloak.realm,
    // requestConfig: {
    //   /* Axios request config options https://github.com/axios/axios#request-config */
    // },
  };
  
  const kcAdminClient = new KcAdminClient(settings);

  const setKcAdminClientAccessToken = async () => {
    kcAdminClient.setAccessToken(keycloak.token!);
  };

  // Should be able to initiate off the bat and still provide as a callback
  setKcAdminClientAccessToken();

  const getServerUrl = () => {
    if (typeof keycloak.authServerUrl !== 'undefined') {
      if (keycloak.authServerUrl.charAt(keycloak.authServerUrl.length - 1) == '/') {
	return keycloak.authServerUrl.substring(keycloak.authServerUrl.length, keycloak.authServerUrl.length-1);
      } else {
	return keycloak.authServerUrl;
      }
    } else {
      return undefined
    }
  };

  const getRealm = () => {
    if (typeof keycloak.realm !== 'undefined') {
      return keycloak.realm;
    } else {
      return undefined
    }
  };
  
  return [kcAdminClient, setKcAdminClientAccessToken, getServerUrl, getRealm ] as const;
};
