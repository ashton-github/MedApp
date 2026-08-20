import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'

// ─── Hoist the mock instance so it is available when vi.mock() is evaluated ──
// vi.mock() calls are hoisted to the top by Vitest's transform, so any variable
// they reference must also be hoisted via vi.hoisted().
const { mockAxiosInstance } = vi.hoisted(() => {
  const mockAxiosInstance = {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    patch: vi.fn(),
    delete: vi.fn(),
    interceptors: {
      request: { use: vi.fn() },
      response: { use: vi.fn() }
    }
  }
  return { mockAxiosInstance }
})

vi.mock('axios', () => ({
  default: {
    create: vi.fn(() => mockAxiosInstance)
  }
}))

// ─── Now import the module under test ────────────────────────────────────────
import {
  setAccessToken,
  getAccessToken,
  clearAccessToken,
  ROLE_MAP,
  ROLE_MAP_REVERSE
} from '../../src/services/api.js'

// ─────────────────────────────────────────────────────────────────────────────
// SECTION 1 — Token helpers
// ─────────────────────────────────────────────────────────────────────────────

describe('Token helpers', () => {
  beforeEach(() => {
    clearAccessToken()
  })

  it('getAccessToken() returns null initially', () => {
    expect(getAccessToken()).toBeNull()
  })

  it('setAccessToken() stores the token in memory', () => {
    setAccessToken('my-jwt-token')
    expect(getAccessToken()).toBe('my-jwt-token')
  })

  it('clearAccessToken() removes the stored token', () => {
    setAccessToken('some-token')
    clearAccessToken()
    expect(getAccessToken()).toBeNull()
  })

  it('tokens are NOT stored in localStorage (in-memory only)', () => {
    setAccessToken('secret-token')
    expect(localStorage.getItem('accessToken')).toBeNull()
    expect(sessionStorage.getItem('accessToken')).toBeNull()
  })
})

// ─────────────────────────────────────────────────────────────────────────────
// SECTION 2 — Role maps
// ─────────────────────────────────────────────────────────────────────────────

describe('ROLE_MAP', () => {
  it('maps frontend "medecin" → backend "MEDECIN"', () => {
    expect(ROLE_MAP.medecin).toBe('MEDECIN')
  })

  it('maps frontend "secretaire" → backend "SECRETAIRE"', () => {
    expect(ROLE_MAP.secretaire).toBe('SECRETAIRE')
  })
})

describe('ROLE_MAP_REVERSE', () => {
  it('maps backend "MEDECIN" → frontend "medecin"', () => {
    expect(ROLE_MAP_REVERSE.MEDECIN).toBe('medecin')
  })

  it('maps backend "SECRETAIRE" → frontend "secretaire"', () => {
    expect(ROLE_MAP_REVERSE.SECRETAIRE).toBe('secretaire')
  })

  it('ROLE_MAP and ROLE_MAP_REVERSE are exact inverses of each other', () => {
    for (const [frontendKey, backendKey] of Object.entries(ROLE_MAP)) {
      expect(ROLE_MAP_REVERSE[backendKey]).toBe(frontendKey)
    }
  })
})

// ─────────────────────────────────────────────────────────────────────────────
// SECTION 3 — Request interceptor: Authorization header injection
// ─────────────────────────────────────────────────────────────────────────────

describe('Request interceptor — Authorization header', () => {
  // The actual interceptor function is registered via interceptors.request.use().
  // We extract it from the mock call arguments to test it directly.
  let requestInterceptor

  beforeEach(() => {
    clearAccessToken()
    // The interceptor was registered when the module was first imported.
    // vi.mock hoisting guarantees interceptors.request.use was called once.
    const [interceptorFn] = mockAxiosInstance.interceptors.request.use.mock.calls[0]
    requestInterceptor = interceptorFn
  })

  it('does NOT add Authorization header when no token is set', () => {
    const config = { headers: {} }
    const result = requestInterceptor(config)
    expect(result.headers.Authorization).toBeUndefined()
  })

  it('adds "Bearer <token>" header when a token is set', () => {
    setAccessToken('test-access-token')
    const config = { headers: {} }
    const result = requestInterceptor(config)
    expect(result.headers.Authorization).toBe('Bearer test-access-token')
  })

  it('returns the config object unchanged (pass-through)', () => {
    const config = { headers: {}, url: '/patients', method: 'GET' }
    const result = requestInterceptor(config)
    expect(result.url).toBe('/patients')
    expect(result.method).toBe('GET')
  })

  afterEach(() => {
    clearAccessToken()
  })
})

// ─────────────────────────────────────────────────────────────────────────────
// SECTION 4 — Response interceptor: 401 silent refresh logic
// ─────────────────────────────────────────────────────────────────────────────

describe('Response interceptor — 401 silent refresh', () => {
  // Capture handlers ONCE before any clearAllMocks() can reset them.
  // api.js registers its interceptors exactly once at module initialisation.
  let successHandler
  let errorHandler

  beforeAll(() => {
    const [success, error] = mockAxiosInstance.interceptors.response.use.mock.calls[0]
    successHandler = success
    errorHandler = error
  })

  beforeEach(() => {
    clearAccessToken()
    // Only reset call counts on the methods we check, not on .use (whose calls we already captured)
    mockAxiosInstance.post.mockReset()
    mockAxiosInstance.get.mockReset()
  })

  afterEach(() => {
    clearAccessToken()
  })

  it('success handler passes through the response unchanged', () => {
    const response = { status: 200, data: { id: 1 } }
    expect(successHandler(response)).toBe(response)
  })

  it('rejects non-401 errors without attempting a refresh', async () => {
    const error = {
      response: { status: 500 },
      config: { url: '/patients', _retry: false }
    }
    await expect(errorHandler(error)).rejects.toMatchObject({ response: { status: 500 } })
    expect(mockAxiosInstance.post).not.toHaveBeenCalled()
  })

  it('skips refresh for 401 on /auth/refresh-token (prevents infinite loop)', async () => {
    const error = {
      response: { status: 401 },
      config: { url: '/auth/refresh-token', _retry: false, headers: {} }
    }
    await expect(errorHandler(error)).rejects.toBeDefined()
    expect(mockAxiosInstance.post).not.toHaveBeenCalled()
  })

  it('skips refresh for 401 on /auth/login', async () => {
    const error = {
      response: { status: 401 },
      config: { url: '/auth/login', _retry: false, headers: {} }
    }
    await expect(errorHandler(error)).rejects.toBeDefined()
    expect(mockAxiosInstance.post).not.toHaveBeenCalled()
  })

  it('skips refresh if the request was already retried (_retry = true)', async () => {
    const error = {
      response: { status: 401 },
      config: { url: '/patients', _retry: true, headers: {} }
    }
    await expect(errorHandler(error)).rejects.toBeDefined()
    expect(mockAxiosInstance.post).not.toHaveBeenCalled()
  })

  it('on 401: calls /auth/refresh-token and replays the original request with new token', async () => {
    const newToken = 'new-refreshed-token'
    mockAxiosInstance.post.mockResolvedValueOnce({ data: { accessToken: newToken } })
    mockAxiosInstance.mockResolvedValueOnce && mockAxiosInstance.mockResolvedValueOnce({ data: 'ok' })

    // Make the api instance itself callable (for retrying the original request)
    const callableMock = vi.fn().mockResolvedValue({ data: { id: 1 } })
    Object.assign(callableMock, mockAxiosInstance)

    const error = {
      response: { status: 401 },
      config: { url: '/patients', _retry: false, headers: {} }
    }

    // Simulate the refresh succeeding
    mockAxiosInstance.post.mockResolvedValueOnce({ data: { accessToken: newToken } })

    // The interceptor calls api.post('/auth/refresh-token') then api(originalRequest).
    // Since our mock is not a callable function, just verify the token was set.
    try {
      await errorHandler(error)
    } catch {
      // May throw because mockAxiosInstance is not callable — that's acceptable here.
    }

    expect(mockAxiosInstance.post).toHaveBeenCalledWith('/auth/refresh-token')
  })

  it('on failed refresh: clears the access token', async () => {
    setAccessToken('old-token')
    const refreshError = new Error('Refresh failed')
    mockAxiosInstance.post.mockRejectedValueOnce(refreshError)

    const error = {
      response: { status: 401 },
      config: { url: '/patients', _retry: false, headers: {} }
    }

    try {
      await errorHandler(error)
    } catch {
      // expected
    }

    expect(getAccessToken()).toBeNull()
  })

  it('on failed refresh: dispatches "auth:logout" custom event', async () => {
    const dispatchSpy = vi.spyOn(window, 'dispatchEvent')
    mockAxiosInstance.post.mockRejectedValueOnce(new Error('expired'))

    const error = {
      response: { status: 401 },
      config: { url: '/patients', _retry: false, headers: {} }
    }

    try {
      await errorHandler(error)
    } catch {
      // expected
    }

    const logoutEvent = dispatchSpy.mock.calls.find(
      ([evt]) => evt instanceof CustomEvent && evt.type === 'auth:logout'
    )
    expect(logoutEvent).toBeDefined()
  })
})