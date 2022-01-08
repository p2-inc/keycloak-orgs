import React, { FC } from "react";
import { InstructionProps, Step } from "@wizardComponents";
import { LdapServerConfig, ServerConfig } from "./forms";
import { Card, CardBody } from "@patternfly/react-core";
import { API_RETURN_PROMISE } from "@app/configurations/api-status";

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

type Props = {
  handleServerConfigValidation: (
    ldapServerConfig: LDAP_SERVER_CONFIG_TEST_CONNECTION,
    serverConfig: ServerConfig
  ) => API_RETURN_PROMISE;
};

export const Step2: FC<Props> = ({ handleServerConfigValidation }) => {
  const handleConfigValidation = async ({
    host,
    sslPort,
    baseDn,
    userBaseDn,
    groupBaseDn,
    userFilter,
    groupFilter,
  }: ServerConfig) => {
    const ldapServerConfig: LDAP_SERVER_CONFIG_TEST_CONNECTION = {
      action: "testConnection",
      connectionUrl: `ldaps://${host}:${sslPort}`,
      authType: "simple",
      bindDn: "",
      bindCredential: "",
      useTruststoreSpi: "ldapsOnly",
      connectionTimeout: "",
      startTls: "",
    };

    return await handleServerConfigValidation(ldapServerConfig, {
      host,
      sslPort,
      baseDn,
      userBaseDn,
      groupBaseDn,
      userFilter,
      groupFilter,
    });
  };

  const instructions: InstructionProps[] = [
    {
      text: "You will need to collect the following LDAP server configuration information.",
      component: (
        <Card>
          <CardBody>
            <LdapServerConfig handleFormSubmit={handleConfigValidation} />
          </CardBody>
        </Card>
      ),
    },
  ];

  return (
    <Step
      title="Step 2: Collect LDAP Configuration Information"
      instructionList={instructions}
    />
  );
};
