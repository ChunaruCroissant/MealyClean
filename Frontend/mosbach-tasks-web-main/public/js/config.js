window.API_BASE =
    location.hostname === "localhost"
        ? "http://localhost:8080"
        : "https://mealy-backend-r5ec.onrender.com";

// Centralized API + Auth helpers (Phase 3)
window.API = `${window.API_BASE}/api`;

// Token storage: we standardize on `token` but keep `authToken` as legacy fallback.
window.AUTH = {
  tokenKey: "token",
  legacyTokenKey: "authToken",

  getToken() {
    return (
      localStorage.getItem(this.tokenKey) ||
      localStorage.getItem(this.legacyTokenKey) ||
      null
    );
  },

  setToken(token) {
    if (!token) return;
    localStorage.setItem(this.tokenKey, token);
    // legacy key so older pages still work
    localStorage.setItem(this.legacyTokenKey, token);
  },

  clearToken() {
    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem(this.legacyTokenKey);
  },

  authHeaders(extra = {}) {
    const t = this.getToken();
    if (t) {
      extra["Authorization"] = `Bearer ${t}`;
    }
    return extra;
  },

  requireAuth(redirectTo = "Login.html") {
    if (!this.getToken()) {
      window.location.href = redirectTo;
      return false;
    }
    return true;
  },
};