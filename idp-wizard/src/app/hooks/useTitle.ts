import { useTitle } from "react-use";
import { useGetFeatureFlagsQuery } from "@app/services";
import keycloak from "../../keycloak";
import { startCase } from "lodash";

export const usePageTitle = (title?: string) => {
  const realmName = keycloak.realm;
  const { data: featureFlags } = useGetFeatureFlagsQuery();
  const appName = featureFlags?.appName;

  const setPageTitle = (pageTitle: string) => {
    const parts = [pageTitle];

    if (appName) {
      parts.push(appName);
    } else if (realmName) {
      parts.push(startCase(realmName));
    }

    useTitle(parts.join(" | "));
  };

  // Set initial title if provided
  if (title) {
    setPageTitle(title);
  }

  return { setPageTitle };
};
