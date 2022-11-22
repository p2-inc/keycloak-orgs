import { useAppSelector } from "@app/hooks/hooks";
import { useOrganization } from "@app/hooks";
import { useGetFeatureFlagsQuery } from "@app/services";
import { Divider, Flex, FlexItem, Title } from "@patternfly/react-core";
import React, { useState } from "react";
import { AppLauncher } from "./app-launcher";
import { OrgPicker } from "./org-picker";

type Props = {
  title?: string;
};

const MainNav: React.FC<Props> = ({ title }) => {
  const { data: featureFlags } = useGetFeatureFlagsQuery();
  const { getSelectedOrgName } = useOrganization();
  const currentOrg = getSelectedOrgName();

  //TODO Move into store so that the picker state can easily be accessed
  // depending on the location it gets determined that it should be used
  // to pick an org
  // Autoopen when
  const [isOrgPickerOpen, setIsOrgPickerOpen] = useState(true);

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
            <Flex alignItems={{ default: "alignItemsCenter" }}>
              {title && (
                <FlexItem>
                  <Title headingLevel="h2" size="lg" playsInline>
                    {title}
                  </Title>
                </FlexItem>
              )}
              {title && currentOrg && <Divider isVertical />}
              {currentOrg && (
                <FlexItem>
                  <Title headingLevel="h2" size="lg" playsInline>
                    {currentOrg}
                  </Title>
                </FlexItem>
              )}
            </Flex>
          </FlexItem>
        </Flex>
      </FlexItem>
      <FlexItem style={{ textAlign: "end" }}>
        {featureFlags?.logoUrl && <img src={featureFlags.logoUrl} />}
      </FlexItem>
    </Flex>
  );
};

export { MainNav };
