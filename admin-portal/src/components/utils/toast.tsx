import toast from "react-hot-toast";
import { CheckCircleIcon, XCircleIcon } from "@heroicons/react/24/outline";
import { XMarkIcon } from "@heroicons/react/20/solid";

type Props = {
  success?: boolean;
  error?: boolean;
  title: string;
  message?: string;
  duration?: number;
};

export default function P2Toast({
  success,
  error,
  title,
  message,
  duration,
}: Props) {
  return toast.custom((t) => {
    if (duration) t.duration = duration;

    return (
      <div
        className={`${
          t.visible ? "animate-enter" : "animate-leave"
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
                  toast.dismiss(t.id);
                }}
              >
                <span className="sr-only">Close</span>
                <XMarkIcon className="h-5 w-5" aria-hidden="true" />
              </button>
            </div>
          </div>
        </div>
      </div>
    );
  });
}
