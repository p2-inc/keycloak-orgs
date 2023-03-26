import { TableRows } from "./table";

type Props = {
  rows: TableRows;
  isLoading?: boolean;
};

const loading = (
  <div className="space-y-2 p-4">
    <div className="h-4 w-20 rounded-md bg-gray-300 dark:bg-zinc-600"></div>
    <div className="h-4 w-40 rounded-md bg-gray-300 dark:bg-zinc-600"></div>
  </div>
);

const MembersTable: React.FC<Props> = ({ rows, isLoading }) => {
  return (
    <div className="rounded border border-gray-200 dark:border-zinc-600">
      {isLoading && loading}
      {!isLoading && (
        <>
          <div className="divide-y md:hidden">
            {rows.map((item) => (
              <div className="p-4" key={item["email"]}>
                <div className="text-sm font-semibold dark:text-zinc-200">{item["name"]}</div>
                <div className="text-sm text-gray-500 dark:text-zinc-500">{item["email"]}</div>
                <div className="space-y-1 py-2">{item["roles"]}</div>
                <div>{item["action"]}</div>
              </div>
            ))}
          </div>
          <table className="hidden w-full md:table">
            <tbody className="divide-y dark:divide-zinc-600">
              {rows.map((item) => (
                <tr key={item["email"]}>
                  <td className="px-5 py-4 align-middle">
                    <div className="text-sm font-semibold dark:text-zinc-200">{item["name"]}</div>
                    <div className="text-sm text-gray-500 dark:text-zinc-500">{item["email"]}</div>
                  </td>
                  <td className="space-x-2 px-5 py-4 text-right align-middle">
                    {item["roles"]}
                  </td>
                  <td className="px-1 py-4 align-middle">{item["action"]}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </>
      )}
    </div>
  );
};

export default MembersTable;
