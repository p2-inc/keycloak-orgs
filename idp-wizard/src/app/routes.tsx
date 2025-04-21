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

export interface RouterParams {
  provider: Providers;
  protocol: Protocols;
  realm: string;
}

export enum ROUTE_PATHS {
  DASHBOARD = "dashboard",
  IDP_SELECTOR = "idpSelector",
  IDP_PROTOCOL_SELECTOR = "idpProtocolSelector",
  IDP_PROVIDER = "idpProvider",
}

export const PATHS = {
  idpSelector: getBasePath(),
  idpProtocolSelector: `${getBasePath()}/idp/:provider/protocol`,
  idpProvider: `${getBasePath()}/idp/:provider/:protocol`,
  dashboard: `${getBasePath()}/dashboard`,
  accessDenied: `${getBasePath()}/access-denied`,
};

// Function to dynamically determine the base path
export function getBasePath() {
  const match = window.location.pathname.match(
    /^(\/.*)?\/realms\/([^/]+)\/wizard/
  );
  if (match) {
    return match[0].replace(/\/realms\/[^/]+\//, "/realms/:realm/");
  }

  return "/auth/realms/:realm/wizard"; // Default fallback
}

export function getRealmFromPath() {
  const match = window.location.pathname.match(/\/realms\/([^/]+)\/wizard/);
  if (match) {
    return match[1];
  }
  return "";
}

export function generateBasePath(realm?: string) {
  const realmParam = getRealmFromPath();
  const pth = generatePath(`${getBasePath()}/`, {
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

const AppRoutes = () => (
  <Routes>
    <Route path={getBasePath()}>
      <Route index element={<IdentityProviderSelector />} />
      <Route path="idp/*">
        <Route path=":provider/protocol" element={<IdPProtocolSelector />} />
        <Route path=":provider/:protocol" element={<Provider />} />
      </Route>
      <Route path="dashboard">
        <Route index element={<Dashboard />} />
      </Route>
      <Route path="access-denied" element={<AccessDenied />} />
    </Route>
    <Route path="*" element={<NotFound />} />
  </Routes>
);

export { AppRoutes };
