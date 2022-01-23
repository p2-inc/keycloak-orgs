import React, { FC, useState } from "react";
import {
  PageSection,
  PageSectionVariants,
  PageSectionTypes,
  Wizard,
} from "@patternfly/react-core";
import LdapLogo from "@app/images/provider-logos/ldap_logo.png";
import { Header, WizardConfirmation } from "@wizardComponents";
import {
  LDAP_SERVER_CONFIG_TEST_CONNECTION,
  Step1,
  Step2,
  Step3,
} from "./steps";
import { useKeycloakAdminApi } from "@app/hooks/useKeycloakAdminApi";
import { useHistory } from "react-router";
import { useKeycloak } from "@react-keycloak/web";
import { API_STATUS } from "@app/configurations/api-status";
import { BindConfig, ServerConfig } from "./steps/forms";

export const GenericLDAP: FC = () => {
  const title = "Okta wizard";
  const [stepIdReached, setStepIdReached] = useState(1);
  const [kcAdminClient] = useKeycloakAdminApi();
  const { keycloak } = useKeycloak();
  const history = useHistory();

  // Complete
  const [isValidating, setIsValidating] = useState(false);
  const [results, setResults] = useState("");
  const [error, setError] = useState<null | boolean>(null);
  const [disableButton, setDisableButton] = useState(false);

  const [config, setConfig] = useState<{
    usernameLDAPAttribute: string[];
    userObjectClasses: string[];
    uuidLDAPAttribute: string[];
    rdnLDAPAttribute: string[];
    vendor: string[];
  }>({});
  const [serverConfig, setServerConfig] = useState<ServerConfig>({});
  const [serverConfigValid, setServerConfigValid] = useState(false);
  const [bindCreds, setBindCreds] = useState<BindConfig>({});
  const [bindCredsValid, setBindCredsValid] = useState(false);

  const onNext = (newStep) => {
    if (stepIdReached === steps.length + 1) {
      history.push("/");
    }
    setStepIdReached(stepIdReached < newStep.id ? newStep.id : stepIdReached);
  };

  const closeWizard = () => {
    history.push("/");
  };

  const validateFn = async () => {
    setIsValidating(true);
    setDisableButton(false);
    setResults("Creating LDAP IdP...");

    const { host, sslPort, baseDn } = serverConfig;

    const payload = {
      name: "ldap",
      parentId: process.env.REALM,
      providerId: "ldap",
      providerType: "org.keycloak.storage.UserStorageProvider",
      config: {
        enabled: ["true"],
        priority: ["0"],
        fullSyncPeriod: ["-1"],
        changedSyncPeriod: ["-1"],
        cachePolicy: ["DEFAULT"],
        evictionDay: [],
        evictionHour: [],
        evictionMinute: [],
        maxLifespan: [],
        batchSizeForSync: ["1000"],
        editMode: ["READ_ONLY"],
        importEnabled: ["true"],
        syncRegistrations: ["false"],
        vendor: config.vendor,
        usePasswordModifyExtendedOp: [],
        usernameLDAPAttribute: config.usernameLDAPAttribute,
        rdnLDAPAttribute: config.rdnLDAPAttribute,
        uuidLDAPAttribute: config.uuidLDAPAttribute,
        userObjectClasses: config.userObjectClasses,
        connectionUrl: [`ldaps://${host}:${sslPort}`],
        usersDn: [serverConfig.userBaseDn],
        authType: ["simple"],
        startTls: [],
        bindDn: [bindCreds.bindDn],
        bindCredential: [bindCreds.bindPassword],
        customUserSearchFilter: [
          serverConfig.userFilter,
          serverConfig.groupFilter,
        ],
        searchScope: ["1"],
        validatePasswordPolicy: ["false"],
        trustEmail: ["false"],
        useTruststoreSpi: ["ldapsOnly"],
        connectionPooling: ["true"],
        connectionPoolingAuthentication: [],
        connectionPoolingDebug: [],
        connectionPoolingInitSize: [],
        connectionPoolingMaxSize: [],
        connectionPoolingPrefSize: [],
        connectionPoolingProtocol: [],
        connectionPoolingTimeout: [],
        connectionTimeout: [],
        readTimeout: [],
        pagination: ["true"],
        allowKerberosAuthentication: ["false"],
        serverPrincipal: [],
        keyTab: [],
        kerberosRealm: [],
        debug: ["false"],
        useKerberosForPasswordAuthentication: ["false"],
      },
    };

    let createResp;
    try {
      createResp = await kcAdminClient.components.create(payload);
    } catch (e) {
      setResults("Failure to create LDAP IdP. Check values and try again.");
      setError(true);
      setIsValidating(false);
      return {
        status: API_STATUS.ERROR,
        message: "Failure to create LDAP IdP. Check values and try again.",
      };
    }

    try {
      await kcAdminClient.userStorageProvider.sync({
        id: createResp.id,
        action: "triggerChangedUsersSync",
        realm: process.env.REALM,
      });
    } catch (e) {
      setResults(
        "LDAP IdP created but failed to sync contacts with LDAP instance. Try sync again at a later time."
      );
      setError(true);
      setIsValidating(false);
      setDisableButton(true);
      return {
        status: API_STATUS.ERROR,
        message: "Failure to sync contacts with LDAP instance.",
      };
    }

    setResults(
      "LDAP IdP created and contacts synced. Click finish to complete."
    );
    setStepIdReached(5);
    setError(false);
    setDisableButton(true);
    setIsValidating(false);

    return {
      status: API_STATUS.SUCCESS,
      message:
        "LDAP IdP created and contacts synced. Click finish to complete.",
    };
  };

  const handleConfigUpdate = (updateConfig: Object) =>
    setConfig({ ...updateConfig });

  const handleServerConfigValidation = async (
    ldapServerConfig: LDAP_SERVER_CONFIG_TEST_CONNECTION,
    serverConfig: ServerConfig
  ) => {
    setServerConfigValid(false);
    try {
      await kcAdminClient.realms.testLDAPConnection(
        { realm: process.env.REALM! },
        ldapServerConfig
      );
      setServerConfig(serverConfig);
      setServerConfigValid(true);
      return {
        status: API_STATUS.SUCCESS,
        message:
          "LDAP connection test is successful. Continue to the next step.",
      };
    } catch (e) {
      return {
        status: API_STATUS.ERROR,
        message: "LDAP connection test failed. Check values and try again.",
      };
    }
  };

  const handleAdminCredentialValidation = async ({
    bindDn,
    bindPassword,
  }: BindConfig) => {
    const { host, sslPort, baseDn } = serverConfig;
    const credentialConfig = {
      action: "testAuthentication",
      connectionUrl: `ldaps://${host}:${sslPort}`,
      authType: "simple",
      bindDn,
      bindCredential: bindPassword,
      useTruststoreSpi: "ldapsOnly",
      connectionTimeout: "",
      startTls: "",
    };
    setBindCredsValid(false);
    try {
      await kcAdminClient.realms.testLDAPConnection(
        { realm: process.env.REALM! },
        credentialConfig
      );
      setBindCreds({ bindDn, bindPassword });
      setBindCredsValid(true);
      return {
        status: API_STATUS.SUCCESS,
        message:
          "Admin credential validation is successful. Continue to the next step.",
      };
    } catch (e) {
      return {
        status: API_STATUS.ERROR,
        message:
          "Admin credential validation failed. Check values and try again.",
      };
    }
  };

  const isConfigValid =
    config.usernameLDAPAttribute &&
    config.userObjectClasses &&
    config.uuidLDAPAttribute &&
    config.rdnLDAPAttribute &&
    config.vendor &&
    config.region;

  const steps = [
    {
      id: 1,
      name: "Tell Us About Your Server",
      component: (
        <Step1 handleConfigUpdate={handleConfigUpdate} config={config} />
      ),
      hideCancelButton: true,
      enableNext: !!isConfigValid,
    },
    {
      id: 2,
      name: "Collect LDAP Configuration Information",
      component: (
        <Step2
          handleServerConfigValidation={handleServerConfigValidation}
          config={serverConfig}
        />
      ),
      hideCancelButton: true,
      canJumpTo: stepIdReached >= 2,
      enableNext: serverConfigValid,
    },
    {
      id: 3,
      name: "LDAP Authentication",
      component: (
        <Step3
          handleAdminConfigValidation={handleAdminCredentialValidation}
          config={bindCreds}
        />
      ),
      hideCancelButton: true,
      canJumpTo: stepIdReached >= 3,
      enableNext: bindCredsValid,
    },
    {
      id: 4,
      name: "Confirmation",
      component: (
        <WizardConfirmation
          title="SSO Configuration Complete"
          message="Your users can now sign-in with LDAP."
          buttonText="Create LDAP IdP in Keycloak"
          disableButton={disableButton}
          resultsText={results}
          error={error}
          isValidating={isValidating}
          validationFunction={validateFn}
        />
      ),
      nextButtonText: "Finish",
      hideCancelButton: true,
      enableNext: stepIdReached === 5,
      canJumpTo: stepIdReached >= 4,
    },
  ];

  return (
    <>
      <Header logo={LdapLogo} />
      <PageSection
        type={PageSectionTypes.wizard}
        variant={PageSectionVariants.light}
      >
        <Wizard
          navAriaLabel={`${title} steps`}
          isNavExpandable
          mainAriaLabel={`${title} content`}
          onClose={closeWizard}
          nextButtonText="Continue to Next Step"
          steps={steps}
          height="100%"
          width="100%"
          onNext={onNext}
        />
      </PageSection>
    </>
  );
};
