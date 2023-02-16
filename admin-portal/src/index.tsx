import React from "react";
import ReactDOM from "react-dom/client";
import "./index.css";
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
import { keycloak } from "keycloak";
import { ReactKeycloakProvider } from "@react-keycloak/web";
import Loading from "components/elements/loading";
import RoleProfile from "pages/profile/role";
import SigninProfile from "pages/profile/signin";
import GeneralProfile from "pages/profile/general";
import ActivityProfile from "pages/profile/activity";
import LinkedProfile from "pages/profile/linked";
import { store } from "./store/";
import { Provider } from "react-redux";
import Invitation from "pages/invitation";

const router = createBrowserRouter([
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
        path: "/invitation",
        element: <Invitation />,
      },
      {
        path: "/profile",
        index: true,
        loader: () => redirect("/profile/general"),
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
            path: "role",
            element: <RoleProfile />,
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
]);

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
  </ReactKeycloakProvider>
);
