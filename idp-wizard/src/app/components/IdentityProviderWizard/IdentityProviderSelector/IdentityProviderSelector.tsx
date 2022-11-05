import React, { FC, useState } from "react";
import { IdPButton } from "./components/IdPButton";
import { useKeycloak } from "@react-keycloak/web";
import { generatePath, Link, useParams } from "react-router-dom";
import {
  ApplicationLauncher,
  ApplicationLauncherGroup,
  ApplicationLauncherItem,
  ApplicationLauncherSeparator,
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
import { PATHS } from "@app/routes";
import { useTitle } from "react-use";
import { useHostname } from "@app/hooks/useHostname";
import { useRoleAccess } from "@app/hooks";
import { useGetFeatureFlagsQuery } from "@app/services";
import cs from "classnames";

export const IdentityProviderSelector: FC = () => {
  useTitle("Select your Identity Provider | Phase Two");
  const { keycloak } = useKeycloak();
  let { realm } = useParams();
  const { hasAccess } = useRoleAccess();
  const hostname = useHostname();
  const { data: featureFlags } = useGetFeatureFlagsQuery();

  const [isOpen, setIsOpen] = useState(false);

  const onToggle = (isOpen: boolean) => setIsOpen(isOpen);
  const onSelect = (_event: any) => setIsOpen((prevIsOpen) => !prevIsOpen);

  const linkStyle: React.CSSProperties = {
    color: "var(--pf-c-app-launcher__menu-item--Color)",
    textDecoration: "none",
  };

  // TODO: add icons
  const appLauncherItems: React.ReactElement[] = [
    <ApplicationLauncherItem
      key="dashboard"
      component={
        <Link to={generatePath(PATHS.dashboard, { realm })} style={linkStyle}>
          Dashboard
        </Link>
      }
      className={cs({
        "pf-u-display-none": featureFlags?.enableDashboard !== false,
      })}
    />,
    <ApplicationLauncherItem
      key="idpSelector"
      component={
        <Link to={generatePath(PATHS.idpSelector, { realm })} style={linkStyle}>
          IDP Selector
        </Link>
      }
    />,

    <ApplicationLauncherGroup key="group 1c">
      <ApplicationLauncherItem
        key="switchOrganization"
        isDisabled
        onClick={() => console.log("wire me up")}
      >
        Switch Organization
      </ApplicationLauncherItem>
      <ApplicationLauncherSeparator key="separator" />
    </ApplicationLauncherGroup>,
    <ApplicationLauncherItem
      key="logout"
      component={
        <Link to={keycloak.createLogoutUrl({})} style={linkStyle}>
          Logout
        </Link>
      }
    />,
  ];

  return (
    <PageSection variant={PageSectionVariants.light}>
      <Stack hasGutter>
        <StackItem>
          <Flex justifyContent={{ default: "justifyContentSpaceBetween" }}>
            <FlexItem>
              <ApplicationLauncher
                onSelect={onSelect}
                onToggle={onToggle}
                isOpen={isOpen}
                items={appLauncherItems}
              />
            </FlexItem>
            <FlexItem>Logo</FlexItem>
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
    </PageSection>
  );
};
