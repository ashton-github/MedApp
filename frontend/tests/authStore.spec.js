import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useAuthStore } from '../src/stores/authStore.js'

// Mock the api module so no real HTTP calls are made
vi.mock('../src/services/api.js', () => {
  const mockApi = {
    post: vi.fn(),
    interceptors: {
      request: { use: vi.fn() },
      response: { use: vi.fn() }
    }
  }
  return {
    default: mockApi,
    setAccessToken: vi.fn(),
    clearAccessToken: vi.fn(),
    getAccessToken: vi.fn(() => null),
    ROLE_MAP: { medecin: 'MEDECIN', secretaire: 'SECRETAIRE', admin: 'ADMIN' },
    ROLE_MAP_REVERSE: { MEDECIN: 'medecin', SECRETAIRE: 'secretaire', ADMIN: 'admin' }
  }
})

// Mock the composable used for navigation
vi.mock('../src/composables/useMedAppState.js', () => ({
  useMedAppState: () => ({
    signIn: vi.fn(),
    showScreen: vi.fn(),
    currentScreen: { value: 'login' }
  })
}))

// Mock constants
vi.mock('../src/constants/medapp.js', () => ({
  screens: { login: 'login', dashboard: 'dashboard' }
}))

// Get the mocked api to configure responses per test
import api from '../src/services/api.js'
import { setAccessToken, clearAccessToken } from '../src/services/api.js'

describe('authStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  // ─── login ────────────────────────────────────────────────────────────────

  describe('login()', () => {
    it('sets isAuthenticated and role on successful login', async () => {
      api.post.mockResolvedValueOnce({
        data: { accessToken: 'fake-access-token', role: 'MEDECIN' }
      })

      const store = useAuthStore()
      await store.login('jean@test.fr', 'Motdepasse1')

      expect(store.isAuthenticated).toBe(true)
      expect(store.role).toBe('medecin') // mapped from MEDECIN
      expect(setAccessToken).toHaveBeenCalledWith('fake-access-token')
    })

    it('maps SECRETAIRE role to secretaire', async () => {
      api.post.mockResolvedValueOnce({
        data: { accessToken: 'tok', role: 'SECRETAIRE' }
      })
      const store = useAuthStore()
      await store.login('sec@test.fr', 'Motdepasse1')
      expect(store.role).toBe('secretaire')
    })

    it('throws on 401 invalid credentials', async () => {
      const err = { response: { status: 401, data: { message: 'Email ou mot de passe incorrect.' } } }
      api.post.mockRejectedValueOnce(err)

      const store = useAuthStore()
      await expect(store.login('bad@test.fr', 'wrong')).rejects.toMatchObject(err)
      expect(store.isAuthenticated).toBe(false)
    })

    it('throws on 403 disabled account', async () => {
      const err = { response: { status: 403, data: { message: 'Ce compte est desactivé.' } } }
      api.post.mockRejectedValueOnce(err)

      const store = useAuthStore()
      await expect(store.login('disabled@test.fr', 'Motdepasse1')).rejects.toMatchObject(err)
      expect(store.isAuthenticated).toBe(false)
    })
  })

  // ─── register ─────────────────────────────────────────────────────────────

  describe('register()', () => {
    it('sends correct mapped payload to backend', async () => {
      api.post.mockResolvedValueOnce({
        data: { id: '123', email: 'jean@test.fr', nom: 'Martin', prenom: 'Jean', role: 'MEDECIN' }
      })

      const store = useAuthStore()
      const result = await store.register({
        firstName: 'Jean',
        lastName: 'Martin',
        email: 'jean@test.fr',
        password: 'Motdepasse1',
        role: 'medecin'
      })

      // Verify the API was called with backend-expected payload
      expect(api.post).toHaveBeenCalledWith('/auth/register', {
        email: 'jean@test.fr',
        password: 'Motdepasse1',
        nom: 'Martin',
        prenom: 'Jean',
        role: 'MEDECIN' // mapped from 'medecin'
      })

      expect(result.email).toBe('jean@test.fr')
    })

    it('maps secretaire role correctly', async () => {
      api.post.mockResolvedValueOnce({ data: { id: '456', email: 'sec@test.fr', nom: 'Dupont', prenom: 'Marie', role: 'SECRETAIRE' } })
      const store = useAuthStore()
      await store.register({ firstName: 'Marie', lastName: 'Dupont', email: 'sec@test.fr', password: 'Motdepasse1', role: 'secretaire' })

      expect(api.post).toHaveBeenCalledWith('/auth/register', expect.objectContaining({ role: 'SECRETAIRE' }))
    })

    it('throws 409 when email is already in use', async () => {
      const err = { response: { status: 409, data: { message: "L'email est déjà utilise : jean@test.fr" } } }
      api.post.mockRejectedValueOnce(err)

      const store = useAuthStore()
      await expect(store.register({
        firstName: 'Jean', lastName: 'Martin', email: 'jean@test.fr', password: 'Motdepasse1', role: 'medecin'
      })).rejects.toMatchObject(err)
    })
  })

  // ─── logout ───────────────────────────────────────────────────────────────

  describe('logout()', () => {
    it('clears session after logout', async () => {
      // First login
      api.post.mockResolvedValueOnce({ data: { accessToken: 'tok', role: 'ADMIN' } })
      const store = useAuthStore()
      await store.login('admin@test.fr', 'Motdepasse1')
      expect(store.isAuthenticated).toBe(true)

      // Then logout
      api.post.mockResolvedValueOnce({}) // POST /auth/logout → 204
      await store.logout()

      expect(store.isAuthenticated).toBe(false)
      expect(store.role).toBeNull()
      expect(clearAccessToken).toHaveBeenCalled()
    })

    it('clears session even if logout API call fails', async () => {
      api.post.mockResolvedValueOnce({ data: { accessToken: 'tok', role: 'MEDECIN' } })
      const store = useAuthStore()
      await store.login('jean@test.fr', 'Motdepasse1')

      api.post.mockRejectedValueOnce(new Error('Network error'))
      await store.logout() // should not throw

      expect(store.isAuthenticated).toBe(false)
    })
  })

  // ─── restoreSession ───────────────────────────────────────────────────────

  describe('restoreSession()', () => {
    it('restores session from httpOnly cookie via refresh-token endpoint', async () => {
      api.post.mockResolvedValueOnce({ data: { accessToken: 'new-token', role: 'MEDECIN' } })
      const store = useAuthStore()
      await store.restoreSession()

      expect(store.isAuthenticated).toBe(true)
      expect(store.role).toBe('medecin')
      expect(store.isInitializing).toBe(false)
    })

    it('stays unauthenticated if refresh cookie is absent/expired', async () => {
      api.post.mockRejectedValueOnce({ response: { status: 401 } })
      const store = useAuthStore()
      await store.restoreSession()

      expect(store.isAuthenticated).toBe(false)
      expect(store.isInitializing).toBe(false)
    })
  })

  // ─── hasRole getter ───────────────────────────────────────────────────────

  describe('hasRole getter', () => {
    it('returns true for the correct role', async () => {
      api.post.mockResolvedValueOnce({ data: { accessToken: 'tok', role: 'ADMIN' } })
      const store = useAuthStore()
      await store.login('admin@test.fr', 'Motdepasse1')

      expect(store.hasRole('admin')).toBe(true)
      expect(store.hasRole('medecin')).toBe(false)
    })
  })
})
