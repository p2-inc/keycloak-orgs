// Need to use the React-specific entry point to import createApi
import { createApi, fetchBaseQuery } from "@reduxjs/toolkit/query/react";
import { Orgs } from "api/orgs";
import config from "config";

// Define a service using a base URL and expected endpoints
export const orgsApi = createApi({
  reducerPath: "orgsApi",
  baseQuery: fetchBaseQuery({ baseUrl: `${config.baseUrl}/${config.realm}` }),
  endpoints: (builder) => ({
    getOrgs: builder.query({
      async queryFn() {
        const res = await Orgs.getOrgs();
        console.log("🚀 ~ file: service.ts:21 ~ queryFn ~ res", res);

        return { data: res };
      },
    }),
  }),
});

// Export hooks for usage in functional components, which are
// auto-generated based on the defined endpoints
export const { useGetOrgsQuery } = orgsApi;
