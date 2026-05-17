import React, { createContext, useContext, useEffect, useState } from "react";
import * as A from "../lib/api";

const AuthCtx = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    try {
      const raw = localStorage.getItem("bibliotheca_user");
      return raw ? JSON.parse(raw) : null;
    } catch {
      return null;
    }
  });

  useEffect(() => {
    if (user) localStorage.setItem("bibliotheca_user", JSON.stringify(user));
    else localStorage.removeItem("bibliotheca_user");
  }, [user]);

  const login = async (email, password) => {
    const u = await A.login(email, password);
    setUser(u);
    return u;
  };
  const register = async (data) => {
    const u = await A.register(data);
    setUser(u);
    return u;
  };
  const logout = () => setUser(null);
  const isAdmin = user?.roleName === "ADMIN";

  return (
    <AuthCtx.Provider value={{ user, login, register, logout, isAdmin }}>{children}</AuthCtx.Provider>
  );
}

export const useAuth = () => useContext(AuthCtx);
