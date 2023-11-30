import Button from "components/elements/forms/buttons/button";
import SectionHeader from "components/navs/section-header";
import { useTranslation } from "react-i18next";
import { Link } from "react-router-dom";

const DeleteProfile = () => {
  const { t } = useTranslation();

  return (
    <div className="pt-10">
      <div className="space-y-4 rounded border border-red-500 p-6">
        <SectionHeader
          variant="medium"
          title={t("deleteYourProfile")}
          description={t(
            "permanentlyRemoveYourProfileAndAllOfItsContentsThisActionIsNotReversibleSoPleaseContinueWithCaution"
          )}
        />
        <div>
          <Link to={`/profile/delete`}>
            <Button isBlackButton>{t("deleteYourProfile")}</Button>
          </Link>
        </div>
      </div>
    </div>
  );
};

export default DeleteProfile;
