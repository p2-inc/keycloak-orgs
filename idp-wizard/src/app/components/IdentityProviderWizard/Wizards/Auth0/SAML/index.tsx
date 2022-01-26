import React, { FC, useState } from "react";
import {
  PageSection,
  PageSectionVariants,
  PageSectionTypes,
  Wizard,
} from "@patternfly/react-core";

import axios from "axios";
import * as Steps from "./Steps";
import * as SharedSteps from "../shared/Steps";
import authoLogo from "@app/images/auth0/auth0-logo.png";
import { WizardConfirmation, Header } from "@wizardComponents";
import { useHistory } from "react-router";
import { useKeycloakAdminApi } from "@app/hooks/useKeycloakAdminApi";
import { customAlphabet } from "nanoid";
import { alphanumeric } from "nanoid-dictionary";
import { useKeycloak } from "@react-keycloak/web";
import { METADATA_CONFIG } from "@app/configurations/api-status";
import IdentityProviderRepresentation from "@keycloak/keycloak-admin-client/lib/defs/identityProviderRepresentation";
const identifierURL = `${process.env.KEYCLOAK_URL}/admin/realms/${process.env.REALM}/identity-provider/import-config`;

const nanoId = customAlphabet(alphanumeric, 6);

export const Auth0WizardSAML: FC = () => {
  const { keycloak } = useKeycloak();
  const [alias, setAlias] = useState(`auth0-saml-${nanoId()}`);
  const loginRedirectURL = `${process.env.KEYCLOAK_URL}/realms/${process.env.REALM}/broker/${alias}/endpoint`;

  const [stepIdReached, setStepIdReached] = useState(1);
  const [results, setResults] = useState("");
  const [error, setError] = useState(false);
  const [disableButton, setDisableButton] = useState(false);

  const [configData, setConfigData] = useState<METADATA_CONFIG | null>(null);
  const [isValidating, setIsValidating] = useState(false);
  const history = useHistory();
  const [kcAdminClient] = useKeycloakAdminApi();

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
  const uploadMetadataFile = async (file: File) => {
    const fd = new FormData();
    fd.append("providerId", "saml");
    fd.append("file", file);

    try {
      const resp = await Axios.post(identifierURL, fd);

      if (resp.status === 200) {
        setConfigData(resp.data);
        return true;
      }
    } catch (err) {
      console.log(err);
    }

    return false;
  };

  const createIdP = async () => {
    setIsValidating(true);
    setResults("Creating Auth0 SAML IdP...");

    const payload: IdentityProviderRepresentation = {
      alias,
      displayName: `Auth0 SAML Single Sign-on`,
      providerId: "saml",
      config: configData!,
    };

    try {
      await kcAdminClient.identityProviders.create({
        ...payload,
        realm: process.env.REALM!,
      });

      setResults("Auth0 SAML IdP created successfully. Click finish.");
      setStepIdReached(6);
      setError(false);
      setDisableButton(true);
    } catch (e) {
      setResults("Error creating Auth0 SAML IdP.");
      setError(true);
    } finally {
      setIsValidating(false);
    }

    setIsValidating(false);
  };

  const steps = [
    {
      id: 1,
      name: "Create An Application",
      component: <SharedSteps.Auth0StepOne />,
      hideCancelButton: true,
    },
    {
      id: 2,
      name: "Select SAML Addon",
      component: <Steps.Auth0StepTwo />,
      hideCancelButton: true,
      canJumpTo: stepIdReached >= 2,
    },
    {
      id: 3,
      name: "Upload Auth0 IdP Information",
      component: (
        <Steps.Auth0StepThree uploadMetadataFile={uploadMetadataFile} />
      ),
      hideCancelButton: true,
      canJumpTo: stepIdReached >= 3,
      enableNext: configData !== null,
    },
    {
      id: 4,
      name: "Enter Application Callback URL",
      component: <Steps.Auth0StepFour loginRedirectURL={loginRedirectURL} />,
      hideCancelButton: true,
      canJumpTo: stepIdReached >= 4,
    },
    {
      id: 5,
      name: "Confirmation",
      component: (
        <WizardConfirmation
          title="SSO Configuration Complete"
          message="Your users can now sign-in with Auth0."
          buttonText="Create Auth0 SAML IdP in Keycloak"
          resultsText={results}
          error={error}
          isValidating={isValidating}
          validationFunction={createIdP}
          disableButton={disableButton}
        />
      ),
      nextButtonText: "Finish",
      hideCancelButton: true,
      enableNext: stepIdReached === 6,
      canJumpTo: stepIdReached >= 5,
    },
  ];

  const title = "Auth0 wizard";

  return (
    <>
      <Header logo={authoLogo} />
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
