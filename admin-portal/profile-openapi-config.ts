import type { ConfigFile } from "@rtk-query/codegen-openapi";

const config: ConfigFile = {
  schemaFile:
    "https://gist.githubusercontent.com/xgp/2d77cbebc6164160faae6aa77d127a57/raw/c51a2d44ef1ce2d176a0f0c53cde0183738045ce/openapi.yaml",
  apiFile: "./src/store/apis/empty.ts",
  apiImport: "emptySplitApi",
  outputFile: "./src/store/apis/profile.ts",
  exportName: "profileApi",
  hooks: true,
  tag: true,
};

export default config;
