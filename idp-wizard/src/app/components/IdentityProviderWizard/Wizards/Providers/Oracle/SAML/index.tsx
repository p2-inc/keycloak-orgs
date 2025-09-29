import React, { FC, useEffect, useState } from "react";
import {
  PageSection,
  PageSectionVariants,
  PageSectionTypes,
  Wizard,
} from "@patternfly/react-core";
import * as Steps from "./Steps";
import oracleLogo from "@app/images/oracle/oracle-logo.png";
import { WizardConfirmation, Header } from "@wizardComponents";
import IdentityProviderRepresentation from "@keycloak/keycloak-admin-client/lib/defs/identityProviderRepresentation";
import { useNavigateToBasePath } from "@app/routes";
import { API_STATUS, METADATA_CONFIG } from "@app/configurations/api-status";
import { Protocols, Providers, SamlIDPDefaults } from "@app/configurations";
import {
  getAlias,
  clearAlias,
  CreateIdp,
  SamlAttributeMapper,
  Axios,
} from "@wizardServices";
import { useApi, usePrompt } from "@app/hooks";
import { useGetFeatureFlagsQuery } from "@app/services";
import { useGenerateIdpDisplayName } from "@app/hooks/useGenerateIdpDisplayName";

export const OracleWizard: FC = () => {
  const idpCommonName = "Oracle Cloud SAML IdP";
  const { data: featureFlags } = useGetFeatureFlagsQuery();
  const navigateToBasePath = useNavigateToBasePath();
  const [stepIdReached, setStepIdReached] = useState(1);
  const [results, setResults] = useState("");
  const [error, setError] = useState<null | boolean>(null);
  const [isFormValid, setIsFormValid] = useState(false);
  const [isValidating, setIsValidating] = useState(false);
  const [disableButton, setDisableButton] = useState(false);
  const {
    alias,
    setAlias,
    adminLinkSaml: adminLink,
    loginRedirectURL: acsUrl,
    entityId,
    identifierURL,
    createIdPUrl,
  } = useApi();
  const { generateIdpDisplayName } = useGenerateIdpDisplayName();

  const [metadata, setMetadata] = useState<METADATA_CONFIG>();

  useEffect(() => {
    const genAlias = getAlias({
      provider: Providers.ORACLE,
      protocol: Protocols.SAML,
      preface: "oracle-saml",
    });
    setAlias(genAlias);
  }, []);

  const finishStep = 7;

  usePrompt(
    "The wizard is incomplete. Leaving will lose any saved progress. Are you sure?",
    stepIdReached < finishStep
  );

  const onNext = (newStep) => {
    if (stepIdReached === finishStep) {
      clearAlias({
        provider: Providers.ORACLE,
        protocol: Protocols.SAML,
      });
      navigateToBasePath();
    }
    setStepIdReached(stepIdReached < newStep.id ? newStep.id : stepIdReached);
  };

  const closeWizard = () => {
    navigateToBasePath();
  };

  const handleFormSubmit = async ({
    metadataFile: file,
  }: {
    metadataFile: File;
  }) => {
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
          message: `Configuration successfully validated with ${idpCommonName}. Continue to next step.`,
        };
      }
    } catch (err) {
      console.log(err);
    }

    return {
      status: API_STATUS.ERROR,
      message: `Configuration validation failed with ${idpCommonName}. Check file and try again.`,
    };
  };

  const createOracleSamlIdP = async () => {
    // On final validation set stepIdReached to steps.length+1
    setIsValidating(true);
    setDisableButton(false);
    setResults(`Creating ${idpCommonName}...`);

    const payload: IdentityProviderRepresentation = {
      alias,
      hideOnLogin: true,
      displayName: generateIdpDisplayName(alias),
      providerId: "saml",
      config: metadata!,
    };

    try {
      await CreateIdp({ createIdPUrl, payload, featureFlags });

      await SamlAttributeMapper({
        alias,
        createIdPUrl,
        usernameAttribute: { attributeName: "username", friendlyName: "" },
        emailAttribute: { attributeName: "email", friendlyName: "" },
        firstNameAttribute: { attributeName: "firstName", friendlyName: "" },
        lastNameAttribute: { attributeName: "lastName", friendlyName: "" },
        featureFlags,
      });

      setResults(`${idpCommonName} created successfully. Click finish.`);
      setStepIdReached(finishStep);
      setError(false);
      setDisableButton(true);
      clearAlias({
        provider: Providers.ORACLE,
        protocol: Protocols.SAML,
      });
    } catch (e) {
      setResults(
        `Error creating ${idpCommonName}. Please confirm there is no Oracle Cloud SAML configured already.`
      );
      setError(true);
    } finally {
      setIsValidating(false);
    }
  };

  const steps = [
    {
      id: 1,
      name: "Create SAML Application",
      component: <Steps.OracleStepOne />,
      hideCancelButton: true,
    },
    {
      id: 2,
      name: "Upload SAML Metadata",
      component: <Steps.OracleStepTwo handleFormSubmit={handleFormSubmit} />,
      hideCancelButton: true,
      canJumpTo: stepIdReached >= 2,
      enableNext: isFormValid,
    },
    {
      id: 3,
      name: "Enter Service Provider Details",
      component: <Steps.OracleStepThree acsUrl={acsUrl} entityId={entityId} />,
      hideCancelButton: true,
      canJumpTo: stepIdReached >= 3,
    },
    {
      id: 4,
      name: "Configure Attribute Mapping",
      component: <Steps.OracleStepFour />,
      hideCancelButton: true,
      canJumpTo: stepIdReached >= 4,
    },
    {
      id: 5,
      name: "Assign Users & Groups",
      component: <Steps.OracleStepFive />,
      hideCancelButton: true,
      canJumpTo: stepIdReached >= 5,
    },
    {
      id: 6,
      name: "Confirmation",
      component: (
        <WizardConfirmation
          title="SSO Configuration Complete"
          message="Your users can now sign-in with Oracle Cloud."
          buttonText={`Create ${idpCommonName} in Keycloak`}
          resultsText={results}
          error={error}
          isValidating={isValidating}
          validationFunction={createOracleSamlIdP}
          disableButton={disableButton}
          adminLink={adminLink}
          adminButtonText={`Manage ${idpCommonName} in Keycloak`}
        />
      ),
      nextButtonText: "Finish",
      hideCancelButton: true,
      canJumpTo: stepIdReached >= 6,
      enableNext: stepIdReached === finishStep,
    },
  ];

  const title = "Oracle Cloud wizard";

  return (
    <>
      <Header logo={oracleLogo} />
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
