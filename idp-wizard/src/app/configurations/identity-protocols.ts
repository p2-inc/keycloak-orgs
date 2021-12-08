import samlLogo from "@app/images/provider-logos/saml_logo.png";
import openIDLogo from "@app/images/provider-logos/openid_logo.png";
import ldapLogo from "@app/images/provider-logos/ldap_logo.png";

interface ProtocolType {
  name: string;
  imageSrc: string;
  active: string[];
  id: string;
}

export const IdentityProtocols: ProtocolType[] = [
  {
    name: "SAML",
    id: "saml",
    imageSrc: samlLogo,
    active: ["azure"],
  },
  {
    name: "OpenID",
    id: "openid",
    imageSrc: openIDLogo,
    active: ["openid"],
  },
  {
    name: "LDAP",
    id: "ldap",
    imageSrc: ldapLogo,
    active: ["okta"],
  },
];
