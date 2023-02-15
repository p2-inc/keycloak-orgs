import { UserIcon } from "@heroicons/react/20/solid";
import SectionHeader from "components/navs/section-header";
import cs from "classnames";

const loadingIcon = (
  <div className="mb-8">
    <div
      className={cs(
        "h-12 w-12 rounded-md relative overflow-hidden"
      )}
    >
      <div className="loading-animate absolute z-10 -inset-10"></div>
      <div className="flex items-center justify-center rounded bg-white absolute z-20 inset-[2px]">
        <UserIcon className="h-5 w-5"></UserIcon>
      </div>
    </div>
  </div>
);

const Invitation = () => {
  return (
    <div>
      <div className="mx-auto max-w-prose">
        <div className="mt-16">
          <SectionHeader
            title="Sending invitation..."
            description="One morning, when Gregor Samsa woke from troubled dreams, he found himself transformed in his bed into a horrible vermin."
            icon={loadingIcon}
          />
        </div>
      </div>
    </div>
  );
};

export default Invitation;
