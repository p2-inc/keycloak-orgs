import React, { FC, useEffect, useState } from "react";
import {
  PageSection,
  PageSectionVariants,
  PageSectionTypes,
  Wizard,
} from "@patternfly/react-core";
import * as Steps from "./Steps";
import cyberarkLogo from "@app/images/provider-logos/cyberark_logo.svg";
import { WizardConfirmation, Header } from "@wizardComponents";
import { useKeycloakAdminApi } from "@app/hooks/useKeycloakAdminApi";
import IdentityProviderRepresentation from "@keycloak/keycloak-admin-client/lib/defs/identityProviderRepresentation";
import { useNavigateToBasePath } from "@app/routes";
import {
  API_RETURN,
  API_STATUS,
  METADATA_CONFIG,
} from "@app/configurations/api-status";
import { Protocols, Providers, SamlIDPDefaults } from "@app/configurations";
import {
  getAlias,
  clearAlias,
  CreateIdp,
  SamlAttributeMapper,
  Axios,
} from "@wizardServices";
import { useApi, usePrompt } from "@app/hooks";
import { useGetFeatureFlagsQuery } from "@app/services";
import { useGenerateIdpDisplayName } from "@app/hooks/useGenerateIdpDisplayName";
import { useCreateTestIdpLink } from "@app/hooks/useCreateTestIdpLink";

export const CyberArkWizard: FC = () => {
  const idpCommonName = "CyberArk SAML Identity Provider";
  const { data: featureFlags } = useGetFeatureFlagsQuery();
  const navigateToBasePath = useNavigateToBasePath();
  const [stepIdReached, setStepIdReached] = useState(1);
  const [results, setResults] = useState("");
  const [error, setError] = useState<null | boolean>(null);
  const [isFormValid, setIsFormValid] = useState(false);
  const [isValidating, setIsValidating] = useState(false);
  const [disableButton, setDisableButton] = useState(false);
  const { getRealm } = useKeycloakAdminApi();
  const {
    alias,
    setAlias,
    adminLinkSaml: adminLink,
    loginRedirectURL: acsUrl,
    entityId,
    identifierURL,
    createIdPUrl,
  } = useApi();
  const { generateIdpDisplayName } = useGenerateIdpDisplayName();

  const [metadata, setMetadata] = useState<METADATA_CONFIG>();
  const [metadataUrl, setMetadataUrl] = useState("");

  useEffect(() => {
    const genAlias = getAlias({
      provider: Providers.CYBERARK,
      protocol: Protocols.SAML,
      preface: "cyberark-saml",
    });
    setAlias(genAlias);
  }, []);

  const finishStep = 6;

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
        provider: Providers.CYBERARK,
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
    url,
  }: {
    url: string;
  }): Promise<API_RETURN> => {
    // make call to submit the URL and verify it
    setMetadataUrl(metadataUrl);

    try {
      const payload = {
        fromUrl: url,
        providerId: "saml",
        realm: getRealm(),
      };
      const resp = await Axios.post(identifierURL, payload);

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

  const createCyberarkSamlIdP = async () => {
    // On final validation set stepIdReached to steps.length+1
    setIsValidating(true);
    setDisableButton(false);
    setResults(`Creating ${idpCommonName}...`);

    const payload: IdentityProviderRepresentation = {
      alias,
      displayName: generateIdpDisplayName(alias),
      providerId: "saml",
      hideOnLogin: true,
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
        provider: Providers.CYBERARK,
        protocol: Protocols.SAML,
      });
    } catch (e) {
      setResults(
        `Error creating ${idpCommonName}. Please confirm there is no CyberArk SAML configured already.`,
      );
      setError(true);
    } finally {
      setIsValidating(false);
    }
  };

  const steps = [
    {
      id: 1,
      name: "Create Web App",
      component: <Steps.CyberArkStepOne />,
      hideCancelButton: true,
    },
    {
      id: 2,
      name: "Upload SAML Metadata",
      component: (
        <Steps.CyberArkStepTwo
          handleFormSubmit={handleFormSubmit}
          url={metadataUrl}
        />
      ),
      hideCancelButton: true,
      canJumpTo: stepIdReached >= 2,
      enableNext: isFormValid,
    },
    {
      id: 3,
      name: "Enter Service Provider Details",
      component: (
        <Steps.CyberArkStepThree acsUrl={acsUrl} entityId={entityId} />
      ),
      hideCancelButton: true,
      canJumpTo: stepIdReached >= 3,
    },
    {
      id: 4,
      name: "Configure Attribute Mapping",
      component: <Steps.CyberArkStepFour />,
      hideCancelButton: true,
      canJumpTo: stepIdReached >= 4,
    },
    {
      id: 5,
      name: "Assign Users, Groups, and Roles",
      component: <Steps.CyberArkStepFive />,
      hideCancelButton: true,
      canJumpTo: stepIdReached >= 5,
    },
    {
      name: "Confirmation",
      component: (
        <WizardConfirmation
          title="SSO Configuration Complete"
          message="Your users can now sign-in with CyberArk."
          buttonText={`Create ${idpCommonName}`}
          resultsText={results}
          error={error}
          isValidating={isValidating}
          validationFunction={createCyberarkSamlIdP}
          disableButton={disableButton}
          adminLink={adminLink}
          adminButtonText={`Manage ${idpCommonName}`}
          idpTestLink={idpTestLink}
        />
      ),
      nextButtonText: "Finish",
      hideCancelButton: true,
      canJumpTo: stepIdReached >= 6,
      enableNext: stepIdReached === finishStep,
    },
  ];

  const title = "CyberArk wizard";

  return (
    <>
      <Header logo={cyberarkLogo} />
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
