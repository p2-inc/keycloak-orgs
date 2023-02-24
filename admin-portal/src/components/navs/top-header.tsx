import { FC, ReactElement } from "react";
import RoundBadge from "../elements/badges/round-badge";
import HeaderLayout from "./components/header-layout";

type Props = {
  header: string;
  badgeVal?: string | number;
  rightAreaItems?: ReactElement | ReactElement[];
};

const TopHeader: FC<Props> = ({ header, badgeVal, rightAreaItems }) => {
  return (
    <HeaderLayout
      leftAreaItems={
        <>
          <h1 className="text-2xl font-medium">{header}</h1>
          {badgeVal && (
            <div className="ml-2">
              <RoundBadge>{badgeVal}</RoundBadge>
            </div>
          )}
        </>
      }
      rightAreaItems={rightAreaItems}
    />
  );
};

export default TopHeader;
