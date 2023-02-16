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
import { useGetOrganizationsQuery } from "store/p2-api/p2-api";
import { apiRealm } from "api/helpers";

const people = [
  {
    name: "Garth Patil",
    title: "Grand Master",
    email: "gpatil@phasetwo.com",
    role: "Master",
  },
  {
    name: "Garth Patil",
    title: "Grand Master",
    email: "gpatil@phasetwo.com",
    role: "Master",
  },
  {
    name: "Garth Patil",
    title: "Grand Master",
    email: "gpatil@phasetwo.com",
    role: "Master",
  },
  {
    name: "Garth Patil",
    title: "Grand Master",
    email: "gpatil@phasetwo.com",
    role: "Master",
  },
  {
    name: "Garth Patil",
    title: "Grand Master",
    email: "gpatil@phasetwo.com",
    role: "Master",
  },
];

const Title = ({ children }) => (
  <div className=" font-semibold">{children}</div>
);
const SubTitle = ({ children }) => (
  <div className=" text-[14px]">{children}</div>
);

export default function Organizations() {
  const { data: orgs = [] } = useGetOrganizationsQuery({ realm: apiRealm });

  return (
    <>
      <TopHeader
        header="Organizations"
        badgeVal="2"
        rightAreaItems={
          <>
            <FormTextInputWithIcon
              inputArgs={{ placeholder: "Search Organizations" }}
            />
            <Button isBlackButton>
              <PlusIcon className={ButtonIconLeftClasses} aria-hidden="true" />
              Create Organization
            </Button>
          </>
        }
      />
      <MainContentArea>
        {/* Primary content */}
        <PrimaryContentArea>
          <ul className="mt-10 grid grid-cols-1 gap-6 sm:grid-cols-1 md:grid-cols-2 lg:grid-cols-3">
            {orgs.map((org) => (
              <Link to={`/organizations/${org.id}/details`} key={org.id}>
                <li className="col-span-1 flex flex-col rounded-lg border bg-white p-6">
                  <div className="mb-7">
                    <Title>{org.displayName}</Title>
                    <SubTitle>{org.name}</SubTitle>
                  </div>
                  <div className="flex flex-row space-x-8">
                    <Stat value="4" label="members" />
                    <Stat percent={50} value="3" label="domains" />
                  </div>
                </li>
              </Link>
            ))}
          </ul>
        </PrimaryContentArea>
      </MainContentArea>
    </>
  );
}
