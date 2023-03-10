import Button from "components/elements/forms/buttons/button";
import RHFFormTextInputWithLabel from "components/elements/forms/inputs/rhf-text-input-with-label";
import FormTextInputWithLabel from "components/elements/forms/inputs/text-input-with-label";
import SectionHeader from "components/navs/section-header";
import P2Toast from "components/utils/toast";
import { P2Params } from "index";
import { useEffect } from "react";
import { useForm } from "react-hook-form";
import { useParams } from "react-router-dom";
import { apiRealm } from "store/apis/helpers";
import {
  useGetOrganizationByIdQuery,
  useUpdateOrganizationMutation,
} from "store/apis/orgs";

const SettingsGeneral = () => {
  const { orgId } = useParams<keyof P2Params>() as P2Params;

  const { data: org, isLoading: isLoadingOrganization } =
    useGetOrganizationByIdQuery({
      orgId: orgId!,
      realm: apiRealm,
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
    console.log("🚀 ~ file: general.tsx:26 ~ onSubmit ~ data:", data);

    const updatedOrg = { ...org, ...data };

    const resp = await updateOrg({
      orgId,
      realm: apiRealm,
      organizationRepresentation: updatedOrg,
    });

    //@ts-ignore
    if (resp.error) {
      console.log("🚀 ~ file: general.tsx:54 ~ onSubmit ~ resp:", resp);
      //@ts-ignore
      let errorMsg = resp.error?.data?.error;
      //@ts-ignore
      if (resp.error?.status === 401) {
        errorMsg = "Missing correct role to perform this action.";
      }
      return P2Toast({
        error: true,
        title: `Error during Organization update. ${errorMsg}`,
      });
    }

    P2Toast({
      success: true,
      title: `Organization updated successfully.`,
    });
  }

  return (
    <div className="space-y-4">
      <div>
        <SectionHeader
          title="General"
          description="View organization information. Change display name."
        />
      </div>
      <form onSubmit={handleSubmit(onSubmit)} className="max-w-xl space-y-4">
        <RHFFormTextInputWithLabel
          label="Id"
          slug="id"
          register={register}
          inputArgs={{ defaultValue: org?.id, disabled: true }}
        />
        <RHFFormTextInputWithLabel
          label="Name"
          slug="name"
          register={register}
          inputArgs={{ defaultValue: org?.name, disabled: true }}
        />
        <RHFFormTextInputWithLabel
          label="Display Name"
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
          disabled={isLoading || isLoadingOrganization}
        >
          Update Organization
        </Button>
      </form>
    </div>
  );
};

export default SettingsGeneral;
