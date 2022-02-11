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

const AccessDenied: React.FunctionComponent = () => {
  useTitle("Access Denied | PhaseTwo.io");
  const [kcAdminClient, setKcAdminClientAccessToken, getServerUrl, getRealm] =
    useKeycloakAdminApi();
  const navigateToBasePath = useNavigateToBasePath(getRealm());

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
        <Button onClick={() => navigateToBasePath()}>
          Return to Dashboard
        </Button>
      </EmptyState>
    </PageSection>
  );
};

export { AccessDenied };
