import * as React from "react";
import "@patternfly/react-core/dist/styles/base.css";
import { BrowserRouter as Router } from "react-router-dom";
import { AppLayout } from "@app/AppLayout/AppLayout";
import { AppRoutes } from "@app/routes";
import "@app/styles/app.css";
import { useEffect } from "react";
import { setOrganization } from "@app/services";
import { useAppDispatch, useAppSelector } from "@app/hooks/hooks";
import { useKeycloak } from "@react-keycloak/web";
import { first } from "lodash";
import { useRoleAccess } from "@app/hooks";

const App: React.FC = () => {
  const dispatch = useAppDispatch();
  const { hasOrganizationRoles, hasRealmRoles } = useRoleAccess();
  const { keycloak } = useKeycloak();
  // const orgs = keycloak?.tokenParsed?.organizations;

  const kcTPOrgId = keycloak?.tokenParsed?.org_id;

  useEffect(() => {
    if (kcTPOrgId) {
      const hasAdminRole = hasOrganizationRoles("admin", kcTPOrgId);
      if (hasAdminRole) {
        dispatch(setOrganization(keycloak?.tokenParsed.org_id));
      }
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
