import Button from "components/elements/forms/buttons/button";
import FormTextInputWithLabel from "components/elements/forms/inputs/text-input-with-label";
import SectionHeader from "components/navs/section-header";
import { useParams } from "react-router-dom";
import { apiRealm } from "store/apis/helpers";
import { useGetOrganizationByIdQuery } from "store/apis/orgs";

const SettingsGeneral = () => {
  let { orgId } = useParams();
  const { data: org } = useGetOrganizationByIdQuery({
    orgId: orgId!,
    realm: apiRealm,
  });
  return (
    <div className="space-y-4">
      <div>
        <SectionHeader
          title="General Settings"
          description="One morning, when Gregor Samsa woke from troubled dreams, he found himself transformed in his bed into a horrible vermin."
        />
      </div>
      <FormTextInputWithLabel
        label="Id"
        slug="id"
        inputArgs={{ defaultValue: org?.id, disabled: true }}
      />
      <FormTextInputWithLabel
        label="Name"
        slug="name"
        inputArgs={{ defaultValue: org?.name, disabled: true }}
      />
      <FormTextInputWithLabel
        label="Display Name"
        slug="display-name"
        inputArgs={{ defaultValue: org?.name }}
      />

      <Button isBlackButton>Update Organization</Button>
    </div>
  );
};

export default SettingsGeneral;
