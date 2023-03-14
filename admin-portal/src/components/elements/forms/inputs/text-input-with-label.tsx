import cs from "classnames";
import { DetailedHTMLProps, FC, InputHTMLAttributes } from "react";
import { BasicFormClasses } from "./text-input";

type Props = {
  slug: string;
  label: string;
  inputArgs?: DetailedHTMLProps<
    InputHTMLAttributes<HTMLInputElement>,
    HTMLInputElement
  >;
};

const FormTextInputWithLabel: FC<Props> = ({
  slug = "",
  label = "",
  inputArgs = {},
}) => {
  return (
    <div className="sm:col-span-3">
      <label htmlFor={slug} className="block text-sm font-medium text-gray-700">
        {label}
      </label>
      <div className="mt-1">
        <input
          type="text"
          name={slug}
          id={slug}
          className={cs(
            BasicFormClasses,
            "block w-full rounded-md border-gray-300 sm:text-sm focus:border-transparent focus:ring-[#134FC2]"
          )}
          placeholder="placeholder"
          {...inputArgs}
        />
      </div>
    </div>
  );
};

export default FormTextInputWithLabel;
