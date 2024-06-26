package io.phasetwo.service;

public class Orgs {

  public static final String ORG_OWNER_CONFIG_KEY = "home.idp.discovery.org";
  public static final String FIELD_ORG_ID = "org_id";
  public static final String ORG_AUTH_FLOW_ALIAS = "post org broker login";
  public static final String ORG_DEFAULT_POST_BROKER_FLOW_KEY =
      "_providerConfig.orgs.defaults.postBrokerFlow";
  public static final String ORG_DEFAULT_SYNC_MODE_KEY = "_providerConfig.orgs.defaults.syncMode";
  public static final String ACTIVE_ORGANIZATION = "org.ro.active";
  public static final String KC_ORGS_SKIP_MIGRATION = System.getenv("KC_ORGS_SKIP_MIGRATION");
  public static final String ORG_BROWSER_AUTH_FLOW_ALIAS = "Org Browser Flow";
  public static final String ORG_DIRECT_GRANT_AUTH_FLOW_ALIAS = "Org Direct Grant Flow";
  public static final String ORG_CONFIG_CREATE_ADMIN_USER_KEY =
          "_providerConfig.orgs.config.createAdminUser";
  public static final String ORG_CONFIG_SHARED_IDPS_KEY = "_providerConfig.orgs.config.sharedIdps";
}
