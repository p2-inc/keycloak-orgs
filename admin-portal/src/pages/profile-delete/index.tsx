import RHFFormTextInputWithLabel from "components/elements/forms/inputs/rhf-text-input-with-label";
import SectionHeader from "components/navs/section-header";
import { User } from "lucide-react";
import { useForm } from "react-hook-form";
import { Link } from "react-router-dom";
import cs from "classnames";
import Button from "components/elements/forms/buttons/button";

const loadingIcon = (
  <div>
    <div className={cs("relative h-12 w-12 overflow-hidden rounded-md")}>
      <div className="absolute -inset-10 z-10 bg-gradient-to-tr from-red-600 to-red-800"></div>
      <div className="absolute inset-[2px] z-20 flex items-center justify-center rounded bg-white">
        <User />
      </div>
    </div>
  </div>
);

const ProfileDelete = () => {
  const {
    register,
    handleSubmit,
    formState: { errors, isDirty },
    setValue,
    reset,
  } = useForm();
  return (
    <div className="my-16 rounded-md border border-red-500 p-6 md:mx-auto md:max-w-prose">
      <div className="space-y-4">
        <SectionHeader
          title="Delete your profile"
          description="Permanently remove your profile and all of its contents. This action is not reversible, so please continue with caution."
          icon={loadingIcon}
          rightContent={
            <Link
              to={`/profile/general`}
              className="inline-block rounded-lg px-4 py-2 font-medium opacity-60 transition hover:bg-gray-100 hover:opacity-100"
            >
              Cancel
            </Link>
          }
        />
        <RHFFormTextInputWithLabel
          slug="delete"
          label="Write `delete` to confirm"
          register={register}
          inputArgs={{
            placeholder: "",
            autoFocus: true,
          }}
        />
        <Button isBlackButton>Confirm profile delete</Button>
      </div>
    </div>
  );
};

export default ProfileDelete;
