import { Roles } from "@/services/role";
import { OrganizationRoleRepresentation } from "@/store/apis/orgs";
import { isObject } from "lodash";

export function checkOrgForRole(
  orgRoles: OrganizationRoleRepresentation[],
  roleName: Roles
) {
  return isObject(orgRoles.find((uor) => uor.name === roleName));
}
