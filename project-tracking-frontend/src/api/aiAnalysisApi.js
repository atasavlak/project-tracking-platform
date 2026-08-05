import { apiRequest } from './httpClient.js'

function buildDashboardSummaryQuery(status, healthStatus) {
  const searchParams = new URLSearchParams()

  if (status) {
    searchParams.set('status', status)
  }

  if (healthStatus) {
    searchParams.set('healthStatus', healthStatus)
  }

  const query = searchParams.toString()
  return query ? `?${query}` : ''
}

export function analyzeWeeklyReport(payload) {
  return apiRequest('/api/ai/weekly-report-analysis', {
    method: 'POST',
    body: payload,
  })
}

export function getAiDashboardSummary(
  status,
  healthStatus,
  signal,
) {
  return apiRequest(
    `/api/ai/dashboard-summary${buildDashboardSummaryQuery(
      status,
      healthStatus,
    )}`,
    { signal },
  )
}
