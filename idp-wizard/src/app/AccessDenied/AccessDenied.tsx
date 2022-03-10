import * as React from "react";
import { ExclamationTriangleIcon } from "@patternfly/react-icons";
import {
  PageSection,
  Title,
  EmptyState,
  EmptyStateIcon,
  EmptyStateBody,
} from "@patternfly/react-core";
import { useTitle } from "react-use";

const AccessDenied: React.FunctionComponent = () => {
  useTitle("Access Denied | PhaseTwo.io");

  return (
    <PageSection>
      <EmptyState variant="full">
        <EmptyStateIcon icon={ExclamationTriangleIcon} />
        <Title headingLevel="h1" size="lg">
          Access Denied
        </Title>
        <EmptyStateBody>
          Sorry, but you lack the necessary permissions to access this content.
          Please contact your administrator to request access.
        </EmptyStateBody>
      </EmptyState>
    </PageSection>
  );
};

export { AccessDenied };
