import Button from "components/elements/forms/buttons/button";
import SectionHeader from "components/navs/section-header";

const LinkedProfile = () => {
  return (
    <div>
      <div className="mb-12">
        <SectionHeader
          title="Linked accounts"
          description="One morning, when Gregor Samsa woke from troubled dreams, he found himself transformed in his bed into a horrible vermin."
        />
      </div>
      <div className="w-full rounded border border-gray-200 bg-gray-50 divide-y">
        {[...Array(4)].map((e, i) => (
          <div className="flex items-center justify-between p-2">
            <div className="px-2">
              <span className="font-medium">GitHub</span>
            </div>
            <div><Button isBlackButton>Link account</Button></div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default LinkedProfile;
