import React, { FC, useState } from "react";
import {
  PageSection,
  PageSectionVariants,
  PageSectionTypes,
  Wizard,
} from "@patternfly/react-core";
import OktaLogo from "@app/images/okta/okta-logo.png";
import { Header, WizardConfirmation } from "@wizardComponents";
import { Step1, Step2, Step3, Step4, Step5, Step6 } from "./Steps";
import { useKeycloakAdminApi } from "@app/hooks/useKeycloakAdminApi";
import IdentityProviderRepresentation from "@keycloak/keycloak-admin-client/lib/defs/identityProviderRepresentation";
import { API_STATUS } from "@app/configurations/api-status";
import { useNavigateToBasePath } from "@app/routes";
import { getAlias } from "@wizardServices";
import { Providers, Protocols } from "@app/configurations";

export const OktaWizardSaml: FC = () => {
  const title = "Okta wizard";
  const navigateToBasePath = useNavigateToBasePath();

  const [metadata, setMetadata] = useState();
  const [stepIdReached, setStepIdReached] = useState(1);
  const [kcAdminClient, setKcAdminClientAccessToken, getServerUrl, getRealm] =
    useKeycloakAdminApi();

  const alias = getAlias({
    provider: Providers.AWS,
    protocol: Protocols.SAML,
    preface: "okta-saml",
  });
  const ssoUrl = `${getServerUrl()}/admin/realms/${getRealm()}/broker/${alias}/endpoint`;
  const audienceUri = `${getServerUrl()}/realms/${getRealm()}`;

  // Complete
  const [isValidating, setIsValidating] = useState(false);
  const [results, setResults] = useState("");
  const [error, setError] = useState<null | boolean>(null);
  const [disableButton, setDisableButton] = useState(false);

  const onNext = (newStep) => {
    if (stepIdReached === steps.length + 1) {
      navigateToBasePath();
    }
    setStepIdReached(stepIdReached < newStep.id ? newStep.id : stepIdReached);
  };

  const closeWizard = () => {
    navigateToBasePath();
  };

  const validateMetadata = async ({ metadataUrl }: { metadataUrl: string }) => {
    // make call to submit the URL and verify it

    try {
      const resp = await kcAdminClient.identityProviders.importFromUrl({
        fromUrl: metadataUrl,
        providerId: "saml",
        realm: getRealm(),
      });

      setMetadata(resp);

      return {
        status: API_STATUS.SUCCESS,
        message:
          "Configuration successfully validated with Okta. Continue to next step.",
      };
    } catch (e) {
      return {
        status: API_STATUS.ERROR,
        message:
          "Configuration validation failed with Okta. Check URL and try again.",
      };
    }
  };

  const createOktaSamlIdp = async () => {
    // On final validation set stepIdReached to steps.length+1
    setIsValidating(true);
    setResults("Creating Okta SAML IdP...");

    const payload: IdentityProviderRepresentation = {
      alias: alias,
      displayName: `Okta SAML Single Sign-on ${alias}`,
      providerId: "saml",
      config: metadata!,
    };

    try {
      await kcAdminClient.identityProviders.create({
        ...payload,
        realm: getRealm()!,
      });

      setResults("Okta SAML IdP created successfully. Click finish.");
      setStepIdReached(8);
      setError(false);
      setDisableButton(true);
    } catch (e) {
      setResults("Error creating Okta SAML IdP.");
      setError(true);
    } finally {
      setIsValidating(false);
    }
  };

  const steps = [
    {
      id: 1,
      name: "Add a SAML Application",
      component: <Step1 />,
      hideCancelButton: true,
    },
    {
      id: 2,
      name: "Enter Service Provider Details",
      component: <Step2 ssoUrl={ssoUrl} audienceUri={audienceUri} />,
      hideCancelButton: true,
      canJumpTo: stepIdReached >= 2,
    },
    {
      id: 3,
      name: "Configure Attribute Mapping",
      component: <Step3 />,
      hideCancelButton: true,
      canJumpTo: stepIdReached >= 3,
    },
    {
      id: 4,
      name: "Complete Feedback Section",
      component: <Step4 />,
      hideCancelButton: true,
      canJumpTo: stepIdReached >= 4,
    },
    {
      id: 5,
      name: "Assign People and Groups",
      component: <Step5 />,
      hideCancelButton: true,
      canJumpTo: stepIdReached >= 5,
    },
    {
      id: 6,
      name: "Upload Okta IdP Information",
      component: <Step6 validateMetadata={validateMetadata} />,
      hideCancelButton: true,
      canJumpTo: stepIdReached >= 6,
    },
    {
      id: 7,
      name: "Confirmation",
      component: (
        <WizardConfirmation
          title="SSO Configuration Complete"
          message="Your users can now sign-in with Okta SAML."
          buttonText="Create Okta SAML IdP in Keycloak"
          disableButton={disableButton}
          resultsText={results}
          error={error}
          isValidating={isValidating}
          validationFunction={createOktaSamlIdp}
        />
      ),
      nextButtonText: "Finish",
      hideCancelButton: true,
      enableNext: stepIdReached === 8,
      canJumpTo: stepIdReached >= 7,
    },
  ];

  return (
    <>
      <Header logo={OktaLogo} />
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
