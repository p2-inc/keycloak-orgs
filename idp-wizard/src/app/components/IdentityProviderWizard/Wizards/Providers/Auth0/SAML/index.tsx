import React, { FC, useState } from "react";
import {
  PageSection,
  PageSectionVariants,
  PageSectionTypes,
  Wizard,
} from "@patternfly/react-core";
import { API_STATUS, METADATA_CONFIG } from "@app/configurations/api-status";
import { Axios, clearAlias } from "@wizardServices";
import * as Steps from "./Steps";
import * as SharedSteps from "../shared/Steps";
import authoLogo from "@app/images/auth0/auth0-logo.png";
import { WizardConfirmation, Header } from "@wizardComponents";
import { useKeycloakAdminApi } from "@app/hooks/useKeycloakAdminApi";
import IdentityProviderRepresentation from "@keycloak/keycloak-admin-client/lib/defs/identityProviderRepresentation";
import { SamlUserAttributeMapper } from "@app/components/IdentityProviderWizard/Wizards/services";
import { useNavigateToBasePath } from "@app/routes";
import { getAlias } from "@wizardServices";
import { Protocols, Providers, SamlIDPDefaults } from "@app/configurations";
import { useApi, usePrompt } from "@app/hooks";

export const Auth0WizardSAML: FC = () => {
  const idpCommonName = "Auth0 SAML IdP";
  const alias = getAlias({
    provider: Providers.AUTH0,
    protocol: Protocols.SAML,
    preface: "auth0-saml",
  });
  const navigateToBasePath = useNavigateToBasePath();
  const { kcAdminClient, getServerUrl, getRealm, getAuthRealm } =
    useKeycloakAdminApi();
  const { endpoints, setAlias } = useApi();

  React.useEffect(() => {
    setAlias(alias);
  }, [alias]);

  const loginRedirectURL = `${getServerUrl()}/realms/${getRealm()}/broker/${alias}/endpoint`;
  const identifierURL = `${getServerUrl()}/admin/realms/${
    endpoints?.importConfig.endpoint
  }`;

  const [stepIdReached, setStepIdReached] = useState(1);
  const [results, setResults] = useState("");
  const [error, setError] = useState(false);
  const [disableButton, setDisableButton] = useState(false);
  const [isFormValid, setIsFormValid] = useState(false);
  const adminLink = `${getServerUrl()}/admin/${getAuthRealm()}/console/#/realms/${getRealm()}/identity-provider-settings/provider/saml/${alias}`;

  const [configData, setConfigData] = useState<METADATA_CONFIG | null>(null);
  const [isValidating, setIsValidating] = useState(false);

  const finishStep = 6;

  usePrompt(
    "The wizard is incomplete. Leaving will lose any saved progress. Are you sure?",
    stepIdReached < finishStep
  );

  const onNext = (newStep) => {
    if (stepIdReached === finishStep) {
      clearAlias({
        provider: Providers.AUTH0,
        protocol: Protocols.SAML,
      });
      navigateToBasePath();
    }
    setStepIdReached(stepIdReached < newStep.id ? newStep.id : stepIdReached);
  };

  const closeWizard = () => navigateToBasePath();

  const uploadMetadataFile = async (file: File) => {
    const fd = new FormData();
    fd.append("providerId", "saml");
    fd.append("file", file);

    try {
      const resp = await Axios.post(identifierURL, fd);

      if (resp.status === 200) {
        setConfigData({
          ...SamlIDPDefaults,
          ...resp.data,
        });
        setIsFormValid(true);
        return {
          status: API_STATUS.SUCCESS,
          message:
            "Configuration successfully validated with SAML. Continue to next step.",
        };
      }
    } catch (err) {
      console.log(err);
    }

    return {
      status: API_STATUS.ERROR,
      message:
        "Configuration validation failed with SAML. Check file and try again.",
    };
  };

  const createIdP = async () => {
    setIsValidating(true);
    setResults(`Creating ${idpCommonName}...`);

    const payload: IdentityProviderRepresentation = {
      alias,
      displayName: `Auth0 SAML Single Sign-on`,
      providerId: "saml",
      config: configData!,
    };

    try {
      // await kcAdminClient.identityProviders.create({
      //   ...payload,
      //   realm: getRealm()!,
      // });

      await Axios.post(
        `${getServerUrl()}/admin/realms/${endpoints?.createIdP.endpoint!}`,
        payload
      );

      // Map attributes
      await SamlUserAttributeMapper({
        alias,
        keys: {
          serverUrl: getServerUrl()!,
          realm: getRealm()!,
        },
        attributes: [
          {
            attributeName:
              "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/givenname",
            friendlyName: "",
            userAttribute: "firstName",
          },
          {
            attributeName:
              "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/surname",
            friendlyName: "",
            userAttribute: "lastName",
          },
          {
            attributeName:
              "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/emailaddress",
            friendlyName: "",
            userAttribute: "email",
          },
          {
            attributeName:
              "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/username",
            friendlyName: "",
            userAttribute: "username",
          },
        ],
        path: endpoints?.addMapperToIdP.endpoint!,
      });

      setResults(`${idpCommonName} created successfully. Click finish.`);
      setStepIdReached(finishStep);
      setError(false);
      setDisableButton(true);
    } catch (e) {
      setResults(`Error creating ${idpCommonName}.`);
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
      enableNext: isFormValid,
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
          buttonText={`Create ${idpCommonName} in Keycloak`}
          resultsText={results}
          error={error}
          isValidating={isValidating}
          validationFunction={createIdP}
          disableButton={disableButton}
          adminLink={adminLink}
          adminButtonText={`Manage ${idpCommonName} in Keycloak`}
        />
      ),
      nextButtonText: "Finish",
      hideCancelButton: true,
      enableNext: stepIdReached === finishStep,
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
