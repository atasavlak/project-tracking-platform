import { apiRequest } from './httpClient.js'

export function getWorkItems(filters = {}, signal) {
  const params = new URLSearchParams()

  if (filters.projectId) {
    params.set('projectId', filters.projectId)
  }

  if (filters.weeklyReportId) {
    params.set('weeklyReportId', filters.weeklyReportId)
  }

  if (filters.status) {
    params.set('status', filters.status)
  }

  if (filters.responsiblePerson?.trim()) {
    params.set(
      'responsiblePerson',
      filters.responsiblePerson.trim(),
    )
  }

  const query = params.toString()

  return apiRequest(
    `/api/work-items${query ? `?${query}` : ''}`,
    { signal },
  )
}

export function getWorkItemById(workItemId, signal) {
  return apiRequest(`/api/work-items/${workItemId}`, {
    signal,
  })
}

export function createWorkItem(workItem) {
  return apiRequest('/api/work-items', {
    method: 'POST',
    body: workItem,
  })
}

export function updateWorkItem(workItemId, workItem) {
  return apiRequest(`/api/work-items/${workItemId}`, {
    method: 'PUT',
    body: workItem,
  })
}

export function patchWorkItem(workItemId, workItem) {
  return apiRequest(`/api/work-items/${workItemId}`, {
    method: 'PATCH',
    body: workItem,
  })
}

export function deactivateWorkItem(workItemId) {
  return apiRequest(`/api/work-items/${workItemId}`, {
    method: 'DELETE',
  })
}