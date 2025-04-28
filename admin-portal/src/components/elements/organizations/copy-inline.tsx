import { FC, useState } from "react";
import { useTranslation } from "react-i18next";
import Button from "../forms/buttons/button";
import { CopyIcon } from "lucide-react";
import { CopyCheckIcon } from "components/icons/copy-check";
import { CopyXIcon } from "components/icons/copy-x";

type Props = {
  label: string;
  value?: string;
  labelNumber?: number;
};

const CopyInline: FC<Props> = ({ label, value, labelNumber }) => {
  const { t } = useTranslation();
  const [copySuccess, setCopySuccess] = useState("copy");

  const copyToClipBoard = async (copyMe) => {
    try {
      await navigator.clipboard.writeText(copyMe);
      setCopySuccess("copySuccess");
    } catch (err) {
      setCopySuccess("copyError");
    } finally {
      setTimeout(() => setCopySuccess(t("copy")), 5000);
    }
  };

  let copyIconToShow = <CopyIcon className="h-5 w-5" />;
  if (copySuccess === "copySuccess") {
    copyIconToShow = <CopyCheckIcon className="h-5 w-5" />;
  } else if (copySuccess === "copyError") {
    copyIconToShow = <CopyXIcon className="h-5 w-5" />;
  }

  return (
    <div>
      <div className="space-y-2">
        <div className="flex items-center space-x-3">
          <div className="font-semibold dark:text-zinc-200">{label}</div>
        </div>
        <div className="flex items-center space-x-1">
          <div
            className="flex max-w-[500px] items-center justify-between space-x-10 rounded border p-2 transition hover:border-gray-400 dark:border-zinc-600"
            onClick={() => copyToClipBoard(value)}
          >
            <div className="overflow-hidden text-ellipsis whitespace-nowrap break-all  font-mono text-sm text-gray-800 dark:text-zinc-200">
              {value}
            </div>
          </div>
          <Button onClick={() => copyToClipBoard(value)}>
            {copyIconToShow}
          </Button>
        </div>
      </div>
    </div>
  );
};

export default CopyInline;
