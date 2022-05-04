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
      text: (
        <div>
          Select <b>Add Rule</b> in the <b>Edit Claims Issuance Policy</b>{" "}
          window.
        </div>
      ),
      component: <StepImage src={Images.ADFS_SAML_7} alt="Step 3.1" />,
    },
    {
      text: (
        <div>
          Select <b>Transform an Incoming Claim</b> and click <b>Next</b>.
        </div>
      ),
      component: <StepImage src={Images.ADFS_SAML_8} alt="Step 3.2" />,
    },
    {
      text: (
        <div>
          In the following <b>Add Transform Claim Rule Wizard</b>, name the
          <b>Claim rule name</b> “Name ID”. For <b>Incoming claim type</b>,
          select UPN. For <b>Outgoing claim type</b>, select “Name ID”. For the
          <b>Outgoing name ID format</b>, select “Persistent Identifier”. Click
          <b>Finish</b> to save and continue.
        </div>
      ),
      component: <StepImage src={Images.ADFS_SAML_9} alt="Step 3.3" />,
    },
    {
      text: (
        <div>
          Again, select <b>Add Rule</b> in the{" "}
          <b>Edit Claims Issuance Policy</b> window.
        </div>
      ),
      component: <StepImage src={Images.ADFS_SAML_10} alt="Step 3.4" />,
    },
    {
      text: (
        <div>
          Select <b>Send LDAP Attributes as Claims</b> and click <b>Next</b>.
        </div>
      ),
      component: <StepImage src={Images.ADFS_SAML_11} alt="Step 3.5" />,
    },
    {
      text: (
        <div>
          Submit <b>Attributes</b> as <b>Claim rule name</b>, select{" "}
          <b>Active Directory</b> as <b>Attribute Store</b>, and create the
          following attribute mappings.
        </div>
      ),
      component: <StepImage src={Images.ADFS_SAML_12} alt="Step 3.6" />,
    },
    {
      component: (
        <div>
          <DoubleItemClipboardCopy
            leftValue="E-Mail-Addresses"
            rightValue="E-Mail Address"
          />
          <DoubleItemClipboardCopy
            leftValue="Given-Name"
            rightValue="Given Name"
          />
          <DoubleItemClipboardCopy leftValue="Surname" rightValue="Surname" />
          <DoubleItemClipboardCopy
            leftValue="SAM-Account-Name"
            rightValue="Subject Name"
          />
        </div>
      ),
    },
    {
      component: (
        <div>
          Click OK in the <b>Edit Claims issuance Policy</b> window to complete
          this step.
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
