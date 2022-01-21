import React, { FC, useState } from "react";
import {
  PageSection,
  PageSectionVariants,
  PageSectionTypes,
  Wizard,
} from "@patternfly/react-core";
import AWSLogo from "@app/images/provider-logos/aws.jpg";
import { Header, WizardConfirmation } from "@wizardComponents";
import { Step1, Step2, Step3, Step4, Step5 } from "./steps";
import { useKeycloakAdminApi } from "@app/hooks/useKeycloakAdminApi";
import axios from "axios";
import { useHistory } from "react-router";
import { useKeycloak } from "@react-keycloak/web";
import { generateId } from "@app/utils/generate-id";

const nanoId = generateId();

export const AWSSamlWizard: FC = () => {
  const [alias, setAlias] = useState(`auth0-oidc-${nanoId}`);

  const title = "Okta wizard";
  const [stepIdReached, setStepIdReached] = useState(1);
  const [kcAdminClient] = useKeycloakAdminApi();
  const { keycloak } = useKeycloak();
  const history = useHistory();

  const samlAudience = `${process.env.KEYCLOAK_URL}/realms/${process.env.REALM}/broker/${alias}/endpoint`;
  const acsURL = `${process.env.KEYCLOAK_URL}/realms/${process.env.REALM}`;

  // Complete
  const [isValidating, setIsValidating] = useState(false);
  const [results, setResults] = useState("");
  const [error, setError] = useState(null);
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

  const validateFn = () => {
    // On final validation set stepIdReached to steps.length+1
    console.log("validated!");
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
      component: <Step2 />,
      hideCancelButton: true,
      enableNext: true,
    },
    {
      id: 3,
      name: "Enter Service Provider Details",
      component: <Step3 urls={{ samlAudience, acsURL }} />,
      hideCancelButton: true,
      enableNext: true,
    },
    {
      id: 4,
      name: "Configure Attribute Mapping",
      component: <Step4 />,
      hideCancelButton: true,
      enableNext: true,
    },
    {
      id: 5,
      name: "Assign Users and Groups",
      component: <Step5 />,
      hideCancelButton: true,
      enableNext: true,
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
      <Header logo={AWSLogo} />
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
