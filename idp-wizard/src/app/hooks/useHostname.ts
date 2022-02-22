import { useState } from "react";

export function useHostname() {
  const [hostname, setHostname] = useState(window.location.hostname);

  return hostname;
}
