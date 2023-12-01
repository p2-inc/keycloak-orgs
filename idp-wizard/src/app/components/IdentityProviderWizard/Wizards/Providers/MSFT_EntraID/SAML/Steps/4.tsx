import React from "react";
import {
  TripleItemClipboardCopy,
  InstructionProps,
  Step,
  StepImage,
} from "@wizardComponents";
import * as Images from "@app/images/msft_entra_id/saml";

interface IClaims {
  name: string;
  namespace: string;
  value: string;
}

export function EntraIdStepFour() {
  const claimNames: IClaims[] = [
    {
      name: "emailaddress",
      namespace:
        "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/emailaddress",
      value: "user.mail",
    },
    {
      name: "givenname",
      namespace:
        "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/givenname",
      value: "user.givenname",
    },
    {
      name: "name",
      namespace: "http://schemas.microsoft.com/identity/claims/name",
      value: "user.userprincipalname",
    },
    {
      name: "surname",
      namespace:
        "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/surname",
      value: "user.surname",
    },
  ];

  const instructionList: InstructionProps[] = [
    {
      text: "Click the Edit icon in the top right of the second section.",
      component: <StepImage src={Images.AzureSaml7} alt="Step 4.1" />,
    },
    {
      text: "If the claims are as shown in the image, you can accept these default and move on to the next step. If not, click each Claim name in the Additional claims section to open an edit page.",
      component: <StepImage src={Images.AzureSaml8} alt="Step 4.2" />,
    },
    {
      text: (
        <div>
          In the edit page for each claim, paste the following values in the
          <i>Name</i>, <i>Namespace</i> and <i>Source Attribute</i>.
        </div>
      ),
      component: <StepImage src={Images.EntraId1} alt="Step 4.4" />,
    },
    {
      text: "Copy the following Name, Namespace and Source Attribute values.",
      component: claimNames.map(
        (
          { name: firstValue, namespace: secondValue, value: thirdValue },
          index
        ) => (
          <TripleItemClipboardCopy
            firstLabel="Name"
            secondLabel="Namespace"
            thirdLabel="Source Attribute"
            firstValue={firstValue}
            secondValue={secondValue}
            thirdValue={thirdValue}
            key={index}
          />
        )
      ),
    },
  ];

  return (
    <Step
      title="Step 4: User Attributes & Claims"
      instructionList={instructionList}
    />
  );
}
