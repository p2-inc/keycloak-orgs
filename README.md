> :rocket: **Try it for free** in the new Phase Two [keycloak managed service](https://phasetwo.io/?utm_source=github&utm_medium=readme&utm_campaign=keycloak-orgs). See the [announcement and demo video](https://phasetwo.io/blog/self-service/) for more information.

# Organizations for Keycloak

*Single realm, multi-tenancy for SaaS apps*

This project intends to provide a range of Keycloak extensions focused on solving several of the common use cases of multi-tenant SaaS applications that Keycloak does not solve out of the box.

The extensions herein are used in the [Phase Two](https://phasetwo.io) cloud offering, and are released here as part of its commitment to making its [core extensions](https://phasetwo.io/docs/introduction/open-source) open source. Please consult the [license](COPYING) for information regarding use.

## Contents

- [Organizations for Keycloak](#organizations-for-keycloak)
  - [Contents](#contents)
  - [Overview](#overview)
    - [Definitions](#definitions)
  - [Quick start](#quick-start)
  - [Building](#building)
  - [Installation](#installation)
    - [Admin UI](#admin-ui)
    - [Compatibility](#compatibility)
  - [Extensions](#extensions)
    - [Data](#data)
      - [Models](#models)
      - [Entities](#entities)
    - [Resources](#resources)
    - [Mappers](#mappers)
    - [Authentication](#authentication)
      - [Invitations](#invitations)
      - [IdP Discovery](#idp-discovery)
  - [License](#license)

## Overview

If you search for "multi-tenant Keycloak", you'll find several opinionated approaches, each promising, and each with their own tradeoffs. This project represents one such approach. It was built initially for a multi-tenant, public cloud, SaaS application. It has now been, in the form of the [Phase Two](https://phasetwo.io) cloud offering, adopted by several other companies for the same purpose.

Other approaches that we tried and decided against were:

- One Realm for each tenant
- Using existing Keycloak Groups to model Organizations, Roles and Memberships

But each of these approaches had tradeoffs of scale or frailty we found undesirable or unacceptable to meet our requirements. Instead, we opted to make Organizations, and their Invitations, Roles and Memberships first-class entities in Keycloak.

### Definitions

- **Organizations** are "tenants" or "customers" as commonly used. A Realm can have multiple Organizations.
- **Memberships** are the relationship of Users to Organizations. Users may be members of multiple Organizations.
- **Roles** are mechanisms of role-based security specific to an Organization, much like Keycloak Realm Roles and Client Roles. In addition to a set of standard roles related to Organization data visibilty and management, administrators can create Roles unique to an organization. Users who are Members of Organizations can be granted that Organization's Roles.
- **Invitations** allow Users and non-Users to be invited to join an Organization. Invitations can be created by administrators or Organization members with permission.
- **Domains** are email domains that are used to automatically select Organization IdPs using the optional authenticators. Included is a facility to validate customer domain ownership using DNS records.

## Quick start

The easiest way to get started is our [Docker image](https://quay.io/repository/phasetwo/phasetwo-keycloak?tab=tags). Documentation and examples for using it are in the [phasetwo-containers](https://github.com/p2-inc/phasetwo-containers) repo. The most recent version of this extension is included.

## Building

Checkout this project and run `mvn package`, which will produce a jar in the `target/` directory.

The build uses `keycloak-testsuite-utils` for the unit tests. If you want to run the tests, you'll need to install Keycloak from source locally, as the test utility never gets published to maven central by the Keycloak team. To build Keycloak from source you must check out the tag of the Keycloak version you are using and then build (do this in a separate directory):

```bash
KC_VERSION=21.1.1
git clone https://github.com/keycloak/keycloak
git fetch origin --tags
git checkout $KC_VERSION
mvn clean install -DskipTests
```

And then run the build with the tests using the `test` profile:

```bash
mvn clean install -Ptest
```

## Installation

The maven build uses the shade plugin to package a fat-jar with all dependencies, except for the [`keycloak-admin-client`](https://mvnrepository.com/artifact/org.keycloak/keycloak-admin-client). Put the `keycloak-orgs` jar and `keycloak-admin-client` jar (that corresponds to your Keycloak version) in your `provider` (for Quarkus-based distribution) or in `standalone/deployments` (for Wildfly, legacy distribution) directory and restart Keycloak. It is unknown if these extensions will work with hot reloading using the legacy distribution.

During the first run, some initial migrations steps will occur:

- Database migrations will be run to add the tables for use by the JPA entities. These have been tested with SQL Server,
  MySQL, MariaDB, H2, and Postgres. Other database types may fail.
- Initial `realm-management` client roles (`view-organizations` and `manage-organizations`) will be be added to each realm.

### Admin UI

If you are using the extension as bundled in the [Docker image](https://quay.io/repository/phasetwo/phasetwo-keycloak?tab=tags) or by building our [Admin UI theme](https://github.com/p2-inc/keycloak-ui), you must take an additional step in order to show that theme. In the Admin Console UI, go to the *Realm Settings* -> *Themes* page and select `phasetwo.v2`. Then, the "Organizations" section will be available in the left navigation. Because of a quirk in Keycloak, if you are logging in to the `master` realm, the theme must be set in *that* realm, rather than the realm you wish to administer.  

### Compatibility

Although it has been developed and working since Keycloak 9.0.0, the extensions are currently known to work with Keycloak > 17.0.0. Other versions may work also. Please file an issue if you have successfully installed it with prior versions. Additionally, because of the fast pace of breaking changes since Keycloak "X" (Quarkus version), we don't make any guaranteed that this will work with any version other than it is packaged with in the [Docker image](https://quay.io/repository/phasetwo/phasetwo-keycloak?tab=tags).

## Extensions

### Data

We've adopted a similar model that Keycloak uses for making the Organization data available to the application. There is a custom SPI that makes the [OrganizationProvider](src/main/java/io/phasetwo/service/model/OrganizationProvider.java) available. The methods provided are:

```java
  OrganizationModel createOrganization(
      RealmModel realm, String name, UserModel createdBy, boolean admin);

  OrganizationModel getOrganizationById(RealmModel realm, String id);

  Stream<OrganizationModel> searchForOrganizationStream(
      RealmModel realm,
      Map<String, String> attributes,
      Integer firstResult,
      Integer maxResults,
      Optional<UserModel> member);

  Long getOrganizationsCount(RealmModel realm, String search);

  boolean removeOrganization(RealmModel realm, String id);

  void removeOrganizations(RealmModel realm);

  Stream<OrganizationModel> getOrganizationsStreamForDomain(
      RealmModel realm, String domain, boolean verified);

  Stream<OrganizationModel> getUserOrganizationsStream(RealmModel realm, UserModel user);

  Stream<InvitationModel> getUserInvitationsStream(RealmModel realm, UserModel user);
```

#### Models

The OrganizationProvider returns model delegates that wrap the underlying entities and provide conveniences for working with the data. They are available in the `io.phasetwo.service.model` package.

- [OrganizationModel](src/main/java/io/phasetwo/service/model/OrganizationModel.java)
- [OrganizationRoleModel](src/main/java/io/phasetwo/service/model/OrganizationRoleModel.java)
- [InvitationModel](src/main/java/io/phasetwo/service/model/InvitationModel.java)
- [DomainModel](src/main/java/io/phasetwo/service/model/DomainModel.java)

#### Entities

There are JPA entities that represent the underlying tables that are available in the `io.phasetwo.service.model.jpa.entity` package. The providers and models are implemented using these entities in the `io.phasetwo.service.model.jpa` package.

- [OrganizationEntity](src/main/java/io/phasetwo/service/model/jpa/entity/OrganizationEntity.java)
- [OrganizationAttributeEntity](src/main/java/io/phasetwo/service/model/jpa/entity/OrganizationAttributeEntity.java)
- [OrganizationMemberEntity](src/main/java/io/phasetwo/service/model/jpa/entity/OrganizationMemberEntity.java)
- [OrganizationRoleEntity](src/main/java/io/phasetwo/service/model/jpa/entity/OrganizationRoleEntity.java)
- [UserOrganizationRoleMappingEntity](src/main/java/io/phasetwo/service/model/jpa/entity/UserOrganizationRoleMapping.java)
- [InvitationEntity](src/main/java/io/phasetwo/service/model/jpa/entity/InvitationEntity.java)
- [DomainEntity](src/main/java/io/phasetwo/service/model/jpa/entity/DomainEntity.java)

### Resources

A group of custom REST resources are made available for administrator and customer use and UI. Current documentation on the available resource methods is in this [openapi.yaml](https://github.com/p2-inc/phasetwo-docs/blob/master/openapi.yaml) specification file, and you can find browsable documentation on the [Phase Two API](https://phasetwo.io/api/) site.

- Organizations - CRUD Organizations
- Memberships - CRUD and check User-Organization membership
- Roles - CRUD Organization Roles and grant/revoke Roles to Users
- Identity Providers - A subset of the Keycloak IdP APIs that allows Organization administrators to manage their own IdP

### Mappers

There is currently a single OIDC mapper that adds Organization membership and roles to the token. The format of the addition to the token is

```json
  "organizations": {
    "5aeb9aeb-97a3-4deb-af9f-516615b59a2d" : {
      "name": "foo",
      "roles": [ "admin", "viewer" ]
    }
  }
```

You can configure the mapper, by going to **Clients** > ***your-client-name*** > **Client scopes** > ***your-client-name*-dedicated** and choosing to add a new mapper **By configuration**. Once selected, choose the **Organization Role** mapper from the list and specify the details like the following:

![mapper](./docs/assets/mapper.png)

### Authentication

#### Invitations

For most use cases, set the `Invitation` required action to `Enabled` in *Authentication*->*Required Actions*. It does not need to be set as a default. It will automatically check on each login if the user has outstanding Invitations to Organizations, and enable itself.

![Install and enable Invitation Required Action](https://github.com/p2-inc/keycloak-orgs/assets/244253/c454cfaa-e50f-4a3c-94b4-87e9e85801d6)

There are some non-standard flows where the required action does not do this detection. For these cases, there is a custom Authenticator you can add to a copy of the standard browser flow. Add the `Invitation` authenticator as a "REQUIRED" execution following the "Username Password Form" as a child of the forms group. This authenticator checks to see if the authenticated user has outstanding Invitations to Organizations, and then adds the Required Action that they must complete to accept or reject their Invitations following a successful authentication.

#### IdP Discovery

Organizations may optionally be given permission to manage their own IdP. The custom resources that allow this write a configuration in the IdP entities that is compatible with a 3rd party extension that allows for IdP discovery based on email domain configured for the Organization. It works by writing the `home.idp.discovery.domains` value into the `config` map for the IdP. Information on further configuration is available at [sventorben/keycloak-home-idp-discovery](https://github.com/sventorben/keycloak-home-idp-discovery).

tbd screenshot of installing in flow

## License

We’ve changed the license of our core extensions from the AGPL v3 to the [Elastic License v2](https://github.com/elastic/elasticsearch/blob/main/licenses/ELASTIC-LICENSE-2.0.txt). 

- Our blog post on the subject https://phasetwo.io/blog/licensing-change/
- An attempt at a clarification https://github.com/p2-inc/keycloak-orgs/issues/81#issuecomment-1554683102

-----

Portions of the [Home IdP Discovery](https://github.com/p2-inc/keycloak-orgs/tree/main/src/main/java/io/phasetwo/service/auth/idp) code are Copyright (c) 2021-2023 Sven-Torben Janus, and are licensed under the [MIT License](https://github.com/p2-inc/keycloak-orgs/blob/main/src/main/java/io/phasetwo/service/auth/idp/LICENSE.md).

All other ocumentation, source code and other files in this repository are Copyright 2023 Phase Two, Inc.
