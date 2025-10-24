'use client';

import React, { createContext, useContext, useState, useEffect, useCallback, ReactNode } from 'react';
import { authAPI, UserDTO } from '@/lib/api';

interface AuthContextType {
  user: UserDTO | null;
  apiKey: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (apiKey: string) => Promise<void>;
  logout: () => void;
  register: (name: string) => Promise<UserDTO>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<UserDTO | null>(null);
  const [apiKey, setApiKey] = useState<string | null>(null);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isLoading, setIsLoading] = useState(true);

  const login = useCallback(async (key: string) => {
    try {
      localStorage.setItem('apiKey', key);
      const userData = await authAPI.getMe();
      setUser(userData);
      setApiKey(key);
      setIsAuthenticated(true);
    } catch (error) {
      localStorage.removeItem('apiKey');
      setUser(null);
      setApiKey(null);
      setIsAuthenticated(false);
      throw error;
    }
  }, []);

  useEffect(() => {
    // Check if user is already logged in (only on mount)
    const checkAuth = async () => {
      const storedApiKey = localStorage.getItem('apiKey');
      if (storedApiKey) {
        try {
          await login(storedApiKey);
        } catch (error) {
          // If login fails, clear stored data
          localStorage.removeItem('apiKey');
        }
      }
      setIsLoading(false);
    };

    checkAuth();
  }, [login]);

  const logout = () => {
    localStorage.removeItem('apiKey');
    setUser(null);
    setApiKey(null);
    setIsAuthenticated(false);
  };

  const register = async (name: string): Promise<UserDTO> => {
    const userData = await authAPI.register(name);
    await login(userData.apiKey);
    return userData;
  };

  return (
    <AuthContext.Provider value={{ user, apiKey, isAuthenticated, isLoading, login, logout, register }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
