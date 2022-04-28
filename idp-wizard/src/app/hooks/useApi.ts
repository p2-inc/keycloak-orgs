import { useGetFeatureFlagsQuery } from "@app/services";
import { useState } from "react";
import { useKeycloakAdminApi } from "./useKeycloakAdminApi";

enum HTTP_METHODS {
  GET = "GET",
  POST = "POST",
  PUT = "PUT",
}

export const useApi = () => {
  const { data: featureFlags } = useGetFeatureFlagsQuery();
  const { getRealm, kcAdminClient } = useKeycloakAdminApi();
  const realm = getRealm();
  const [alias, setAlias] = useState("");
  const [orgId, setOrgId] = useState("SET_ORG_ID");

  const baseOPUrl = `/${realm}/identity-provider`;
  const baseOPUrlInstances = `${baseOPUrl}/instances`;
  const baseCUrl = `/${realm}/orgs/${orgId}/idps`;

  console.log("[featureFlags]", featureFlags);
  console.log(
    "[kcAdminClient]",
    kcAdminClient,
    kcAdminClient.userStorageProvider
  );

  // onprem endpoint
  const onprem = {
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
  const cloud = {
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

  return {
    setAlias,
    onprem,
    cloud,
  };
};
