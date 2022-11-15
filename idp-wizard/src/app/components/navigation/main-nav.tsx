import { useGetFeatureFlagsQuery } from "@app/services";
import { Flex, FlexItem, Title } from "@patternfly/react-core";
import React from "react";
import { AppLauncher } from "./app-launcher";

type Props = {
  title?: string;
};

const MainNav: React.FC<Props> = ({ title }) => {
  const { data: featureFlags } = useGetFeatureFlagsQuery();

  return (
    <Flex
      justifyContent={{ default: "justifyContentSpaceBetween" }}
      alignItems={{ default: "alignItemsCenter" }}
    >
      <FlexItem>
        <Flex alignItems={{ default: "alignItemsCenter" }}>
          <FlexItem>
            <AppLauncher />
          </FlexItem>
          <FlexItem>
            {title && (
              <Title headingLevel="h1" size="3xl">
                {title}
              </Title>
            )}
          </FlexItem>
        </Flex>
      </FlexItem>
      <FlexItem>
        {featureFlags?.logoUrl && <img src={featureFlags.logoUrl} />}
      </FlexItem>
    </Flex>
  );
};

export { MainNav };
