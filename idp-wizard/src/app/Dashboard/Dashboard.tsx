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
import { useHistory } from "react-router";

const Dashboard: React.FunctionComponent = () => {
  const history = useHistory();
  const { keycloak } = useKeycloak();

  const goToIDPSelector = () => {
    let path = `idp`;
    history.push(path);
  };
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
              <Button variant="link" isInline onClick={goToIDPSelector}>
                IDP Selector
              </Button>
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
