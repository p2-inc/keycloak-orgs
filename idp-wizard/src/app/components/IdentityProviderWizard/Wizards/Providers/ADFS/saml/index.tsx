import React, { FC, useEffect, useState } from "react";
import {
  PageSection,
  PageSectionVariants,
  PageSectionTypes,
  Wizard,
} from "@patternfly/react-core";
import ADFSLogo from "@app/images/provider-logos/active-directory.svg";
import { Header, WizardConfirmation } from "@wizardComponents";
import { Step1, Step2, Step3, Step4 } from "./steps";
import { useKeycloakAdminApi } from "@app/hooks/useKeycloakAdminApi";
import {
  API_RETURN,
  API_STATUS,
  METADATA_CONFIG,
} from "@app/configurations/api-status";
import IdentityProviderRepresentation from "@keycloak/keycloak-admin-client/lib/defs/identityProviderRepresentation";
import {
  Axios,
  CreateIdp,
  SamlAttributeMapper,
} from "@app/components/IdentityProviderWizard/Wizards/services";
import { useNavigateToBasePath } from "@app/routes";
import { getAlias, clearAlias } from "@wizardServices";
import { Providers, Protocols, SamlIDPDefaults } from "@app/configurations";
import { useApi, usePrompt } from "@app/hooks";
import { useGetFeatureFlagsQuery } from "@app/services";

export const ADFSWizard: FC = () => {
  const idpCommonName = "ADFS IdP";
  const alias = getAlias({
    provider: Providers.ADFS,
    protocol: Protocols.SAML,
    preface: "adfs-saml",
  });
  const { data: featureFlags } = useGetFeatureFlagsQuery();
  const navigateToBasePath = useNavigateToBasePath();
  const title = "ADFS wizard";
  const [stepIdReached, setStepIdReached] = useState(1);
  const { getRealm } = useKeycloakAdminApi();
  const {
    setAlias,
    adminLinkSaml: adminLink,
    federationMetadataAddressUrl,
    entityId,
    identifierURL,
    createIdPUrl,
    updateIdPUrl,
  } = useApi();

  const [issuerUrl, setIssuerUrl] = useState(
    "https://HOSTNAME/federationmetadata/2007-06/federationmetadata.xml"
  );
  const [metadata, setMetadata] = useState<METADATA_CONFIG>();
  const [isFormValid, setIsFormValid] = useState(false);

  // Complete
  const [isValidating, setIsValidating] = useState(false);
  const [results, setResults] = useState("");
  const [error, setError] = useState<null | boolean>(null);
  const [disableButton, setDisableButton] = useState(false);

  useEffect(() => {
    setAlias(alias);
  }, [alias]);

  const finishStep = 5;

  usePrompt(
    "The wizard is incomplete. Leaving will lose any saved progress. Are you sure?",
    stepIdReached < finishStep
  );

  const onNext = (newStep) => {
    if (stepIdReached === finishStep) {
      clearAlias({
        provider: Providers.ADFS,
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
    url,
  }: {
    url: string;
  }): Promise<API_RETURN> => {
    // make call to submit the URL and verify it
    setIssuerUrl(url);

    try {
      const urlPayload = {
        fromUrl: url,
        providerId: "saml",
        realm: getRealm(),
      };
      // Validate the configuration
      const resp = await Axios.post(identifierURL, urlPayload);

      if (resp.status === 200) {
        const newMetadata = {
          ...SamlIDPDefaults,
          ...resp.data,
	  nameIDPolicyFormat: "urn:oasis:names:tc:SAML:2.0:nameid-format:persistent"
        };
        setMetadata(newMetadata);
        setIsFormValid(true);

        // Create the IDP
        const payload: IdentityProviderRepresentation = {
          alias,
          displayName: `ADFS Single Sign-on`,
          providerId: "saml",
          config: newMetadata,
          enabled: false,
        };
        // create the idp with the start config
        await CreateIdp({
          createIdPUrl,
          payload,
          featureFlags,
        });

        return {
          status: API_STATUS.SUCCESS,
          message: `Configuration successfully validated with ${idpCommonName}. Continue to next step.`,
        };
      }
    } catch (e) {
      console.log(e);
    }

    return {
      status: API_STATUS.ERROR,
      message: `Configuration validation failed with ${idpCommonName}. Check URL and try again.`,
    };
  };

  const validateFn = async () => {
    setIsValidating(true);
    setDisableButton(false);
    setResults(`Creating ${idpCommonName}...`);

    const payload: IdentityProviderRepresentation = {
      alias,
      displayName: `ADFS Single Sign-on`,
      providerId: "saml",
      config: metadata,
      enabled: true,
    };

    try {
      // Update the idp
      await Axios.put(updateIdPUrl, payload);

      await SamlAttributeMapper({
        alias,
        createIdPUrl,
        usernameAttribute: {
          attributeName:
            "http://schemas.microsoft.com/2012/12/certificatecontext/field/subjectname",
          friendlyName: "",
        },
        emailAttribute: {
          attributeName:
            "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/emailaddress",
          friendlyName: "",
        },
        firstNameAttribute: {
          attributeName:
            "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/givenname",
          friendlyName: "",
        },
        lastNameAttribute: {
          attributeName:
            "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/surname",
          friendlyName: "",
        },
        featureFlags,
      });

      setResults(`${idpCommonName} created successfully. Click finish.`);
      setStepIdReached(finishStep);
      setError(false);
      setDisableButton(true);
    } catch (e) {
      setResults(
        `Error creating ${idpCommonName}. Please confirm there is no ${idpCommonName} configured already.`
      );
      setError(true);
    } finally {
      setIsValidating(false);
    }
  };

  const steps = [
    {
      id: 1,
      name: "Setup Relying Party Trust",
      component: <Step1 url={issuerUrl} handleFormSubmit={handleFormSubmit} />,
      hideCancelButton: true,
      enableNext: isFormValid,
      canJumpTo: stepIdReached >= 1,
    },
    {
      id: 2,
      name: "Assign People and Groups",
      component: (
        <Step2 federationMetadataAddress={federationMetadataAddressUrl} />
      ),
      hideCancelButton: true,
      enableNext: true,
      canJumpTo: stepIdReached >= 2,
    },
    {
      id: 3,
      name: "Configure Attribute Mapping",
      component: <Step3 />,
      hideCancelButton: true,
      enableNext: true,
      canJumpTo: stepIdReached >= 3,
    },
    {
      id: 4,
      name: `Import ADFS Metadata`,
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
          message="Your users can now sign-in with ADFS."
          buttonText={`Create ${idpCommonName} in Keycloak`}
          disableButton={disableButton}
          resultsText={results}
          error={error}
          isValidating={isValidating}
          validationFunction={validateFn}
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
      <Header logo={ADFSLogo} />
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
