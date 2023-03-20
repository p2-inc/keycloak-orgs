import FormTextInputWithIcon from "components/elements/forms/inputs/text-input-with-icon";
import MainContentArea from "components/layouts/main-content-area";
import TopHeader from "components/navs/top-header";
import PrimaryContentArea from "components/layouts/primary-content-area";
import { useGetOrganizationsQuery } from "store/apis/orgs";
import { config } from "config";
import OrganizationsLoader from "components/loaders/organizations";
import OrganizationItem from "components/elements/organizations/item";
import ViewSwitch, {
  ViewLayoutOptions,
} from "components/elements/forms/switches/view-switch";
import { useState } from "react";
import cs from "classnames";
import DomainStat from "./components/domain-stat";
import MembersStat from "./components/members-stat";

export default function Organizations() {
  const [viewType, setViewType] = useState<ViewLayoutOptions>(
    ViewLayoutOptions.GRID
  );
  const { data: orgs = [], isFetching } = useGetOrganizationsQuery({
    realm: config.env.realm,
  });

  return (
    <>
      <TopHeader
        header="Organizations"
        badgeVal={orgs.length}
        rightAreaItems={
          <>
            <FormTextInputWithIcon
              inputArgs={{ placeholder: "Search Organizations" }}
              className="w-full md:w-auto"
            />
            <ViewSwitch onChange={(value) => setViewType(value)} />
          </>
        }
      />
      <MainContentArea>
        {/* Primary content */}
        <PrimaryContentArea>
          <div>
            {isFetching && (
              <div className="grid grid-cols-1 gap-6 sm:grid-cols-1 md:grid-cols-2 lg:grid-cols-3">
                <OrganizationsLoader />
              </div>
            )}
            {!isFetching && (
              <div
                className={cs({
                  "grid grid-cols-1 gap-6 sm:grid-cols-1 md:grid-cols-2 lg:grid-cols-3":
                    viewType === ViewLayoutOptions.GRID,
                  "divide-y rounded-md border border-gray-200 bg-gray-50":
                    viewType === ViewLayoutOptions.LIST,
                })}
              >
                {orgs.map((org) => (
                  <OrganizationItem
                    key={org.id}
                    link={`/organizations/${org.id}/details`}
                    title={org.displayName}
                    subTitle={org.name}
                    viewType={viewType}
                  >
                    <MembersStat org={org} realm={config.env.realm} />
                    <DomainStat org={org} realm={config.env.realm} />
                  </OrganizationItem>
                ))}
              </div>
            )}
          </div>
        </PrimaryContentArea>
      </MainContentArea>
    </>
  );
}
