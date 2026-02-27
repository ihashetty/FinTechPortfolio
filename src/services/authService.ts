// FUTURE: Integrate with Spring Boot backend
// Endpoint: /api/auth/login, /api/auth/register
// Method: POST
// FUTURE: JWT validation via Spring Security

import { mockUser } from "@/mock/data";
import type { UserProfile } from "@/types";

const AUTH_KEY = "niveshtrack_auth_token";
const USER_KEY = "niveshtrack_user";

export const authService = {
  login: async (email: string, _password: string): Promise<{ token: string; user: UserProfile }> => {
    // FUTURE: Replace with axios.post("/api/auth/login", { email, password })
    await new Promise((r) => setTimeout(r, 500));
    if (email) {
      const token = "mock_jwt_" + Date.now();
      const user = { ...mockUser, email };
      localStorage.setItem(AUTH_KEY, token);
      localStorage.setItem(USER_KEY, JSON.stringify(user));
      return { token, user };
    }
    throw new Error("Invalid credentials");
  },

  register: async (name: string, email: string, _password: string): Promise<{ token: string; user: UserProfile }> => {
    // FUTURE: Replace with axios.post("/api/auth/register", { name, email, password })
    await new Promise((r) => setTimeout(r, 500));
    const token = "mock_jwt_" + Date.now();
    const user: UserProfile = { name, email, currency: "₹", darkMode: false };
    localStorage.setItem(AUTH_KEY, token);
    localStorage.setItem(USER_KEY, JSON.stringify(user));
    return { token, user };
  },

  logout: () => {
    localStorage.removeItem(AUTH_KEY);
    localStorage.removeItem(USER_KEY);
  },

  isAuthenticated: (): boolean => {
    return !!localStorage.getItem(AUTH_KEY);
  },

  getUser: (): UserProfile | null => {
    const raw = localStorage.getItem(USER_KEY);
    return raw ? JSON.parse(raw) : null;
  },

  updateProfile: async (profile: Partial<UserProfile>): Promise<UserProfile> => {
    // FUTURE: Replace with axios.put("/api/user/profile", profile)
    const current = authService.getUser() || mockUser;
    const updated = { ...current, ...profile };
    localStorage.setItem(USER_KEY, JSON.stringify(updated));
    return updated;
  },
};
