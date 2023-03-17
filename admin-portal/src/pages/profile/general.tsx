import React, { useState } from "react";
import Button from "components/elements/forms/buttons/button";
import FormTextInputWithLabel from "components/elements/forms/inputs/text-input-with-label";
import SectionHeader from "components/navs/section-header";
import { AIACommand } from "services/aia-command";
import { KeycloakService } from "services/keycloak.service";
import { apiRealm } from "store/apis/helpers";
import { useKeycloak } from "@react-keycloak/web";
import { useFeatureFlags } from "store/feature-flags/hooks";
import {
  useGetAccountQuery,
  useUpdateAccountMutation,
} from "store/apis/profile";
import { useTranslation } from "react-i18next";
import GeneralLoader from "components/loaders/general";

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

const ROOT_ATTRIBUTES = ["username", "firstName", "lastName", "email"];
const isRootAttribute = (attr?: string) => attr && ROOT_ATTRIBUTES.includes(attr);
const fieldName = (name: string) => `${isRootAttribute(name) ? "" : "attributes."}${name}`;
const unWrap = (key: string) => key.substring(2, key.length - 1);
const isBundleKey = (key?: string) => key?.includes("${");

const GeneralProfile = () => {
  const { t } = useTranslation();
  const { keycloak, initialized } = useKeycloak();
  const [state, setState] = useState<AccountPageState | undefined>(undefined);
  const { featureFlags } = useFeatureFlags();
  const { data, error, isLoading, refetch } = useGetAccountQuery({
    userProfileMetadata: true,
    realm: apiRealm,
  });
  const [updateAccount, { isSuccess }] = useUpdateAccountMutation();

  const DEFAULT_STATE: AccountPageState = {
    errors: {
      username: "",
      firstName: "",
      lastName: "",
      email: "",
    },
    formFields: {
      username: "",
      firstName: "",
      lastName: "",
      email: "",
      attributes: {},
    },
  };

  //setState(DEFAULT_STATE);
  console.log(data);
  const handleSubmit = (): void => {
    //updateAccount()
  };

  const handleCancel = (): void => {
    //how do i refresh the data
    refetch();
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
          title="Personal information"
          description="Manage your user profile information."
        />
      </div>
      {isLoading && <GeneralLoader />}
      {!isLoading && (
        <form className="max-w-xl space-y-4"> <>
          {!featureFlags.registrationEmailAsUsername && data?.username != undefined && (
            <FormTextInputWithLabel
              slug="username"
              label={t("username")}
              inputArgs={{ value: data?.username }}
            />
          )}
          {featureFlags.updateEmailFeatureEnabled && (
            <FormTextInputWithLabel
              slug="email"
              label={t("email")}
              inputArgs={{ value: data?.email }}
            />
          )}
          <FormTextInputWithLabel
            slug="firstName"
            label="First Name"
            inputArgs={{ value: data?.firstName }}
          />
          <FormTextInputWithLabel
            slug="lastName"
            label="Last Name"
            inputArgs={{ value: data?.lastName }}
          />
          {(data?.userProfileMetadata?.attributes || []).filter((attribute) => !isRootAttribute(attribute.name)).map((attribute) =>(
            <FormTextInputWithLabel
              slug={attribute.name!}
              label={
                (isBundleKey(attribute.displayName)
                  ? t(unWrap(attribute.displayName!))
                  : attribute.displayName) || attribute.name!
              }
              inputArgs={{ value: data ? data[attribute.name!] : "" }}
            />
          ))}
          <div className="space-x-2">
            <Button isBlackButton onClick={handleSubmit}>
              Save
            </Button>
            <Button onClick={handleCancel}>Cancel</Button>
          </div>
        </></form>
      )}
    </div>
  );
};

export default GeneralProfile;
