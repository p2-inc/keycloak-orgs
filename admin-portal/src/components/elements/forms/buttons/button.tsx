import { FC } from "react";
import cs from "classnames";

type ButtonProps = { isBlackButton?: boolean } & React.DetailedHTMLProps<
  React.ButtonHTMLAttributes<HTMLButtonElement>,
  HTMLButtonElement
>;

export const ButtonIconLeftClasses = "fill-current -ml-1 mr-2 h-5 w-5";

const Button: FC<ButtonProps> = ({ children, isBlackButton }, ...args) => {
  return (
    <button
      type="button"
      className={cs(
        "inline-flex items-center rounded border border-neutral-200 bg-neutral-50 px-4 py-2 text-sm font-medium text-p2blue-900  hover:bg-neutral-100 focus:outline-none focus:ring-1 focus:ring-neutral-50 focus:ring-offset-1",
        {
          "bg-p2gray-900 text-white hover:bg-zinc-900": isBlackButton,
        }
      )}
      {...args}
    >
      {children}
    </button>
  );
};

export default Button;
