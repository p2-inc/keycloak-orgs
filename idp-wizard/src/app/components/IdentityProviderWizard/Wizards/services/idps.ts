import { Axios } from "./axios";
import { FeatureFlagsState, useGetFeatureFlagsQuery } from "@app/services";
import IdentityProviderRepresentation from "@keycloak/keycloak-admin-client/lib/defs/identityProviderRepresentation";

const usernameAttributeName = "username";
const emailAttributeName = "email";
const firstNameAttributeName = "firstName";
const lastNameAttributeName = "lastName";
const idpUsernameAttributeName = "idpUsername";

type CreateIdpProps = {
  createIdPUrl: string;
  payload: IdentityProviderRepresentation;
  featureFlags: FeatureFlagsState | undefined;
};
  
export const CreateIdp = async ({
  createIdPUrl,
  payload,
  featureFlags
}: CreateIdpProps) => {
  payload.trustEmail = featureFlags?.trustEmail;
  return Axios.post(createIdPUrl, payload);
};

type AttributesConfig = {
  attributeName: string;
  userAttribute: string;
  friendlyName: string;
  syncMode: string;
};
  
type AttributesProps = {
  alias: string;
  attributes: AttributesConfig[];
  createIdPUrl: string;
};
  
type MapperConfig = {
  attributeName: string;
  friendlyName: string;
};

type MapperProps = {
  createIdPUrl: string;
  alias: string;
  usernameAttribute: MapperConfig;
  emailAttribute: MapperConfig;
  firstNameAttribute: MapperConfig;
  lastNameAttribute: MapperConfig;
  attributes?: AttributesConfig[];
  featureFlags: FeatureFlagsState | undefined;
};

export const SamlAttributeMapper = async ({
  createIdPUrl,
  alias,
  usernameAttribute,
  emailAttribute,
  firstNameAttribute,
  lastNameAttribute,
  attributes = [],
  featureFlags,
}: MapperProps) => {
  if (featureFlags?.emailAsUsername) {
    // create a new attribute mapper with the idpUsername from the usernameAttribute
    attributes.push({
      attributeName: usernameAttribute.attributeName,
      friendlyName: usernameAttribute.friendlyName,
      userAttribute: idpUsernameAttributeName,
      syncMode: 'INHERIT',
    });
    // update the usernameAttribute with the emailAttribute attributeName and friendlyName
    usernameAttribute.attributeName = emailAttribute.attributeName;
    usernameAttribute.friendlyName = emailAttribute.friendlyName;
  }
  attributes.push({
    attributeName: usernameAttribute.attributeName,
    friendlyName: usernameAttribute.friendlyName,
    userAttribute: usernameAttributeName,
    syncMode: featureFlags?.usernameMapperImport ? "IMPORT" : "INHERIT",
  });
  attributes.push({
    attributeName: emailAttribute.attributeName,
    friendlyName: emailAttribute.friendlyName,
    userAttribute: emailAttributeName,
    syncMode: 'INHERIT',
  });
  attributes.push({
    attributeName: firstNameAttribute.attributeName,
    friendlyName: firstNameAttribute.friendlyName,
    userAttribute: firstNameAttributeName,
    syncMode: 'INHERIT',
  });
  attributes.push({
    attributeName: lastNameAttribute.attributeName,
    friendlyName: lastNameAttribute.friendlyName,
    userAttribute: lastNameAttributeName,
    syncMode: 'INHERIT',
  });

  return SamlUserAttributeMapper({
    alias,
    attributes,
    createIdPUrl,
  });
};

export const SamlUserAttributeMapper = async ({
  alias,
  attributes,
  createIdPUrl,
}: AttributesProps) => {
  const mapAttribute = async ({
    attributeName,
    friendlyName,
    userAttribute,
    syncMode,
  }: AttributesConfig) => {
    let endpoint = `${createIdPUrl}/${alias}/mappers`;
    /*
      ? `${createIdpUrl}/${alias}/mappers`
      : `${serverUrl}/${realm}/identity-provider/instances/${alias}/mappers`;
    */
    if (!syncMode) syncMode = "INHERIT";
    return await Axios.post(endpoint, {
      identityProviderAlias: alias,
      config: {
        syncMode: syncMode,
        attributes: "[]",
        "attribute.name": attributeName,
        "attribute.friendly.name": friendlyName,
        "user.attribute": userAttribute,
      },
      name: userAttribute,
      identityProviderMapper: "saml-user-attribute-idp-mapper",
    });
  };

  return Promise.all(attributes.map((atr) => mapAttribute(atr)));
};

