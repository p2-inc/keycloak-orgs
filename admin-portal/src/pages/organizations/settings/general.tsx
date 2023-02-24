import { useParams } from "react-router-dom";
import { apiRealm } from "store/apis/helpers";
import { useGetOrganizationByIdQuery } from "store/apis/orgs";

const SettingsGeneral = () => {
  let { orgId } = useParams();
  const { data: org } = useGetOrganizationByIdQuery({
    orgId: orgId!,
    realm: apiRealm,
  });
  return <div>General Settings</div>;
};

export default SettingsGeneral;
