import React, { FC, useEffect, useState } from "react";
import { useParams } from "react-router";
import { AzureWizard } from "./AzureWizard/AzureWizard";
import { OktaWizard } from "./OktaWizard/OktaWizard";

const Provider = () => {
  const { provider } = useParams();

  switch (provider) {
    case "okta":
      return <OktaWizard />;
    case "azure":
      return <AzureWizard />;

    default:
      return <div>No provider found</div>;
  }
};

export default Provider;
