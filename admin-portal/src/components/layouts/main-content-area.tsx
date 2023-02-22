import React from "react";
import cs from "classnames";

const MainContentArea: React.FC<{
  children: React.ReactElement | React.ReactElement[];
  className?: string;
}> = ({ children, className }) => (
  <div className={cs("flex flex-grow pb-4", className)}>{children}</div>
);

export default MainContentArea;
