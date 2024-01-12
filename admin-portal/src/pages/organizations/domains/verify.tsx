import Button from "components/elements/forms/buttons/button";
import CopyBlock from "components/elements/organizations/copy-block";
import RoundedIcon from "components/elements/rounded-icon";
import SectionHeader from "components/navs/section-header";
import P2Toast from "components/utils/toast";
import { Link, useNavigate, useParams } from "react-router-dom";
import { config } from "config";
import {
  useGetOrganizationDomainsQuery,
  useVerifyDomainMutation,
} from "store/apis/orgs";
import { Globe } from "lucide-react";
import useUser from "components/utils/useUser";
import { t } from "i18next";

const addIcon = (
  <RoundedIcon className="my-4">
    <Globe className="h-5 w-5" />
  </RoundedIcon>
);

const DomainsVerify = () => {
  let { orgId, domainRecord } = useParams();
  const { realm } = config.env;
  const navigate = useNavigate();
  const { hasManageOrganizationRole: hasManageOrganizationRoleCheck } =
    useUser();

  const { data: domains = [] } = useGetOrganizationDomainsQuery({
    realm,
    orgId: orgId!,
  });
  const hasManageOrganizationRole = hasManageOrganizationRoleCheck(orgId);

  const domain = domains.find((domain) => domain.record_value === domainRecord);

  const [verifyDomain, { isLoading: isVerifyDomainLoading }] =
    useVerifyDomainMutation();

  const checkVerification = async () => {
    if (domain && domain.domain_name) {
      await verifyDomain({
        domainName: domain.domain_name,
        orgId: orgId!,
        realm,
      })
        .unwrap()
        .then((r) => {
          //@ts-ignore
          if (r.verified) {
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
        })
        .catch((e) => {
          return P2Toast({
            error: true,
            title: `${domain.domain_name} failed to verify. ${e.data.error}`,
          });
        });
    }
  };

  return (
    <div className="space-y-10 md:py-20">
      <div>
        <SectionHeader
          title={`${t("domains-verify")} ${domain?.domain_name}.`}
          description={t("useTheFollowingDetailsToVerifyYourDomain")}
          icon={addIcon}
          rightContent={
            <Link
              to={`/organizations/${orgId}/settings`}
              className="inline-block rounded-lg px-4 py-2 font-medium opacity-60 transition hover:bg-gray-100 hover:opacity-100 dark:text-zinc-200 dark:hover:bg-p2dark-1000"
            >
              {t("backToSettings")}
            </Link>
          }
        />
      </div>
      <div className="space-y-8">
        <CopyBlock
          labelNumber={1}
          label={t("createATxtRecordInYourDnsConfigurationForTheFollowingHostname")}
          value={domain?.record_key}
        />
        <CopyBlock
          labelNumber={2}
          label={t("useThisCodeForTheValueOfTheTxtRecord")}
          value={domain?.record_value}
        />
        <Button
          isBlackButton={true}
          onClick={checkVerification}
          disabled={isVerifyDomainLoading || !hasManageOrganizationRole}
        >
          {t("verify")}
        </Button>
      </div>
    </div>
  );
};

export default DomainsVerify;
