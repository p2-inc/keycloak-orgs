import React from "react";
import ReactDOM from "react-dom/client";
import "./index.css";
import App from "./App";
import { createBrowserRouter, RouterProvider } from "react-router-dom";
import ErrorPage from "./pages/error-page";
import Organizations from "pages/organizations";
import OrganizationDetail from "pages/organizations/detail";
import OrganizationSettings from "pages/organizations/settings";
import Profile from "pages/profile";
import keycloak from "keycloak";
import { ReactKeycloakProvider } from "@react-keycloak/web";
import Loading from "components/elements/loading";

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
        path: "/organizations/:id/details",
        element: <OrganizationDetail />,
      },
      {
        path: "/organizations/:id/settings",
        element: <OrganizationSettings />,
      },
      {
        path: "/profile",
        element: <Profile />,
      },
      {
        path: "/profile/signin",
        element: <Profile />,
      },
      {
        path: "/profile/activity",
        element: <Profile />,
      },
      {
        path: "/profile/linked",
        element: <Profile />,
      },
    ],
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
    <React.StrictMode>
      <RouterProvider router={router} />
    </React.StrictMode>
  </ReactKeycloakProvider>
);
