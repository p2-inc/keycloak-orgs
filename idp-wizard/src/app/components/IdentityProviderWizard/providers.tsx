import { IdentityProviders, Protocols, Providers } from "@app/configurations";
import { RouterParams } from "@app/routes";
import React from "react";
import { useParams } from "react-router";
import { AzureWizard } from "./Wizards/Azure";
import { OktaWizardLDAP, OktaWizardSaml } from "./Wizards/Okta";
import { GoogleWizard } from "./Wizards/Google";
import { useTitle } from "react-use";

const Provider = () => {
  const { provider, protocol } = useParams<RouterParams>();

  useTitle(
    `${IdentityProviders.find((ip) => ip.id === provider)?.name} | PhaseTwo`
  );

  switch (provider) {
    case Providers.OKTA:
      if (protocol === Protocols.LDAP) return <OktaWizardLDAP />;
      if (protocol === Protocols.SAML) return <OktaWizardSaml />;
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
