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
      text: "Login to the ADFS server and open the ADFS management console, and right-click on Relying Party Trust. Select Add Relying Party Trust from the submenu. ",
      component: <StepImage src={Images.ADFS_SAML_1} alt="Step 1.1" />,
    },
    {
      text: "In the window that appears, select Claims Aware and click Start.",
      component: <StepImage src={Images.ADFS_SAML_2} alt="Step 1.2" />,
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
      text: "In the Select Data Source step in the ADFS wizard, select the option labeled Import data about the relying party published online or on a local network, input the following link into the Federation metadata address input field, and click Next.",
      component: <StepImage src={Images.ADFS_SAML_3} alt="Step 1.3" />,
    },
    {
      text: "In the Select Data Source step in the ADFS wizard, enter the Display Name and click Next.",
      component: <StepImage src={Images.ADFS_SAML_4} alt="Step 1.4" />,
    },
  ];

  return (
    <Step
      title="Step 1: Setup Relying Party Trust"
      instructionList={instructions}
    />
  );
};
