// Or from '@reduxjs/toolkit/query' if not using the auto-generated hooks
import { createApi, fetchBaseQuery } from "@reduxjs/toolkit/query/react";
import config from "config";
import { getAccessToken } from "./helpers";

// initialize an empty api service that we'll inject endpoints into later as needed
export const emptySplitApi = createApi({
  baseQuery: fetchBaseQuery({
    baseUrl: `${config.baseUrl}/`,
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
