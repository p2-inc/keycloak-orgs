import { IdentityProviders, Providers } from "@app/configurations";
import { RouterParams } from "@app/routes";
import React from "react";
import { useParams } from "react-router";
import { AzureWizard } from "./Wizards/Azure";
import { OktaWizardLDAP } from "./Wizards/Okta";
import { GoogleWizard } from "./Wizards/Google";
import { useTitle } from "react-use";

const Provider = () => {
  const { provider } = useParams<RouterParams>();

  useTitle(
    `${IdentityProviders.find((ip) => ip.id === provider)?.name} | PhaseTwo`
  );

  switch (provider) {
    case Providers.OKTA:
      return <OktaWizardLDAP />;
    case Providers.AZURE:
      return <AzureWizard />;
    case Providers.GOOGLE_SAML:
      return <GoogleWizard />;

    default:
      return <div>No provider found</div>;
  }
};

export default Provider;
