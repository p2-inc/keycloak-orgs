import { UserIcon } from "@heroicons/react/20/solid";
import SectionHeader from "components/navs/section-header";
import cs from "classnames";
import FormTextInputWithLabel from "components/elements/forms/inputs/text-input-with-label";
import Button from "components/elements/forms/buttons/button";
import Dropdown from "components/elements/forms/dropdown/dropdown";

const loadingIcon = (
  <div className="mb-8">
    <div className={cs("relative h-12 w-12 overflow-hidden rounded-md")}>
      <div className="absolute -inset-10 z-10 bg-gradient-to-tr from-[#C7DFF0] to-[#1476B7]"></div>
      <div className="absolute inset-[2px] z-20 flex items-center justify-center rounded bg-white">
        <UserIcon className="h-5 w-5"></UserIcon>
      </div>
    </div>
  </div>
);

const admin = (
  <div>
    <div className="text-sm font-medium">Admin</div>
    <div className="space-x-2">
      <span className="rounded border border-p2gray-900 px-1 py-px font-mono text-xs font-medium">
        read-org
      </span>
      <span className="rounded border border-p2gray-900 px-1 py-px font-mono text-xs font-medium">
        read-org
      </span>
      <span className="rounded border border-p2gray-900 px-1 py-px font-mono text-xs font-medium">
        read-org
      </span>
    </div>
  </div>
);
const member = (
  <div>
    <div className="text-sm font-medium">Member</div>
    <div className="space-x-2">
      <span className="rounded border border-p2gray-900 px-1 py-px font-mono text-xs font-medium">
        read-org
      </span>
      <span className="rounded border border-p2gray-900 px-1 py-px font-mono text-xs font-medium">
        read-org
      </span>
      <span className="rounded border border-p2gray-900 px-1 py-px font-mono text-xs font-medium">
        read-org
      </span>
    </div>
  </div>
);

const NewInvitation = () => {
  return (
    <div className="mt-16">
      <SectionHeader
        title="Invite new member"
        description="One morning, when Gregor Samsa woke from troubled dreams, he found himself transformed in his bed into a horrible vermin."
        icon={loadingIcon}
      />
      <div className="mt-8 space-y-4">
        <Dropdown
          items={[
            { content: admin, value: "United States", id: 1 },
            { content: member, value: "Canada", id: 2 },
            {
              content: <div className="text-sm font-medium">Custom</div>,
              value: "Canada",
              id: 2,
            },
          ]}
          emptyContent={<span>Select role</span>}
          className="block w-full"
        />
        <FormTextInputWithLabel
          slug="email"
          label="Email"
          inputArgs={{ placeholder: "you@email.com" }}
        />

        <div className="mt-4">
          <Button isBlackButton={true}>Send invitation</Button>
        </div>
      </div>
    </div>
  );
};

export default NewInvitation;
