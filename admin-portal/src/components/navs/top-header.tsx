import { FC, ReactElement } from "react";
import RoundBadge from "../elements/badges/round-badge";
import HeaderLayout from "./components/header-layout";
import { isNil } from "lodash";

type Props = {
  header: string;
  badgeVal?: string | number;
  rightAreaItems?: ReactElement | ReactElement[];
  leftAreaItems?: ReactElement | ReactElement[];
  collapseOnMobile?: boolean;
};

const TopHeader: FC<Props> = ({
  header,
  badgeVal,
  rightAreaItems,
  leftAreaItems,
  collapseOnMobile,
}) => {
  return (
    <HeaderLayout
      leftAreaItems={
        <>
          {leftAreaItems}
          <h1 className="text-xl font-medium leading-[38px] dark:text-zinc-200">
            {header}
          </h1>
          {!isNil(badgeVal) && (
            <div className="ml-2">
              <RoundBadge>{badgeVal}</RoundBadge>
            </div>
          )}
        </>
      }
      rightAreaItems={rightAreaItems}
      collapseOnMobile={collapseOnMobile}
    />
  );
};

export default TopHeader;
