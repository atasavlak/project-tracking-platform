import { apiRequest } from './httpClient.js'

export function getCurrentUser(token, signal) {
  return apiRequest('/api/auth/me', {
    token,
    signal,
  })
}

export function activateAccount(payload, signal) {
  return apiRequest('/api/auth/activate', {
    method: 'POST',
    body: payload,
    signal,
    skipAuth: true,
  })
}

export function forgotPassword(payload, signal) {
  return apiRequest('/api/auth/forgot-password', {
    method: 'POST',
    body: payload,
    signal,
    skipAuth: true,
  })
}

export function resetPassword(payload, signal) {
  return apiRequest('/api/auth/reset-password', {
    method: 'POST',
    body: payload,
    signal,
    skipAuth: true,
  })
}
