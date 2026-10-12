import { create } from "zustand";
import type { AuthResponse } from "../types/auth";
import { logout as logoutApi } from "../api/authApi";

interface AuthState {
    user: AuthResponse | null;
    login: (user: AuthResponse) => void;
    logout: () => Promise<void>;
}

export const useAuthStore = create<AuthState>((set) => ({
    user: JSON.parse(localStorage.getItem("user") || "null"),

    login: (user) => {
        localStorage.setItem("user", JSON.stringify(user));
        localStorage.setItem("token", user.token);
        set({ user });
    },

    logout: async () => {
        try {
            await logoutApi();
        } catch (error) {
            console.error("Session logout failed:", error);
        } finally {
            localStorage.removeItem("user");
            localStorage.removeItem("token");
            set({ user: null });
        }
    }
}));