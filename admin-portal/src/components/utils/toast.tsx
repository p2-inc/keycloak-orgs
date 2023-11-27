import toast from "react-hot-toast";
import {
  CheckCircleIcon,
  XCircleIcon,
  InformationCircleIcon,
} from "@heroicons/react/24/outline";
import { XMarkIcon } from "@heroicons/react/20/solid";
import { useTranslation } from "react-i18next";

type Props = {
  success?: boolean;
  error?: boolean;
  information?: boolean;
  title: string;
  message?: string;
  duration?: number;
};

const CloseButton = () => {
  const { t } = useTranslation();
  return <span className="sr-only">{t("close")}</span>;
};

export default function P2Toast({
  success,
  error,
  information,
  title,
  message,
  duration,
}: Props) {
  return toast.custom((to) => {
    if (duration) to.duration = duration;

    return (
      <div
        className={`${
          to.visible ? "animate-enter" : "animate-leave"
        } pointer-events-auto w-full max-w-sm overflow-hidden rounded-lg bg-white shadow-lg ring-1 ring-black ring-opacity-5`}
      >
        <div className="p-4">
          <div className="flex items-start">
            <div className="flex-shrink-0">
              {success && (
                <CheckCircleIcon
                  className="h-6 w-6 text-green-400"
                  aria-hidden="true"
                />
              )}
              {information && (
                <InformationCircleIcon
                  className="h-6 w-6 text-p2primary-500"
                  aria-hidden="true"
                />
              )}
              {error && (
                <XCircleIcon
                  className="h-6 w-6 text-red-400"
                  aria-hidden="true"
                />
              )}
            </div>
            <div className="ml-3 w-0 flex-1 pt-0.5">
              {title && (
                <p className="text-sm font-medium text-gray-900">{title}</p>
              )}
              {message && (
                <p className="mt-1 text-sm text-gray-500">{message}</p>
              )}
            </div>
            <div className="ml-4 flex flex-shrink-0">
              <button
                type="button"
                className="inline-flex rounded-md bg-white text-gray-400 hover:text-gray-500 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2"
                onClick={() => {
                  toast.dismiss(to.id);
                }}
              >
                <CloseButton />
                <XMarkIcon className="h-5 w-5" aria-hidden="true" />
              </button>
            </div>
          </div>
        </div>
      </div>
    );
  });
}
