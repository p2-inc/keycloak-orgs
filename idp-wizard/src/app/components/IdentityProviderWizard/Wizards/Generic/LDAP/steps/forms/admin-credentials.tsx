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

const AdminCredentialsSchema = Yup.object().shape({
  adminUsername: Yup.string().required("Admin username is a required field."),
  adminPassword: Yup.string().required("Admin password is a required field."),
});

export type AdminCrednetialsConfig = {
  adminUsername: string;
  adminPassword: string;
};

type Props = {
  handleFormSubmit: ({
    adminUsername,
    adminPassword,
  }: AdminCrednetialsConfig) => API_RETURN_PROMISE;
  formActive?: boolean;
};

export const AdminCredentials: FC<Props> = ({
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
      adminUsername: "",
      adminPassword: "",
    },
    onSubmit: async (values) => {
      const resp = await handleFormSubmit(values);
      setSubmissionResp(resp);
      setSubmitting(false);
    },
    validationSchema: AdminCredentialsSchema,
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
        label="Admin Username"
        isRequired
        fieldId="adminUsername"
        validated={hasError("adminUsername")}
        helperTextInvalid={errors.adminUsername}
      >
        <TextInput
          isRequired
          id="adminUsername"
          name="adminUsername"
          value={values.adminUsername}
          onChange={(val, e) => handleChange(e)}
          validated={hasError("adminUsername")}
          isDisabled={!formActive}
        />
      </FormGroup>

      <FormGroup
        label="Admin Password"
        isRequired
        fieldId="adminPassword"
        validated={hasError("adminPassword")}
        helperTextInvalid={errors.adminPassword}
      >
        <TextInput
          isRequired
          id="adminPassword"
          name="adminPassword"
          value={values.adminPassword}
          onChange={(val, e) => handleChange(e)}
          validated={hasError("adminPassword")}
          isDisabled={!formActive}
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
