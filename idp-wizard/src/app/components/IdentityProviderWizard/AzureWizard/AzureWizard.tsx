import React, { FC, useEffect, useState } from "react";
import {
  PageSection,
  PageSectionVariants,
  PageSectionTypes,
  Wizard,
  Flex,
  FlexItem,
  Button,
} from "@patternfly/react-core";
import { useKeycloak } from "@react-keycloak/web";
import { AzureStepOne } from "./Steps/AzureStepOne";
import { AzureStepOneA } from "./Steps/AzureStepOneA";
import { AzureStepTwo } from "./Steps/AzureStepTwo";
import { AzureStepThree } from "./Steps/AzureStepThree";
import { AzureStepFour } from "./Steps/AzureStepFour";
import azureLogo from "@app/images/azure/azure-logo.png";
import { WizardConfirmation } from "../FinalStepConfirmation";
import { useHistory } from "react-router";

import { azureStepOneAValidation } from "@app/services/AzureValidation";

export const AzureWizard: FC = () => {
  const [stepIdReached, setStepIdReached] = useState(1);
  const [results, setResults] = useState("");
  const [error, setError] = useState(false);
  const [isFormValid, setIsFormValid] = useState(false);
  const { keycloak } = useKeycloak();
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
    setResults("Final Validation Running...");
    const metadataURL = sessionStorage.getItem("azure_metadata_url");
    const results = await azureStepOneAValidation(metadataURL!, true);
    setError(results.status == "error");
    setResults("Results: " + results.message);
    console.log(results);
  };

  const steps = [
    {
      id: 1,
      name: "Create Enterprise Application",
      component: <AzureStepOne />,
      hideCancelButton: true,
    },
    {
      id: 2,
      name: "Configure Attribute Statements",
      component: <AzureStepTwo />,
      hideCancelButton: true,
      canJumpTo: stepIdReached >= 3,
    },
    {
      id: 3,
      name: "Upload Azure SAML Metadata file",
      component: <AzureStepOneA onChange={onFormChange} />,
      hideCancelButton: true,
      canJumpTo: stepIdReached >= 2,
      enableNext: isFormValid,
    },
    {
      id: 4,
      name: "User Attributes & Claims",
      component: <AzureStepThree />,
      hideCancelButton: true,
      canJumpTo: stepIdReached >= 4,
    },
    {
      id: 5,
      name: "Assign People & Groups",
      component: <AzureStepFour />,
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
          validationFunction={validateAzureWizard}
        />
      ),
      canJumpTo: stepIdReached >= 4,
    },
  ];

  const goToDashboard = () => {
    let path = ``;
    history.push(path);
  };

  const title = "Finished wizard";

  return (
    <>
      <PageSection variant={PageSectionVariants.light}>
        <Flex>
          <FlexItem>
            <img className="step-header-image" src={azureLogo} alt="Azure" />
          </FlexItem>

          <FlexItem align={{ default: "alignRight" }}>
            <Button variant="link" isInline onClick={goToDashboard}>
              My Dashboard
            </Button>
          </FlexItem>
          <FlexItem>
            <Button variant="link" isInline onClick={() => keycloak.logout()}>
              Logout
            </Button>
          </FlexItem>
        </Flex>
      </PageSection>
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
