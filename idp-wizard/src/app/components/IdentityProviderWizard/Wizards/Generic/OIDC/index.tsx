import React, { FC, useState } from "react";
import {
  PageSection,
  PageSectionVariants,
  PageSectionTypes,
  Wizard,
} from "@patternfly/react-core";
import OpenIdLogo from "@app/images/oidc/openid-logo.svg";
import { Header, WizardConfirmation } from "@wizardComponents";
import { Step1, Step2, Step3 } from "./steps";
import { useKeycloakAdminApi } from "@app/hooks/useKeycloakAdminApi";
import axios from "axios";
import { useHistory } from "react-router";
import { useKeycloak } from "@react-keycloak/web";
import { generateId } from "@app/utils/generate-id";
import { OidcConfig, ClientCreds } from "./steps/forms";
import { API_STATUS } from "@app/configurations/api-status";
import IdentityProviderRepresentation from "@keycloak/keycloak-admin-client/lib/defs/identityProviderRepresentation";

const nanoId = generateId();

const forms = {
  URL: true,
  FILE: true,
  CONFIG: true,
};

export const GenericOIDC: FC = () => {
  const title = "OIDC wizard";
  const [alias, setAlias] = useState(`generic-oidc-${nanoId}`);
  const [stepIdReached, setStepIdReached] = useState(1);
  const [kcAdminClient] = useKeycloakAdminApi();
  const { keycloak } = useKeycloak();
  const history = useHistory();

  const redirectUri = `${process.env.KEYCLOAK_URL}/auth/realms/${process.env.REALM}/broker/${nanoId}/endpoint`;
  const identifierURL = `${process.env.KEYCLOAK_URL}/admin/realms/${process.env.REALM}/identity-provider/import-config`;

  // Complete
  const [isValidating, setIsValidating] = useState(false);
  const [results, setResults] = useState("");
  const [error, setError] = useState(false);
  const [disableButton, setDisableButton] = useState(false);

  // Form Values
  const [formsActive, setFormsActive] = useState(forms);
  const [isFormValid, setIsFormValid] = useState(false);
  const [url, setUrl] = useState("");
  const [metadata, setMetadata] = useState<OidcConfig>({
    authorizationUrl: "",
    tokenUrl: "",
    userInfoUrl: "",
    validateSignature: false,
    jwksUrl: "",
    issuer: "",
    logoutUrl: "",
  });
  const [credentials, setCredentials] = useState<ClientCreds>({
    clientId: "",
    clientSecret: "",
  });
  const [credentailsValid, setCredentailsValid] = useState(false);
  const [credentialValidationResp, setCredentialValidationResp] = useState({});

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

  const validateUrl = async ({ url }: { url: string }) => {
    let resp;
    setUrl(url);
    try {
      resp = await kcAdminClient.identityProviders.importFromUrl({
        fromUrl: url,
        providerId: "oidc",
        realm: process.env.REALM,
      });

      setIsFormValid(true);
      setMetadata(resp);
      setFormsActive({
        ...formsActive,
        FILE: false,
        CONFIG: false,
      });

      return {
        status: API_STATUS.SUCCESS,
        message:
          "Configuration successfully validated with OIDC. Continue to next step.",
      };
    } catch (e) {
      return {
        status: API_STATUS.ERROR,
        message:
          "Configuration validation failed with OIDC. Check URL and try again.",
      };
    }
  };

  const validateFile = async ({ file }: { file: File }) => {
    const fd = new FormData();
    fd.append("providerId", "oidc");
    fd.append("file", file);

    try {
      const resp = await Axios.post(identifierURL, fd);

      if (resp.status === 200) {
        setMetadata(resp.data);
        setIsFormValid(true);
        setFormsActive({
          ...formsActive,
          URL: false,
          CONFIG: false,
        });

        return {
          status: API_STATUS.SUCCESS,
          message:
            "Configuration successfully validated with OIDC. Continue to next step.",
        };
      }
    } catch (err) {
      console.log(err);
    }

    return {
      status: API_STATUS.ERROR,
      message:
        "Configuration validation failed with OIDC. Check file and try again.",
    };
  };

  const validateConfig = async (config: OidcConfig) => {
    let resp;

    try {
      resp = await kcAdminClient.identityProviders.create({
        alias,
        displayName: "Generic OIDC Single Sign-on",
        providerId: "oidc",
        config,
      });

      setIsFormValid(true);
      setMetadata(resp);
      setFormsActive({
        ...formsActive,
        FILE: false,
        URL: false,
      });

      return {
        status: API_STATUS.SUCCESS,
        message:
          "Configuration successfully validated with OIDC. Continue to next step.",
      };
    } catch (e) {
      return {
        status: API_STATUS.ERROR,
        message:
          "Configuration validation failed with OIDC. Check values and try again.",
      };
    }
  };

  const validateCredentials = async ({
    clientId,
    clientSecret,
  }: ClientCreds) => {
    setCredentials({ clientId, clientSecret });
    const resp = await Axios.post(
      metadata.tokenUrl,
      `grant_type=client_credentials&client_id=${clientId}&client_secret=${clientSecret}`
    );

    kcAdminClient.identityProviders.makeRequest;

    if (resp.status === 200) {
      setCredentialValidationResp(resp.data);
      setCredentailsValid(true);

      return {
        status: API_STATUS.SUCCESS,
        message: "Credentials successfully validated. Continue to next step.",
      };
    }

    return {
      status: API_STATUS.ERROR,
      message: "Credentials validation failed. Check values and try again.",
    };
  };

  const validateFn = async () => {
    setIsValidating(true);
    setResults("Creating OIDC IdP...");

    const payload: IdentityProviderRepresentation = {
      alias,
      displayName: `OIDC Single Sign-on`,
      providerId: "oidc",
      config: { ...credentialValidationResp, ...credentials },
    };

    try {
      await kcAdminClient.identityProviders.create({
        ...payload,
        realm: process.env.REALM!,
      });

      setResults("OIDC IdP created successfully. Click finish.");
      setStepIdReached(4);
      setError(false);
      setDisableButton(true);
    } catch (e) {
      setResults("Error creating OIDC IdP.");
      setError(true);
    } finally {
      setIsValidating(false);
    }

    setIsValidating(false);
  };

  const steps = [
    {
      id: 1,
      name: "Create an OpenID Connect Application",
      component: <Step1 redirectUri={redirectUri} />,
      hideCancelButton: true,
      enableNext: true,
    },
    {
      id: 2,
      name: "Configure Application Configuration",
      component: (
        <Step2
          validateUrl={validateUrl}
          validateFile={validateFile}
          validateConfig={validateConfig}
          url={url}
          formsActive={formsActive}
          metadata={metadata}
        />
      ),
      enableNext: isFormValid,
      hideCancelButton: true,
      // canJumpTo: stepIdReached >= 2,
    },
    {
      id: 3,
      name: "Provide the Client Credentials",
      component: (
        <Step3
          validateCredentials={validateCredentials}
          credentials={credentials}
        />
      ),
      hideCancelButton: true,
      enableNext: credentailsValid,
    },
    {
      id: 4,
      name: "Confirmation",
      component: (
        <WizardConfirmation
          title="SSO Configuration Complete"
          message="Your users can now sign-in with {{Provider}}."
          buttonText="Create {{Provider}} IdP in Keycloak"
          disableButton={disableButton}
          resultsText={results}
          error={error}
          isValidating={isValidating}
          validationFunction={validateFn}
        />
      ),
      nextButtonText: "Finish",
      hideCancelButton: true,
      enableNext: stepIdReached === 4,
      canJumpTo: stepIdReached >= 3,
    },
  ];

  return (
    <>
      <Header logo={OpenIdLogo} />
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
