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

const BindSchema = Yup.object().shape({
  bindDn: Yup.string().required("Okta Admin Username is a required field."),
  bindPassword: Yup.string().required(
    "Okta Admin Password is a required field."
  ),
});

export type BindConfig = {
  bindDn: string;
  bindPassword: string;
};

type Props = {
  handleFormSubmit: ({
    bindDn,
    bindPassword,
  }: BindConfig) => API_RETURN_PROMISE;
  config: BindConfig;
  formActive?: boolean;
};

export const Bind: FC<Props> = ({
  handleFormSubmit,
  formActive = true,
  config,
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
      bindDn: config.bindDn || "",
      bindPassword: config.bindPassword || "",
    },
    onSubmit: async (values) => {
      const resp = await handleFormSubmit(values);
      setSubmissionResp(resp);
      setSubmitting(false);
    },
    validationSchema: BindSchema,
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
        label="Okta Admin Username"
        isRequired
        fieldId="bindDn"
        validated={hasError("bindDn")}
        helperTextInvalid={errors.bindDn}
      >
        <TextInput
          isRequired
          id="bindDn"
          name="bindDn"
          value={values.bindDn}
          onChange={(val, e) => handleChange(e)}
          validated={hasError("bindDn")}
          isDisabled={!formActive}
        />
      </FormGroup>

      <FormGroup
        label="Okta Admin Password"
        isRequired
        fieldId="bindPassword"
        validated={hasError("bindPassword")}
        helperTextInvalid={errors.bindPassword}
      >
        <TextInput
          isRequired
          id="bindPassword"
          name="bindPassword"
          value={values.bindPassword}
          onChange={(val, e) => handleChange(e)}
          validated={hasError("bindPassword")}
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
