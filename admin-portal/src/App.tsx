import Layout from "components/layouts/layout";
import { Outlet } from "react-router-dom";
import { useGetFeatureFlagsQuery } from "store/feature-flags/service";

function App() {
  // Don't need the data, just need to fetch it.
  useGetFeatureFlagsQuery();

  return (
    <Layout>
      <Outlet />
    </Layout>
  );
}

export default App;
