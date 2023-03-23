import { useEffect } from "react";
import Button from "components/elements/forms/buttons/button";
import SectionHeader from "components/navs/section-header";
import { config } from "config";
import {
  useGetAccountQuery,
  useUpdateAccountMutation,
} from "store/apis/profile";
import { useTranslation } from "react-i18next";
import { useForm } from "react-hook-form";
import RHFFormTextInputWithLabel from "components/elements/forms/inputs/rhf-text-input-with-label";
import P2Toast from "components/utils/toast";

const ROOT_ATTRIBUTES = ["username", "firstName", "lastName", "email"];
const isRootAttribute = (attr?: string) =>
  attr && ROOT_ATTRIBUTES.includes(attr);
const fieldName = (name: string) =>
  `${isRootAttribute(name) ? "" : "attributes."}${name}`;
const unWrap = (key: string) => key.substring(2, key.length - 1);
const isBundleKey = (key?: string) => key?.includes("${");

const GeneralProfile = () => {
  const { t } = useTranslation();
  const { features: featureFlags } = config.env;
  const { data: account, isLoading: isLoadingAccount } = useGetAccountQuery({
    userProfileMetadata: true,
    realm: config.env.realm,
  });

  const [updateAccount, { isLoading: isUpdatingAccount }] =
    useUpdateAccountMutation();

  /*
  //setState(DEFAULT_STATE);
  console.log(data);
  const handleSubmit = (): void => {
    //updateAccount()
  };

  const handleCancel = (): void => {
    //how do i refresh the data
    refetch();
  };
  */
  const {
    register,
    handleSubmit,
    formState: { errors, isDirty },
    setValue,
    reset,
  } = useForm();

  useEffect(() => {
    setValue("firstName", account?.firstName);
    setValue("lastName", account?.lastName);
    setValue("email", account?.email);
  }, [account, setValue]);

  const onSubmit = async (formData) => {
    const updatedAccount = {
      ...account,
      ...formData,
    };

    updateAccount({
      accountRepresentation: updatedAccount,
      realm: config.env.realm,
    })
      .unwrap()
      .then(() => {
        P2Toast({
          success: true,
          title: `Profile updated successfully.`,
        });
      })
      .catch((err) => {
        return P2Toast({
          error: true,
          title: `Error during Profile update. ${err.data.error}`,
        });
      });
  };

  /*
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
  */

  return (
    <div>
      <div className="mb-12">
        <SectionHeader
          title="Personal information"
          description="Manage your user profile information."
        />
      </div>
      <form className="max-w-xl space-y-4" onSubmit={handleSubmit(onSubmit)}>
        <>
          {!featureFlags.registrationEmailAsUsername && (
            <RHFFormTextInputWithLabel
              slug="username"
              label={t("username")}
              register={register}
              registerArgs={{
                required: true,
                pattern: /\S+@\S+\.\S+/,
              }}
              inputArgs={{
                disabled: isLoadingAccount,
                placeholder: "you",
                type: "username",
              }}
              error={errors.username}
            />
          )}
          {featureFlags.updateEmailFeatureEnabled && (
            <RHFFormTextInputWithLabel
              slug="email"
              label={t("email")}
              register={register}
              registerArgs={{
                required: true,
                pattern: /\S+@\S+\.\S+/,
              }}
              inputArgs={{
                disabled: isLoadingAccount,
                placeholder: "your@email.com",
                type: "email",
              }}
              error={errors.email}
            />
          )}
          <RHFFormTextInputWithLabel
            slug="firstName"
            label="First Name"
            register={register}
            inputArgs={{
              disabled: isLoadingAccount,
              placeholder: "First name",
            }}
            error={errors.firstName}
          />
          <RHFFormTextInputWithLabel
            slug="lastName"
            label="Last Name"
            register={register}
            inputArgs={{ disabled: isLoadingAccount, placeholder: "Last name" }}
            error={errors.lastName}
          />
          <div className="space-x-2">
            <Button
              isBlackButton
              type="submit"
              disabled={isUpdatingAccount || isLoadingAccount || !isDirty}
            >
              Save
            </Button>
            <Button
              type="button"
              onClick={() =>
                reset({
                  email: account?.username,
                  firstName: account?.firstName,
                  lastName: account?.lastName,
                })
              }
              disabled={isUpdatingAccount || !isDirty}
            >
              Reset
            </Button>
          </div>
        </>
      </form>
    </div>
  );
};

export default GeneralProfile;
