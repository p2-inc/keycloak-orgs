import Button from "components/elements/forms/buttons/button";
import SectionHeader from "components/navs/section-header";
import OpenSSOLink from "components/utils/ssoLink";
import { P2Params } from "index";
import { useParams } from "react-router-dom";
import { SettingsProps } from ".";
import { useTranslation } from "react-i18next";

const SettingsSSO = ({ hasManageIDPRole }: SettingsProps) => {
  const { t } = useTranslation();
  const { orgId } = useParams<keyof P2Params>() as P2Params;
  return (
    <div className="space-y-4">
      <div>
        <SectionHeader title={t("sso")} description={t("addAnSsoProvider")} />
        <div className="mt-4">
          <Button
            isBlackButton
            onClick={() => OpenSSOLink({ orgId })}
            disabled={!hasManageIDPRole}
          >
            {t("setupSso")}
          </Button>
        </div>
      </div>
    </div>
  );
};

export default SettingsSSO;
