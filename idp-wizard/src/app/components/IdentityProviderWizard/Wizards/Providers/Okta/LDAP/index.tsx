import React, { FC, useState } from "react";
import {
  PageSection,
  PageSectionVariants,
  PageSectionTypes,
  Wizard,
} from "@patternfly/react-core";
import {
  OktaStepOne,
  OktaStepTwo,
  OktaStepThree,
  LDAP_SERVER_CONFIG_TEST_CONNECTION,
  BindConfig,
  ServerConfig,
  GroupConfig,
} from "./Steps";
import oktaLogo from "@app/images/okta/okta-logo.png";
import { WizardConfirmation, Header } from "@wizardComponents";
import { useNavigateToBasePath } from "@app/routes";
import { useKeycloakAdminApi, usePrompt } from "@app/hooks";
import { API_STATUS } from "@app/configurations";

export const OktaWizardLDAP: FC = () => {
  const idpCommonName = "Okta LDAP Identity Provider";
  const title = "Okta LDAP Wizard";
  const navigateToBasePath = useNavigateToBasePath();
  const { kcAdminClient, getRealm } = useKeycloakAdminApi();

  const [stepIdReached, setStepIdReached] = useState(1);

  // Complete
  const [isValidating, setIsValidating] = useState(false);
  const [results, setResults] = useState("");
  const [error, setError] = useState<null | boolean>(null);
  const [disableButton, setDisableButton] = useState(false);

  const [serverConfig, setServerConfig] = useState<ServerConfig>({});
  const [serverConfigValid, setServerConfigValid] = useState(false);

  const [bindCreds, setBindCreds] = useState<BindConfig>({});
  const [bindCredsValid, setBindCredsValid] = useState(false);

  const [groups, setGroups] = useState("");

  let finishStep = 5;

  usePrompt(
    "The wizard is incomplete. Leaving will lose any saved progress. Are you sure?",
    stepIdReached < finishStep,
  );

  const onNext = (newStep) => {
    if (stepIdReached === finishStep) {
      navigateToBasePath();
    }
    setStepIdReached(stepIdReached < newStep.id ? newStep.id : stepIdReached);
  };

  const closeWizard = () => {
    navigateToBasePath();
  };

  const handleServerConfigValidation = async (
    ldapServerConfig: LDAP_SERVER_CONFIG_TEST_CONNECTION,
    serverConfig: ServerConfig,
  ) => {
    setServerConfigValid(false);
    try {
      await kcAdminClient.realms.testLDAPConnection(
        { realm: getRealm()! },
        ldapServerConfig,
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
      bindDn: `uid=${bindDn}, ${baseDn}`,
      bindCredential: bindPassword,
      useTruststoreSpi: "ldapsOnly",
      connectionTimeout: "",
      startTls: "",
    };
    setBindCredsValid(false);
    try {
      await kcAdminClient.realms.testLDAPConnection(
        { realm: getRealm()! },
        credentialConfig,
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

  const handleGroupSave = ({ groupFilter }: GroupConfig) => {
    setGroups(groupFilter!);
    return { status: API_STATUS.SUCCESS, message: "Group filter saved." };
  };

  const validateFn = async () => {
    setIsValidating(true);
    setDisableButton(false);
    setResults("Creating LDAP IdP...");

    const { host, sslPort, baseDn } = serverConfig;

    const payload = {
      name: "ldap",
      parentId: getRealm(),
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
        vendor: ["other"],
        usePasswordModifyExtendedOp: [],
        usernameLDAPAttribute: ["uid"],
        rdnLDAPAttribute: ["uid"],
        uuidLDAPAttribute: ["entryUUID"],
        userObjectClasses: ["inetOrgPerson, organizationalPerson"],
        connectionUrl: [`ldaps://${host}:${sslPort}`],
        usersDn: [serverConfig.userBaseDn],
        authType: ["simple"],
        startTls: [],
        bindDn: [`uid=${bindCreds.bindDn}, ${baseDn}`],
        bindCredential: [bindCreds.bindPassword],
        // TODO: what is the correct format for this?
        // customUserSearchFilter: [groups],
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
      setResults(
        `Failure to create ${idpCommonName}. Check values and try again.`,
      );
      setError(true);
      setIsValidating(false);
      return {
        status: API_STATUS.ERROR,
        message: `Failure to create ${idpCommonName}. Check values and try again.`,
      };
    }

    try {
      await kcAdminClient.userStorageProvider.sync({
        id: createResp.id,
        action: "triggerChangedUsersSync",
        realm: getRealm(),
      });
    } catch (e) {
      setResults(
        `${idpCommonName} created but failed to sync contacts with LDAP instance. Try sync again at a later time.`,
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
      `${idpCommonName} created and contacts synced. Click finish to complete.`,
    );
    setStepIdReached(finishStep);
    setError(false);
    setDisableButton(true);
    setIsValidating(false);

    return {
      status: API_STATUS.SUCCESS,
      message: `${idpCommonName} created and contacts synced. Click finish to complete.`,
    };
  };

  let steps = [
    {
      id: 1,
      name: "Enable LDAP Interface",
      component: (
        <OktaStepOne
          handleServerConfigValidation={handleServerConfigValidation}
          config={serverConfig}
        />
      ),
      enableNext: serverConfigValid,
      hideCancelButton: true,
    },
    {
      id: 2,
      name: "LDAP Authentication",
      component: (
        <OktaStepTwo
          handleAdminConfigValidation={handleAdminCredentialValidation}
          config={bindCreds}
        />
      ),
      hideCancelButton: true,
      canJumpTo: stepIdReached >= 2,
      enableNext: bindCredsValid,
    },
    {
      id: 3,
      name: "Group Mapping",
      component: (
        <OktaStepThree
          handleGroupSave={handleGroupSave}
          config={{ groupFilter: groups }}
        />
      ),
      hideCancelButton: true,
      canJumpTo: stepIdReached >= 3,
    },
    {
      id: 4,
      name: "Confirmation",
      component: (
        <WizardConfirmation
          title="OKTA LDAP Configuration Complete"
          message="Okta."
          buttonText="Create OKTA LDAP IdP"
          disableButton={disableButton}
          resultsText={results}
          error={error}
          isValidating={isValidating}
          validationFunction={validateFn}
        />
      ),
      canJumpTo: stepIdReached >= 4,
      enableNext: stepIdReached === finishStep,
      hideCancelButton: true,
    },
  ];

  return (
    <>
      <Header logo={oktaLogo} />
      <PageSection
        type={PageSectionTypes.wizard}
        variant={PageSectionVariants.light}
      >
        <Wizard
          navAriaLabel={`${title} steps`}
          mainAriaLabel={`${title} content`}
          onClose={closeWizard}
          steps={steps}
          height="100%"
          width="100%"
          onNext={onNext}
          nextButtonText="Continue to Next Step"
        />
      </PageSection>
    </>
  );
};
