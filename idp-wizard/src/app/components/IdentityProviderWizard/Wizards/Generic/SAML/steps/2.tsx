import React, { FC, useEffect, useState } from "react";
import { InstructionProps, Step } from "@wizardComponents";
import {
  ActionGroup,
  Alert,
  Button,
  Card,
  CardBody,
  CardTitle,
  FileUpload,
  FileUploadFieldProps,
  Form,
  FormAlert,
  FormFieldGroupExpandable,
  FormFieldGroupHeader,
  FormGroup,
  FormHelperText,
  HelperText,
  TextInput,
} from "@patternfly/react-core";
import {
  API_RETURN,
  API_STATUS,
  METADATA_CONFIG,
} from "@app/configurations/api-status";
import { isString } from "lodash";

interface Props {
  validateMetadataUrl: ({
    metadataUrl,
  }: {
    metadataUrl: string;
  }) => Promise<API_RETURN>;
  uploadMetadataFile: (file: File) => Promise<API_RETURN>;
  uploadCertifcateMetadataInfo: ({
    file,
    ssoUrl,
    entityId,
  }: {
    file: File;
    ssoUrl: METADATA_CONFIG["singleSignOnServiceUrl"];
    entityId: string;
  }) => Promise<API_RETURN>;
  metadata: METADATA_CONFIG | undefined;
}

export const Step2: FC<Props> = ({
  uploadMetadataFile,
  validateMetadataUrl,
  uploadCertifcateMetadataInfo,
  metadata,
}) => {
  const [metadataUrl, setMetadataUrl] = useState("");
  const [ssoUrl, setSsoUrl] = useState("");
  const [entityId, setEntityId] = useState("");

  const [errors, setErrors] = useState({});
  const [results, setResults] = useState({});
  const [isValidating, setIsValidating] = useState<boolean | string>(false);
  const [isUploading, setIsUploading] = useState(false);

  // XML config
  const [metadataFileValue, setMetadataFileValue] = useState("");
  const [metadataFileName, setMetadataFileName] = useState("");
  const [uploadValid, setUploadValid] = useState<boolean | null>(null);

  // Form 2 certifcate
  const [certifcateMetadataFileValue, setCertifcateMetadataFileValue] =
    useState("");
  const [certifcateMetadataFileName, setCertifcateMetadataFileName] =
    useState("");
  const [certificateUploadValid, setCertificateUploadValid] = useState<
    boolean | null
  >(null);

  useEffect(() => {
    if (metadata) {
      setSsoUrl(metadata.singleSignOnServiceUrl);
      // TODO: is this being returned?
      // setEntityId(metadata);
    }
  }, [metadata]);

  const resetForms = () => {
    setErrors({});
    setResults({});
  };

  const handleMetadatUrlValidation = async () => {
    resetForms();

    if (isString(metadataUrl) && metadataUrl.length > 0) {
      setIsValidating("METADATA_URL");
      let resp: API_RETURN;
      resp = await validateMetadataUrl({ metadataUrl });
      if (resp.status === API_STATUS.SUCCESS) {
        setResults({
          "form-metadata-url": resp.message,
          "form2-validate": true,
        });
      }
      if (resp.status === API_STATUS.ERROR) {
        setErrors({
          "form-metadata-url": resp.message,
        });
      }
      setIsValidating(false);
    }
  };

  const handleFileInputChange: FileUploadFieldProps["onChange"] = async (
    value,
    filename,
    event
  ) => {
    resetForms();
    setUploadValid(null);
    setMetadataFileName(filename);
    setMetadataFileValue(value);

    if (!value) return;

    setIsUploading(true);

    const uploadStatus = await uploadMetadataFile(value);

    setUploadValid(uploadStatus.status === API_STATUS.SUCCESS);
    if (uploadStatus.status === API_STATUS.SUCCESS) {
      setResults({
        "metadata-file": uploadStatus.message,
        "form2-validate": true,
      });
    } else {
      setErrors({ "metadata-file": uploadStatus.message });
    }
    setIsUploading(false);
  };

  const handleCertificateInputChange: FileUploadFieldProps["onChange"] = async (
    value,
    filename,
    event
  ) => {
    setCertifcateMetadataFileName(filename);
    setCertifcateMetadataFileValue(value);
    // setIsUploading(true);
    // setCertificateUploadValid(null);

    // const uploadStatus = await uploadCertifcateMetadataFile({ value });

    // if (uploadStatus) {
    //   setCertificateUploadValid(true);
    // } else {
    //   setCertificateUploadValid(false);
    // }

    // setIsUploading(false);
  };

  const handleForm2Validation = async () => {
    if (ssoUrl !== "" && entityId === "" && certifcateMetadataFileValue) {
      uploadCertifcateMetadataInfo({
        file: certifcateMetadataFileValue,
        ssoUrl,
        entityId,
      });
    }
  };

  console.log(errors, results);

  const isValidResult = (key: string) =>
    errors[key] ? "error" : results[key] ? "success" : "default";

  const validatedMetadataUrl = isValidResult("form-metadata-url");
  const validatedMetadataFile = isValidResult("metadata-file");
  const validatedCertficateMetadataFile = isValidResult("certificate");

  const instructions: InstructionProps[] = [
    {
      component: (
        <div>
          Your identity provider should provide you with configuration
          information in the form of a metadata url, metadata file, or a few
          configuration parameters.
        </div>
      ),
    },
    {
      component: (
        <Card>
          <CardTitle>Metadata URL</CardTitle>
          <CardBody>
            <div className="pf-u-pb-sm">
              If your identity provider provides a SAML metadata URL, input it
              here.
            </div>
            <Form>
              <FormGroup
                label="Metadata Url"
                isRequired
                fieldId="form-metadata-url"
                validated={validatedMetadataUrl}
                helperTextInvalid={errors["form-metadata-url"]}
              >
                <TextInput
                  isRequired
                  id="form-metadata-url"
                  name="form-metadata-url"
                  value={metadataUrl}
                  onChange={setMetadataUrl}
                  validated={validatedMetadataUrl}
                />
              </FormGroup>
              <ActionGroup style={{ marginTop: 0 }}>
                <Button
                  onClick={handleMetadatUrlValidation}
                  isDisabled={
                    isValidating === "METADATA_URL" || metadataUrl === ""
                  }
                  isLoading={isValidating === "METADATA_URL"}
                >
                  Validate URL
                </Button>
              </ActionGroup>
            </Form>
          </CardBody>
        </Card>
      ),
    },
    {
      component: (
        <Card>
          <CardTitle>Metadata File</CardTitle>
          <CardBody>
            <div className="pf-u-pb-sm">
              If your identity provider provides a SAML metadata File, upload it
              here.
            </div>
            <Form>
              {uploadValid && (
                <FormAlert>
                  <Alert
                    variant={validatedMetadataFile ? "success" : "danger"}
                    title={
                      uploadValid
                        ? results["metadata-file"]
                        : errors["metadata-file"]
                    }
                    aria-live="polite"
                    isInline
                  />
                </FormAlert>
              )}

              <FormGroup
                label="Metadata File"
                isRequired
                fieldId="metadata-file"
                className="form-label"
                helperText="Choosing file will initiate validation."
              >
                <FileUpload
                  id="metadata-file"
                  value={metadataFileValue}
                  filename={metadataFileName}
                  filenamePlaceholder="Drop or choose metadata file .xml to upload."
                  browseButtonText="Select"
                  onChange={handleFileInputChange}
                  dropzoneProps={{
                    accept: "text/xml",
                  }}
                  isLoading={isUploading}
                />
              </FormGroup>
            </Form>
          </CardBody>
        </Card>
      ),
    },
    {
      component: (
        <Card>
          <CardTitle>Configuration Information</CardTitle>
          <CardBody>
            <div className="pf-u-pb-sm">
              If your identity provider provides a SAML metadata URL, Entity ID,
              and Certificate, input those below.
            </div>
            <Form>
              <FormGroup label="SSO Url" isRequired fieldId="form-sso-url">
                <TextInput
                  isRequired
                  id="form-sso-url"
                  name="form-sso-url"
                  value={ssoUrl}
                  onChange={setSsoUrl}
                />
              </FormGroup>
              <FormGroup label="Entity ID" isRequired fieldId="form-entity-id">
                <TextInput
                  isRequired
                  id="form-entity-id"
                  name="form-entity-id"
                  value={entityId}
                  onChange={setEntityId}
                />
              </FormGroup>
              {certificateUploadValid && (
                <FormAlert>
                  <Alert
                    variant={
                      validatedCertficateMetadataFile ? "success" : "danger"
                    }
                    title={
                      uploadValid
                        ? results["metadata-file"]
                        : errors["metadata-file"]
                    }
                    aria-live="polite"
                    isInline
                  />
                </FormAlert>
              )}
              <FormGroup
                label="Certificate"
                isRequired
                fieldId="certificate"
                className="form-label"
              >
                <FileUpload
                  id="certificate"
                  value={certifcateMetadataFileValue}
                  filename={certifcateMetadataFileName}
                  filenamePlaceholder="Drop or choose metadata file .xml to upload."
                  browseButtonText="Select"
                  onChange={handleCertificateInputChange}
                  dropzoneProps={{
                    accept: "text/xml",
                  }}
                  isLoading={isUploading}
                />
              </FormGroup>
              <ActionGroup style={{ marginTop: 0 }}>
                <Button
                  id="form2-validate"
                  isDisabled={results["form2-validate"]}
                  onClick={handleForm2Validation}
                >
                  Validate Configuration Data
                </Button>
                {results["form2-validate"] && (
                  <HelperText>
                    No further information required here, please continue.
                  </HelperText>
                )}
              </ActionGroup>
            </Form>
          </CardBody>
        </Card>
      ),
    },
  ];

  return (
    <Step
      title="Step 2: Configure Application Metadata"
      instructionList={instructions}
    />
  );
};
