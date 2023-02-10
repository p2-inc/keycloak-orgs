import Switch from "components/elements/forms/switches/switch";
import SectionHeader from "components/navs/section-header";

const RoleProfile = () => {
  return (
    <div>
      <div className="mb-12">
        <SectionHeader
          title="Role"
          description="One morning, when Gregor Samsa woke from troubled dreams, he found himself transformed in his bed into a horrible vermin."
        />
      </div>
      <div className="grid grid-cols-2 gap-4">
        <Switch>
          <div className="space-y-1 pr-4">
            <span className="font-mono text-xs border border-p2gray-900 px-1 py-px rounded font-medium">read-org</span>
            <p className="text-sm text-p2gray-900/60">One morning, when Gregor Samsa woke from troubled dreams</p>
          </div>
        </Switch>
        <Switch>
          <div className="space-y-1 pr-4">
            <span className="font-mono text-xs border border-p2gray-900 px-1 py-px rounded font-medium">read-org</span>
            <p className="text-sm text-p2gray-900/60">One morning, when Gregor Samsa woke from troubled dreams</p>
          </div>
        </Switch>
        <Switch>
          <div className="space-y-1 pr-4">
            <span className="font-mono text-xs border border-p2gray-900 px-1 py-px rounded font-medium">read-org</span>
            <p className="text-sm text-p2gray-900/60">One morning, when Gregor Samsa woke from troubled dreams</p>
          </div>
        </Switch>
        <Switch>
          <div className="space-y-1 pr-4">
            <span className="font-mono text-xs border border-p2gray-900 px-1 py-px rounded font-medium">read-org</span>
            <p className="text-sm text-p2gray-900/60">One morning, when Gregor Samsa woke from troubled dreams</p>
          </div>
        </Switch>
      </div>
    </div>
  );
};

export default RoleProfile;
