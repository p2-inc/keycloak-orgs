import Button from "components/elements/forms/buttons/button";
import Table, {
  TableColumns,
  TableRows,
} from "components/elements/table/table";
import { PlusIcon } from "components/icons";
import SectionHeader from "components/navs/section-header";
import { Link, useNavigate, useParams } from "react-router-dom";
import { apiRealm } from "store/apis/helpers";
import { useGetOrganizationDomainsQuery } from "store/apis/orgs";

const columns: TableColumns = [
  { key: "domain_name", data: "Domain name" },
  { key: "verifiedC", data: "Validated" },
  { key: "action", data: "", columnClasses: "flex justify-end" },
];

const SettingsDomain = () => {
  let { orgId } = useParams();
  const navigate = useNavigate();
  const { data: domains = [], isLoading } = useGetOrganizationDomainsQuery({
    realm: apiRealm,
    orgId: orgId!,
  });

  const rows: TableRows = domains.map((domain) => ({
    ...domain,
    verifiedC: domain.verified ? (
      <div className="text-green-600">Verified</div>
    ) : (
      <div>
        <span className="mr-2 text-orange-600">Verification pending</span>
      </div>
    ),
    action: domain.verified ? (
      <></>
    ) : (
      <div>
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
          title="Domains"
          description="View linked domains and verify DNS entries."
        />
      </div>
      <div>
        <Link to={`/organizations/${orgId}/domains/add`}>
          <Button isBlackButton={true}>
            <PlusIcon
              aria-hidden="true"
              className="-ml-1 mr-2 h-5 w-5 fill-current"
            />
            Add new domain
          </Button>
        </Link>
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
