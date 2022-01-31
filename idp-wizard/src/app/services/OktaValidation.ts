import { useKeycloakAdminApi } from "../hooks/useKeycloakAdminApi";

export const oktaStepOneValidation = async (ldapConUrl: string) => {
  const [kcAdminClient, setKcAdminClientAccessToken, getRealm] = useKeycloakAdminApi();
  await setKcAdminClientAccessToken();

  const connSetting = {
    action: "testConnection",
    connectionUrl: `ldaps://${ldapConUrl}`,
    authType: "simple",
    bindDn: "",
    bindCredential: "",
    useTruststoreSpi: "ldapsOnly",
    connectionTimeout: "",
    startTls: "",
  };

  const response = await kcAdminClient.realms
    .testLDAPConnection({ realm: getRealm()! }, connSetting)
    .then((res) => {
      console.log("result", res);
      return {
        status: "success",
        message: "Successfully connected to Okta LDAP",
      };
    })
    .catch((err) => {
      console.log("error", err);
      return {
        status: "error",
        message: "Errored Connecting to Okta LDAP",
      };
    });

  return response;
};

export const oktaValidateUsernamePassword = async (
  ldapConUrl: string,
  uid: string,
  password: string,
  baseDN: string
) => {
  const [kcAdminClient, setKcAdminClientAccessToken, getServerUrl, getRealm ] = useKeycloakAdminApi();
  await setKcAdminClientAccessToken();

  const connSetting = {
    action: "testAuthentication",
    connectionUrl: `ldaps://${ldapConUrl}`,
    authType: "simple",
    bindDn: `uid=${uid}, ${baseDN}`,
    bindCredential: `${password}`,
    useTruststoreSpi: "ldapsOnly",
    connectionTimeout: "",
    startTls: "",
  };

  const testConnection = await kcAdminClient.realms
    .testLDAPConnection({ realm: getRealm()! }, connSetting)
    .then((res) => {
      console.log("result", res);
      return {
        status: "success",
        message: "Successfully connected to Okta LDAP",
      };
    })
    .catch((err) => {
      console.log("error", err);
      return {
        status: "error",
        message: "Errored Connecting to Okta LDAP",
      };
    });
  return testConnection;
};

export const oktaTestConnection = async (ldapConUrl: string) => {
  const [kcAdminClient, setKcAdminClientAccessToken, getServerUrl, getRealm ] = useKeycloakAdminApi();
  await setKcAdminClientAccessToken();
  const connSetting = {
    action: "testConnection",
    connectionUrl: `ldaps://${ldapConUrl}`,
    authType: "simple",
    bindDn: "",
    bindCredential: "",
    useTruststoreSpi: "ldapsOnly",
    connectionTimeout: "",
    startTls: "",
  };

  const testConnection = await kcAdminClient.realms
    .testLDAPConnection({ realm: getRealm()! }, connSetting)
    .then((res) => console.log("result", res))
    .catch((err) => console.log("error", err));
  return testConnection;
};

export const oktaCreateFederationAndSyncUsers = async (
  customer_id,
  login_id,
  password
) => {
  const [kcAdminClient, setKcAdminClientAccessToken, getServerUrl, getRealm ] = useKeycloakAdminApi();
  await setKcAdminClientAccessToken();

  console.log("validating", customer_id, login_id, password);

  const payload = {
    name: "ldap",
    parentId: getRealm(),
    providerId: "ldap",
    providerType: "org.keycloak.storage.UserStorageProvider",
    config: {
      enabled: ["true"],
      priority: ["0"],
      fullSyncPeriod: ["-1"],
      changedSyncPeriod: ["-1"],
      cachePolicy: ["DEFAULT"],
      evictionDay: [],
      evictionHour: [],
      evictionMinute: [],
      maxLifespan: [],
      batchSizeForSync: ["1000"],
      editMode: ["READ_ONLY"],
      importEnabled: ["true"],
      syncRegistrations: ["false"],
      vendor: ["other"],
      usePasswordModifyExtendedOp: [],
      usernameLDAPAttribute: ["uid"],
      rdnLDAPAttribute: ["uid"],
      uuidLDAPAttribute: ["entryUUID"],
      userObjectClasses: ["inetOrgPerson, organizationalPerson"],
      connectionUrl: [`ldaps://${customer_id}.ldap.okta.com`],
      usersDn: [`ou=users, dc=${customer_id}, dc=okta, dc=com`],
      authType: ["simple"],
      startTls: [],
      bindDn: [`uid=${login_id}, dc=${customer_id}, dc=okta, dc=com`],
      bindCredential: [`${password}`],
      customUserSearchFilter: [],
      searchScope: ["1"],
      validatePasswordPolicy: ["false"],
      trustEmail: ["false"],
      useTruststoreSpi: ["ldapsOnly"],
      connectionPooling: ["true"],
      connectionPoolingAuthentication: [],
      connectionPoolingDebug: [],
      connectionPoolingInitSize: [],
      connectionPoolingMaxSize: [],
      connectionPoolingPrefSize: [],
      connectionPoolingProtocol: [],
      connectionPoolingTimeout: [],
      connectionTimeout: [],
      readTimeout: [],
      pagination: ["true"],
      allowKerberosAuthentication: ["false"],
      serverPrincipal: [],
      keyTab: [],
      kerberosRealm: [],
      debug: ["false"],
      useKerberosForPasswordAuthentication: ["false"],
    },
  };

  try {
    const component = await kcAdminClient.components.create(payload);

    console.log("User Federation Created:", component);
    try {
      console.log("Syncing Users", component.id, getRealm());
      const syncResult = await kcAdminClient.userStorageProvider.sync({
        id: component.id,
        action: "triggerChangedUsersSync",
        realm: getRealm(),
      });
      console.log("Component Sync result", syncResult);

      const returnMessage = `Successfully created component (${payload.name}) with id (${component.id}). Sync status: ${syncResult.status}`;
      return {
        status: "success",
        message: returnMessage,
      };
    } catch (error) {
      console.log("sync error ", error);
      return {
        status: "error",
        syncError: error,
      };
    }
  } catch (error) {
    console.log("component creation error", error);
    return {
      status: "error",
      componentError: error,
    };
  }
};
