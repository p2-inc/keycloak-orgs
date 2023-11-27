import { FC, ReactElement, useState } from "react";
import { ChevronIcon } from "../../../icons";
import cs from "classnames";

type DropdownItem = {
  content: ReactElement;
  value: string | string[];
  id: number;
};

type DropdownProps = {
  items: Array<DropdownItem>;
  emptyContent?: ReactElement;
  className?: string;
  onChange?;
};

const Dropdown: FC<DropdownProps> = ({
  items,
  emptyContent,
  className,
  onChange,
}) => {
  const [selectedItemIndex, setSelectedItemIndex] = useState(-1);
  const [isOpen, toggleIsOpen] = useState(false);

  const handleSelect = (index) => {
    setSelectedItemIndex(index);
    toggleIsOpen(false);
    onChange({ id: items[index].id, value: items[index].value });
  };

  return (
    <div className={cs("relative", className)}>
      <button
        onClick={() => toggleIsOpen(!isOpen)}
        className={cs(
          "flex w-full items-center justify-between space-x-3 rounded border bg-neutral-50 py-2 px-4 text-left",
          "transition",
          "hover:bg-white",
          {
            "border-p2primary-700": isOpen,
            "border-neutral-300": !isOpen,
          }
        )}
      >
        <div>
          {selectedItemIndex > -1 && (
            <div>{items[selectedItemIndex].content}</div>
          )}
          {selectedItemIndex === -1 && <div>{emptyContent}</div>}
        </div>
        <div>
          <ChevronIcon className="rotate-90 stroke-gray-800" />
        </div>
      </button>
      {isOpen && (
        <div className={cs("absolute z-50 -mt-px w-full")}>
          <div
            className={cs(
              "relative z-20 w-full divide-y rounded border border-neutral-300 bg-neutral-50"
            )}
          >
            {items.map((item, index) => (
              <button
                onClick={(e) => handleSelect(index)}
                key={index}
                className={cs(
                  "block w-full py-2 px-4 text-left",
                  "transition",
                  "hover:bg-white"
                )}
              >
                {item.content}
              </button>
            ))}
          </div>
          <div
            className={cs(
              "absolute inset-x-3 bottom-0 z-10 h-1/2 rounded-full bg-white drop-shadow-btn-light"
            )}
          ></div>
        </div>
      )}
    </div>
  );
};

export default Dropdown;
