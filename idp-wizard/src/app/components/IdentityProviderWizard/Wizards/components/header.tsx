import React, { CSSProperties, FC } from "react";
import {
  PageSection,
  PageSectionVariants,
  Flex,
  FlexItem,
  Button,
} from "@patternfly/react-core";
import { Link } from "react-router-dom";
import { useKeycloak } from "@react-keycloak/web";

interface Props {
  logo: string;
  logoStyles?: CSSProperties;
}

export const Header: FC<Props> = ({ logo, logoStyles = {} }) => {
  const { keycloak } = useKeycloak();

  return (
    <PageSection variant={PageSectionVariants.light}>
      <Flex alignItems={{ default: "alignItemsCenter" }}>
        <FlexItem>
          <img
            className="step-header-image"
            src={logo}
            alt="Logo"
            style={logoStyles}
          />
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
