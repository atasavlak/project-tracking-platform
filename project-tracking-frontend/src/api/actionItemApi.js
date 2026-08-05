import { apiRequest } from './httpClient.js'

export function getActionItems(filters = {}, signal) {
  const params = new URLSearchParams()

  if (filters.projectId) {
    params.set('projectId', filters.projectId)
  }

  if (filters.weeklyReportId) {
    params.set(
      'weeklyReportId',
      filters.weeklyReportId,
    )
  }

  if (filters.status) {
    params.set('status', filters.status)
  }

  if (filters.priority) {
    params.set('priority', filters.priority)
  }

  if (filters.responsibleUserId) {
    params.set(
      'responsibleUserId',
      filters.responsibleUserId,
    )
  }

  if (filters.targetDateFrom) {
    params.set(
      'targetDateFrom',
      filters.targetDateFrom,
    )
  }

  if (filters.targetDateTo) {
    params.set(
      'targetDateTo',
      filters.targetDateTo,
    )
  }

  const query = params.toString()

  return apiRequest(
    `/api/action-items${query ? `?${query}` : ''}`,
    { signal },
  )
}

export function getActionItemById(
  actionItemId,
  signal,
) {
  return apiRequest(
    `/api/action-items/${actionItemId}`,
    { signal },
  )
}

export function createActionItem(actionItem) {
  return apiRequest('/api/action-items', {
    method: 'POST',
    body: actionItem,
  })
}

export function updateActionItem(
  actionItemId,
  actionItem,
) {
  return apiRequest(
    `/api/action-items/${actionItemId}`,
    {
      method: 'PUT',
      body: actionItem,
    },
  )
}

export function patchActionItem(
  actionItemId,
  actionItem,
) {
  return apiRequest(
    `/api/action-items/${actionItemId}`,
    {
      method: 'PATCH',
      body: actionItem,
    },
  )
}

export function deactivateActionItem(actionItemId) {
  return apiRequest(
    `/api/action-items/${actionItemId}`,
    {
      method: 'DELETE',
    },
  )
}
