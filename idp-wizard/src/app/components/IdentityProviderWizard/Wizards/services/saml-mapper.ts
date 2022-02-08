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
};

export const SamlUserAttributeMapper = async ({
  alias,
  attributes,
  keys: { serverUrl, realm },
}: Props) => {
  const mapAttribute = async ({
    attributeName,
    friendlyName,
    userAttribute,
  }: AttributesConfig) => {
    return await Axios.post(
      `${serverUrl}/admin/realms/${realm}/identity-provider/instances/${alias}/mappers`,
      {
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
      }
    );
  };

  return Promise.all(attributes.map((atr) => mapAttribute(atr)));
};
