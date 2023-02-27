import Button from "components/elements/forms/buttons/button";
import CopyBlock from "components/elements/organizations/copy-block";
import RoundedIcon from "components/elements/rounded-icon";
import { GlobeIcon } from "components/icons";
import SectionHeader from "components/navs/section-header";

const addIcon = (
  <RoundedIcon className="my-4">
    <GlobeIcon />
  </RoundedIcon>
);

const DomainsVerify = () => {
  return (
    <div className="space-y-10 md:py-20">
      <div>
        <SectionHeader
          title="Verify domain"
          description="Use the following details to verify your domain."
          icon={addIcon}
        />
      </div>
      <div>
        <CopyBlock
          label="Create a TXT record in your DNS configuration for the following hostname"
          value="_phasetwo-domain-ownership"
        />
        <CopyBlock
          label="Use this code for the value of the TXT record"
          value="06dsf023021lldsf002320ds0230120103045060070"
        />
        <Button isBlackButton={true}>Verify</Button>
      </div>
    </div>
  );
};

export default DomainsVerify;
