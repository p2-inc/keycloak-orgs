import Button, {
  ButtonIconLeftClasses,
} from "components/elements/forms/buttons/button";
import FormTextInputWithIcon from "components/elements/forms/inputs/text-input-with-icon";
import MainContentArea from "components/layouts/main-content-area";
import TopHeader from "components/navs/top-header";
import { PlusIcon } from "components/icons";
import PrimaryContentArea from "components/layouts/primary-content-area";
import { Link } from "react-router-dom";
import Stat from "components/elements/cards/stat";
import {
  useGetOrganizationDomainQuery,
  useGetOrganizationsQuery,
} from "store/apis/orgs";
import { apiRealm } from "store/apis/helpers";
import OrganizationsLoader from "components/loaders/organizations";
import OrganizationItem from "components/elements/organizations/item";
import DomainStat from "./components/domain-stat";

export default function Organizations() {
  const { data: orgs = [], isFetching } = useGetOrganizationsQuery({
    realm: apiRealm,
  });

  console.log("🚀 ~ file: index.tsx:18 ~ Organizations ~ orgs:", orgs);

  return (
    <>
      <TopHeader
        header="Organizations"
        badgeVal="2"
        rightAreaItems={
          <>
            <FormTextInputWithIcon
              inputArgs={{ placeholder: "Search Organizations" }}
              className="w-full md:w-auto"
            />
            <Button isBlackButton className="w-full md:w-auto">
              <PlusIcon className={ButtonIconLeftClasses} aria-hidden="true" />
              Create Organization
            </Button>
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
              <div className="grid grid-cols-1 gap-6 sm:grid-cols-1 md:grid-cols-2 lg:grid-cols-3">
                {orgs.map((org) => (
                  <OrganizationItem
                    key={org.id}
                    link={`/organizations/${org.id}/details`}
                    title={org.displayName}
                    subTitle={org.name}
                  >
                    <Stat value="4" label="members" />
                    <DomainStat org={org} realm={apiRealm} />
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
