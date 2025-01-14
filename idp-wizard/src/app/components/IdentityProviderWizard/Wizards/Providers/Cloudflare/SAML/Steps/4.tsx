import React from "react";
import {
  DoubleItemClipboardCopy,
  InstructionProps,
  Step,
  StepImage,
} from "@wizardComponents";
import * as Images from "@app/images/cloudflare/saml";

export function CloudflareStepFour() {
  const instructionList: InstructionProps[] = [
    {
      text: (
        <div>
          In the <b>SAML attribute statements</b> section, provide the following attribute
          statements by clicking <b>+ Add Statement</b> for each attribute. Set the{" "}
          <b>Name Format</b> for each attribute to <b>Basic</b>. Copy the following
        </div>
      ),
      component: <StepImage src={Images.CloudflareSaml5} alt="Step 4.1" />,
    },
    {
      component: (
        <div>
          Note that Cloudflare passes attributes directly from the identity providers configured in your Cloudflare Zero Trust organization.{" "}
          Ensure that your IdP is configured to provide these attributes to Cloudflare or adjust the attribute mapping as needed.{" "}
          The built-in OTP identity provider does not provide <b>firstName</b> or <b>lastName</b> attributes.
        </div>
      ),
    },
    {
      component: (
        <>
          <DoubleItemClipboardCopy
            leftValue="username"
            rightValue="username"
            leftLabel="Name"
            rightLabel="IdP Attribute"
          />
          <DoubleItemClipboardCopy
            leftValue="email"
            rightValue="email"
            leftLabel="Name"
            rightLabel="IdP Attribute"
          />
          <DoubleItemClipboardCopy
            leftValue="firstName"
            rightValue="firstName"
            leftLabel="Name"
            rightLabel="IdP Attribute"
          />
          <DoubleItemClipboardCopy
            leftValue="lastName"
            rightValue="lastName"
            leftLabel="Name"
            rightLabel="IdP Attribute"
          />
          
        </>
      ),
    },
    {
      component: (
        <div>
          Select <b>Save configuration</b> at the bottom of the page to save your changes.
        </div>
      ),
    },
  ];

  return (
    <Step
      title="Step 4: Configure Attribute Mapping"
      instructionList={instructionList}
    />
  );
}
