import { KeycloakProfile } from "keycloak-js";
import { useState, useEffect } from "react";
import { keycloak } from "keycloak";

export default function useUser() {
  const [user, setUser] = useState<KeycloakProfile>();
  //TODO: move roles check functions into this hook to remove duplication

  async function loadUser() {
    const u = await keycloak.loadUserProfile();
    setUser(u);
  }

  useEffect(() => {
    loadUser();
  }, []);

  function fullName() {
    if (!user) return "member";
    return user.firstName || user.lastName
      ? `${user.firstName} ${user.lastName}`.trim()
      : user.username || user.email || "member";
  }

  return { user, fullName };
}
