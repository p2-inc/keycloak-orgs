import React, { FC, useEffect, useState } from "react";
import { useFormik } from "formik";
import {
  ActionGroup,
  Alert,
  Button,
  Checkbox,
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

export type OidcConfig = {
  authorizationUrl: string;
  tokenUrl: string;
  userInfoUrl: string;
  validateSignature: boolean;
  jwksUrl: string;
  issuer: string;
  logoutUrl: string;
};

const configSchema = Yup.object().shape({
  authorizationUrl: Yup.string()
    .url("Authorization URL should be a valid Url.")
    .required("Authorization URL is a required field."),
  tokenUrl: Yup.string()
    .url("Token URL should be a valid Url.")
    .required("Token URL is a required field."),
  userInfoUrl: Yup.string()
    .url("User Info URL should be a valid Url.")
    .required("User Info URL is a required field."),
  validateSignature: Yup.boolean(),
  jwksUrl: Yup.string().when("validateSignature", {
    is: true,
    then: (schema) => schema.required(),
  }),
  issuer: Yup.string(),
  logoutUrl: Yup.string().url("Logout URL should be a valid Url."),
});

type Props = {
  handleFormSubmit: (config: OidcConfig) => API_RETURN_PROMISE;
  formActive?: boolean;
  metadata: OidcConfig;
};

export const Config: FC<Props> = ({
  handleFormSubmit,
  formActive = true,
  metadata,
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
    setValues,
  } = useFormik({
    initialValues: metadata,
    onSubmit: async (values) => {
      const resp = await handleFormSubmit(values);
      setSubmissionResp(resp);
      setSubmitting(false);
    },
    validationSchema: configSchema,
  });

  useEffect(() => {
    if (metadata !== {}) {
      setValues({
        ...metadata,
        validateSignature: metadata.validateSignature === "true",
        useJwksUrl: metadata.useJwksUrl === "true",
      });
    }
  }, [metadata]);

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
        label="Authorization Url"
        isRequired
        fieldId="authorizationUrl"
        validated={hasError("authorizationUrl")}
        helperTextInvalid={errors.authorizationUrl}
      >
        <TextInput
          isRequired
          id="authorizationUrl"
          name="authorizationUrl"
          value={values.authorizationUrl}
          onChange={(val, e) => handleChange(e)}
          validated={hasError("authorizationUrl")}
          isDisabled={!formActive}
        />
      </FormGroup>
      <FormGroup
        label="Token Url"
        isRequired
        fieldId="tokenUrl"
        validated={hasError("tokenUrl")}
        helperTextInvalid={errors.tokenUrl}
      >
        <TextInput
          isRequired
          id="tokenUrl"
          name="tokenUrl"
          value={values.tokenUrl}
          onChange={(val, e) => handleChange(e)}
          validated={hasError("tokenUrl")}
          isDisabled={!formActive}
        />
      </FormGroup>
      <FormGroup
        label="User Info Url"
        fieldId="userInfoUrl"
        validated={hasError("userInfoUrl")}
        helperTextInvalid={errors.userInfoUrl}
        isRequired
      >
        <TextInput
          isRequired
          id="userInfoUrl"
          name="userInfoUrl"
          value={values.userInfoUrl}
          onChange={(val, e) => handleChange(e)}
          validated={hasError("userInfoUrl")}
          isDisabled={!formActive}
        />
      </FormGroup>
      <FormGroup
        label="Validate Signature"
        fieldId="validateSignature"
        validated={hasError("validateSignature")}
        helperTextInvalid={errors.validateSignature}
      >
        <Checkbox
          label="Validate Signature"
          isChecked={values.validateSignature}
          onChange={(val, e) => handleChange(e)}
          aria-label="controlled checkbox example"
          id="validateSignature"
          name="validateSignature"
          isDisabled={!formActive}
        />
      </FormGroup>
      <FormGroup
        label="JWKS Url"
        fieldId="jwksUrl"
        validated={hasError("jwksUrl")}
        helperTextInvalid={errors.jwksUrl}
      >
        <TextInput
          isRequired
          id="jwksUrl"
          name="jwksUrl"
          value={values.jwksUrl}
          onChange={(val, e) => handleChange(e)}
          validated={hasError("jwksUrl")}
          isDisabled={!formActive}
        />
      </FormGroup>
      <FormGroup
        label="Issuer"
        fieldId="issuer"
        validated={hasError("issuer")}
        helperTextInvalid={errors.issuer}
      >
        <TextInput
          isRequired
          id="issuer"
          name="issuer"
          value={values.issuer}
          onChange={(val, e) => handleChange(e)}
          validated={hasError("issuer")}
          isDisabled={!formActive}
        />
      </FormGroup>
      <FormGroup
        label="Logout Url"
        fieldId="logoutUrl"
        validated={hasError("logoutUrl")}
        helperTextInvalid={errors.logoutUrl}
      >
        <TextInput
          isRequired
          id="logoutUrl"
          name="logoutUrl"
          value={values.logoutUrl}
          onChange={(val, e) => handleChange(e)}
          validated={hasError("logoutUrl")}
          isDisabled={!formActive}
        />
      </FormGroup>
      <ActionGroup style={{ marginTop: 0 }}>
        <Button
          type="submit"
          isDisabled={isSubmitting || !formActive}
          isLoading={isSubmitting}
        >
          Validate Configuration Values
        </Button>
      </ActionGroup>
    </Form>
  );
};
