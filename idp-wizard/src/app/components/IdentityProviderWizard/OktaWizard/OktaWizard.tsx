import React, { FC, useState } from "react";
import {
  PageSection,
  PageSectionVariants,
  PageSectionTypes,
  TextContent,
  Wizard,
  Flex,
  FlexItem,
  Button,
} from "@patternfly/react-core";
import { OktaStepOne } from "./Steps/OktaStepOne";
import { OktaStepTwo } from "./Steps/OktaStepTwo";
import { OktaStepThree } from "./Steps/OktaStepThree";
import { useKeycloak } from "@react-keycloak/web";
import octaLogo from "@app/images/okta/okta-logo.png";
import { WizardConfirmation } from "../WizardConfirmation";
import { useHistory } from "react-router";

export const OktaWizard: FC = () => {
  const [stepIdReached, setStepIdReached] = useState(1);
  const [isFormValid, setIsForValid] = useState(false);
  const { keycloak } = useKeycloak();
  const history = useHistory();

  const onNext = (newStep) => {
    setStepIdReached(stepIdReached < newStep.id ? newStep.id : stepIdReached);
  };

  const closeWizard = () => {
    console.log("close wizard");
  };

  const onFormChange = (value) => {
    setIsForValid(value);
  };

  const goToDashboard = () => {
    let path = ``;
    history.push(path);
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
      component: <OktaStepTwo onChange={onFormChange} />,
      hideCancelButton: true,
      canJumpTo: stepIdReached >= 2,
      enableNext: isFormValid,
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
      hideCancelButton: true,
    },
  ];

  const title = "Finished wizard";
  return (
    <>
      <PageSection variant={PageSectionVariants.light}>
        <Flex>
          <FlexItem>
            <img className="step-header-image" src={octaLogo} alt="Okta" />
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
