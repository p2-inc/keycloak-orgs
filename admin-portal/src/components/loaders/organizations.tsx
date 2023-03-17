import cs from "classnames";

const OrganizationsLoader: React.FC = () => {
  return (
    <div
      className={cs(
        "col-span-1 flex flex-col rounded-md border border-gray-200 bg-gray-50",
        "px-10 py-9",
        "space-y-6",
        "min-h-[179px]"
      )}
    >
      <div className="animate-pulse space-y-2">
        <div className="h-3 w-28 rounded-md bg-gray-300"></div>
        <div className="h-2 w-20 rounded-md bg-gray-300"></div>
      </div>
      <div className="grid animate-pulse grid-cols-2">
        <div className="space-y-2">
          <div className="h-5 w-5 rounded-md bg-gray-300"></div>
          <div className="h-2 w-8 rounded-md bg-gray-300"></div>
        </div>
        <div className="space-y-2">
          <div className="h-5 w-5 rounded-md bg-gray-300"></div>
          <div className="h-2 w-8 rounded-md bg-gray-300"></div>
        </div>
      </div>
    </div>
  );
};

export default OrganizationsLoader;
