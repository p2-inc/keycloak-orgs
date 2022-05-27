import { useGetFeatureFlagsQuery } from "@app/services";
import { last } from "lodash";
import { useEffect, useState } from "react";
import { useKeycloakAdminApi } from "./useKeycloakAdminApi";

enum HTTP_METHODS {
  GET,
  POST,
  PUT,
}

type apiEndpointNames =
  | "getIdPs"
  | "getIdP"
  | "createIdP"
  | "updateIdP"
  | "importConfig"
  | "addMapperToIdP";

type endpoint = {
  method: HTTP_METHODS;
  endpoint: string;
};

export const useApi = () => {
  const { data: featureFlags } = useGetFeatureFlagsQuery();
  const { getRealm, keycloakToken, getServerUrl, getAuthRealm } =
    useKeycloakAdminApi();
  const realm = getRealm();
  const authRealm = getAuthRealm();
  const serverUrl = getServerUrl();
  const [alias, setAlias] = useState("");
  const [orgId, setOrgId] = useState("SET_ORG_ID");

  const baseOPUrl = `${realm}/identity-provider`;
  const baseOPUrlInstances = `${baseOPUrl}/instances`;
  const baseCloudUrl = `${realm}/orgs/${orgId}/idps`;

  let serverUrlSuffix = "/admin";
  const aliasId = last(alias.split("-"));

  useEffect(() => {
    if (keycloakToken?.org_id) {
      setOrgId(keycloakToken.org_id);
    }
  }, [keycloakToken]);

  // onprem endpoint
  const onPremEndpoints: Record<apiEndpointNames, endpoint> = {
    getIdPs: {
      method: HTTP_METHODS.GET,
      endpoint: `${baseOPUrlInstances}`,
    },
    getIdP: {
      method: HTTP_METHODS.GET,
      endpoint: `${baseOPUrlInstances}/${alias}`,
    },
    createIdP: {
      method: HTTP_METHODS.POST,
      endpoint: `${baseOPUrlInstances}`,
    },
    updateIdP: {
      method: HTTP_METHODS.PUT,
      endpoint: `${baseOPUrlInstances}/${alias}`,
    },
    importConfig: {
      method: HTTP_METHODS.POST,
      endpoint: `${baseOPUrl}/import-config`,
    },
    addMapperToIdP: {
      method: HTTP_METHODS.POST,
      endpoint: `${baseOPUrlInstances}/${alias}/mappers`,
    },
  };

  // cloud endpoints
  const cloudEndpoints: Record<apiEndpointNames, endpoint> = {
    getIdPs: {
      method: HTTP_METHODS.GET,
      endpoint: `${baseCloudUrl}`,
    },
    getIdP: {
      method: HTTP_METHODS.GET,
      endpoint: `${baseCloudUrl}/${alias}`,
    },
    createIdP: {
      method: HTTP_METHODS.POST,
      endpoint: `${baseCloudUrl}`,
    },
    updateIdP: {
      method: HTTP_METHODS.PUT,
      endpoint: `${baseCloudUrl}/${alias}`,
    },
    importConfig: {
      method: HTTP_METHODS.POST,
      endpoint: `${baseCloudUrl}/import-config`,
    },
    addMapperToIdP: {
      method: HTTP_METHODS.POST,
      endpoint: `${baseCloudUrl}/${alias}/mappers`,
    },
  };

  let endpoints: Record<apiEndpointNames, endpoint> | undefined;
  if (featureFlags?.apiMode === "onprem") {
    endpoints = onPremEndpoints;
    serverUrlSuffix = "/admin";
  }
  if (featureFlags?.apiMode === "cloud") {
    endpoints = cloudEndpoints;
    serverUrlSuffix = "";
  }

  let baseServerUrl = `${serverUrl}${serverUrlSuffix}`;
  let baseServerRealmsUrl = `${baseServerUrl}/realms`;

  let adminLinkSaml = `${baseServerUrl}/${authRealm}/console/#/realms/${realm}/identity-provider-settings/provider/saml/${alias}`;
  let adminLinkOidc = `${baseServerUrl}/${authRealm}/console/#/realms/${realm}/identity-provider-settings/provider/oidc/${alias}`;

  let identifierURL = `${baseServerRealmsUrl}/${endpoints?.importConfig.endpoint}`;
  let createIdPUrl = `${baseServerRealmsUrl}/${endpoints?.createIdP.endpoint!}`;
  let updateIdPUrl = `${baseServerRealmsUrl}/${endpoints?.updateIdP.endpoint!}`;

  let entityId = `${serverUrl}/realms/${realm}`;
  let loginRedirectURL = `${entityId}/broker/${alias}/endpoint`;
  let loginRedirectURI = `${entityId}/broker/${aliasId}/endpoint`;
  let federationMetadataAddressUrl = `${loginRedirectURL}/descriptor`;

  return {
    setAlias,
    endpoints,
    baseServerRealmsUrl,
    adminLinkSaml,
    adminLinkOidc,
    identifierURL,
    createIdPUrl,
    updateIdPUrl,
    loginRedirectURL,
    loginRedirectURI,
    federationMetadataAddressUrl,
    entityId,
  };
};
