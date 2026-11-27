"""Realm for the four use cases, on Keycloak 26.8.2 + Phase Two.

ShopHost is a Shopify-like platform. Each store is an organization (globex, initech, acme).
Users arrive with a password, with Google, through their company's SSO, or through an
external partner IdP. The app always sends account_hint=<store>.

Rule: you get in only if you are a member of that store or hold a pending invitation to it.
Pending invitations are accepted on sign-in (ext-inv-auto-join), then Select Organization
checks membership against account_hint.
"""
import json
import sys

import requests

KC = "http://localhost:8081"
PW = "Passw0rd!"
SECRET = "test-secret"
H = {}
OVERRIDES = {}


def admin_token():
    r = requests.post(f"{KC}/realms/master/protocol/openid-connect/token",
                      data={"grant_type": "password", "client_id": "admin-cli",
                            "username": "admin", "password": "admin"})
    r.raise_for_status()
    return r.json()["access_token"]


def call(method, path, body=None, ok=(200, 201, 204), base=f"{KC}/admin/realms"):
    r = requests.request(method, f"{base}{path}", json=body, headers=H)
    if r.status_code not in ok:
        sys.exit(f"{method} {path} -> {r.status_code} {r.text[:400]}")
    return r


def user(email, first, last):
    return {"username": email, "email": email, "firstName": first, "lastName": last,
            "enabled": True, "emailVerified": True,
            "credentials": [{"type": "password", "value": PW, "temporary": False}]}


def broker_client(alias):
    return {"clientId": alias, "enabled": True, "publicClient": False, "secret": SECRET,
            "standardFlowEnabled": True, "redirectUris": [f"{KC}/realms/shophost/broker/{alias}/endpoint"]}


def idp(alias, display, post_broker, hide):
    up = f"{KC}/realms/upstream/protocol/openid-connect"
    return {"alias": alias, "displayName": display, "providerId": "keycloak-oidc", "enabled": True,
            "trustEmail": True, "hideOnLogin": hide, "firstBrokerLoginFlowAlias": "first broker login",
            "postBrokerLoginFlowAlias": post_broker,
            "config": {"clientId": alias, "clientSecret": SECRET, "clientAuthMethod": "client_secret_post",
                       "authorizationUrl": f"{up}/auth", "tokenUrl": f"{up}/token", "userInfoUrl": f"{up}/userinfo",
                       "jwksUrl": f"{up}/certs", "useJwksUrl": "true", "validateSignature": "true",
                       "issuer": f"{KC}/realms/upstream", "defaultScope": "openid email profile",
                       "syncMode": "IMPORT"}}


def ex(authenticator, req, prio, cfg=None):
    e = {"authenticator": authenticator, "authenticatorFlow": False, "requirement": req,
         "priority": prio, "userSetupAllowed": False}
    if cfg:
        e["authenticatorConfig"] = cfg
    return e


def sub(alias, req, prio):
    return {"flowAlias": alias, "authenticatorFlow": True, "requirement": req, "priority": prio,
            "userSetupAllowed": False}


def flow(alias, top, executions):
    return {"alias": alias, "description": alias, "providerId": "basic-flow", "topLevel": top,
            "builtIn": False, "authenticationExecutions": executions}


FLOWS = [
    flow("shop login", True, [
        sub("shop login - session", "ALTERNATIVE", 10),
        ex("identity-provider-redirector", "ALTERNATIVE", 20),
        sub("shop login - forms", "ALTERNATIVE", 30),
    ]),
    flow("shop login - session", False, [
        ex("auth-cookie", "REQUIRED", 10),
        ex("ext-inv-auto-join", "REQUIRED", 20),
        ex("ext-select-org", "REQUIRED", 30, "select-org-1"),
    ]),
    flow("shop login - forms", False, [
        sub("shop login - sso or password", "REQUIRED", 10),
        ex("ext-inv-auto-join", "REQUIRED", 20),
        ex("ext-select-org", "REQUIRED", 30, "select-org-2"),
    ]),
    flow("shop login - sso or password", False, [
        ex("ext-auth-home-idp-discovery", "ALTERNATIVE", 10, "home-idp"),
        ex("auth-password-form", "ALTERNATIVE", 20),
    ]),
    # Company SSO owned by a store: join that store, accept invitations, then check.
    flow("company sso post broker", True, [
        ex("ext-auth-org-add-user", "REQUIRED", 10),
        ex("ext-inv-auto-join", "REQUIRED", 20),
        ex("ext-select-org", "REQUIRED", 30, "select-org-3"),
    ]),
    # Google and external partner IdPs: accept invitations, then check.
    flow("external idp post broker", True, [
        ex("ext-inv-auto-join", "REQUIRED", 10),
        ex("ext-select-org", "REQUIRED", 20, "select-org-4"),
    ]),
]

CONFIGS = [{"alias": f"select-org-{i}", "config": {"match_by_org_name": "true"}} for i in range(1, 5)] + [
    {"alias": "home-idp", "config": {"userAttribute": "email", "forwardToFirstMatch": "true",
                                     "bypassLoginPage": "true", "forwardToLinkedIdp": "false",
                                     "requireVerifiedDomain": "false", "requireVerifiedEmail": "false"}},
]

MAPPER = {"name": "active organization", "protocol": "openid-connect",
          "protocolMapper": "oidc-active-organization-mapper",
          "config": {"included.active.organization.properties": "id, name, role, attribute",
                     "claim.name": "active_organization", "jsonType.label": "JSON",
                     "access.token.claim": "true", "id.token.claim": "true", "userinfo.token.claim": "true"}}

UPSTREAM = {
    "realm": "upstream", "enabled": True, "displayName": "External IdP (test)",
    "users": [user("erin@globex.test", "Erin", "Globex"), user("ann@acme.test", "Ann", "Acme"),
              user("sam@gmail.test", "Sam", "Invited"), user("zoe@gmail.test", "Zoe", "Stranger"),
              user("pete@partner.test", "Pete", "Partner"), user("quinn@partner.test", "Quinn", "Partner")],
    "clients": [broker_client(a) for a in ("globex-sso", "acme-sso", "google", "partner")],
}

SHOPHOST = {
    "realm": "shophost", "enabled": True, "displayName": "ShopHost", "loginWithEmailAllowed": True,
    "registrationAllowed": False, "bruteForceProtected": True,
    "authenticatorConfig": CONFIGS, "authenticationFlows": FLOWS, "browserFlow": "shop login",
    "users": [user("paula@mail.test", "Paula", "Member"), user("ivan@mail.test", "Ivan", "Invited"),
              user("nina@mail.test", "Nina", "Stranger"), user("carl@contractor.test", "Carl", "Contractor"),
              {**user("pete@partner.test", "Pete", "Partner"), "credentials": []}],
    "clients": [{"clientId": "store-admin", "enabled": True, "publicClient": True, "standardFlowEnabled": True,
                 "redirectUris": ["http://localhost:3000/*"], "webOrigins": ["+"], "protocolMappers": [MAPPER]}],
    "identityProviders": [
        idp("globex-sso", "Globex SSO", "company sso post broker", True),
        idp("acme-sso", "Acme SSO", "company sso post broker", True),
        idp("google", "Google (test)", "external idp post broker", False),
        idp("partner", "Partner IdP", "external idp post broker", True),
    ],
}


def main():
    H["Authorization"] = f"Bearer {admin_token()}"
    for realm in ("shophost", "upstream", "acme"):
        requests.delete(f"{KC}/admin/realms/{realm}", headers=H)
    call("POST", "", UPSTREAM)
    call("POST", "", SHOPHOST)

    orgs_api = f"{KC}/realms/shophost/orgs"

    def org(name, display, domains):
        r = call("POST", "", {"name": name, "displayName": display, "domains": domains}, base=orgs_api)
        return r.headers["Location"].rsplit("/", 1)[1]

    ids = {"globex": org("globex", "Globex Store", ["globex.test"]),
           "initech": org("initech", "Initech Store", ["initech.test"]),
           "acme": org("acme", "Acme Store", ["acme.test"])}

    def uid(realm, email):
        return call("GET", f"/{realm}/users?email={email}&exact=true").json()[0]["id"]

    for email, stores in [("paula@mail.test", ["globex"]), ("carl@contractor.test", ["globex", "initech"]),
                          ("pete@partner.test", ["globex"])]:
        for s in stores:
            call("PUT", f"/{ids[s]}/members/{uid('shophost', email)}", base=orgs_api)

    # Pete already has a ShopHost account linked to the partner IdP.
    call("POST", f"/shophost/users/{uid('shophost', 'pete@partner.test')}/federated-identity/partner",
         {"identityProvider": "partner", "userId": uid("upstream", "pete@partner.test"),
          "userName": "pete@partner.test"})

    # Company SSO owned by each store, routed by email domain.
    for store, alias, domain in [("globex", "globex-sso", "globex.test"), ("acme", "acme-sso", "acme.test")]:
        call("POST", f"/{ids[store]}/idps/link", {"alias": alias, "post_broker_flow": "company sso post broker",
                                                  "sync_mode": "IMPORT", "domains": [domain]}, base=orgs_api)

    for email in ("ivan@mail.test", "sam@gmail.test"):
        call("POST", f"/{ids['globex']}/invitations", {"email": email, "send": False}, base=orgs_api)

    print(json.dumps(ids))


if __name__ == "__main__":
    main()
