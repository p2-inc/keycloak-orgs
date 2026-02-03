import React, { FC, useEffect, useState } from "react";
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
import {
  CreateIdp,
  SamlAttributeMapper,
} from "@app/components/IdentityProviderWizard/Wizards/services";
import { Axios } from "@wizardServices";
import { useNavigateToBasePath } from "@app/routes";
import { getAlias, clearAlias } from "@wizardServices";
import { Providers, Protocols, SamlIDPDefaults } from "@app/configurations";
import { useApi, usePrompt } from "@app/hooks";
import { useGetFeatureFlagsQuery } from "@app/services";
import { useGenerateIdpDisplayName } from "@app/hooks/useGenerateIdpDisplayName";
import { useCreateTestIdpLink } from "@app/hooks/useCreateTestIdpLink";

export const JumpCloudWizard: FC = () => {
  const idpCommonName = "JumpCloud IdP";

  const { data: featureFlags } = useGetFeatureFlagsQuery();
  const navigateToBasePath = useNavigateToBasePath();
  const title = "JumpCloud wizard";
  const [stepIdReached, setStepIdReached] = useState(1);
  const {
    alias,
    setAlias,
    loginRedirectURL: acsUrl,
    entityId,
    adminLinkSaml: adminLink,
    identifierURL,
    createIdPUrl,
  } = useApi();
  const { generateIdpDisplayName } = useGenerateIdpDisplayName();

  const [metadata, setMetadata] = useState<METADATA_CONFIG>();
  const [isFormValid, setIsFormValid] = useState(false);

  // Complete
  const [isValidating, setIsValidating] = useState(false);
  const [results, setResults] = useState("");
  const [error, setError] = useState<null | boolean>(null);
  const [disableButton, setDisableButton] = useState(false);

  useEffect(() => {
    const genAlias = getAlias({
      provider: Providers.JUMP_CLOUD,
      protocol: Protocols.SAML,
      preface: "jumpcloud-saml",
    });
    setAlias(genAlias);
  }, []);

  const finishStep = 7;

  usePrompt(
    "The wizard is incomplete. Leaving will lose any saved progress. Are you sure?",
    stepIdReached < finishStep,
  );

  const { isValidationPendingForAlias } = useCreateTestIdpLink();
  const [idpTestLink, setIdpTestLink] = useState<string>("");
  const checkPendingValidationStatus = async () => {
    const pendingLink = await isValidationPendingForAlias(alias);
    if (pendingLink) {
      setIdpTestLink(pendingLink);
    }
  };

  const onNext = (newStep) => {
    if (stepIdReached === finishStep) {
      clearAlias({
        provider: Providers.JUMP_CLOUD,
        protocol: Protocols.SAML,
      });
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
        setMetadata({
          ...SamlIDPDefaults,
          ...resp.data,
        });
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

  const createIdP = async () => {
    setIsValidating(true);
    setDisableButton(false);
    setResults(`Creating ${idpCommonName}...`);

    const payload: IdentityProviderRepresentation = {
      alias,
      displayName: generateIdpDisplayName(alias),
      hideOnLogin: true,
      providerId: "saml",
      config: metadata!,
    };

    try {
      await CreateIdp({ createIdPUrl, payload, featureFlags });

      await SamlAttributeMapper({
        alias,
        createIdPUrl,
        usernameAttribute: { attributeName: "username", friendlyName: "" },
        emailAttribute: { attributeName: "email", friendlyName: "" },
        firstNameAttribute: { attributeName: "firstName", friendlyName: "" },
        lastNameAttribute: { attributeName: "lastName", friendlyName: "" },
        featureFlags,
      });

      setResults(`${idpCommonName} created successfully. Click finish.`);
      setStepIdReached(finishStep);
      setError(false);
      setDisableButton(true);

      await checkPendingValidationStatus();
      clearAlias({
        provider: Providers.JUMP_CLOUD,
        protocol: Protocols.SAML,
      });
    } catch (e) {
      setResults(
        `Error creating ${idpCommonName}. Please confirm there is no ${idpCommonName} configured already.`,
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
          title="SSO Configuration Complete. Create Identity Provider."
          message="JumpCloud."
          buttonText={`Create ${idpCommonName}`}
          disableButton={disableButton}
          resultsText={results}
          error={error}
          isValidating={isValidating}
          validationFunction={createIdP}
          adminLink={adminLink}
          adminButtonText={`Manage ${idpCommonName}`}
          idpTestLink={idpTestLink}
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
