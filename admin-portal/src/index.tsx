import React from "react";
import ReactDOM from "react-dom/client";
import "./index.css";
import App from "./App";
import { createBrowserRouter, RouterProvider, BrowserRouter, Routes, Route } from "react-router-dom";
import ErrorPage from "./pages/error-page";
import Organizations from "pages/organizations";
import OrganizationDetail from "pages/organizations/detail";
import OrganizationSettings from "pages/organizations/settings";
import Profile from "pages/profile";
import keycloak from "keycloak";
import { ReactKeycloakProvider } from '@react-keycloak/web';
import Loading from 'components/elements/loading';
import Layout from "components/layouts/layout";

/*
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
        path: "/organizations/details",
        element: <OrganizationDetail />,
      },
      {
        path: "/organizations/settings",
        element: <OrganizationSettings />,
      },
      {
        path: "/profile",
        element: <Profile />,
      },
    ],
  },
]);
*/

const root = ReactDOM.createRoot(
  document.getElementById("root") as HTMLElement
);
root.render(
  <ReactKeycloakProvider authClient={keycloak} initOptions={{ onLoad: 'login-required', checkLoginIframe: false, }} LoadingComponent={<Loading />}>
    <React.StrictMode>
      <BrowserRouter>
      <Layout>
        <Routes>
           <Route path="/organizations" element={<Organizations />} />
           <Route path="/profile" element={<Profile />} />
        </Routes>
      </Layout>
      </BrowserRouter>
    </React.StrictMode>
  </ReactKeycloakProvider>
);

/*
<React.StrictMode>
<ReactKeycloakProvider authClient={keycloak} initOptions={{ onLoad: 'login-required', checkLoginIframe: false, }} LoadingComponent={<Loading />}>
  <RouterProvider router={router} />
</ReactKeycloakProvider>
</React.StrictMode>
*/