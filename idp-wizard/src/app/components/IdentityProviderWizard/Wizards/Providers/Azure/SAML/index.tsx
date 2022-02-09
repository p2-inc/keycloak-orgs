import React, { FC, useState } from "react";
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
import { SamlUserAttributeMapper } from "@app/components/IdentityProviderWizard/Wizards/services";
import { Protocols, Providers } from "@app/configurations";
import { getAlias } from "@wizardServices";

export const AzureWizard: FC = () => {
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
  const [kcAdminClient, setKcAdminClientAccessToken, getServerUrl, getRealm] =
    useKeycloakAdminApi();
  const [metadata, setMetadata] = useState<METADATA_CONFIG>();
  const [metadataUrl, setMetadataUrl] = useState("");

  const acsUrl = `${getServerUrl()}/realms/${getRealm()}/broker/${alias}/endpoint`;
  const entityId = `${getServerUrl()}/realms/${getRealm()}`;

  const onNext = (newStep) => {
    if (stepIdReached === steps.length + 1) {
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
      const resp = await kcAdminClient.identityProviders.importFromUrl({
        fromUrl: url,
        providerId: "saml",
        realm: getRealm(),
      });

      setMetadata({
        syncMode: "IMPORT",
        allowCreate: "true",
        nameIDPolicyFormat:
          "urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified",
        principalType: "SUBJECT",
        ...resp,
      });
      setIsFormValid(true);

      return {
        status: API_STATUS.SUCCESS,
        message:
          "Configuration successfully validated with OneLogin IdP. Continue to next step.",
      };
    } catch (e) {
      return {
        status: API_STATUS.ERROR,
        message:
          "Configuration validation failed with OneLogin IdP. Check URL and try again.",
      };
    }
  };

  const createAzureSamlIdP = async () => {
    // On final validation set stepIdReached to steps.length+1
    setIsValidating(true);
    setDisableButton(false);
    setResults("Creating Azure SAML IdP...");

    const payload: IdentityProviderRepresentation = {
      alias: alias,
      displayName: `Azure SAML Single Sign-on`,
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

      setResults("Azure SAML IdP created successfully. Click finish.");
      setStepIdReached(7);
      setError(false);
      setDisableButton(true);
    } catch (e) {
      setResults(
        "Error creating Azure SAML IdP. Please confirm there is no Azure SAML configured already."
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
          buttonText="Create SAML IdP in Keycloak"
          resultsText={results}
          error={error}
          isValidating={isValidating}
          validationFunction={createAzureSamlIdP}
          disableButton={disableButton}
        />
      ),
      nextButtonText: "Finish",
      hideCancelButton: true,
      canJumpTo: stepIdReached >= 6,
      enableNext: stepIdReached === 7,
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
