import cs from "classnames";
import { FC } from "react";
import { Link } from "react-router-dom";
import { ViewLayoutOptions } from "../forms/switches/view-switch";
import { config } from "config";
import { OrganizationRepresentation } from "store/apis/orgs";
import useUser from "components/utils/useUser";
import { useTranslation } from "react-i18next";
const { features: featureFlags } = config.env;

type Props = {
  children: React.ReactNode;
  viewType: ViewLayoutOptions;
  org: OrganizationRepresentation;
};

const Title = ({ children }) => (
  <div className="font-semibold text-secondary-900 dark:text-zinc-200">
    {children}
  </div>
);
const SubTitle = ({ children }) => (
  <div className="text-[14px] dark:text-zinc-400">{children}</div>
);

const InnerItem = ({
  children,
  title,
  subTitle,
  viewType,
  isViewCard,
}: {
  children: React.ReactNode;
  title?: string;
  subTitle?: string;
  viewType: ViewLayoutOptions;
  isViewCard?: boolean;
}) => {
  return (
    <div className="relative h-full">
      <div className="relative z-20 h-full">
        <div
          className={cs(
            "col-span-1 flex h-full",
            "group-hover:border-primary-400 group-hover:bg-white dark:group-hover:border-zinc-500 dark:group-hover:bg-p2dark-900",
            {
              "flex-col space-y-5 rounded-md border  bg-gray-50 px-10 py-9 dark:border-zinc-600 dark:bg-p2dark-1000":
                viewType === ViewLayoutOptions.GRID,
              "flex-row justify-between px-5 py-4":
                viewType === ViewLayoutOptions.LIST,
              "border-gray-100 dark:border-zinc-800":
                viewType === ViewLayoutOptions.GRID && isViewCard,
              " border-primary-600":
                viewType === ViewLayoutOptions.GRID && !isViewCard,
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

const OrganizationItem: FC<Props> = ({ children, org, viewType }) => {
  const { t } = useTranslation();
  let { displayName: title, name: subTitle } = org;
  if (!title) {
    title = subTitle;
  }
  const link = `/organizations/${org.id}/details`;
  const { hasViewOrganizationRole: hasViewOrganizationRoleCheck } = useUser();
  const hasViewOrganizationRole = hasViewOrganizationRoleCheck(org.id);

  const ViewCard = () => (
    <div
      className={cs(
        "block",
        "focus:outline-none focus:ring-1 focus:ring-neutral-50 focus:ring-offset-1",
        "hover:cursor-not-allowed",
        {
          "md:pb-3": viewType === ViewLayoutOptions.GRID,
        }
      )}
      title={t("insufficientPermissionsToViewOrganization")}
    >
      <InnerItem
        title={title}
        subTitle={subTitle}
        viewType={viewType}
        isViewCard
      >
        {children}
      </InnerItem>
    </div>
  );

  const LinkCard = () => (
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
  );

  if (hasViewOrganizationRole && featureFlags.orgDetailsEnabled) {
    return <LinkCard />;
  } else {
    return <ViewCard />;
  }
};

export default OrganizationItem;
