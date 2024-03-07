# Import/Export

## Contents

* [Import/Export feature](#importexport)
    * [Contents](#contents)
    * [Overview](#overview)
    * [Import](#import)
        * [Organization import/export schema](#organization-importexport-schema)
        * [Organization roles import/export schema](#organization-roles-importexport-schema)
        * [IDP link import/export schema](#idp-link-importexport-schema)
        * [Members import/export schema](#members-importexport-schema)
        * [Invitations import/export schema](#invitations-importexport-schema)
    * [Export](#export)
    * [Compatibility](#compatibility)

## Overview

This documents describes the functionality for importing and exporting a realm containing organizations.

## Import

Organizations can be imported, by adding `organizations` in the realm representation json.

```
{
  "realm": "org-realm",
  "enabled": true,
  .....
  "organizations": [...]
}
```

### Organization import/export schema

To import an organization use the following schema.

```
{
  "realm": "org-realm",
  "enabled": true,
  .....
  "organizations": [
    {
      "organization": {
        "name": "test",
        "displayName": "test name",
        "url": "test.com",
        "domains": [
          "test-realm.com",
          "test-realm.org"
        ],
        "attributes": {}
      }
    }
    ....
  ]
}
```

| Organization attribute | Required |
|------------------------|----------|
| `name`                 | `true`   |
| `displayName`          | `false`  |
| `url`                  | `false`  |
| `domains`              | `false`  |
| `attributes`           | `false`  |

### Organization roles import/export schema

To import an organization roles use the following schema.

```
{
  "realm": "org-realm",
  "enabled": true,
  .......
  "organizations": [
    {
      "organization": {
        "name": "test"
      },
      "roles": [
        ....
        {
          "name": "test_role1"
        },
        {
          "name": "test_role2",
          "description": "test_role2 description"
        }
      ]
    }
  ]
}
```

| Role attribute | Required |
|----------------|----------|
| `name`         | `true`   |
| `description`  | `false`  |

The default organization roles are created even if not defined in the `roles`.

### Identity provider link import/export schema

To add an identity provider to a organization use the following schema. <br>
The identity provider with the `alias` mentioned in the property `idpLink` must be defined in the realm `identityProviders`.

```
{
  "realm": "org-realm",
  "enabled": true,
  "identityProviders": [
    {
      "alias": "keycloak-oidc",
       ......
    }
  ]
  .....
  "organizations": [
    {
      "organization": {
        "name": "test3"
      }
      "idpLink": "keycloak-oidc",
    }
    ....
  ]
}
```

### Members import/export schema

To add a member to an organization use the following schema.<br>
The users from the `members` must be defined in realm `users`.<br>
The roles associated to a `member` must be defined in organization `roles`.

```
{
  "realm": "org-realm",
  "enabled": true,
  .....
  "users": [
    {
      "username": "test",
      "enabled": true
    },
    {
      "username": "test2",
      "enabled": true
    }
    .......
  ],
  "organizations": [
    {
      "organization": {
        "name": "test1"
      },
      "roles": [
        {
          "name": "test_role1"
        }
      ],
      "members": [
        {
          "username": "test",
          "roles": []
        },
        {
          "username": "test2",
          "roles": [
            "test_role1",
            "view-members",
            "manage-members"
          ]
        }
        ....
      ]
    }
  ]
}

```

| Member attribute | Required |
|------------------|----------|
| `username`       | `true`   |
| `roles`          | `false`  |

### Invitations import/export schema

To add an invitations to an organization use the following schema. <br>
The invitation `email` should not belong to a member of the organization.<br>
The `inviter` must be defined in `users` import schema.

```
 "realm": "org-realm",
  "enabled": true,
  ......
  "users": [
    {
      "username": "test",
      "enabled": true
    }
  ],
  "organizations": [
    {
      "organization": {
        "name": "test"
      },
      "roles": [
        {
          "name": "test_role",
          "description": ""
        }
      ],
      "invitations": [
        {
          "email": "test+2@phasetwo.io",
          "inviterUsername": "test",
          "roles": ["test_role"],
          "redirectUri": "",
          "attributes": {}
        }
      ]
    }
  ]
```

| Invitation attribute | Required |
|----------------------|----------|
| `email`              | `true`   |
| `inviterUsername`    | `true`   |
| `roles`              | `false`  |
| `redirectUri`        | `false`  |
| `attributes`         | `false`  |

## Export

Using Keycloak `/partial-export` the realm representation should include the `organizations`. Organization will not
contain `members` and `invitations`.<br>
Using startup export functionality [Configuring how users are exported](https://www.keycloak.org/server/importExport#_exporting_a_realm_to_a_directory)
the realm representation should include the `organizations`. Organization should contain `members` and `invitations`.

## Compatibility

### Standard Keycloak -> PhaseTwo

A realm can be created using a valid realm representation json for the current Keycloak version.

### PhaseTwo -> Standard Keycloak

To port back from PhaseTwo to standard Keycloak the `organizations` must be removed from the realm representation json.