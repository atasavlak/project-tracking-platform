import { apiRequest } from './httpClient.js'

export function getWeeklyReports(filters = {}, signal) {
  const params = new URLSearchParams()

  if (filters.projectId) {
    params.set('projectId', filters.projectId)
  }

  if (filters.status) {
    params.set('status', filters.status)
  }

  if (filters.weekStartDate) {
    params.set('weekStartDate', filters.weekStartDate)
  }

  if (filters.weekEndDate) {
    params.set('weekEndDate', filters.weekEndDate)
  }

  const query = params.toString()

  return apiRequest(
    `/api/weekly-reports${query ? `?${query}` : ''}`,
    { signal },
  )
}

export function getWeeklyReportById(
  weeklyReportId,
  signal,
) {
  return apiRequest(
    `/api/weekly-reports/${weeklyReportId}`,
    { signal },
  )
}

export function createWeeklyReport(weeklyReport) {
  return apiRequest('/api/weekly-reports', {
    method: 'POST',
    body: weeklyReport,
  })
}

export function updateWeeklyReport(
  weeklyReportId,
  weeklyReport,
) {
  return apiRequest(
    `/api/weekly-reports/${weeklyReportId}`,
    {
      method: 'PUT',
      body: weeklyReport,
    },
  )
}

export function patchWeeklyReport(
  weeklyReportId,
  weeklyReport,
) {
  return apiRequest(
    `/api/weekly-reports/${weeklyReportId}`,
    {
      method: 'PATCH',
      body: weeklyReport,
    },
  )
}

export function deactivateWeeklyReport(
  weeklyReportId,
) {
  return apiRequest(
    `/api/weekly-reports/${weeklyReportId}`,
    {
      method: 'DELETE',
    },
  )
}