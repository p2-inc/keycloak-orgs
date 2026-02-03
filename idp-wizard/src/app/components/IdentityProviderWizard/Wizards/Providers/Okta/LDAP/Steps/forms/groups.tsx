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
import { API_RETURN, API_STATUS } from "@app/configurations/api-status";

const ServerConfigSchema = Yup.object().shape({
  userFilter: Yup.string(),
  groupFilter: Yup.string(),
});

export type GroupConfig = {
  groupFilter?: string;
};

type Props = {
  handleFormSubmit: ({ groupFilter }: GroupConfig) => API_RETURN;
  formActive?: boolean;
  config: GroupConfig;
};

export const LdapGroupFilter: FC<Props> = ({
  handleFormSubmit,
  config,
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
      groupFilter: config.groupFilter || "",
    },
    onSubmit: async (values) => {
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

      <HelperText>
        <HelperTextItem variant="indeterminate">
          If you wish to filter the groups returned by your LDAP server, you can
          optionally add LDAP queries here:
        </HelperTextItem>
      </HelperText>
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
          placeholder="creator,viewer"
        />
      </FormGroup>

      <ActionGroup style={{ marginTop: 0 }}>
        <Button
          type="submit"
          isDisabled={isSubmitting || !formActive}
          isLoading={isSubmitting}
        >
          Save groups
        </Button>
      </ActionGroup>
    </Form>
  );
};
