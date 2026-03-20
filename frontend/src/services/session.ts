import type { AuthResponse, UserSession } from '../types';

const TOKEN_KEY = 'token';
const USER_KEY = 'user';
const SUBSCRIPTION_REASON_KEY = 'subscription_reason';

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY);
}

export function getUser(): UserSession | null {
  const raw = localStorage.getItem(USER_KEY);
  if (!raw || raw === 'undefined' || raw === 'null') {
    return null;
  }

  try {
    return JSON.parse(raw) as UserSession;
  } catch {
    return null;
  }
}

export function storeAuthSession(response: AuthResponse) {
  localStorage.setItem(TOKEN_KEY, response.accessToken);
  localStorage.setItem(USER_KEY, JSON.stringify(response.usuario));
}

export function clearSession() {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
  localStorage.removeItem(SUBSCRIPTION_REASON_KEY);
}

export function setSubscriptionReason(reason: string) {
  localStorage.setItem(SUBSCRIPTION_REASON_KEY, reason);
}

export function getSubscriptionReason(): string | null {
  return localStorage.getItem(SUBSCRIPTION_REASON_KEY);
}
