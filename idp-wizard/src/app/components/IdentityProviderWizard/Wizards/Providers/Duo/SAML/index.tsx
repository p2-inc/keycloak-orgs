import React, { FC, useState } from "react";
import {
  PageSection,
  PageSectionVariants,
  PageSectionTypes,
  Wizard,
} from "@patternfly/react-core";
import * as Steps from "./Steps";
import duoLogo from "@app/images/duo/duo-large.svg";
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
import { getAlias, clearAlias, SamlUserAttributeMapper } from "@wizardServices";
import { usePrompt } from "@app/hooks";

export const DuoWizard: FC = () => {
  const idpCommonName = "Duo SAML IdP";
  const alias = getAlias({
    provider: Providers.DUO,
    protocol: Protocols.SAML,
    preface: "duo-saml",
  });
  const navigateToBasePath = useNavigateToBasePath();
  const [stepIdReached, setStepIdReached] = useState(1);
  const [results, setResults] = useState("");
  const [error, setError] = useState<null | boolean>(null);
  const [isFormValid, setIsFormValid] = useState(false);
  const [isValidating, setIsValidating] = useState(false);
  const [disableButton, setDisableButton] = useState(false);
  const [
    kcAdminClient,
    setKcAdminClientAccessToken,
    getServerUrl,
    getRealm,
    getAuthRealm,
  ] = useKeycloakAdminApi();
  const [metadata, setMetadata] = useState<METADATA_CONFIG>();
  const [metadataUrl, setMetadataUrl] = useState("");
  const adminLink = `${getServerUrl()}/admin/${getAuthRealm()}/console/#/realms/${getRealm()}/identity-provider-settings/provider/saml/${alias}`;

  const acsUrl = `${getServerUrl()}/realms/${getRealm()}/broker/${alias}/endpoint`;
  const entityId = `${getServerUrl()}/realms/${getRealm()}`;

  const finishStep = 6;

  usePrompt(
    "The wizard is incomplete. Leaving will lose any saved progress. Are you sure?",
    stepIdReached < finishStep
  );

  const onNext = (newStep) => {
    if (stepIdReached === finishStep) {
      clearAlias({
        provider: Providers.DUO,
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
      const resp = await kcAdminClient.identityProviders.importFromUrl({
        fromUrl: url,
        providerId: "saml",
        realm: getRealm(),
      });

      setMetadata({
        ...SamlIDPDefaults,
        ...resp,
      });
      setIsFormValid(true);

      return {
        status: API_STATUS.SUCCESS,
        message: `Configuration successfully validated with ${idpCommonName}. Continue to next step.`,
      };
    } catch (e) {
      return {
        status: API_STATUS.ERROR,
        message: `Configuration validation failed with ${idpCommonName}. Check URL and try again.`,
      };
    }
  };

  const createDuoSamlIdP = async () => {
    // On final validation set stepIdReached to steps.length+1
    setIsValidating(true);
    setDisableButton(false);
    setResults(`Creating ${idpCommonName}...`);

    const payload: IdentityProviderRepresentation = {
      alias: alias,
      displayName: `Duo SAML Single Sign-on`,
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
              "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/email",
            friendlyName: "",
            userAttribute: "email",
          },
          {
            attributeName:
              "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/firstname",
            friendlyName: "",
            userAttribute: "firstName",
          },
          {
            attributeName:
              "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/lastname",
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
        `Error creating ${idpCommonName}. Please confirm there is no Duo SAML configured already.`
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
      component: <Steps.DuoStepOne />,
      hideCancelButton: true,
    },
    {
      id: 2,
      name: "Upload SAML Metadata",
      component: (
        <Steps.DuoStepTwo
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
      component: <Steps.DuoStepThree acsUrl={acsUrl} entityId={entityId} />,
      hideCancelButton: true,
      canJumpTo: stepIdReached >= 3,
    },
    {
      id: 4,
      name: "Configure Attribute Mapping",
      component: <Steps.DuoStepFour />,
      hideCancelButton: true,
      canJumpTo: stepIdReached >= 4,
    },
    {
      id: 5,
      name: "Assign People & Groups",
      component: <Steps.DuoStepFive />,
      hideCancelButton: true,
      canJumpTo: stepIdReached >= 5,
    },
    {
      name: "Confirmation",
      component: (
        <WizardConfirmation
          title="SSO Configuration Complete"
          message="Your users can now sign-in with Duo AD."
          buttonText={`Create ${idpCommonName} in Keycloak`}
          resultsText={results}
          error={error}
          isValidating={isValidating}
          validationFunction={createDuoSamlIdP}
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

  const title = "Duo wizard";

  return (
    <>
      <Header logo={duoLogo} />
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
