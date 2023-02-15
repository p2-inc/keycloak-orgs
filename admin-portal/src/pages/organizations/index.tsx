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
import { useGetOrgsQuery } from "store/orgs/service";

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
  // const [organizations, setOrganizations] = useState();
  const { data: orgs } = useGetOrgsQuery(1);

  // async function fetchOrgs() {
  //   const res = await Orgs.getOrgs();
  //   console.log("🚀 ~ file: index.tsx:59 ~ fetchOrgs ~ res", res);
  // }

  // useEffect(() => {
  //   fetchOrgs();
  // }, []);

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
            {people.map((person, index) => (
              <Link to={`/organizations/${index}/details`}>
                <li
                  key={person.email}
                  className="col-span-1 flex flex-col rounded-lg border bg-white p-6"
                >
                  <div className="mb-7">
                    <Title>{person.name}</Title>
                    <SubTitle>{person.title}</SubTitle>
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
