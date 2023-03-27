import Button from "components/elements/forms/buttons/button";
import SectionHeader from "components/navs/section-header";
import OpenSSOLink from "components/utils/ssoLink";
import { P2Params } from "index";
import { useParams } from "react-router-dom";
import { SettingsProps } from ".";

const SettingsSSO = ({ hasManageIDPRole }: SettingsProps) => {
  const { orgId } = useParams<keyof P2Params>() as P2Params;
  return (
    <div className="space-y-4">
      <div>
        <SectionHeader title="SSO" description="Add an SSO provider." />
        <div className="mt-4">
          <Button
            isBlackButton
            onClick={() => OpenSSOLink({ orgId })}
            disabled={!hasManageIDPRole}
          >
            Setup SSO
          </Button>
        </div>
      </div>
    </div>
  );
};

export default SettingsSSO;
