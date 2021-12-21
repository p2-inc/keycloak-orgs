import React from "react";
import azureStep1Image from "@app/images/azure/azure-1.png";
import azureStep2Image from "@app/images/azure/azure-2.png";
import azureStep3Image from "@app/images/azure/azure-3.png";
import azureStep4Image from "@app/images/azure/azure-4.png";
import { InstructionProps, Step, StepImage } from "@wizardComponents";

export function AzureStepOne() {
  const instructions: InstructionProps[] = [
    {
      text: 'Select "Enterprise applications" from your Azure AD dashboard.',
      component: <StepImage src={azureStep1Image} alt="Step 1.1" />,
    },
    {
      text: 'Click "New application" and continue.',
      component: <StepImage src={azureStep2Image} alt="Step 1.2" />,
    },
    {
      text: 'Select "Create your own application", then enter an App name that describers demo.phasetwo.io. Under "What are you looking to do with your application?", select "Integrate any other application you dont find in the gallery (Non-gallery)", then select "Create".',
      component: <StepImage src={azureStep3Image} alt="Step 1.3" />,
    },
    {
      text: 'Next, select "Single Sign On" from the "Manage" section in the left sidebar navigation menu, and then "SAML".',
      component: <StepImage src={azureStep4Image} alt="Step 1.4" />,
    },
  ];

  return (
    <Step
      title="Step 1: Create Enterprise Application"
      instructionList={instructions}
    />
  );
}
