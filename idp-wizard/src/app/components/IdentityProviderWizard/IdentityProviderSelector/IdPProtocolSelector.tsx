import React, { FC } from "react";
import { IdPButton } from "./components/IdPButton";
import samlLogo from "@app/bgimages/logos/saml_logo.png";
import openIDLogo from "@app/bgimages/logos/openid_logo.png";
import ldapLogo from "@app/bgimages/logos/ldap_logo.png";
import { Link, useHistory, useParams } from "react-router-dom";
import { ArrowLeftIcon, OpenidIcon } from "@patternfly/react-icons";
import { Stack, StackItem, Text, TextVariants } from "@patternfly/react-core";
import { idpList } from "./IdentityProviderSelector";

interface ProtocolType {
  name: string;
  imageSrc: string;
  active: string[];
  id: string;
}

export const IdPProtocolSelector: FC = ({}) => {
  const { provider } = useParams();
  const history = useHistory();

  const idpProtocolList: ProtocolType[] = [
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

  return (
    <Stack id="protocol-selector" className="container">
      <StackItem>
        <Link to="/idp">
          <Text component={TextVariants.h2} className="link">
            <ArrowLeftIcon />
            {" Back to identity provider selection"}
          </Text>
        </Link>
      </StackItem>
      <StackItem className="selection-container">
        <IdPButton
          text={provider}
          image={idpList.find((i) => i.id === provider)?.imageSrc!}
          active={true}
        />
      </StackItem>
      <StackItem>
        <br />
        <Text component={TextVariants.h1}>Choose Your Connection Protocol</Text>
      </StackItem>
      <StackItem>
        <Text component={TextVariants.h3}>
          This is the protocol your Identity Provider will use to connect to
          demo.phasetwo.io. If you don't know which to choose, we recommend
          SAML.
        </Text>
      </StackItem>
      <StackItem className="selection-container">
        {idpProtocolList.map(({ name, imageSrc, active, id }, i) => {
          return (
            <Link to={`/idp/${provider}/${id}`} key={i}>
              <IdPButton
                key={i}
                text={name}
                image={imageSrc}
                active={active.includes(provider)}
              />
            </Link>
          );
        })}
      </StackItem>
    </Stack>
  );
};
