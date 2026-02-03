import keyBy from "lodash";

import samlLogo from "@app/images/provider-logos/saml_logo.svg";
import openIDLogo from "@app/images/provider-logos/openid_logo.png";
import ldapLogo from "@app/images/provider-logos/ldap_logo.svg";

export enum Protocols {
  SAML = "saml",
  OPEN_ID = "openId",
  LDAP = "ldap",
}

interface ProtocolType {
  name: string;
  imageSrc: string;
  id: Protocols;
}

export const IdentityProtocols: ProtocolType[] = [
  {
    name: "SAML",
    id: Protocols.SAML,
    imageSrc: samlLogo,
  },
  {
    name: "OpenID",
    id: Protocols.OPEN_ID,
    imageSrc: openIDLogo,
  },
  {
    name: "LDAP",
    id: Protocols.LDAP,
    imageSrc: ldapLogo,
  },
];

interface ProtocolTypeObject {
  [Protocols.SAML]: ProtocolType;
  [Protocols.OPEN_ID]: ProtocolType;
  [Protocols.LDAP]: ProtocolType;
}

export const IdentityProtocolsObject: ProtocolTypeObject = keyBy(
  IdentityProtocols,
  "id"
);
