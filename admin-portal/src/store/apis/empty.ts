import { createApi, fetchBaseQuery } from "@reduxjs/toolkit/query/react";
import { config } from "@/config";
import { getAccessToken } from "./helpers";

export const emptySplitApi = createApi({
  baseQuery: fetchBaseQuery({
    baseUrl: `${config.env.authServerUrl}realms`,
    prepareHeaders(headers, api) {
      headers.set("accept", "application/json");
      const token = getAccessToken();
      if (token) {
        headers.set("authorization", `Bearer ${token}`);
      }
      return headers;
    },
  }),
  endpoints: () => ({}),
});
