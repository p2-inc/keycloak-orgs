import cs from "classnames";
import { FC, ReactElement } from "react";

type Props = {
  leftAreaItems?: ReactElement | ReactElement[];
  rightAreaItems?: ReactElement | ReactElement[];
  collapseOnMobile?: boolean;
};

const HeaderLayout: FC<Props> = ({
  leftAreaItems,
  rightAreaItems,
  collapseOnMobile,
}) => {
  return (
    <div className="flex flex-col space-y-4 px-4 py-4 md:flex-row md:space-y-0 md:px-10 md:py-6">
      <div
        className={cs("items-center justify-between md:justify-start", {
          flex: !collapseOnMobile,
          "md:flex": collapseOnMobile,
        })}
      >
        {leftAreaItems}
      </div>
      <div className="flex-grow flex-col-reverse items-center justify-end gap-2 md:flex md:flex-row">
        {rightAreaItems}
      </div>
    </div>
  );
};

export default HeaderLayout;
