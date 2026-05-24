# SCIM (experimental)

> **Experimental.** SCIM 2.0 provisioning for organizations is a new
> capability and the surface — API shape, configuration schema, and
> realm-level enablement flag — may change in a backwards-incompatible
> way before it stabilizes. Pin to a specific version of `keycloak-orgs`
> if you depend on it.

## Contents

* [Overview](#overview)
* [Enabling SCIM at the realm level](#enabling-scim-at-the-realm-level)
  * [Admin UI](#admin-ui)
  * [Realm config API](#realm-config-api)
* [Configuring SCIM for an organization](#configuring-scim-for-an-organization)
  * [SCIM tab in the Admin UI](#scim-tab-in-the-admin-ui)
  * [Organization SCIM API](#organization-scim-api)
* [Authentication modes](#authentication-modes)
* [SCIM endpoint](#scim-endpoint)

## Overview

Each organization can act as its own SCIM 2.0 service provider, with an
endpoint at:

```
/realms/{realm}/scim/v2/organizations/{orgId}/
```

External identity providers (Okta, Azure AD, etc.) call this endpoint to
provision and de-provision users into a specific organization. The
provisioned users become members of the organization and, optionally,
get linked to the organization's configured IdP.

The capability ships disabled by default and is gated by a realm-level
flag. When the flag is off:

* The **SCIM** tab is hidden in the organization detail UI.
* The configuration REST endpoints under
  `/realms/{realm}/orgs/{orgId}/scim` return **404 Not Found**.

Turn the flag on per realm, then configure each organization that
should accept SCIM traffic.

## Enabling SCIM at the realm level

### Admin UI

1. Navigate to **Organizations** in the left navigation.
2. Click the **gear icon** ("Organization settings") at the top right
   of the organization list.
3. Tick **Enable SCIM** and save.

The SCIM tab will now appear on each organization's detail page.

### Realm config API

The flag is part of the orgs realm config payload. Read or write it
via `GET` / `PUT` on `/realms/{realm}/orgs/config`.

```bash
# Read the current realm orgs config
curl -H "Authorization: Bearer $TOKEN" \
  https://{host}/auth/realms/{realm}/orgs/config
```

```json
{
  "createAdminUser": true,
  "sharedIdps": false,
  "multipleIdps": false,
  "validateIdp": false,
  "expirationInSecs": 86400,
  "scimEnabled": false
}
```

```bash
# Enable SCIM
curl -X PUT -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"scimEnabled": true, "createAdminUser": true, "sharedIdps": false, "multipleIdps": false, "validateIdp": false, "expirationInSecs": 86400}' \
  https://{host}/auth/realms/{realm}/orgs/config
```

The flag is persisted in the realm config under
`_providerConfig.orgs.config.scimEnabled`.

The caller needs `manage-realm` permission.

## Configuring SCIM for an organization

### SCIM tab in the Admin UI

Once the realm flag is on, open an organization and switch to the
**SCIM** tab. The tab always shows two read-only fields:

* **ID** — the SCIM configuration ID (always equal to the organization
  ID). Copy with the clipboard button.
* **SCIM URL** — the full URL the external IdP should call. Copy and
  paste into the SCIM provisioning settings of the upstream system.

The remaining form fields configure how the SCIM endpoint authenticates
incoming requests and how provisioned users are mapped:

| Field | Type | Description |
| --- | --- | --- |
| **Enabled** | Switch | Whether the SCIM endpoint accepts traffic for this organization. |
| **Email as username** | Switch | Force the provisioned user's `username` to mirror their `email`. When on, the username is not changed by subsequent SCIM update operations. |
| **Link to organization IdP** | Switch | Federate the provisioned user with the organization's configured IdP, so they can sign in via SSO once provisioned. |
| **Authentication type** | Select | One of `KEYCLOAK`, `EXTERNAL_JWT`, `EXTERNAL_SECRET`, `EXTERNAL_BASIC`. See [Authentication modes](#authentication-modes). |

Fields beyond the auth-type dropdown depend on the chosen mode (JWT
issuer/audience/JWKS, shared secret, or basic username/password).

> **Secrets are write-only.** Password and shared-secret inputs render
> blank when re-opening the form. To preserve the existing credential,
> leave the field blank when saving. To rotate, type a new value — the
> server replaces the stored Argon2id hash.

### Organization SCIM API

The same operations are available over REST at
`/realms/{realm}/orgs/{orgId}/scim`. Returns **404** if the realm-level
SCIM flag is off.

| Method | Path | Purpose |
| --- | --- | --- |
| `GET` | `/realms/{realm}/orgs/{orgId}/scim` | Fetch the current SCIM configuration for an organization. |
| `POST` | `/realms/{realm}/orgs/{orgId}/scim` | Create the SCIM configuration. **409** if one already exists. |
| `PUT` | `/realms/{realm}/orgs/{orgId}/scim` | Update the configuration. |
| `DELETE` | `/realms/{realm}/orgs/{orgId}/scim` | Remove the configuration. |

Required permissions:

* `GET` — `view-organizations`, or `view-identity-providers` on the org.
* `POST` / `PUT` / `DELETE` — `manage-organizations`, or
  `manage-identity-providers` on the org.

The request and response bodies are an `OrganizationScimRepresentation`:

```json
{
  "enabled": true,
  "email_as_username": false,
  "link_idp": true,
  "auth": {
    "type": "EXTERNAL_BASIC",
    "username": "okta",
    "password": "S3cret!"
  }
}
```

`auth` is polymorphic — the `type` discriminator picks the shape of the
rest of the object. See [Authentication modes](#authentication-modes)
for the four variants.

Cleartext secrets and basic passwords sent on `POST` / `PUT` are hashed
with Argon2id (PHC string format) before persistence. To leave the
existing secret in place on update, omit it (or send back the hashed
value previously returned by `GET`). To rotate, send the new cleartext.

## Authentication modes

The SCIM endpoint authenticates each inbound request using one of four
modes. Select via the `auth.type` field:

### `KEYCLOAK`

Authenticates with a Keycloak access token (Bearer). The caller must
hold the realm-management roles required to manage members of the
organization.

```json
{ "enabled": true, "auth": { "type": "KEYCLOAK" } }
```

### `EXTERNAL_JWT`

Validates an inbound JWT issued by an external IdP. Supply the issuer,
audience, and JWKS URI:

```json
{
  "enabled": true,
  "auth": {
    "type": "EXTERNAL_JWT",
    "issuer": "https://idp.example.com/",
    "audience": "scim.example.com",
    "jwks_uri": "https://idp.example.com/.well-known/jwks.json"
  }
}
```

### `EXTERNAL_SECRET`

Bearer-style shared secret. The client sends
`Authorization: Bearer <secret>`; the server compares it against the
stored Argon2id hash:

```json
{
  "enabled": true,
  "auth": {
    "type": "EXTERNAL_SECRET",
    "shared_secret": "long-random-value"
  }
}
```

### `EXTERNAL_BASIC`

HTTP Basic auth. The password is hashed with Argon2id before storage:

```json
{
  "enabled": true,
  "auth": {
    "type": "EXTERNAL_BASIC",
    "username": "okta",
    "password": "S3cret!"
  }
}
```

## SCIM endpoint

Once configured and `enabled: true`, the SCIM 2.0 service is available at:

```
{authServerUrl}/realms/{realm}/scim/v2/organizations/{orgId}/
```

It implements the standard SCIM 2.0 resource endpoints
(`/ServiceProviderConfig`, `/ResourceTypes`, `/Schemas`, `/Users`,
`/Groups`) for the organization's member graph. Configure the upstream
IdP's SCIM provisioning to point at this URL using the authentication
mode selected above.
