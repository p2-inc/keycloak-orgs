import React from "react";
import {
  generatePath,
  Route,
  Routes,
  useNavigate,
  useParams,
} from "react-router-dom";
import { Dashboard } from "@app/Dashboard/Dashboard";
import { NotFound } from "@app/NotFound/NotFound";
import { IdentityProviderSelector } from "./components/IdentityProviderWizard/IdentityProviderSelector/IdentityProviderSelector";
import Provider from "./components/IdentityProviderWizard/providers";
import { IdPProtocolSelector } from "./components/IdentityProviderWizard/IdentityProviderSelector/IdPProtocolSelector";
import { Protocols, Providers } from "./configurations";
import { AccessDenied } from "./AccessDenied/AccessDenied";

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
};

export function useNavigateToBasePath(realm?: string) {
  let navigate = useNavigate();

  const { realm: realmParam } = useParams();
  const pth = generatePath(`${BASE_PATH}/`, {
    realm: realmParam || realm || "",
  });

  const navigateToBasePath = () => navigate(pth);

  return navigateToBasePath;
}

const AppRoutes = (): React.ReactElement => (
  <Routes>
    <Route path={BASE_PATH}>
      <Route index element={<IdentityProviderSelector />} />
      <Route path="idp/*">
        <Route path=":provider/protocol" element={<IdPProtocolSelector />} />
        <Route path=":provider/:protocol" element={<Provider />} />
      </Route>
      <Route path="dashboard">
        <Route index element={<Dashboard />} />
      </Route>
      <Route path="access-denied" element={AccessDenied} />
    </Route>

    <Route path="*" element={<NotFound />} />
  </Routes>
);

export { AppRoutes };
