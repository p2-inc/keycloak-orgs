import React, { FC, useEffect, useState } from "react";
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
import { Axios, clearAlias } from "@wizardServices";
import { OidcConfig, ClientCreds } from "./steps/forms";
import { API_STATUS } from "@app/configurations/api-status";
import IdentityProviderRepresentation from "@keycloak/keycloak-admin-client/lib/defs/identityProviderRepresentation";
import { useNavigateToBasePath } from "@app/routes";
import { getAlias } from "@wizardServices";
import { OidcDefaults, Protocols, Providers } from "@app/configurations";
import { useApi, usePrompt } from "@app/hooks";

const forms = {
  URL: true,
  FILE: true,
  CONFIG: true,
};

export const GenericOIDC: FC = () => {
  const idpCommonName = "OIDC IdP";
  const title = "OIDC wizard";
  const navigateToBasePath = useNavigateToBasePath();
  const alias = getAlias({
    provider: Providers.OPEN_ID,
    protocol: Protocols.OPEN_ID,
    preface: "generic-oidc",
  });
  const [stepIdReached, setStepIdReached] = useState(1);
  const { getRealm } = useKeycloakAdminApi();
  const {
    setAlias,
    adminLinkOidc: adminLink,
    identifierURL,
    createIdPUrl,
    loginRedirectURI: redirectUri,
  } = useApi();

  useEffect(() => {
    setAlias(alias);
  }, [alias]);

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
    ...OidcDefaults,
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

  const finishStep = 5;

  usePrompt(
    "The wizard is incomplete. Leaving will lose any saved progress. Are you sure?",
    stepIdReached < finishStep
  );

  const onNext = (newStep) => {
    if (stepIdReached === finishStep) {
      clearAlias({
        provider: Providers.OPEN_ID,
        protocol: Protocols.OPEN_ID,
      });
      navigateToBasePath();
    }
    setStepIdReached(stepIdReached < newStep.id ? newStep.id : stepIdReached);
  };

  const closeWizard = () => {
    navigateToBasePath();
  };

  const validateUrl = async ({ url }: { url: string }) => {
    let resp;
    setUrl(url);
    try {
      const payload = {
        fromUrl: url,
        providerId: "oidc",
        realm: getRealm(),
      };

      const resp = await Axios.post(identifierURL, payload);

      if (resp.status === 200) {
        setMetadata({
          ...resp.data,
        });
        setIsFormValid(true);
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
      }
    } catch (e) {
      console.log(err);
    }

    return {
      status: API_STATUS.ERROR,
      message:
        "Configuration validation failed with OIDC. Check URL and try again.",
    };
  };

  const validateFile = async ({ metadataFile }: { metadataFile: File }) => {
    const fd = new FormData();
    fd.append("providerId", "oidc");
    fd.append("file", metadataFile);

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
      const payload = {
        alias,
        displayName: "OIDC Single Sign-on",
        providerId: "oidc",
        config,
      };

      await Axios.post(identifierURL, payload);

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

    // let resp;
    // try {
    //   resp = await Axios.post(
    //     metadata.tokenUrl,
    //     `grant_type=client_credentials&client_id=${clientId}&client_secret=${clientSecret}`
    //   );
    // } catch (e) {
    //   return {
    //     status: API_STATUS.ERROR,
    //     message: "Credentials validation failed. Check values and try again.",
    //   };
    // }

    // if (resp.status === 200) {
    //   setCredentialValidationResp(resp.data);
    setCredentailsValid(true);

    return {
      status: API_STATUS.SUCCESS,
      // message: "Credentials successfully validated. Continue to next step.",
      message: "Credentials saved. Continue to next step.",
    };
    // }

    // return {
    //   status: API_STATUS.ERROR,
    //   message: "Credentials validation failed. Check values and try again.",
    // };
  };

  const createIdP = async () => {
    setIsValidating(true);
    setResults(`Creating ${idpCommonName}...`);

    const payload: IdentityProviderRepresentation = {
      alias,
      displayName: `OIDC Single Sign-on`,
      providerId: "oidc",
      config: { ...OidcDefaults, ...credentialValidationResp, ...credentials },
    };

    try {
      await Axios.post(createIdPUrl, payload);

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
      canJumpTo: stepIdReached >= 2,
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
      id: 3,
      name: "Confirmation",
      component: (
        <WizardConfirmation
          title="SSO Configuration Complete"
          message="Your users can now sign-in with OIDC."
          buttonText={`Create ${idpCommonName} in Keycloak`}
          disableButton={disableButton}
          resultsText={results}
          error={error}
          isValidating={isValidating}
          validationFunction={createIdP}
          adminLink={adminLink}
          adminButtonText={`Manage ${idpCommonName} in Keycloak`}
        />
      ),
      nextButtonText: "Finish",
      hideCancelButton: true,
      enableNext: stepIdReached === finishStep,
      canJumpTo: stepIdReached >= finishStep,
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
