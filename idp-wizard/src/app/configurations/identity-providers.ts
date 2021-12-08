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

export interface IIDPType {
  name: string;
  id: string;
  imageSrc: string;
  active: boolean | false;
}

export const IdentityProviders: IIDPType[] = [
  {
    name: "Azure",
    imageSrc: azureLogo,
    active: true,
    id: "azure",
  },
  { name: "Okta", imageSrc: oktaLogo, active: true, id: "okta" },
  { name: "ADFS", imageSrc: adfsLogo, active: false, id: "adfs" },
  {
    name: "Auth0",
    imageSrc: authoLogo,
    active: false,
    id: "auth0",
  },
  {
    name: "Cyberark",
    imageSrc: cyberarkLogo,
    active: false,
    id: "cyberark",
  },
  {
    name: "Google SAML",
    imageSrc: googleLogo,
    active: false,
    id: "googlesaml",
  },
  {
    name: "Jumpcloud",
    imageSrc: jumpcloudLogo,
    active: false,
    id: "jumpcloud",
  },
  {
    name: "OneLogin",
    imageSrc: oneloginLogo,
    active: false,
    id: "onelogin",
  },
  {
    name: "OpenID",
    imageSrc: openidLogo,
    active: false,
    id: "openid",
  },
  {
    name: "Ping Federate",
    imageSrc: pingFedLogo,
    active: false,
    id: "pingfederate",
  },
  {
    name: "Ping One",
    imageSrc: pingOneLogo,
    active: false,
    id: "pingone",
  },
  { name: "SAML", imageSrc: samlLogo, active: false, id: "saml" },
  {
    name: "VMWare",
    imageSrc: vmwareLogo,
    active: false,
    id: "vmware",
  },
];
