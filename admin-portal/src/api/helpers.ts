import keycloak from 'keycloak';
import config from 'config';
import { Configuration } from '@p2-inc/js-sdk';

const getAccessToken = () => {
    if (keycloak.isTokenExpired(10)) {
        keycloak.updateToken(10);
    }
    return keycloak.token || "";
};

export const apiConfig = new Configuration({
    basePath: config.baseUrl,
    accessToken: getAccessToken,
    headers: { Accept: "application/json" }
});

export const apiRealm = config.realm;
