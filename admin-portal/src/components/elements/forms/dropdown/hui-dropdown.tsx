import { Listbox, Transition } from "@headlessui/react";
import { CheckIcon, ChevronUpDownIcon } from "@heroicons/react/20/solid";
import { Fragment } from "react";

type DDItem = {
  id: string;
  name: string;
  disabled?: boolean;
};

type Props = {
  name: string;
  items: DDItem[];
  selectedItem: DDItem | undefined;
  onChange: (DDItem) => void;
  listBoxProps?: any;
};

const DropDown: React.FC<Props> = ({
  items,
  selectedItem,
  onChange,
  name,
  listBoxProps = {},
}) => {
  return (
    <Listbox
      value={selectedItem}
      onChange={onChange}
      name={name}
      {...listBoxProps}
    >
      <div className="relative mt-1">
        <Listbox.Button className="relative w-full cursor-default rounded border bg-white py-2 pl-3 pr-10 text-left focus:outline-none focus-visible:border-indigo-500 focus-visible:ring-2 focus-visible:ring-white focus-visible:ring-opacity-75 focus-visible:ring-offset-2 focus-visible:ring-offset-p2blue-100 sm:text-sm">
          <span className="block truncate">{selectedItem?.name}</span>
          <span className="pointer-events-none absolute inset-y-0 right-0 flex items-center pr-2">
            <ChevronUpDownIcon
              className="h-5 w-5 text-gray-400"
              aria-hidden="true"
            />
          </span>
        </Listbox.Button>
        <Transition
          as={Fragment}
          leave="transition ease-in duration-100"
          leaveFrom="opacity-100"
          leaveTo="opacity-0"
        >
          <Listbox.Options className="absolute z-50 mt-1 max-h-60 w-full overflow-auto rounded-md bg-white py-1 text-base ring-1 ring-black ring-opacity-5 focus:outline-none sm:text-sm">
            {items.map((item, itemIdx) => (
              <Listbox.Option
                key={itemIdx}
                className={({ active, disabled }) =>
                  `relative cursor-default select-none py-2 pl-10 pr-4 ${
                    active ? "bg-p2blue-200 text-p2blue-700" : "text-gray-900"
                  } ${disabled && "bg-gray-100 opacity-50"}`
                }
                value={item}
                disabled={item.disabled}
              >
                {({ selected }) => (
                  <>
                    <span
                      className={`block truncate ${
                        selected ? "font-medium" : "font-normal"
                      }`}
                    >
                      {item.name}
                    </span>
                    {selected ? (
                      <span className="absolute inset-y-0 left-0 flex items-center pl-3 text-p2blue-500">
                        <CheckIcon className="h-5 w-5" aria-hidden="true" />
                      </span>
                    ) : null}
                  </>
                )}
              </Listbox.Option>
            ))}
          </Listbox.Options>
        </Transition>
      </div>
    </Listbox>
  );
};

export default DropDown;
