import axios from "axios";
import { api, saveTokens, clearTokens } from "@/lib/api";
import type { AuthResponse, UserProfile } from "@/types";

const USER_KEY = "niveshtrack_user";
const API_BASE = import.meta.env.VITE_API_BASE_URL || "http://localhost:8081";

const toUserProfile = (r: AuthResponse): UserProfile => ({
  userId: r.userId,
  name: r.name,
  email: r.email,
  currency: r.currency,
  darkMode: r.darkMode,
});

/** Maps backend UserProfileDTO (has `id`) to frontend UserProfile (has `userId`) */
const fromProfileDTO = (d: { id?: number; userId?: number; name: string; email: string; currency: string; darkMode: boolean }): UserProfile => ({
  userId: d.userId ?? d.id ?? 0,
  name: d.name,
  email: d.email,
  currency: d.currency,
  darkMode: d.darkMode,
});

export const authService = {
  login: async (email: string, password: string): Promise<{ user: UserProfile }> => {
    const { data } = await axios.post<AuthResponse>(`${API_BASE}/api/auth/login`, {
      email,
      password,
    });
    saveTokens(data.accessToken, data.refreshToken);
    const user = toUserProfile(data);
    localStorage.setItem(USER_KEY, JSON.stringify(user));
    return { user };
  },

  register: async (name: string, email: string, password: string): Promise<{ user: UserProfile }> => {
    const { data } = await axios.post<AuthResponse>(`${API_BASE}/api/auth/register`, {
      name,
      email,
      password,
    });
    saveTokens(data.accessToken, data.refreshToken);
    const user = toUserProfile(data);
    localStorage.setItem(USER_KEY, JSON.stringify(user));
    return { user };
  },

  logout: () => {
    clearTokens();
  },

  isAuthenticated: (): boolean => {
    return !!localStorage.getItem("niveshtrack_access_token");
  },

  getUser: (): UserProfile | null => {
    const raw = localStorage.getItem(USER_KEY);
    return raw ? JSON.parse(raw) : null;
  },

  updateProfile: async (profile: Partial<UserProfile>): Promise<UserProfile> => {
    const { data } = await api.put("/api/user/profile", profile);
    const updated = fromProfileDTO(data);
    localStorage.setItem(USER_KEY, JSON.stringify(updated));
    return updated;
  },

  getProfile: async (): Promise<UserProfile> => {
    const { data } = await api.get("/api/user/profile");
    return fromProfileDTO(data);
  },
};
