import keycloak from "src/keycloak";
import axios from "axios";

const instance = axios.create();

instance.interceptors.request.use((config: any) => {
  const token = keycloak.token;

  if (token) {
    config.headers["Authorization"] = `Bearer ${token}`;
  }

  return config;
});

export const Axios = instance;
