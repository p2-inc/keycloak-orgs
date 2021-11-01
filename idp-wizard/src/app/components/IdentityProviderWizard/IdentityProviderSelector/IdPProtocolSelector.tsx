import React, { FC } from "react";
import { IdPButton } from "./components/IdPButton";
import samlLogo from "@app/bgimages/logos/saml_logo.png";
import openIDLogo from "@app/bgimages/logos/openid_logo.png";
import ldapLogo from "@app/bgimages/logos/ldap_logo.png";
import { useHistory } from "react-router-dom";
import { ArrowLeftIcon, OpenidIcon } from "@patternfly/react-icons";
import { Stack, StackItem, Text, TextVariants } from "@patternfly/react-core";

interface ProtocolProps {
  selectedProtocol: string;
  selectedProtocolImage: string;
  goBack: () => void;
}

interface ProtocolType {
  name: string;
  imageSrc: string;
  active: boolean | false;
}

export const IdPProtocolSelector: FC<ProtocolProps> = ({
  selectedProtocol,
  selectedProtocolImage,
  goBack,
}) => {
  const history = useHistory();
  const goToProviderSetup = () => {
    selectedProtocol && history.push(`/${selectedProtocol.toLowerCase()}`);
  };

  const idpProtocolList: ProtocolType[] = [
    {
      name: "SAML",
      imageSrc: samlLogo,
      active: selectedProtocol === "Azure",
    },
    {
      name: "OpenID",
      imageSrc: openIDLogo,
      active: selectedProtocol === "None",
    },
    {
      name: "LDAP",
      imageSrc: ldapLogo,
      active: selectedProtocol === "Okta",
    },
  ];

  return (
    <Stack id="protocol-selector" className="container">
      <StackItem>
        <Text component={TextVariants.h2} onClick={goBack} className="link">
          <ArrowLeftIcon />
          {" Back to identity provider selection"}
        </Text>
      </StackItem>
      <StackItem className="selection-container">
        <IdPButton
          text={selectedProtocol}
          image={selectedProtocolImage}
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
        {idpProtocolList.map((item, i) => {
          return (
            <IdPButton
              key={i}
              text={item.name}
              image={item.imageSrc}
              active={item.active}
              onSelect={() => goToProviderSetup()}
            />
          );
        })}
      </StackItem>
    </Stack>
  );
};
