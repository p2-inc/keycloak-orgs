import Button from "components/elements/forms/buttons/button";
import Table, {
  TableColumns,
  TableRows,
} from "components/elements/table/table";
import { PlusIcon } from "components/icons";
import SectionHeader from "components/navs/section-header";
import { Link, useParams } from "react-router-dom";
import { config } from "config";
import { useGetOrganizationDomainsQuery } from "store/apis/orgs";
import { SettingsProps } from ".";
import { useTranslation } from "react-i18next";

const { realm } = config.env;

const SettingsDomain = ({ hasManageOrganizationRole }: SettingsProps) => {
  const { t } = useTranslation();
  let { orgId } = useParams();
  const { data: domains = [], isLoading } = useGetOrganizationDomainsQuery({
    realm: realm,
    orgId: orgId!,
  });

  const columns: TableColumns = [
    { key: "domain_name", data: t("domainName") },
    { key: "verifiedC", data: t("validated") },
    { key: "action", data: "", columnClasses: "flex justify-end" },
  ];

  const rows: TableRows = domains.map((domain) => ({
    ...domain,
    verifiedC: domain.verified ? (
      <div className="text-green-600">{t("verified")}</div>
    ) : (
      <div>
        <span className="mr-2 text-orange-600">{t("verificationPending")}</span>
      </div>
    ),
    action:
      !hasManageOrganizationRole || domain.verified ? (
        <></>
      ) : (
        <div>
          <Link
            to={`/organizations/${orgId}/domains/verify/${domain.record_value}`}
          >
            <Button>{t("verifyDomain")}</Button>
          </Link>
        </div>
      ),
  }));

  return (
    <div className="space-y-4">
      <div>
        <SectionHeader
          title={t("domains")}
          description={t("viewLinkedDomainsAndVerifyDnsEntries")}
        />
      </div>
      <div>
        {hasManageOrganizationRole && (
          <Link to={`/organizations/${orgId}/domains/add`}>
            <Button isBlackButton={true}>
              <PlusIcon
                aria-hidden="true"
                className="-ml-1 mr-2 h-5 w-5 fill-current"
              />
              {t("addNewDomain")}
            </Button>
          </Link>
        )}
      </div>
      <div>
        <Table columns={columns} rows={rows} isLoading={isLoading} />
      </div>
    </div>
  );
};

export default SettingsDomain;
