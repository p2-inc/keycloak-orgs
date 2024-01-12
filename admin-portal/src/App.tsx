import Layout from "components/layouts/layout";
import { setLanguage } from "i18n";
import { Outlet } from "react-router-dom";
import { useGetAccountQuery } from "store/apis/profile";
import { config } from "config";
import { useEffect } from "react";

const { supportedLocales, realm } = config.env;

function App() {
  const hasLocale = (locale: string): boolean => locale in supportedLocales;

  const { data: account } = useGetAccountQuery({
    userProfileMetadata: true,
    realm,
  });
  useEffect(() => {
    // load language (after login)
    if (
      account?.attributes?.locale &&
      hasLocale(account?.attributes?.locale[0])
    ) {
      setLanguage(account?.attributes?.locale[0]);
    }
  }, [account]);

  return (
    <Layout>
      <Outlet />
    </Layout>
  );
}

export default App;
