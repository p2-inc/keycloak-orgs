const realmUri = Cypress.config('baseUrl') + "realms/master";
const authUri = realmUri.concat("/protocol/openid-connect/auth");
const tokenUri = realmUri.concat("/protocol/openid-connect/token");
const accountUri = realmUri.concat("/account?userProfileMetadata=true");
const orgsUri = realmUri.concat("/orgs");
const activeOrganizationUri = realmUri.concat("/users/active-organization");

const loginUri = authUri.concat(
    '?response_type=code',
    '&client_id=account',
    '&scope=openid',
    '&redirect_uri=', realmUri.concat("/account")
);
const loginUriSelectAccount = loginUri.concat('&prompt=select_account');
const loginUriAccountHint = loginUri.concat('&account_hint=')

export { tokenUri, orgsUri, accountUri, activeOrganizationUri, loginUriSelectAccount, loginUriAccountHint }
