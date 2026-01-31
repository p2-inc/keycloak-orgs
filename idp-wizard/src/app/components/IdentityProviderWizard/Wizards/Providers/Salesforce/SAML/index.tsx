import React, { FC, useState, useEffect } from "react";
import {
  PageSection,
  PageSectionVariants,
  PageSectionTypes,
  Wizard,
} from "@patternfly/react-core";
import {
  API_STATUS,
  METADATA_CONFIG,
  API_RETURN,
} from "@app/configurations/api-status";
import { Axios, clearAlias } from "@wizardServices";
import * as Steps from "./Steps";
import * as SharedSteps from "../shared/Steps";
import salesforceLogo from "@app/images/salesforce/salesforce-logo.png";
import { WizardConfirmation, Header } from "@wizardComponents";
import { useKeycloakAdminApi } from "@app/hooks/useKeycloakAdminApi";
import IdentityProviderRepresentation from "@keycloak/keycloak-admin-client/lib/defs/identityProviderRepresentation";
import {
  CreateIdp,
  SamlAttributeMapper,
} from "@app/components/IdentityProviderWizard/Wizards/services";
import { useNavigateToBasePath } from "@app/routes";
import { getAlias } from "@wizardServices";
import { Protocols, Providers, SamlIDPDefaults } from "@app/configurations";
import { useApi, usePrompt } from "@app/hooks";
import { useGetFeatureFlagsQuery } from "@app/services";
import { useGenerateIdpDisplayName } from "@app/hooks/useGenerateIdpDisplayName";
import { useCreateTestIdpLink } from "@app/hooks/useCreateTestIdpLink";

export const SalesforceWizardSAML: FC = () => {
  const idpCommonName = "Salesforce SAML Identity Provider";

  const navigateToBasePath = useNavigateToBasePath();
  const { data: featureFlags } = useGetFeatureFlagsQuery();
  const {
    alias,
    setAlias,
    adminLinkSaml: adminLink,
    identifierURL,
    createIdPUrl,
    loginRedirectURL: acsUrl,
    entityId,
  } = useApi();
  const { generateIdpDisplayName } = useGenerateIdpDisplayName();

  useEffect(() => {
    const genAlias = getAlias({
      provider: Providers.SALESFORCE,
      protocol: Protocols.SAML,
      preface: "salesforce-saml",
    });
    setAlias(genAlias);
  }, []);

  const [stepIdReached, setStepIdReached] = useState(1);
  const [results, setResults] = useState("");
  const [error, setError] = useState(false);
  const [disableButton, setDisableButton] = useState(false);
  const [isFormValid, setIsFormValid] = useState(false);

  const [configData, setConfigData] = useState<METADATA_CONFIG | null>(null);
  const [metadataUrl, setMetadataUrl] = useState("");
  const [isValidating, setIsValidating] = useState(false);
  const { getRealm } = useKeycloakAdminApi();

  const finishStep = 8;

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
        provider: Providers.SALESFORCE,
        protocol: Protocols.SAML,
      });
      navigateToBasePath();
    }
    setStepIdReached(stepIdReached < newStep.id ? newStep.id : stepIdReached);
  };

  const closeWizard = () => navigateToBasePath();

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
        setConfigData({
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
    setResults(`Creating ${idpCommonName}...`);

    const payload: IdentityProviderRepresentation = {
      alias,
      displayName: generateIdpDisplayName(alias),
      hideOnLogin: true,
      providerId: "saml",
      config: configData!,
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
        provider: Providers.SALESFORCE,
        protocol: Protocols.SAML,
      });
    } catch (e) {
      setResults(`Error creating ${idpCommonName}.`);
      setError(true);
    } finally {
      setIsValidating(false);
    }

    setIsValidating(false);
  };

  const steps = [
    {
      id: 1,
      name: "Enable Identity Provider",
      component: <Steps.SalesforceStepOne />,
      hideCancelButton: true,
      enabledNext: true,
      canJumpTo: stepIdReached >= 1,
    },
    {
      id: 2,
      name: "Create Connected App",
      component: <SharedSteps.SalesforceStepConnectedApp stepNumber={2} />,
      hideCancelButton: true,
      enableNext: true,
      canJumpTo: stepIdReached >= 2,
    },
    {
      id: 3,
      name: "Enter Service Provider Details",
      component: (
        <Steps.SalesforceStepThree entityId={entityId} acsUrl={acsUrl} />
      ),
      hideCancelButton: true,
      enableNext: true,
      canJumpTo: stepIdReached >= 3,
    },
    {
      id: 4,
      name: "Upload Salesforce IdP Information",
      component: (
        <Steps.SalesforceStepFour
          handleFormSubmit={handleFormSubmit}
          url={metadataUrl}
        />
      ),
      hideCancelButton: true,
      canJumpTo: stepIdReached >= 4,
      enableNext: isFormValid,
    },
    {
      id: 5,
      name: "Assign Profiles",
      component: <Steps.SalesforceStepFive />,
      hideCancelButton: true,
      enabledNext: true,
      canJumpTo: stepIdReached >= 5,
    },
    {
      id: 6,
      name: "Configure Attribute Mapping",
      component: <Steps.SalesforceStepSix />,
      hideCancelButton: true,
      canJumpTo: stepIdReached >= 6,
    },
    {
      id: 7,
      name: "Confirmation",
      component: (
        <WizardConfirmation
          title="SSO Configuration Complete. Create Identity Provider."
          message="Salesforce."
          buttonText={`Create ${idpCommonName}`}
          resultsText={results}
          error={error}
          isValidating={isValidating}
          validationFunction={createIdP}
          disableButton={disableButton}
          adminLink={adminLink}
          adminButtonText={`Manage ${idpCommonName}`}
          idpTestLink={idpTestLink}
        />
      ),
      nextButtonText: "Finish",
      hideCancelButton: true,
      enableNext: stepIdReached === finishStep,
      canJumpTo: stepIdReached >= 7,
    },
  ];

  const title = "Salesforce wizard";

  return (
    <>
      <Header logo={salesforceLogo} />
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
