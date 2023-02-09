import FormTextInputWithLabel from "components/elements/forms/inputs/text-input-with-label";

const GeneralProfile = () => {
  return (
    <form className=" space-y-4">
      <FormTextInputWithLabel
        slug="email"
        label="Email"
        inputArgs={{ placeholder: "you@email.com" }}
      />
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
    </form>
  );
};

export default GeneralProfile;
