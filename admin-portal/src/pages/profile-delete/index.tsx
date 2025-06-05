import RHFFormTextInputWithLabel from "@/components/elements/forms/inputs/rhf-text-input-with-label";
import SectionHeader from "@/components/navs/section-header";
import { User } from "lucide-react";
import { useForm } from "react-hook-form";
import { Link } from "react-router-dom";
import cs from "classnames";
import Button from "@/components/elements/forms/buttons/button";
import { useTranslation } from "react-i18next";
import { toLower } from "lodash";
import { AIACommand } from "@/services/aia-command";
import { keycloakService } from "@/keycloak";

const deleteAccount = new AIACommand(keycloakService, "delete_account");

const loadingIcon = (
  <div>
    <div className={cs("relative h-12 w-12 overflow-hidden rounded-md")}>
      <div className="absolute -inset-10 z-10 bg-gradient-to-tr from-red-600 to-red-800"></div>
      <div className="absolute inset-[2px] z-20 flex items-center justify-center rounded bg-white">
        <User />
      </div>
    </div>
  </div>
);

const ProfileDelete = () => {
  const { t } = useTranslation();
  const { register, watch, handleSubmit } = useForm();
  const watchConfirmDelete = watch("delete");
  const confirmDelete = ["delete", "supprimer"].includes(
    toLower(watchConfirmDelete)
  );

  const onSubmit = () => {
    if (confirmDelete) {
      deleteAccount.execute();
    }
  };

  return (
    <div className="my-16 rounded-md border border-red-500 p-6 md:mx-auto md:max-w-prose">
      <div className="space-y-4">
        <SectionHeader
          title={t("profile-delete-title")}
          description={t(
            "permanentlyRemoveYourProfileAndAllOfItsContentsThisActionIsNotReversibleSoPleaseContinueWithCaution"
          )}
          icon={loadingIcon}
          rightContent={
            <Link
              to={`/profile/general`}
              className="inline-block rounded-lg px-4 py-2 font-medium opacity-60 transition hover:bg-gray-100 hover:opacity-100"
            >
              {t("cancel")}
            </Link>
          }
        />
        <form onSubmit={handleSubmit(onSubmit)}>
          <div className="space-y-4">
            <RHFFormTextInputWithLabel
              slug="delete"
              label={t("profile-delete-write-to-confirm")}
              register={register}
              inputArgs={{
                placeholder: "",
                autoFocus: true,
              }}
            />
            <Button isBlackButton disabled={!confirmDelete} type="submit">
              {t("profile-delete-confirm")}
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default ProfileDelete;
