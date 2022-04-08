import React, { FC, useState } from "react";
import {
  PageSection,
  PageSectionVariants,
  PageSectionTypes,
  Wizard,
} from "@patternfly/react-core";
import { JumpCloudLogo } from "@app/images/jumpcloud";
import { Header, WizardConfirmation } from "@wizardComponents";
import { Step1, Step2, Step3, Step4, Step5 } from "./steps";
import { useKeycloakAdminApi } from "@app/hooks/useKeycloakAdminApi";
import { API_STATUS, METADATA_CONFIG } from "@app/configurations/api-status";
import IdentityProviderRepresentation from "@keycloak/keycloak-admin-client/lib/defs/identityProviderRepresentation";
import { SamlUserAttributeMapper } from "@app/components/IdentityProviderWizard/Wizards/services";
import { Axios } from "@wizardServices";
import { useNavigateToBasePath } from "@app/routes";
import { getAlias } from "@wizardServices";
import { Providers, Protocols, SamlIDPDefaults } from "@app/configurations";
import { usePrompt } from "@app/hooks";

export const JumpCloudWizard: FC = () => {
  const idpCommonName = "JumpCloud IdP";
  const alias = getAlias({
    provider: Providers.JUMP_CLOUD,
    protocol: Protocols.OPEN_ID,
    preface: "auth0-oidc",
  });
  const navigateToBasePath = useNavigateToBasePath();
  const title = "JumpCloud wizard";
  const [stepIdReached, setStepIdReached] = useState(1);
  const [
    kcAdminClient,
    setKcAdminClientAccessToken,
    getServerUrl,
    getRealm,
    getAuthRealm,
  ] = useKeycloakAdminApi();

  const acsUrl = `${getServerUrl()}/realms/${getRealm()}/broker/${alias}/endpoint`;
  const entityId = `${getServerUrl()}/realms/${getRealm()}`;
  const identifierURL = `${getServerUrl()}/admin/realms/${getRealm()}/identity-provider/import-config`;
  const adminLink = `${getServerUrl()}/admin/${getAuthRealm()}/console/#/realms/${getRealm()}/identity-provider-settings/provider/saml/${alias}`;

  const [metadata, setMetadata] = useState<METADATA_CONFIG>();
  const [isFormValid, setIsFormValid] = useState(false);

  // Complete
  const [isValidating, setIsValidating] = useState(false);
  const [results, setResults] = useState("");
  const [error, setError] = useState<null | boolean>(null);
  const [disableButton, setDisableButton] = useState(false);

  const finishStep = 7;

  usePrompt(
    "The wizard is incomplete. Leaving will lose any saved progress. Are you sure?",
    stepIdReached < finishStep
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

  const handleFormSubmit = async ({
    metadataFile: file,
  }: {
    metadataFile: File;
  }) => {
    const fd = new FormData();
    fd.append("providerId", "saml");
    fd.append("file", file);

    try {
      const resp = await Axios.post(identifierURL, fd);

      if (resp.status === 200) {
        setMetadata({ ...SamlIDPDefaults, ...resp.data });
        setIsFormValid(true);

        return {
          status: API_STATUS.SUCCESS,
          message: `Configuration successfully validated with ${idpCommonName}. Continue to next step.`,
        };
      }
    } catch (err) {
      console.log(err);
    }

    return {
      status: API_STATUS.ERROR,
      message: `Configuration validation failed with ${idpCommonName}. Check file and try again.`,
    };
  };

  const validateFn = async () => {
    setIsValidating(true);
    setDisableButton(false);
    setResults(`Creating ${idpCommonName}...`);

    const payload: IdentityProviderRepresentation = {
      alias,
      displayName: `JumpCloud Single Sign-on`,
      providerId: "saml",
      config: metadata!,
    };

    try {
      await kcAdminClient.identityProviders.create({
        ...payload,
        realm: getRealm()!,
      });

      // Map attributes
      await SamlUserAttributeMapper({
        alias,
        keys: {
          serverUrl: getServerUrl()!,
          realm: getRealm()!,
        },
        attributes: [
          {
            attributeName: "firstName",
            friendlyName: "",
            userAttribute: "firstName",
          },
          {
            attributeName: "lastName",
            friendlyName: "",
            userAttribute: "lastName",
          },
          {
            attributeName: "email",
            friendlyName: "",
            userAttribute: "email",
          },
        ],
      });

      setResults(`${idpCommonName} created successfully. Click finish.`);
      setStepIdReached(finishStep);
      setError(false);
      setDisableButton(true);
    } catch (e) {
      setResults(
        `Error creating ${idpCommonName}. Please confirm there is no ${idpCommonName} configured already.`
      );
      setError(true);
    } finally {
      setIsValidating(false);
    }
  };

  const steps = [
    {
      id: 1,
      name: "Add a new SSO Application",
      component: <Step1 />,
      hideCancelButton: true,
      enableNext: true,
      canJumpTo: stepIdReached >= 1,
    },
    {
      id: 2,
      name: "Enter Service Provider Details",
      component: <Step2 acsUrl={acsUrl} entityId={entityId} />,
      hideCancelButton: true,
      enableNext: true,
      canJumpTo: stepIdReached >= 2,
    },
    {
      id: 3,
      name: "Configure Attribute Mapping",
      component: <Step3 />,
      hideCancelButton: true,
      enableNext: true,
      canJumpTo: stepIdReached >= 3,
    },
    {
      id: 4,
      name: "Assign Groups",
      component: <Step4 />,
      hideCancelButton: true,
      enableNext: true,
      canJumpTo: stepIdReached >= 3,
    },
    {
      id: 5,
      name: `Upload ${idpCommonName} Information`,
      component: <Step5 handleFormSubmit={handleFormSubmit} />,
      hideCancelButton: true,
      enableNext: isFormValid,
      canJumpTo: stepIdReached >= 5,
    },
    {
      id: 6,
      name: "Confirmation",
      component: (
        <WizardConfirmation
          title="SSO Configuration Complete"
          message="Your users can now sign-in with JumpCloud."
          buttonText={`Create ${idpCommonName} in Keycloak`}
          disableButton={disableButton}
          resultsText={results}
          error={error}
          isValidating={isValidating}
          validationFunction={validateFn}
          adminLink={adminLink}
          adminButtonText={`Manage ${idpCommonName} in Keycloak`}
        />
      ),
      nextButtonText: "Finish",
      hideCancelButton: true,
      enableNext: stepIdReached === finishStep,
      canJumpTo: stepIdReached >= 6,
    },
  ];

  return (
    <>
      <Header logo={JumpCloudLogo} />
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
