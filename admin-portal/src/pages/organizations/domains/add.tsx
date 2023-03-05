import Button from "components/elements/forms/buttons/button";
import FormTextInputWithLabel from "components/elements/forms/inputs/text-input-with-label";
import RoundedIcon from "components/elements/rounded-icon";
import { GlobeIcon } from "components/icons";
import SectionHeader from "components/navs/section-header";
import { Link, useParams } from "react-router-dom";

const addIcon = (
  <RoundedIcon className="my-4">
    <GlobeIcon />
  </RoundedIcon>
);

const DomainsAdd = () => {
  let { orgId } = useParams();
  return (
    <div className="md:py-20">
      <SectionHeader
        title="Add new domain"
        description="One morning, when Gregor Samsa woke from troubled dreams, he found himself transformed in his bed into a horrible vermin."
        icon={addIcon}
        rightContent={
          <Link to={`/organizations/${orgId}/details`} className="font-medium inline-block px-4 py-2 opacity-60 rounded-lg transition hover:bg-gray-100 hover:opacity-100">Cancel</Link>
        }
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
