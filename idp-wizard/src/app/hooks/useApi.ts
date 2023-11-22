import { useGetFeatureFlagsQuery } from "@app/services";
import { isString, last } from "lodash";
import { useEffect, useState } from "react";
import { useAppSelector } from "./hooks";
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
  const apiMode = useAppSelector((state) => state.settings.apiMode);
  const { getRealm, getServerUrl, getAuthRealm } = useKeycloakAdminApi();

  const realm = getRealm();
  const authRealm = getAuthRealm();
  const serverUrl = getServerUrl();
  const [alias, setAlias] = useState("");
  const orgId = useAppSelector((state) => state.settings.currentOrg);

  const baseOPUrl = `${realm}/identity-provider`;
  const baseOPUrlInstances = `${baseOPUrl}/instances`;
  const baseCloudUrl = `${realm}/orgs/${orgId}/idps`;

  let serverUrlSuffix = "/admin";
  const aliasId = isString(alias) ? last(alias.split("-")) : "";

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
  if (apiMode === "onprem") {
    endpoints = onPremEndpoints;
    serverUrlSuffix = "/admin";
  }
  if (apiMode === "cloud") {
    endpoints = cloudEndpoints;
    serverUrlSuffix = "";
  }

  let baseServerUrl = `${serverUrl}${serverUrlSuffix}`;
  let baseServerRealmsUrl = `${baseServerUrl}/realms`;

  let adminLinkSaml = `${baseServerUrl}/${authRealm}/console/#/${realm}/identity-providers/saml/${alias}/settings`;
  let adminLinkOidc = `${baseServerUrl}/${authRealm}/console/#/${realm}/identity-providers/oidc/${alias}/settings`;

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
