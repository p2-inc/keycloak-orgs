import React, { FC } from "react";
import { IdPButton } from "./components/IdPButton";
import { generatePath, Link, Navigate, useParams } from "react-router-dom";
import {
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
import { PATHS } from "@app/routes";
import { useTitle } from "react-use";
import { useHostname } from "@app/hooks/useHostname";
import { useGetFeatureFlagsQuery } from "@app/services";
import { MainNav } from "@app/components/navigation/main-nav";
import { useAppSelector, useOrganization, useRoleAccess } from "@app/hooks";

export const IdentityProviderSelector: FC = () => {
  useTitle("Select your Identity Provider | Phase Two");

  let { realm } = useParams();
  const hostname = useHostname();
  const { getCurrentOrgName } = useOrganization();
  const currentOrgName = getCurrentOrgName();

  const { data: featureFlags } = useGetFeatureFlagsQuery();

  return (
    <PageSection variant={PageSectionVariants.light}>
      <Stack hasGutter>
        <StackItem>
          <MainNav />
        </StackItem>
        <StackItem isFilled>
          <div className="container">
            <div className="vertical-center">
              <h1>Select your Identity Provider</h1>
              <h2 className="description">
                This is how users will sign in to{" "}
                <span className="currentOrg">
                  {currentOrgName === "Global" ? "realms" : currentOrgName}
                </span>
              </h2>
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
                        <Link to={linkTo} key={provider} title={name}>
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
    </PageSection>
  );
};
