import SectionHeader from "components/navs/section-header";
import { useParams } from "react-router-dom";
import { apiRealm } from "store/apis/helpers";
import { useGetOrganizationByIdQuery } from "store/apis/orgs";

const SettingsGeneral = () => {
  let { orgId } = useParams();
  const { data: org } = useGetOrganizationByIdQuery({
    orgId: orgId!,
    realm: apiRealm,
  });
  return (
    <div className="space-y-4">
      <div>
        <SectionHeader
          title="General Settings"
          description="One morning, when Gregor Samsa woke from troubled dreams, he found himself transformed in his bed into a horrible vermin."
        />
      </div>
    </div>
  );
};

export default SettingsGeneral;
