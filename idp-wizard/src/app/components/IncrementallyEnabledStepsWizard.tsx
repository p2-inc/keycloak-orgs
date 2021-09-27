import React from 'react';
import { Button, Wizard } from '@patternfly/react-core';

class IncrementallyEnabledStepsWizard extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      stepIdReached: 1
    };
    this.onNext = ({ id }) => {
      this.setState({
        stepIdReached: this.state.stepIdReached < id ? id : this.state.stepIdReached
      });
    };
    this.closeWizard = () => {
      console.log("close wizard");
    }
  }

  render() {
    const { stepIdReached } = this.state;

    const steps = [
      { id: 1, name: 'First step', component: <p>Step 1 content</p> },
      { id: 2, name: 'Second step', component: <p>Step 2 content</p>, canJumpTo: stepIdReached >= 2 },
      { id: 3, name: 'Third step', component: <p>Step 3 content</p>, canJumpTo: stepIdReached >= 3 },
      { id: 4, name: 'Fourth step', component: <p>Step 4 content</p>, canJumpTo: stepIdReached >= 4 },
      { id: 5, name: 'Review', component: <p>Review step content</p>, nextButtonText: 'Finish', canJumpTo: stepIdReached >= 5 }
    ];
    const title = 'Incrementally enabled wizard';
    return (
      <Wizard
        navAriaLabel={`${title} steps`}
        mainAriaLabel={`${title} content`}
        onClose={this.closeWizard}
        steps={steps}
        onNext={this.onNext}
        height={400}
      />
    );
  }
}

export default IncrementallyEnabledStepsWizard;
