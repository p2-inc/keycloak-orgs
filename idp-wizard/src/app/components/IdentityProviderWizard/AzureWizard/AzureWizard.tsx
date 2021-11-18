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
import { AzureStepTwo } from "./Steps/AzureStepTwo";
import { AzureStepThree } from "./Steps/AzureStepThree";
import { AzureStepFour } from "./Steps/AzureStepFour";
import { AzureStepFive } from "./Steps/AzureStepFive";
import { AzureStepSix } from "./Steps/AzureStepSix";
import azureLogo from "@app/images/azure/azure-logo.png";
import { WizardConfirmation } from "../OktaWizard/Steps/OktaConfirmation";
import { useHistory } from "react-router";
import axios from "axios";
import KcAdminClient from "@keycloak/keycloak-admin-client";

export const AzureWizard: FC = () => {
  const [stepIdReached, setStepIdReached] = useState(1);
  const [results, setResults] = useState({});
  const { keycloak } = useKeycloak();
  const history = useHistory();
  const onNext = (newStep) => {
    setStepIdReached(stepIdReached < newStep.id ? newStep.id : stepIdReached);
  };

  useEffect(() => {
    const bearer = getBearerToken();
    // localStorage.setItem("REACT_TOKEN", bearer.access_token);
    console.log("bearer", bearer);
  }, []);

  const getAllUsers = async () => {
    // const settings = {
    //   baseUrl: process.env.KEYCLOAK_URL,
    //   realmName: process.env.REALM,
    //   // requestConfig: {
    //   //   /* Axios request config options https://github.com/axios/axios#request-config */
    //   // },
    // };
    // const kcAdminClient = new KcAdminClient(settings);
    // // Authorize with username / password
    // await kcAdminClient.auth({
    //   username: "martin@shrinedev.com",
    //   password: "Ut@U*1w6ZzEk",
    //   grantType: "password",
    //   clientId: process.env.CLIENT_ID,
    //   totp: "123456", // optional Time-based One-time Password if OTP is required in authentication flow
    // });
    // // List all users
    // const users = await kcAdminClient.users.find();
    // // This operation will now be performed in 'another-realm' if the user has access.
    // const groups = await kcAdminClient.groups.find();
  };

  const getBearerToken = async () => {
    //To configure the client, pass an object to override any of these  options:
    const settings = {
      baseUrl: process.env.KEYCLOAK_URL,
      realmName: process.env.REALM,
      // requestConfig: {
      //   /* Axios request config options https://github.com/axios/axios#request-config */
      // },
    };
    console.log("grabbing bearer token");
    // const kcAdminClient = new KcAdminClient(settings);

    // const credentials = {
    //   username: "martin@shrinedev.com",
    //   password: "Ut@U*1w6ZzEk",
    //   grantType: "password",
    //   clientId: process.env.CLIENT_ID,
    // };
    console.log("authing");

    console.log("writing to local storage");
    // localStorage.setItem("access_token", kcAdminClient.accessToken);
    console.log("after authing");
    //setInterval(() => kcAdminClient.auth(credentials), 58 * 1000); // 58 seconds
    console.log("before users.find");

    // fetch(
    //   "https://app.phasetwo.io/auth/realms/wizard/protocol/openid-connect/token",
    //   {
    //     method: "POST",
    //     headers: { "Content-Type": "application/x-www-form-urlencoded" },
    //     body: JSON.stringify({
    //       client_id: "idp-wizard",
    //       client_secret: "094e7246-8291-4a8b-bea1-d97b5eac732b",
    //       grant_type: "client_credentials",
    //     }),
    //   }
    // )
    //   .then(function (res) {
    //     return res.json();
    //   })
    //   .then(function (resJson) {
    //     return resJson;
    //   });
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
      name: "Confirmation",
      component: (
        <WizardConfirmation
          title="SSO Configuration Complete"
          message="Your users can now sign-in with Azure AD."
          buttonText="Test Single Sign-On"
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
