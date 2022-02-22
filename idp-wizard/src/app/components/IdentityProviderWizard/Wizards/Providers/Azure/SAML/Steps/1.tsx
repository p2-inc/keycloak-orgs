import React from "react";
import * as Images from "@app/images/azure/saml";
import { InstructionProps, Step, StepImage } from "@wizardComponents";
import { useHostname } from "@app/hooks/useHostname";

export function AzureStepOne() {
  const hostname = useHostname();

  const instructions: InstructionProps[] = [
    {
      text: 'Select "Enterprise applications" from your Azure AD dashboard.',
      component: <StepImage src={Images.AzureSaml1} alt="Step 1.1" />,
    },
    {
      text: 'Click "New application" and continue.',
      component: <StepImage src={Images.AzureSaml2} alt="Step 1.2" />,
    },
    {
      text: 'Select "Create your own application", which will open a dialog that will allow you to enter a name.',
      component: <StepImage src={Images.AzureSaml3a} alt="Step 1.3" />,
    },
    {
      text: `Enter an App name that describers ${hostname}. Under "What are you looking to do with your application?", select "Integrate any other application you dont find in the gallery (Non-gallery)", then select "Create".`,
      component: <StepImage src={Images.AzureSaml3} alt="Step 1.4" />,
    },
    {
      text: 'Next, select "Single Sign On" from the "Manage" section in the left sidebar navigation menu, and then "SAML".',
      component: <StepImage src={Images.AzureSaml4} alt="Step 1.5" />,
    },
  ];

  return (
    <Step
      title="Step 1: Create Enterprise Application"
      instructionList={instructions}
    />
  );
}
