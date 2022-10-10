import { Axios } from "./axios";

type AttributesConfig = {
  attributeName: string;
  userAttribute: string;
  friendlyName: string;
};

type Props = {
  alias: string;
  attributes: AttributesConfig[];
  keys: {
    serverUrl: string;
    realm: string;
  };
  createIdPUrl: string;
};

export const SamlUserAttributeMapper = async ({
  alias,
  attributes,
  keys: { serverUrl, realm },
  createIdPUrl,
}: Props) => {
  const mapAttribute = async ({
    attributeName,
    friendlyName,
    userAttribute,
  }: AttributesConfig) => {
    let endpoint = `${createIdPUrl}/${alias}/mappers`;
    console.log("using url for idp mapper create", endpoint);
    /*
      ? `${createIdpUrl}/${alias}/mappers`
      : `${serverUrl}/${realm}/identity-provider/instances/${alias}/mappers`;
    */
    return await Axios.post(endpoint, {
      identityProviderAlias: alias,
      config: {
        syncMode: "INHERIT",
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
