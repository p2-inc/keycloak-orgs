import React, { FC, useEffect, useState } from "react";
import {
  PageSection,
  PageSectionVariants,
  PageSectionTypes,
  Wizard,
} from "@patternfly/react-core";
import SamlLogo from "@app/images/provider-logos/saml_logo.svg";
import { Header, WizardConfirmation } from "@wizardComponents";
import { Step1, Step2, Step3, Step4 } from "./steps";
import { useKeycloakAdminApi } from "@app/hooks/useKeycloakAdminApi";
import { Axios, clearAlias } from "@wizardServices";
import {
  API_RETURN,
  API_STATUS,
  METADATA_CONFIG,
} from "@app/configurations/api-status";
import IdentityProviderRepresentation from "@keycloak/keycloak-admin-client/lib/defs/identityProviderRepresentation";
import { useNavigateToBasePath } from "@app/routes";
import { getAlias, CreateIdp, SamlAttributeMapper } from "@wizardServices";
import { Providers, Protocols, SamlIDPDefaults } from "@app/configurations";
import { useApi, usePrompt } from "@app/hooks";
import { useGetFeatureFlagsQuery } from "@app/services";

export const GenericSAML: FC = () => {
  const idpCommonName = "Saml IdP";
  const title = "Generic SAML wizard";
  const navigateToBasePath = useNavigateToBasePath();
  const { data: featureFlags } = useGetFeatureFlagsQuery();
  const [stepIdReached, setStepIdReached] = useState(1);
  const { getServerUrl, getRealm } = useKeycloakAdminApi();
  const {
    setAlias,
    loginRedirectURL: ssoUrl,
    entityId,
    adminLinkSaml: adminLink,
    identifierURL,
    createIdPUrl,
    baseServerRealmsUrl,
  } = useApi();

  const alias = getAlias({
    provider: Providers.SAML,
    protocol: Protocols.SAML,
    preface: "generic-saml",
  });

  const samlMetadata = `${getServerUrl()}/realms/${getRealm()}/protocol/saml/descriptor`;

  // Metadata
  const [metadata, setMetadata] = useState<METADATA_CONFIG>();
  const [metadataUrl, setMetadataUrl] = useState("");
  const [isFormValid, setIsFormValid] = useState(false);

  // Complete
  const [isValidating, setIsValidating] = useState(false);
  const [results, setResults] = useState("");
  const [error, setError] = useState<null | boolean>(null);
  const [disableButton, setDisableButton] = useState(false);

  useEffect(() => {
    setAlias(alias);
  }, [alias]);

  const finishStep = 6;

  usePrompt(
    "The wizard is incomplete. Leaving will lose any saved progress. Are you sure?",
    stepIdReached < finishStep
  );

  const onNext = (newStep) => {
    if (stepIdReached === finishStep) {
      clearAlias({
        provider: Providers.SAML,
        protocol: Protocols.SAML,
      });
      navigateToBasePath();
    }
    setStepIdReached(stepIdReached < newStep.id ? newStep.id : stepIdReached);
  };

  const closeWizard = () => {
    navigateToBasePath();
  };

  const createIdP = async () => {
    // On final validation set stepIdReached to steps.length+1
    setIsValidating(true);
    setDisableButton(false);
    setResults(`Creating ${idpCommonName}...`);

    const payload: IdentityProviderRepresentation = {
      alias,
      displayName: `SAML Single Sign-on`,
      providerId: "saml",
      config: metadata!,
    };

    try {
      await CreateIdp({ createIdPUrl, payload, featureFlags });

      await SamlAttributeMapper({
        alias,
        createIdPUrl,
        usernameAttribute: {
          attributeName: "username",
          friendlyName: "username",
        },
        emailAttribute: { attributeName: "email", friendlyName: "email" },
        firstNameAttribute: {
          attributeName: "firstName",
          friendlyName: "firstName",
        },
        lastNameAttribute: {
          attributeName: "lastName",
          friendlyName: "lastName",
        },
        featureFlags,
      });

      setResults(`${idpCommonName} created successfully. Click finish.`);
      setStepIdReached(finishStep);
      setError(false);
      setDisableButton(true);
    } catch (e) {
      setResults(
        `Error creating ${idpCommonName}. Please confirm there is no SAML configured already.`
      );
      setError(true);
    } finally {
      setIsValidating(false);
    }
  };

  const uploadMetadataFile = async (file: File) => {
    const fd = new FormData();
    fd.append("providerId", "saml");
    fd.append("file", file);

    try {
      const resp = await Axios.post(identifierURL, fd);

      if (resp.status === 200) {
        setMetadata({
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

  const uploadCertifcateMetadataInfo = async ({
    file,
    ssoUrl,
    entityId,
  }: {
    file: File;
    ssoUrl: METADATA_CONFIG["singleSignOnServiceUrl"];
    entityId: string;
  }) => {
    const fd = new FormData();
    fd.append("providerId", "saml");
    fd.append("file", file);
    fd.append("ssoUrl", ssoUrl);
    fd.append("entityId", entityId);

    try {
      const resp = await Axios.post(identifierURL, fd);

      if (resp.status === 200) {
        setMetadata({
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

  const validateMetadataUrl = async ({
    url,
  }: {
    url: string;
  }): Promise<API_RETURN> => {
    // make call to submit the URL and verify it
    setMetadataUrl(url);

    try {
      const payload = {
        fromUrl: url,
        providerId: "saml",
        realm: getRealm(),
      };

      const resp = await Axios.post(identifierURL, payload);

      setMetadata({
        ...SamlIDPDefaults,
        ...resp,
      });
      setIsFormValid(true);

      return {
        status: API_STATUS.SUCCESS,
        message:
          "Configuration successfully validated with SAML. Continue to next step.",
      };
    } catch (e) {
      return {
        status: API_STATUS.ERROR,
        message:
          "Configuration validation failed with SAML. Check URL and try again.",
      };
    }
  };

  const steps = [
    {
      id: 1,
      name: "Create a SAML Application",
      component: (
        <Step1
          ssoUrl={ssoUrl}
          entityId={entityId}
          samlMetadata={samlMetadata}
        />
      ),
      hideCancelButton: true,
    },
    {
      id: 2,
      name: "Configure Application Metadata",
      component: (
        <Step2
          validateMetadataUrl={validateMetadataUrl}
          url={metadataUrl}
          metadata={metadata}
          uploadMetadataFile={uploadMetadataFile}
          uploadCertifcateMetadataInfo={uploadCertifcateMetadataInfo}
        />
      ),
      hideCancelButton: true,
      enableNext: isFormValid,
      canJumpTo: stepIdReached >= 2,
    },
    {
      id: 3,
      name: "Configure Attribute Mapping",
      component: <Step3 />,
      hideCancelButton: true,
      canJumpTo: stepIdReached >= 3,
    },
    {
      id: 4,
      name: "Configure User Access",
      component: <Step4 />,
      hideCancelButton: true,
      canJumpTo: stepIdReached >= 4,
    },
    {
      id: 5,
      name: "Confirmation",
      component: (
        <WizardConfirmation
          title="SSO Configuration Complete"
          message="Your users can now sign-in with SAML."
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
      canJumpTo: stepIdReached >= 5,
    },
  ];

  return (
    <>
      <Header logo={SamlLogo} />
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
