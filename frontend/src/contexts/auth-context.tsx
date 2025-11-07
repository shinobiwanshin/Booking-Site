import { createContext, useState, useEffect, ReactNode } from "react";
import { useAuth as useOidcAuth } from "react-oidc-context";

interface SigninRedirectOptions {
  extraQueryParams?: Record<string, string>;
}

interface AuthContextType {
  isAuthenticated: boolean;
  isLoading: boolean;
  user: User | null;
  login: (username: string, password: string) => Promise<void>;
  logout: () => void;
  signinRedirect: (options?: SigninRedirectOptions) => Promise<void>;
  signoutRedirect: () => Promise<void>;
}

interface User {
  username: string;
  role: string;
  accessToken: string;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export { AuthContext };

export const AuthContextProvider: React.FC<{ children: ReactNode }> = ({
  children,
}) => {
  const oidcAuth = useOidcAuth();
  const [customUser, setCustomUser] = useState<User | null>(null);
  const [isCustomLoading, setIsCustomLoading] = useState(true);

  // Check for custom login tokens on mount
  useEffect(() => {
    const token = localStorage.getItem("access_token");
    const username = localStorage.getItem("username");
    const role = localStorage.getItem("userRole");

    if (token && username && role) {
      setCustomUser({
        username,
        role,
        accessToken: token,
      });
    }
    setIsCustomLoading(false);
  }, []);

  const login = async (username: string, password: string) => {
    try {
      const API_BASE_URL = import.meta.env.VITE_API_URL || "";
      const response = await fetch(`${API_BASE_URL}/api/auth/login`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ username, password }),
      });

      if (!response.ok) {
        const data = await response.json();
        console.error("Login failed:", data);
        throw new Error(data.message || "Login failed");
      }

      const data = await response.json();
      console.log("Login successful:", {
        username: data.username,
        role: data.role,
      });

      // Clear any existing auth data first
      localStorage.removeItem("access_token");
      localStorage.removeItem("refresh_token");
      localStorage.removeItem("userRole");
      localStorage.removeItem("username");

      // Store new tokens and user info
      localStorage.setItem("access_token", data.access_token);
      localStorage.setItem("refresh_token", data.refresh_token);
      localStorage.setItem("userRole", data.role);
      localStorage.setItem("username", data.username);

      setCustomUser({
        username: data.username,
        role: data.role,
        accessToken: data.access_token,
      });
    } catch (error) {
      console.error("Login error:", error);
      throw error;
    }
  };

  const logout = () => {
    // Clear custom login tokens
    localStorage.removeItem("access_token");
    localStorage.removeItem("refresh_token");
    localStorage.removeItem("userRole");
    localStorage.removeItem("username");
    setCustomUser(null);

    // Also logout from OIDC if authenticated
    if (oidcAuth.isAuthenticated) {
      oidcAuth.removeUser();
    }
  };

  const signinRedirect = async (options?: SigninRedirectOptions) => {
    await oidcAuth.signinRedirect(options);
  };

  const signoutRedirect = async () => {
    // Clear custom login tokens
    localStorage.removeItem("access_token");
    localStorage.removeItem("refresh_token");
    localStorage.removeItem("userRole");
    localStorage.removeItem("username");
    setCustomUser(null);

    // Also logout from OIDC if authenticated
    if (oidcAuth.isAuthenticated) {
      await oidcAuth.signoutRedirect();
    }
  };

  // Determine if user is authenticated from either custom login or OIDC
  const isAuthenticated = customUser !== null || oidcAuth.isAuthenticated;
  const isLoading = isCustomLoading || oidcAuth.isLoading;

  // Get user from either custom login or OIDC
  interface UserProfile {
    role?: string;
    preferred_username?: string;
  }

  const user =
    customUser ||
    (oidcAuth.user
      ? {
          username: oidcAuth.user.profile.preferred_username || "",
          role: (oidcAuth.user.profile as UserProfile).role || "ATTENDEE",
          accessToken: oidcAuth.user.access_token,
        }
      : null);

  return (
    <AuthContext.Provider
      value={{
        isAuthenticated,
        isLoading,
        user,
        login,
        logout,
        signinRedirect,
        signoutRedirect,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};
