import Button from "components/elements/forms/buttons/button";
import CopyBlock from "components/elements/organizations/copy-block";
import RoundedIcon from "components/elements/rounded-icon";
import { GlobeIcon } from "components/icons";
import SectionHeader from "components/navs/section-header";
import { useParams } from "react-router-dom";
import { apiRealm } from "store/apis/helpers";
import {
  useGetOrganizationDomainQuery,
  useGetOrganizationDomainsQuery,
} from "store/apis/orgs";

const addIcon = (
  <RoundedIcon className="my-4">
    <GlobeIcon />
  </RoundedIcon>
);

const DomainsVerify = () => {
  let { orgId, domainRecord } = useParams();

  const { data: domains = [], isLoading } = useGetOrganizationDomainsQuery({
    realm: apiRealm,
    orgId: orgId!,
  });

  const domain = domains.find((domain) => domain.record_value === domainRecord);

  return (
    <div className="space-y-10 md:py-20">
      <div>
        <SectionHeader
          title="Verify domain"
          description="Use the following details to verify your domain."
          icon={addIcon}
        />
      </div>
      <div>
        <CopyBlock
          label="Create a TXT record in your DNS configuration for the following hostname"
          value={domain?.record_key}
        />
        <CopyBlock
          label="Use this code for the value of the TXT record"
          value={domain?.record_value}
        />
        <Button isBlackButton={true}>Verify</Button>
      </div>
    </div>
  );
};

export default DomainsVerify;
