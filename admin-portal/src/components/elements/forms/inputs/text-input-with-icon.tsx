import { MagnifyingGlassIcon } from "@heroicons/react/20/solid";
import { BasicFormClasses } from "./text-input";
import cs from "classnames";
import { DetailedHTMLProps, FC, InputHTMLAttributes } from "react";

type Props = {
  inputArgs?: DetailedHTMLProps<
    InputHTMLAttributes<HTMLInputElement>,
    HTMLInputElement
  >;
  className?: string;
};

const FormTextInputWithIcon: FC<Props> = ({ inputArgs = {}, className }) => {
  return (
    <div className={cs("relative rounded-md shadow-sm", className)}>
      <div className="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3">
        <MagnifyingGlassIcon
          className="h-5 w-5 text-p2blue-900"
          aria-hidden="true"
        />
      </div>
      <input
        type="email"
        name="email"
        id="email"
        className={cs(BasicFormClasses, "w-full pl-10")}
        placeholder="you@example.com"
        {...inputArgs}
      />
    </div>
  );
};

export default FormTextInputWithIcon;
