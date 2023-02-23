import { FC, useState } from "react";
import { GridIcon, ListIcon } from "../../../icons";
import cs from "classnames";

type ViewSwitchProps = {
  onChange;
};

const ViewSwitchBtn = ({ children, isActive, onClick }) => (
  <div
    className={cs(
      "flex h-8 w-full md:w-10 cursor-pointer items-center justify-center rounded transition",
      {
        "bg-white shadow": isActive,
        "opacity-60 hover:opacity-100": !isActive,
      }
    )}
    onClick={onClick}
  >
    {children}
  </div>
);

const ViewSwitch: FC<ViewSwitchProps> = ({ onChange }) => {
  const [selectValue, setSelectValue] = useState("grid");

  const handleSelect = (name) => {
    setSelectValue(name);
    if (onChange) {
      onChange(name);
    }
  };

  return (
    <div className="flex rounded-md border border-gray-200 bg-gray-50 p-[2px] transition hover:border-gray-300 w-full md:w-auto">
      <ViewSwitchBtn
        isActive={selectValue === "grid"}
        onClick={() => handleSelect("grid")}
      >
        <GridIcon className="h-4 w-4 stroke-gray-800" />
      </ViewSwitchBtn>
      <ViewSwitchBtn
        isActive={selectValue === "list"}
        onClick={() => handleSelect("list")}
      >
        <ListIcon className="h-4 w-4 stroke-gray-800" />
      </ViewSwitchBtn>
    </div>
  );
};

export default ViewSwitch;
