import React, { FC } from "react";
import {
  InstructionProps,
  Step,
  StepImage,
  DoubleItemClipboardCopy,
} from "@wizardComponents";
import * as Images from "@app/images/adfs/saml";

export const Step3: FC = () => {
  const instructions: InstructionProps[] = [
    {
      text: "Click Add Rule in the Edit Claims Issuance Policy window.",
      component: <StepImage src={Images.ADFS_SAML_8} alt="Step 3.1" />,
    },
    {
      text: "Select Send LDAP Attributes as Claims and click Next.",
      component: <StepImage src={Images.ADFS_SAML_9} alt="Step 3.2" />,
    },
    {
      text: "Submit Attributes as Claim rule name, select Active Directory as Attribute Store, and create the following attribute mappings.",
      component: <StepImage src={Images.ADFS_SAML_10} alt="Step 3.3" />,
    },
    {
      component: (
        <div>
          <DoubleItemClipboardCopy
            leftValue="User-Principal-Name"
            rightValue="UPN"
          />
          <DoubleItemClipboardCopy
            leftValue="E-Mail-Addresses"
            rightValue="E-Mail Address"
          />
          <DoubleItemClipboardCopy
            leftValue="Given-Name"
            rightValue="Given Name"
          />
          <DoubleItemClipboardCopy leftValue="Surname" rightValue="Surname" />
        </div>
      ),
    },
  ];

  return (
    <Step
      title="Step 3: Configure Attribute Mapping"
      instructionList={instructions}
    />
  );
};
