import { FC } from "react";
import cs from "classnames";

type SwitchProps = {} & React.DetailedHTMLProps<
  React.InputHTMLAttributes<HTMLInputElement>,
  HTMLInputElement
>;

const Switch: FC<SwitchProps> = ({ children }, args) => {
  return (
    <label className="relative inline-flex cursor-pointer items-center">
      <input type="checkbox" value="" className="peer sr-only" />
      <div>{children}</div>
      <div
        className={cs(
          "peer relative h-6 w-11 flex-shrink-0 rounded-full bg-gray-200",
          "after:absolute after:top-[2px] after:left-[2px] after:h-5 after:w-5 after:rounded-full after:border after:border-gray-300 after:bg-white after:transition-all after:content-['']",
          "peer-checked:bg-secondary-800 peer-checked:after:translate-x-full peer-checked:after:border-white peer-focus:outline-none"
        )}
      ></div>
    </label>
  );
};

export default Switch;
