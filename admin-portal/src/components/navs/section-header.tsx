import { FC, ReactElement } from "react";
import cs from "classnames";

type Props = {
  title: string;
  description?: string;
  variant?: "large" | "medium" | "small";
  icon?: ReactElement;
  rightContent?: ReactElement;
};

const SectionHeader: FC<Props> = ({
  title,
  description,
  variant = "large",
  icon,
  rightContent,
}) => {
  return (
    <div
      className={cs({
        "space-y-1": variant === "large" || variant === "medium",
        "space-y-0": variant === "small"
      })}
    >
      {(icon || rightContent) && (
        <div className="mb-8 flex items-center justify-between">
          {icon && <>{icon}</>}
          {rightContent && <>{rightContent}</>}
        </div>
      )}
      <h2
        className={cs("font-semibold text-p2gray-900", {
          "text-2xl": variant === "large",
          "text-xl": variant === "medium",
          "text-l": variant === "small",
        })}
      >
        {title}
      </h2>
      {description && (
        <p className="max-w-prose text-base text-gray-600">
          {description}
        </p>
      )}
    </div>
  );
};

export default SectionHeader;
