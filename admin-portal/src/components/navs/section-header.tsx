import { FC } from "react";
import cs from "classnames";

type Props = {
  title: string;
  description?: string;
  variant?: "large" | "small";
};

const SectionHeader: FC<Props> = ({
  title,
  description,
  variant = "large",
}) => {
  return (
    <div
      className={cs({
        "space-y-2": variant === "large",
        "space-y-1": variant === "small",
      })}
    >
      <h2
        className={cs("font-semibold text-p2gray-900", {
          "text-2xl": variant === "large",
          "text-xl": variant === "small",
        })}
      >
        {title}
      </h2>
      {description && (
        <p className="max-w-prose text-base text-p2gray-900/80">
          {description}
        </p>
      )}
    </div>
  );
};

export default SectionHeader;
