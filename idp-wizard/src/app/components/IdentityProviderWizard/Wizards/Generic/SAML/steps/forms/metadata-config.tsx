import React, { FC, useState } from "react";
import { useFormik } from "formik";
import {
  ActionGroup,
  Alert,
  Button,
  FileUpload,
  Form,
  FormAlert,
  FormGroup,
  TextInput,
} from "@patternfly/react-core";
import * as Yup from "yup";
import {
  API_RETURN,
  API_RETURN_PROMISE,
  API_STATUS,
} from "@app/configurations/api-status";

const metadataConfigSchema = Yup.object().shape({
  ssoUrl: Yup.string()
    .url("SSO Url should be a valid Url.")
    .required("SSO Url is a required field."),
  entityId: Yup.string().required("Entity Id is required."),
  metadataFile: Yup.mixed().required("Certifcate File is required."),
});

type Props = {
  handleFormSubmit: ({
    ssoUrl,
    entityId,
    metadataFile,
  }: {
    ssoUrl: string;
    entityId: string;
    metadataFile: File;
  }) => API_RETURN_PROMISE;
  formActive?: boolean;
};

export const MetadataConfig: FC<Props> = ({
  handleFormSubmit,
  formActive = true,
}) => {
  const [submissionResp, setSubmissionResp] = useState<API_RETURN | null>();
  const {
    handleSubmit,
    handleChange,
    values,
    errors,
    touched,
    isSubmitting,
    setSubmitting,
    setFieldValue,
  } = useFormik({
    initialValues: {
      ssoUrl: "",
      entityId: "",
      metadataFile: "",
    },
    onSubmit: async (values) => {
      const resp = await handleFormSubmit(values);
      setSubmissionResp(resp);
      setSubmitting(false);
    },
    validationSchema: metadataConfigSchema,
  });

  const hasError = (key: string) =>
    errors[key] && touched[key] ? "error" : "default";

  return (
    <Form onSubmit={handleSubmit}>
      {submissionResp && (
        <FormAlert>
          <Alert
            variant={
              submissionResp.status === API_STATUS.SUCCESS
                ? "success"
                : "danger"
            }
            title={submissionResp.message}
            aria-live="polite"
            isInline
          />
        </FormAlert>
      )}
      <FormGroup
        label="SSO Url"
        isRequired
        fieldId="ssoUrl"
        validated={hasError("ssoUrl")}
        helperTextInvalid={errors.ssoUrl}
      >
        <TextInput
          isRequired
          id="ssoUrl"
          name="ssoUrl"
          value={values.ssoUrl}
          onChange={(val, e) => handleChange(e)}
          validated={hasError("ssoUrl")}
        />
      </FormGroup>
      <FormGroup
        label="Entity Id"
        isRequired
        fieldId="entityId"
        validated={hasError("entityId")}
        helperTextInvalid={errors.entityId}
      >
        <TextInput
          isRequired
          id="entityId"
          name="entityId"
          value={values.entityId}
          onChange={(val, e) => handleChange(e)}
          validated={hasError("entityId")}
        />
      </FormGroup>
      <FormGroup
        label="Certificate File"
        isRequired
        fieldId="metadataFile"
        className="form-label"
        validated={hasError("metadataFile")}
        helperTextInvalid={errors.metadataFile}
      >
        <FileUpload
          id="metadataFile"
          value={values.metadataFile}
          filename={values.metadataFile?.name}
          filenamePlaceholder="Drop or choose certificate file (.pem, etc) to upload."
          browseButtonText="Select"
          onChange={(val) => setFieldValue("metadataFile", val)}
          isLoading={isSubmitting}
        />
      </FormGroup>
      <ActionGroup style={{ marginTop: 0 }}>
        <Button
          type="submit"
          isDisabled={isSubmitting || !formActive}
          isLoading={isSubmitting}
        >
          Validate Metadata File
        </Button>
      </ActionGroup>
    </Form>
  );
};
