import {
  CheckCircleIcon,
  ExclamationTriangleIcon,
  InformationCircleIcon,
  XCircleIcon,
} from "@heroicons/react/24/outline";
import cs from "classnames";

type Props = {
  title?: string;
  body?: string;
  type?: "warning" | "danger" | "info";
};

const Alert: React.FC<Props> = ({ title, body, type = "success" }) => {
  let color = "green";

  if (type === "warning") {
    color = "yellow";
  }
  if (type === "danger") {
    color = "red";
  }
  if (type === "info") {
    color = "blue";
  }

  return (
    <div
      className={cs(`rounded-md p-4`, {
        "bg-green-50": color === "green",
        "bg-yellow-50": color === "yellow",
        "bg-red-50": color === "red",
        "bg-blue-50": color === "blue",
      })}
    >
      <div className="flex">
        <div className="flex-shrink-0">
          {type === "success" && (
            <CheckCircleIcon
              className={`h-5 w-5 text-green-400`}
              aria-hidden="true"
            />
          )}
          {type === "warning" && (
            <ExclamationTriangleIcon
              className={`h-5 w-5 text-yellow-400`}
              aria-hidden="true"
            />
          )}
          {type === "info" && (
            <InformationCircleIcon
              className={`h-5 w-5 text-blue-400`}
              aria-hidden="true"
            />
          )}
          {type === "danger" && (
            <XCircleIcon
              className={`h-5 w-5 text-red-400`}
              aria-hidden="true"
            />
          )}
        </div>
        <div className="ml-3">
          {title && (
            <h3
              className={cs(`text-sm font-medium`, {
                "text-green-800": color === "green",
                "text-yellow-800": color === "yellow",
                "text-red-800": color === "red",
                "text-blue-800": color === "blue",
              })}
            >
              {title}
            </h3>
          )}
          {body && (
            <div
              className={cs(`mt-2 text-sm`, {
                "text-green-700": color === "green",
                "text-yellow-700": color === "yellow",
                "text-red-700": color === "red",
                "text-blue-700": color === "blue",
              })}
            >
              <p>{body}</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default Alert;
