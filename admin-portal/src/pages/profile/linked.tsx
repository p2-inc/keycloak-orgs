import Button from "components/elements/forms/buttons/button";
import SectionHeader from "components/navs/section-header";
import { config } from "config";
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
import { useTranslation } from "react-i18next";

const LinkedProfile = () => {
  const { t } = useTranslation();
  const { features: featureFlags } = config.env;
  const { data: accounts = [], isLoading } = useGetLinkedAccountsQuery({
    realm: config.env.realm,
  });
  const [deleteAccount] = useDeleteLinkedProviderMutation();
  const [buildLinkState, setBuildLinkState] = useState<
    typeof skipToken | BuildLinkingUriApiArg
  >(skipToken);
  const { data: buildLinker } = useBuildLinkingUriQuery(buildLinkState);

  const unlinkAccount = (account: LinkedAccountRepresentation): void => {
    deleteAccount({
      realm: config.env.realm,
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
      realm: config.env.realm,
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
        <label className="inline-block items-center space-x-2 rounded border border-p2primary-700/30 bg-p2primary-700/10 px-3 py-1 text-xs font-medium text-p2primary-700">
          {t("socialLogin")}
        </label>
      );
    }
    return (
      <label className="inline-block items-center space-x-2 rounded border border-green-700/30 bg-green-700/10 px-3 py-1 text-xs font-medium text-green-700">
        {t("systemDefined")}
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
    { key: "providerAlias", data: t("provider") },
    { key: "displayName", data: t("name") },
    { key: "label", data: t("label") },
    { key: "username", data: t("username") },
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
          {t("unlinkAccount")}
        </Button>
      ),
    }));

  const unlinkedColumns: TableColumns = [
    { key: "icon", data: "" },
    { key: "providerAlias", data: t("provider") },
    { key: "displayName", data: t("name") },
    { key: "label", data: t("label") },
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
          {t("linkAccount")}
        </Button>
      ),
    }));

  return (
    <>
      {featureFlags.linkedAccountsEnabled && (
        <div>
          <div className="mb-12">
            <SectionHeader
              title={t("linkedAccounts")}
              description={t("manageLoginsThroughThirdPartyAccounts")}
            />
          </div>
          <div className="space-y-8">
            <div className="space-y-4">
              <SectionHeader
                title={t("linkedLoginProviders")}
                variant="medium"
              />
              <Table
                columns={linkedColumns}
                rows={linkedRows}
                isLoading={isLoading}
                emptyState={t("linkedEmpty")}
              />
            </div>
            <div className="space-y-4">
              <SectionHeader
                title={t("unlinkedLoginProviders")}
                variant="medium"
              />
              <Table
                columns={unlinkedColumns}
                rows={unlinkedRows}
                isLoading={isLoading}
                emptyState={t("unlinkedEmpty")}
              />
            </div>
          </div>
        </div>
      )}
    </>
  );
};

export default LinkedProfile;
