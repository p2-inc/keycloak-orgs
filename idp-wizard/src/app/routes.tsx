import React from "react";
import {
  generatePath,
  Route,
  RouteComponentProps,
  Switch,
  useHistory,
  useParams,
} from "react-router-dom";
import { Dashboard } from "@app/Dashboard/Dashboard";
import { NotFound } from "@app/NotFound/NotFound";
import { LastLocationProvider } from "react-router-last-location";
import { IdentityProviderSelector } from "./components/IdentityProviderWizard/IdentityProviderSelector/IdentityProviderSelector";
import Provider from "./components/IdentityProviderWizard/providers";
import { IdPProtocolSelector } from "./components/IdentityProviderWizard/IdentityProviderSelector/IdPProtocolSelector";
import { Protocols, Providers } from "./configurations";

let routeFocusTimer: number;
export interface IAppRoute {
  label?: string; // Excluding the label will exclude the route from the nav sidebar in AppLayout
  /* eslint-disable @typescript-eslint/no-explicit-any */
  component:
    | React.ComponentType<RouteComponentProps<any>>
    | React.ComponentType<any>;
  /* eslint-enable @typescript-eslint/no-explicit-any */
  exact?: boolean;
  path: string;
  title: string;
  id?: string;
  isAsync?: boolean;
  routes?: undefined;
  checkSecurity: boolean;
}

export interface IAppRouteGroup {
  label: string;
  routes: IAppRoute[];
}

export type AppRouteConfig = IAppRoute | IAppRouteGroup;

export interface RouterParams {
  provider: Providers;
  protocol: Protocols;
  realm: string;
}

export const routes = [];

export const BASE_PATH = "/auth/realms/:realm/wizard";

export function useNavigateToBasePath(realm?: string) {
  const history = useHistory();
  const { realm: realmParam } = useParams<RouterParams>();
  const pth = generatePath(`${BASE_PATH}/`, {
    realm: realmParam || realm,
  });

  const navigateToBasePath = () => history.push(pth);

  return navigateToBasePath;
}

const AppRoutes = (): React.ReactElement => (
  <LastLocationProvider>
    <Switch>
      <Route path={`${BASE_PATH}/`} exact>
        <Dashboard />
      </Route>
      <Route path={`${BASE_PATH}/idp`} exact>
        <IdentityProviderSelector />
      </Route>
      <Route path={`${BASE_PATH}/idp/:provider/protocol`} exact>
        <IdPProtocolSelector />
      </Route>
      <Route path={`${BASE_PATH}/idp/:provider/:protocol`} exact>
        <Provider />
      </Route>

      <Route>
        <NotFound />
      </Route>
    </Switch>
  </LastLocationProvider>
);

export { AppRoutes };
