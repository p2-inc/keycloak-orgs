import { useApi } from "./useApi";
import { Axios } from "@app/components/IdentityProviderWizard/Wizards/services";
import { Protocols } from "@app/configurations";
import { useKeycloakAdminApi } from "./useKeycloakAdminApi";

export function useCreateTestIdpLink() {
  const { baseServerRealmsUrl, endpoints } = useApi();
  const { getRealm } = useKeycloakAdminApi();
  const realm = getRealm();

  const fetchIdpDetails = async (alias) => {
    try {
      const resp = await Axios.get(
        `${baseServerRealmsUrl}/${endpoints?.getIdPs.endpoint}/${alias}`,
      );
      if (resp.status === 200) {
        return resp.data;
      }
    } catch (e) {
      console.error("Error fetching identity provider details:", e);
    }
  };

  const generateValidationUrl = (alias: string) => {
    return `${baseServerRealmsUrl}/${realm}/protocol/openid-connect/auth?client_id=idp-tester&redirect_uri=${window.location.href}&response_type=code&scope=openid&kc_idp_hint=${alias}`;
  };

  const isValidationPendingForAlias = async (alias: string) => {
    const idpDetails = await fetchIdpDetails(alias);

    if (
      idpDetails &&
      idpDetails.config["home.idp.discovery.validationPending"] === "true"
    ) {
      return generateValidationUrl(alias);
    }

    return null;
  };

  return { isValidationPendingForAlias, generateValidationUrl };
}
