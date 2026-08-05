import { apiRequest } from './httpClient.js'

export function getAdminUsers(signal) {
  return apiRequest('/api/admin/users', {
    signal,
  })
}

export function getAdminUserById(id, signal) {
  return apiRequest(`/api/admin/users/${id}`, {
    signal,
  })
}

export function createAdminUser(requestBody) {
  return apiRequest('/api/admin/users', {
    method: 'POST',
    body: requestBody,
  })
}

export function updateAdminUserRole(id, role) {
  return apiRequest(`/api/admin/users/${id}/role`, {
    method: 'PATCH',
    body: {
      role,
    },
  })
}

export function updateAdminUserStatus(id, active) {
  return apiRequest(`/api/admin/users/${id}/status`, {
    method: 'PATCH',
    body: {
      active,
    },
  })
}

export function resendAdminUserActivation(id) {
  return apiRequest(
    `/api/admin/users/${id}/resend-activation`,
    {
      method: 'POST',
    },
  )
}
