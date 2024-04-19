import i18n from "i18next";
import HttpBackend from "i18next-http-backend";
import { initReactI18next } from "react-i18next";
import { joinPath } from "services/join-path";

const ENGLISH_LOCALE = "en";
const FRENCH_LOCALE = "fr";
const KOREAN_LOCALE = "ko";
const DEFAULT_NAMESPACE = "translation";

// use localStorage to avoid losing language on page reload
const locale = localStorage.getItem("locale");
if (!locale) {
  localStorage.setItem("locale", ENGLISH_LOCALE);
}

i18n
  .use(HttpBackend)
  .use(initReactI18next)
  .init({
    lng: locale || ENGLISH_LOCALE,
    returnNull: false,
    defaultNS: DEFAULT_NAMESPACE,
    fallbackLng: [ENGLISH_LOCALE, FRENCH_LOCALE, KOREAN_LOCALE],
    ns: [DEFAULT_NAMESPACE],
    interpolation: {
      escapeValue: false,
    },
    backend: {
      loadPath: joinPath("./", "locales/{{lng}}/{{ns}}.json"),
    },
  });

export const setLanguage = (lang: string) => {
  i18n.changeLanguage(lang).then(() => {
    i18n.options.lng = lang;
  });
  localStorage.setItem("locale", lang);
};

export default i18n;
