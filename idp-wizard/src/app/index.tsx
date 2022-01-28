import * as React from "react";
import { ReactKeycloakProvider } from "@react-keycloak/web";
import keycloak from "../keycloak";
import Loading from "@app/utils/Loading";
import "@patternfly/react-core/dist/styles/base.css";
import { BrowserRouter as Router } from "react-router-dom";
import { AppLayout } from "@app/AppLayout/AppLayout";
import { AppRoutes } from "@app/routes";
import "@app/styles/app.css";

const App: React.FunctionComponent = () => (
  <ReactKeycloakProvider
    authClient={keycloak}
    initOptions={{
      onLoad: "login-required",
      silentCheckSsoRedirectUri:
        window.location.origin + "/silent-check-sso.html",
    }}
    LoadingComponent={<Loading />}
  >
    <Router basename="/auth/realms/:wizard/wizard">
      <AppLayout>
        <AppRoutes />
      </AppLayout>
    </Router>
  </ReactKeycloakProvider>
);

export default App;
