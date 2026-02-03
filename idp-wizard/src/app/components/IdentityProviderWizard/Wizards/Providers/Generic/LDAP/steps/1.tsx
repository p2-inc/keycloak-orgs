import React, { FC } from "react";
import { CustomSelect, InstructionProps, Step } from "@wizardComponents";
import { omit, pick } from "lodash";

interface Props {
  handleConfigUpdate: (Object) => void;
  config: Object;
}

export const Step1: FC<Props> = ({
  handleConfigUpdate,
  config: configProp,
}) => {
  var vendorToUUID = {
    "Redhat Directory Server": "nsuniqueid",
    Tivoli: "uniqueidentifier",
    "Novell edirectory": "guid",
    "Active Directory": "objectGUID",
    other: "entryUUID",
  };

  const serverVendors = [
    { value: "Active Directory" },
    { value: "Redhat Directory Server" },
    { value: "Tivoli" },
    { value: "Novell eDirectory" },
    { value: "OpenLDAP" },
    { value: "Apache Directory Server" },
    { value: "Oracle Unified Directory" },
    { value: "Other" },
  ];

  const serverLocation = [
    { value: "North America" },
    { value: "South America" },
    { value: "Europe/Middle East/Africa" },
    { value: "Asia/Pacific" },
  ];

  const handleServerVendorSelect = (vendor: string) => {
    const config = {
      usernameLDAPAttribute: ["uid"],
      userObjectClasses: ["inetOrgPerson, organizationalPerson"],
      uuidLDAPAttribute: ["entryUUID"],
      rdnLDAPAttribute: [""],
      vendor: [vendor],
    };
    if (vendor === null) {
      return handleConfigUpdate(pick(configProp, ["region"]));
    }

    if (vendor === "Active Directory") {
      config.usernameLDAPAttribute = ["cn"];
      config.userObjectClasses = ["person, organizationalPerson, user"];
    }

    config.rdnLDAPAttribute = config.usernameLDAPAttribute;

    if (vendorToUUID[vendor]) {
      config.uuidLDAPAttribute = [vendorToUUID[vendor]];
    }

    handleConfigUpdate({ ...configProp, ...config });
  };

  const handleServerLocationSelect = (location: string) => {
    if (location === null) {
      return handleConfigUpdate(omit(configProp, ["region"]));
    }
    handleConfigUpdate({ ...configProp, region: [location] });
  };

  const instructions: InstructionProps[] = [
    {
      text: "What LDAP server vendor do you use? This will help to identify common default configuration values.",
      component: (
        <CustomSelect
          options={serverVendors}
          handleSelect={handleServerVendorSelect}
          selections={configProp.vendor}
        />
      ),
    },
    {
      text: "Where is your LDAP server located? This will help us to choose a network that will speed up LDAP queries.",
      component: (
        <CustomSelect
          options={serverLocation}
          handleSelect={handleServerLocationSelect}
          selections={configProp.region}
        />
      ),
    },
  ];

  return (
    <Step
      title="Step 1: Tell Us About Your Server"
      instructionList={instructions}
    />
  );
};
