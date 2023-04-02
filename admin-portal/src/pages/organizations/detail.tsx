import Button from "components/elements/forms/buttons/button";
import MainContentArea from "components/layouts/main-content-area";
import TopHeader from "components/navs/top-header";
import SectionHeader from "components/navs/section-header";
import { Link, useParams } from "react-router-dom";
import { config } from "config";
import {
  useGetIdpsQuery,
  useGetOrganizationByIdQuery,
  useGetOrganizationDomainsQuery,
  useGetOrganizationInvitationsQuery,
  useGetOrganizationMembershipsQuery,
} from "store/apis/orgs";
import RoundBadge from "components/elements/badges/round-badge";
import FormTextInputWithIcon from "components/elements/forms/inputs/text-input-with-icon";
import HeaderLayout from "components/navs/components/header-layout";
import { TableRows } from "components/elements/table/table";
import OrganizationActionCard, {
  OACTopRow,
} from "components/elements/organizations/action-card";
import PrimaryContentArea from "components/layouts/primary-content-area";
import Stat from "components/elements/cards/stat";
import RoundedIcon from "components/elements/rounded-icon";
import MemberRoles from "./components/member-roles";
import MembersActionMenu from "./components/member-action-menu";
import Breadcrumbs from "components/navs/breadcrumbs";
import OpenSSOLink from "components/utils/ssoLink";
import MembersTable from "components/elements/table/members-table";
import { Globe, Network, Plus, User } from "lucide-react";
import useUser from "components/utils/useUser";
import { useTranslation } from "react-i18next";

export default function OrganizationDetail() {
  const { t } = useTranslation();
  let { orgId } = useParams();
  const { features: featureFlags, realm } = config.env;
  const {
    hasManageInvitationsRole: hasManageInvitationsRoleCheck,
    hasManageOrganizationRole: hasManageOrganizationRoleCheck,
    hasManageIdentityProvidersRole,
    hasViewIdentityProvidersRole,
  } = useUser();
  const { data: org } = useGetOrganizationByIdQuery({
    orgId: orgId!,
    realm,
  });
  const { data: members = [], isLoading } = useGetOrganizationMembershipsQuery({
    orgId: orgId!,
    realm,
  });
  const { data: invites = [] } = useGetOrganizationInvitationsQuery({
    orgId: org?.id!,
    realm,
  });
  const { data: domains = [] } = useGetOrganizationDomainsQuery({
    orgId: org?.id!,
    realm,
  });
  const verifiedDomains =
    domains.length > 0 ? domains.filter((d) => d.verified).length : 0;
  const { data: idps = [] } = useGetIdpsQuery({
    orgId: org?.id!,
    realm,
  });

  const hasManageInvitationsRole = hasManageInvitationsRoleCheck(orgId);
  const hasManageOrganizationRole = hasManageOrganizationRoleCheck(orgId);
  const hasManageIDPRole = hasManageIdentityProvidersRole(orgId);
  const hasViewIDPRole = hasViewIdentityProvidersRole(orgId);
  // const [createPortalLink, { isSuccess }] = useCreatePortalLinkMutation();

  const filteredMembers = members.filter(
    (member) => !member.email?.startsWith("org-admin")
  );
  const totalMembers = filteredMembers.length;

  const rows: TableRows = filteredMembers.map((member) => ({
    email: member.email,
    name: `${member.firstName || ""} ${member.lastName || ""}`.trim(),
    roles: <MemberRoles member={member} orgId={orgId!} realm={realm} />,
    action: <MembersActionMenu member={member} orgId={orgId!} realm={realm} />,
  }));

  return (
    <>
      <TopHeader
        header={`${org?.displayName || ""}`.trim()}
        collapseOnMobile={true}
        leftAreaItems={
          <Breadcrumbs
            items={[{ title: "Organizations", link: "/organizations" }]}
          />
        }
        rightAreaItems={
          <>
            {hasManageOrganizationRole && (
              <Link to={`/organizations/${org?.id}/settings`}>
                <Button>{t("settings")}</Button>
              </Link>
            )}
          </>
        }
      />
      {/* Action Cards */}
      <MainContentArea>
        <PrimaryContentArea>
          <div className="grid grid-cols-1 gap-6 sm:grid-cols-1 md:grid-cols-2 lg:grid-cols-3">
            {/* Invite new members */}
            {featureFlags.orgInvitationsEnabled && (
              <OrganizationActionCard>
                <OACTopRow>
                  <RoundedIcon>
                    <User className="h-5 w-5" />
                  </RoundedIcon>
                  <Stat label="members" value={totalMembers}></Stat>
                  <Stat label="pending" value={invites.length}></Stat>
                </OACTopRow>
                <div className="text-sm leading-relaxed text-gray-600">
                  {t(
                    "inviteNewMembersOrRemoveMembersFromTheOrganization"
                  )}
                </div>
                <div>
                  {hasManageInvitationsRole ? (
                    <Link to={`/organizations/${orgId}/invitation/new`}>
                      <Button
                        isBlackButton
                        disabled={!hasManageInvitationsRole}
                      >
                        <Plus className="mr-2 w-5" />
                        {t("inviteNewMembers")}
                      </Button>
                    </Link>
                  ) : (
                    <Button isBlackButton disabled={!hasManageInvitationsRole}>
                      <Plus className="mr-2 w-5" />
                      {t("inviteNewMembers")}
                    </Button>
                  )}
                </div>
              </OrganizationActionCard>
            )}

            {/* Setup SSO */}
            {featureFlags.orgSsoEnabled && (
              <OrganizationActionCard>
                <OACTopRow>
                  <RoundedIcon>
                    <Network className="h-5 w-5" />
                  </RoundedIcon>
                  <Stat
                    label="active SSO connections"
                    value={idps.length}
                  ></Stat>
                </OACTopRow>
                <div className="text-sm leading-relaxed text-gray-600">
                  {t(
                    "setupSsoConnectionsAsNecessaryForThisOrganization"
                  )}
                </div>
                <div>
                  <Button
                    isBlackButton
                    onClick={() => OpenSSOLink({ orgId: orgId! })}
                    disabled={!hasManageIDPRole || !hasViewIDPRole}
                  >
                    {t("setupSso")}
                  </Button>
                </div>
              </OrganizationActionCard>
            )}

            {/* Setup domains */}
            {featureFlags.orgDomainsEnabled && (
              <OrganizationActionCard>
                <OACTopRow>
                  <RoundedIcon>
                    <Globe className="h-5 w-5" />
                  </RoundedIcon>
                  <Stat label="Domains" value={domains.length}></Stat>
                  <Stat label="Pending" value={verifiedDomains}></Stat>
                </OACTopRow>
                <div className="text-sm leading-relaxed text-gray-600">
                  {t(
                    "setupAssociatedDomainsAndVerifyThemToEnsureFullSecurity"
                  )}
                </div>
                <div>
                  {hasManageOrganizationRole ? (
                    <Link to={`/organizations/${org?.id}/domains/add`}>
                      <Button isBlackButton>{t("setupDomains")}</Button>
                    </Link>
                  ) : (
                    <Button isBlackButton disabled>
                      {t("setupDomains")}
                    </Button>
                  )}
                </div>
              </OrganizationActionCard>
            )}
          </div>
        </PrimaryContentArea>
      </MainContentArea>

      {/* Members table */}
      <MainContentArea>
        <section
          aria-labelledby="members-area"
          className="flex h-full min-w-0 flex-1 flex-col"
        >
          <HeaderLayout
            leftAreaItems={
              <>
                <SectionHeader title={t("members")} variant="small" />
                <div className="ml-2">
                  <RoundBadge>{totalMembers}</RoundBadge>
                </div>
              </>
            }
          />
          <div className="space-y-2 px-4 pb-4 md:px-10 md:pb-40">
            <div>
              <FormTextInputWithIcon
                inputArgs={{ placeholder: "searchMembers" }}
                className="w-full md:w-auto"
              />
            </div>
            {featureFlags.orgMembersEnabled && (
              <div>
                <MembersTable rows={rows} isLoading={isLoading} />
              </div>
            )}
          </div>
        </section>
      </MainContentArea>
    </>
  );
}
