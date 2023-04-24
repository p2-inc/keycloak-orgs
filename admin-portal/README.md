> :rocket: **Try it for free** in the Phase Two [Keycloak managed service](https://phasetwo.io/dashboard/?utm_source=github&utm_medium=readme&utm_campaign=admin-portal).

# Phase Two Admin Portal

More self-serve. More better.

The Phase Two Admin Portal ties together functionality from the Keycloak Account Console and [Phase Two Organizations](https://github.com/p2-inc/keycloak-orgs) to allow your customers' users to self-manage as much of their account and organization functionality as is possible.

The Portal is deployed as a Keycloak extension, much like the Account Console, and is available at `https://{host}/{relative-path}/realms/{realm}/portal/`

## Quick start

The easiest way to get started is our [Docker image](https://quay.io/repository/phasetwo/phasetwo-keycloak?tab=tags). Documentation and examples for using it are in the [phasetwo-containers](https://github.com/p2-inc/phasetwo-containers) repo. The most recent version of this extension is included.

## Configuration

### Requirements

Because this extension relies on the APIs provided by the [keycloak-orgs](https://github.com/p2-inc/keycloak-orgs) extension, it is required to deploy them in the same Keycloak.

### Visibility

Most of the visibilty of functionality in the Portal is controlled by user permissions. However, it is also possible to control visibility through Realm Attributes. These attributes may be set manually, or by using the [Phase Two extensions to the Keycloak Admin UI](https://github.com/p2-inc/keycloak-ui/) (**Styles**->*Portal* tab), which must also be installed in the same Keycloak. 

When setting the attributes manually, the values are:
| Key | Description | Default |
|---|---|---|
| `_providerConfig.portal.profile.enabled` | Profile section (whole) | `true` |
| `_providerConfig.portal.profile.password.enabled` | Password update | `true` |
| `_providerConfig.portal.profile.twofactor.enabled` | 2fa create/update | `true` |
| `_providerConfig.portal.profile.activity.enabled` | Device activity | `true` |
| `_providerConfig.portal.profile.linked.enabled` | Linked accounts | `true` |
| `_providerConfig.portal.org.enabled` | Organizations section (whole) | `true` |
| `_providerConfig.portal.org.details.enabled` | Details edit | `true` |
| `_providerConfig.portal.org.members.enabled` | Members list | `true` |
| `_providerConfig.portal.org.invitations.enabled` | Invitations | `true` |
| `_providerConfig.portal.org.domains.enabled` | Domains | `true` |
| `_providerConfig.portal.org.sso.enabled` | SSO | `true` |
| `_providerConfig.portal.org.events.enabled` | Events | `true` |

### Style

It is also possible to add branding to the portal. It is recommended these, along with logos, are set through the [Phase Two extensions to the Keycloak Admin UI](https://github.com/p2-inc/keycloak-ui/), as there are other options there that are reused in Login forms styling, and the UI extensions also ensure that the attributes are set with appropriate values. 

The keys specific to the Portal are:
| Key | Description | Default |
|---|---|---|
| `_providerConfig.assets.portal.primaryColor` | Primary color | `[empty]` |
| `_providerConfig.assets.portal.secondaryColor` | Accent color | `[empty]` |
| `_providerConfig.assets.portal.backgroundColor` | Background color | `[empty]` |
| `_providerConfig.assets.portal.` | CSS override | `[empty]` |


## Developers

### Getting Started

First, setup:

```bash
yarn
```

Then, start a Keycloak, create a public OIDC client with `http://localhost:3000` Root URL, and update the `public/keycloak.json` file with the client config.

Finally, run the development server:

```bash
yarn start
```

Open [http://localhost:3000](http://localhost:3000) with your browser to see the result.

### Build the extension

```bash
mvn clean package
```

Put the jar in `target/admin-portal-{version}.jar` in the `providers/` directory of your Keycloak distribution.

-----

All documentation, source code and other files in this repository are Copyright 2023 Phase Two, Inc.
