import React from "react";
import azureStep9Image from "@app/images/azure/azure-9.png";
import azureStep10Image from "@app/images/azure/azure-10.png";
import azureStep11Image from "@app/images/azure/azure-11.png";
import azureStep12Image from "@app/images/azure/azure-12.png";
import { InstructionProps, Step, StepImage } from "@wizardComponents";

export function AzureStepFour() {
  const instructionList: InstructionProps[] = [
    {
      text: 'In order for your users and gorups of users to be synced to demo.phasetwo.io you will need to assign them to your Azure AD SAML Application. Select "Users and groups" from the "Manage" section of the navigations menu.',
      component: <StepImage src={azureStep9Image} alt="Step 4.1" />,
    },
    {
      text: 'Select "Add user/group" from the top menu.',
      component: <StepImage src={azureStep10Image} alt="Step 4.2" />,
    },
    {
      text: 'Select "None selected" under the "Users and Groups". In the menu, select the users and groups of users that you want to add to the SAML application, and click "Select".',
      component: <StepImage src={azureStep11Image} alt="Step 4.3" />,
    },
    {
      text: 'Select "Assign" to add the selected users and groups of users to your SAML application.',
      component: <StepImage src={azureStep12Image} alt="Step 4.4" />,
    },
  ];

  return (
    <Step
      title="Step 4: Assign People & Groups"
      instructionList={instructionList}
    />
  );
}
