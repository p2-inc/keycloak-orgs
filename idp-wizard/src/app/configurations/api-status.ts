export enum API_STATUS {
  SUCCESS = "SUCCESS",
  ERROR = "ERROR",
}

export type API_RETURN = {
  status: API_STATUS;
  message: string;
};

export type API_RETURN_PROMISE = Promise<{
  status: API_STATUS;
  message: string;
}>;

export interface METADATA_CONFIG {
  validateSignature: "false" | "true";
  loginHint: "false" | "true";
  signingCertificate: string;
  enabledFromMetadata: "false" | "true";
  postBindingLogout: "false" | "true";
  postBindingResponse: "false" | "true";
  nameIDPolicyFormat: string;
  postBindingAuthnRequest: "false" | "true";
  singleSignOnServiceUrl: string;
  wantAuthnRequestsSigned: "false" | "true";
  addExtensionsElementWithKeyInfo: "false" | "true";
}
