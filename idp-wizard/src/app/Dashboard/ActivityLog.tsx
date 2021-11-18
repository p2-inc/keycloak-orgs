import React, { useEffect, useState } from "react";
import {
  TableComposable,
  Thead,
  Tbody,
  Tr,
  Th,
  Td,
  nowrap,
} from "@patternfly/react-table";
import { getEventData, IDashboardEvents } from "@app/services/DashboardData";

export function ActivityLog() {
  const columns = ["Time", "User", "Event Type", "Details"];
  const [activityData, setActivityData] = useState<IDashboardEvents[] | []>([]);

  useEffect(() => {
    getEventData().then((res) => setActivityData(res));
  }, []);

  return (
    <TableComposable aria-label="Misc table" className="card-shadow">
      <Thead noWrap>
        <Tr>
          <Th style={{ minWidth: "20%" }}>{columns[0]}</Th>
          <Th style={{ minWidth: "20%" }}>{columns[1]}</Th>
          <Th style={{ minWidth: "20%" }}>{columns[2]}</Th>
          <Th style={{ maxWidth: "40%" }}>{columns[3]}</Th>
        </Tr>
      </Thead>
      <Tbody>
        {activityData.map((row, rowIndex) => {
          return (
            <Tr key={rowIndex}>
              <td style={{ width: "20%", whiteSpace: "nowrap" }}>
                {new Date(row.time).toLocaleDateString()}{" "}
                {new Date(row.time).toLocaleTimeString()}
              </td>
              <td style={{ width: "20%" }}>{row.user}</td>
              <td style={{ width: "20%" }}>{row.eventType}</td>
              <td>{JSON.stringify(row.details)}</td>
            </Tr>
          );
        })}
      </Tbody>
    </TableComposable>
  );
}
