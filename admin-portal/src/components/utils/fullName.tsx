import { UserRepresentation } from "@/store/apis/orgs";

export default function fullName(member: UserRepresentation) {
  let name = `${member.firstName} ${member.lastName}`.trim();
  if (name) {
    return name;
  }
  return member.email;
}
