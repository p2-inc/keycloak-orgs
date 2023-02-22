import { UserIcon } from "@heroicons/react/20/solid";
import SectionHeader from "components/navs/section-header";
import cs from "classnames";

const loadingIcon = (
  <div className="mb-8">
    <div className={cs("relative h-12 w-12 overflow-hidden rounded-md")}>
      <div className="loading-animate absolute -inset-10 z-10"></div>
      <div className="absolute inset-[2px] z-20 flex items-center justify-center rounded bg-white">
        <UserIcon className="h-5 w-5"></UserIcon>
      </div>
    </div>
  </div>
);

const SendingInvitation = () => {
  return (
    <div className="mt-16">
      <SectionHeader
        title="Sending invitation..."
        description="One morning, when Gregor Samsa woke from troubled dreams, he found himself transformed in his bed into a horrible vermin."
        icon={loadingIcon}
      />
    </div>
  );
};

export default SendingInvitation;
