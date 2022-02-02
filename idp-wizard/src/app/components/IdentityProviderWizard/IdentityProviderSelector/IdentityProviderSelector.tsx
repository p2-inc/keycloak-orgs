import React, { FC } from "react";
import { IdPButton } from "./components/IdPButton";
import { useKeycloak } from "@react-keycloak/web";
import { generatePath, Link, useParams } from "react-router-dom";
import {
  Button,
  Flex,
  FlexItem,
  PageSection,
  PageSectionVariants,
  Stack,
  StackItem,
} from "@patternfly/react-core";
import {
  GenericIdentityProviders,
  IdentityProviders,
} from "@app/configurations";
import { BASE_PATH } from "@app/routes";
import { useTitle } from "react-use";

export const IdentityProviderSelector: FC = () => {
  const { keycloak } = useKeycloak();
  let { realm } = useParams();
  useTitle("Select your Identity Provider | PhaseTwo");

  return (
    <PageSection variant={PageSectionVariants.light}>
      <Stack hasGutter>
        <StackItem>
          <Flex>
            <FlexItem align={{ default: "alignRight" }}>
              <Link to={generatePath(BASE_PATH, { realm })}>
                <Button variant="link" isInline>
                  Dashboard
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
              <h1>Select your Identity Provider</h1>
              <h2>This is how users will sign in to demo.phasetwo.io</h2>
              <div className="selection-container">
                {IdentityProviders.filter((idp) => idp.active)
                  .sort((a, b) =>
                    a.active === b.active ? 0 : a.active ? -1 : 1
                  )
                  .map(({ name, imageSrc, active, id, protocols }) => {
                    const genLink = generatePath(
                      `${BASE_PATH}/idp/:id/:protocol`,
                      {
                        realm,
                        id,
                        protocol:
                          protocols.length === 1 ? protocols[0] : "protocol",
                      }
                    );

                    const linkTo = active ? genLink : "#";
                    return (
                      <Link to={linkTo} key={id}>
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
              <h2
                style={{
                  maxWidth: "450px",
                  margin: "auto",
                  marginTop: "1.5rem",
                }}
              >
                If you don't see your identity provider, select one of the
                generic protocols below to connect with your provider.
              </h2>
              <div className="selection-container">
                {GenericIdentityProviders.map(
                  ({ name, imageSrc, active, id, protocols }) => {
                    const linkTo = active
                      ? `/idp/${id}/${
                          protocols.length === 1 ? protocols[0] : "protocol"
                        }`
                      : "#";
                    return (
                      <Link to={linkTo} key={id}>
                        <IdPButton
                          key={name}
                          text={name}
                          image={imageSrc}
                          active={active}
                        />
                      </Link>
                    );
                  }
                )}
              </div>
            </div>
          </div>
        </StackItem>
      </Stack>
    </PageSection>
  );
};
