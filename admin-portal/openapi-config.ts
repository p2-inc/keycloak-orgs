import type { ConfigFile } from "@rtk-query/codegen-openapi";

const config: ConfigFile = {
  schemaFile:
    "https://raw.githubusercontent.com/p2-inc/phasetwo-docs/master/openapi.yaml",
  apiFile: "./src/store/empty-api.ts",
  apiImport: "emptySplitApi",
  outputFile: "./src/store/p2-api/p2-api.ts",
  exportName: "p2Api",
  hooks: true,
  tag: true,
};

export default config;
