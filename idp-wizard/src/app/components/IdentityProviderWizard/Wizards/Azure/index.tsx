import React, { FC, useEffect, useState } from "react";
import {
  PageSection,
  PageSectionVariants,
  PageSectionTypes,
  Wizard,
} from "@patternfly/react-core";
import * as Steps from "./Steps";

import azureLogo from "@app/images/azure/azure-logo.png";
import { WizardConfirmation } from "../components";
import { useHistory } from "react-router";

import { azureStepOneAValidation } from "@app/services/AzureValidation";
import { Header } from "../components";

export const AzureWizard: FC = () => {
  const [stepIdReached, setStepIdReached] = useState(1);
  const [results, setResults] = useState("");
  const [error, setError] = useState(false);
  const [isFormValid, setIsFormValid] = useState(false);
  const [isValidating, setIsValidating] = useState(false);
  const history = useHistory();
  const onNext = (newStep) => {
    setStepIdReached(stepIdReached < newStep.id ? newStep.id : stepIdReached);
    setIsFormValid(false);
  };

  const closeWizard = () => {
    console.log("close wizard");
  };

  const onFormChange = (value) => {
    setIsFormValid(value);
  };

  const validateAzureWizard = async () => {
    setIsValidating(true);
    setResults("Final Validation Running...");
    const metadataURL = sessionStorage.getItem("azure_metadata_url");
    const results = await azureStepOneAValidation(metadataURL!, true);
    setError(results.status == "error");
    setResults("Results: " + results.message);
    setIsValidating(false);
    console.log(results);
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
      component: <Steps.AzureStepTwo />,
      hideCancelButton: true,
      canJumpTo: stepIdReached >= 3,
    },
    {
      id: 3,
      name: "Upload Azure SAML Metadata file",
      component: <Steps.AzureStepOneA onChange={onFormChange} />,
      hideCancelButton: true,
      canJumpTo: stepIdReached >= 2,
      enableNext: isFormValid,
    },
    {
      id: 4,
      name: "User Attributes & Claims",
      component: <Steps.AzureStepThree />,
      hideCancelButton: true,
      canJumpTo: stepIdReached >= 4,
    },
    {
      id: 5,
      name: "Assign People & Groups",
      component: <Steps.AzureStepFour />,
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
          validationFunction={validateAzureWizard}
        />
      ),
      canJumpTo: stepIdReached >= 4,
    },
  ];

  const title = "Azure wizard";

  return (
    <>
      <Header logo={azureLogo} />
      <PageSection
        marginHeight={10}
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
