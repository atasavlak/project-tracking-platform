import { apiRequest } from './httpClient.js'

export function getProjects(status, signal) {
  const params = new URLSearchParams()

  if (status) {
    params.set('status', status)
  }

  const query = params.toString()

  return apiRequest(
    `/api/projects${query ? `?${query}` : ''}`,
    { signal },
  )
}

export function getProjectById(projectId, signal) {
  return apiRequest(`/api/projects/${projectId}`, {
    signal,
  })
}

export function createProject(project) {
  return apiRequest('/api/projects', {
    method: 'POST',
    body: project,
  })
}

export function updateProject(projectId, project) {
  return apiRequest(`/api/projects/${projectId}`, {
    method: 'PUT',
    body: project,
  })
}

export function patchProject(projectId, project) {
  return apiRequest(`/api/projects/${projectId}`, {
    method: 'PATCH',
    body: project,
  })
}

export function deactivateProject(projectId) {
  return apiRequest(`/api/projects/${projectId}`, {
    method: 'DELETE',
  })
}