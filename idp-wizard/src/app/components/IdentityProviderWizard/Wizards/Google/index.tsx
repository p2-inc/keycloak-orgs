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
import GoogleLogo from "@app/images/provider-logos/google_saml_logo.png";
import { useKeycloak } from "@react-keycloak/web";
import { Header } from "../components";

// Items to confirm in patternfly
// - Forms
// - Form validation
// - Wizard steps

export const GoogleWizard: FC = () => {
  const { keycloak } = useKeycloak();

  return (
    <>
      <Header logo={GoogleLogo} />
      <PageSection
        marginHeight={10}
        type={PageSectionTypes.wizard}
        variant={PageSectionVariants.light}
      >
        {/* <Wizard
          navAriaLabel={`${title} steps`}
          isNavExpandable
          mainAriaLabel={`${title} content`}
          onClose={closeWizard}
          nextButtonText="Continue to Next Step"
          steps={steps}
          height="100%"
          width="100%"
          onNext={onNext}
        /> */}
      </PageSection>
    </>
  );
};
