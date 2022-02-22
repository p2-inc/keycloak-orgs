import React, { FC, useEffect } from "react";
import { IdPButton } from "./components/IdPButton";
import { generatePath, Link, useNavigate, useParams } from "react-router-dom";
import { ArrowLeftIcon } from "@patternfly/react-icons";
import { Stack, StackItem, Text, TextVariants } from "@patternfly/react-core";
import { IdentityProtocols, IdentityProviders } from "@app/configurations";
import { BASE_PATH, PATHS, RouterParams } from "@app/routes";

export const IdPProtocolSelector: FC = ({}) => {
  const { provider, realm } = useParams<keyof RouterParams>() as RouterParams;
  let navigate = useNavigate();

  const currentProvider = IdentityProviders.find((i) => i.id === provider)!;

  const {
    name: providerName,
    id: providerId,
    imageSrc: providerLogo,
    protocols: providerProtocols,
  } = currentProvider;

  useEffect(() => {
    if (providerProtocols.length === 1) {
      const pth = generatePath(PATHS.idpProvider, {
        realm,
        provider,
        protocol: providerProtocols[0],
      });
      navigate(pth);
    }
  }, []);

  return (
    <Stack id="protocol-selector" className="container">
      <StackItem>
        <Link to={generatePath(PATHS.idpSelector, { realm })}>
          <Text component={TextVariants.h2} className="link">
            <ArrowLeftIcon />
            {" Back to identity provider selection"}
          </Text>
        </Link>
      </StackItem>
      <StackItem className="selection-container">
        <IdPButton
          text={providerName}
          image={providerLogo}
          active={true}
          noHover
        />
      </StackItem>
      <StackItem>
        <br />
        <Text component={TextVariants.h1}>Select Your Connection Protocol</Text>
      </StackItem>
      <StackItem>
        <Text component={TextVariants.h3}>
          This is the protocol your Identity Provider will use to connect to
          <code className="pf-u-ml-xs">{window.location.hostname}</code>. If you
          don't know which to choose, we recommend SAML.
        </Text>
      </StackItem>
      <StackItem className="selection-container">
        {IdentityProtocols.filter(({ id: protocolId }) =>
          providerProtocols.includes(protocolId)
        ).map(({ name, imageSrc, id: protocolId }, i) => {
          const pth = generatePath(PATHS.idpProvider, {
            realm,
            provider: providerId,
            protocol: protocolId,
          });
          return (
            <Link to={pth} key={i}>
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
