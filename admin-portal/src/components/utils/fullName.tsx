import { UserRepresentation } from "store/apis/orgs";

export default function fullName(member: UserRepresentation) {
  const name = `${member.firstName} ${member.lastName}`.trim();
  return name === "" ? null : name;
}
