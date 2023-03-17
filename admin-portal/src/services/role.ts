export const roleSettings = [
  {
    regexp: new RegExp("^view-"),
    name: "view roles",
    className: "bg-[#7CE0C3]",
  },
  {
    regexp: new RegExp("^manage-"),
    name: "manage roles",
    className: "bg-[#C07CE0]",
  },
  {
    regexp: new RegExp("^(?!view-|manage-).*$"),
    name: "other roles",
    className: "bg-gray-600",
  },
];

export function getRoleSettings(name: string) {
  const settings = roleSettings.find((f) => f.regexp.test(name));
  return settings;
}
