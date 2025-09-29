import React, { FC, useEffect, useState } from "react";
import {
  PageSection,
  PageSectionVariants,
  PageSectionTypes,
  Wizard,
} from "@patternfly/react-core";
import * as Steps from "./Steps";
import entraIDLogo from "@app/images/provider-logos/msft_entraid.svg";
import { WizardConfirmation, Header } from "@wizardComponents";
import { useKeycloakAdminApi } from "@app/hooks/useKeycloakAdminApi";
import IdentityProviderRepresentation from "@keycloak/keycloak-admin-client/lib/defs/identityProviderRepresentation";
import { useNavigateToBasePath } from "@app/routes";
import {
  API_RETURN,
  API_STATUS,
  METADATA_CONFIG,
} from "@app/configurations/api-status";
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

export const EntraIdWizard: FC = () => {
  const idpCommonName = "EntraId SAML IdP";

  const { data: featureFlags } = useGetFeatureFlagsQuery();
  const navigateToBasePath = useNavigateToBasePath();
  const [stepIdReached, setStepIdReached] = useState(1);
  const [results, setResults] = useState("");
  const [error, setError] = useState<null | boolean>(null);
  const [isFormValid, setIsFormValid] = useState(false);
  const [isValidating, setIsValidating] = useState(false);
  const [disableButton, setDisableButton] = useState(false);

  const { getRealm } = useKeycloakAdminApi();
  const {
    alias,
    setAlias,
    adminLinkSaml: adminLink,
    identifierURL,
    createIdPUrl,
    loginRedirectURL: acsUrl,
    entityId,
  } = useApi();
  const { generateIdpDisplayName } = useGenerateIdpDisplayName();
  const [metadata, setMetadata] = useState<METADATA_CONFIG>();
  const [metadataUrl, setMetadataUrl] = useState("");

  useEffect(() => {
    const genAlias = getAlias({
      provider: Providers.ENTRAID,
      protocol: Protocols.SAML,
      preface: "entraid-saml",
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
        provider: Providers.ENTRAID,
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
    setMetadataUrl(metadataUrl);

    try {
      const payload = {
        fromUrl: url,
        providerId: "saml",
        realm: getRealm(),
      };

      const resp = await Axios.post(identifierURL, payload);

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
    } catch (e) {
      console.log(e);
    }

    return {
      status: API_STATUS.ERROR,
      message: `Configuration validation failed with ${idpCommonName}. Check URL and try again.`,
    };
  };

  const createIdP = async () => {
    // On final validation set stepIdReached to steps.length+1
    setIsValidating(true);
    setDisableButton(false);
    setResults(`Creating ${idpCommonName}...`);

    const payload: IdentityProviderRepresentation = {
      alias,
      displayName: generateIdpDisplayName(alias),
      hideOnLogin: true,
      providerId: "saml",
      config: metadata!,
    };
    console.log("foo");

    try {
      await CreateIdp({ createIdPUrl, payload, featureFlags });

      await SamlAttributeMapper({
        alias,
        createIdPUrl,
        usernameAttribute: {
          attributeName:
            "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/emailaddress",
          friendlyName: "",
        }, //setting to email
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

      clearAlias({
        provider: Providers.ENTRAID,
        protocol: Protocols.SAML,
      });
    } catch (e) {
      console.error(e);
      setResults(
        `Error creating ${idpCommonName}. Please confirm there is no EntraId SAML configured already.`
      );
      setError(true);
    } finally {
      setIsValidating(false);
    }
  };

  const steps = [
    {
      id: 1,
      name: "Create Enterprise Application",
      component: <Steps.EntraIdStepOne />,
      hideCancelButton: true,
    },
    {
      id: 2,
      name: "Configure Attribute Statements",
      component: <Steps.EntraIdStepTwo acsUrl={acsUrl} entityId={entityId} />,
      hideCancelButton: true,
      canJumpTo: stepIdReached >= 2,
    },
    {
      id: 3,
      name: "Upload EntraId SAML Metadata file",
      component: (
        <Steps.EntraIdStepThree
          handleFormSubmit={handleFormSubmit}
          url={metadataUrl}
        />
      ),
      hideCancelButton: true,
      canJumpTo: stepIdReached >= 3,
      enableNext: isFormValid,
    },
    {
      id: 4,
      name: "User Attributes & Claims",
      component: <Steps.EntraIdStepFour />,
      hideCancelButton: true,
      canJumpTo: stepIdReached >= 4,
    },
    {
      id: 5,
      name: "Assign People & Groups",
      component: <Steps.EntraIdStepFive />,
      hideCancelButton: true,
      canJumpTo: stepIdReached >= 5,
    },
    {
      name: "Confirmation",
      component: (
        <WizardConfirmation
          title="SSO Configuration Complete"
          message="Your users can now sign-in with EntraId AD."
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
      canJumpTo: stepIdReached >= 6,
      enableNext: stepIdReached === finishStep,
    },
  ];

  const title = "Microsoft Entra Id Wizard";

  return (
    <>
      <Header logo={entraIDLogo} />
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
