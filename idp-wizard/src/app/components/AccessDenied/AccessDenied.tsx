import * as React from "react";
import { ExclamationTriangleIcon } from "@patternfly/react-icons";
import {
  PageSection,
  Title,
  EmptyState,
  EmptyStateIcon,
  EmptyStateBody,
} from "@patternfly/react-core";
import { usePageTitle } from "@app/hooks/useTitle";

const AccessDenied: React.FunctionComponent = () => {
  usePageTitle("Access Denied");

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
