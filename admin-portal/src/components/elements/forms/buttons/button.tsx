import { FC } from "react";
import cs from "classnames";

type ButtonProps = {
  isBlackButton?: boolean;
  isCompact?: boolean;
} & React.DetailedHTMLProps<
  React.ButtonHTMLAttributes<HTMLButtonElement>,
  HTMLButtonElement
>;

export const ButtonIconLeftClasses = "fill-current -ml-1 mr-2 h-5 w-5";

const Button: FC<ButtonProps> = ({
  children,
  isBlackButton,
  isCompact,
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
            "relative z-20 rounded p-px",
            "transition duration-200",
            "group-enabled:group-hover:bg-gradient-to-tl group-enabled:group-hover:from-p2primary-600 group-enabled:group-hover:via-p2primary-400 group-enabled:group-hover:to-p2primary-600",
            {
              "group-enabled:bg-p2gray-900": isBlackButton,
              "bg-neutral-300 dark:bg-zinc-600": !isBlackButton,
            }
          )}
        >
          <div
            className={cs(
              "flex items-center justify-center rounded-[3px] font-medium",
              {
                "px-4 py-2 text-sm": !isCompact,
                "px-3 py-1 text-xs": isCompact,
              },
              {
                "bg-p2gray-900 group-enabled:group-hover:bg-p2gray-800 group-enabled:text-white group-disabled:bg-neutral-400 group-disabled:text-white/50 dark:bg-white dark:group-enabled:text-zinc-800 dark:group-enabled:group-hover:bg-p2dark-1000 dark:group-enabled:group-hover:text-zinc-200":
                  isBlackButton,
                "group-enabled:text-p2gray-900 group-disabled:text-p2gray-900/50 bg-neutral-50 dark:bg-p2dark-1000 dark:group-enabled:text-zinc-200 dark:group-disabled:text-zinc-200/50":
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
            "group-active:hidden",
            {
              "drop-shadow-btn-dark": isBlackButton,
              "drop-shadow-btn-light": !isBlackButton,
            },
            {
              "group-enabled:group-hover:opacity-100": !isCompact,
            }
          )}
        ></div>
      </div>
    </button>
  );
};

export default Button;
