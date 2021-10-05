import React, { FC } from "react";
import { IdPButton } from "./components/IdPButton";
import samlLogo from "@app/bgimages/logos/saml_logo.png";
import openIDLogo from "@app/bgimages/logos/openid_logo.png";
import ldapLogo from "@app/bgimages/logos/ldap_logo.png";
import { useHistory } from "react-router-dom";
import { ArrowLeftIcon, OpenidIcon } from "@patternfly/react-icons";

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
    <div className="container">
      <div className="vertical-center">
        <h1>Your Identity Provider</h1>
        <div className="selection-container">
          <IdPButton
            text={selectedProtocol}
            image={selectedProtocolImage}
            active={true}
          />
        </div>
        <div className="link" onClick={goBack}>
          <h2>
            <ArrowLeftIcon />
            {" Back to identity provider selection"}
          </h2>
        </div>
        <br />
        <br />
        <h1>Choose Your Connection Protocol</h1>

        <h3>
          This is the protocol your Identity Provider will use to connect to
          demo.phasetwo.io.{" "}
        </h3>
        <div className="selection-container">
          {idpProtocolList.map((item) => {
            return (
              <IdPButton
                text={item.name}
                image={item.imageSrc}
                active={item.active}
                onSelect={() => goToProviderSetup()}
              />
            );
          })}
          {/* {selectedProtocol === "Azure" && (
            <IdPButton
              text="SAML"
              image={samlLogo}
              active={true}
              onSelect={() => goToProviderSetup()}
            />
          )}
          {selectedProtocol === "Okta" && (
            <IdPButton
              text="LDAP"
              image={ldapLogo}
              active={true}
              onSelect={() => goToProviderSetup()}
            />
          )} */}
          {/* <IdPButton text="Okta" image={oktaImage} provider="okta" /> */}
        </div>
      </div>
    </div>
  );
};
