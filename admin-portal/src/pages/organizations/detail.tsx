import { apiRealm } from "store/apis/helpers";
import Button from "components/elements/forms/buttons/button";
import MainContentArea from "components/layouts/main-content-area";
import TopHeader from "components/navs/top-header";
import SectionHeader from "components/navs/section-header";
import { useState } from "react";
import { useParams } from "react-router-dom";
import {
  useGetOrganizationByIdQuery,
  useGetOrganizationMembershipsQuery,
  useGetUserOrganizationRolesQuery,
} from "store/apis/orgs";
import RoundBadge from "components/elements/badges/round-badge";
import FormTextInputWithIcon from "components/elements/forms/inputs/text-input-with-icon";
import HeaderLayout from "components/navs/components/header-layout";
import Table, {
  TableColumns,
  TableRows,
} from "components/elements/table/table";

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
  console.log(
    "🚀 ~ file: detail.tsx:27 ~ OrganizationDetail ~ members:",
    members
  );
  console.log("🚀 ~ file: detail.tsx:14 ~ OrganizationDetail ~ org", org);

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

  return (
    <>
      <TopHeader
        header={`${org?.displayName} organization`}
        rightAreaItems={
          <>
            <Button>Settings</Button>
          </>
        }
      />
      <MainContentArea>
        {/* Primary content */}
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
          <div className="px-4 py-4 md:px-10">
            <Table columns={columns} rows={rows} />
          </div>
        </section>
      </MainContentArea>
    </>
  );
}
