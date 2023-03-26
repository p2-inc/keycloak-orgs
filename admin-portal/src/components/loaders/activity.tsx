const ActivityLoader: React.FC = () => {
  return (
    <div className="animate-pulse">
      <div className="md:flex md:items-center">
        <div className="p-4 pr-0 pb-0">
          <div className="h-4 w-4 rounded-md bg-gray-300 dark:bg-zinc-600"></div>
        </div>
        <div className="p-4 pb-0 text-sm font-semibold text-p2gray-900 md:pl-2">
          <div className="h-4 w-14 rounded-md bg-gray-300 dark:bg-zinc-600"></div>
        </div>
      </div>
      <div className="md:grid md:grid-cols-5">
        <div className="space-y-1 p-4">
          <div className="text-xs font-medium leading-3 text-p2gray-800">
            <div className="h-3 w-28 rounded-md bg-gray-300 dark:bg-zinc-600"></div>
          </div>
          <div className="text-xs leading-3 text-p2gray-800/50">
            <div className="h-2 w-20 rounded-md bg-gray-300 dark:bg-zinc-600"></div>
          </div>
        </div>
        <div className="space-y-1 p-4">
          <div className="text-xs font-medium leading-3 text-p2gray-800">
            <div className="h-3 w-28 rounded-md bg-gray-300 dark:bg-zinc-600"></div>
          </div>
          <div className="text-xs leading-3 text-p2gray-800/50">
            <div className="h-2 w-20 rounded-md bg-gray-300 dark:bg-zinc-600"></div>
          </div>
        </div>
        <div className="space-y-1 p-4">
          <div className="text-xs font-medium leading-3 text-p2gray-800">
            <div className="h-3 w-28 rounded-md bg-gray-300 dark:bg-zinc-600"></div>
          </div>
          <div className="text-xs leading-3 text-p2gray-800/50">
            <div className="h-2 w-20 rounded-md bg-gray-300 dark:bg-zinc-600"></div>
          </div>
        </div>
        <div className="space-y-1 p-4">
          <div className="text-xs font-medium leading-3 text-p2gray-800">
            <div className="h-3 w-28 rounded-md bg-gray-300 dark:bg-zinc-600"></div>
          </div>
          <div className="text-xs leading-3 text-p2gray-800/50">
            <div className="h-2 w-20 rounded-md bg-gray-300 dark:bg-zinc-600"></div>
          </div>
        </div>
        <div className="space-y-1 p-4">
          <div className="text-xs font-medium leading-3 text-p2gray-800">
            <div className="h-3 w-28 rounded-md bg-gray-300 dark:bg-zinc-600"></div>
          </div>
          <div className="text-xs leading-3 text-p2gray-800/50">
            <div className="h-2 w-20 rounded-md bg-gray-300 dark:bg-zinc-600"></div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ActivityLoader;
