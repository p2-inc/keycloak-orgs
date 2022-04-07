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

const UrlSchema = Yup.object().shape({
  url: Yup.string()
    .url("Url should be a valid Url.")
    .required("Url is a required field."),
});

type Props = {
  handleFormSubmit: ({ url }: { url: string }) => API_RETURN_PROMISE;
  formActive?: boolean;
  url: string;
  urlLabel?: string;
};

export const UrlForm: FC<Props> = ({
  handleFormSubmit,
  formActive = true,
  url,
  urlLabel = "URL",
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
      url: url || "",
    },
    onSubmit: async (values) => {
      const resp = await handleFormSubmit(values);
      setSubmissionResp(resp);
      setSubmitting(false);
    },
    validationSchema: UrlSchema,
  });

  const hasError = errors.url && touched.url ? "error" : "default";

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
        label={urlLabel}
        isRequired
        fieldId="url"
        validated={hasError}
        helperTextInvalid={errors.url}
      >
        <TextInput
          isRequired
          id="url"
          name="url"
          value={values.url}
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
