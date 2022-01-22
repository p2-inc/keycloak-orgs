import React, { FC, useState } from "react";
import {
  PageSection,
  PageSectionVariants,
  PageSectionTypes,
  Wizard,
} from "@patternfly/react-core";
import GoogleLogo from "@app/images/provider-logos/google_saml_logo.png";
import { Header, WizardConfirmation } from "@wizardComponents";
import { Step1 } from "./steps";
import { useKeycloakAdminApi } from "@app/hooks/useKeycloakAdminApi";
import axios from "axios";
import { useHistory } from "react-router";
import { useKeycloak } from "@react-keycloak/web";

export const TemplateWizardProtocol: FC = () => {
  const title = "Okta wizard";
  const [stepIdReached, setStepIdReached] = useState(1);
  const [kcAdminClient] = useKeycloakAdminApi();
  const { keycloak } = useKeycloak();
  const history = useHistory();

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
      history.push("/");
    }
    setStepIdReached(stepIdReached < newStep.id ? newStep.id : stepIdReached);
  };

  const closeWizard = () => {
    history.push("/");
  };

  const validateFn = async () => {
    // On final validation set stepIdReached to steps.length+1
    console.log("validated!");

    // setIsValidating(true);
    // setDisableButton(false);
    // setResults("Creating SAML IdP...");

    // const payload: IdentityProviderRepresentation = {
    //   alias: "generic-saml",
    //   displayName: `SAML Single Sign-on`,
    //   providerId: "saml",
    //   config: metadata!,
    // };

    // try {
    //   await kcAdminClient.identityProviders.create({
    //     ...payload,
    //     realm: process.env.REALM!,
    //   });

    //   setResults("SAML IdP created successfully. Click finish.");
    //   setStepIdReached(6);
    //   setError(false);
    //   setDisableButton(true);
    // } catch (e) {
    //   setResults(
    //     "Error creating SAML IdP. Please confirm there is no SAML configured already."
    //   );
    //   setError(true);
    // } finally {
    //   setIsValidating(false);
    // }
  };

  const steps = [
    {
      id: 1,
      name: "Step1 Title",
      component: <Step1 />,
      hideCancelButton: true,
      enableNext: true,
      canJumpTo: stepIdReached >= 1,
    },
    {
      id: 2,
      name: "Confirmation",
      component: (
        <WizardConfirmation
          title="SSO Configuration Complete"
          message="Your users can now sign-in with {{Provider}}."
          buttonText="Create {{Provider}} IdP in Keycloak"
          disableButton={disableButton}
          resultsText={results}
          error={error}
          isValidating={isValidating}
          validationFunction={validateFn}
        />
      ),
      nextButtonText: "Finish",
      hideCancelButton: true,
      enableNext: stepIdReached === 3,
      canJumpTo: stepIdReached >= 2,
    },
  ];

  return (
    <>
      <Header logo={GoogleLogo} />
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
