import React from "react";
import ReactDOM from "react-dom/client";
import "./i18n";
import App from "./App";
import {
  createBrowserRouter,
  Navigate,
  redirect,
  RouterProvider,
} from "react-router-dom";
import ErrorPage from "@/pages/error-page";
import Organizations from "@/pages/organizations";
import OrganizationDetail from "@/pages/organizations/detail";
import OrganizationSettings from "@/pages/organizations/settings";
import Profile from "@/pages/profile";
import { config } from "@/config";
import { keycloak } from "@/keycloak";
import { ReactKeycloakProvider } from "@react-keycloak/web";
import Loading from "@/components/elements/loading";
import SigninProfile from "@/pages/profile/signin";
import GeneralProfile from "@/pages/profile/general";
import ActivityProfile from "@/pages/profile/activity";
import LinkedProfile from "@/pages/profile/linked";
import { store } from "./store/";
import { Provider } from "react-redux";
import Invitation from "@/pages/invitation/index";
import NewInvitation from "@/pages/invitation/new";
import DomainsAdd from "@/pages/organizations/domains/add";
import DomainContainer from "@/pages/organizations/domains";
import { Toaster } from "react-hot-toast";
import Roles from "@/pages/member/roles";
import Member from "@/pages/member";
import ProfileDelete from "@/pages/profile-delete";
import InjectStyles from "@/components/utils/injectStyles";
import NotAuthorized from "@/pages/not-authorized";
import { Tooltip } from "react-tooltip";
import PendingInvitations from "@/pages/invitation/pending";
import "./index.css";

const { features: featureFlags } = config.env;

export type P2Params = {
  orgId: string;
  domainRecord: string;
};

const router = createBrowserRouter(
  [
    {
      path: "/",
      loader: () => {
        if (featureFlags.organizationsEnabled) {
          return redirect("organizations");
        } else if (featureFlags.profileEnabled) {
          return redirect("profile");
        } else {
          return redirect("not-allowed");
        }
      },
    },
    {
      path: "/",
      element: <App />,
      errorElement: <ErrorPage />,
      children: [
        {
          path: "/organizations",
          loader: () => {
            return featureFlags.organizationsEnabled
              ? null
              : redirect("/not-allowed");
          },
          children: [
            {
              index: true,
              element: <Organizations />,
            },
            {
              path: ":orgId/details",
              element: <OrganizationDetail />,
            },
            {
              path: ":orgId/settings",
              element: <OrganizationSettings />,
            },
            {
              path: ":orgId/domains",
              index: true,
              loader: () => redirect("add"),
            },
            {
              path: ":orgId/domains/*",
              element: <DomainContainer />,
              children: [
                {
                  path: "add",
                  element: <DomainsAdd />,
                },
              ],
            },
            {
              path: ":orgId/invitation",
              index: true,
              loader: () => redirect("new"),
            },
            {
              path: ":orgId/invitation",
              element: <Invitation />,
              children: [
                {
                  path: "new",
                  element: <NewInvitation />,
                },
              ],
            },
            {
              path: ":orgId/invitation/pending",
              element: <PendingInvitations />,
            },
            {
              path: ":orgId/members/:memberId",
              element: <Member />,
              children: [
                {
                  path: "roles",
                  element: <Roles />,
                },
              ],
            },
          ],
        },
        {
          path: "/profile/delete",
          loader: () =>
            featureFlags.profileEnabled ? null : redirect("/not-allowed"),
          element: <ProfileDelete />,
        },
        {
          path: "/profile",
          element: <Profile />,
          loader: () =>
            featureFlags.profileEnabled ? null : redirect("/not-allowed"),
          children: [
            { index: true, element: <Navigate to="general" /> },
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
        {
          path: "/not-allowed",
          element: <NotAuthorized />,
        },
      ],
    },
    {
      path: "*",
      element: <Navigate to="/" />,
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
    <InjectStyles />
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
    <Tooltip
      id="tooltip"
      positionStrategy="fixed"
      style={{ maxWidth: "300px", zIndex: 1999 }}
    />
  </ReactKeycloakProvider>
);
