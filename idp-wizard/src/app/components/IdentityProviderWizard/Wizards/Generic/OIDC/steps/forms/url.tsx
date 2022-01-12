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

const OpenIdUrlSchema = Yup.object().shape({
  openIdUrl: Yup.string()
    .url("Metadata Url should be a valid Url.")
    .required("Metadata Url is a required field."),
});

type Props = {
  handleFormSubmit: ({ url }: { url: string }) => API_RETURN_PROMISE;
  formActive?: boolean;
  url?: string;
};

export const OpenIdUrl: FC<Props> = ({
  handleFormSubmit,
  formActive = true,
  url,
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
      openIdUrl: url || "",
    },
    onSubmit: async (values) => {
      const resp = await handleFormSubmit({ url: values.openIdUrl });
      setSubmissionResp(resp);
      setSubmitting(false);
    },
    validationSchema: OpenIdUrlSchema,
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
        label="OpenID Endpoint Configuration URL"
        isRequired
        fieldId="openIdUrl"
        validated={hasError("openIdUrl")}
        helperTextInvalid={errors.openIdUrl}
      >
        <TextInput
          isRequired
          id="openIdUrl"
          name="openIdUrl"
          value={values.openIdUrl}
          onChange={(val, e) => handleChange(e)}
          validated={hasError("openIdUrl")}
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
