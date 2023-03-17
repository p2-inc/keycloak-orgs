import cs from "classnames";

export const firstThClasses =
  "py-3.5 pl-4 pr-3 text-left font-medium text-black sm:pl-6";
export const thClasses = "px-3 py-3.5 text-left font-medium text-black";
export const firstTdClasses =
  "whitespace-normal py-4 pl-4 pr-3 font-medium text-black sm:pl-6";
export const tdClasses = "whitespace-normal px-3 py-4";

export type TableColumns = {
  key: string;
  data: string | React.ReactElement;
  columnClasses?: string;
}[];

export type TableRows = {
  [key: string]: any;
}[];

type Props = {
  columns: TableColumns;
  rows: TableRows;
  isLoading?: boolean;
};

const loadingState = (columns) => {
  return (
    <div className="overflow-auto rounded-md border border-gray-200">
      <table className="min-w-full divide-y divide-gray-200 bg-gray-50 text-sm font-medium text-black">
        <thead className="animate-pulse">
          <tr>
            {columns.map(() => (
              <th className="p-4">
                <div className="h-4 w-1/4 rounded-md bg-gray-300"></div>
              </th>
            ))}
          </tr>
        </thead>
        <tbody className="animate-pulse">
          <tr>
            {columns.map(() => (
              <td className="p-4">
                <div className="h-4 w-1/2 rounded-md bg-gray-300"></div>
              </td>
            ))}
          </tr>
        </tbody>
      </table>
    </div>
  );
};

const Table: React.FC<Props> = ({ columns, rows, isLoading }) => {
  if (isLoading) {
    return loadingState(columns);
  }
  return (
    <div className="overflow-auto rounded-md border border-gray-200 md:overflow-visible">
      <table className="min-w-full divide-y divide-gray-200 rounded-md  bg-gray-50 text-sm font-medium text-black">
        <thead>
          <tr>
            {columns.map((column, index) => (
              <th
                className={cs({
                  [firstThClasses]: index === 0,
                  [thClasses]: index > 0,
                })}
                key={column.key}
              >
                {column.data}
              </th>
            ))}
          </tr>
        </thead>
        <tbody className="divide-y divide-gray-200">
          {rows.map((row, index) => (
            <tr key={index}>
              {columns.map((column, index) => (
                <td
                  className={cs(
                    {
                      [firstTdClasses]: index === 0,
                      [tdClasses]: index > 0,
                    },
                    column.columnClasses
                  )}
                  key={column.key}
                >
                  {row[column.key]}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default Table;
