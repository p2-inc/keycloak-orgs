
# Note regarding Keycloak's upcoming organizations feature

The Keycloak team at RedHat has [announced](https://github.com/keycloak/keycloak/discussions/23948) that they will be building an Organizations feature into the core product. We have previously offered to change our license and donate our extension to the project, but this was not considered. We participated in some of the public discussions around this feature, as well as provided feedback on their product features privately.

Work has begun, and they plan to [release](https://github.com/keycloak/keycloak/issues/28609) some base features with `experimental` support in Keycloak `25`. They expect a full feature set and promotion to `preview` by Keycloak `26`. There are a few GitHub issues that indicate the features they plan to release, but there is not a comprehensive requirements or product feature plan available publicly.

It has been our goal since building this extension to have this approach to multi-tenancy built into Keycloak, and we have proven through many customers/user that is serves a real need. We're excited to see what they will build, and are flattered they have used our extension for inspiration.

We have several hundred customers and users of this extension, and there have been many asking about the future of this extension, given the above. We've put together these notes, and will continue to update them as we learn more.

- First of all, **this extension is not going away**. In addition to the fact we have long-term support agreements with many of our customers, we also don't currently have a picture of what Keycloak's implementation will ultimately look like.
- We continue to believe that our (Phase Two's) value is in making Keycloak easy to use, primarily for an audience that is using it as a CIAM tool for enterprise SaaS applications. We have tailored the feature set to that audience, and will continue to build out tools on top of our own organizations extension, such as the [admin portal](https://github.com/p2-inc/phasetwo-admin-portal) and [IdP wizards](https://github.com/p2-inc/idp-wizard) to facilitate making your app enterprise-ready quickly.
- We plan **not** to enable native Keycloak organizations in our hosted product, and it will be set (in env vars) off in our Docker images.
- We have initiated a project to move our organizations (and other) admin UI tools **outside** of the Keycloak Admin UI. In addition to proving very time consuming because of the pace of breaking changes, we realized that we can iterate faster for customer value by building our own admin UI. More information on that coming soon.
- We will continue to participate in discussions with RedHat and the Keycloak maintainers to advocate for our customers' needs and the features we have proven over >3 years.
- **IF** there is eventually sufficient feature parity between native Keycloak organizations and this extension, we will provide a migration path

This will probably cause a lot of confusion as native Keycloak organizations is released. We appreciate the patience and support that customers and users have shown us, and we hope to continue to support you with [great Keycloak extensions and tools](https://github.com/p2-inc).
