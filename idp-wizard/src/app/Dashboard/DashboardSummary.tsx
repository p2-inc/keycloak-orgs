import {
  getSummaryData,
  IDashboardSummaryData,
} from "@app/services/DashboardData";
import {
  Card,
  CardBody,
  CardTitle,
  TextContent,
  TextList,
  TextListItem,
  TextListItemVariants,
  TextListVariants,
  Title,
} from "@patternfly/react-core";
import React, { FC, useEffect, useState } from "react";

export const DashboardSummary: FC = () => {
  const [summaryData, setSummaryData] = useState<IDashboardSummaryData>({
    loginsToday: 0,
    loginsThisWeek: 0,
    users: 0,
    groups: 0,
    failedLogins: 0,
    usersLockedOut: 0,
  });

  useEffect(() => {
    getSummaryData().then((res) => setSummaryData(res));
  }, []);

  return (
    <Card className="card-shadow">
      <CardTitle>
        <Title headingLevel="h2" size="xl">
          Summary
        </Title>
      </CardTitle>
      <CardBody>
        <TextContent>
          <TextList component={TextListVariants.dl}>
            <TextListItem component={TextListItemVariants.dt}>
              Logins Today:
            </TextListItem>
            <TextListItem component={TextListItemVariants.dd}>
              {summaryData.loginsToday}
            </TextListItem>
            <TextListItem component={TextListItemVariants.dt}>
              Logins This Week:
            </TextListItem>
            <TextListItem component={TextListItemVariants.dd}>
              {summaryData.loginsThisWeek}
            </TextListItem>
            <TextListItem component={TextListItemVariants.dt}>
              Users:
            </TextListItem>
            <TextListItem component={TextListItemVariants.dd}>
              {summaryData.users}
            </TextListItem>
            <TextListItem component={TextListItemVariants.dt}>
              Groups:
            </TextListItem>
            <TextListItem component={TextListItemVariants.dd}>
              {summaryData.groups}
            </TextListItem>
            <TextListItem component={TextListItemVariants.dt}>
              Failed Logins:
            </TextListItem>
            <TextListItem component={TextListItemVariants.dd}>
              {summaryData.failedLogins}
            </TextListItem>
            <TextListItem component={TextListItemVariants.dt}>
              Users Locked Out:
            </TextListItem>
            <TextListItem component={TextListItemVariants.dd}>
              {summaryData.usersLockedOut}
            </TextListItem>
          </TextList>
        </TextContent>
      </CardBody>
    </Card>
  );
};
