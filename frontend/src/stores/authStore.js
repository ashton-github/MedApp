import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api, {
  setAccessToken,
  clearAccessToken,
  ROLE_MAP,
  ROLE_MAP_REVERSE
} from '../services/api.js'
import { screens } from '../constants/medapp.js'

// ─── Session storage key ──────────────────────────────────────────────────────
// We persist only non-sensitive display info (email) across page refreshes.
// The access token itself is kept in memory only; the refresh token lives
// in an httpOnly cookie managed by the browser.
const SESSION_KEY = 'medapp_user'

const _loadPersistedUser = () => {
  try {
    const raw = sessionStorage.getItem(SESSION_KEY)
    return raw ? JSON.parse(raw) : null
  } catch {
    return null
  }
}

const _persistUser = (userData) => {
  try {
    sessionStorage.setItem(SESSION_KEY, JSON.stringify(userData))
  } catch { /* ignore */ }
}

const _clearPersistedUser = () => {
  try {
    sessionStorage.removeItem(SESSION_KEY)
  } catch { /* ignore */ }
}

export const useAuthStore = defineStore('auth', () => {
  // ─── State ────────────────────────────────────────────────────────────────
  const user = ref(_loadPersistedUser())  // { email } — restored from sessionStorage on page load
  const role = ref(null)                  // 'medecin' | 'secretaire' | 'admin' (frontend label)
  const isAuthenticated = ref(false)
  const isInitializing = ref(true)        // true while restoreSession is running

  // ─── Getters ──────────────────────────────────────────────────────────────
  const hasRole = computed(() => (requiredRole) => role.value === requiredRole)

  // ─── Helpers ──────────────────────────────────────────────────────────────
  /**
   * Sets in-memory token + role, optionally updates user info.
   * @param {string} accessToken
   * @param {string} backendRole  - backend enum value e.g. 'MEDECIN'
   * @param {{ email: string } | null} userData - supplied at login time
   */
  const _setSession = (accessToken, backendRole, userData = null) => {
    setAccessToken(accessToken)
    role.value = ROLE_MAP_REVERSE[backendRole] ?? backendRole
    isAuthenticated.value = true

    if (userData) {
      user.value = userData
      _persistUser(userData)
    }
    // If no userData provided (e.g. restoreSession), keep the value already
    // loaded from sessionStorage in the ref — nothing to update.
  }

  const _clearSession = () => {
    clearAccessToken()
    user.value = null
    role.value = null
    isAuthenticated.value = false
    _clearPersistedUser()
  }

  // ─── Actions ──────────────────────────────────────────────────────────────

  /**
   * Login — POST /api/auth/login
   * Navigates to dashboard after successful login.
   * @param {string} email
   * @param {string} password
   * @throws Error with .message from the backend JSON response
   */
  const login = async (email, password) => {
    const { data } = await api.post('/auth/login', { email, password })
    // data = { accessToken: string, role: 'MEDECIN' | 'SECRETAIRE' | 'ADMIN' }
    // We know the email because the user just typed it — pass it as userData.
    _setSession(data.accessToken, data.role, { email })

    // Navigate to dashboard — import lazily to avoid circular dependency
    const { useMedAppState } = await import('../composables/useMedAppState.js')
    useMedAppState().showScreen(screens.dashboard)
  }

  /**
   * Register — POST /api/auth/register
   * @param {{ firstName, lastName, email, password, role }} fields  (frontend labels)
   * @throws Error with .message from the backend JSON response
   */
  const register = async ({ firstName, lastName, email, password, role: frontRole }) => {
    const backendRole = ROLE_MAP[frontRole] ?? frontRole
    const { data } = await api.post('/auth/register', {
      email,
      password,
      nom: lastName,
      prenom: firstName,
      role: backendRole
    })
    // data = { id, email, nom, prenom, role }
    return data
  }

  /**
   * Logout — POST /api/auth/logout (invalidates the httpOnly cookie server-side)
   */
  const logout = async () => {
    try {
      await api.post('/auth/logout')
    } catch {
      // Ignore errors — clear session anyway
    } finally {
      _clearSession()
      const { useMedAppState } = await import('../composables/useMedAppState.js')
      useMedAppState().showScreen(screens.login)
    }
  }

  /**
   * restoreSession — called once on app mount.
   * Attempts a silent token refresh using the httpOnly cookie.
   * user.email is restored from sessionStorage (already loaded in initial state).
   * If the cookie is missing/expired, the user stays unauthenticated.
   */
  const restoreSession = async () => {
    isInitializing.value = true
    try {
      const { data } = await api.post('/auth/refresh-token')
      // No userData — user.value already populated from sessionStorage above.
      _setSession(data.accessToken, data.role)
    } catch {
      // Cookie absent or expired → clear everything including sessionStorage
      _clearSession()
    } finally {
      isInitializing.value = false
    }
  }

  // Listen to the custom event dispatched by the Axios interceptor on refresh failure
  if (typeof window !== 'undefined') {
    window.addEventListener('auth:logout', async () => {
      _clearSession()
      const { useMedAppState } = await import('../composables/useMedAppState.js')
      useMedAppState().showScreen(screens.login)
    })
  }

  return {
    // State
    user,
    role,
    isAuthenticated,
    isInitializing,
    // Getters
    hasRole,
    // Actions
    login,
    register,
    logout,
    restoreSession
  }
})
