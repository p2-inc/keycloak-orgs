import { Outlet } from "react-router-dom";

const DomainContainer = () => {
  return (
    <div>
      <div className="px-4 md:mx-auto md:max-w-prose">
        <Outlet />
      </div>
    </div>
  );
};

export default DomainContainer;
