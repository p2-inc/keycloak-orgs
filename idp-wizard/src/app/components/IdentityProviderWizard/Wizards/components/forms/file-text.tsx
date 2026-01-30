import React, { FC, useState } from "react";
import { useFormik } from "formik";
import {
  ActionGroup,
  Alert,
  Button,
  Form,
  FormAlert,
  FormGroup,
  TextArea,
} from "@patternfly/react-core";
import * as Yup from "yup";
import {
  API_RETURN,
  API_RETURN_PROMISE,
  API_STATUS,
} from "@app/configurations/api-status";

const IdpMetadata = Yup.object().shape({
  idpMetadata: Yup.string().required("IDP Metadata is required."),
});

type Props = {
  handleFormSubmit: ({
    idpMetadata,
  }: {
    idpMetadata: string;
  }) => API_RETURN_PROMISE;
  formActive?: boolean;
  idpMetadataLabel?: string;
};

export const FileText: FC<Props> = ({
  handleFormSubmit,
  formActive = true,
  idpMetadataLabel = "IDP Metadata",
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
      idpMetadata: "",
    },
    onSubmit: async (values) => {
      const resp = await handleFormSubmit(values);
      setSubmissionResp(resp);
      setSubmitting(false);
    },
    validationSchema: IdpMetadata,
  });

  const hasError =
    errors.idpMetadata && touched.idpMetadata ? "error" : "default";

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
        label={idpMetadataLabel}
        isRequired
        fieldId="idpMetadata"
        validated={hasError}
        helperTextInvalid={errors.idpMetadata}
        helperText="Paste identity provider metadata."
      >
        <TextArea
          id="idpMetadata"
          name="idpMetadata"
          isRequired
          value={values.idpMetadata}
          onChange={(val, e) => handleChange(e)}
          validated={hasError}
          aria-label="Identity Provider Metadata"
          isDisabled={!formActive}
        />
      </FormGroup>
      <ActionGroup style={{ marginTop: 0 }}>
        <Button
          type="submit"
          isDisabled={isSubmitting || !formActive}
          isLoading={isSubmitting}
        >
          Validate
        </Button>
      </ActionGroup>
    </Form>
  );
};
