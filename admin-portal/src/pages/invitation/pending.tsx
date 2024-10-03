import { config } from "config";
import Breadcrumbs from "components/navs/breadcrumbs";
import TopHeader from "components/navs/top-header";
import Button from "components/elements/forms/buttons/button";
import useOrgDisplayName from "components/utils/org-display-name";
import { Link, useParams } from "react-router-dom";
import {
  InvitationRepresentation,
  useGetOrganizationByIdQuery,
  useGetOrganizationInvitationCountQuery,
  useGetOrganizationInvitationsQuery,
  useRemoveOrganizationInvitationMutation,
  useResendOrganizationInvitationMutation,
} from "store/apis/orgs";
import { useTranslation } from "react-i18next";
import MainContentArea from "components/layouts/main-content-area";
import PrimaryContentArea from "components/layouts/primary-content-area";
import SectionHeader from "components/navs/section-header";
import HeaderLayout from "components/navs/components/header-layout";
import RoundBadge from "components/elements/badges/round-badge";
import Table from "components/elements/table/table";
import useUser from "components/utils/useUser";
import NotAuthorized from "pages/not-authorized";
import { Plus } from "lucide-react";
import TimeUtil from "services/time-util";
import P2Toast from "components/utils/toast";
import ConfirmationModal from "components/elements/confirmation-modal";
import { useState } from "react";

const time = (time: string | undefined): string => {
  if (time === undefined) return "unknown";
  return TimeUtil.formatISOShort(time);
};

const PendingInvitations = () => {
  const { t } = useTranslation();
  let { orgId } = useParams();
  const { realm } = config.env;
  const { data: org } = useGetOrganizationByIdQuery({
    orgId: orgId!,
    realm,
  });
  const [resendInvitation] = useResendOrganizationInvitationMutation();
  const [removeInvitation] = useRemoveOrganizationInvitationMutation();
  const {
    isFetchingUserOrgs,
    hasManageInvitationsRole: hasManageInvitationsRoleCheck,
    hasViewInvitationsRole: hasViewInvitationsRoleCheck,
  } = useUser();
  const { orgName } = useOrgDisplayName(org);

  const hasManageInvitationsRole = hasManageInvitationsRoleCheck(orgId);
  const hasViewInvitationsRole = hasViewInvitationsRoleCheck(orgId);

  const [showRemoveConfirmModal, setShowRemoveConfirmModal] =
    useState<InvitationRepresentation | null>(null);

  const readyForDataPull =
    isFetchingUserOrgs === false && hasViewInvitationsRole === true;

  const { data: inviteCount = 0 } = useGetOrganizationInvitationCountQuery(
    {
      orgId: orgId!,
      realm,
    },
    {
      skip: !readyForDataPull,
    }
  );
  const { data: invites = [], isLoading } = useGetOrganizationInvitationsQuery(
    {
      orgId: orgId!,
      realm,
    },
    {
      skip: !readyForDataPull,
    }
  );

  const resendInvitationHandler = async (
    invitationId: string,
    email: string
  ) => {
    await resendInvitation({
      orgId: orgId!,
      realm,
      invitationId,
    })
      .unwrap()
      .then(() => {
        P2Toast({
          success: true,
          title: t("invitation-toast-resend-success", { email }),
        });
      })
      .catch((e) => {
        P2Toast({
          error: true,
          title: t("invitation-toast-resend-error", { email }),
        });
      });
  };

  const removeInvitationHandler = async (
    invitationId: string,
    email: string
  ) => {
    await removeInvitation({
      orgId: orgId!,
      realm,
      invitationId,
    })
      .unwrap()
      .then(() => {
        P2Toast({
          success: true,
          title: t("invitation-toast-remove-success", { email }),
        });
      })
      .catch((e) => {
        P2Toast({
          error: true,
          title: t("invitation-toast-remove-error", { email }),
        });
      });
  };

  if (!isFetchingUserOrgs && !hasViewInvitationsRole) {
    return <NotAuthorized />;
  }

  return (
    <>
      <TopHeader
        header={t("pendingInvitations")}
        collapseOnMobile={true}
        leftAreaItems={
          <Breadcrumbs
            items={[
              { title: t("organizations"), link: "/organizations" },
              { title: orgName, link: `/organizations/${orgId}/details` },
            ]}
          />
        }
        rightAreaItems={<></>}
      />

      {/* Invitations list */}
      <MainContentArea className="flex h-full min-w-0 flex-1 flex-col">
        <HeaderLayout
          leftAreaItems={
            <>
              <SectionHeader title={t("invitations")} variant="small" />
              <div className="ml-2">
                <RoundBadge>{inviteCount}</RoundBadge>
              </div>
            </>
          }
          rightAreaItems={
            <>
              {hasManageInvitationsRole ? (
                <Link to={`/organizations/${orgId}/invitation/new`}>
                  <Button disabled={!hasManageInvitationsRole}>
                    <Plus className="mr-2 w-5" />
                    {t("inviteNewMembers")}
                  </Button>
                </Link>
              ) : (
                <Button disabled={!hasManageInvitationsRole}>
                  <Plus className="mr-2 w-5" />
                  {t("inviteNewMembers")}
                </Button>
              )}
            </>
          }
          className="pt-0 md:pt-0"
        />
        <PrimaryContentArea>
          <Table
            isLoading={isLoading}
            columns={[
              {
                key: "email",
                data: t("email"),
              },
              {
                key: "createdAt",
                data: t("createdAt"),
              },
              {
                key: "resend",
                data: "",
                columnClasses: "text-right",
              },
            ]}
            rows={invites.map((invite) => ({
              email: invite.email,
              createdAt: time(invite.createdAt),
              resend: (
                <>
                  <Button
                    isCompact
                    isBlackButton
                    disabled={!hasManageInvitationsRole}
                    onClick={() => {
                      resendInvitationHandler(invite.id!, invite.email!);
                    }}
                  >
                    {t("resend")}
                  </Button>
                  <Button
                    isCompact
                    disabled={!hasManageInvitationsRole}
                    onClick={() => setShowRemoveConfirmModal(invite)}
                  >
                    {t("remove")}
                  </Button>
                </>
              ),
            }))}
          />
        </PrimaryContentArea>
      </MainContentArea>
      {showRemoveConfirmModal && (
        <ConfirmationModal
          open={!!showRemoveConfirmModal}
          close={() => {
            setShowRemoveConfirmModal(null);
          }}
          modalTitle={t("invitation-remove-confirm-title")}
          modalMessage={t("invitation-remove-confirm", {
            email: showRemoveConfirmModal.email,
          })}
          onContinue={() =>
            removeInvitationHandler(
              showRemoveConfirmModal.id!,
              showRemoveConfirmModal.email!
            )
          }
        />
      )}
    </>
  );
};

export default PendingInvitations;
