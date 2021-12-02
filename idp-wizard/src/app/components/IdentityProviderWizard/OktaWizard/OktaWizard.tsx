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
import octaLogo from "@app/images/okta/okta-logo.png";
import { WizardConfirmation } from "../FinalStepConfirmation";
import { useHistory } from "react-router";
import { useSessionStorage } from "react-use";
import { oktaCreateFederationAndSyncUsers } from "@app/services/OktaValidation";

export const OktaWizard: FC = () => {
  const [stepIdReached, setStepIdReached] = useState(1);
  const [isFormValid, setIsFormValid] = useState(false);
  const [results, setResults] = useState("");
  const [error, setError] = useState(false);
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

  const [oktaUserInfo] = useSessionStorage("okta_user_info", {
    username: "",
    pass: "",
  });

  const validateOktaWizard = async () => {
    const oktaCustomerIdentifier = sessionStorage.getItem(
      "okta_customer_identifier"
    );
    console.log(oktaCustomerIdentifier?.replace('"', ``));

    setResults("Final Validation Running...");
    const results = await oktaCreateFederationAndSyncUsers(
      oktaCustomerIdentifier?.replace('"', ``),
      oktaUserInfo.username!,
      oktaUserInfo.pass!
    );

    setError(results.status == "error");
    setResults("Results: " + results.message);
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
