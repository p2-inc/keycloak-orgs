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
import * as icons from "lucide-react";

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
    const url = "/linked-accounts/" + account.providerName;

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
      return (
        <>
          <label className="rounded border border-p2blue-700/30 bg-p2blue-700/10 px-3 py-1 text-xs font-medium text-p2blue-700 inline-block items-center space-x-2">
            Social login
          </label>
        </>
      );
    }
    return (
      <>
        <label className="rounded border border-green-700/30 bg-green-700/10 px-3 py-1 text-xs font-medium text-green-700 inline-block items-center space-x-2">
          System defined
        </label>
      </>
    );
  };

  const icon = (account: LinkedAccountRepresentation): React.ReactNode => {
    const k = Object.keys(icons);
    const f = k.find(t => t.toLowerCase() === account.providerAlias?.toLowerCase());
    const LucideIcon = icons[f || "Key"];
    return (
      <LucideIcon />
    );
  };

  const linkedColumns: TableColumns = [
    { key: "icon", data: "" },
    { key: "providerAlias", data: "Provider" },
    { key: "displayName", data: "Name" },
    { key: "label", data: "Label" },
    { key: "username", data: "Username" },
    { key: "action", data: "", columnClasses: "flex justify-end" },
  ];

  const linkedRows: TableRows = accounts
    .filter((account) => account.connected)
    .map((account) => ({
      icon: icon(account),
      providerAlias: account.providerAlias,
      displayName: account.displayName,
      label: label(account),
      username: account.linkedUsername,
      action: (
        <Button
          isBlackButton
          className="inline-flex w-full justify-center sm:ml-3 sm:w-auto"
          onClick={() => unlinkAccount(account)}
        >
          Unlink account
        </Button>
      ),
    }));

  const unlinkedColumns: TableColumns = [
    { key: "icon", data: "" },
    { key: "providerAlias", data: "Provider" },
    { key: "displayName", data: "Name" },
    { key: "label", data: "Label" },
    { key: "action", data: "", columnClasses: "flex justify-end" },
  ];

  const unlinkedRows: TableRows = accounts
    .filter((account) => !account.connected)
    .map((account) => ({
      icon: icon(account),
      providerAlias: account.providerAlias,
      displayName: account.displayName,
      label: label(account),
      action: (
        <Button
          isBlackButton
          className="inline-flex w-full justify-center sm:ml-3 sm:w-auto"
          onClick={() => linkAccount(account)}
        >
          Link account
        </Button>
      ),
    }));

  return (
    <div>
      <div className="mb-12">
        <SectionHeader
          title="Linked accounts"
          description="Manage logins through third-party accounts."
        />
      </div>
      <div className="space-y-8">
        <div className="space-y-4">
          <SectionHeader title="Linked login providers" variant="medium" />
          <Table columns={linkedColumns} rows={linkedRows} />
        </div>
        <div className="space-y-4">
          <SectionHeader title="Unlinked login providers" variant="medium" />
          <Table columns={unlinkedColumns} rows={unlinkedRows} />
        </div>
      </div>
    </div>
  );
};

export default LinkedProfile;
