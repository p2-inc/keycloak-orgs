import * as React from "react";
import { Page, SkipToContent, Alert } from "@patternfly/react-core";

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
  const pageId = "primary-app-container";

  const PageSkipToContent = (
    <SkipToContent
      onClick={(event) => {
        event.preventDefault();
        const primaryContentContainer = document.getElementById(pageId);
        primaryContentContainer && primaryContentContainer.focus();
      }}
      href={`#${pageId}`}
      style={{ position: "absolute" }}
    >
      Skip to Content
    </SkipToContent>
  );

  return (
    <Page
      mainContainerId={pageId}
      skipToContent={PageSkipToContent}
      isManagedSidebar={false}
      style={{ backgroundColor: "white" }}
    >
      {/* <AlertGroup isToast isLiveRegion>
        {hasAccess === false && <AlertAccessWarning />}
      </AlertGroup> */}
      {children}
    </Page>
  );
};

export { AppLayout };
