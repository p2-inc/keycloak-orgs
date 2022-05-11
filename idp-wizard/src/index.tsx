import React from "react";
import ReactDOM from "react-dom";
import App from "@app/index";

import { store } from "@app/store";
import { Provider as ReduxProvider } from "react-redux";
import { ReactKeycloakProvider } from "@react-keycloak/web";
import keycloak from "./keycloak";
import Loading from "@app/utils/Loading";

if (process.env.NODE_ENV !== "production") {
  const config = {
    rules: [
      {
        id: "color-contrast",
        enabled: false,
      },
    ],
  };
  // eslint-disable-next-line @typescript-eslint/no-var-requires, no-undef
  const axe = require("react-axe");
  axe(React, ReactDOM, 1000, config);
}

ReactDOM.render(
  <ReactKeycloakProvider
    authClient={keycloak}
    initOptions={{
      onLoad: "login-required",
      silentCheckSsoRedirectUri:
        window.location.origin + "/silent-check-sso.html",
    }}
    LoadingComponent={<Loading />}
  >
    <ReduxProvider store={store}>
      <App />
    </ReduxProvider>
  </ReactKeycloakProvider>,
  document.getElementById("root") as HTMLElement
);
