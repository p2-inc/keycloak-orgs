import React, { FC, useState } from "react";
import { useFormik } from "formik";
import {
  ActionGroup,
  Alert,
  Button,
  Form,
  FormAlert,
  FormGroup,
  HelperText,
  HelperTextItem,
  TextInput,
} from "@patternfly/react-core";
import * as Yup from "yup";
import {
  API_RETURN,
  API_RETURN_PROMISE,
  API_STATUS,
} from "@app/configurations/api-status";

const ServerConfigSchema = Yup.object().shape({
  host: Yup.string()
    .url("LDAP host should be a valid Url.")
    .required("LDAP host is a required field."),
  sslPort: Yup.string().required("LDAP SSL Port is required."),
  baseDn: Yup.string().required("LDAP Base DN is required."),
  userBaseDn: Yup.string().required("LDAP User Base DN is required."),
  groupBaseDn: Yup.string().required("LDAP Group Base DN is required."),
  userFilter: Yup.string(),
  groupFilter: Yup.string(),
});

export type ServerConfig = {
  host: string;
  sslPort: string;
  baseDn: string;
  userBaseDn: string;
  groupBaseDn: string;
  userFilter: string;
  groupFilter: string;
};

type Props = {
  handleFormSubmit: ({
    host,
    sslPort,
    baseDn,
    userBaseDn,
    groupBaseDn,
    userFilter,
    groupFilter,
  }: ServerConfig) => API_RETURN_PROMISE;
  formActive?: boolean;
};

export const LdapServerConfig: FC<Props> = ({
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
      host: "",
      sslPort: "",
      baseDn: "",
      userBaseDn: "",
      groupBaseDn: "",
      userFilter: "",
      groupFilter: "",
    },
    onSubmit: async (values) => {
      console.log("[onSubmit]", values);
      const resp = await handleFormSubmit(values);
      setSubmissionResp(resp);
      setSubmitting(false);
    },
    validationSchema: ServerConfigSchema,
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
        label="LDAP Host"
        helperText="Must use SSL."
        isRequired
        fieldId="host"
        validated={hasError("host")}
        helperTextInvalid={errors.host}
      >
        <TextInput
          isRequired
          id="host"
          name="host"
          value={values.host}
          onChange={(val, e) => handleChange(e)}
          validated={hasError("host")}
          isDisabled={!formActive}
        />
      </FormGroup>

      <FormGroup
        label="LDAP SSL Port"
        isRequired
        fieldId="sslPort"
        validated={hasError("sslPort")}
        helperTextInvalid={errors.sslPort}
      >
        <TextInput
          isRequired
          id="sslPort"
          name="sslPort"
          value={values.sslPort}
          onChange={(val, e) => handleChange(e)}
          validated={hasError("sslPort")}
          isDisabled={!formActive}
        />
      </FormGroup>

      <FormGroup
        label="LDAP Base DN"
        isRequired
        fieldId="baseDn"
        validated={hasError("baseDn")}
        helperTextInvalid={errors.baseDn}
      >
        <TextInput
          isRequired
          id="baseDn"
          name="baseDn"
          value={values.baseDn}
          onChange={(val, e) => handleChange(e)}
          validated={hasError("baseDn")}
          isDisabled={!formActive}
        />
      </FormGroup>

      <FormGroup
        label="LDAP User Base DN"
        isRequired
        fieldId="userBaseDn"
        validated={hasError("userBaseDn")}
        helperTextInvalid={errors.userBaseDn}
      >
        <TextInput
          isRequired
          id="userBaseDn"
          name="userBaseDn"
          value={values.userBaseDn}
          onChange={(val, e) => handleChange(e)}
          validated={hasError("userBaseDn")}
          isDisabled={!formActive}
        />
      </FormGroup>

      <FormGroup
        label="LDAP Group Base DN"
        isRequired
        fieldId="groupBaseDn"
        validated={hasError("groupBaseDn")}
        helperTextInvalid={errors.groupBaseDn}
      >
        <TextInput
          isRequired
          id="groupBaseDn"
          name="groupBaseDn"
          value={values.groupBaseDn}
          onChange={(val, e) => handleChange(e)}
          validated={hasError("groupBaseDn")}
          isDisabled={!formActive}
        />
      </FormGroup>

      <HelperText>
        <HelperTextItem variant="indeterminate">
          If you wish to filter the users and groups returned by your LDAP
          server, you can optionally add LDAP queries here:
        </HelperTextItem>
      </HelperText>

      <FormGroup
        label="LDAP User Filter"
        fieldId="userFilter"
        validated={hasError("userFilter")}
        helperTextInvalid={errors.userFilter}
      >
        <TextInput
          id="userFilter"
          name="userFilter"
          value={values.userFilter}
          onChange={(val, e) => handleChange(e)}
          validated={hasError("userFilter")}
          isDisabled={!formActive}
        />
      </FormGroup>

      <FormGroup
        label="LDAP Group Filter"
        fieldId="groupFilter"
        validated={hasError("groupFilter")}
        helperTextInvalid={errors.groupFilter}
      >
        <TextInput
          id="groupFilter"
          name="groupFilter"
          value={values.groupFilter}
          onChange={(val, e) => handleChange(e)}
          validated={hasError("groupFilter")}
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
