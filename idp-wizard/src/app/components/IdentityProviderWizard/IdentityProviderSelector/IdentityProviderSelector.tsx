import { MainNav } from "@app/components/navigation/main-nav";
import {
  GenericIdentityProviders,
  IdentityProviders,
  Providers,
} from "@app/configurations";
import {
  useApi,
  useKeycloakAdminApi,
  useOrganization,
  useRoleAccess,
} from "@app/hooks";
import { PATHS } from "@app/routes";
import { useGetFeatureFlagsQuery } from "@app/services";
import { usePageTitle } from "@app/hooks/useTitle";
import {
  PageSection,
  PageSectionVariants,
  Stack,
  StackItem,
  ExpandableSection,
  Switch,
  Button,
  Tooltip,
  ClipboardCopy,
} from "@patternfly/react-core";
import React, { FC, useState, useEffect } from "react";
import { generatePath, Link, useParams } from "react-router-dom";
import { IdPButton } from "./components/IdPButton";
import { Axios } from "../Wizards/services";
import { Table, Tbody, Td, Th, Thead, Tr } from "@patternfly/react-table";
import {
  CheckCircleIcon,
  TrashAltIcon,
  WarningTriangleIcon,
} from "@patternfly/react-icons";
import { useCreateTestIdpLink } from "@app/hooks/useCreateTestIdpLink";

export const IdentityProviderSelector: FC = () => {
  usePageTitle("Select Your Identity Provider");
  const [currentIdps, setCurrentIdps] = useState(null);
  const [orgsConfig, setOrgsConfig] = useState(null);
  const { idpsListUrl, orgsConfigUrl } = useApi();
  const { kcAdminClient } = useKeycloakAdminApi();

  let { realm } = useParams();
  const { getCurrentOrgName, currentOrg } = useOrganization();
  const currentOrgName = getCurrentOrgName();
  const { data: featureFlags } = useGetFeatureFlagsQuery();
  const { hasOrganizationRole } = useRoleAccess();

  const { generateValidationUrl } = useCreateTestIdpLink();

  // org? has right roles? show IDP list
  const showAdditionalIdps =
    hasOrganizationRole("view-organization", currentOrg) &&
    hasOrganizationRole("view-identity-providers", currentOrg);
  // Technically this case doesn't show since manage
  const hasManageIdpsRole = hasOrganizationRole(
    "manage-identity-providers",
    currentOrg,
  );

  const fetchIdps = async () => {
    try {
      if (showAdditionalIdps) {
        const resp = await Axios.get(idpsListUrl);
        if (resp.status !== 200) {
          throw new Error(
            `Error fetching identity providers: ${resp.statusText}`,
          );
        }
        setCurrentIdps(resp.data);
      }
    } catch (e) {
      console.error("Error fetching identity providers:", e);
    }
  };

  const fetchOrgsConfig = async () => {
    try {
      if (showAdditionalIdps) {
        const resp = await Axios.get(orgsConfigUrl);
        if (resp.status !== 200) {
          throw new Error(
            `Error fetching organizations config: ${resp.statusText}`,
          );
        }
        setOrgsConfig(resp.data);
      }
    } catch (e) {
      console.error("Error fetching organizations config:", e);
    }
  };

  useEffect(() => {
    fetchIdps();
    fetchOrgsConfig();
  }, [showAdditionalIdps, currentOrg]);

  async function handleIdpEnable(
    checked: boolean,
    idp: any,
    _event: React.FormEvent<HTMLInputElement>,
  ) {
    try {
      await kcAdminClient.identityProviders.update(
        {
          alias: idp.alias,
          realm,
        },
        {
          ...idp,
          enabled: checked,
        },
      );
    } catch (e) {
      console.error("Error updating identity provider:", e);
    } finally {
      fetchIdps();
      fetchOrgsConfig();
    }
  }

  async function handleIdpDelete(idp: any) {
    try {
      await kcAdminClient.identityProviders.del({
        alias: idp.alias,
        realm,
      });
    } catch (e) {
      console.error("Error deleting identity provider:", e);
    } finally {
      fetchIdps();
      fetchOrgsConfig();
    }
  }

  return (
    <PageSection variant={PageSectionVariants.light}>
      <Stack hasGutter>
        <StackItem>
          <MainNav />
          {showAdditionalIdps && currentIdps?.length > 0 && (
            <ExpandableSection
              toggleText="Existing Identity Provider Configurations"
              className="customExpansion"
            >
              {orgsConfig?.multipleIdpsEnabled === false && (
                <div className="infoBox">
                  <WarningTriangleIcon className="infoBoxWarning" />
                  <p className="infoText">
                    Multiple identity provider support is currently disabled.
                    Any change to an <b>enabled</b> state for an existing
                    identity provider will automatically disable all other
                    identity providers.
                  </p>
                </div>
              )}
              <Table variant="compact" className="existingIdpsTable">
                <Thead>
                  <Tr>
                    <Th></Th>
                    {hasManageIdpsRole && <Th></Th>}
                  </Tr>
                </Thead>
                <Tbody>
                  {currentIdps.map((idp) => (
                    <Tr key={idp.alias}>
                      <Td>
                        <div
                          style={{
                            display: "flex",
                            gap: ".5rem",
                            flexDirection: "column",
                          }}
                        >
                          <div
                            style={{
                              display: "flex",
                              alignItems: "center",
                              gap: ".5rem",
                            }}
                          >
                            <span>
                              {idp.config[
                                "home.idp.discovery.validationPending"
                              ] === "true" && (
                                <Tooltip content="Validation Pending. Use the link below to complete validation.">
                                  <WarningTriangleIcon
                                    className="infoBoxWarning"
                                    title="Validation Pending"
                                  />
                                </Tooltip>
                              )}
                              {idp.config[
                                "home.idp.discovery.validationPending"
                              ] === "false" && (
                                <Tooltip content="Validation complete.">
                                  <CheckCircleIcon
                                    title="Validated"
                                    className="icon-success"
                                  />
                                </Tooltip>
                              )}
                            </span>
                            <span style={{ fontWeight: "bold" }}>
                              {idp.displayName || idp.alias}
                            </span>
                          </div>
                          {idp.config[
                            "home.idp.discovery.validationPending"
                          ] === "true" && (
                            <>
                              <p>
                                This SSO configuration has not been validated
                                yet. Use the link below to open and validate the
                                identity provider. Open the link in a new
                                browser or incognito window to avoid being
                                signed out of the wizard.
                              </p>
                              <ClipboardCopy
                                hoverTip="Copy and open in another browser or incognito window, or you will be logged out of the wizard."
                                clickTip="Copied. Open in another browser or incognito window, or you will be logged out of the wizard."
                                className="clipboard-copy"
                              >
                                {generateValidationUrl(idp.alias)}
                              </ClipboardCopy>
                            </>
                          )}
                        </div>
                      </Td>
                      {hasManageIdpsRole && (
                        <Td>
                          <div
                            style={{
                              display: "flex",
                              gap: "1rem",
                              alignItems: "center",
                              justifyContent: "flex-end",
                            }}
                          >
                            <Switch
                              label={idp.enabled ? "Enabled" : "Disabled"}
                              isChecked={idp.enabled}
                              onChange={(checked, evt) =>
                                handleIdpEnable(checked, idp, evt)
                              }
                              isDisabled={
                                idp.config[
                                  "home.idp.discovery.validationPending"
                                ] === "true"
                              }
                            />
                            <Button
                              variant="link"
                              isDanger
                              icon={<TrashAltIcon />}
                              onClick={() => handleIdpDelete(idp)}
                              title="Delete identity provider"
                            />
                          </div>
                        </Td>
                      )}
                    </Tr>
                  ))}
                </Tbody>
              </Table>
              {}
            </ExpandableSection>
          )}
        </StackItem>
        <StackItem isFilled>
          <div className="container">
            <div className="vertical-center">
              <h1>Select Your Identity Provider</h1>
              <h2 className="description">
                This is how users will sign in to{" "}
                <span className="currentOrg">
                  {currentOrgName === "Global" ? "realms" : currentOrgName}
                </span>
              </h2>

              <div className="selection-container">
                {IdentityProviders.filter((idp) => idp.active)
                  .sort((a, b) =>
                    a.active === b.active ? 0 : a.active ? -1 : 1,
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
                    },
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
                  idp.id === Providers.LDAP ? featureFlags?.enableLdap : true,
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
