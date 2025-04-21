import Button from "components/elements/forms/buttons/button";
import Table, {
  TableColumns,
  TableRows,
} from "components/elements/table/table";
import { PlusIcon } from "components/icons";
import SectionHeader from "components/navs/section-header";
import { Link, useParams } from "react-router-dom";
import { config } from "config";
import {
  OrganizationDomainRepresentation,
  useGetOrganizationByIdQuery,
  useGetOrganizationDomainsQuery,
  useUpdateOrganizationMutation,
  useVerifyDomainMutation,
} from "store/apis/orgs";
import { SettingsProps } from ".";
import { useTranslation } from "react-i18next";
import { useState } from "react";
import ConfirmationModal from "components/elements/confirmation-modal";
import P2Toast from "components/utils/toast";
import CopyInline from "components/elements/organizations/copy-inline";
import { SpinnerIcon } from "components/icons/spinner";

const { realm } = config.env;

const SettingsDomain = ({ hasManageOrganizationRole }: SettingsProps) => {
  const { t } = useTranslation();
  let { orgId } = useParams();
  const [showRemoveConfirmModal, setShowRemoveConfirmModal] =
    useState<OrganizationDomainRepresentation | null>();
  const {
    data: domains = [],
    isLoading,
    refetch: refetchDomains,
  } = useGetOrganizationDomainsQuery({
    realm: realm,
    orgId: orgId!,
  });
  const { data: org } = useGetOrganizationByIdQuery({
    realm,
    orgId: orgId!,
  });
  const [updateDomain] = useUpdateOrganizationMutation();

  const [verifyDomain, { isLoading: isVerifyDomainLoading }] =
    useVerifyDomainMutation();

  const checkVerification = async (domain) => {
    await verifyDomain({
      domainName: domain,
      orgId: orgId!,
      realm,
    })
      .unwrap()
      .then((r) => {
        //@ts-ignore
        if (r.verified) {
          P2Toast({
            success: true,
            title: t("domainVerified", [domain]),
          });
        } else {
          P2Toast({
            error: true,
            title: t("domainVerificationFailed", [domain]),
          });
        }
      })
      .catch((e) => {
        return P2Toast({
          error: true,
          title: t("domainVerificationError", [domain, e.data.error]),
        });
      });
  };

  const columns: TableColumns = [
    { key: "domain_name", data: t("domainName") },
    { key: "verifiedC", data: t("validated") },
    { key: "action", data: "", columnClasses: "flex justify-end" },
  ];

  const removeDomain = async (domain: OrganizationDomainRepresentation) => {
    const updateOrg = {
      ...org,
      domains: org?.domains?.filter((d) => d !== domain),
    };
    await updateDomain({
      organizationRepresentation: updateOrg,
      realm,
      orgId: orgId!,
    })
      .unwrap()
      .then((r) => {
        P2Toast({
          success: true,
          title: t("removeDomainSuccess", [domain]),
        });
        refetchDomains();
      })
      .catch((e) => {
        P2Toast({
          error: true,
          title: t("removeDomainError", [domain, e.data.error]),
        });
      })
      .finally(() => setShowRemoveConfirmModal(null));
  };

  const rows: TableRows = domains
    .filter((domain) => domain.domain_name !== "")
    .map((domain) => {
      let domainActions = <></>;
      const verifyDomainBtn = (
        <Button
          onClick={() => checkVerification(domain.domain_name as string)}
          disabled={isVerifyDomainLoading}
        >
          {isVerifyDomainLoading && <SpinnerIcon className="mr-2" />}
          {t("verify")}
        </Button>
      );
      const removeDomainBtn = (
        <Button
          onClick={() => setShowRemoveConfirmModal(domain)}
          className="ml-2"
        >
          {t("removeDomain")}
        </Button>
      );

      if (hasManageOrganizationRole) {
        domainActions = (
          <>
            {!domain.verified && <>{verifyDomainBtn}</>}
            {removeDomainBtn}
          </>
        );
      }

      return {
        ...domain,
        verifiedC: domain.verified ? (
          <div className="text-green-600">{t("verified")}</div>
        ) : (
          <div>
            <span className="mr-2 text-orange-600">
              {t("verificationPending")}
            </span>
            <div className="mt-4">
              <CopyInline
                labelNumber={1}
                label={t(
                  "createATxtRecordInYourDnsConfigurationForTheFollowingHostname"
                )}
                value={`${domain.record_key}=${domain.record_value}`}
              />
            </div>
          </div>
        ),
        action: domainActions,
      };
    });

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
                className="-ml-1 mr-2 h-auto w-4 fill-current"
              />
              {t("addNewDomain")}
            </Button>
          </Link>
        )}
      </div>
      <div>
        <Table columns={columns} rows={rows} isLoading={isLoading} />
      </div>
      {showRemoveConfirmModal && (
        <ConfirmationModal
          open={!!showRemoveConfirmModal}
          close={() => {
            setShowRemoveConfirmModal(null);
          }}
          modalTitle={t("removeDomainTitle", [
            showRemoveConfirmModal.domain_name,
          ])}
          modalMessage={t("removeDomainQuestion")}
          onContinue={() =>
            removeDomain(
              showRemoveConfirmModal as OrganizationDomainRepresentation
            )
          }
        />
      )}
    </div>
  );
};

export default SettingsDomain;
