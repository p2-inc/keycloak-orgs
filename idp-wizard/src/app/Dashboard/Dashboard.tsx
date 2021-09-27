import * as React from 'react';
import { PageSection, Title, Button, Wizard } from '@patternfly/react-core';
import IncrementallyEnabledStepsWizard from '../components/IncrementallyEnabledStepsWizard';

const Dashboard: React.FunctionComponent = () => (
  <PageSection>
    <Title headingLevel="h1" size="lg">Dashboard Page Title!</Title>
    <IncrementallyEnabledStepsWizard />
  </PageSection>
)

export { Dashboard };
