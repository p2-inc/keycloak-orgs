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
import axios from "axios";
import { useHistory } from "react-router";
import { useKeycloak } from "@react-keycloak/web";
import { generateId } from "@app/utils/generate-id";
import {
  API_RETURN,
  API_STATUS,
  METADATA_CONFIG,
} from "@app/configurations/api-status";
import IdentityProviderRepresentation from "@keycloak/keycloak-admin-client/lib/defs/identityProviderRepresentation";
import { useNavigateToBasePath } from "@app/routes";

const nanoId = generateId();

export const AWSSamlWizard: FC = () => {
  const [alias, setAlias] = useState(`auth0-oidc-${nanoId}`);
  const navigateToBasePath = useNavigateToBasePath();

  const title = "AWS wizard";
  const [stepIdReached, setStepIdReached] = useState(1);
  const [kcAdminClient] = useKeycloakAdminApi();
  const { keycloak } = useKeycloak();
  const history = useHistory();

  const samlAudience = `${process.env.KEYCLOAK_URL}/realms/${process.env.REALM}/broker/${alias}/endpoint`;
  const acsURL = `${process.env.KEYCLOAK_URL}/realms/${process.env.REALM}`;

  const [providerUrl, setProviderUrl] = useState("");
  const [metadata, setMetadata] = useState<METADATA_CONFIG>();
  const [isFormValid, setIsFormValid] = useState(false);

  // Complete
  const [isValidating, setIsValidating] = useState(false);
  const [results, setResults] = useState("");
  const [error, setError] = useState<null | boolean>(null);
  const [disableButton, setDisableButton] = useState(false);

  const Axios = axios.create({
    headers: {
      authorization: `bearer ${keycloak.token}`,
    },
  });

  const onNext = (newStep) => {
    if (stepIdReached === steps.length + 1) {
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
        realm: process.env.REALM,
      });

      setMetadata(resp);
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

    const payload: IdentityProviderRepresentation = {
      alias: alias,
      displayName: "AWS SSO Saml",
      providerId: "saml",
      config: metadata!,
    };

    try {
      await kcAdminClient.identityProviders.create({
        ...payload,
        realm: process.env.REALM!,
      });

      // For AWS SSO, additional mapping call is required after creation

      // Have to use Axios post bc built in keycloak-js makes the request wrong
      await Axios.post(
        `${process.env.KEYCLOAK_URL}/admin/realms/${process.env.REALM}/identity-provider/instances/${alias}/mappers`,
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

      setResults("AWS SAML IdP created successfully. Click finish.");
      setStepIdReached(7);
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
      name: "Upload AWS SSO IdP Information",
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
          buttonText="Create AWS SSO IdP in Keycloak"
          disableButton={disableButton}
          resultsText={results}
          error={error}
          isValidating={isValidating}
          validationFunction={validateFn}
        />
      ),
      nextButtonText: "Finish",
      hideCancelButton: true,
      enableNext: stepIdReached === 7,
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
