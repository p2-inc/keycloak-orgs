import awsLogo from "@app/images/provider-logos/aws.jpg";
import azureLogo from "@app/images/provider-logos/azure_logo.png";
import oktaLogo from "@app/images/provider-logos/okta_logo.png";
import cyberarkLogo from "@app/images/provider-logos/cyberark_logo.png";
import adfsLogo from "@app/images/provider-logos/adfs_logo.png";
import authoLogo from "@app/images/provider-logos/auth0_logo.png";
import googleLogo from "@app/images/provider-logos/google_saml_logo.png";
import jumpcloudLogo from "@app/images/provider-logos/jumpcloud_logo.png";
import oneloginLogo from "@app/images/provider-logos/onelogin_logo.png";
import openidLogo from "@app/images/provider-logos/openid_logo.png";
import pingFedLogo from "@app/images/provider-logos/ping_federate_logo.png";
import pingOneLogo from "@app/images/provider-logos/ping_one_logo.png";
import samlLogo from "@app/images/provider-logos/saml_logo.png";
import vmwareLogo from "@app/images/provider-logos/vmware_logo.png";
import ldapLogo from "@app/images/provider-logos/ldap_logo.png";
import { Protocols } from ".";

export enum Providers {
  AWS = "aws",
  ADFS = "adfs",
  AUTH0 = "auth0",
  AZURE = "azure",
  CYBERARK = "cyberark",
  GOOGLE_SAML = "googlesaml",
  JUMP_CLOUD = "jumpcloud",
  LDAP = "ldap",
  OKTA = "okta",
  ONE_LOGIN = "onelogin",
  OPEN_ID = "openid",
  PING_FEDERATE = "pingfederate",
  PING_ONE = "pingone",
  SAML = "saml",
  VMWARE = "vmware",
}
export interface IIDPType {
  name: string;
  id: string;
  imageSrc: string;
  active: boolean | false;
  protocols: Protocols[];
}

export const IdentityProviders: IIDPType[] = [
  {
    name: "AWS",
    imageSrc: awsLogo,
    active: true,
    id: Providers.AWS,
    protocols: [Protocols.SAML],
  },
  {
    name: "Azure",
    imageSrc: azureLogo,
    active: true,
    id: Providers.AZURE,
    protocols: [Protocols.SAML],
  },
  {
    name: "Okta",
    imageSrc: oktaLogo,
    active: true,
    id: Providers.OKTA,
    protocols: [Protocols.LDAP, Protocols.SAML],
  },
  {
    name: "Google SAML",
    imageSrc: googleLogo,
    active: true,
    id: Providers.GOOGLE_SAML,
    protocols: [Protocols.SAML],
  },
  {
    name: "ADFS",
    imageSrc: adfsLogo,
    active: false,
    id: Providers.ADFS,
    protocols: [],
  },
  {
    name: "Auth0",
    imageSrc: authoLogo,
    active: true,
    id: Providers.AUTH0,
    protocols: [Protocols.OPEN_ID],
  },
  {
    name: "Cyberark",
    imageSrc: cyberarkLogo,
    active: false,
    id: Providers.CYBERARK,
    protocols: [],
  },
  {
    name: "Jumpcloud",
    imageSrc: jumpcloudLogo,
    active: false,
    id: Providers.JUMP_CLOUD,
    protocols: [],
  },
  {
    name: "OneLogin",
    imageSrc: oneloginLogo,
    active: false,
    id: Providers.ONE_LOGIN,
    protocols: [],
  },

  {
    name: "Ping Federate",
    imageSrc: pingFedLogo,
    active: false,
    id: Providers.PING_FEDERATE,
    protocols: [],
  },
  {
    name: "Ping One",
    imageSrc: pingOneLogo,
    active: false,
    id: Providers.PING_ONE,
    protocols: [],
  },

  {
    name: "VMWare",
    imageSrc: vmwareLogo,
    active: false,
    id: Providers.VMWARE,
    protocols: [],
  },
];

export const GenericIdentityProviders: IIDPType[] = [
  {
    name: "SAML",
    imageSrc: samlLogo,
    active: true,
    id: Providers.SAML,
    protocols: [Protocols.SAML],
  },
  {
    name: "OpenID",
    imageSrc: openidLogo,
    active: true,
    id: Providers.OPEN_ID,
    protocols: [Protocols.OPEN_ID],
  },
  {
    name: "LDAP",
    imageSrc: ldapLogo,
    active: true,
    id: Providers.LDAP,
    protocols: [Protocols.LDAP],
  },
];
