import { FC, ReactElement } from "react";
import RoundBadge from "../elements/badges/round-badge";

type Props = {
  header: string;
  badgeVal?: string;
  rightAreaItems?: ReactElement | ReactElement[];
};

const TopHeader: FC<Props> = ({ header, badgeVal, rightAreaItems }) => {
  return (
    <div className="flex px-4 py-4 md:px-10 md:py-6">
      <div className="flex items-center">
        <h1 className="text-2xl font-medium">{header}</h1>
        {badgeVal && (
          <div className="ml-2">
            <RoundBadge>{badgeVal}</RoundBadge>
          </div>
        )}
      </div>
      <div className="flex flex-grow items-center justify-end gap-2">
        {rightAreaItems}
      </div>
    </div>
  );
};

export default TopHeader;
