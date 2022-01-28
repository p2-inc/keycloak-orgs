import {
  getSummaryData,
  IDashboardSummaryData,
} from "@app/services/DashboardData";
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
import React, { FC, useEffect, useRef, useState } from "react";

export const DashboardSummary: FC = () => {
  const isMounted = useRef(false);
  const [loading, setLoading] = useState(true);
  const [summaryData, setSummaryData] = useState<IDashboardSummaryData>({
    loginsToday: 0,
    loginsThisWeek: 0,
    users: 0,
    groups: 0,
    failedLogins: 0,
    usersLockedOut: 0,
  });

  useEffect(() => {
    isMounted.current = true;
    getSummaryData().then((res) => {
      if (isMounted.current) {
        setSummaryData(res);
        setLoading(false);
      }
    });
    return () => {
      isMounted.current = false;
    };
  }, []);

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
