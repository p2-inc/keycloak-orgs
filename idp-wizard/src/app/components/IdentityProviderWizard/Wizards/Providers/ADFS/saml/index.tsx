import React, { FC, useState } from "react";
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
import { SamlUserAttributeMapper } from "@app/components/IdentityProviderWizard/Wizards/services";
import { useNavigateToBasePath } from "@app/routes";
import { getAlias, clearAlias } from "@wizardServices";
import { Providers, Protocols, SamlIDPDefaults } from "@app/configurations";
import { usePrompt } from "@app/hooks";

export const ADFSWizard: FC = () => {
  const idpCommonName = "ADFS IdP";
  const alias = getAlias({
    provider: Providers.ADFS,
    protocol: Protocols.SAML,
    preface: "adfs-saml",
  });
  const navigateToBasePath = useNavigateToBasePath();
  const title = "ADFS wizard";
  const [stepIdReached, setStepIdReached] = useState(1);
  const [
    kcAdminClient,
    setKcAdminClientAccessToken,
    getServerUrl,
    getRealm,
    getAuthRealm,
  ] = useKeycloakAdminApi();

  const entityId = `${getServerUrl()}/realms/${getRealm()}`;
  const acsUrl = `${getServerUrl()}/realms/${getRealm()}/broker/${alias}/endpoint`;
  const federationMetadataAddress = `${acsUrl}/descriptor`;
  const recipient = acsUrl;
  const acsUrlValidator = acsUrl.replace(/\//g, "\\/");
  const adminLink = `${getServerUrl()}/admin/${getAuthRealm()}/console/#/realms/${getRealm()}/identity-provider-settings/provider/saml/${alias}`;

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

  const finishStep = 5;

  // usePrompt(
  //   "The wizard is incomplete. Leaving will lose any saved progress. Are you sure?",
  //   stepIdReached < finishStep
  // );

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
      const resp = await kcAdminClient.identityProviders.importFromUrl({
        fromUrl: url,
        providerId: "saml",
        realm: getRealm(),
      });

      setMetadata({
        ...SamlIDPDefaults,
        ...resp,
      });
      setIsFormValid(true);

      return {
        status: API_STATUS.SUCCESS,
        message: `Configuration successfully validated with ${idpCommonName}. Continue to next step.`,
      };
    } catch (e) {
      return {
        status: API_STATUS.ERROR,
        message: `Configuration validation failed with ${idpCommonName}. Check URL and try again.`,
      };
    }
  };

  // TODO: In this case, if they get to this step and click “Create”,
  // we need to overlay what came back from the metadata url with what
  // we originally created, set enabled = true and hit the update endpoint
  // rather than the create endpoint(PUT rather than POST, with the alias
  // as the last segment of the url)
  const validateFn = async () => {
    setIsValidating(true);
    setDisableButton(false);
    setResults(`Creating ${idpCommonName}...`);

    const payload: IdentityProviderRepresentation = {
      alias,
      displayName: `ADFS Single Sign-on`,
      providerId: "saml",
      config: metadata!,
    };

    try {
      await kcAdminClient.identityProviders.update(
        {
          ...payload,
          realm: getRealm()!,
        },
        metadata
      );
      // await kcAdminClient.identityProviders.create({
      //   ...payload,
      //   realm: getRealm()!,
      // });

      // Map attributes
      // await SamlUserAttributeMapper({
      //   alias,
      //   keys: {
      //     serverUrl: getServerUrl()!,
      //     realm: getRealm()!,
      //   },
      //   attributes: [
      //     {
      //       attributeName: "firstName",
      //       friendlyName: "",
      //       userAttribute: "firstName",
      //     },
      //     {
      //       attributeName: "lastName",
      //       friendlyName: "",
      //       userAttribute: "lastName",
      //     },
      //     {
      //       attributeName: "email",
      //       friendlyName: "",
      //       userAttribute: "email",
      //     },
      //   ],
      // });

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
      component: (
        <Step1 federationMetadataAddress={federationMetadataAddress} />
      ),
      hideCancelButton: true,
      enableNext: true,
      canJumpTo: stepIdReached >= 1,
    },
    {
      id: 2,
      name: "Assign People and Groups",
      component: <Step2 />,
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
      component: <Step4 url={issuerUrl} handleFormSubmit={handleFormSubmit} />,
      hideCancelButton: true,
      enableNext: isFormValid,
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
