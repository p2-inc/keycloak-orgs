import { FC, useState } from "react";
import Button from "../forms/buttons/button";

type Props = {
  label: string;
  value?: string;
};

const CopyBlock: FC<Props> = ({ label, value }) => {
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
    <div className="space-x-2 border-t border-t-gray-200 py-4 pt-6">
      <div>
        <div className="text-sm font-semibold">{label}</div>
        <div className="flex items-center space-x-2 justify-between">
          <div className="overflow-x-auto py-2 text-xl font-medium">
            {value}
          </div>
          <Button onClick={() => copyToClipBoard(value)}>{copySuccess}</Button>
        </div>
      </div>
    </div>
  );
};

export default CopyBlock;
