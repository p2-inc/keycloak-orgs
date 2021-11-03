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
import React from "react";

export function DashboardSummary() {
  const loginsToday = 100;
  const loginsThisWeek = 500;
  const users = 20;
  const groups = 3;
  const failedLogins = 6;
  const usersLockedOut = 2;

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
              {loginsToday}
            </TextListItem>
            <TextListItem component={TextListItemVariants.dt}>
              Logins This Week:
            </TextListItem>
            <TextListItem component={TextListItemVariants.dd}>
              {loginsThisWeek}
            </TextListItem>
            <TextListItem component={TextListItemVariants.dt}>
              Users:
            </TextListItem>
            <TextListItem component={TextListItemVariants.dd}>
              {users}
            </TextListItem>
            <TextListItem component={TextListItemVariants.dt}>
              Groups:
            </TextListItem>
            <TextListItem component={TextListItemVariants.dd}>
              {groups}
            </TextListItem>
            <TextListItem component={TextListItemVariants.dt}>
              Failed Logins:
            </TextListItem>
            <TextListItem component={TextListItemVariants.dd}>
              {failedLogins}
            </TextListItem>
            <TextListItem component={TextListItemVariants.dt}>
              Users Locked Out:
            </TextListItem>
            <TextListItem component={TextListItemVariants.dd}>
              {usersLockedOut}
            </TextListItem>
          </TextList>
        </TextContent>
      </CardBody>
    </Card>
  );
}
