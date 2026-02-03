import cs from "classnames";
import { DetailedHTMLProps, FC, InputHTMLAttributes } from "react";
import {
  FieldError,
  FieldErrorsImpl,
  FieldValues,
  Merge,
  UseFormRegister,
} from "react-hook-form";
import { BasicFormClasses } from "./text-input";

type Props = {
  slug: string;
  label: string;
  register: UseFormRegister<FieldValues>;
  registerArgs?: {
    [key: string]: any;
  };
  inputArgs?: DetailedHTMLProps<
    InputHTMLAttributes<HTMLInputElement>,
    HTMLInputElement
  >;
  error?: FieldError | Merge<FieldError, FieldErrorsImpl<any>> | undefined;
  helpText?: string;
};

const RHFFormTextInputWithLabel: FC<Props> = ({
  slug = "",
  label = "",
  inputArgs = {},
  register,
  registerArgs = {},
  error,
  helpText,
}) => {
  return (
    <div className="sm:col-span-3">
      <label
        htmlFor={slug}
        className="block text-sm font-medium text-gray-700 dark:text-zinc-200"
      >
        {label}
      </label>
      <div className="mt-1">
        <input
          type="text"
          id={slug}
          className={cs(BasicFormClasses, "w-full", {
            "border-pink-500 text-pink-600 focus:border-pink-500 focus:ring-pink-500":
              error,
          })}
          placeholder="placeholder"
          {...register(slug, registerArgs)}
          {...inputArgs}
        />
      </div>
      {helpText && (
        <p className="mt-2 text-sm text-gray-500" id={`${slug}__help_text`}>
          {helpText}
        </p>
      )}
    </div>
  );
};

export default RHFFormTextInputWithLabel;
