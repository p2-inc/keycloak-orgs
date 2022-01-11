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

const metadataFileSchema = Yup.object().shape({
  metadataFile: Yup.mixed().required("Metadata File is required."),
});

type Props = {
  handleFormSubmit: ({
    metadataFile,
  }: {
    metadataFile: File;
  }) => API_RETURN_PROMISE;
  formActive?: boolean;
};

export const MetadataFile: FC<Props> = ({
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
      metadataFile: "",
    },
    onSubmit: async (values) => {
      const resp = await handleFormSubmit(values);
      setSubmissionResp(resp);
      setSubmitting(false);
    },
    validationSchema: metadataFileSchema,
  });

  const hasError =
    errors.metadataFile && touched.metadataFile ? "error" : "default";

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
        label="Metadata File"
        isRequired
        fieldId="metadataFile"
        className="form-label"
        validated={hasError}
        helperTextInvalid={errors.metadataFile}
      >
        <FileUpload
          id="metadataFile"
          value={values.metadataFile}
          filename={values.metadataFile?.name}
          filenamePlaceholder="Drop or choose metadata file .xml to upload."
          browseButtonText="Select"
          onChange={(val) => setFieldValue("metadataFile", val)}
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
          Validate Metadata File
        </Button>
      </ActionGroup>
    </Form>
  );
};
