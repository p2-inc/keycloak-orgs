import * as React from "react";
import { ReactKeycloakProvider } from "@react-keycloak/web";
import keycloak from "../keycloak";
import "@patternfly/react-core/dist/styles/base.css";
import { BrowserRouter as Router } from "react-router-dom";
import { AppLayout } from "@app/AppLayout/AppLayout";
import { AppRoutes } from "@app/routes";
import "@app/app.css";

//import '@app/index.css';

const App: React.FunctionComponent = () => (
  <ReactKeycloakProvider
    authClient={keycloak}
    initOptions={{ onLoad: "login-required" }}
  >
    <Router>
      <AppLayout>
        <AppRoutes />
      </AppLayout>
    </Router>
  </ReactKeycloakProvider>
);

export default App;
