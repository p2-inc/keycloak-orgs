import IdentityProviderRepresentation from '@keycloak/keycloak-admin-client/lib/defs/identityProviderRepresentation';
import { useKeycloakAdminApi } from '../hooks/useKeycloakAdminApi';

export const auth0StepTwoValidation = async (domain: string, createIdP: boolean, clientID? : string, clientSecret? : string ) => {
    const [kcAdminClient, setKcAdminClientAccessToken] = useKeycloakAdminApi();
    
    //console.log("setting accessToken");
    await setKcAdminClientAccessToken();
    //console.log(kcAdminClient.accessToken);

    const response = await kcAdminClient.identityProviders.importFromUrl({fromUrl: domain, providerId: 'oidc', realm: process.env.REALM || "wizard"})
        .then((res) => 
        {
           // console.log("success result", res)
            sessionStorage.setItem('auth0_domain', domain)
            if(clientID){
                sessionStorage.setItem('auth0_clientID', clientID)
            }
            if(clientSecret){
                sessionStorage.setItem('auth0_clientSecret', clientSecret)
            }
            if(createIdP) {
                return createIdPInKeycloak(res, kcAdminClient);
            } else {
                return {
                    status: 'success',
                    message: 'Successfully validated config from Auth0'
                }
            }
        })
        .catch((err) => {
           // console.log("import error", err)
            return {
                status: 'error',
                message: `Error validating config from Auth0. ${err}`
            }
        })
    return response;
}


function createIdPInKeycloak(res: any, kcAdminClient) {
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
