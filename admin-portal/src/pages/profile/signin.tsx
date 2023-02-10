import Button from "components/elements/forms/buttons/button";
import Table from "components/elements/table/table";
import SectionHeader from "components/navs/section-header";

const SigninProfile = () => {
  return (
    <div>
      <div className="mb-12">
        <SectionHeader
          title="Signing in"
          description="One morning, when Gregor Samsa woke from troubled dreams, he found himself transformed in his bed into a horrible vermin."
        />
      </div>
      <div className="space-y-12">
        <div className="space-y-5">
          <SectionHeader
            title="Basic authentication"
            description="Sign in by entering your password."
            variant="small"
          />
          <Table />
        </div>
        <div className="space-y-5">
          <SectionHeader
            title="Two factor authnetication"
            description="Sign in by entering your password."
            variant="small"
          />
          <div className="flex items-center justify-center rounded border border-gray-200 bg-gray-50 py-8">
            <Button isBlackButton>Setup authenticator app</Button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default SigninProfile;
