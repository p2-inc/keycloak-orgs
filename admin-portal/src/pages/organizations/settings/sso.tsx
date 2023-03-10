import Button from "components/elements/forms/buttons/button";
import SectionHeader from "components/navs/section-header";
import OpenSSOLink from "components/utils/ssoLink";
import { P2Params } from "index";
import { useParams } from "react-router-dom";

const SettingsSSO = () => {
  const { orgId } = useParams<keyof P2Params>() as P2Params;
  return (
    <div className="space-y-4">
      <div>
        <a href="#sso"></a>
        <SectionHeader title="SSO" description="Add an SSO provider." />
        <div className="mt-4">
          <Button isBlackButton onClick={() => OpenSSOLink({ orgId })}>
            Setup SSO
          </Button>
        </div>
      </div>
    </div>
  );
};

export default SettingsSSO;
