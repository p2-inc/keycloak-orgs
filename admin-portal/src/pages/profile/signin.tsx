import Button from "components/elements/forms/buttons/button";
import Table from "components/elements/table/table";
import SectionHeader from "components/navs/section-header";

const SigninProfile = () => {
  return (
    <div>
      <div className="mb-12">
        <SectionHeader
          title="Signing in"
          description="Configure ways to sign in."
        />
      </div>
      <div className="space-y-12">
        <div className="space-y-5">
          <SectionHeader
            title="Basic authentication"
            variant="medium"
          />
          <SectionHeader
            title="Password"
            description="Sign in by entering your password."
            variant="small"
          />
          {/* <Table /> */}
        </div>
      </div>
      <div className="space-y-12">
        <div className="space-y-5">
          <SectionHeader
            title="Two-factor authentication"
            variant="medium"
          />
          <SectionHeader
            title="Authenticator application"
            description="Enter a verification code from authenticator application."
            variant="small"
          />
          {/* <Table /> */}
          <SectionHeader
            title="Security key"
            description="Use your security key to sign in."
            variant="small"
          />
          {/* <Table /> */}
        </div>
      </div>
      <div className="space-y-12">
        <div className="space-y-5">
          <SectionHeader
            title="Passwordless"
            variant="medium"
          />
          <SectionHeader
            title="Security key"
            description="Use your security key for passwordless sign in."
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
