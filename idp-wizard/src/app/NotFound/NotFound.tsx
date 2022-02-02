import * as React from "react";
import { ExclamationTriangleIcon } from "@patternfly/react-icons";
import {
  PageSection,
  Title,
  Button,
  EmptyState,
  EmptyStateIcon,
  EmptyStateBody,
} from "@patternfly/react-core";
import { useTitle } from "react-use";
import { useNavigateToBasePath } from "@app/routes";
import { useKeycloakAdminApi } from "@app/hooks/useKeycloakAdminApi";

const NotFound: React.FunctionComponent = () => {
  useTitle("404 Page Not Found");
  const [kcAdminClient, setKcAdminClientAccessToken, getServerUrl, getRealm] =
    useKeycloakAdminApi();
  const navigateToBasePath = useNavigateToBasePath(getRealm());

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
