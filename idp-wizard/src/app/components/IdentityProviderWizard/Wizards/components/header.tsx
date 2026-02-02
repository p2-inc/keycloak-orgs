import React, { CSSProperties, FC } from "react";
import {
  PageSection,
  PageSectionVariants,
  Flex,
  FlexItem,
  Button,
} from "@patternfly/react-core";
import { generatePath, Link, useParams } from "react-router-dom";
import { PATHS } from "@app/routes";
import { useGetFeatureFlagsQuery } from "@app/services";

interface Props {
  logo: string;
  logoStyles?: CSSProperties;
}

export const Header: FC<Props> = ({ logo, logoStyles = {} }) => {
  const { realm } = useParams();
  const { data: featureFlags } = useGetFeatureFlagsQuery();

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
          <Link to={generatePath(PATHS.idpSelector, { realm })}>
            <Button variant="link" isInline>
              Identity Provider Selector
            </Button>
          </Link>
        </FlexItem>
        {featureFlags?.enableDashboard && (
          <FlexItem>
            <Link to={generatePath(PATHS.dashboard, { realm })}>
              <Button variant="link" isInline>
                Dashboard
              </Button>
            </Link>
          </FlexItem>
        )}
      </Flex>
    </PageSection>
  );
};
