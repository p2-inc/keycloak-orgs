import { useCallback, useEffect, useMemo, useState } from "react";
import { skipToken } from "@reduxjs/toolkit/query";
import {
  useGetByRealmUsersAndUserIdOrgsQuery,
  orgsApi,
  OrganizationRepresentation,
  IdentityProviderRepresentation,
} from "store/apis/orgs";
import { useKeycloak } from "@react-keycloak/web";
import { useDispatch } from "react-redux";
import { AppDispatch } from "store";

export const useUserOrgIdps = (realm: string) => {
  const { keycloak } = useKeycloak();
  const userId = keycloak?.tokenParsed?.sub;
  const dispatch = useDispatch<AppDispatch>();

  const [allIdps, setAllIdps] = useState<IdentityProviderRepresentation[]>([]);

  const { data: userOrgs = [] } = useGetByRealmUsersAndUserIdOrgsQuery(
    userId
      ? {
          realm,
          userId,
        }
      : skipToken
  );

  const stableUserOrgs = useMemo(() => userOrgs ?? [], [userOrgs]);

  const fetchAllIdps = useCallback(
    async (orgs: OrganizationRepresentation[], realm: string) => {
      const validOrgs = orgs.filter((org) => org.id); // Ensure org.id is defined
      const promises = validOrgs.map((org) =>
        dispatch(
          orgsApi.endpoints.getIdps.initiate({ orgId: org.id as string, realm })
        )
      );

      const results = await Promise.all(promises);

      const allIdps = results
        .filter((result) => "data" in result)
        .flatMap((result) => result.data) as IdentityProviderRepresentation[];

      setAllIdps(allIdps);
    },
    [dispatch]
  );

  useEffect(() => {
    if (stableUserOrgs.length > 0) {
      fetchAllIdps(stableUserOrgs, realm);
    }
  }, [stableUserOrgs, realm, fetchAllIdps]);

  return {
    idps: allIdps,
  };
};
