import React, { FC } from "react";
import { InstructionProps, Step } from "@wizardComponents";
import { API_RETURN_PROMISE } from "@app/configurations/api-status";
import { ClientCredentials, ClientCreds } from "./forms";
import { useHostname } from "@app/hooks/useHostname";

type Props = {
  validateCredentials: ({
    clientId,
    clientSecret,
  }: ClientCreds) => API_RETURN_PROMISE;
  credentials: ClientCreds;
};

export const Step3: FC<Props> = ({
  validateCredentials,
  credentials = { clientId: "", clientSecret: "" },
}) => {
  const hostname = useHostname();
  const instructions: InstructionProps[] = [
    {
      component: (
        <div>
          Your identity provider should provide you with credentials that will
          allow you to authenticate with {hostname}. These are usually called a
          Client ID and a Client Secret or Key.
        </div>
      ),
    },
    {
      component: (
        <ClientCredentials
          handleFormSubmit={validateCredentials}
          credentials={credentials}
        />
      ),
    },
  ];

  return (
    <Step
      title="Step 3: Provide The Client Credentials"
      instructionList={instructions}
    />
  );
};
