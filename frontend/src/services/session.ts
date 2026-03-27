import type { AuthResponse, FarmSession, FarmSummary, UserSession } from '../types';

const TOKEN_KEY = 'token';
const USER_KEY = 'user';
const FARM_KEY = 'current_farm';
const FARMS_KEY = 'farms';
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
  localStorage.setItem(FARM_KEY, JSON.stringify(response.fazenda));
  localStorage.setItem(FARMS_KEY, JSON.stringify(response.farms));
  localStorage.removeItem(SUBSCRIPTION_REASON_KEY);
}

export function getCurrentFarm(): FarmSession | null {
  const raw = localStorage.getItem(FARM_KEY);
  if (!raw || raw === 'undefined' || raw === 'null') {
    return null;
  }

  try {
    return JSON.parse(raw) as FarmSession;
  } catch {
    return null;
  }
}

export function storeCurrentFarm(farm: FarmSession) {
  localStorage.setItem(FARM_KEY, JSON.stringify(farm));
}

export function getFarms(): FarmSummary[] {
  const raw = localStorage.getItem(FARMS_KEY);
  if (!raw || raw === 'undefined' || raw === 'null') {
    return [];
  }

  try {
    return JSON.parse(raw) as FarmSummary[];
  } catch {
    return [];
  }
}

export function clearSession() {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
  localStorage.removeItem(FARM_KEY);
  localStorage.removeItem(FARMS_KEY);
  localStorage.removeItem(SUBSCRIPTION_REASON_KEY);
}

export function storeFarms(farms: FarmSummary[]) {
  localStorage.setItem(FARMS_KEY, JSON.stringify(farms));
}

export function setSubscriptionReason(reason: string) {
  localStorage.setItem(SUBSCRIPTION_REASON_KEY, reason);
}

export function getSubscriptionReason(): string | null {
  return localStorage.getItem(SUBSCRIPTION_REASON_KEY);
}

export function clearSubscriptionReason() {
  localStorage.removeItem(SUBSCRIPTION_REASON_KEY);
}
