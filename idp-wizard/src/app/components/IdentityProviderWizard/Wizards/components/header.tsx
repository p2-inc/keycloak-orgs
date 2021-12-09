import React from "react";
import {
  PageSection,
  PageSectionVariants,
  Flex,
  FlexItem,
  Button,
} from "@patternfly/react-core";
import { Link } from "react-router-dom";
import { useKeycloak } from "@react-keycloak/web";

export const Header = ({ logo }) => {
  const { keycloak } = useKeycloak();

  return (
    <PageSection variant={PageSectionVariants.light}>
      <Flex>
        <FlexItem>
          <img className="step-header-image" src={logo} alt="Azure" />
        </FlexItem>

        <FlexItem align={{ default: "alignRight" }}>
          <Link to="/">
            <Button variant="link" isInline>
              My Dashboard
            </Button>
          </Link>
        </FlexItem>
        <FlexItem>
          <Button variant="link" isInline onClick={() => keycloak.logout()}>
            Logout
          </Button>
        </FlexItem>
      </Flex>
    </PageSection>
  );
};
