import { Providers } from "@app/configurations";
import { RouterParams } from "@app/routes";
import React from "react";
import { useParams } from "react-router";
import { AzureWizard } from "./Wizards/Azure";
import { OktaWizard } from "./Wizards/Okta";
import { GoogleWizard } from "./Wizards/Google";
import { Auth0Wizard } from "./Wizards/Auth0";

const Provider = () => {
  const { provider } = useParams<RouterParams>();

  switch (provider) {
    case Providers.OKTA:
      return <OktaWizard />;
    case Providers.AZURE:
      return <AzureWizard />;
    case Providers.GOOGLE_SAML:
      return <GoogleWizard />;
    case Providers.AUTH0:
      return <Auth0Wizard />;

    default:
      return <div>No provider found</div>;
  }
};

export default Provider;
