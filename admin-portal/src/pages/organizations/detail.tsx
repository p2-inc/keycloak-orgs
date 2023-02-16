import { apiRealm } from "api/helpers";
import Button from "components/elements/forms/buttons/button";
import MainContentArea from "components/layouts/main-content-area";
import TopHeader from "components/navs/top-header";
import { useState } from "react";
import { useParams } from "react-router-dom";
import {
  useGetOrganizationByIdQuery,
  useUpdateOrganizationMutation,
} from "store/apis/orgs";

export default function OrganizationDetail() {
  let { orgId } = useParams();
  const [orgName, setOrgName] = useState("");
  const { data } = useGetOrganizationByIdQuery({
    orgId: orgId!,
    realm: apiRealm,
  });
  const [updateOrg, { isSuccess }] = useUpdateOrganizationMutation();
  console.log("🚀 ~ file: detail.tsx:14 ~ OrganizationDetail ~ data", data);

  const triggerOrgUpdateTemp = () => {
    console.log(
      "🚀 ~ file: detail.tsx:27 ~ triggerOrgUpdateTemp ~ orgName",
      orgName
    );
    updateOrg({
      orgId: orgId!,
      realm: apiRealm,
      organizationRepresentation: { ...data, name: orgName },
    });
  };

  return (
    <>
      <TopHeader
        header="Organization Detail"
        rightAreaItems={
          <>
            <Button>Settings</Button>
          </>
        }
      />
      <MainContentArea>
        {/* Primary content */}
        <section
          aria-labelledby="primary-heading"
          className="flex h-full min-w-0 flex-1 flex-col overflow-y-auto px-4"
        >
          <div>{data?.displayName}</div>
          <div>{data?.name}</div>
          <input
            type="text"
            name="orgName"
            id="orgName"
            defaultValue={data?.name}
            onChange={(e) => setOrgName(e.target.value)}
          />
          <div>{data?.id}</div>
          <div>{data?.url}</div>
          <button onClick={triggerOrgUpdateTemp}>Update Org</button>
        </section>
      </MainContentArea>
    </>
  );
}
