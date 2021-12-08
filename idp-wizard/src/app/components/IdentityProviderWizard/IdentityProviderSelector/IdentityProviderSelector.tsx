import React, { FC } from "react";
import { IdPButton } from "./components/IdPButton";
import { useKeycloak } from "@react-keycloak/web";
import { Link } from "react-router-dom";
import {
  Button,
  Flex,
  FlexItem,
  PageSection,
  PageSectionVariants,
  Stack,
  StackItem,
} from "@patternfly/react-core";
import { IdentityProviders } from "@app/configurations";

export const IdentityProviderSelector: FC = () => {
  const { keycloak } = useKeycloak();

  return (
    <PageSection variant={PageSectionVariants.light}>
      <Stack hasGutter>
        <StackItem>
          <Flex>
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
        </StackItem>
        <StackItem isFilled>
          <div className="container">
            <div className="vertical-center">
              <h1>Choose your Identity Provider</h1>
              <h2>This is how users will sign in to demo.phasetwo.io</h2>
              <div className="selection-container">
                {IdentityProviders.map(({ name, imageSrc, active, id }) => {
                  return (
                    <Link to={active ? `/idp/${id}/protocol` : "#"} key={id}>
                      <IdPButton
                        key={name}
                        text={name}
                        image={imageSrc}
                        active={active}
                      />
                    </Link>
                  );
                })}
              </div>
            </div>
          </div>
        </StackItem>
      </Stack>
    </PageSection>
  );
};
