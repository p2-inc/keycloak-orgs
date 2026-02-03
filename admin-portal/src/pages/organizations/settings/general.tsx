import Button from "@/components/elements/forms/buttons/button";
import RHFFormTextInputWithLabel from "@/components/elements/forms/inputs/rhf-text-input-with-label";
import SectionHeader from "@/components/navs/section-header";
import P2Toast from "@/components/utils/toast";
import { config } from "@/config";
import { t } from "i18next";
import { P2Params } from "index";
import { useEffect } from "react";
import { useForm } from "react-hook-form";
import { useParams } from "react-router-dom";
import {
  useGetOrganizationByIdQuery,
  useUpdateOrganizationMutation,
} from "@/store/apis/orgs";
import { SettingsProps } from ".";

const SettingsGeneral = ({ hasManageOrganizationRole }: SettingsProps) => {
  const { orgId } = useParams<keyof P2Params>() as P2Params;
  const { realm } = config.env;

  const { data: org, isLoading: isLoadingOrganization } =
    useGetOrganizationByIdQuery({
      orgId: orgId!,
      realm,
    });

  const {
    register,
    handleSubmit,
    formState: { errors },
    setValue,
  } = useForm();

  useEffect(() => {
    setValue("id", org?.id);
    setValue("name", org?.name);
    setValue("displayName", org?.displayName);
  }, [org]);

  const [updateOrg, { isLoading }] = useUpdateOrganizationMutation();

  async function onSubmit(data) {
    const updatedOrg = { ...org, ...data };

    await updateOrg({
      orgId,
      realm,
      organizationRepresentation: updatedOrg,
    })
      .unwrap()
      .then(() => {
        P2Toast({
          success: true,
          title: `Organization updated successfully.`,
        });
      })
      .catch((e) => {
        let errorMsg = e.data.error;
        if (e.status === 401) {
          errorMsg = "Missing correct role to perform this action.";
        }
        return P2Toast({
          error: true,
          title: `Error during Organization update. ${errorMsg}`,
        });
      });
  }

  return (
    <div className="space-y-4">
      <div>
        <SectionHeader
          title={t("general")}
          description={t("viewOrganizationInformationChangeDisplayName")}
        />
      </div>
      <form onSubmit={handleSubmit(onSubmit)} className="max-w-xl space-y-4">
        <RHFFormTextInputWithLabel
          label={t("id")}
          slug="id"
          register={register}
          inputArgs={{ defaultValue: org?.id, disabled: true }}
        />
        <RHFFormTextInputWithLabel
          label={t("name")}
          slug="name"
          register={register}
          inputArgs={{ defaultValue: org?.name, disabled: true }}
        />
        <RHFFormTextInputWithLabel
          label={t("displayName")}
          slug="displayName"
          register={register}
          registerArgs={{
            min: 3,
            max: 50,
          }}
          error={errors.displayName}
          inputArgs={{
            defaultValue: org?.displayName,
            disabled: isLoadingOrganization,
          }}
        />
        <Button
          isBlackButton
          type="submit"
          disabled={
            isLoading || isLoadingOrganization || !hasManageOrganizationRole
          }
        >
          {t("updateOrganization")}
        </Button>
      </form>
    </div>
  );
};

export default SettingsGeneral;
