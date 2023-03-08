import Button from "components/elements/forms/buttons/button";
import SectionHeader from "components/navs/section-header";
import { apiRealm } from "store/apis/helpers";
import { keycloakService } from "keycloak";
import {
  useGetLinkedAccountsQuery,
  useDeleteLinkedProviderMutation,
  LinkedAccountRepresentation,
} from "store/apis/profile";
import Table, {
  TableColumns,
  TableRows,
} from "components/elements/table/table";

const LinkedProfile = () => {
  const { data: accounts = [] } = useGetLinkedAccountsQuery({
    realm: apiRealm,
  });
  const [deleteAccount, { isSuccess }] = useDeleteLinkedProviderMutation();

  const unlinkAccount = (account: LinkedAccountRepresentation): void => {
    deleteAccount({
      realm: apiRealm,
      providerId: account.providerName!,
    }).then(() => {
      //refresh accoutns? automatic?
      //ContentAlert.
    });
  };

  const linkAccount = (account: LinkedAccountRepresentation) => {
    const url = '/linked-accounts/' + account.providerName;
 
    /*
    const redirectUri: string = createRedirect(this.props.location.pathname);

    this.context!.doGet<{accountLinkUri: string}>(url, { params: {providerId: account.providerName, redirectUri}})
        .then((response: HttpResponse<{accountLinkUri: string}>) => {
            console.log({response});
            window.location.href = response.data!.accountLinkUri;
        });
    */
  };
  
  const label = (account: LinkedAccountRepresentation): React.ReactNode => {
    if (account.social) {
      return (<><label className="block text-sm font-medium text-blue-700">Social login</label></>);
    }
    return (<><label className="block text-sm font-medium text-green-700">System defined</label></>);
  };

  const linkedColumns: TableColumns = [
    { key: "providerAlias", data: "Provider" },
    { key: "displayName", data: "Name" },
    { key: "label", data: "Label" },
    { key: "username", data: "Username" },
    { key: "action", data: "" },
  ];
  
  const linkedRows: TableRows = accounts.filter((account) => account.connected).map((account) => ({
    providerAlias: account.providerAlias,
    displayName: account.displayName,
    label: label(account),
    username: account.linkedUsername,
    action: (<Button
      isBlackButton
      className="inline-flex w-full justify-center sm:ml-3 sm:w-auto"
      onClick={()=>unlinkAccount(account)}
    >Unlink account</Button>),
  }));


  const unlinkedColumns: TableColumns = [
    { key: "providerAlias", data: "Provider" },
    { key: "displayName", data: "Name" },
    { key: "label", data: "Label" },
    { key: "action", data: "" },
  ];
  
  const unlinkedRows: TableRows = accounts.filter((account) => !account.connected).map((account) => ({
    providerAlias: account.providerAlias,
    displayName: account.displayName,
    label: label(account),
    action: (<Button
      isBlackButton
      className="inline-flex w-full justify-center sm:ml-3 sm:w-auto"
      onClick={()=>linkAccount(account)}
    >Link account</Button>),
  }));

  return (
    <div>
      <div className="mb-12">
        <SectionHeader
          title="Linked accounts"
          description="Manage logins through third-party accounts."
        />
      </div>
      <div className="mb-12">
        <SectionHeader
          title="Linked login providers"
          variant="medium"
        />
      </div>
      <div className="px-4 py-4 md:px-10 md:py-6">
        <Table columns={linkedColumns} rows={linkedRows} />
      </div>
      <div className="mb-12">
        <SectionHeader
          title="Unlinked login providers"
          variant="medium"
        />
      </div>
      <div className="px-4 py-4 md:px-10 md:py-6">
        <Table columns={unlinkedColumns} rows={unlinkedRows} />
      </div>
    </div>
  );
};

export default LinkedProfile;
