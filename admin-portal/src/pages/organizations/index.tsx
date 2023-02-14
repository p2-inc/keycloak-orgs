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

const ProgressBar = ({ percent = 20 }) => {
  const radius = 10;
  const circumference = radius * 2 * Math.PI;
  return (
    <svg className="h-6 w-6">
      <circle
        className="text-gray-300"
        stroke-width="3"
        stroke="currentColor"
        fill="transparent"
        r={radius}
        cx="12"
        cy="12"
      />
      <circle
        className=" text-p2blue-700"
        stroke-width="3"
        stroke-dasharray={circumference}
        stroke-dashoffset={circumference - (percent / 100) * circumference}
        stroke-linecap="round"
        stroke="currentColor"
        fill="transparent"
        r={radius}
        cx="12"
        cy="12"
      />
    </svg>
  );
};

export default function Organizations() {
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
