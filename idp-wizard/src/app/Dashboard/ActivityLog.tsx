import React from "react";
import {
  TableComposable,
  Thead,
  Tbody,
  Tr,
  Th,
  Td,
} from "@patternfly/react-table";

export function ActivityLog() {
  const columns = ["Time", "User", "Event Type", "Details"];
  const [rows, setRows] = React.useState([
    {
      cells: ["10/5/2021", "Garth Patil", "LOGIN_ERROR", ""],
      isRowSelected: false,
    },
    {
      cells: ["10/6/2021", "Martin Bak", "LOGIN_ERROR", ""],
      isRowSelected: false,
    },
  ]);
  const onRowClick = (event, rowIndex, row) => {
    const updatedRows = [...rows];
    updatedRows[rowIndex].isRowSelected = !rows[rowIndex].isRowSelected;
    setRows(updatedRows);
  };

  return (
    <TableComposable aria-label="Misc table" className="card-shadow">
      <Thead noWrap>
        <Tr>
          <Th>{columns[0]}</Th>
          <Th>{columns[1]}</Th>
          <Th>{columns[2]}</Th>
          <Th>{columns[3]}</Th>
        </Tr>
      </Thead>
      <Tbody>
        {rows.map((row, rowIndex) => {
          return (
            <Tr
              key={rowIndex}
              onRowClick={(event) => onRowClick(event, rowIndex, row.cells)}
              isHoverable
              isRowSelected={row.isRowSelected}
            >
              {row.cells.map((cell, cellIndex) => {
                return (
                  <Td
                    key={`${rowIndex}_${cellIndex}`}
                    dataLabel={columns[cellIndex]}
                  >
                    {cell}
                  </Td>
                );
              })}
            </Tr>
          );
        })}
      </Tbody>
    </TableComposable>
  );
}
