import * as React from "react";
import "@patternfly/react-core/dist/styles/base.css";
import { BrowserRouter as Router } from "react-router-dom";
import { AppLayout } from "@app/AppLayout/AppLayout";
import { AppRoutes } from "@app/routes";
import "@app/styles/app.css";
import { useEffect } from "react";
import { setOrganization, setMustPickOrg } from "@app/services";
import { useAppDispatch } from "@app/hooks/hooks";
import { useKeycloak } from "@react-keycloak/web";
import { useRoleAccess } from "@app/hooks";
import { first } from "lodash";

const App: React.FC = () => {
  const dispatch = useAppDispatch();
  const { hasOrganizationRoles, hasRealmRoles } = useRoleAccess();
  const { keycloak } = useKeycloak();

  // Organization setup and selection
  const kcTPOrgId = keycloak?.tokenParsed?.org_id;
  const orgsObj = keycloak?.tokenParsed?.organizations;
  const orgsArr = Object.keys(orgsObj)
    .map((orgId) => {
      const hasRealmRoles = hasOrganizationRoles("admin", orgId);
      return hasRealmRoles ? orgId : false;
    })
    .filter((orgId) => orgId);

  useEffect(() => {
    if (kcTPOrgId) {
      const hasAdminRole = hasOrganizationRoles("admin", kcTPOrgId);
      if (hasAdminRole) {
        dispatch(setOrganization(keycloak?.tokenParsed.org_id));
      }
      return;
    }
    // Naively grab the first orgId that has the right permissions
    if (orgsArr.length > 0) {
      dispatch(setOrganization(first(orgsArr)!));
    }
    // No org available, must pick org.
    if (orgsArr.length === 0) {
      dispatch(setMustPickOrg(true));
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
