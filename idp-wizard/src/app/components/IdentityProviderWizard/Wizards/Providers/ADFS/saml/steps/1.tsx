import React, { FC } from "react";
import {
  ClipboardCopyComponent,
  InstructionProps,
  Step,
  StepImage,
} from "@wizardComponents";
import * as Images from "@app/images/adfs/saml";

interface Props {
  federationMetadataAddress: string;
}

export const Step1: FC<Props> = ({ federationMetadataAddress }) => {
  const instructions: InstructionProps[] = [
    {
      text: (
        <div>
          Login to the ADFS server and open the ADFS management console, and
          right-click on <b>Relying Party Trust</b>. Select{" "}
          <b>Add Relying Party Trust</b> from the submenu.
        </div>
      ),
      component: <StepImage src={Images.ADFS_SAML_0} alt="Step 1.1" />,
    },
    {
      text: (
        <div>
          In the window that appears, select <b>Claims Aware</b> and click
          Start.
        </div>
      ),
      component: <StepImage src={Images.ADFS_SAML_1} alt="Step 1.2" />,
    },
    {
      component: (
        <ClipboardCopyComponent
          label="Copy the Federation metadata address"
          initialValue={federationMetadataAddress}
        />
      ),
    },
    {
      text: (
        <div>
          In the <b>Select Data Source</b> step in the ADFS wizard, select the
          option labeled{" "}
          <b>
            Import data about the relying party published online or on a local
            network
          </b>
          , input the following link into the <b>Federation metadata address</b>{" "}
          input field, and click <b>Next</b>.
        </div>
      ),
      component: <StepImage src={Images.ADFS_SAML_2} alt="Step 1.3" />,
    },
    {
      text: (
        <div>
          In the <b>Select Data Source</b> step in the ADFS wizard, enter the
          <b>Display Name</b> and click <b>Next</b>.
        </div>
      ),
      component: <StepImage src={Images.ADFS_SAML_3} alt="Step 1.4" />,
    },
  ];

  return (
    <Step
      title="Step 1: Setup Relying Party Trust"
      instructionList={instructions}
    />
  );
};
