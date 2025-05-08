
# Import/Export

## Contents

* [Import/Export feature](#importexport)
    * [Contents](#contents)
    * [Overview](#overview)
    * [Import](#import)
        * [Organization import/export schema](#organization-importexport-schema)
        * [Organization roles import/export schema](#organization-roles-importexport-schema)
        * [IDP link import/export schema](#identity-provider-link-importexport-schema)
        * [Members import/export schema](#members-importexport-schema)
        * [Invitations import/export schema](#invitations-importexport-schema)
    * [Export](#export)

## Overview

This documents describes the functionality for importing and exporting organizations from a realm.

## Import

Organizations can be imported, by performing a `POST` http call to `orgs/import` endpoint.  <br>
The endpoint support two query parameters `skipMissingMember` and `skipMissingIdp`. <br>


The import functionality is transactional meaning that all elements in the `organizations` array must be imported in order to complete successfully.


If both `skipMissingMember` and `skipMissingIdp`are set to `false` the import will be strict, meaning that the realm should contain the all users and idps which are referred in the import json file. <br>

E.q.:
```
curl --location 'https://{$fqdn}/auth/realms/{{$realm}}/orgs/import?skipMissingMember=false&skipMissingIdp=false' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer {{$access_token}}' \
--data-raw '{
    "organizations": [
        {
            "organization": {
                "id": "0196afb8-60de-7838-91c1-092d8fe5e150",
                "name": "test",
                "displayName": "test",
                "url": "test",
                "domains": [
                    "test.com",
                    "test2.com"
                ],
                "attributes": {
                    "attr1": [
                        "attr1"
                    ]
                }
            },
            "roles": [
                {
                    "name": "role1",
                    "description": ""
                },
                {
                   "name": "role2",
                    "description": "role2"
                }
            ],
            "idpLink": "keycloak-oidc",
            "members": [
                {
                    "username": "testUser",
                    "roles": [
                        "role1",
                        "manage-members"
                    ]
                }
            ],
            "invitations": [
                {
                    "email": "new_user@test.com",
                    "inviterUsername": "testUser",
                    "roles": ["role2"],
                    "redirectUri": "",
                    "attributes": {}
                }
            ]
        },
        {
            "organization": {
                "name": "test2",
                "displayName": "test",
                "url": "",
                "domains": [],
                "attributes": {}
            },
            "roles": [
                {
                    "name": "view-organization"
                },
                {
                    "name": "manage-organization"
                },
                {
                    "name": "view-members"
                },
                {
                    "name": "manage-members"
                },
                {
                    "name": "view-roles"
                },
                {
                    "name": "manage-roles"
                },
                {
                    "name": "view-invitations"
                },
                {
                    "name": "manage-invitations"
                },
                {
                    "name": "view-identity-providers"
                },
                {
                    "name": "manage-identity-providers"
                },
                {
                    "name": "role2_test",
                    "description": "gdssdg"
                }
            ],
            "members": [
                {
                    "username": "testUser2",
                    "roles": [
                        "view-identity-providers",
                        "role2_test"
                    ]
                },
                {
                    "username": "testUser3",
                    "roles": [
                        "view-organization",
                        "role2_test"
                    ]
                }
            ],
            "invitations": []
        }
    ]
}'
```

### Organization import/export schema

To import an organization use the following schema.

```
{
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
| `id`                   | `false`  |
| `name`                 | `true`   |
| `displayName`          | `false`  |
| `url`                  | `false`  |
| `domains`              | `false`  |
| `attributes`           | `false`  |

### Organization roles import/export schema

To import an organization roles use the following schema.

```
{
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

To add an identity provider to a organization use the following schema.

If the `skipMissingIdp` is set to `false`, the identity provider with the `alias` mentioned in the property `idpLink` must be present the realm `identityProviders`.

If the `skipMissingIdp` is set to `true`, if the identity provider with the `alias` mentioned in the property `idpLink` is not present the realm `identityProviders` the import will ignore it.

```
{
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

To add a member to an organization use the following schema.


If the `skipMissingMember` is set to `false`:
- The users from the `members` must be defined in realm `users`.
- The roles associated to a `member` must be defined in organization `roles`.


If the `skipMissingMember` is set to `true`:
- The users from the `members` which are not found in the realm `users` will be ignored.

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

To add an invitations to an organization use the following schema.  
The invitation `email` should not belong to a existing member of the organization.  

If the `skipMissingMember` is set to `false`:
- The `inviter` must be defined in `users` import schema.


If the `skipMissingMember` is set to `true`:
- If the `inviter` is not found in the realm `users` the invitation import will be skipped.

```
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

Using Keycloak a `GET` http call to `orgs/export` the realm representation should include the `organizations`. Organization will not
contain `members` and `invitations` if the flag `exportMembersAndInvitations` is set to `false`.<br>

E.q.:
```
curl --location 'https://{$fqdn}/auth/realms/{{$realm}}/orgs/export?exportMembersAndInvitations=true' \
--header 'Authorization: Bearer {{$access_token}}'
```
