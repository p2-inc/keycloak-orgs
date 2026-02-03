import { useAppDispatch, useAppSelector } from "@app/hooks/hooks";
import { useOrganization } from "@app/hooks";
import { setMustPickOrg, useGetFeatureFlagsQuery } from "@app/services";
import { Divider, Flex, FlexItem, Title } from "@patternfly/react-core";
import React, { useEffect, useState } from "react";
import { AppLauncher } from "./app-launcher";
import { OrgPicker } from "./org-picker";

type Props = {
  title?: string;
};

const MainNav: React.FC<Props> = ({ title }) => {
  const dispatch = useAppDispatch();
  const { data: featureFlags } = useGetFeatureFlagsQuery();
  const { getCurrentOrgName } = useOrganization();
  const mustPickOrg = useAppSelector((state) => state.settings.mustPickOrg);
  const currentOrgName = getCurrentOrgName();
  const [isOrgPickerOpen, setIsOrgPickerOpen] = useState(mustPickOrg);

  useEffect(() => {
    if (mustPickOrg) {
      setIsOrgPickerOpen(mustPickOrg);
      dispatch(setMustPickOrg(false));
    }
  }, [mustPickOrg]);

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
              {title && currentOrgName && <Divider isVertical />}
              {currentOrgName && (
                <FlexItem>
                  <Title headingLevel="h2" size="lg" playsInline>
                    {currentOrgName}
                  </Title>
                </FlexItem>
              )}
            </Flex>
          </FlexItem>
        </Flex>
      </FlexItem>
      <FlexItem style={{ textAlign: "end" }}>
        {featureFlags?.logoUrl && (
          <img
            src={featureFlags.logoUrl}
            style={{ maxHeight: "50px", width: "auto" }}
          />
        )}
      </FlexItem>
    </Flex>
  );
};

export { MainNav };
