import { useMemo } from "react";
import { useSelector } from "react-redux";
import { selectFeatureFlags } from "./slice";

export const useFeatureFlags = () => {
  const featureFlags = useSelector(selectFeatureFlags);

  return useMemo(() => ({ featureFlags }), [featureFlags]);
};
