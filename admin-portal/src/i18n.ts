import i18n from "i18next";
import HttpBackend from "i18next-http-backend";
import { initReactI18next } from "react-i18next";
import { joinPath } from "services/join-path";

const ENGLISH_LOCALE = "en";
const FRENCH_LOCAL = "fr";
const DEFAULT_NAMESPACE = "translation";

i18n
  .use(HttpBackend)
  .use(initReactI18next)
  .init({
    returnNull: false,
    defaultNS: DEFAULT_NAMESPACE,
    fallbackLng: [ENGLISH_LOCALE, FRENCH_LOCAL],
    ns: [DEFAULT_NAMESPACE],
    interpolation: {
      escapeValue: false,
    },
    backend: {
      loadPath: joinPath("./", "locales/{{lng}}/{{ns}}.json"),
    },
  });

// use localStorage to avoid losing language on page reload
if (localStorage.getItem("lng") != undefined) {
  i18n.changeLanguage(localStorage.getItem("lng")!);
} else {
  localStorage.setItem("lng", ENGLISH_LOCALE);
}

export const setLanguage = (lang: string) => {
  i18n.changeLanguage(lang).then(() => {
    i18n.options.lng = lang;
  });
  localStorage.setItem("lng", lang);
};

export default i18n;
