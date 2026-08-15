import axios from 'axios'
import { getErrorMessage } from './errors'

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8081'

export const ACCESS_KEY = 'mtid.accessToken'
export const REFRESH_KEY = 'mtid.refreshToken'

export const api = axios.create({
  baseURL: API_URL,
  headers: { 'Content-Type': 'application/json' },
})

const raw = axios.create({
  baseURL: API_URL,
  headers: { 'Content-Type': 'application/json' },
})

function read(key) {
  return sessionStorage.getItem(key)
}

function writeTokens(accessToken, refreshToken) {
  if (accessToken) sessionStorage.setItem(ACCESS_KEY, accessToken)
  if (refreshToken) sessionStorage.setItem(REFRESH_KEY, refreshToken)
}

export function clearTokens() {
  sessionStorage.removeItem(ACCESS_KEY)
  sessionStorage.removeItem(REFRESH_KEY)
}

export function persistSession(payload) {
  writeTokens(payload.accessToken, payload.refreshToken)
  return payload
}

api.interceptors.request.use((config) => {
  const token = read(ACCESS_KEY)
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

let refreshPromise = null

async function refreshSession() {
  const refreshToken = read(REFRESH_KEY)
  if (!refreshToken) {
    throw new Error('Sessão expirada.')
  }
  const { data } = await raw.post('/api/v1/auth/owners/refresh', null, {
    headers: { Authorization: `Bearer ${refreshToken}` },
  })
  persistSession(data)
  return data.accessToken
}

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const original = error.config
    if (error.response?.status === 401 && original && !original._retry) {
      original._retry = true
      try {
        if (!refreshPromise) {
          refreshPromise = refreshSession().finally(() => {
            refreshPromise = null
          })
        }
        const accessToken = await refreshPromise
        original.headers.Authorization = `Bearer ${accessToken}`
        return api(original)
      } catch {
        clearTokens()
      }
    }
    return Promise.reject(error)
  },
)

export const ownerAuth = {
  login: (email, password) => api.post('/api/v1/auth/owners/token', { email, password }),
  google: (idToken) => api.post('/api/v1/auth/owners/google-token', { idToken }),
  logout: () =>
    raw.post('/api/v1/auth/owners/logout', null, {
      headers: { Authorization: `Bearer ${read(REFRESH_KEY) || read(ACCESS_KEY)}` },
    }),
}

export const owners = {
  me: () => api.get('/api/v1/owner/me'),
  list: () => api.get('/api/v1/owner'),
  get: (ownerId) => api.get(`/api/v1/owner/${ownerId}`),
  create: (body) => api.post('/api/v1/owner/create', body),
  forgotPassword: (email) => api.post('/api/v1/owner/password/forgot', { email }),
  disable: (ownerId) => api.patch(`/api/v1/owner/${ownerId}/disable`),
  enable: (ownerId) => api.patch(`/api/v1/owner/${ownerId}/enable`),
}

export const apps = {
  list: () => api.get('/api/v1/client-applications'),
  get: (appId) => api.get(`/api/v1/client-applications/${appId}`),
  create: (body) => api.post('/api/v1/client-applications/create', body),
  update: (body) => api.patch('/api/v1/client-applications/settings', body),
  rotateSecret: (appId) => api.patch(`/api/v1/client-applications/${appId}/rotate-secret`),
  disable: (appId) => api.patch(`/api/v1/client-applications/${appId}/disable`),
  enable: (appId) => api.patch(`/api/v1/client-applications/${appId}/enable`),
  addOwners: (body) => api.patch('/api/v1/client-applications/add-owner', body),
  removeOwner: (appId, ownerId) => api.delete(`/api/v1/client-applications/${appId}/owners/${ownerId}`),
}

export const roles = {
  list: (appId) => api.get(`/api/v1/user-roles/app/${appId}`),
  create: (appId, roleName) => api.post(`/api/v1/user-roles/app/${appId}/create`, { roleName }),
  update: (appId, body) => api.patch(`/api/v1/user-roles/app/${appId}/update`, body),
  remove: (userRoleId) => api.delete(`/api/v1/user-roles/${userRoleId}`),
}

export { getErrorMessage, API_URL }
