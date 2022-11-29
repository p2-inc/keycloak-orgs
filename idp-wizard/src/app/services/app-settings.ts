import { createSlice } from "@reduxjs/toolkit";
import type { PayloadAction } from "@reduxjs/toolkit";

interface Settings {
  currentOrg: string | "global" | null; // Org Id
  mustPickOrg: boolean;
  apiMode: "onprem" | "cloud";
  // realm
}

const initialState = {
  currentOrg: null,
  mustPickOrg: false,
} as Settings;

const settingsSlice = createSlice({
  name: "settings",
  initialState,
  reducers: {
    setOrganization(state, action: PayloadAction<string | "global">) {
      state.currentOrg = action.payload;
      state.apiMode = action.payload === "global" ? "onprem" : "cloud";
    },
    setMustPickOrg(state, action: PayloadAction<boolean>) {
      state.mustPickOrg = action.payload;
    },
    setApiMode(state, action: PayloadAction<Settings["apiMode"]>) {
      state.apiMode = action.payload;
    },
  },
});

export const { setOrganization, setMustPickOrg, setApiMode } =
  settingsSlice.actions;
export { settingsSlice };
