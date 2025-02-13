import * as React from "react";
import "@patternfly/react-core/dist/styles/base.css";
import { BrowserRouter as Router, useParams } from "react-router-dom";
import { AppLayout } from "@app/AppLayout/AppLayout";
import { AppRoutes } from "@app/routes";
import "@app/styles/app.css";
import { useEffect } from "react";
import { setOrganization, setMustPickOrg, setApiMode } from "@app/services";
import { useAppDispatch, useAppSelector } from "@app/hooks";
import { useKeycloak } from "@react-keycloak/web";
import { useRoleAccess } from "@app/hooks";
import { has } from "lodash";
import { useGetFeatureFlagsQuery } from "@app/services";

const App: React.FC = () => {
  const dispatch = useAppDispatch();
  const { hasOrganizationRoles, hasRealmRoles, navigateToAccessDenied } =
    useRoleAccess();
  const { keycloak } = useKeycloak();
  const { data: featureFlags } = useGetFeatureFlagsQuery();
  const apiMode = useAppSelector((state) => state.settings.apiMode);
  const organization = useAppSelector((state) => state.settings.currentOrg);

  // Set apiMode into Settings
  useEffect(() => {
    if (apiMode) {
      return;
    }
    if (featureFlags && has(featureFlags, "apiMode")) {
      dispatch(setApiMode(featureFlags?.apiMode));
    }
  }, [featureFlags]);

  // Organization setup and selection
  const orgsObj = keycloak?.tokenParsed?.organizations || {};
  const orgsArr: string[] = Object.keys(orgsObj)
    .map((orgId): string | false =>
      hasOrganizationRoles("admin", orgId) ? orgId : false
    )
    .filter((orgId): orgId is string => Boolean(orgId));

  useEffect(() => {
    // Organization already selected and is still locally saved
    // and still in token
    if (organization && orgsArr.includes(organization)) {
      return;
    }

    const checkRoles = hasRealmRoles();

    if (checkRoles === "skip") {
      return;
    }

    // No org available but has Realm admin role
    if (orgsArr.length === 0 && hasRealmRoles()) {
      dispatch(setOrganization("global"));
      return;
    }

    // Organizations array is only 1
    // Set organization to that specific org
    if (orgsArr.length === 1) {
      dispatch(setOrganization(orgsArr[0]));
      dispatch(setMustPickOrg(false));
      return;
    }

    // Must pick an org or global
    // Global option presented in picker
    if (orgsArr.length > 1) {
      dispatch(setMustPickOrg(true));
      return;
    }

    // Did not have Realm Role and No orgs available
    // Redirect to Access Denied
    return navigateToAccessDenied();
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
