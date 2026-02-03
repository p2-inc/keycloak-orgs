import { useRoleAccess } from "@app/hooks";
import { usePageTitle } from "@app/hooks/useTitle";
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
import { MainNav } from "../navigation";
import { ActivityLog } from "./ActivityLog";
import { ConnectionStatus } from "./ConnectionStatus";
import { DashboardSummary } from "./DashboardSummary";

const Dashboard: React.FunctionComponent = () => {
  usePageTitle("Dashboard");
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
