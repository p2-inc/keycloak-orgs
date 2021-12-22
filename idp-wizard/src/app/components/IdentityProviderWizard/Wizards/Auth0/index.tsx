import React, { FC, useState } from "react";
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
import { Auth0StepOne } from "./Steps/1";
import { Auth0StepTwo } from "./Steps/2";
import { Auth0StepThree } from "./Steps/3";
import authoLogo from "@app/images/provider-logos/auth0_logo.png";
import { WizardConfirmation } from "@wizardComponents";
import { useHistory } from "react-router";
import { auth0StepTwoValidation } from "@app/services/Auth0Validation";

export const Auth0Wizard: FC = () => {
  const [stepIdReached, setStepIdReached] = useState(1);
  const [results, setResults] = useState("");
  const [error, setError] = useState(false);
  const [isFormValid, setIsFormValid] = useState(false);
  const [isValidating, setIsValidating] = useState(false);
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

  const validateAuth0Wizard = async () => {
    setIsValidating(true);
    setResults("Final Validation Running...");
    const domain = sessionStorage.getItem("auth0_domain");
    const results = await auth0StepTwoValidation(domain!, true);
    setError(results.status == "error");
    setResults("Results: " + results.message);
    setIsValidating(false);
  };

  const steps = [
    {
      id: 1,
      name: "Create An Application",
      component: <Auth0StepOne />,
      hideCancelButton: true,
    },
    {
      id: 2,
      name: "Provide Domain And Credentials",
      component: <Auth0StepTwo  onChange={onFormChange} />,
      hideCancelButton: true,
      canJumpTo: stepIdReached >= 1,
      enableNext: isFormValid,
    },
    {
      id: 3,
      name: "Configure Redirect URI",
      component: <Auth0StepThree/>,
      hideCancelButton: true,
      canJumpTo: stepIdReached >= 2,
      
    },
    {
      name: "Confirmation",
      component: (
        <WizardConfirmation
          title="SSO Configuration Complete"
          message="Your users can now sign-in with Auth0."
          buttonText="Create OpenID IdP in Keycloak"
          resultsText={results}
          error={error}
          isValidating={isValidating}
          validationFunction={validateAuth0Wizard}
        />
      ),
      canJumpTo: stepIdReached >= 3,
    },
  ];

  const goToDashboard = () => {
    let path = `/`;
    history.push(path);
  };

  const title = "Auth0 wizard";

  return (
    <>
      <PageSection variant={PageSectionVariants.light}>
        <Flex>
          <FlexItem>
            <img className="step-header-image" src={authoLogo} alt="Auth0" />
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
