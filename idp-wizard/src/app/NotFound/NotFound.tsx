import { usePageTitle } from "@app/hooks/useTitle";
import { useNavigateToBasePath } from "@app/routes";
import {
  Button,
  EmptyState,
  EmptyStateBody,
  EmptyStateIcon,
  PageSection,
  Title,
} from "@patternfly/react-core";
import { ExclamationTriangleIcon } from "@patternfly/react-icons";
import * as React from "react";

const NotFound: React.FunctionComponent = () => {
  usePageTitle("404 Page Not Found");
  const navigateToBasePath = useNavigateToBasePath();

  return (
    <PageSection>
      <EmptyState variant="full">
        <EmptyStateIcon icon={ExclamationTriangleIcon} />
        <Title headingLevel="h1" size="lg">
          404 Page not found
        </Title>
        <EmptyStateBody>
          We didn&apos;t find a page that matches the address you navigated to.
        </EmptyStateBody>
        <Button onClick={() => navigateToBasePath()}>
          Return to Dashboard
        </Button>
      </EmptyState>
    </PageSection>
  );
};

export { NotFound };
