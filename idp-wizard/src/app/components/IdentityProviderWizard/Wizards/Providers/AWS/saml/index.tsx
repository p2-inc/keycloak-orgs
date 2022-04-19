import React, { FC, useState } from "react";
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
import { Axios, clearAlias } from "@wizardServices";
import {
  API_RETURN,
  API_STATUS,
  METADATA_CONFIG,
} from "@app/configurations/api-status";
import IdentityProviderRepresentation from "@keycloak/keycloak-admin-client/lib/defs/identityProviderRepresentation";
import { useNavigateToBasePath } from "@app/routes";
import { getAlias } from "@wizardServices";
import { Protocols, Providers, SamlIDPDefaults } from "@app/configurations";
import { usePrompt } from "@app/hooks";

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
  const [
    kcAdminClient,
    setKcAdminClientAccessToken,
    getServerUrl,
    getRealm,
    getAuthRealm,
  ] = useKeycloakAdminApi();

  const acsURL = `${getServerUrl()}/realms/${getRealm()}/broker/${alias}/endpoint`;
  const samlAudience = `${getServerUrl()}/realms/${getRealm()}`;

  const [providerUrl, setProviderUrl] = useState("");
  const [metadata, setMetadata] = useState<METADATA_CONFIG>();
  const [isFormValid, setIsFormValid] = useState(false);
  const adminLink = `${getServerUrl()}/admin/${getAuthRealm()}/console/#/realms/${getRealm()}/identity-provider-settings/provider/saml/${alias}`;

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
        message:
          "Configuration successfully validated with AWS SSO SAML. Continue to next step.",
      };
    } catch (e) {
      return {
        status: API_STATUS.ERROR,
        message:
          "Configuration validation failed with AWS SSO SAML. Check URL and try again.",
      };
    }
  };

  const validateFn = async () => {
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
      alias: alias,
      displayName: "AWS SSO Saml",
      providerId: "saml",
      config: metadata!,
    };

    try {
      await kcAdminClient.identityProviders.create({
        ...payload,
        realm: getRealm()!,
      });

      // For AWS SSO, additional mapping call is required after creation
      // TODO we should abstract this out into a class that executes API methods
      // Have to use Axios post bc built in keycloak-js makes the request wrong
      await Axios.post(
        `${getServerUrl()}/admin/realms/${getRealm()}/identity-provider/instances/${alias}/mappers`,
        {
          identityProviderAlias: alias,
          config: {
            syncMode: "INHERIT",
            attributes: "[]",
            "attribute.friendly.name": "email",
            "user.attribute": "email",
          },
          name: "email",
          identityProviderMapper: "saml-user-attribute-idp-mapper",
        }
      );
      await Axios.post(
        `${getServerUrl()}/admin/realms/${getRealm()}/identity-provider/instances/${alias}/mappers`,
        {
          identityProviderAlias: alias,
          config: {
            syncMode: "INHERIT",
            attributes: "[]",
            "attribute.friendly.name": "firstName",
            "user.attribute": "firstName",
          },
          name: "firstName",
          identityProviderMapper: "saml-user-attribute-idp-mapper",
        }
      );
      await Axios.post(
        `${getServerUrl()}/admin/realms/${getRealm()}/identity-provider/instances/${alias}/mappers`,
        {
          identityProviderAlias: alias,
          config: {
            syncMode: "INHERIT",
            attributes: "[]",
            "attribute.friendly.name": "lastName",
            "user.attribute": "lastName",
          },
          name: "lastName",
          identityProviderMapper: "saml-user-attribute-idp-mapper",
        }
      );

      setResults("AWS SAML IdP created successfully. Click finish.");
      setStepIdReached(finishStep);
      setError(false);
      setDisableButton(true);
    } catch (e) {
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
      component: <Step3 urls={{ samlAudience, acsURL }} />,
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
