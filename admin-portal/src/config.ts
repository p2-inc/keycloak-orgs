//get the base url and realm from the window
const windowBaseUrl: string =
  window.location.protocol +
  "//" +
  window.location.hostname +
  (window.location.port ? ":" + window.location.port : "") +
  "/auth/realms";
const windowRealm: string = (function (): string {
  let segments = window.location.pathname.split("/");
  for (let i = 0; i < segments.length; i++) {
    if (segments[i] === "realms" && segments.length > i + 1) {
      return segments[i + 1];
    }
  }
  return "";
})();

/*
export default {
    baseUrl: process.env.BASE_URL ?? windowBaseUrl,
    realm: process.env.REALM ?? windowRealm
};
*/
// for testing
const config = {
  baseUrl: "https://app.phasetwo.io/auth/realms" ?? windowBaseUrl,
  realm: "test" ?? windowRealm,
};

export default config;
