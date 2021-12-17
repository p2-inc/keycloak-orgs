import React, { FC, useState } from "react";
import { InstructionProps, Step } from "@wizardComponents";
import * as Images from "@app/images/okta/saml";
import { useImageModal } from "@app/hooks/useImageModal";
import {
  ActionGroup,
  Alert,
  Button,
  Card,
  CardBody,
  FormAlert,
  FormGroup,
  Modal,
  ModalVariant,
  TextInput,
} from "@patternfly/react-core";
import { API_STATUS } from "@app/configurations/api-status";

interface Props {
  validateMetadata: ({ metadataUrl }: { metadataUrl: string }) => Promise<{
    status: API_STATUS;
    message: string;
  }>;
}

export const Step6: FC<Props> = ({ validateMetadata }) => {
  const [isModalOpen, modalImageSrc, { onImageClick }, setIsModalOpen] =
    useImageModal();

  const [metadataUrl, setMetadataUrl] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [result, setResult] = useState<null | {
    status: API_STATUS;
    message: string;
  }>(null);

  const submitMetadata = async () => {
    setIsLoading(true);
    const validateResult = await validateMetadata({ metadataUrl });
    setResult(validateResult);
    setIsLoading(false);
  };

  const instructions: InstructionProps[] = [
    {
      text: 'In the "Sign On" section, right click and click to copy the "Identity Provider metadata" link and paste below.',
      component: (
        <img
          src={Images.OktaSaml8}
          alt="Step 6.1"
          className="step-image"
          onClick={() => onImageClick(Images.OktaSaml8)}
        />
      ),
    },
    {
      text: "Enter and validate the Identity Provider metadata.",
      component: (
        <Card>
          <CardBody>
            {result && (
              <FormAlert className="pf-u-mb-md">
                <Alert
                  variant={
                    result.status === API_STATUS.ERROR ? "danger" : "default"
                  }
                  title={result.message}
                  aria-live="polite"
                  isInline
                />
              </FormAlert>
            )}
            <FormGroup
              label="Identity Provider Metadata"
              isRequired
              fieldId="ipm-01"
              className="pf-u-mb-md"
            >
              <TextInput
                isRequired
                type="text"
                id="ipm-01"
                name="ipm-01"
                placeholder="Paste Metadata URL"
                value={metadataUrl}
                onChange={setMetadataUrl}
              />
            </FormGroup>
            <ActionGroup>
              <Button
                variant="primary"
                onClick={submitMetadata}
                isLoading={isLoading}
              >
                Submit
              </Button>
            </ActionGroup>
          </CardBody>
        </Card>
      ),
    },
  ];

  return (
    <>
      <Modal
        aria-label="Image"
        variant={ModalVariant.large}
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
      >
        <img src={modalImageSrc} alt="Step Image" />
      </Modal>
      <Step
        title="Step 6: Upload Okta IdP Information"
        instructionList={instructions}
      />
    </>
  );
};
