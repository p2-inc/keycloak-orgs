> :rocket: **Try it for free** in the Phase Two Enhanced [Keycloak as a service](https://phasetwo.io/?utm_source=github&utm_medium=readme&utm_campaign=idp-wizard).

# Identity Provider and Directory Sync setup wizards for Keycloak

Phase Two SSO and Directory Sync setup wizards for on-prem onboarding and enterprise SaaS self-management. This application uses the [Keycloak Admin API](https://www.keycloak.org/docs-api/24.0.1/rest-api/index.html) and the [Phase Two Organizations API](https://phasetwo.io/api/phase-two-admin-rest-api) to provide wizards for onboarding customer Identity Providers. The goal of these wizards is to solve the complex and error-prone process of connecting a vendor identity system a bit easier, and to avoid exposing customers to the Keycloak UI.

In addition to providing support for Identity Providers using OIDC and SAML, the wizards also supports Directory Synchronization protocols (aka "User Federation" in Keycloak) such as LDAP.

![youtube-video-gif](https://github.com/p2-inc/idp-wizard/assets/244253/e9b421c0-b487-4c07-9eed-87ea89fc574b)

## Quick start

The easiest way to get started is our [Docker image](https://quay.io/repository/phasetwo/phasetwo-keycloak?tab=info). Documentation and examples for using it are in the [phasetwo-containers](https://github.com/p2-inc/phasetwo-containers) repo. The most recent version of this extension is included.

## Configuration

There are some reasonable defaults used for the configuration, but the behavior of the wizards depends on a few variables, stored as Realm attributes.

| Realm attribute key                             | Default     | Description                                                                                                                                                                                                                                                                                                                                                                                      |
| ----------------------------------------------- | ----------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| `_providerConfig.wizard.apiMode`                | `onprem`    | `onprem` or `cloud`. `onprem` uses the Keycloak Admin APIs to set up an Identity Provider, so the user must have the correct `realm-management` roles. `cloud` uses the Phase Two Organizations API, so the user must have membership in an organization with the correct organization roles. A "picker" will be shown to the user if they have both and/or roles in more than one organization. |
| `_providerConfig.wizard.emailAsUsername`        | `false`     | When building Identity Provider mappers, should the IdP email address be mapped to the Keycloak `username` field.                                                                                                                                                                                                                                                                                |
| `_providerConfig.wizard.enableDashboard`        | `true`      | Show a minimal dashboard showing the state of the setup.                                                                                                                                                                                                                                                                                                                                         |
| `_providerConfig.wizard.enableDirectorySync`    | `true`      | Show Directory Sync section.                                                                                                                                                                                                                                                                                                                                                                     |
| `_providerConfig.wizard.enableGroupMapping`     | `true`      | Currently does nothing.                                                                                                                                                                                                                                                                                                                                                                          |
| `_providerConfig.wizard.enableIdentityProvider` | `true`      | Show Identity Provider section.                                                                                                                                                                                                                                                                                                                                                                  |
| `_providerConfig.wizard.enableLdap`             | `true`      | Allow LDAP config.                                                                                                                                                                                                                                                                                                                                                                               |
| `_providerConfig.wizard.enableScim`             | `true`      | Allow SCIM config. (not currently used)                                                                                                                                                                                                                                                                                                                                                          |
| `_providerConfig.wizard.trustEmail`             | `false`     | Toggle _trust email_ in the IdP config.                                                                                                                                                                                                                                                                                                                                                          |
| `_providerConfig.assets.logo.url`               | _none_      | URL for logo override. Inherited from `keycloak-orgs` config so we can use the same logo.                                                                                                                                                                                                                                                                                                        |
| `_providerConfig.wizard.appName`                | `Phase Two` | App name to appear in the HTML title.                                                                                                                                                                                                                                                                                                                                                            |

## Building and installing

This uses the `frontend-maven-plugin` to build UI code and then packages it as a jar file that can be installed as an extension in Keycloak. Checkout this project and run `mvn package`, which will produce a jar in the `target/` directory. Place the jar in the `providers` dir of your Keycloak distribution.

### Dependencies

This extension depends on 2 other extensions. You must install all of the jars of the other extensions for this to function properly. Please see the documentation in those repos for installation instructions.

- [keycloak-orgs](https://github.com/p2-inc/keycloak-orgs)
- [keycloak-scim](https://github.com/p2-inc/keycloak-scim) (not currently used or required)

### Compatibility

Although it has been developed and working since Keycloak 14.0.0, the extensions are currently known to work with Keycloak > 23.0.0. Additionally, because of the fast pace of breaking changes since Keycloak "X" (Quarkus version), we don't make any guarantee that this will work with any version other than it is packaged with in the [Docker image](https://quay.io/repository/phasetwo/phasetwo-keycloak?tab=tags).

## Vendors

Wizards are currently available for the following vendors.

| Vendor    | SAML               | OIDC               | LDAP               | SCIM | Other |
| --------- | ------------------ | ------------------ | ------------------ | ---- | ----- |
| ADFS      | :white_check_mark: |                    | :white_check_mark: |      |       |
| AWS       | :white_check_mark: |                    |                    |      |       |
| Auth0     | :white_check_mark: | :white_check_mark: |                    |      |       |
| Azure     | :white_check_mark: |                    |                    |      |       |
| Duo       | :white_check_mark: |                    |                    |      |       |
| Generic   | :white_check_mark: | :white_check_mark: | :white_check_mark: |      |       |
| Google    | :white_check_mark: |                    |                    |      |       |
| JumpCloud | :white_check_mark: |                    |                    |      |       |
| Okta      | :white_check_mark: |                    | :white_check_mark: |      |       |
| OneLogin  | :white_check_mark: |                    |                    |      |       |
| PingOne   | :white_check_mark: |                    |                    |      |       |

## Contributing

> :moneybag: :dollar: A $250US bounty will be paid for each complete and accepted vendor wizard that has been labeled with [bounty](https://github.com/p2-inc/idp-wizard/labels/bounty). Please file a PR with your implementation and reference the issue to be considered for the bounty. Acceptance of PRs is at the sole discretion of Phase Two, Inc.

Note: By submitting any code, documentation, or other materials submitted to this repository by pull request, you are immediately granting Phase Two, Inc. the copyright and an exclusive, perpetual, unlimited license to use it in this and any derivative works.

### Working with the code

Run the latest version of the Phase Two enhanced Keycloak distribution:

```bash
docker run --name phasetwo_test --rm -p 8080:8080 \
    -e KEYCLOAK_ADMIN=admin \
    -e KEYCLOAK_ADMIN_PASSWORD=admin \
    -e KC_HTTP_RELATIVE_PATH=/auth \
    quay.io/phasetwo/phasetwo-keycloak:latest \
    start-dev \
    --spi-email-template-provider=freemarker-plus-mustache \
    --spi-email-template-freemarker-plus-mustache-enabled=true
```

Build and run a local container with the idp-wizard extension (uses `Dockerfile` + `docker-compose.yml` in this repo):

```bash
mvn clean package
docker compose up --build
```

Create a Realm, and in the `idp-wizard` Client configuration, update redirect URI for `http://localhost:9090/*` (default for the IdP wizard) and add `http://localhost:9090` to the Web Origins. Download the Client's `keycloak.json` and put it in `src/keycloak.json`.

Using wizard at a different relative path than `/auth`? If so, make sure to update the following:

- `RELATIVE_PATH` within [routes.tsx](./src/app/routes.tsx)
- `wizard.ftl` ([login](./ext/main/resources/theme/wizard/login/wizard.ftl), [templates](./ext/main/resources/theme-resources/templates/wizard.ftl)) `<base href...`
- [keycloak.json](./src/keycloak.json) key of `auth-server-url`

Start the idp-wizard:

```bash
git clone https://github.com/p2-inc/idp-wizard
cd idp-wizard
pnpm install && pnpm start:dev
```

## License

The extensions herein are used in the [Phase Two](https://phasetwo.io) cloud offering, and are released here as part of its commitment to making its [core extensions](https://phasetwo.io/docs/introduction/open-source) open source. Please consult the [license](COPYING) for information regarding use.

We’ve changed the license of our core extensions from the AGPL v3 to the [Elastic License v2](https://github.com/elastic/elasticsearch/blob/main/licenses/ELASTIC-LICENSE-2.0.txt).

- Our blog post on the subject https://phasetwo.io/blog/licensing-change/
- An attempt at a clarification https://github.com/p2-inc/keycloak-orgs/issues/81#issuecomment-1554683102

---

All other documentation, source code and other files in this repository are Copyright 2024 Phase Two, Inc.
