import Dropdown from "components/elements/forms/dropdown/dropdown";
import SquareBadge from "components/elements/badges/square-badge";
import Button from "components/elements/forms/buttons/button";
import Switch from "components/elements/forms/switches/switch";
import SectionHeader from "components/navs/section-header";

const RoleProfile = () => {
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

  return (
    <div>
      <div className="mb-12">
        <SectionHeader
          title="Role"
          description="One morning, when Gregor Samsa woke from troubled dreams, he found himself transformed in his bed into a horrible vermin."
        />
      </div>
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
        emptyContent={<span>Select item</span>}
        className="block w-full"
      />
      <div className="md:grid md:grid-cols-2 md:gap-4">
        <Switch>
          <div className="space-y-1 pr-4">
            <SquareBadge>read-org</SquareBadge>
            <p className="text-sm text-p2gray-900/60">
              One morning, when Gregor Samsa woke from troubled dreams
            </p>
          </div>
        </Switch>
        <Switch>
          <div className="space-y-1 pr-4">
            <SquareBadge>read-org</SquareBadge>
            <p className="text-sm text-p2gray-900/60">
              One morning, when Gregor Samsa woke from troubled dreams
            </p>
          </div>
        </Switch>
        <Switch>
          <div className="space-y-1 pr-4">
            <SquareBadge>read-org</SquareBadge>
            <p className="text-sm text-p2gray-900/60">
              One morning, when Gregor Samsa woke from troubled dreams
            </p>
          </div>
        </Switch>
        <Switch>
          <div className="space-y-1 pr-4">
            <SquareBadge>read-org</SquareBadge>
            <p className="text-sm text-p2gray-900/60">
              One morning, when Gregor Samsa woke from troubled dreams
            </p>
          </div>
        </Switch>
      </div>
      <div className="py-4">
        <Button isBlackButton>Save changes</Button>
      </div>
    </div>
  );
};

export default RoleProfile;
