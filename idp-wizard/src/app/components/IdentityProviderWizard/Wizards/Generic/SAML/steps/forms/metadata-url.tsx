import React, { FC, useState } from "react";
import { useFormik } from "formik";
import {
  ActionGroup,
  Alert,
  Button,
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

const MetadataUrlSchema = Yup.object().shape({
  metadataUrl: Yup.string()
    .url("Metadata Url should be a valid Url.")
    .required("Metadata Url is a required field."),
});

type Props = {
  handleFormSubmit: ({
    metadataUrl,
  }: {
    metadataUrl: string;
  }) => API_RETURN_PROMISE;
  formActive?: boolean;
};

export const MetadataUrlForm: FC<Props> = ({
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
  } = useFormik({
    initialValues: {
      metadataUrl: "",
    },
    onSubmit: async (values) => {
      const resp = await handleFormSubmit(values);
      setSubmissionResp(resp);
      setSubmitting(false);
    },
    validationSchema: MetadataUrlSchema,
  });

  const hasError =
    errors.metadataUrl && touched.metadataUrl ? "error" : "default";

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
        label="Metadata Url"
        isRequired
        fieldId="metadataUrl"
        validated={hasError}
        helperTextInvalid={errors.metadataUrl}
      >
        <TextInput
          isRequired
          id="metadataUrl"
          name="metadataUrl"
          value={values.metadataUrl}
          onChange={(val, e) => handleChange(e)}
          validated={hasError}
          isDisabled={!formActive}
        />
      </FormGroup>
      <ActionGroup style={{ marginTop: 0 }}>
        <Button
          type="submit"
          isDisabled={isSubmitting || !formActive}
          isLoading={isSubmitting}
        >
          Validate URL
        </Button>
      </ActionGroup>
    </Form>
  );
};
