import React, { useState } from 'react';
import Button from "components/elements/forms/buttons/button";
import FormTextInputWithLabel from "components/elements/forms/inputs/text-input-with-label";
import SectionHeader from "components/navs/section-header";
import { AIACommand } from "services/aia-command";
import { KeycloakService } from "services/keycloak.service";
import { apiRealm } from "store/apis/helpers";
import { useKeycloak } from "@react-keycloak/web";
import { useFeatureFlags } from "store/feature-flags/hooks";
import { useGetAccountQuery, useUpdateAccountMutation } from 'store/apis/profile';

interface FormFields {
  readonly username?: string;
  readonly firstName?: string;
  readonly lastName?: string;
  readonly email?: string;
  attributes?: { locale?: [string] };
}

interface AccountPageState {
  readonly errors: FormFields;
  readonly formFields: FormFields;
}

const GeneralProfile = () => {
  const { keycloak, initialized } = useKeycloak();
  const [ state, setState ] = useState<AccountPageState | undefined>(undefined);
  const { featureFlags } = useFeatureFlags();
  const { data, error } = useGetAccountQuery({ userProfileMetadata: true, realm: apiRealm });
  const [ updateAccount, { isSuccess }] = useUpdateAccountMutation();

  /*
  export type AccountRepresentation = {
    id?: string;
    username?: string;
    firstName?: string;
    lastName?: string;
    email?: string;
    emailVerified?: boolean;
    userProfileMetadata?: UserProfileMetadataRepresentation;
  };
  */


  const DEFAULT_STATE: AccountPageState = {
    errors: {
        username: '',
        firstName: '',
        lastName: '',
        email: ''
    },
    formFields: {
        username: '',
        firstName: '',
        lastName: '',
        email: '',
        attributes: {}
    }
  };

  //setState(DEFAULT_STATE);
  console.log(data);
  const handleSubmit = (event: React.FormEvent<HTMLFormElement>): void => {

  };


  const handleCancel = (): void => {
    //how do i refresh the data
  };

  const handleDelete = (keycloak: KeycloakService): void => {
    new AIACommand(keycloak, "delete_account").execute();
  };

  const handleEmailUpdate = (keycloak: KeycloakService): void => {
    new AIACommand(keycloak, "UPDATE_EMAIL").execute();
  };

  return (
    <div>
      <div className="mb-12">
        <SectionHeader
          title="General"
          description="One morning, when Gregor Samsa woke from troubled dreams, he found himself transformed in his bed into a horrible vermin."
        />
      </div>
      <form className="space-y-4" onSubmit={(event) => handleSubmit(event)}>
        {featureFlags.updateEmailFeatureEnabled && (
          <FormTextInputWithLabel
            slug="email"
            label="Email"
            inputArgs={{ placeholder: "you@email.com" }}
          />
        )}
        <FormTextInputWithLabel
          slug="firstName"
          label="First Name"
          inputArgs={{ placeholder: "jane" }}
        />
        <FormTextInputWithLabel
          slug="lastName"
          label="Last Name"
          inputArgs={{ placeholder: "doe" }}
        />
        <Button isBlackButton>Save changes</Button>
      </form>
    </div>
  );
};

export default GeneralProfile;
