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

export interface RouterParams {
  provider: Providers;
  protocol: Protocols;
  realm: string;
}

export const routes = [];

export const BASE_PATH = "/auth/realms/:realm/wizard";

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
      <Route index element={<Dashboard />} />
      <Route path="idp/*">
        <Route index element={<IdentityProviderSelector />} />
        <Route path=":provider/protocol" element={<IdPProtocolSelector />} />
        <Route path=":provider/:protocol" element={<Provider />} />
      </Route>
    </Route>

    <Route path="*" element={<NotFound />} />
  </Routes>
);

export { AppRoutes };
