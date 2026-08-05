import { apiRequest } from './httpClient.js'

export function getDecisionLogs(filters = {}, signal) {
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

  if (filters.decisionOwnerId) {
    params.set(
      'decisionOwnerId',
      filters.decisionOwnerId,
    )
  }

  if (filters.decisionDateFrom) {
    params.set(
      'decisionDateFrom',
      filters.decisionDateFrom,
    )
  }

  if (filters.decisionDateTo) {
    params.set(
      'decisionDateTo',
      filters.decisionDateTo,
    )
  }

  const query = params.toString()

  return apiRequest(
    `/api/decision-logs${query ? `?${query}` : ''}`,
    { signal },
  )
}

export function getDecisionLogById(
  decisionLogId,
  signal,
) {
  return apiRequest(
    `/api/decision-logs/${decisionLogId}`,
    { signal },
  )
}

export function createDecisionLog(decisionLog) {
  return apiRequest('/api/decision-logs', {
    method: 'POST',
    body: decisionLog,
  })
}

export function updateDecisionLog(
  decisionLogId,
  decisionLog,
) {
  return apiRequest(
    `/api/decision-logs/${decisionLogId}`,
    {
      method: 'PUT',
      body: decisionLog,
    },
  )
}

export function patchDecisionLog(
  decisionLogId,
  decisionLog,
) {
  return apiRequest(
    `/api/decision-logs/${decisionLogId}`,
    {
      method: 'PATCH',
      body: decisionLog,
    },
  )
}

export function deactivateDecisionLog(
  decisionLogId,
) {
  return apiRequest(
    `/api/decision-logs/${decisionLogId}`,
    {
      method: 'DELETE',
    },
  )
}
