import { Providers } from "@app/configurations";
import { RouterParams } from "@app/routes";
import React from "react";
import { useParams } from "react-router";
import { AzureWizard } from "./Wizards/Azure";
import { OktaWizard } from "./Wizards/Okta";
import { GoogleWizard } from "./Wizards/Google";

const Provider = () => {
  const { provider } = useParams<RouterParams>();

  switch (provider) {
    case Providers.OKTA:
      return <OktaWizard />;
    case Providers.AZURE:
      return <AzureWizard />;
    case Providers.GOOGLE_SAML:
      return <GoogleWizard />;

    default:
      return <div>No provider found</div>;
  }
};

export default Provider;
