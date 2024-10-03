import type { ConfigFile } from "@rtk-query/codegen-openapi";

const config: ConfigFile = {
  schemaFile:
    "https://raw.githubusercontent.com/p2-inc/phasetwo-docs/main/openapi.yaml",
  apiFile: "./src/store/apis/empty.ts",
  apiImport: "emptySplitApi",
  outputFile: "./src/store/apis/orgs.ts",
  exportName: "orgsApi",
  hooks: true,
  tag: true,
};

export default config;
