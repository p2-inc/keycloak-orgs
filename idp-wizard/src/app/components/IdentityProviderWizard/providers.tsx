import {
  GenericIdentityProviders,
  IdentityProviders,
  Protocols,
  Providers,
} from "@app/configurations";
import { RouterParams } from "@app/routes";
import React from "react";
import { useParams } from "react-router";
import { AzureWizard } from "./Wizards/Azure";
import { OktaWizardLDAP, OktaWizardSaml } from "./Wizards/Okta";
import { GoogleWizard } from "./Wizards/Google";
import { useTitle } from "react-use";
import { GenericLDAP, GenericOIDC, GenericSAML } from "./Wizards/Generic";
import { Auth0Wizard } from "./Wizards/Auth0";

const Provider = () => {
  const { provider, protocol } = useParams<RouterParams>();

  const providers = [...IdentityProviders, ...GenericIdentityProviders];

  useTitle(`${providers.find((ip) => ip.id === provider)?.name} | PhaseTwo`);

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
    case Providers.SAML:
      return <GenericSAML />;
    case Providers.OPEN_ID:
      return <GenericOIDC />;
    case Providers.LDAP:
      return <GenericLDAP />;
    case Providers.AUTH0:
      return <Auth0Wizard />;

    default:
      return <div>No provider found</div>;
  }
};

export default Provider;
