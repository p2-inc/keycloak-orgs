import React, { FC } from "react";
import {
  InstructionProps,
  Step,
  StepImage,
  DoubleItemClipboardCopy,
} from "@wizardComponents";
import * as Images from "@app/images/aws";

const codeString = `{
  "identityProviderAlias": "<idp_alias>",
  "config": {
    "syncMode": "INHERIT",
    "attributes": "[]",
    "attribute.friendly.name": "email",
    "user.attribute": "email"
  },
  "name": "email",
  "identityProviderMapper": "saml-user-attribute-idp-mapper"
}`;

const realmS = "<realm>";
const idpAliasS = "<idp_alias>";
const codeEndpoint = (
  <code>
    auth/admin/realms/{realmS}/identity-provider/instances/{idpAliasS}/mappers
  </code>
);

export const Step4: FC = () => {
  const instructions: InstructionProps[] = [
    {
      component: (
        <div>
          In the "Attribute mappings" section, provide the following attribute
          mappings and select "Save". Note you may need to click "+ Add new
          attribute mapping" to configure each of the mappings. Format should be
          set to "unspecified" for each of the mappings.
        </div>
      ),
    },
    {
      component: (
        <div>
          <DoubleItemClipboardCopy
            leftValue="Subject"
            rightValue="${user.subject}"
          />
          <DoubleItemClipboardCopy
            leftValue="firstName"
            rightValue="${user.givenName}"
          />
          <DoubleItemClipboardCopy
            leftValue="lastName"
            rightValue="${user.familyName}"
          />
          <DoubleItemClipboardCopy
            leftValue="email"
            rightValue="${user.email}"
          />
        </div>
      ),
    },
    {
      component: <StepImage src={Images.AWS_SSO_SAML_5} alt="Step 4.1" />,
    },
    {
      component: (
        <div>
          Note: each of these mappings (email, firstName, lastName) will have to
          be made in Keycloak after the creation of the IdP. They are made by
          calling the{" "}
          <code>
            auth/admin/realms/{realmS}/identity-provider/instances/{idpAliasS}
            /mappers
          </code>{" "}
          endpoint with the following payload:
        </div>
      ),
    },
    {
      component: (
        <pre>
          <code>{codeString}</code>
        </pre>
      ),
    },
  ];

  return (
    <Step
      title="Step 4: Configure Attribute Mapping"
      instructionList={instructions}
    />
  );
};
