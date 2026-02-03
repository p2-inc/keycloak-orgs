export const Loader = () => {
  return (
    <div className="flex justify-between space-x-2 py-3">
      <div className="flex space-x-2">
        <div className="animate-pulse">
          <div className="h-4 w-4 rounded-md bg-gray-300"></div>
        </div>
        <div className="animate-pulse">
          <div className="h-4 w-20 rounded-md bg-gray-300"></div>
        </div>
      </div>
      <div className="animate-pulse">
        <div className="h-4 w-10 rounded-md bg-gray-300"></div>
      </div>
    </div>
  );
};
