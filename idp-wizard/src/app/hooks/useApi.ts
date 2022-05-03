import { useGetFeatureFlagsQuery } from "@app/services";
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
  const { getRealm, keycloakToken } = useKeycloakAdminApi();
  const realm = getRealm();
  const [alias, setAlias] = useState("");
  const [orgId, setOrgId] = useState("SET_ORG_ID");

  const baseOPUrl = `${realm}/identity-provider`;
  const baseOPUrlInstances = `${baseOPUrl}/instances`;
  const baseCUrl = `${realm}/orgs/${orgId}/idps`;

  useEffect(() => {
    if (keycloakToken.org_id) {
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
      endpoint: `${baseCUrl}`,
    },
    getIdP: {
      method: HTTP_METHODS.GET,
      endpoint: `${baseCUrl}/${alias}`,
    },
    createIdP: {
      method: HTTP_METHODS.POST,
      endpoint: `${baseCUrl}`,
    },
    updateIdP: {
      method: HTTP_METHODS.PUT,
      endpoint: `${baseCUrl}/${alias}`,
    },
    importConfig: {
      method: HTTP_METHODS.POST,
      endpoint: `${baseCUrl}/import-config`,
    },
    addMapperToIdP: {
      method: HTTP_METHODS.POST,
      endpoint: `${baseCUrl}/${alias}/mappers`,
    },
  };

  let endpoints: Record<apiEndpointNames, endpoint> | undefined;
  if (featureFlags?.apiMode === "onprem") {
    endpoints = onPremEndpoints;
  }
  if (featureFlags?.apiMode === "cloud") {
    endpoints = cloudEndpoints;
  }

  return {
    setAlias,
    endpoints,
  };
};
