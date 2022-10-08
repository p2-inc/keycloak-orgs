import {
  GenericIdentityProviders,
  IdentityProviders,
  Protocols,
  Providers,
} from "@app/configurations";
import { useRoleAccess } from "@app/hooks";
import { RouterParams } from "@app/routes";
import React from "react";
import { useParams } from "react-router";
import { useTitle } from "react-use";

import {
  ADFSWizard,
  Auth0WizardOIDC,
  Auth0WizardSAML,
  AWSSamlWizard,
  AzureWizard,
  DuoWizard,
  GenericLDAP,
  GenericOIDC,
  GenericSAML,
  GoogleWizard,
  JumpCloudWizard,
  OktaWizardLDAP,
  OktaWizardSaml,
  OneLoginWizard,
  PingOneWizard,
} from "./Wizards";

const Provider = () => {
  const { provider, protocol } = useParams<
    keyof RouterParams
  >() as RouterParams;
  const { hasAccess } = useRoleAccess();

  const providers = [...IdentityProviders, ...GenericIdentityProviders];

  useTitle(`${providers.find((ip) => ip.id === provider)?.name} | Phase Two`);

  switch (provider) {
    case Providers.OKTA:
      if (protocol === Protocols.LDAP) return <OktaWizardLDAP />;
      if (protocol === Protocols.SAML) return <OktaWizardSaml />;
    case Providers.AZURE:
      return <AzureWizard />;
    case Providers.GOOGLE_SAML:
      return <GoogleWizard />;
    case Providers.AUTH0:
      if (protocol === Protocols.OPEN_ID) return <Auth0WizardOIDC />;
      if (protocol === Protocols.SAML) return <Auth0WizardSAML />;
    case Providers.SAML:
      return <GenericSAML />;
    case Providers.OPEN_ID:
      return <GenericOIDC />;
    case Providers.LDAP:
      return <GenericLDAP />;
    case Providers.AWS:
      return <AWSSamlWizard />;
    case Providers.ONE_LOGIN:
      return <OneLoginWizard />;
    case Providers.PING_ONE:
      return <PingOneWizard />;
    case Providers.JUMP_CLOUD:
      return <JumpCloudWizard />;
    case Providers.ADFS:
      return <ADFSWizard />;
    case Providers.DUO:
      return <DuoWizard />;

    default:
      return <div>No provider found</div>;
  }
};

export default Provider;
