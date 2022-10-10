import React, { FC, useEffect, useState } from "react";
import {
  PageSection,
  PageSectionVariants,
  PageSectionTypes,
  Wizard,
} from "@patternfly/react-core";
import { AWS_LOGO } from "@app/images/aws";
import { Header, WizardConfirmation } from "@wizardComponents";
import { Step1, Step2, Step3, Step4, Step5 } from "./steps";
import { useKeycloakAdminApi } from "@app/hooks/useKeycloakAdminApi";
import { Axios, clearAlias, SamlUserAttributeMapper } from "@wizardServices";
import {
  API_RETURN,
  API_STATUS,
  METADATA_CONFIG,
} from "@app/configurations/api-status";
import IdentityProviderRepresentation from "@keycloak/keycloak-admin-client/lib/defs/identityProviderRepresentation";
import { useNavigateToBasePath } from "@app/routes";
import { getAlias } from "@wizardServices";
import { Protocols, Providers, SamlIDPDefaults } from "@app/configurations";
import { useApi, usePrompt } from "@app/hooks";

export const AWSSamlWizard: FC = () => {
  const idpCommonName = "AWS SSO IdP";
  const alias = getAlias({
    provider: Providers.AWS,
    protocol: Protocols.SAML,
    preface: "awssso-saml",
  });
  const navigateToBasePath = useNavigateToBasePath();

  const title = "AWS wizard";
  const [stepIdReached, setStepIdReached] = useState(1);
  const { getRealm } = useKeycloakAdminApi();

  const {
    setAlias,
    identifierURL,
    createIdPUrl,
    loginRedirectURL,
    adminLinkSaml: adminLink,
    entityId,
    baseServerRealmsUrl,
  } = useApi();

  useEffect(() => {
    setAlias(alias);
  }, [alias]);

  const [providerUrl, setProviderUrl] = useState("");
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
      clearAlias({
        provider: Providers.AWS,
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
    setProviderUrl(url);

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

  const createIdP = async () => {
    // On final validation set stepIdReached to steps.length+1
    setIsValidating(true);
    setDisableButton(false);
    setResults("Creating AWS SSO SAML IdP...");

    // Set defaults that may not have come through with the metadata
    metadata!.syncMode = "IMPORT";
    metadata!.allowCreate = "true";
    metadata!.nameIDPolicyFormat =
      "urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified";
    metadata!.principalType = "SUBJECT";

    const payload: IdentityProviderRepresentation = {
      alias,
      displayName: "AWS SSO Saml",
      providerId: "saml",
      config: metadata!,
    };

    try {
      await Axios.post(createIdPUrl, payload);

      await SamlUserAttributeMapper({
        alias,
        createIdPUrl,
        keys: { serverUrl: baseServerRealmsUrl, realm: getRealm()! },
        attributes: [
          {
            attributeName: "email",
            friendlyName: "email",
            userAttribute: "email",
          },
          {
            attributeName: "firstName",
            friendlyName: "firstName",
            userAttribute: "firstName",
          },
          {
            attributeName: "lastName",
            friendlyName: "lastName",
            userAttribute: "lastName",
          },
        ],
      });

      setResults("AWS SAML IdP created successfully. Click finish.");
      setStepIdReached(finishStep);
      setError(false);
      setDisableButton(true);
    } catch (e) {
      console.error(e);
      setResults(
        "Error creating AWS SAML IdP. Please check values or confirm there is no SAML configured already."
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
    },
    {
      id: 2,
      name: `Upload ${idpCommonName} Information`,
      component: (
        <Step2 url={providerUrl} handleFormSubmit={handleFormSubmit} />
      ),
      hideCancelButton: true,
      enableNext: isFormValid,
      canJumpTo: stepIdReached >= 2,
    },
    {
      id: 3,
      name: "Enter Service Provider Details",
      component: (
        <Step3 urls={{ samlAudience: entityId, acsURL: loginRedirectURL }} />
      ),
      hideCancelButton: true,
      enableNext: true,
      canJumpTo: stepIdReached >= 3,
    },
    {
      id: 4,
      name: "Configure Attribute Mapping",
      component: <Step4 />,
      hideCancelButton: true,
      enableNext: true,
      canJumpTo: stepIdReached >= 4,
    },
    {
      id: 5,
      name: "Assign Users and Groups",
      component: <Step5 />,
      hideCancelButton: true,
      enableNext: true,
      canJumpTo: stepIdReached >= 5,
    },
    {
      id: 6,
      name: "Confirmation",
      component: (
        <WizardConfirmation
          title="SSO Configuration Complete"
          message="Your users can now sign-in with AWS SSO."
          buttonText={`Create ${idpCommonName} in Keycloak`}
          disableButton={disableButton}
          resultsText={results}
          error={error}
          isValidating={isValidating}
          validationFunction={createIdP}
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
      <Header logo={AWS_LOGO} />
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
