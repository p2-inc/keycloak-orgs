import RoundedIcon from "components/elements/rounded-icon";
import { GlobeIcon } from "components/icons";
import SectionHeader from "components/navs/section-header";

const addIcon = (
  <RoundedIcon className="my-4">
    <GlobeIcon />
  </RoundedIcon>
);

const DomainsAdd = () => {
  return (
    <div className="py-20">
      <SectionHeader
        title="Add new domain"
        description="One morning, when Gregor Samsa woke from troubled dreams, he found himself transformed in his bed into a horrible vermin."
        icon={addIcon}
      />
    </div>
  );
};

export default DomainsAdd;
