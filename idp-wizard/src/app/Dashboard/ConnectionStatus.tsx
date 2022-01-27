import {
  Card,
  CardBody,
  CardTitle,
  TextContent,
  TextList,
  TextListItem,
  TextListItemVariants,
  TextListVariants,
  Title,
} from "@patternfly/react-core";
import React from "react";
import azureLogo from "@app/images/provider-logos/azure_logo.svg";
import samlLogo from "@app/images/provider-logos/saml_logo.svg";

export function ConnectionStatus() {
  const externalIdentityProvider = () => {
    return (
      <div className="container">
        <img className="step-header-image" src={azureLogo} alt="Azure" />
      </div>
    );
  };

  const currentProtocal = () => {
    return (
      <div className="container">
        <img className="step-header-image" src={samlLogo} alt="SAML" />
      </div>
    );
  };

  return (
    <Card className="card-shadow">
      <CardTitle>
        <Title headingLevel="h2" size="xl">
          Connection Status
        </Title>
      </CardTitle>
      <CardBody>
        <TextContent>
          <TextList component={TextListVariants.dl}>
            <TextListItem component={TextListItemVariants.dt}>
              External Identity Provider:
            </TextListItem>
            <TextListItem component={TextListItemVariants.dd}>
              {externalIdentityProvider()}
            </TextListItem>
            <TextListItem component={TextListItemVariants.dt}>
              Protocol:
            </TextListItem>
            <TextListItem component={TextListItemVariants.dd}>
              {currentProtocal()}
            </TextListItem>
          </TextList>
        </TextContent>
      </CardBody>
    </Card>
  );
}
