import cs from "classnames";
import { FC, useEffect } from "react";
import { Link } from "react-router-dom";
import { ViewLayoutOptions } from "../forms/switches/view-switch";
import { config } from "config";
import {
  OrganizationRepresentation,
  useGetByRealmUsersAndUserIdOrgsOrgIdRolesQuery,
} from "store/apis/orgs";
import useUser from "components/utils/useUser";
import { Roles } from "services/role";
import OrganizationsLoader from "components/loaders/organizations";
import { checkOrgForRole } from "components/utils/check-org-for-role";
const { features: featureFlags, realm } = config.env;

type Props = {
  children: React.ReactNode;
  viewType: ViewLayoutOptions;
  org: OrganizationRepresentation;
  setVisibility: () => void;
};

const Title = ({ children }) => (
  <div className="font-semibold dark:text-zinc-200">{children}</div>
);
const SubTitle = ({ children }) => (
  <div className="text-[14px] dark:text-zinc-400">{children}</div>
);

const InnerItem = ({ children, title, subTitle, viewType }) => {
  return (
    <div className="relative h-full">
      <div className="relative z-20 h-full">
        <div
          className={cs(
            "col-span-1 flex h-full",
            "group-hover:border-gray-300 group-hover:bg-white dark:group-hover:border-zinc-500 dark:group-hover:bg-p2dark-900",
            {
              "flex-col space-y-5 rounded-md border border-gray-200 bg-gray-50 px-10 py-9 dark:border-zinc-600 dark:bg-p2dark-1000":
                viewType === ViewLayoutOptions.GRID,
              "flex-row justify-between px-5 py-4":
                viewType === ViewLayoutOptions.LIST,
            }
          )}
        >
          <div>
            <Title>{title}</Title>
            <SubTitle>{subTitle}</SubTitle>
          </div>
          <div className="flex flex-row space-x-8">{children}</div>
        </div>
      </div>
      {viewType === ViewLayoutOptions.GRID && (
        <div
          className={cs(
            "absolute inset-x-3 bottom-0 z-10 h-1/2 rounded-full bg-white opacity-0",
            "transition-opacity duration-200",
            "group-hover:opacity-100",
            "drop-shadow-btn-light group-active:hidden"
          )}
        ></div>
      )}
    </div>
  );
};

const OrganizationItem: FC<Props> = ({
  children,
  org,
  viewType,
  setVisibility,
}) => {
  const { user } = useUser();
  const { displayName: title, name: subTitle } = org;
  const link = `/organizations/${org.id}/details`;

  const { data: userRolesForOrg = [], isFetching: isFetchingRole } =
    useGetByRealmUsersAndUserIdOrgsOrgIdRolesQuery(
      {
        orgId: org.id!,
        realm,
        userId: user?.id!,
      },
      { skip: !user?.id }
    );

  const hasViewRole = checkOrgForRole(userRolesForOrg, Roles.ViewOrganization);

  useEffect(() => {
    if (hasViewRole) setVisibility();
  }, [hasViewRole]);

  if (isFetchingRole) {
    return <OrganizationsLoader />;
  }

  if (!hasViewRole) {
    return <></>;
  }
  return featureFlags.orgDetailsEnabled ? (
    <Link
      to={link}
      className={cs("group block", "focus:outline-none focus:ring-0", {
        "md:pb-3": viewType === ViewLayoutOptions.GRID,
      })}
    >
      <InnerItem title={title} subTitle={subTitle} viewType={viewType}>
        {children}
      </InnerItem>
    </Link>
  ) : (
    <div
      className={cs(
        "block",
        "focus:outline-none focus:ring-1 focus:ring-neutral-50 focus:ring-offset-1",
        {
          "md:pb-3": viewType === ViewLayoutOptions.GRID,
        }
      )}
    >
      <InnerItem title={title} subTitle={subTitle} viewType={viewType}>
        {children}
      </InnerItem>
    </div>
  );
};

export default OrganizationItem;
