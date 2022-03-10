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
} from "./steps";
import oktaLogo from "@app/images/okta/okta-logo.png";
import { WizardConfirmation, Header } from "@wizardComponents";
import { oktaCreateFederationAndSyncUsers } from "@app/services/OktaValidation";
import { useNavigateToBasePath } from "@app/routes";
import { BindConfig, ServerConfig } from "./steps/forms";
import { useKeycloakAdminApi } from "@app/hooks";
import { API_STATUS } from "@app/configurations";

export const OktaWizardLDAP: FC = () => {
  const idpCommonName = "Okta IdP";
  const title = "Okta LDAP Wizard";
  const navigateToBasePath = useNavigateToBasePath();
  const [kcAdminClient, setKcAdminClientAccessToken, getServerUrl, getRealm] =
    useKeycloakAdminApi();

  const [stepIdReached, setStepIdReached] = useState(1);
  const [isFormValid, setIsFormValid] = useState(false);
  const [results, setResults] = useState("");
  const [error, setError] = useState(false);
  const [isValidating, setIsValidating] = useState(false);

  const [serverConfig, setServerConfig] = useState<ServerConfig>({});
  const [serverConfigValid, setServerConfigValid] = useState(false);

  const [bindCreds, setBindCreds] = useState<BindConfig>({});
  const [bindCredsValid, setBindCredsValid] = useState(false);

  const finalStep = 5;

  const onNext = (newStep) => {
    if (stepIdReached === finalStep) {
      navigateToBasePath();
    }
    setStepIdReached(stepIdReached < newStep.id ? newStep.id : stepIdReached);
  };

  const closeWizard = () => {
    navigateToBasePath();
  };

  const username = sessionStorage.getItem("okta_un") || "";
  const pass = sessionStorage.getItem("okta_p") || "";

  const validateOktaWizard = async () => {
    setIsValidating(true);
    const oktaCustomerIdentifier =
      sessionStorage.getItem("okta_customer_identifier") || "dev-11111111";

    setResults("Final Validation Running...");
    const results = await oktaCreateFederationAndSyncUsers(
      oktaCustomerIdentifier,
      username,
      pass
    );

    setError(results.status == "error");
    setResults("Results: " + results.message);
    setIsValidating(false);
  };

  const handleServerConfigValidation = async (
    ldapServerConfig: LDAP_SERVER_CONFIG_TEST_CONNECTION,
    serverConfig: ServerConfig
  ) => {
    setServerConfigValid(false);
    try {
      await kcAdminClient.realms.testLDAPConnection(
        { realm: getRealm()! },
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

  const steps = [
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
      component: <OktaStepThree />,
      hideCancelButton: true,
      canJumpTo: stepIdReached >= 3,
    },
    {
      id: 4,
      name: "Confirmation",
      component: (
        <WizardConfirmation
          title="LDAP Configuration Complete"
          message="Your users can now sign-in with Okta."
          buttonText="Test Sign-On"
          resultsText={results}
          error={error}
          isValidating={isValidating}
          validationFunction={validateOktaWizard}
        />
      ),
      canJumpTo: stepIdReached >= 4,
      enableNext: stepIdReached === 5,
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
