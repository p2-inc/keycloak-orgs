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
import axios from "axios";
import { useHistory } from "react-router";
import { useKeycloak } from "@react-keycloak/web";

export const GenericLDAP: FC = () => {
  const title = "Okta wizard";
  const [stepIdReached, setStepIdReached] = useState(1);
  const [kcAdminClient] = useKeycloakAdminApi();
  const { keycloak } = useKeycloak();
  const history = useHistory();

  // Complete
  const [isValidating, setIsValidating] = useState(false);
  const [results, setResults] = useState("");
  const [error, setError] = useState(null);
  const [disableButton, setDisableButton] = useState(false);

  const [config, setConfig] = useState<
    | {
        usernameLDAPAttribute: string[];
        userObjectClasses: string[];
        uuidLDAPAttribute: string[];
        rdnLDAPAttribute: string[];
        vendor: string[];
      }
    | {}
  >({});

  // const payload = {
  //   name: "ldap",
  //   parentId: process.env.REALM,
  //   providerId: "ldap",
  //   providerType: "org.keycloak.storage.UserStorageProvider",
  //   config: {
  //     enabled: ["true"],
  //     priority: ["0"],
  //     fullSyncPeriod: ["-1"],
  //     changedSyncPeriod: ["-1"],
  //     cachePolicy: ["DEFAULT"],
  //     evictionDay: [],
  //     evictionHour: [],
  //     evictionMinute: [],
  //     maxLifespan: [],
  //     batchSizeForSync: ["1000"],
  //     editMode: ["READ_ONLY"],
  //     importEnabled: ["true"],
  //     syncRegistrations: ["false"],
  //     vendor: ["other"],
  //     usePasswordModifyExtendedOp: [],
  //     usernameLDAPAttribute: ["uid"], // set from step 1
  //     rdnLDAPAttribute: ["uid"], // set from step 1
  //     uuidLDAPAttribute: ["entryUUID"],
  //     userObjectClasses: ["inetOrgPerson, organizationalPerson"],
  //     connectionUrl: [`ldaps://${customer_id}.ldap.okta.com`],
  //     usersDn: [`ou=users, dc=${customer_id}, dc=okta, dc=com`],
  //     authType: ["simple"],
  //     startTls: [],
  //     bindDn: [`uid=${login_id}, dc=${customer_id}, dc=okta, dc=com`],
  //     bindCredential: [`${password}`],
  //     customUserSearchFilter: [],
  //     searchScope: ["1"],
  //     validatePasswordPolicy: ["false"],
  //     trustEmail: ["false"],
  //     useTruststoreSpi: ["ldapsOnly"],
  //     connectionPooling: ["true"],
  //     connectionPoolingAuthentication: [],
  //     connectionPoolingDebug: [],
  //     connectionPoolingInitSize: [],
  //     connectionPoolingMaxSize: [],
  //     connectionPoolingPrefSize: [],
  //     connectionPoolingProtocol: [],
  //     connectionPoolingTimeout: [],
  //     connectionTimeout: [],
  //     readTimeout: [],
  //     pagination: ["true"],
  //     allowKerberosAuthentication: ["false"],
  //     serverPrincipal: [],
  //     keyTab: [],
  //     kerberosRealm: [],
  //     debug: ["false"],
  //     useKerberosForPasswordAuthentication: ["false"],
  //   },
  // };

  const Axios = axios.create({
    headers: {
      authorization: `bearer ${keycloak.token}`,
    },
  });

  const onNext = (newStep) => {
    if (stepIdReached === steps.length + 1) {
      history.push("/");
    }
    setStepIdReached(stepIdReached < newStep.id ? newStep.id : stepIdReached);
  };

  const closeWizard = () => {
    history.push("/");
  };

  const validateFn = () => {
    // On final validation set stepIdReached to steps.length+1
    console.log("validated!");
  };

  const handleConfigUpdate = (updateConfig: Object) => {
    setConfig({ ...updateConfig });
  };

  const handleServerConfigValidation = async (
    serverConfig: LDAP_SERVER_CONFIG_TEST_CONNECTION
  ) => {
    const resp = await kcAdminClient.realms.testLDAPConnection(
      { realm: process.env.REALM! },
      serverConfig
    );

    console.log("[handleServerConfigValidation]", resp);

    return resp;
  };

  console.log(config);

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
      // enableNext: !!isConfigValid,
    },
    {
      id: 2,
      name: "Collect LDAP Configuration Information",
      component: (
        <Step2 handleServerConfigValidation={handleServerConfigValidation} />
      ),
      hideCancelButton: true,
      canJumpTo: stepIdReached >= 2,
    },
    {
      id: 3,
      name: "LDAP Authentication",
      component: <Step3 />,
      hideCancelButton: true,
      canJumpTo: stepIdReached >= 3,
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
