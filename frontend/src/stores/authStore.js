import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api, {
  setAccessToken,
  clearAccessToken,
  ROLE_MAP,
  ROLE_MAP_REVERSE
} from '../services/api.js'
import { useMedAppState } from '../composables/useMedAppState.js'
import { screens } from '../constants/medapp.js'

export const useAuthStore = defineStore('auth', () => {
  // ─── State ────────────────────────────────────────────────────────────────
  const user = ref(null)        // { id?, email, nom, prenom }
  const role = ref(null)        // 'medecin' | 'secretaire' | 'admin' (frontend label)
  const isAuthenticated = ref(false)
  const isInitializing = ref(true)  // true while restoreSession is running

  // ─── Getters ──────────────────────────────────────────────────────────────
  const hasRole = computed(() => (requiredRole) => role.value === requiredRole)

  // ─── Helpers ──────────────────────────────────────────────────────────────
  const _setSession = (accessToken, backendRole, userData = null) => {
    setAccessToken(accessToken)
    role.value = ROLE_MAP_REVERSE[backendRole] ?? backendRole
    isAuthenticated.value = true
    if (userData) user.value = userData
  }

  const _clearSession = () => {
    clearAccessToken()
    user.value = null
    role.value = null
    isAuthenticated.value = false
  }

  // ─── Actions ──────────────────────────────────────────────────────────────

  /**
   * Login — POST /api/auth/login
   * @param {string} email
   * @param {string} password
   * @throws Error with .message from the backend JSON response
   */
  const login = async (email, password) => {
    const { data } = await api.post('/auth/login', { email, password })
    // data = { accessToken: string, role: 'MEDECIN' | 'SECRETAIRE' | 'ADMIN' }
    _setSession(data.accessToken, data.role)

    // Navigate to dashboard using existing composable
    const { signIn } = useMedAppState()
    signIn({ email, role: role.value })
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
      const { showScreen } = useMedAppState()
      showScreen(screens.login)
    }
  }

  /**
   * restoreSession — called once on app mount.
   * Attempts a silent token refresh using the httpOnly cookie.
   * If the cookie is missing/expired, the user stays unauthenticated.
   */
  const restoreSession = async () => {
    isInitializing.value = true
    try {
      const { data } = await api.post('/auth/refresh-token')
      _setSession(data.accessToken, data.role)
    } catch {
      // Cookie absent or expired → stay logged out, no action needed
      _clearSession()
    } finally {
      isInitializing.value = false
    }
  }

  // Listen to the custom event dispatched by the Axios interceptor on refresh failure
  if (typeof window !== 'undefined') {
    window.addEventListener('auth:logout', () => {
      _clearSession()
      const { showScreen } = useMedAppState()
      showScreen(screens.login)
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
