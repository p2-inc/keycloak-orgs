import FormTextInputWithIcon from "components/elements/forms/inputs/text-input-with-icon";
import MainContentArea from "components/layouts/main-content-area";
import TopHeader from "components/navs/top-header";
import PrimaryContentArea from "components/layouts/primary-content-area";
import { useGetByRealmUsersAndUserIdOrgsQuery } from "store/apis/orgs";
import { config } from "config";
import OrganizationsLoader from "components/loaders/organizations";
import OrganizationItem from "components/elements/organizations/item";
import ViewSwitch, {
  ViewLayoutOptions,
} from "components/elements/forms/switches/view-switch";
import { useEffect, useState } from "react";
import cs from "classnames";
import DomainStat from "./components/domain-stat";
import MembersStat from "./components/members-stat";
import useUser from "components/utils/useUser";
import Fuse from "fuse.js";

const { realm } = config.env;

export default function Organizations() {
  const { user } = useUser();
  const [viewType, setViewType] = useState<ViewLayoutOptions>(
    ViewLayoutOptions.GRID
  );
  const [searchString, setSearchString] = useState("");
  const { data: userOrgs = [], isFetching } =
    useGetByRealmUsersAndUserIdOrgsQuery(
      {
        realm,
        userId: user?.id!,
      },
      { skip: !user?.id }
    );

  const fuse = new Fuse(userOrgs, {
    keys: ["displayName", "name", "domains"],
  });

  useEffect(() => {
    fuse.setCollection(userOrgs);
  }, [userOrgs]);

  const searchOrgs =
    searchString === ""
      ? userOrgs.map((org) => ({ item: org }))
      : fuse.search(searchString);

  return (
    <>
      <TopHeader
        header="Organizations"
        badgeVal={userOrgs.length}
        rightAreaItems={
          <>
            <FormTextInputWithIcon
              inputArgs={{
                placeholder: "Search Organizations",
                onChange: (e) => setSearchString(e.target.value),
              }}
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
              <div className="grid grid-cols-1 justify-items-stretch gap-6 sm:grid-cols-1 md:grid-cols-2 lg:grid-cols-3">
                <OrganizationsLoader />
                <OrganizationsLoader />
                <OrganizationsLoader />
                <OrganizationsLoader />
                <OrganizationsLoader />
                <OrganizationsLoader />
              </div>
            )}
            {!isFetching && (
              <div
                className={cs({
                  "grid grid-cols-1 justify-items-stretch gap-6 sm:grid-cols-1 md:grid-cols-2 lg:grid-cols-3":
                    viewType === ViewLayoutOptions.GRID,
                  "divide-y rounded-md border border-gray-200 bg-gray-50 dark:divide-zinc-600 dark:border-zinc-600 dark:bg-p2dark-1000":
                    viewType === ViewLayoutOptions.LIST,
                })}
              >
                {searchOrgs.map(({ item: org }) => {
                  return (
                    <OrganizationItem
                      key={org.id}
                      org={org}
                      viewType={viewType}
                    >
                      <MembersStat org={org} realm={realm} />
                      <DomainStat org={org} realm={realm} />
                    </OrganizationItem>
                  );
                })}
              </div>
            )}
          </div>
        </PrimaryContentArea>
      </MainContentArea>
    </>
  );
}
