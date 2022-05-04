import React, { FC } from "react";
import { InstructionProps, Step, StepImage } from "@wizardComponents";
import * as Images from "@app/images/adfs/saml";

export const Step2: FC = () => {
  const instructions: InstructionProps[] = [
    {
      text: (
        <div>
          In the <b>Choose Access Control Policy</b> step in the ADFS wizard,
          select an access control policy that is most appropriate for your
          users. <b>Permit everyone</b> is the most permissive, and is useful
          while you are testing the integration.
        </div>
      ),
      component: <StepImage src={Images.ADFS_SAML_4} alt="Step 2.1" />,
    },
    {
      text: (
        <div>
          Validate all the information on the <b>Ready to Add Trust</b> step in
          the ADFS wizard and click <b>Next</b> to save the configuration.
        </div>
      ),
      component: <StepImage src={Images.ADFS_SAML_5} alt="Step 2.2" />,
    },
    {
      text: (
        <div>
          In the <b>Finish</b> step in the ADFS wizard, select{" "}
          <b>Configure claims issuance policy for this application</b> and click
          <b>Close</b>.
        </div>
      ),
      component: <StepImage src={Images.ADFS_SAML_6} alt="Step 2.3" />,
    },
  ];

  return (
    <Step
      title="Step 2: Assign People and Groups"
      instructionList={instructions}
    />
  );
};
