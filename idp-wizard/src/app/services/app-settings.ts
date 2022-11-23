import { createSlice } from "@reduxjs/toolkit";
import type { PayloadAction } from "@reduxjs/toolkit";

interface Settings {
  currentOrg: string | null;
  mustPickOrg: boolean;
  // realm
  // org, orgId
  // onprem or not
}

const initialState = {
  currentOrg: null,
  mustPickOrg: false,
} as Settings;

const settingsSlice = createSlice({
  name: "settings",
  initialState,
  reducers: {
    setOrganization(state, action: PayloadAction<string>) {
      state.currentOrg = action.payload;
    },
    setMustPickOrg(state, action: PayloadAction<boolean>) {
      state.mustPickOrg = action.payload;
    },
  },
});

export const { setOrganization, setMustPickOrg } = settingsSlice.actions;
export { settingsSlice };
