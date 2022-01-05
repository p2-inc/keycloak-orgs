import IdentityProviderRepresentation from '@keycloak/keycloak-admin-client/lib/defs/identityProviderRepresentation';
import { useKeycloakAdminApi } from '../hooks/useKeycloakAdminApi';

export const auth0StepTwoValidation = async (domain: string, clientID?: string, clientSecret?: string) => {
    var trustedDomain = `https://${domain}/.well-known/openid-configuration`;

    const [kcAdminClient, setKcAdminClientAccessToken] = useKeycloakAdminApi();

    await setKcAdminClientAccessToken();
    console.log(trustedDomain)
    const response = await kcAdminClient.identityProviders.importFromUrl({ fromUrl: trustedDomain, providerId: 'oidc', realm: process.env.REALM || "wizard" })
        .then((res) => {
            // console.log("success result", res)
            sessionStorage.setItem('auth0_domain', trustedDomain)
            if (clientID) {
                sessionStorage.setItem('auth0_clientID', clientID)
            }
            if (clientSecret) {
                sessionStorage.setItem('auth0_clientSecret', clientSecret)
            }
            // return createIdPInKeycloak(res, kcAdminClient);
            return {
                idpTemplate: res,
                kcAdmin: kcAdminClient,
                status: 'success',
                message: 'Successfully validated config from Auth0'
            }
        })
        .catch((err) => {
            // console.log("import error", err)
            return {
                idpTemplate: 'error',
                kcAdmin: kcAdminClient,
                status: 'error',
                message: `Error validating config from Auth0. ${err}`
            }
        })
    return response;
}


export const createIdPInKeycloak = async (res: any, kcAdminClient) => {
    console.log(res)
    console.log(kcAdminClient)
    const payload: IdentityProviderRepresentation = {
        alias: process.env.AUTH0_CUSTOMER_IDENTIFIER,
        displayName: `${process.env.AUTH0_CUSTOMER_IDENTIFIER} Single Sign-on`,
        providerId: 'openid',
        config: {
            ...res
        }
    };

    // Create the IdP in keycloak
    const response = kcAdminClient.identityProviders.create({ ...payload, realm: process.env.REALM! })
        .then((res) => {
            return {
                status: 'success',
                message: 'Successfully created IdP in Keycloak'
            };

        })
        .catch((err) => {
            //console.log("import error", err);
            return {
                status: 'error',
                message: `Errored importing config from Auth0. ${err}`
            };
        });
    return response;
}
