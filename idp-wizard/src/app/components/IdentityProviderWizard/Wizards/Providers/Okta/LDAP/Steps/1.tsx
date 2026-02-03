import React, { FC, useState } from "react";
import oktaStep1Image from "@app/images/okta/okta-1.png";
import oktaStep2Image from "@app/images/okta/okta-2.png";
import { Card, CardBody } from "@patternfly/react-core";
import { InstructionProps, Step, StepImage } from "@wizardComponents";
import { LdapServerConfig, ServerConfig } from "./forms";
import { API_RETURN_PROMISE } from "@app/configurations";

interface Props {
  handleServerConfigValidation: (
    ldapServerConfig: LDAP_SERVER_CONFIG_TEST_CONNECTION,
    serverConfig: ServerConfig
  ) => API_RETURN_PROMISE;
  config: ServerConfig;
}

export type LDAP_SERVER_CONFIG_TEST_CONNECTION = {
  action: string;
  connectionUrl: string;
  authType: string;
  bindDn: string;
  bindCredential: string;
  useTruststoreSpi: string;
  connectionTimeout: string;
  startTls: string;
};

export const OktaStepOne: FC<Props> = ({
  handleServerConfigValidation,
  config,
}) => {
  const handleConfigValidation = async (serverConfig: ServerConfig) => {
    const ldapServerConfig: LDAP_SERVER_CONFIG_TEST_CONNECTION = {
      action: "testConnection",
      connectionUrl: `ldaps://${serverConfig.host}:${serverConfig.sslPort}`,
      authType: "simple",
      bindDn: "",
      bindCredential: "",
      useTruststoreSpi: "ldapsOnly",
      connectionTimeout: "",
      startTls: "",
    };

    return await handleServerConfigValidation(ldapServerConfig, serverConfig);
  };

  const instructionList: InstructionProps[] = [
    {
      text: "Directory Integrations",
      component: <StepImage src={oktaStep1Image} alt="Step 1.1" />,
    },
    {
      text: "If you have not already, add LDAP Interface from the Okta Directory Integration section.",
      component: <StepImage src={oktaStep2Image} alt="Step 1.2" />,
    },
    {
      text: "Note the settings and input them below.",
      component: <></>,
    },
    {
      component: (
        <Card className="card-shadow">
          <CardBody>
            <LdapServerConfig
              handleFormSubmit={handleConfigValidation}
              config={config}
            />
          </CardBody>
        </Card>
      ),
    },
  ];

  return (
    <Step
      title="Step 1: Enable LDAP Interface"
      instructionList={instructionList}
    />
  );
};
