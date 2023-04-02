import React from "react";
import ReactDOM from "react-dom/client";
import "./index.css";
import "./i18n";
import App from "./App";
import {
  createBrowserRouter,
  Navigate,
  redirect,
  RouterProvider,
} from "react-router-dom";
import ErrorPage from "./pages/error-page";
import Organizations from "pages/organizations";
import OrganizationDetail from "pages/organizations/detail";
import OrganizationSettings from "pages/organizations/settings";
import Profile from "pages/profile";
import { config } from "config";
import { keycloak } from "keycloak";
import { ReactKeycloakProvider } from "@react-keycloak/web";
import Loading from "components/elements/loading";
import SigninProfile from "pages/profile/signin";
import GeneralProfile from "pages/profile/general";
import ActivityProfile from "pages/profile/activity";
import LinkedProfile from "pages/profile/linked";
import { store } from "./store/";
import { Provider } from "react-redux";
import Invitation from "pages/invitation/index";
import NewInvitation from "pages/invitation/new";
import DomainsAdd from "pages/organizations/domains/add";
import DomainsVerify from "pages/organizations/domains/verify";
import DomainContainer from "pages/organizations/domains";
import { Toaster } from "react-hot-toast";
import { basename } from "path";
import Roles from "pages/member/roles";
import Member from "pages/member";
import ProfileDelete from "pages/profile-delete";

export type P2Params = {
  orgId: string;
  domainRecord: string;
};

const router = createBrowserRouter(
  [
    {
      path: "/",
      element: <App />,
      errorElement: <ErrorPage />,
      children: [
        {
          path: "/organizations",
          element: <Organizations />,
        },
        {
          path: "/organizations/:orgId/details",
          element: <OrganizationDetail />,
        },
        {
          path: "/organizations/:orgId/settings",
          element: <OrganizationSettings />,
        },

        {
          path: "/organizations/:orgId/domains",
          index: true,
          loader: () => redirect("add"),
        },
        {
          path: "/organizations/:orgId/domains/*",
          element: <DomainContainer />,
          children: [
            {
              path: "add",
              element: <DomainsAdd />,
            },
            {
              path: "verify/:domainRecord",
              element: <DomainsVerify />,
            },
          ],
        },
        {
          path: "/organizations/:orgId/invitation",
          element: <Invitation />,
          children: [
            {
              path: "new",
              element: <NewInvitation />,
            },
          ],
        },
        {
          path: "/organizations/:orgId/members/:memberId",
          element: <Member />,
          children: [
            {
              path: "roles",
              element: <Roles />,
            },
          ],
        },
        {
          path: "/profile",
          index: true,
          loader: () => redirect("general"),
        },
        {
          path: "/profile-delete",
          element: <ProfileDelete />,
        },
        {
          path: "/profile/*",
          element: <Profile />,
          children: [
            {
              path: "general",
              element: <GeneralProfile />,
            },
            {
              path: "signin",
              element: <SigninProfile />,
            },
            {
              path: "activity",
              element: <ActivityProfile />,
            },
            {
              path: "linked",
              element: <LinkedProfile />,
            },
          ],
        },
      ],
    },
    {
      path: "*",
      element: <Navigate to="organizations" />,
    },
  ],
  {
    basename: config.basename,
  }
);

const root = ReactDOM.createRoot(
  document.getElementById("root") as HTMLElement
);
root.render(
  <ReactKeycloakProvider
    authClient={keycloak}
    initOptions={{ onLoad: "login-required", checkLoginIframe: false }}
    LoadingComponent={<Loading />}
  >
    <Provider store={store}>
      <React.StrictMode>
        <RouterProvider router={router} />
      </React.StrictMode>
    </Provider>
    <Toaster
      position="top-right"
      toastOptions={{
        duration: 6000,
      }}
    />
  </ReactKeycloakProvider>
);
