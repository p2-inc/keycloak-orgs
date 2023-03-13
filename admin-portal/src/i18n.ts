import i18n from "i18next";
import HttpBackend from "i18next-http-backend";
import { initReactI18next } from "react-i18next";
import { joinPath } from "services/join-path";

const DEFAULT_LOCALE = "en";
const DEFAULT_NAMESPACE = "translation";

i18n
  .use(HttpBackend)
  .use(initReactI18next)
  .init({
    defaultNS: DEFAULT_NAMESPACE,
    fallbackLng: DEFAULT_LOCALE,
    ns: [DEFAULT_NAMESPACE],
    interpolation: {
      escapeValue: false,
    },
    backend: {
      loadPath: joinPath('./', "locales/{{lng}}/{{ns}}.json"),
    },
  });

export default i18n;