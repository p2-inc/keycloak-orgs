import Button from "components/elements/forms/buttons/button";
import Table, {
  TableColumns,
  TableRows,
} from "components/elements/table/table";
import { PlusIcon } from "components/icons";
import SectionHeader from "components/navs/section-header";
import { useNavigate, useParams } from "react-router-dom";
import { apiRealm } from "store/apis/helpers";
import { useGetOrganizationDomainsQuery } from "store/apis/orgs";

const columns: TableColumns = [
  { key: "domain_name", data: "Domain name" },
  { key: "verifiedC", data: "Validated" },
];

const SettingsDomain = () => {
  let { orgId } = useParams();
  const navigate = useNavigate();
  const { data: domains = [], isLoading } = useGetOrganizationDomainsQuery({
    realm: apiRealm,
    orgId: orgId!,
  });
  console.log("🚀 ~ file: domains.tsx:21 ~ SettingsDomain ~ domains:", domains);

  const rows: TableRows = domains.map((domain) => ({
    ...domain,
    verifiedC: domain.verified ? (
      <div className="text-green-600">Verified</div>
    ) : (
      <div>
        <span className="mr-2 text-orange-600">Verification pending</span>
        <Button
          onClick={() =>
            navigate(
              `/organizations/${orgId}/domains/verify/${domain.record_value}`
            )
          }
        >
          Verify domain
        </Button>
      </div>
    ),
  }));

  return (
    <div className="space-y-4">
      <div>
        <SectionHeader
          title="Domain Settings"
          description="One morning, when Gregor Samsa woke from troubled dreams, he found himself transformed in his bed into a horrible vermin."
        />
      </div>
      <div>
        <Button isBlackButton={true}>
          <PlusIcon
            aria-hidden="true"
            className="-ml-1 mr-2 h-5 w-5 fill-current"
          />
          Add new domain
        </Button>
      </div>
      {/* TODO: add loading state */}
      {!isLoading && (
        <div>
          <Table columns={columns} rows={rows} />
        </div>
      )}
    </div>
  );
};

export default SettingsDomain;
