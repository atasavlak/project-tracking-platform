import { apiRequest } from './httpClient.js'

export function getAssignableUsers(signal) {
  return apiRequest('/api/users/assignable', {
    signal,
  })
}