import * as React from "react";
import "@patternfly/react-core/dist/styles/base.css";
import { BrowserRouter as Router } from "react-router-dom";
import { AppLayout } from "@app/AppLayout/AppLayout";
import { AppRoutes } from "@app/routes";
import "@app/styles/app.css";
import { useEffect } from "react";
import { setOrganization, setMustPickOrg, setApiMode } from "@app/services";
import { useAppDispatch, useAppSelector } from "@app/hooks";
import { useKeycloak } from "@react-keycloak/web";
import { useRoleAccess } from "@app/hooks";
import { first, has } from "lodash";
import { useGetFeatureFlagsQuery } from "@app/services";

const App: React.FC = () => {
  const dispatch = useAppDispatch();
  const { hasOrganizationRoles, hasRealmRoles } = useRoleAccess();
  const { keycloak } = useKeycloak();
  const { data: featureFlags } = useGetFeatureFlagsQuery();
  const apiMode = useAppSelector((state) => state.settings.apiMode);
  const organization = useAppSelector((state) => state.settings.currentOrg);

  // Set apiMode into Settings
  useEffect(() => {
    // console.log("[apiMode useEffect]", apiMode);
    if (apiMode) {
      return;
    }
    if (featureFlags && has(featureFlags, "apiMode")) {
      dispatch(setApiMode(featureFlags?.apiMode));
    }
  }, [featureFlags]);

  // Organization setup and selection
  const kcTPOrgId = keycloak?.tokenParsed?.org_id;
  const orgsObj = keycloak?.tokenParsed?.organizations || {};
  const orgsArr = Object.keys(orgsObj)
    .map((orgId) => {
      const hasRealmRoles = hasOrganizationRoles("admin", orgId);
      return hasRealmRoles ? orgId : false;
    })
    .filter((orgId) => orgId);

  useEffect(() => {
    // console.log("[useEffect Org Check]", organization);
    if (organization) {
      return;
    }
    if (kcTPOrgId) {
      const hasAdminRole = hasOrganizationRoles("admin", kcTPOrgId);
      if (hasAdminRole) {
        dispatch(setOrganization(keycloak?.tokenParsed.org_id));
      }
      return;
    }
    // Naively grab the first orgId that has the right permissions
    if (orgsArr.length > 0) {
      dispatch(setMustPickOrg(true));
      return;
    }
    // No org available but has Realm admin role
    if (orgsArr.length === 0 && hasRealmRoles()) {
      dispatch(setOrganization("global"));
    }
  }, []);

  return (
    <Router>
      <AppLayout>
        <AppRoutes />
      </AppLayout>
    </Router>
  );
};

export default App;
