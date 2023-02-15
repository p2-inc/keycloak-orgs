import { configureStore } from "@reduxjs/toolkit";
import { setupListeners } from "@reduxjs/toolkit/query";
import { featureFlagsApi } from "./feature-flags/service";
import { orgsApi } from "./orgs/service";
// import featureFlagReducer from "./feature-flags/slice";

export const store = configureStore({
  reducer: {
    [featureFlagsApi.reducerPath]: featureFlagsApi.reducer,
    [orgsApi.reducerPath]: orgsApi.reducer,
  },
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware().concat(
      featureFlagsApi.middleware,
      orgsApi.middleware
    ),
});

setupListeners(store.dispatch);

// Infer the `RootState` and `AppDispatch` types from the store itself
export type RootState = ReturnType<typeof store.getState>;
// Inferred type: {posts: PostsState, comments: CommentsState, users: UsersState}
export type AppDispatch = typeof store.dispatch;
