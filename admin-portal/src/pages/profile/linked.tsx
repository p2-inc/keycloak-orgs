import Button from "components/elements/forms/buttons/button";
import SectionHeader from "components/navs/section-header";
import { apiRealm } from "store/apis/helpers";
import {
  useBuildLinkingUriQuery,
  useGetLinkedAccountsQuery,
  useDeleteLinkedProviderMutation,
  LinkedAccountRepresentation,
  BuildLinkingUriApiArg,
} from "store/apis/profile";
import { skipToken } from "@reduxjs/toolkit/dist/query";
import { useState, useEffect } from "react";
import Table, {
  TableColumns,
  TableRows,
} from "components/elements/table/table";
import * as icons from "components/icons/providers";
import P2Toast from "components/utils/toast";

const LinkedProfile = () => {
  const { data: accounts = [], isLoading } = useGetLinkedAccountsQuery({
    realm: apiRealm,
  });
  const [deleteAccount] = useDeleteLinkedProviderMutation();
  const [buildLinkState, setBuildLinkState] = useState<
    typeof skipToken | BuildLinkingUriApiArg
  >(skipToken);
  const { data: buildLinker } = useBuildLinkingUriQuery(buildLinkState);

  const unlinkAccount = (account: LinkedAccountRepresentation): void => {
    deleteAccount({
      realm: apiRealm,
      providerId: account.providerName!,
    })
      .then(() => {
        P2Toast({
          success: true,
          title: `${account.providerName} unlinked.`,
        });
      })
      .catch((e) => {
        P2Toast({
          error: true,
          title: `Error during unlinking from ${account.providerName} . Please try again.`,
        });
      });
  };

  const linkAccount = (account: LinkedAccountRepresentation) => {
    setBuildLinkState({
      realm: apiRealm,
      providerId: account.providerAlias ?? "",
      redirectUri: window.location.href,
    });
  };

  useEffect(() => {
    if (buildLinker && buildLinker.accountLinkUri) {
      console.log(buildLinker.accountLinkUri);
      //todo change the client_id in the requests params
      //https://www.keycloak.org/docs/latest/server_development/index.html#client-initiated-account-linking
      window.location.href = buildLinker.accountLinkUri;
    }
  }, [buildLinker]);

  const label = (account: LinkedAccountRepresentation): React.ReactNode => {
    if (account.social) {
      return (
        <label className="inline-block items-center space-x-2 rounded border border-p2blue-700/30 bg-p2blue-700/10 px-3 py-1 text-xs font-medium text-p2blue-700">
          Social login
        </label>
      );
    }
    return (
      <label className="inline-block items-center space-x-2 rounded border border-green-700/30 bg-green-700/10 px-3 py-1 text-xs font-medium text-green-700">
        System defined
      </label>
    );
  };

  const icon = (account: LinkedAccountRepresentation): React.ReactNode => {
    const k = Object.keys(icons);
    const f = k.find(
      (t) => t.toLowerCase() === account.providerAlias?.toLowerCase()
    );
    const LucideIcon = icons[f || "Key"];
    return <LucideIcon />;
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
          <Table
            columns={linkedColumns}
            rows={linkedRows}
            isLoading={isLoading}
          />
        </div>
        <div className="space-y-4">
          <SectionHeader title="Unlinked login providers" variant="medium" />
          <Table
            columns={unlinkedColumns}
            rows={unlinkedRows}
            isLoading={isLoading}
          />
        </div>
      </div>
    </div>
  );
};

export default LinkedProfile;
