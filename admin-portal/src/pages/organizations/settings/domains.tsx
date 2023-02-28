import Button from "components/elements/forms/buttons/button";
import Table, { TableColumns, TableRows } from "components/elements/table/table";
import { PlusIcon } from "components/icons";
import SectionHeader from "components/navs/section-header";

const columns: TableColumns = [
  { key: "domainName", data: "Domain name" },
  { key: "validated", data: "Validated" },
];
const rows: TableRows = [].map((item) => ({
  
}));

const SettingsDomain = () => {
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
      <div>
        <Table columns={columns} rows={rows} />
      </div>
    </div>
  );
};

export default SettingsDomain;
