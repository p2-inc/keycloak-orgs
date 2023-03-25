import { FC, useState } from "react";
import Button from "../forms/buttons/button";

type Props = {
  label: string;
  value?: string;
  labelNumber?: number;
};

const CopyBlock: FC<Props> = ({ label, value, labelNumber }) => {
  const [copySuccess, setCopySuccess] = useState("Copy");

  const copyToClipBoard = async (copyMe) => {
    try {
      await navigator.clipboard.writeText(copyMe);
      setCopySuccess("Copied!");
    } catch (err) {
      setCopySuccess("Failed to copy!");
    } finally {
      setTimeout(() => setCopySuccess("Copy"), 5000);
    }
  };

  return (
    <div>
      <div className="space-y-2">
        <div className="flex items-center space-x-3">
          {labelNumber && <div className="font-semibold w-7 h-7 bg-gray-900 text-white items-center justify-center flex text-sm rounded-full flex-shrink-0 dark:bg-zinc-200 dark:text-p2dark-900">{labelNumber}</div>}
          <div className="font-semibold dark:text-zinc-200">{label}</div>
        </div>
        <div className="flex items-center justify-between space-x-10 rounded border p-2 dark:border-zinc-600 hover:border-gray-400 transition">
          <div className="break-all p-2 text-sm text-gray-800 dark:text-zinc-200">{value}</div>
          <Button onClick={() => copyToClipBoard(value)}>{copySuccess}</Button>
        </div>
      </div>
    </div>
  );
};

export default CopyBlock;
