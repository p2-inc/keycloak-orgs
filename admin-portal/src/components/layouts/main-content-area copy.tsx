import React from "react";
import cs from "classnames";

const FixedWidthMainContent: React.FC<{
  children: React.ReactElement | React.ReactElement[];
  className?: string;
}> = ({ children, className }) => (
  <div className={cs("flex max-w-7xl flex-grow", className)}>{children}</div>
);

export default FixedWidthMainContent;
