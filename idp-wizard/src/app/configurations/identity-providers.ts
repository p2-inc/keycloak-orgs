import awsLogo from "@app/images/provider-logos/aws.jpg";
import entraIdLogo from "@app/images/provider-logos/msft_entraid.svg";
import oktaLogo from "@app/images/provider-logos/okta_logo.png";
import cyberarkLogo from "@app/images/provider-logos/cyberark_logo.svg";
import adfsLogo from "@app/images/provider-logos/active-directory.svg";
import authoLogo from "@app/images/provider-logos/auth0_logo.png";
import googleLogo from "@app/images/provider-logos/google-workspace-logo.svg";
import jumpcloudLogo from "@app/images/provider-logos/jumpcloud_logo.svg";
import oneloginLogo from "@app/images/provider-logos/onelogin_logo.png";
import openidLogo from "@app/images/provider-logos/openid_logo.png";
import pingFedLogo from "@app/images/provider-logos/ping_federate_logo.png";
import pingOneLogo from "@app/images/provider-logos/ping_one_logo.svg";
import samlLogo from "@app/images/provider-logos/saml_logo.svg";
import vmwareLogo from "@app/images/provider-logos/vmware_logo.svg";
import ldapLogo from "@app/images/provider-logos/ldap_logo.svg";
import duoLogo from "@app/images/duo/duo.svg";
import cloudflareLogo from "@app/images/cloudflare/cloudflare.svg";
import oracleLogo from "@app/images/oracle/oracle-logo.png";
import { Protocols } from ".";

export enum Providers {
  AWS = "aws",
  ADFS = "adfs",
  AUTH0 = "auth0",
  ENTRAID = "entraid",
  CLOUDFLARE = "cloudflare",
  CYBERARK = "cyberark",
  DUO = "duo",
  GOOGLE_SAML = "googlesaml",
  JUMP_CLOUD = "jumpcloud",
  LDAP = "ldap",
  OKTA = "okta",
  ONE_LOGIN = "onelogin",
  OPEN_ID = "openid",
  ORACLE = "oracle",
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
    name: "Entra Id",
    imageSrc: entraIdLogo,
    active: true,
    id: Providers.ENTRAID,
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
    active: true,
    id: Providers.ADFS,
    protocols: [Protocols.SAML],
  },
  {
    name: "Auth0",
    imageSrc: authoLogo,
    active: true,
    id: Providers.AUTH0,
    protocols: [Protocols.OPEN_ID, Protocols.SAML],
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
    active: true,
    id: Providers.JUMP_CLOUD,
    protocols: [Protocols.SAML],
  },
  {
    name: "OneLogin",
    imageSrc: oneloginLogo,
    active: true,
    id: Providers.ONE_LOGIN,
    protocols: [Protocols.SAML],
  },
  {
    name: "Oracle",
    imageSrc: oracleLogo,
    active: true,
    id: Providers.ORACLE,
    protocols: [Protocols.SAML],
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
    active: true,
    id: Providers.PING_ONE,
    protocols: [Protocols.SAML],
  },
  {
    name: "VMWare",
    imageSrc: vmwareLogo,
    active: false,
    id: Providers.VMWARE,
    protocols: [],
  },
  {
    name: "Duo",
    imageSrc: duoLogo,
    active: true,
    id: Providers.DUO,
    protocols: [Protocols.SAML],
  },
  {
    name: "Cloudflare",
    imageSrc: cloudflareLogo,
    active: true,
    id: Providers.CLOUDFLARE,
    protocols: [Protocols.SAML],
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
