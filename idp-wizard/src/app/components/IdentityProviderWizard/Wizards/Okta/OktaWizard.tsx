import React, { FC, useEffect, useState } from "react";
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
import oktaLogo from "@app/images/okta/okta-logo.png";
import { WizardConfirmation } from "../../FinalStepConfirmation";
import { useHistory } from "react-router";
import { useSessionStorage } from "react-use";
import { oktaCreateFederationAndSyncUsers } from "@app/services/OktaValidation";
import { Header } from "../components";

export const OktaWizard: FC = () => {
  const [stepIdReached, setStepIdReached] = useState(1);
  const [isFormValid, setIsFormValid] = useState(false);
  const [results, setResults] = useState("");
  const [error, setError] = useState(false);
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

  const goToDashboard = () => {
    let path = ``;
    history.push(path);
  };

  const username = sessionStorage.getItem("okta_un") || "";
  const pass = sessionStorage.getItem("okta_p") || "";

  const validateOktaWizard = async () => {
    setIsValidating(true);
    const oktaCustomerIdentifier =
      sessionStorage.getItem("okta_customer_identifier") ||
      process.env.OKTA_DEFAULT_CUSTOMER_IDENTIFER;

    setResults("Final Validation Running...");
    const results = await oktaCreateFederationAndSyncUsers(
      oktaCustomerIdentifier,
      username,
      pass
    );

    setError(results.status == "error");
    setResults("Results: " + results.message);
    setIsValidating(false);
  };

  const steps = [
    {
      id: 1,
      name: "Enable LDAP Inteface",
      component: <OktaStepOne onChange={onFormChange} />,
      enableNext: isFormValid,
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
          resultsText={results}
          error={error}
          isValidating={isValidating}
          validationFunction={validateOktaWizard}
        />
      ),
      canJumpTo: stepIdReached >= 3,
      hideCancelButton: true,
    },
  ];

  const title = "Finished wizard";
  return (
    <>
      <Header logo={oktaLogo} />
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
