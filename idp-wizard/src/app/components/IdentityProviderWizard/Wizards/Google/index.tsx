import React, { FC, useEffect, useState } from "react";
import {
  PageSection,
  PageSectionVariants,
  PageSectionTypes,
  Wizard,
  Button,
} from "@patternfly/react-core";
import GoogleLogo from "@app/images/provider-logos/google_saml_logo.png";
import { Header } from "../components";

// Items to confirm in patternfly
// - Forms
// - Form validation
// - Wizard steps

export const GoogleWizard: FC = () => {
  const title = "Google wizard";

  const steps = [
    { name: "First step", component: <p>Step 1 content</p> },
    { name: "Second step", component: <p>Step 2 content</p> },
    { name: "Third step", component: <p>Step 3 content</p> },
    { name: "Fourth step", component: <p>Step 4 content</p> },
    {
      name: "Review",
      component: <p>Review step content</p>,
      nextButtonText: "Finish",
    },
  ];

  return (
    <>
      <Header logo={GoogleLogo} />
      <PageSection
        marginHeight={10}
        type={PageSectionTypes.wizard}
        variant={PageSectionVariants.light}
      >
        <Wizard
          navAriaLabel={`${title} steps`}
          isNavExpandable
          mainAriaLabel={`${title} content`}
          // onClose={closeWizard}
          nextButtonText="Next"
          steps={steps}
          height="100%"
          width="100%"
          // onNext={onNext}
        />
      </PageSection>
    </>
  );
};
