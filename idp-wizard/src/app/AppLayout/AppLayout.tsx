import * as React from "react";
import { NavLink, useLocation } from "react-router-dom";
import {
  Nav,
  NavList,
  NavItem,
  NavExpandable,
  Page,
  PageHeader,
  PageSidebar,
  SkipToContent,
  AlertGroup,
  AlertActionCloseButton,
  Alert,
} from "@patternfly/react-core";
import { routes, useNavigateToBasePath } from "@app/routes";
import logo from "@app/images/phasetwo-logos/logo_phase_slash.svg";
import { useRoleAccess } from "@app/hooks";

interface IAppLayout {
  children: React.ReactNode;
}

const AlertAccessWarning = (onClose) => (
  <Alert
    variant="warning"
    title={"Access denied."}
    // actionClose={
    //   <AlertActionCloseButton
    //     title={"Access denied"}
    //     variantLabel={`access denied alert`}
    //     onClose={() => onClose()}
    //   />
    // }
  >
    <p>Please speak to your admin for access to this content.</p>
  </Alert>
);

const AppLayout: React.FunctionComponent<IAppLayout> = ({ children }) => {
  const [isNavOpen, setIsNavOpen] = React.useState(true);
  const [isMobileView, setIsMobileView] = React.useState(true);
  const [isNavOpenMobile, setIsNavOpenMobile] = React.useState(false);
  const navigateToBasePath = useNavigateToBasePath();

  const [hasAccess] = useRoleAccess();
  console.log("[HasAccess]", hasAccess);

  const onNavToggleMobile = () => {
    setIsNavOpenMobile(!isNavOpenMobile);
  };
  const onNavToggle = () => {
    setIsNavOpen(!isNavOpen);
  };
  const onPageResize = (props: { mobileView: boolean; windowSize: number }) => {
    setIsMobileView(props.mobileView);
  };

  function LogoImg() {
    return (
      <img src={logo} onClick={() => navigateToBasePath()} alt="PhaseTwo" />
    );
  }

  const Header = (
    <PageHeader
      logo={<LogoImg />}
      style={{ backgroundColor: "white", borderBottom: "1px solid" }}
      showNavToggle={false}
    />
  );

  const location = useLocation();

  const renderNavItem = (route, index: number) => (
    <NavItem key={`${route.label}-${index}`} id={`${route.label}-${index}`}>
      <NavLink
        exact={route.exact}
        to={route.path}
        activeClassName="pf-m-current"
      >
        {route.label}
      </NavLink>
    </NavItem>
  );

  const renderNavGroup = (group, groupIndex: number) => (
    <NavExpandable
      key={`${group.label}-${groupIndex}`}
      id={`${group.label}-${groupIndex}`}
      title={group.label}
      isActive={group.routes.some((route) => route.path === location.pathname)}
    >
      {group.routes.map(
        (route, idx) => route.label && renderNavItem(route, idx)
      )}
    </NavExpandable>
  );

  const Navigation = (
    <Nav id="nav-primary-simple" theme="dark">
      <NavList id="nav-list-simple">
        {routes.map(
          (route, idx) =>
            route.label &&
            (!route.routes
              ? renderNavItem(route, idx)
              : renderNavGroup(route, idx))
        )}
      </NavList>
    </Nav>
  );

  const Sidebar = (
    <PageSidebar
      theme="dark"
      nav={Navigation}
      isNavOpen={isMobileView ? isNavOpenMobile : isNavOpen}
    />
  );

  const pageId = "primary-app-container";

  const PageSkipToContent = (
    <SkipToContent
      onClick={(event) => {
        event.preventDefault();
        const primaryContentContainer = document.getElementById(pageId);
        primaryContentContainer && primaryContentContainer.focus();
      }}
      href={`#${pageId}`}
    >
      Skip to Content
    </SkipToContent>
  );
  return (
    <Page
      mainContainerId={pageId}
      // header={Header}
      // sidebar={Sidebar}

      onPageResize={onPageResize}
      skipToContent={PageSkipToContent}
      isManagedSidebar={false}
      style={{ backgroundColor: "white" }}
    >
      <AlertGroup isToast isLiveRegion>
        {hasAccess === false && <AlertAccessWarning />}
      </AlertGroup>
      {children}
    </Page>
  );
};

export { AppLayout };
