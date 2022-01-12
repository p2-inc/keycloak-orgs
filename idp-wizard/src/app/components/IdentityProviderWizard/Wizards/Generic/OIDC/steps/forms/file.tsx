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
} from "@patternfly/react-core";
import * as Yup from "yup";
import {
  API_RETURN,
  API_RETURN_PROMISE,
  API_STATUS,
} from "@app/configurations/api-status";

const configurationFileSchema = Yup.object().shape({
  configurationFile: Yup.mixed().required("Configuration File is required."),
});

type Props = {
  handleFormSubmit: ({ file }: { file: File }) => API_RETURN_PROMISE;
  formActive?: boolean;
};

export const ConfigurationFile: FC<Props> = ({
  handleFormSubmit,
  formActive = true,
}) => {
  const [submissionResp, setSubmissionResp] = useState<API_RETURN | null>();
  const {
    handleSubmit,
    values,
    errors,
    touched,
    isSubmitting,
    setSubmitting,
    setFieldValue,
  } = useFormik({
    initialValues: {
      configurationFile: "",
    },
    onSubmit: async (values) => {
      const resp = await handleFormSubmit(values);
      setSubmissionResp(resp);
      setSubmitting(false);
    },
    validationSchema: configurationFileSchema,
  });

  const hasError =
    errors.configurationFile && touched.configurationFile ? "error" : "default";

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
        label="Configuration File"
        isRequired
        fieldId="configurationFile"
        className="form-label"
        validated={hasError}
        helperTextInvalid={errors.configurationFile}
      >
        <FileUpload
          id="configurationFile"
          value={values.configurationFile}
          filename={values.configurationFile?.name}
          filenamePlaceholder="Drop or choose configuration file .xml to upload."
          browseButtonText="Select"
          onChange={(val) => setFieldValue("configurationFile", val)}
          dropzoneProps={{
            accept: "text/xml",
          }}
          isLoading={isSubmitting}
          isDisabled={!formActive}
        />
      </FormGroup>
      <ActionGroup style={{ marginTop: 0 }}>
        <Button
          type="submit"
          isDisabled={isSubmitting || !formActive}
          isLoading={isSubmitting}
        >
          Validate Configuration File
        </Button>
      </ActionGroup>
    </Form>
  );
};
