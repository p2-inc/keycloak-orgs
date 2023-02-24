import cs from "classnames";
import { apiRealm } from "store/apis/helpers";
import Button, {
  ButtonIconLeftClasses,
} from "components/elements/forms/buttons/button";
import MainContentArea from "components/layouts/main-content-area";
import TopHeader from "components/navs/top-header";
import SectionHeader from "components/navs/section-header";
import { Link, useParams } from "react-router-dom";
import {
  useCreatePortalLinkMutation,
  useGetByRealmUsersAndUserIdOrgsOrgIdRolesQuery,
  useGetIdpsQuery,
  useGetOrganizationByIdQuery,
  useGetOrganizationDomainsQuery,
  useGetOrganizationInvitationsQuery,
  useGetOrganizationMembershipsQuery,
} from "store/apis/orgs";
import RoundBadge from "components/elements/badges/round-badge";
import FormTextInputWithIcon from "components/elements/forms/inputs/text-input-with-icon";
import HeaderLayout from "components/navs/components/header-layout";
import Table, {
  TableColumns,
  TableRows,
} from "components/elements/table/table";
import OrganizationActionCard, {
  OACTopRow,
} from "components/elements/organizations/action-card";
import PrimaryContentArea from "components/layouts/primary-content-area";
import Stat from "components/elements/cards/stat";
import {
  BuildingIcon,
  DoubleSlashBrandIcon,
  GlobeIcon,
  PersonIcon,
  PlusIcon,
} from "components/icons";
import { keycloak } from "keycloak";
import { useEffect, useState } from "react";
import { KeycloakProfile } from "keycloak-js";
import RoundedIcon from "components/elements/rounded-icon";
import { UserIcon } from "@heroicons/react/20/solid";

export default function OrganizationDetail() {
  let { orgId } = useParams();
  const { data: org } = useGetOrganizationByIdQuery({
    orgId: orgId!,
    realm: apiRealm,
  });
  const { data: members = [] } = useGetOrganizationMembershipsQuery({
    orgId: orgId!,
    realm: apiRealm,
  });
  const { data: invites = [] } = useGetOrganizationInvitationsQuery({
    orgId: org?.id!,
    realm: apiRealm,
  });
  const { data: domains = [] } = useGetOrganizationDomainsQuery({
    orgId: org?.id!,
    realm: apiRealm,
  });
  const verifiedDomains =
    domains.length > 0 ? domains.filter((d) => d.verified).length : 0;
  const { data: idps = [] } = useGetIdpsQuery({
    orgId: org?.id!,
    realm: apiRealm,
  });

  const [createPortalLink, { isSuccess }] = useCreatePortalLinkMutation();

  // Role checking
  // const [user, setUser] = useState<KeycloakProfile>();

  // const { data } = useGetByRealmUsersAndUserIdOrgsOrgIdRolesQuery({
  //   orgId: orgId!,
  //   realm: apiRealm,
  //   userId: user?.id,
  // });

  // async function loadUser() {
  //   const u = await keycloak.loadUserProfile();
  //   setUser(u);
  // }

  // useEffect(() => {
  //   loadUser();
  // }, []);

  const columns: TableColumns = [
    { key: "userName", data: "Username" },
    { key: "email", data: "Email" },
    { key: "firstName", data: "First Name" },
    { key: "role", data: "Last Name" },
  ];
  const rows: TableRows = members.map((member) => ({
    username: member.username,
    email: member.email,
    firstName: member.firstName,
    lastName: member.lastName,
  }));

  const OpenSSOLink = async () => {
    // const user = await keycloak.loadUserProfile();

    // TODO: used in self serve, is this the correct link to the "wizard"?
    const link = `${keycloak.authServerUrl}realms/${
      keycloak.realm
    }/wizard/?org_id=${encodeURIComponent(org?.id!)}`;
    window.open(link);

    // TODO: or is this the correct method? throws a 415 error
    // try {
    // const portalLink = await createPortalLink({
    //   orgId: org?.id!,
    //   realm: apiRealm,
    //   body: {
    //     userId: user.id,
    //   },
    // });
    // window.open(portalLink);
    // } catch (e) {
    //   console.error(e);
    // }
  };

  return (
    <>
      <TopHeader
        header={`${org?.displayName || ""} Organization`.trim()}
        rightAreaItems={
          <>
            <Link to={`/organizations/${org?.id}/settings`}>
              <Button>Settings</Button>
            </Link>
          </>
        }
      />
      {/* Action Cards */}
      <MainContentArea>
        <PrimaryContentArea>
          <div className="grid grid-cols-1 gap-6 sm:grid-cols-1 md:grid-cols-2 lg:grid-cols-3">
            {/* Invite new members */}
            <OrganizationActionCard>
              <OACTopRow>
                <RoundedIcon>
                  <PersonIcon
                    className={cs("h-[18] w-[18] stroke-current")}
                    aria-hidden="true"
                  />
                </RoundedIcon>
                <Stat label="members" value={members.length}></Stat>
                <Stat label="pending" value={invites.length}></Stat>
              </OACTopRow>
              <div>
                Invite new members or remove members from the organization.
              </div>
              <div>
                <Link to="/invitation/new">
                  <Button isBlackButton>
                    <PlusIcon
                      className={ButtonIconLeftClasses}
                      aria-hidden="true"
                    />
                    Invite new members
                  </Button>
                </Link>
              </div>
            </OrganizationActionCard>

            {/* Setup SSO */}
            <OrganizationActionCard>
              <OACTopRow>
                <RoundedIcon>
                  <BuildingIcon />
                </RoundedIcon>
                <Stat label="active SSO connections" value={idps.length}></Stat>
              </OACTopRow>
              <div>
                Setup SSO connections as necessary for this organization.
              </div>
              <div>
                <Button isBlackButton onClick={OpenSSOLink}>
                  Setup SSO
                </Button>
              </div>
            </OrganizationActionCard>

            {/* Setup domains */}
            <OrganizationActionCard>
              <OACTopRow>
                <RoundedIcon>
                  <GlobeIcon />
                </RoundedIcon>
                <Stat label="Domains" value={domains.length}></Stat>
                <Stat label="Pending" value={verifiedDomains}></Stat>
              </OACTopRow>
              <div>
                Setup associated domains and verify them to ensure full
                security.
              </div>
              <div>
                <Link to={`/organizations/${org?.id}/domains/add`}>
                  <Button isBlackButton>Setup domains</Button>
                </Link>
              </div>
            </OrganizationActionCard>
          </div>
        </PrimaryContentArea>
      </MainContentArea>

      {/* Members table */}
      <MainContentArea>
        <section
          aria-labelledby="members-area"
          className="flex h-full min-w-0 flex-1 flex-col overflow-y-auto"
        >
          <HeaderLayout
            leftAreaItems={
              <>
                <SectionHeader title="Members" variant="small" />
                {members && (
                  <div className="ml-2">
                    <RoundBadge>{members.length}</RoundBadge>
                  </div>
                )}
              </>
            }
            rightAreaItems={
              <FormTextInputWithIcon
                inputArgs={{ placeholder: "Search Members" }}
                className="w-full md:w-auto"
              />
            }
          />
          <div className="px-4 py-4 md:px-10 md:py-6">
            <Table columns={columns} rows={rows} />
          </div>
        </section>
      </MainContentArea>
    </>
  );
}
