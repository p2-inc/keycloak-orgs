import React, { FC, useState } from "react";
import { IdPButton } from "./components/IdPButton";
import { IdPProtocolSelector } from "./IdPProtocolSelector";
import { useKeycloak } from "@react-keycloak/web";

import azureLogo from "@app/bgimages/logos/azure_logo.png";
import oktaLogo from "@app/bgimages/logos/okta_logo.png";
import cyberarkLogo from "@app/bgimages/logos/cyberark_logo.png";
import adfsLogo from "@app/bgimages/logos/adfs_logo.png";
import authoLogo from "@app/bgimages/logos/auth0_logo.png";
import googleLogo from "@app/bgimages/logos/google_saml_logo.png";
import jumpcloudLogo from "@app/bgimages/logos/jumpcloud_logo.png";
import oneloginLogo from "@app/bgimages/logos/onelogin_logo.png";
import openidLogo from "@app/bgimages/logos/openid_logo.png";
import pingFedLogo from "@app/bgimages/logos/ping_federate_logo.png";
import pingOneLogo from "@app/bgimages/logos/ping_one_logo.png";
import samlLogo from "@app/bgimages/logos/saml_logo.png";
import vmwareLogo from "@app/bgimages/logos/vmware_logo.png";
import { Link } from "react-router-dom";
import {
  Button,
  Flex,
  FlexItem,
  PageSection,
  PageSectionVariants,
  Stack,
  StackItem,
} from "@patternfly/react-core";

interface IIDPType {
  name: string;
  id: string;
  imageSrc: string;
  active: boolean | false;
}

export const idpList: IIDPType[] = [
  {
    name: "Azure",
    imageSrc: azureLogo,
    active: true,
    id: "azure",
  },
  { name: "Okta", imageSrc: oktaLogo, active: true, id: "okta" },
  { name: "ADFS", imageSrc: adfsLogo, active: false, id: "adfs" },
  {
    name: "Auth0",
    imageSrc: authoLogo,
    active: false,
    id: "auth0",
  },
  {
    name: "Cyberark",
    imageSrc: cyberarkLogo,
    active: false,
    id: "cyberark",
  },
  {
    name: "Google SAML",
    imageSrc: googleLogo,
    active: false,
    id: "googlesaml",
  },
  {
    name: "Jumpcloud",
    imageSrc: jumpcloudLogo,
    active: false,
    id: "jumpcloud",
  },
  {
    name: "OneLogin",
    imageSrc: oneloginLogo,
    active: false,
    id: "onelogin",
  },
  {
    name: "OpenID",
    imageSrc: openidLogo,
    active: false,
    id: "openid",
  },
  {
    name: "Ping Federate",
    imageSrc: pingFedLogo,
    active: false,
    id: "pingfederate",
  },
  {
    name: "Ping One",
    imageSrc: pingOneLogo,
    active: false,
    id: "pingone",
  },
  { name: "SAML", imageSrc: samlLogo, active: false, id: "saml" },
  {
    name: "VMWare",
    imageSrc: vmwareLogo,
    active: false,
    id: "vmware",
  },
];

export const IdentityProviderSelector: FC = () => {
  const { keycloak } = useKeycloak();

  return (
    <PageSection variant={PageSectionVariants.light}>
      <Stack hasGutter>
        <StackItem>
          <Flex>
            <FlexItem align={{ default: "alignRight" }}>
              <Link to="/">
                <Button variant="link" isInline>
                  My Dashboard
                </Button>
              </Link>
            </FlexItem>
            <FlexItem>
              <Button variant="link" isInline onClick={() => keycloak.logout()}>
                Logout
              </Button>
            </FlexItem>
          </Flex>
        </StackItem>
        <StackItem isFilled>
          <div className="container">
            <div className="vertical-center">
              <h1>Choose your Identity Provider</h1>
              <h2>This is how users will sign in to demo.phasetwo.io</h2>
              <div className="selection-container">
                {idpList.map(({ name, imageSrc, active, id }: IIDPType) => {
                  return (
                    <Link to={active ? `/idp/${id}/protocol` : "#"} key={id}>
                      <IdPButton
                        key={name}
                        text={name}
                        image={imageSrc}
                        active={active}
                      />
                    </Link>
                  );
                })}
              </div>
            </div>
          </div>
        </StackItem>
      </Stack>
    </PageSection>
  );
};
