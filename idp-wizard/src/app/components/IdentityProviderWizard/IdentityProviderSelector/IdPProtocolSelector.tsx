import React, { FC } from "react";
import { IdPButton } from "./components/IdPButton";
import samlLogo from "@app/images/saml-logo.png";
import ldapLogo from "@app/images/ldap-logo.jpeg";
import { useHistory } from "react-router-dom";
import { ArrowLeftIcon } from "@patternfly/react-icons";

interface ProtocolProps {
  selectedProtocol: string;
  selectedProtocolImage: string;
  goBack: () => void;
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

  return (
    <div className="container">
      <div className="vertical-center">
        <h1>Your Identity Provider</h1>
        <div className="selection-container">
          <IdPButton text={selectedProtocol} image={selectedProtocolImage} />
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
          {selectedProtocol === "Azure" && (
            <IdPButton
              text="SAML"
              image={samlLogo}
              onSelect={() => goToProviderSetup()}
            />
          )}
          {selectedProtocol === "Okta" && (
            <IdPButton
              text="LDAP"
              image={ldapLogo}
              onSelect={() => goToProviderSetup()}
            />
          )}
          {/* <IdPButton text="Okta" image={oktaImage} provider="okta" /> */}
        </div>
      </div>
    </div>
  );
};
