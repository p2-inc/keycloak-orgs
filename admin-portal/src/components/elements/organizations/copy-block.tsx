import { FC, useState } from "react";
import Button from "../forms/buttons/button";

type Props = {
  label: string;
  value: string;
};

const CopyBlock: FC<Props> = ({ label, value }) => {
  const [copySuccess, setCopySuccess] = useState("Copy");

  const copyToClipBoard = async (copyMe) => {
    try {
      await navigator.clipboard.writeText(copyMe);
      setCopySuccess("Copied!");
    } catch (err) {
      setCopySuccess("Failed to copy!");
    }
  };

  return (
    <div className="items-center justify-between border-t border-t-gray-200 py-4 pt-6 md:flex">
      <div>
        <div className="text-sm font-semibold">{label}</div>
        <div className="overflow-x-hidden text-ellipsis py-2 text-xl font-medium">
          {value}
        </div>
      </div>
      <div>
        <Button onClick={() => copyToClipBoard(value)}>{copySuccess}</Button>
      </div>
    </div>
  );
};

export default CopyBlock;
