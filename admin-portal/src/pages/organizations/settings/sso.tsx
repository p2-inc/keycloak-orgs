import Button from "@/components/elements/forms/buttons/button";
import cs from "classnames";
import SectionHeader from "@/components/navs/section-header";
import OpenSSOLink from "@/components/utils/ssoLink";
import { P2Params } from "index";
import { useParams } from "react-router-dom";
import { SettingsProps } from ".";
import { useTranslation } from "react-i18next";
import { config } from "@/config";
import {
  IdentityProviderRepresentation,
  useDeleteIdpMutation,
  useGetIdpsQuery,
  useUpdateIdpMutation,
} from "@/store/apis/orgs";
import Table, {
  TableColumns,
  TableRows,
} from "@/components/elements/table/table";
import { FindIdpIcon } from "@/pages/profile/linked";
import SquareBadge from "@/components/elements/badges/square-badge";
import { useState } from "react";
import ConfirmationModal from "@/components/elements/confirmation-modal";
import P2Toast from "@/components/utils/toast";
import { TrashIcon } from "lucide-react";

const SettingsSSO = ({ hasManageIDPRole }: SettingsProps) => {
  const { realm } = config.env;
  const { t } = useTranslation();
  const { orgId } = useParams<keyof P2Params>() as P2Params;
  const { data: idps = [], isLoading } = useGetIdpsQuery({
    orgId: orgId!,
    realm,
  });
  const [confirmEnableSSOProvider, setConfirmEnableSSOProvider] =
    useState<IdentityProviderRepresentation | null>();
  const [confirmRemoveSSOProvider, setConfirmRemoveSSOProvider] =
    useState<IdentityProviderRepresentation | null>();
  const [updateIdP, { isLoading: isUpdatingIdP }] = useUpdateIdpMutation();
  const [deleteIdp, { isLoading: isDeletingIdP }] = useDeleteIdpMutation();

  const idpColumns = [
    { key: "enabled", data: t("status") },
    { key: "alias", data: t("provider") },
    { key: "displayName", data: t("name") },
    { key: "authorizationUrl", data: t("authUrl") },
    { key: "actions", data: "" },
  ] as TableColumns;

  const toggleSSOProvider = (idp: IdentityProviderRepresentation) => {
    updateIdP({
      orgId: orgId!,
      realm,
      alias: idp.alias as string,
      identityProviderRepresentation: {
        ...idp,
        enabled: true,
      },
    })
      .then(() => {
        setConfirmEnableSSOProvider(null);
        P2Toast({
          title: t("ssoProviderEnabled", [idp.displayName || idp.alias]),
          success: true,
        });
      })
      .catch((err) => {
        console.error("Error enabling SSO provider:", err);
        P2Toast({
          title: t("ssoProviderEnableError", [idp.displayName || idp.alias]),
          error: true,
        });
      });
  };

  const handleDeleteIdP = (idp: IdentityProviderRepresentation) => {
    deleteIdp({
      orgId: orgId!,
      realm,
      alias: idp.alias as string,
    })
      .then(() => {
        P2Toast({
          title: t("ssoProviderDeleted", [idp.displayName || idp.alias]),
          success: true,
        });
      })
      .catch((err) => {
        P2Toast({
          title: t("ssoProviderDeleteError", [
            idp.displayName || idp.alias,
            err.message,
          ]),
          error: true,
        });
      });
  };

  const rows: TableRows = idps.map((idp) => {
    const actions = (
      <div className="flex items-center space-x-2">
        {!idp.enabled ? (
          <></>
        ) : (
          <Button
            isCompact
            isBlackButton
            onClick={() => setConfirmEnableSSOProvider(idp)}
            disabled={isUpdatingIdP}
          >
            {t("enable")}
          </Button>
        )}
        <Button
          isCompact
          onClick={() => setConfirmRemoveSSOProvider(idp)}
          disabled={isDeletingIdP}
        >
          <TrashIcon className="h-4 w-4" aria-hidden="true"></TrashIcon>
        </Button>
      </div>
    );

    return {
      enabled: (
        <div className="flex items-center space-x-2">
          {FindIdpIcon(idp)}{" "}
          <SquareBadge>
            <div className="flex items-center space-x-2">
              <span
                className="relative flex h-2 w-2"
                title={idp.enabled ? t("enabled") : t("disabled")}
              >
                <span
                  className={cs(
                    "absolute inline-flex h-full w-full rounded-full  opacity-75",
                    {
                      "animate-ping  bg-primary-700": idp.enabled,
                      "bg-gray-300": !idp.enabled,
                    }
                  )}
                ></span>
                <span
                  className={cs("relative inline-flex h-2 w-2 rounded-full ", {
                    "bg-primary-700": idp.enabled,
                    "bg-gray-300": !idp.enabled,
                  })}
                ></span>
              </span>
              <div>{idp.enabled ? t("enabled") : t("disabled")}</div>
            </div>
          </SquareBadge>
        </div>
      ),
      alias: idp.alias,
      displayName: idp.displayName || "--",
      authorizationUrl: idp.config?.authorizationUrl,
      actions: actions,
    };
  });

  return (
    <div className="space-y-4">
      <SectionHeader title={t("sso")} description={t("addAnSsoProvider")} />
      <div>
        <Button
          isBlackButton
          onClick={() => OpenSSOLink({ orgId })}
          disabled={!hasManageIDPRole}
        >
          {t("setupSso")}
        </Button>
      </div>
      <Table
        columns={idpColumns}
        rows={rows}
        isLoading={isLoading}
        emptyState={t("linkedEmpty")}
      ></Table>

      {confirmEnableSSOProvider && (
        <ConfirmationModal
          open={!!confirmEnableSSOProvider}
          close={() => {
            setConfirmEnableSSOProvider(null);
          }}
          modalTitle={t("toggleSSOProvider", [
            confirmEnableSSOProvider?.displayName ||
              confirmEnableSSOProvider?.alias,
          ])}
          modalMessage={t("toggleSSOQuestion")}
          onContinue={() => toggleSSOProvider(confirmEnableSSOProvider)}
        />
      )}
      {confirmRemoveSSOProvider && (
        <ConfirmationModal
          open={!!confirmRemoveSSOProvider}
          close={() => {
            setConfirmRemoveSSOProvider(null);
          }}
          modalTitle={t("deleteSSOProvider", [
            confirmEnableSSOProvider?.displayName ||
              confirmEnableSSOProvider?.alias,
          ])}
          modalMessage={t("deleteSSOProviderQuestion")}
          onContinue={() => handleDeleteIdP(confirmRemoveSSOProvider)}
        />
      )}
    </div>
  );
};

export default SettingsSSO;
