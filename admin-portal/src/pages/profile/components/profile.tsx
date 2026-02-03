import { useEffect } from "react";
import Button from "@/components/elements/forms/buttons/button";
import SectionHeader from "@/components/navs/section-header";
import { config } from "@/config";
import {
  useGetAccountQuery,
  useUpdateAccountMutation,
} from "@/store/apis/profile";
import { useTranslation } from "react-i18next";
import { useForm } from "react-hook-form";
import RHFFormTextInputWithLabel from "@/components/elements/forms/inputs/rhf-text-input-with-label";
import P2Toast from "@/components/utils/toast";

const ProfileData = () => {
  const { t } = useTranslation();
  const { features: featureFlags, realm } = config.env;

  const { data: account, isLoading: isLoadingAccount } = useGetAccountQuery({
    userProfileMetadata: true,
    realm,
  });

  const [updateAccount, { isLoading: isUpdatingAccount }] =
    useUpdateAccountMutation();

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
    setValue("username", account?.username);
  }, [account, setValue, featureFlags.registrationEmailAsUsername]);

  const onSubmit = async (formData) => {
    const updatedAccount = {
      ...account,
      ...formData,
    };

    updateAccount({
      accountRepresentation: updatedAccount,
      realm,
    })
      .unwrap()
      .then(() => {
        P2Toast({
          success: true,
          title: t("profile-toast-success"),
        });
      })
      .catch((err) => {
        return P2Toast({
          error: true,
          title: `${t("profile-toast-error")} ${err.data.error}`,
        });
      });
  };

  return (
    <>
      <div className="mb-12">
        <SectionHeader
          title={t("personalInformation")}
          description={t("manageYourUserProfileInformation")}
        />
      </div>
      <form className="max-w-xl space-y-4" onSubmit={handleSubmit(onSubmit)}>
        <>
          {featureFlags.registrationEmailAsUsername ? (
            // Username is email, can edit username but must be a valid email
            <RHFFormTextInputWithLabel
              slug="email"
              label={`${t("username")} / ${t("email")}`}
              register={register}
              registerArgs={{
                required: true,
                pattern: /\S+@\S+\.\S+/,
              }}
              inputArgs={{
                disabled: isLoadingAccount || !featureFlags.editUsernameAllowed,
                placeholder: t("profile-email-placeholder"),
                type: "email",
                title: t("usernameEmailSame"),
              }}
              error={errors.username}
              helpText={t("usernameEmailSame")}
            />
          ) : (
            // Username is freeform, can edit username independent of email
            <>
              <RHFFormTextInputWithLabel
                slug="username"
                label={t("username")}
                register={register}
                registerArgs={{
                  required: true,
                }}
                inputArgs={{
                  disabled:
                    isLoadingAccount || !featureFlags.editUsernameAllowed,
                  placeholder: "you",
                  type: "text",
                }}
                error={errors.username}
              />
              <RHFFormTextInputWithLabel
                slug="email"
                label={t("email")}
                register={register}
                registerArgs={{
                  required: true,
                  pattern: /\S+@\S+\.\S+/,
                }}
                inputArgs={{
                  disabled:
                    isLoadingAccount || !featureFlags.updateEmailFeatureEnabled,
                  placeholder: t("profile-email-placeholder"),
                  type: "email",
                }}
                error={errors.email}
              />
            </>
          )}

          <RHFFormTextInputWithLabel
            slug="firstName"
            label={t("firstName")}
            register={register}
            inputArgs={{
              disabled: isLoadingAccount,
              placeholder: t("firstName"),
            }}
            error={errors.firstName}
          />
          <RHFFormTextInputWithLabel
            slug="lastName"
            label={t("lastName")}
            register={register}
            inputArgs={{
              disabled: isLoadingAccount,
              placeholder: t("lastName"),
            }}
            error={errors.lastName}
          />
          <div className="space-x-2">
            <Button
              isBlackButton
              type="submit"
              disabled={isUpdatingAccount || isLoadingAccount || !isDirty}
            >
              {t("save")}
            </Button>
            <Button
              type="button"
              onClick={() =>
                reset({
                  email: account?.email,
                  firstName: account?.firstName,
                  lastName: account?.lastName,
                })
              }
              disabled={isUpdatingAccount || !isDirty}
            >
              {t("reset")}
            </Button>
          </div>
        </>
      </form>
    </>
  );
};

export default ProfileData;
