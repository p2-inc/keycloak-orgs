import { useAppSelector } from "@app/hooks/hooks";
import { useGetFeatureFlagsQuery } from "@app/services";
import { Flex, FlexItem, Title } from "@patternfly/react-core";
import React, { useState } from "react";
import { AppLauncher } from "./app-launcher";
import { OrgPicker } from "./org-picker";

type Props = {
  title?: string;
};

const MainNav: React.FC<Props> = ({ title }) => {
  const { data: featureFlags } = useGetFeatureFlagsQuery();
  const currentOrg = useAppSelector((state) => state.settings.selectedOrg);

  const [isOrgPickerOpen, setIsOrgPickerOpen] = useState(false);

  return (
    <Flex
      justifyContent={{ default: "justifyContentSpaceBetween" }}
      alignItems={{ default: "alignItemsFlexStart" }}
    >
      <FlexItem>
        <Flex alignItems={{ default: "alignItemsCenter" }}>
          <FlexItem>
            <AppLauncher toggleOrgPicker={setIsOrgPickerOpen} />
            <OrgPicker
              open={isOrgPickerOpen}
              toggleModal={setIsOrgPickerOpen}
            />
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
      <FlexItem style={{ textAlign: "end" }}>
        {featureFlags?.logoUrl && <img src={featureFlags.logoUrl} />}
        <Title headingLevel="h2" size="lg">
          {currentOrg}
        </Title>
      </FlexItem>
    </Flex>
  );
};

export { MainNav };
