import { FC } from "react";
import cs from "classnames";

type ButtonProps = {
  isBlackButton?: boolean;
} & React.DetailedHTMLProps<
  React.ButtonHTMLAttributes<HTMLButtonElement>,
  HTMLButtonElement
>;

export const ButtonIconLeftClasses = "fill-current -ml-1 mr-2 h-5 w-5";

const Button: FC<ButtonProps> = ({
  children,
  isBlackButton,
  className,
  ...args
}) => {
  return (
    <button
      {...args}
      className={cs(
        "group",
        "inline-flex",
        "focus:shadow-sm focus:outline-1 focus:ring-1 focus:ring-neutral-50 focus:ring-offset-1",
        className
      )}
    >
      <div className="relative w-full">
        <div
          className={cs(
            "relative z-20 rounded-[4px] p-px",
            "transition duration-200",
            "group-enabled:group-hover:bg-gradient-to-tl group-enabled:group-hover:from-p2grad-200 group-enabled:group-hover:via-p2grad-100 group-enabled:group-hover:to-p2grad-200",
            {
              "group-enabled:bg-p2gray-900": isBlackButton,
              "bg-neutral-300": !isBlackButton,
            }
          )}
        >
          <div
            className={cs(
              "flex items-center justify-center rounded-[3px] px-4 py-2 text-sm font-medium",
              {
                "bg-p2gray-900 group-enabled:text-white group-disabled:bg-neutral-400 group-disabled:text-white/50":
                  isBlackButton,
                "group-enabled:group-hover:bg-p2gray-800": isBlackButton,
                "bg-neutral-50 group-enabled:text-p2gray-900 group-disabled:text-p2gray-900/50":
                  !isBlackButton,
              }
            )}
          >
            {children}
          </div>
        </div>
        <div
          className={cs(
            "absolute inset-x-3 bottom-0 z-10 h-1/2 rounded-full bg-white opacity-0",
            "transition-opacity duration-200",
            "group-enabled:group-hover:opacity-100",
            "group-active:hidden",
            {
              "drop-shadow-btn-dark": isBlackButton,
              "drop-shadow-btn-light": !isBlackButton,
            }
          )}
        ></div>
      </div>
    </button>
  );
};

export default Button;
