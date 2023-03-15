import { FC, ReactElement } from "react";
import RoundBadge from "../elements/badges/round-badge";
import HeaderLayout from "./components/header-layout";

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
          <h1 className="text-xl font-medium leading-[38px]">{header}</h1>
          {badgeVal && (
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
