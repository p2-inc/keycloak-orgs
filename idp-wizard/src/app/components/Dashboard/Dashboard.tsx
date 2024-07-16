import { useRoleAccess } from "@app/hooks";
import { useGetFeatureFlagsQuery } from "@app/services";
import Loading from "@app/utils/Loading";
import {
  Flex,
  FlexItem,
  PageSection,
  Stack,
  StackItem,
} from "@patternfly/react-core";
import * as React from "react";
import { useTitle } from "react-use";
import { MainNav } from "../navigation";
import { ActivityLog } from "./ActivityLog";
import { ConnectionStatus } from "./ConnectionStatus";
import { DashboardSummary } from "./DashboardSummary";
import { Navigate, generatePath, useParams } from "react-router-dom";
import { PATHS } from "@app/routes";

const Dashboard: React.FunctionComponent = () => {
  useTitle("Dashboard | Phase Two");
  const { navigateToAccessDenied } = useRoleAccess();
  const { data: featureFlags, isLoading: isLoadingFeatureFlags } =
    useGetFeatureFlagsQuery();

  if (isLoadingFeatureFlags) {
    return <Loading />;
  } else {
    if (!featureFlags?.enableDashboard) {
      navigateToAccessDenied();
      return <Loading />;
    }
  }

  return (
    <PageSection>
      <Stack hasGutter>
        <StackItem>
          <MainNav title="Dashboard" />
        </StackItem>
        <StackItem>
          <Flex>
            <FlexItem flex={{ default: "flex_2" }}>
              <DashboardSummary />
            </FlexItem>
            <FlexItem flex={{ default: "flex_2" }}>
              <ConnectionStatus />
            </FlexItem>
          </Flex>
        </StackItem>
        <StackItem isFilled>
          <Flex>
            <FlexItem flex={{ default: "flex_1" }}>
              <ActivityLog />
            </FlexItem>
          </Flex>
        </StackItem>
      </Stack>
    </PageSection>
  );
};

export { Dashboard };
