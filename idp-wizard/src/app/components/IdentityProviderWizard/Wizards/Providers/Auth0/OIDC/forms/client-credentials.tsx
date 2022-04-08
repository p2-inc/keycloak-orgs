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

export type ClientCreds = {
  domain: string;
  clientId: string;
  clientSecret: string;
};

const ClientCredentialsSchema = Yup.object().shape({
  domain: Yup.string().required("Domain is a required field."),
  clientId: Yup.string().required("Client Id is a required field."),
  clientSecret: Yup.string().required("Client secret is a required field."),
});

type Props = {
  handleFormSubmit: ({
    domain,
    clientId,
    clientSecret,
  }: ClientCreds) => API_RETURN_PROMISE;
  formActive?: boolean;
  credentials: ClientCreds;
};

export const ClientCredentials: FC<Props> = ({
  handleFormSubmit,
  formActive = true,
  credentials,
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
      domain: credentials.domain,
      clientId: credentials.clientId,
      clientSecret: credentials.clientSecret,
    },
    onSubmit: async (values) => {
      const resp = await handleFormSubmit(values);
      setSubmissionResp(resp);
      setSubmitting(false);
    },
    validationSchema: ClientCredentialsSchema,
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
        label="Domain"
        isRequired
        fieldId="domain"
        validated={hasError("domain")}
        helperTextInvalid={errors.domain}
      >
        <TextInput
          isRequired
          id="domain"
          name="domain"
          value={values.domain}
          onChange={(val, e) => handleChange(e)}
          validated={hasError("domain")}
          isDisabled={!formActive}
        />
      </FormGroup>
      <FormGroup
        label="Client Id"
        isRequired
        fieldId="clientId"
        validated={hasError("clientId")}
        helperTextInvalid={errors.clientId}
      >
        <TextInput
          isRequired
          id="clientId"
          name="clientId"
          value={values.clientId}
          onChange={(val, e) => handleChange(e)}
          validated={hasError("clientId")}
          isDisabled={!formActive}
        />
      </FormGroup>
      <FormGroup
        label="Client Secret"
        isRequired
        fieldId="clientSecret"
        validated={hasError("clientSecret")}
        helperTextInvalid={errors.clientSecret}
      >
        <TextInput
          isRequired
          id="clientSecret"
          name="clientSecret"
          value={values.clientSecret}
          onChange={(val, e) => handleChange(e)}
          validated={hasError("clientSecret")}
          isDisabled={!formActive}
          type="password"
        />
      </FormGroup>
      <ActionGroup style={{ marginTop: 0 }}>
        <Button
          type="submit"
          isDisabled={isSubmitting || !formActive}
          isLoading={isSubmitting}
        >
          Validate Credentials
        </Button>
      </ActionGroup>
    </Form>
  );
};
