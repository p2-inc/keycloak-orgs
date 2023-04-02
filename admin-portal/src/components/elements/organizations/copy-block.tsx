import { FC, useState } from "react";
import { useTranslation } from "react-i18next";
import Button from "../forms/buttons/button";

type Props = {
  label: string;
  value?: string;
  labelNumber?: number;
};

const CopyBlock: FC<Props> = ({ label, value, labelNumber }) => {
  const { t } = useTranslation();
  const [copySuccess, setCopySuccess] = useState(t("copy"));

  const copyToClipBoard = async (copyMe) => {
    try {
      await navigator.clipboard.writeText(copyMe);
      setCopySuccess(t("copied"));
    } catch (err) {
      setCopySuccess(t("failedToCopy"));
    } finally {
      setTimeout(() => setCopySuccess(t("copy")), 5000);
    }
  };

  return (
    <div>
      <div className="space-y-2">
        <div className="flex items-center space-x-3">
          {labelNumber && (
            <div className="flex h-7 w-7 flex-shrink-0 items-center justify-center rounded-full bg-gray-900 text-sm font-semibold text-white dark:bg-zinc-200 dark:text-p2dark-900">
              {labelNumber}
            </div>
          )}
          <div className="font-semibold dark:text-zinc-200">{label}</div>
        </div>
        <div className="flex items-center justify-between space-x-10 rounded border p-2 transition hover:border-gray-400 dark:border-zinc-600">
          <div className="break-all p-2 text-sm text-gray-800 dark:text-zinc-200">
            {value}
          </div>
          <Button onClick={() => copyToClipBoard(value)}>{copySuccess}</Button>
        </div>
      </div>
    </div>
  );
};

export default CopyBlock;
