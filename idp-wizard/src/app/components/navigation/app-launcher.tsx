import { useGetFeatureFlagsQuery } from "@app/services";
import React, { useState } from "react";
import cs from "classnames";
import { useKeycloak } from "@react-keycloak/web";
import { PATHS } from "@app/routes";
import {
  ApplicationLauncherItem,
  ApplicationLauncherGroup,
  ApplicationLauncherSeparator,
  ApplicationLauncher,
} from "@patternfly/react-core";
import { Link, generatePath, useParams, NavLink } from "react-router-dom";
import { useRoleAccess } from "@app/hooks";

const linkStyle: React.CSSProperties = {
  color: "var(--pf-c-app-launcher__menu-item--Color)",
  textDecoration: "none",
};

let activeStyle = {
  color: "var(--pf-c-app-launcher__toggle--active--Color)",
  fontWeight: "bold",
};

type Props = {
  toggleOrgPicker: (orgPickerState: boolean) => void;
};

const AppLauncher: React.FC<Props> = ({ toggleOrgPicker }) => {
  const { hasAccess } = useRoleAccess();
  let { realm } = useParams();
  const { keycloak } = useKeycloak();
  const { data: featureFlags } = useGetFeatureFlagsQuery();

  const [isOpen, setIsOpen] = useState(false);
  const onToggle = (isOpen: boolean) => setIsOpen(isOpen);
  const onSelect = (_event: any) => setIsOpen((prevIsOpen) => !prevIsOpen);

  const dashPath = generatePath(PATHS.dashboard, { realm });
  const idpPath = generatePath(PATHS.idpSelector, { realm });

  const AppLauncherItems: React.ReactElement[] = [
    <ApplicationLauncherItem
      key="dashboard"
      component={
        <Link
          to={dashPath}
          style={{
            ...linkStyle,
            ...(window.location.pathname === dashPath ? activeStyle : {}),
          }}
        >
          Dashboard
        </Link>
      }
      className={cs({
        "pf-u-display-none":
          featureFlags?.enableDashboard !== false || !hasAccess,
      })}
    />,
    <ApplicationLauncherItem
      key="idpSelector"
      component={
        <Link
          to={idpPath}
          style={{
            ...linkStyle,
            ...(window.location.pathname === idpPath ? activeStyle : {}),
          }}
        >
          IDP Selector
        </Link>
      }
    />,

    <ApplicationLauncherGroup key="group 1c">
      <ApplicationLauncherItem
        key="switchOrganization"
        onClick={() => toggleOrgPicker(true)}
        title="Change the active organization for IdP creation."
      >
        Switch Organization
      </ApplicationLauncherItem>
      <ApplicationLauncherSeparator key="separator" />
    </ApplicationLauncherGroup>,
    <ApplicationLauncherItem
      key="logout"
      component={
        <a href={keycloak.createLogoutUrl({})} style={linkStyle}>
          Logout
        </a>
      }
    />,
  ];

  return (
    <ApplicationLauncher
      onSelect={onSelect}
      onToggle={onToggle}
      isOpen={isOpen}
      items={AppLauncherItems}
    />
  );
};

export { AppLauncher };
