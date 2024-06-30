import React from "react";
import {
  generatePath,
  Navigate,
  Route,
  Routes,
  useNavigate,
  useParams,
} from "react-router-dom";
import { Dashboard } from "@app/components/Dashboard/Dashboard";
import { NotFound } from "@app/NotFound/NotFound";
import { IdentityProviderSelector } from "./components/IdentityProviderWizard/IdentityProviderSelector/IdentityProviderSelector";
import Provider from "./components/IdentityProviderWizard/providers";
import { IdPProtocolSelector } from "./components/IdentityProviderWizard/IdentityProviderSelector/IdPProtocolSelector";
import { Protocols, Providers } from "./configurations";
import { AccessDenied } from "./components/AccessDenied/AccessDenied";
import { useRoleAccess } from "./hooks";

export interface RouterParams {
  provider: Providers;
  protocol: Protocols;
  realm: string;
}

export const routes = [];

export const BASE_PATH = "/auth/realms/:realm/wizard";

export enum ROUTE_PATHS {
  DASHBOARD = "dashboard",
  IDP_SELECTOR = "idpSelector",
  IDP_PROTOCOL_SELECTOR = "idpProtocolSelector",
  IDP_PROVIDER = "idpProvider",
}

export const PATHS = {
  idpSelector: BASE_PATH,
  idpProtocolSelector: `${BASE_PATH}/idp/:provider/protocol`,
  idpProvider: `${BASE_PATH}/idp/:provider/:protocol`,
  dashboard: `${BASE_PATH}/dashboard`,
  accessDenied: `${BASE_PATH}/access-denied`,
};

export function generateBasePath(realm?: string) {
  const { realm: realmParam } = useParams();
  const pth = generatePath(`${BASE_PATH}/`, {
    realm: realmParam || realm || "",
  });
  return pth;
}

export function useNavigateToBasePath(realm?: string) {
  let navigate = useNavigate();
  const pth = generateBasePath(realm);
  const navigateToBasePath = () => navigate(pth);
  return navigateToBasePath;
}

// Check for Roles and Redirect to Access Denied
const PrivateRoute = ({ children }) => {
  const { hasRealmRoles } = useRoleAccess();
  if (!hasRealmRoles()) {
    return <Navigate to={`${generateBasePath()}access-denied`} />;
  }
  return children;
};

const AppRoutes = () => {
  return (
    <Routes>
      <Route path={BASE_PATH}>
        <Route
          index
          element={
            <PrivateRoute>
              <IdentityProviderSelector />
            </PrivateRoute>
          }
        />
        <Route path="idp/*">
          <Route
            path=":provider/protocol"
            element={
              <PrivateRoute>
                <IdPProtocolSelector />{" "}
              </PrivateRoute>
            }
          />
          <Route
            path=":provider/:protocol"
            element={
              <PrivateRoute>
                <Provider />
              </PrivateRoute>
            }
          />
        </Route>
        <Route path="dashboard">
          <Route
            index
            element={
              <PrivateRoute>
                <Dashboard />
              </PrivateRoute>
            }
          />
        </Route>
        <Route path="access-denied" element={<AccessDenied />} />
      </Route>
      <Route path="*" element={<NotFound />} />
    </Routes>
  );
};

export { AppRoutes };
