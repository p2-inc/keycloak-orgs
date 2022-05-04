import React from "react";
import * as Images from "@app/images/azure/saml";
import { InstructionProps, Step, StepImage } from "@wizardComponents";
import { useHostname } from "@app/hooks/useHostname";

export function AzureStepFive() {
  const hostname = useHostname();
  const instructionList: InstructionProps[] = [
    {
      text: `In order for your users and groups of users to be synced to ${hostname} you will need to assign them to your Azure AD SAML Application. Select "Users and groups" from the "Manage" section of the navigations menu.`,
      component: <StepImage src={Images.AzureSaml9} alt="Step 4.1" />,
    },
    {
      text: 'Select "Add user/group" from the top menu.',
      component: <StepImage src={Images.AzureSaml10} alt="Step 4.2" />,
    },
    {
      text: 'Select "None selected" under the "Users and Groups". In the menu, select the users and groups of users that you want to add to the SAML application, and click "Select".',
      component: <StepImage src={Images.AzureSaml11} alt="Step 4.3" />,
    },
    {
      text: 'Select "Assign" to add the selected users and groups of users to your SAML application.',
      component: <StepImage src={Images.AzureSaml12} alt="Step 4.4" />,
    },
  ];

  return (
    <Step
      title="Step 5: Assign People & Groups"
      instructionList={instructionList}
    />
  );
}
