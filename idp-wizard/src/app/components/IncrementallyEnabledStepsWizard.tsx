import React from 'react';
import { Button, ClipboardCopy, FileUpload, Form, FormGroup, InputGroup, TextArea, TextInput, Wizard } from '@patternfly/react-core';

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
      { id: 1, name: 'First step', component: <><p>Step 1 content</p>
        <Form>
        <FormGroup label="Copy this" fieldId="copy-form">
        <ClipboardCopy isReadOnly hoverTip="Copy" clickTip="Copied">This is NOT editable</ClipboardCopy>
        </FormGroup>
        <FormGroup label="Input here" fieldId="input-form">
        <InputGroup>
          <TextInput name="textarea2" id="textarea2" aria-label="textarea with button" />
          <Button id="textAreaButton2" variant="control">
            Button
          </Button>
        </InputGroup>
        </FormGroup>
        <FormGroup label="Upload here" fieldId="file-form">
        <FileUpload id="simple-file" filenamePlaceholder="Drag and drop a file or upload one" browseButtonText="Upload" />
        </FormGroup>
        </Form>
        </>
     },
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
      />
    );
  }
}

export default IncrementallyEnabledStepsWizard;
