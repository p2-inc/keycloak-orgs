import * as React from "react";
import {
  PageSection,
  Title,
  Button,
  Stack,
  StackItem,
  Flex,
  FlexItem,
} from "@patternfly/react-core";
import { useKeycloak } from "@react-keycloak/web";
import { DashboardSummary } from "./DashboardSummary";
import { ConnectionStatus } from "./ConnectionStatus";
import { ActivityLog } from "./ActivityLog";
import { useParams } from "react-router";
import { generatePath, Link } from "react-router-dom";
import { PATHS } from "@app/routes";
import { useTitle } from "react-use";
import { useRoleAccess } from "@app/hooks";

const Dashboard: React.FunctionComponent = () => {
  const { keycloak } = useKeycloak();
  let { realm } = useParams();

  const [hasAccess] = useRoleAccess();

  useTitle("Dashboard | PhaseTwo");

  return (
    <PageSection>
      <Stack hasGutter>
        <StackItem>
          <Flex>
            <FlexItem>
              <Title headingLevel="h1" size="3xl">
                Dashboard
              </Title>
            </FlexItem>
            <FlexItem align={{ default: "alignRight" }}>
              <Link
                to={generatePath(PATHS.idpSelector, {
                  realm,
                })}
              >
                IDP Selector
              </Link>
            </FlexItem>
            <FlexItem>
              <Button variant="link" isInline onClick={() => keycloak.logout()}>
                Logout
              </Button>
            </FlexItem>
          </Flex>
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
