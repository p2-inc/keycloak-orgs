import type { ConfigFile } from "@rtk-query/codegen-openapi";

const config: ConfigFile = {
  schemaFile:
    "https://gist.githubusercontent.com/xgp/2d77cbebc6164160faae6aa77d127a57/raw/d217351b0d895ba46d1059aeced85c8d381b1c0e/openapi.yaml",
  apiFile: "./src/store/apis/empty.ts",
  apiImport: "emptySplitApi",
  outputFile: "./src/store/apis/profile.ts",
  exportName: "profileApi",
  hooks: true,
  tag: true,
};

export default config;
