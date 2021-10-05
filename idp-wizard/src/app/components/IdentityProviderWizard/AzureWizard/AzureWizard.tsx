import React, { FC, useState } from "react";
import {
  PageSection,
  PageSectionVariants,
  PageSectionTypes,
  Wizard,
  Flex,
  FlexItem,
} from "@patternfly/react-core";
import { AzureStepOne } from "./Steps/AzureStepOne";
import { AzureStepTwo } from "./Steps/AzureStepTwo";
import { AzureStepThree } from "./Steps/AzureStepThree";
import { AzureStepFour } from "./Steps/AzureStepFour";
import { AzureStepFive } from "./Steps/AzureStepFive";
import { AzureStepSix } from "./Steps/AzureStepSix";
import azureLogo from "@app/images/azure/azure-logo.png";
import logo from "@app/bgimages/logo_phase_slash.svg";
import { WizardConfirmation } from "../WizardConfirmation";

export const AzureWizard: FC = () => {
  const [stepIdReached, setStepIdReached] = useState(1);
  const onNext = (newStep) => {
    setStepIdReached(stepIdReached < newStep.id ? newStep.id : stepIdReached);
  };

  const closeWizard = () => {
    console.log("close wizard");
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
      canJumpTo: stepIdReached >= 2,
    },
    {
      id: 3,
      name: "User Attributes & Claims",
      component: <AzureStepThree />,
      hideCancelButton: true,
      canJumpTo: stepIdReached >= 3,
    },
    {
      id: 4,
      name: "Assign People & Groups",
      component: <AzureStepFour />,
      hideCancelButton: true,
      canJumpTo: stepIdReached >= 4,
    },
    {
      id: 5,
      name: "SAML Signing Certificate",
      component: <AzureStepFive />,
      hideCancelButton: true,
      canJumpTo: stepIdReached >= 5,
    },
    {
      id: 6,
      name: "Provide a Login URL",
      component: <AzureStepSix />,
      hideCancelButton: true,
      canJumpTo: stepIdReached >= 6,
    },
    {
      name: "Confirmation",
      component: (
        <WizardConfirmation
          title="SSO Configuration Complete"
          message="Your users can now sign-in with Azure AD."
          buttonText="Test Single Sign-On"
        />
      ),
      canJumpTo: stepIdReached >= 6,
    },
  ];

  const title = "Finished wizard";
  return (
    <>
      <PageSection variant={PageSectionVariants.light}>
        <Flex>
          <FlexItem>
            <img className="step-header-image" src={azureLogo} alt="Azure" />
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
