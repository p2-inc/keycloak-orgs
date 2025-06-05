import RoleBadge from "@/components/elements/badges/role-badge";
import SquareBadge from "@/components/elements/badges/square-badge";
import { Switch } from "@headlessui/react";

export const SwitchItem = ({
  name,
  isChecked,
  onChange,
  isDisabled,
  roleType,
}: {
  name: string;
  isChecked: boolean;
  onChange: (roleName, checked) => void;
  isDisabled?: boolean;
  roleType: "organization" | "application";
}) => {
  return (
    <Switch.Group>
      <div className="flex items-center justify-between py-2">
        <Switch.Label className="mr-4 flex-1">
          <div className="flex items-center justify-between">
            <RoleBadge name={name} />
            <SquareBadge className="ml-2">{roleType.toLowerCase()}</SquareBadge>
          </div>
        </Switch.Label>
        <Switch
          checked={isChecked}
          disabled={isDisabled}
          onChange={(checked) => onChange(name, checked)}
          className={`${
            isChecked ? "bg-primary-500" : "bg-gray-200 dark:bg-secondary-900"
          } relative inline-flex h-6 w-11 items-center rounded-full transition-colors focus:outline-none focus:ring-2 focus:ring-primary-500 focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50`}
        >
          <span
            className={`${
              isChecked ? "translate-x-6" : "translate-x-1"
            } inline-block h-4 w-4 transform rounded-full bg-white transition-transform`}
          />
        </Switch>
      </div>
    </Switch.Group>
  );
};
