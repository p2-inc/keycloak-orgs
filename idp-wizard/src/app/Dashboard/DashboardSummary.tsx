import { useSummaryData } from "@app/services/DashboardData";
import {
  Card,
  CardBody,
  CardTitle,
  Spinner,
  TextContent,
  TextList,
  TextListItem,
  TextListItemVariants,
  TextListVariants,
  Title,
} from "@patternfly/react-core";
import React, { FC } from "react";

export const DashboardSummary: FC = () => {
  const {
    loginsToday,
    loginsThisWeek,
    users,
    groups,
    failedLogins,
    usersLockedOut,
    loading,
  } = useSummaryData();

  return (
    <Card className="card-shadow">
      <CardTitle>
        <div className="pf-u-display-flex pf-u-justify-content-flex-start pf-u-align-items-center">
          <Title headingLevel="h2" size="xl">
            Summary
          </Title>
          {loading && <Spinner isSVG size="lg" className="pf-u-ml-md" />}
        </div>
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
};
