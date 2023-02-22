import { FC, ReactElement } from "react";
import RoundBadge from "../elements/badges/round-badge";

type Props = {
  header: string;
  badgeVal?: string;
  rightAreaItems?: ReactElement | ReactElement[];
};

const TopHeader: FC<Props> = ({ header, badgeVal, rightAreaItems }) => {
  return (
    <div className="flex px-4 py-4 flex-col md:flex-row md:px-10 md:py-6 space-y-4 md:space-y-0">
      <div className="flex items-center justify-between md:justify-start">
        <h1 className="text-2xl font-medium">{header}</h1>
        {badgeVal && (
          <div className="ml-2">
            <RoundBadge>{badgeVal}</RoundBadge>
          </div>
        )}
      </div>
      <div className="flex flex-grow items-center justify-end gap-2 flex-col-reverse md:flex-row">
        {rightAreaItems}
      </div>
    </div>
  );
};

export default TopHeader;
