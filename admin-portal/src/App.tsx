import Layout from "components/layouts/layout";
import { Outlet } from "react-router-dom";
// import { useGetFeatureFlagsQuery } from "store/feature-flags/service";

function App() {
  // const { data: featureFlags, error, isLoading } = useGetFeatureFlagsQuery();

  return (
    <Layout>
      <Outlet />
    </Layout>
  );
}

export default App;
