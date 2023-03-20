import Button from "components/elements/forms/buttons/button";
import CopyBlock from "components/elements/organizations/copy-block";
import RoundedIcon from "components/elements/rounded-icon";
import { GlobeIcon } from "components/icons";
import SectionHeader from "components/navs/section-header";
import P2Toast from "components/utils/toast";
import { Link, useNavigate, useParams } from "react-router-dom";
import { config } from "config";
import {
  useGetOrganizationDomainsQuery,
  useVerifyDomainMutation,
} from "store/apis/orgs";

const addIcon = (
  <RoundedIcon className="my-4">
    <GlobeIcon />
  </RoundedIcon>
);

const DomainsVerify = () => {
  let { orgId, domainRecord } = useParams();
  const navigate = useNavigate();

  const { data: domains = [] } = useGetOrganizationDomainsQuery({
    realm: config.env.realm,
    orgId: orgId!,
  });

  const domain = domains.find((domain) => domain.record_value === domainRecord);

  const [verifyDomain, { isLoading: isVerifyDomainLoading }] =
    useVerifyDomainMutation();

  const checkVerification = async () => {
    if (domain && domain.domain_name) {
      const resp = await verifyDomain({
        domainName: domain.domain_name,
        orgId: orgId!,
        realm: config.env.realm,
      });

      //@ts-ignore
      if (resp.error) {
        return P2Toast({
          error: true,
          //@ts-ignore
          title: `${domain.domain_name} failed to verify. ${resp.error?.data?.error}`,
        });
      }

      //@ts-ignore
      if (resp.data) {
        //@ts-ignore
        if (resp.data.verified) {
          P2Toast({
            success: true,
            title: `${domain.domain_name} has been verified.`,
          });
          navigate(`/organizations/${orgId}/settings`);
        } else {
          P2Toast({
            error: true,
            title: `${domain.domain_name} failed to verify. Please check DNS records and try again.`,
          });
        }
      }
    }
  };

  return (
    <div className="space-y-10 md:py-20">
      <div>
        <SectionHeader
          title="Verify domain"
          description="Use the following details to verify your domain."
          icon={addIcon}
          rightContent={
            <Link
              to={`/organizations/${orgId}/settings`}
              className="inline-block rounded-lg px-4 py-2 font-medium opacity-60 transition hover:bg-gray-100 hover:opacity-100"
            >
              Back to Settings
            </Link>
          }
        />
      </div>
      <div className="space-y-8">
        <CopyBlock
          labelNumber={1}
          label="Create a TXT record in your DNS configuration for the following hostname"
          value={domain?.record_key}
        />
        <CopyBlock
          labelNumber={2}
          label="Use this code for the value of the TXT record"
          value={domain?.record_value}
        />
        <Button
          isBlackButton={true}
          onClick={checkVerification}
          disabled={isVerifyDomainLoading}
        >
          Verify
        </Button>
      </div>
    </div>
  );
};

export default DomainsVerify;
