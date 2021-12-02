import { kcAdminClient } from './Keycloak';
import IdentityProviderRepresentation from '@keycloak/keycloak-admin-client/lib/defs/identityProviderRepresentation';
import { useKeycloakAdminApi } from '../hooks/useKeycloakAdminApi';
import { useKeycloak } from '@react-keycloak/web';

export const azureStepOneAValidation = async (metadataURL: string, createIdP: boolean ) => {
    const [kcAdminClient, setKcAdminClientAccessToken] = useKeycloakAdminApi();
    await setKcAdminClientAccessToken();

    console.log('meta data url', metadataURL);

    const response = await kcAdminClient.identityProviders.importFromUrl({fromUrl: metadataURL, providerId: 'saml', realm: process.env.REALM || "wizard"})
        .then((res) => 
        {
            console.log("success result", res)
            sessionStorage.setItem('azure_metadata_url', metadataURL)
            if(createIdP) {
                return createIdPInKeycloak(res, kcAdminClient);
            } else {
                return {
                    status: 'success',
                    message: 'Successfully validated config from Azure'
                }
            }
        })
        .catch((err) => {
            console.log("import error", err)
            return {
                status: 'error',
                message: `Error validating config from Azure. ${err}`
            }
        })
    return response;
}


export const azureFinalValidation = async (metadataURL: string ) => {
    //TODO: Need some info
    const { keycloak } = useKeycloak();
    keycloak.logout();
    return
    // const azureTemplate = sessionStorage.getItem('azure_template')

    // const payload: IdentityProviderRepresentation = {
    //     alias: 'saml',
    //     providerId: 'saml',
    //     config: {
    //         azureTemplate
    //     }
    // }
    
    // console.log('am i here', metadataURL);

    // // const response = await kcAdminClient.realms.testLDAPConnection({realm: process.env.REALM || "wizard"}, connSetting)
    // const response = await kcAdminClient.identityProviders.create({...payload, realm: process.env.REALM || "wizard"})
    //     .then((res) => 
    //     {
    //         console.log("success result", res)
    //         sessionStorage.setItem('test_idp', res.id);
    //         return {
    //             status: 'success',
    //             message: 'Successfully imported config from Azure'
    //         }
            
    //     })
    //     .catch((err) => {
    //         console.log("import error", err)
    //         return {
    //             status: 'error',
    //             message: `Errored importing config from Azure. ${err}`
    //         }
    //     })
    
    // return response;
}

function createIdPInKeycloak(res: any, kcAdminClient) {
    const payload: IdentityProviderRepresentation = {
        alias: process.env.AZURE_CUSTOMER_IDENTIFIER,
        displayName: `${process.env.AZURE_CUSTOMER_IDENTIFIER} Single Sign-on`,
        providerId: 'saml',
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
            console.log("import error", err);
            return {
                status: 'error',
                message: `Errored importing config from Azure. ${err}`
            };
        });
    return response;
}
