import { useKeycloakAdminApi } from '../hooks/useKeycloakAdminApi';

export const azureStepOneAValidation = async (metadataURL: string ) => {
    const [kcAdminClient, setKcAdminClientAccessToken] = useKeycloakAdminApi();
    await setKcAdminClientAccessToken();

    console.log(metadataURL);

    //const response = await kcAdminClient.realms.testLDAPConnection({realm: process.env.REALM || "wizard"}, connSetting)
    const response = await kcAdminClient.identityProviders.importFromUrl({fromUrl: metadataURL, providerId: 'saml', realm: process.env.REALM || "wizard"})
        .then((res) => 
        {
            console.log("success result", res)
            sessionStorage.setItem('azure_template', res);
            return {
                status: 'success',
                message: 'Successfully imported config from Azure'
            }
            
        })
        .catch((err) => {
            console.log("import error", err)
            return {
                status: 'error',
                message: `Errored importing config from Azure. ${err}`
            }
        })
    
    return response;
}


export const azureFinalValidation = async (metadataURL: string ) => {
    //TODO: Need some info
}