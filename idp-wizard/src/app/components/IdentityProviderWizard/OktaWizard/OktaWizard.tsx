import React, { FC, useState } from "react";
import {
  Card,
  CardBody,
  CardTitle,
  PageSection,
  PageSectionVariants,
  PageSectionTypes,
  Text,
  TextContent,
  Stack,
  StackItem,
  Wizard,
  WizardStepFunctionType,
} from "@patternfly/react-core";
import { OktaStepOne } from "./Steps/OktaStepOne";
import { OktaStepTwo } from "./Steps/OktaStepTwo";
import { OktaStepThree } from "./Steps/OktaStepThree";
import octaLogo from "@app/images/okta/okta-logo.png";
import { WizardConfirmation } from "../WizardConfirmation";

export const OktaWizard: FC = () => {
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
      name: "Enable LDAP Inteface",
      component: <OktaStepOne />,
      hideCancelButton: true,
    },
    {
      id: 2,
      name: "LDAP Authentication",
      component: <OktaStepTwo />,
      hideCancelButton: true,
      canJumpTo: stepIdReached >= 2,
    },
    {
      id: 3,
      name: "Group Mapping",
      component: <OktaStepThree />,
      hideCancelButton: true,
      canJumpTo: stepIdReached >= 3,
    },
    {
      name: "Confirmation",
      component: (
        <WizardConfirmation
          title="LDAP Configuration Complete"
          message="Your users can now sign-in with Okta."
          buttonText="Test Sign-On"
        />
      ),
      canJumpTo: stepIdReached >= 3,
    },
  ];

  const title = "Finished wizard";
  return (
    <>
      <PageSection variant={PageSectionVariants.light}>
        <TextContent>
          <img className="step-header-image" src={octaLogo} alt="Okta" />
        </TextContent>
      </PageSection>
      <PageSection
        type={PageSectionTypes.wizard}
        variant={PageSectionVariants.light}
      >
        <Wizard
          navAriaLabel={`${title} steps`}
          mainAriaLabel={`${title} content`}
          onClose={closeWizard}
          steps={steps}
          height="100%"
          width="100%"
          onNext={onNext}
          nextButtonText="Continue to Next Step"
        />
      </PageSection>
    </>
  );
};
