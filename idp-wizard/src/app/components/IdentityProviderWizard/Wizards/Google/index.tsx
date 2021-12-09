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
import { Step1, Step2, Step3, Step4, Step5, Step6 } from "./steps";

// Items to confirm in patternfly
// - Forms
// - Form validation
// - Wizard steps

export const GoogleWizard: FC = () => {
  const title = "Google wizard";

  const steps = [
    { name: "Add Custom SAML Application", component: <Step1 /> },
    {
      name: "Enter Details for your Custom App",
      component: <Step2 />,
    },
    { name: "Upload Google IdP Information", component: <Step3 /> },
    {
      name: "Enter Service Provider Details",
      component: <Step4 />,
    },
    {
      name: "Configure Attribute Mapping",
      component: <Step5 />,
    },
    {
      name: "Configure User Access",
      component: <Step6 />,
    },
    {
      name: "Confirmation",
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
