import React, { FC, useState } from "react";
import azureImage from "@app/images/azure/azure-logo.png";
import oktaImage from "@app/images/okta/okta-logo.png";
import { IdPButton } from "./components/IdPButton";
import { IdPProtocolSelector } from "./IdPProtocolSelector";

export const IdentityProviderSelector: FC = () => {
  const [provider, setProvider] = useState("");
  const [providerImage, setProviderImage] = useState("");

  const setProviders = (selectedProvider, selectedProviderImage) => {
    setProvider(selectedProvider);
    setProviderImage(selectedProviderImage);
  };

  return provider == "" ? (
    <div className="container">
      <div className="vertical-center">
        <h1>Choose your Identity Provider {provider}</h1>
        <h2>This is how users will sign in to demo.phasetwo.io</h2>
        <div className="selection-container">
          <IdPButton
            text="Azure"
            image={azureImage}
            onSelect={() => setProviders("Azure", azureImage)}
          />
          <IdPButton
            text="Okta"
            image={oktaImage}
            onSelect={() => setProviders("Okta", oktaImage)}
          />
        </div>
      </div>
    </div>
  ) : (
    <IdPProtocolSelector
      selectedProtocol={provider}
      selectedProtocolImage={providerImage}
      goBack={() => setProviders("", "")}
    />
  );
};
