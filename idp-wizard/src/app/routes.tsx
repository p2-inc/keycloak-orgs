import React from "react";
import { Route, RouteComponentProps, Switch } from "react-router-dom";
import { accessibleRouteChangeHandler } from "@app/utils/utils";
import { Dashboard } from "@app/Dashboard/Dashboard";
import { Support } from "@app/Support/Support";
import { GeneralSettings } from "@app/Settings/General/GeneralSettings";
import { ProfileSettings } from "@app/Settings/Profile/ProfileSettings";
import { NotFound } from "@app/NotFound/NotFound";
import { useDocumentTitle } from "@app/utils/useDocumentTitle";
import {
  LastLocationProvider,
  useLastLocation,
} from "react-router-last-location";
import { IdentityProviderSelector } from "./components/IdentityProviderWizard/IdentityProviderSelector/IdentityProviderSelector";
import { Auth0Wizard } from "./components/IdentityProviderWizard/Wizards/Auth0";
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
}

const routes: AppRouteConfig[] = [
  {
    component: Dashboard,
    exact: true,
    label: "Dashboard",
    id: "dashboard",
    path: "/",
    title: "Dashboard | PhaseTwo",
    checkSecurity: true,
  },
  {
    component: IdentityProviderSelector,
    exact: true,
    label: "Selector",
    id: "selector",
    path: "/idp",
    title: "Select your Identity Provider | PhaseTwo",
    checkSecurity: true,
  },
  {
    component: Auth0Wizard,
    exact: true,
    label: "Auth0 Wizard",
    id: "auth0",
    path: "/auth0",
    title: "PhaseTwo - Auth0",
    checkSecurity: true,
  },
];

// a custom hook for sending focus to the primary content container
// after a view has loaded so that subsequent press of tab key
// sends focus directly to relevant content
const useA11yRouteChange = (isAsync: boolean) => {
  const lastNavigation = useLastLocation();
  React.useEffect(() => {
    if (!isAsync && lastNavigation !== null) {
      routeFocusTimer = accessibleRouteChangeHandler();
    }
    return () => {
      window.clearTimeout(routeFocusTimer);
    };
  }, [isAsync, lastNavigation]);
};

const RouteWithTitleUpdates = ({
  component: Component,
  isAsync = false,
  title,
  ...rest
}: IAppRoute) => {
  useA11yRouteChange(isAsync);
  useDocumentTitle(title);

  function routeWithTitle(routeProps: RouteComponentProps) {
    return <Component {...rest} {...routeProps} />;
  }

  return <Route render={routeWithTitle} {...rest} />;
};

const PageNotFound = ({ title }: { title: string }) => {
  useDocumentTitle(title);
  return <Route component={NotFound} />;
};

const flattenedRoutes: IAppRoute[] = routes.reduce(
  (flattened, route) => [
    ...flattened,
    ...(route.routes ? route.routes : [route]),
  ],
  [] as IAppRoute[]
);

const AppRoutes = (): React.ReactElement => (
  <LastLocationProvider>
    <Switch>
      <Route
        path="/idp/:provider/protocol"
        exact
        component={IdPProtocolSelector}
      />
      <Route path="/idp/:provider/:protocol" exact component={Provider} />

      {flattenedRoutes.map(
        (
          { path, exact, component, title, isAsync, checkSecurity, id },
          idx
        ) => (
          <RouteWithTitleUpdates
            path={path}
            exact={exact}
            component={component}
            key={idx}
            title={title}
            isAsync={isAsync}
            checkSecurity={checkSecurity}
          />
        )
      )}
      <PageNotFound title="404 Page Not Found" />
    </Switch>
  </LastLocationProvider>
);

export { AppRoutes, routes };
