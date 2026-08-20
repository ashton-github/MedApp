import axios from 'axios'

// Role mapping: frontend labels → backend enum values
export const ROLE_MAP = {
  medecin: 'MEDECIN',
  secretaire: 'SECRETAIRE'
}

// Reverse mapping: backend enum values → frontend labels
export const ROLE_MAP_REVERSE = {
  MEDECIN: 'medecin',
  SECRETAIRE: 'secretaire'
}

// In-memory token storage (never localStorage)
let _accessToken = null

export const setAccessToken = (token) => { _accessToken = token }
export const getAccessToken = () => _accessToken
export const clearAccessToken = () => { _accessToken = null }

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api',
  withCredentials: true // required for httpOnly refresh_token cookie
})

// ─── Request interceptor ────────────────────────────────────────────────────
// Inject Authorization header if an access token is available in memory
api.interceptors.request.use((config) => {
  const token = getAccessToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// ─── Response interceptor ───────────────────────────────────────────────────
// On 401: attempt silent refresh via httpOnly cookie, then replay the request.
// If refresh fails: clear token and redirect to login.
let _isRefreshing = false
let _pendingRequests = []

const processQueue = (error, token = null) => {
  _pendingRequests.forEach((cb) => {
    if (error) cb.reject(error)
    else cb.resolve(token)
  })
  _pendingRequests = []
}

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config

    // Avoid infinite loop on refresh endpoint itself
    if (
      error.response?.status === 401 &&
      !originalRequest._retry &&
      !originalRequest.url?.includes('/auth/refresh-token') &&
      !originalRequest.url?.includes('/auth/login')
    ) {
      if (_isRefreshing) {
        // Queue concurrent requests while refresh is in progress
        return new Promise((resolve, reject) => {
          _pendingRequests.push({ resolve, reject })
        }).then((token) => {
          originalRequest.headers.Authorization = `Bearer ${token}`
          return api(originalRequest)
        })
      }

      originalRequest._retry = true
      _isRefreshing = true

      try {
        // POST /auth/refresh-token — the browser sends the httpOnly cookie automatically
        const { data } = await api.post('/auth/refresh-token')
        setAccessToken(data.accessToken)
        processQueue(null, data.accessToken)

        originalRequest.headers.Authorization = `Bearer ${data.accessToken}`
        return api(originalRequest)
      } catch (refreshError) {
        processQueue(refreshError, null)
        clearAccessToken()
        // Redirect to login: use a custom event so the app can react
        window.dispatchEvent(new CustomEvent('auth:logout'))
        return Promise.reject(refreshError)
      } finally {
        _isRefreshing = false
      }
    }

    return Promise.reject(error)
  }
)

export default api
