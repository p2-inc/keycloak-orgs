import { keycloak } from "keycloak";

export default function OpenSSOLink({ orgId }: { orgId: string }) {
  var base = keycloak.authServerUrl;
  if (!base?.endsWith("/")) base += "/";
  const link = `${base}realms/${keycloak.realm}/wizard/?org_id=${encodeURIComponent(orgId)}`;
  window.open(link);

  // const user = await keycloak.loadUserProfile();
  // TODO: switch when method is ready
  // try {
  // const portalLink = await createPortalLink({
  //   orgId: org?.id!,
  //   realm: apiRealm,
  //   body: {
  //     userId: user.id,
  //   },
  // });
  // window.open(portalLink);
  // } catch (e) {
  //   console.error(e);
  // }
}
