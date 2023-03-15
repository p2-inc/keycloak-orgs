import type { ConfigFile } from "@rtk-query/codegen-openapi";

const config: ConfigFile = {
  schemaFile:
    "https://gist.githubusercontent.com/xgp/2d77cbebc6164160faae6aa77d127a57/raw/c2f467591016ec4449aed49455d424fe3db07951/openapi.yaml",
  apiFile: "./src/store/apis/empty.ts",
  apiImport: "emptySplitApi",
  outputFile: "./src/store/apis/profile.ts",
  exportName: "profileApi",
  hooks: true,
  tag: true,
};

export default config;
