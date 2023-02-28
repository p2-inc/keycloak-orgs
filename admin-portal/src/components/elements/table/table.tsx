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
}[];

export type TableRows = {
  [key: string]: any;
}[];

type Props = {
  columns: TableColumns;
  rows: TableRows;
};

const Table: React.FC<Props> = ({ columns, rows }) => {
  return (
    <div className="overflow-auto rounded-md border border-gray-200">
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
                  className={cs({
                    [firstTdClasses]: index === 0,
                    [tdClasses]: index > 0,
                  })}
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
