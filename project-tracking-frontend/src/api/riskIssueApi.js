import { apiRequest } from './httpClient.js'

export function getRiskIssues(filters = {}, signal) {
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

  if (filters.type) {
    params.set('type', filters.type)
  }

  if (filters.severity) {
    params.set('severity', filters.severity)
  }

  if (filters.status) {
    params.set('status', filters.status)
  }

  if (filters.responsibleUserId) {
    params.set(
      'responsibleUserId',
      filters.responsibleUserId,
    )
  }

  if (filters.followUpDateFrom) {
    params.set(
      'followUpDateFrom',
      filters.followUpDateFrom,
    )
  }

  if (filters.followUpDateTo) {
    params.set(
      'followUpDateTo',
      filters.followUpDateTo,
    )
  }

  const query = params.toString()

  return apiRequest(
    `/api/risk-issues${query ? `?${query}` : ''}`,
    { signal },
  )
}

export function getRiskIssueById(
  riskIssueId,
  signal,
) {
  return apiRequest(
    `/api/risk-issues/${riskIssueId}`,
    { signal },
  )
}

export function createRiskIssue(riskIssue) {
  return apiRequest('/api/risk-issues', {
    method: 'POST',
    body: riskIssue,
  })
}

export function updateRiskIssue(
  riskIssueId,
  riskIssue,
) {
  return apiRequest(
    `/api/risk-issues/${riskIssueId}`,
    {
      method: 'PUT',
      body: riskIssue,
    },
  )
}

export function patchRiskIssue(
  riskIssueId,
  riskIssue,
) {
  return apiRequest(
    `/api/risk-issues/${riskIssueId}`,
    {
      method: 'PATCH',
      body: riskIssue,
    },
  )
}

export function deactivateRiskIssue(riskIssueId) {
  return apiRequest(
    `/api/risk-issues/${riskIssueId}`,
    {
      method: 'DELETE',
    },
  )
}