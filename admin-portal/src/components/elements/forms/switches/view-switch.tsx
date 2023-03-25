import { FC, useState } from "react";
import { GridIcon, ListIcon } from "../../../icons";
import cs from "classnames";
import { Grid, List } from "lucide-react";

type ViewSwitchProps = {
  onChange?: (name: ViewLayoutOptions) => void;
};

const ViewSwitchBtn = ({ children, isActive, onClick }) => (
  <div
    className={cs(
      "flex h-8 w-full cursor-pointer items-center justify-center rounded transition md:w-10",
      {
        "bg-white shadow dark:bg-zinc-600": isActive,
        "opacity-60 hover:opacity-100": !isActive,
      }
    )}
    onClick={onClick}
  >
    {children}
  </div>
);

export enum ViewLayoutOptions {
  GRID = "grid",
  LIST = "list",
}

const ViewSwitch: FC<ViewSwitchProps> = ({ onChange }) => {
  const [selectValue, setSelectValue] = useState<ViewLayoutOptions>(
    ViewLayoutOptions.GRID
  );

  const handleSelect = (name: ViewLayoutOptions) => {
    setSelectValue(name);
    if (onChange) {
      onChange(name);
    }
  };

  return (
    <div className="flex w-full rounded-md border border-gray-200 bg-gray-50 p-[2px] transition hover:border-gray-300 md:w-auto dark:bg-p2dark-1000 dark:border-zinc-600">
      <ViewSwitchBtn
        isActive={selectValue === ViewLayoutOptions.GRID}
        onClick={() => handleSelect(ViewLayoutOptions.GRID)}
      >
        <Grid className="h-5 w-5 dark:text-zinc-200" />
      </ViewSwitchBtn>
      <ViewSwitchBtn
        isActive={selectValue === ViewLayoutOptions.LIST}
        onClick={() => handleSelect(ViewLayoutOptions.LIST)}
      >
        <List className="h-5 w-5 dark:text-zinc-200" />
      </ViewSwitchBtn>
    </div>
  );
};

export default ViewSwitch;
