import axios, { type AxiosRequestConfig } from "axios";

const ACCESS_TOKEN_KEY = "niveshtrack_access_token";
const REFRESH_TOKEN_KEY = "niveshtrack_refresh_token";

export const getAccessToken = () => localStorage.getItem(ACCESS_TOKEN_KEY);
export const getRefreshToken = () => localStorage.getItem(REFRESH_TOKEN_KEY);

export const saveTokens = (accessToken: string, refreshToken: string) => {
  localStorage.setItem(ACCESS_TOKEN_KEY, accessToken);
  localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken);
};

export const clearTokens = () => {
  localStorage.removeItem(ACCESS_TOKEN_KEY);
  localStorage.removeItem(REFRESH_TOKEN_KEY);
  localStorage.removeItem("niveshtrack_user");
};

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "http://localhost:8081",
  headers: { "Content-Type": "application/json" },
});

// Attach Bearer token on every request
api.interceptors.request.use((config) => {
  const token = getAccessToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

let isRefreshing = false;
let pendingQueue: Array<{
  resolve: (value: unknown) => void;
  reject: (reason?: unknown) => void;
  config: AxiosRequestConfig;
}> = [];

const processQueue = (error: unknown, token: string | null) => {
  pendingQueue.forEach(({ resolve, reject, config }) => {
    if (error) {
      reject(error);
    } else {
      if (config.headers) {
        config.headers.Authorization = `Bearer ${token}`;
      }
      resolve(api(config));
    }
  });
  pendingQueue = [];
};

// On 401: call /api/auth/refresh, retry once, else redirect to /login
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config as AxiosRequestConfig & { _retry?: boolean };

    if (error.response?.status === 401 && !originalRequest._retry) {
      const refreshToken = getRefreshToken();

      if (!refreshToken) {
        clearTokens();
        window.location.href = "/login";
        return Promise.reject(error);
      }

      if (isRefreshing) {
        // Queue subsequent 401s while refresh is in flight
        return new Promise((resolve, reject) => {
          pendingQueue.push({ resolve, reject, config: originalRequest });
        });
      }

      originalRequest._retry = true;
      isRefreshing = true;

      try {
        const baseURL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8081";
        const { data } = await axios.post(
          `${baseURL}/api/auth/refresh`,
          { refreshToken }
        );
        saveTokens(data.accessToken, data.refreshToken);
        // Update stored user fields that may have changed
        const storedUser = localStorage.getItem("niveshtrack_user");
        if (storedUser) {
          const parsed = JSON.parse(storedUser);
          localStorage.setItem(
            "niveshtrack_user",
            JSON.stringify({ ...parsed, ...data })
          );
        }
        processQueue(null, data.accessToken);
        if (originalRequest.headers) {
          originalRequest.headers.Authorization = `Bearer ${data.accessToken}`;
        }
        return api(originalRequest);
      } catch (refreshError) {
        processQueue(refreshError, null);
        clearTokens();
        window.location.href = "/login";
        return Promise.reject(refreshError);
      } finally {
        isRefreshing = false;
      }
    }

    return Promise.reject(error);
  }
);

export default api;
