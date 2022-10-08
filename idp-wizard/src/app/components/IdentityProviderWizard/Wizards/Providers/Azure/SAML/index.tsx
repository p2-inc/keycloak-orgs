import React, { FC, useEffect, useState } from "react";
import {
  PageSection,
  PageSectionVariants,
  PageSectionTypes,
  Wizard,
} from "@patternfly/react-core";
import * as Steps from "./Steps";
import azureLogo from "@app/images/provider-logos/azure_logo.svg";
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
  SamlUserAttributeMapper,
  Axios,
} from "@wizardServices";
import { useApi, usePrompt } from "@app/hooks";

export const AzureWizard: FC = () => {
  const idpCommonName = "Azure SAML IdP";
  const alias = getAlias({
    provider: Providers.AZURE,
    protocol: Protocols.SAML,
    preface: "azure-saml",
  });
  const navigateToBasePath = useNavigateToBasePath();
  const [stepIdReached, setStepIdReached] = useState(1);
  const [results, setResults] = useState("");
  const [error, setError] = useState<null | boolean>(null);
  const [isFormValid, setIsFormValid] = useState(false);
  const [isValidating, setIsValidating] = useState(false);
  const [disableButton, setDisableButton] = useState(false);

  const { getRealm } = useKeycloakAdminApi();
  const {
    setAlias,
    adminLinkSaml: adminLink,
    identifierURL,
    createIdPUrl,
    loginRedirectURL: acsUrl,
    entityId,
    baseServerRealmsUrl,
  } = useApi();

  const [metadata, setMetadata] = useState<METADATA_CONFIG>();
  const [metadataUrl, setMetadataUrl] = useState("");

  useEffect(() => {
    setAlias(alias);
  }, [alias]);

  const finishStep = 7;

  usePrompt(
    "The wizard is incomplete. Leaving will lose any saved progress. Are you sure?",
    stepIdReached < finishStep
  );

  const onNext = (newStep) => {
    if (stepIdReached === finishStep) {
      clearAlias({
        provider: Providers.AZURE,
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
    } catch (e) {
      console.log(err);
    }

    return {
      status: API_STATUS.ERROR,
      message: `Configuration validation failed with ${idpCommonName}. Check URL and try again.`,
    };
  };

  const createIdP = async () => {
    // On final validation set stepIdReached to steps.length+1
    setIsValidating(true);
    setDisableButton(false);
    setResults(`Creating ${idpCommonName}...`);

    const payload: IdentityProviderRepresentation = {
      alias,
      displayName: `Azure SAML Single Sign-on`,
      providerId: "saml",
      config: metadata!,
    };

    try {
      await Axios.post(createIdPUrl, payload);

      // Map attributes
      await SamlUserAttributeMapper({
        alias,
        keys: {
          serverUrl: baseServerRealmsUrl,
          realm: getRealm()!,
        },
        attributes: [
          {
            attributeName:
              "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/emailaddress",
            friendlyName: "",
            userAttribute: "email",
          },
          {
            attributeName:
              "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/givenname",
            friendlyName: "",
            userAttribute: "firstName",
          },
          {
            attributeName:
              "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/surname",
            friendlyName: "",
            userAttribute: "lastName",
          },
        ],
      });

      setResults(`${idpCommonName} created successfully. Click finish.`);
      setStepIdReached(finishStep);
      setError(false);
      setDisableButton(true);
    } catch (e) {
      setResults(
        `Error creating ${idpCommonName}. Please confirm there is no Azure SAML configured already.`
      );
      setError(true);
    } finally {
      setIsValidating(false);
    }
  };

  const steps = [
    {
      id: 1,
      name: "Create Enterprise Application",
      component: <Steps.AzureStepOne />,
      hideCancelButton: true,
    },
    {
      id: 2,
      name: "Configure Attribute Statements",
      component: <Steps.AzureStepTwo acsUrl={acsUrl} entityId={entityId} />,
      hideCancelButton: true,
      canJumpTo: stepIdReached >= 2,
    },
    {
      id: 3,
      name: "Upload Azure SAML Metadata file",
      component: (
        <Steps.AzureStepThree
          handleFormSubmit={handleFormSubmit}
          url={metadataUrl}
        />
      ),
      hideCancelButton: true,
      canJumpTo: stepIdReached >= 3,
      enableNext: isFormValid,
    },
    {
      id: 4,
      name: "User Attributes & Claims",
      component: <Steps.AzureStepFour />,
      hideCancelButton: true,
      canJumpTo: stepIdReached >= 4,
    },
    {
      id: 5,
      name: "Assign People & Groups",
      component: <Steps.AzureStepFive />,
      hideCancelButton: true,
      canJumpTo: stepIdReached >= 5,
    },
    {
      name: "Confirmation",
      component: (
        <WizardConfirmation
          title="SSO Configuration Complete"
          message="Your users can now sign-in with Azure AD."
          buttonText={`Create ${idpCommonName} in Keycloak`}
          resultsText={results}
          error={error}
          isValidating={isValidating}
          validationFunction={createIdP}
          disableButton={disableButton}
          adminLink={adminLink}
          adminButtonText={`Manage ${idpCommonName} in Keycloak`}
        />
      ),
      nextButtonText: "Finish",
      hideCancelButton: true,
      canJumpTo: stepIdReached >= 6,
      enableNext: stepIdReached === finishStep,
    },
  ];

  const title = "Azure wizard";

  return (
    <>
      <Header logo={azureLogo} />
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
