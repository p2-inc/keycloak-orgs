import cs from "classnames";
import { FC } from "react";
import { Link } from "react-router-dom";
import { ViewLayoutOptions } from "../forms/switches/view-switch";
import { config } from "config";
const { features: featureFlags } = config.env;

type Props = {
  children: React.ReactNode;
  link: string;
  title?: string;
  subTitle?: string;
  viewType: ViewLayoutOptions;
};

const Title = ({ children }) => (
  <div className="font-semibold dark:text-zinc-200">{children}</div>
);
const SubTitle = ({ children }) => (
  <div className="text-[14px] dark:text-zinc-400">{children}</div>
);

const InnerItem = ({ children, title, subTitle, viewType }) => {
  return (
    <div className="relative">
      <div className="relative z-20">
        <div
          className={cs(
            "col-span-1 flex",
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
  link,
  title,
  subTitle,
  viewType,
}) => {
  return (
    <>
      {featureFlags.orgDetailsEnabled && (
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
      )}
      {!featureFlags.orgDetailsEnabled && (
        <div
          className={cs(
            " block",
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
      )}
    </>
  );
};

export default OrganizationItem;
