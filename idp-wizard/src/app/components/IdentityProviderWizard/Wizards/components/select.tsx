import {
  Select,
  SelectOption,
  SelectOptionObject,
  SelectOptionProps,
  SelectProps,
  SelectVariant,
} from "@patternfly/react-core";
import React, { FC, useState } from "react";

type Props = {
  options: SelectOptionProps[];
  handleSelect: (SelectOptionObject) => void;
  placeholder?: String;
  selections?: SelectOptionObject;
};

export const CustomSelect: FC<Props> = ({
  options,
  handleSelect: handleSelectProp,
  placeholder = "",
  selections: selectionsProp,
}) => {
  const [selectOpen, setSelectOpen] = useState(false);
  const [selected, setSelected] = useState<SelectOptionObject | null>(
    selectionsProp
  );

  const clearSelection = () => {
    setSelectOpen(false);
    setSelected(null);
    handleSelectProp(null);
  };

  const handleSelect: SelectProps["onSelect"] = (e, sel, isPlaceholder) => {
    if (isPlaceholder) clearSelection();
    else {
      setSelected(sel);
      handleSelectProp(sel);
      setSelectOpen(false);
    }
  };

  return (
    <Select
      variant={SelectVariant.typeahead}
      aria-label={placeholder}
      placeholderText={placeholder}
      onToggle={setSelectOpen}
      onSelect={handleSelect}
      onClear={clearSelection}
      selections={selected}
      isOpen={selectOpen}
      aria-labelledby={"server-vendor-select"}
    >
      {options.map((opt, i) => (
        <SelectOption key={i} {...opt} />
      ))}
    </Select>
  );
};
