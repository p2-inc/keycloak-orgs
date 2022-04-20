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
  Providers,
} from "@app/configurations";
import { BASE_PATH, PATHS } from "@app/routes";
import { useTitle } from "react-use";
import { useHostname } from "@app/hooks/useHostname";
import { useRoleAccess } from "@app/hooks";
import { useGetFeatureFlagsQuery } from "@app/services";

export const IdentityProviderSelector: FC = () => {
  useTitle("Select your Identity Provider | PhaseTwo");
  const { keycloak } = useKeycloak();
  let { realm } = useParams();
  const { hasAccess } = useRoleAccess();
  const hostname = useHostname();
  const { data: featureFlags } = useGetFeatureFlagsQuery();

  return (
    <PageS ection variant={PageSectionVariants.light}>
      <Stack hasGutter>
        <StackItem>
          <Flex justifyContent={{ default: "justifyContentFlexEnd" }}>
            {featureFlags?.enableDashboard && (
              <FlexItem>
                <Link to={generatePath(PATHS.dashboard, { realm })}>
                  <Button variant="link" isInline>
                    Dashboard
                  </Button>
                </Link>
              </FlexItem>
            )}
            <FlexItem>
              <Button
                variant="link"
                href={keycloak.createLogoutUrl({})}
                isInline
                component="a"
              >
                Logout
              </Button>
            </FlexItem>
          </Flex>
        </StackItem>
        <StackItem isFilled>
          <div className="container">
            <div className="vertical-center">
              <h1>Select your Identity Provider</h1>
              <h2>This is how users will sign in to {hostname}</h2>
              <div className="selection-container">
                {IdentityProviders.filter((idp) => idp.active)
                  .sort((a, b) =>
                    a.active === b.active ? 0 : a.active ? -1 : 1
                  )
                  .map(
                    ({ name, imageSrc, active, id: provider, protocols }) => {
                      const genLink = generatePath(PATHS.idpProvider, {
                        realm,
                        provider,
                        protocol:
                          protocols.length === 1 ? protocols[0] : "protocol",
                      });

                      const linkTo = active ? genLink : "#";
                      return (
                        <Link to={linkTo} key={provider}>
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
                {GenericIdentityProviders.filter((idp) =>
                  idp.id === Providers.LDAP ? featureFlags?.enableLdap : true
                ).map(({ name, imageSrc, active, id, protocols }) => {
                  const pth = generatePath(PATHS.idpProvider, {
                    realm,
                    provider: id,
                    protocol: protocols[0],
                  });
                  return (
                    <Link to={pth} key={id}>
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
    </PageS>
  );
};
