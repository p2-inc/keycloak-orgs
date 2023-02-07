import React from "react";

const MainContentArea: React.FC<{
  children: React.ReactElement | React.ReactElement[];
}> = ({ children }) => (
  <div className="flex flex-grow pt-8 pb-4">{children}</div>
);

export default MainContentArea;
