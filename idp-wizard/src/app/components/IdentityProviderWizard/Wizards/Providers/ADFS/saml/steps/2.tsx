import React, { FC } from "react";
import { InstructionProps, Step, StepImage } from "@wizardComponents";
import * as Images from "@app/images/adfs/saml";

export const Step2: FC = () => {
  const instructions: InstructionProps[] = [
    {
      text: "In the Choose Access Control Policy step in the ADFS wizard, select an access control policy that is most appropriate for your users. Permit everyone is the most permissive, and is useful while you are testing the integration.",
      component: <StepImage src={Images.ADFS_SAML_5} alt="Step 2.1" />,
    },
    {
      text: "Validate all the information on the Ready to Add Trust step in the ADFS wizard and click Next to save the configuration.",
      component: <StepImage src={Images.ADFS_SAML_6} alt="Step 2.2" />,
    },
    {
      text: "In the Finish step in the ADFS wizard, select Configure claims issuance policy for this application and click Close.",
      component: <StepImage src={Images.ADFS_SAML_7} alt="Step 2.3" />,
    },
  ];

  return (
    <Step
      title="Step 2: Assign People and Groups"
      instructionList={instructions}
    />
  );
};
