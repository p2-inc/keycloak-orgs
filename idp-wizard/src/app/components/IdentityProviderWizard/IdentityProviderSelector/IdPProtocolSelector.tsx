import React, { FC, useEffect } from "react";
import { IdPButton } from "./components/IdPButton";
import { Link, useHistory, useParams } from "react-router-dom";
import { ArrowLeftIcon, OpenidIcon } from "@patternfly/react-icons";
import { Stack, StackItem, Text, TextVariants } from "@patternfly/react-core";
import { IdentityProtocols, IdentityProviders } from "@app/configurations";

export const IdPProtocolSelector: FC = ({}) => {
  const { provider } = useParams();
  const history = useHistory();

  const currentProvider = IdentityProviders.find((i) => i.id === provider)!;

  const {
    name: providerName,
    id: providerId,
    imageSrc: providerLogo,
    protocols: providerProtocols,
  } = currentProvider;

  useEffect(() => {
    if (providerProtocols.length === 1) {
      history.replace(`/idp/${provider}/${providerProtocols[0]}`);
    }
  }, []);

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
        <IdPButton text={providerName} image={providerLogo} active={true} />
      </StackItem>
      <StackItem>
        <br />
        <Text component={TextVariants.h1}>Select Your Connection Protocol</Text>
      </StackItem>
      <StackItem>
        <Text component={TextVariants.h3}>
          This is the protocol your Identity Provider will use to connect to
          demo.phasetwo.io. If you don't know which to choose, we recommend
          SAML.
        </Text>
      </StackItem>
      <StackItem className="selection-container">
        {IdentityProtocols.map(({ name, imageSrc, id: protocolId }, i) => {
          return (
            <Link to={`/idp/${providerId}/${protocolId}`} key={i}>
              <IdPButton
                key={i}
                text={name}
                image={imageSrc}
                active={providerProtocols.includes(protocolId)}
              />
            </Link>
          );
        })}
      </StackItem>
    </Stack>
  );
};
