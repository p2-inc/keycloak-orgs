import Button from "components/elements/forms/buttons/button";
import FormTextInputWithLabel from "components/elements/forms/inputs/text-input-with-label";
import SectionHeader from "components/navs/section-header";
import { useFeatureFlags } from "store/feature-flags/hooks";

const GeneralProfile = () => {
  const { featureFlags } = useFeatureFlags();
  return (
    <div>
      <div className="mb-12">
        <SectionHeader
          title="General"
          description="One morning, when Gregor Samsa woke from troubled dreams, he found himself transformed in his bed into a horrible vermin."
        />
      </div>
      <form className="space-y-4">
        {featureFlags.updateEmailFeatureEnabled && (
          <FormTextInputWithLabel
            slug="email"
            label="Email"
            inputArgs={{ placeholder: "you@email.com" }}
          />
        )}
        <FormTextInputWithLabel
          slug="firstName"
          label="First Name"
          inputArgs={{ placeholder: "jane" }}
        />
        <FormTextInputWithLabel
          slug="lastName"
          label="Last Name"
          inputArgs={{ placeholder: "doe" }}
        />
        <Button isBlackButton>Save changes</Button>
      </form>
    </div>
  );
};

export default GeneralProfile;
