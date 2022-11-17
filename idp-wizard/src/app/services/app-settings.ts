import { createSlice } from "@reduxjs/toolkit";
import type { PayloadAction } from "@reduxjs/toolkit";

interface Settings {
  selectedOrg: string | null;
}

const initialState = { selectedOrg: null } as Settings;

const settingsSlice = createSlice({
  name: "settings",
  initialState,
  reducers: {
    setOrganization(state, action: PayloadAction<string>) {
      state.selectedOrg = action.payload;
    },
  },
});

export const { setOrganization } = settingsSlice.actions;
export { settingsSlice };
