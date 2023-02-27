import Button from "components/elements/forms/buttons/button";
import FormTextInputWithLabel from "components/elements/forms/inputs/text-input-with-label";
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
      <div className="py-10 space-y-5">
        <FormTextInputWithLabel
          slug="domain-name"
          label="Domain name"
          inputArgs={{ placeholder: "Domain name", autoFocus: true }}
        />
        <Button isBlackButton={true}>Add domain</Button>
      </div>
    </div>
  );
};

export default DomainsAdd;
