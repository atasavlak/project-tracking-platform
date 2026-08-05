import { apiDownload, apiRequest } from './httpClient.js'

function buildDashboardQuery(status, healthStatus) {
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

export function getDashboardSummary(status, healthStatus, signal) {
  return apiRequest(
    `/api/dashboard/summary${buildDashboardQuery(status, healthStatus)}`,
    { signal },
  )
}

export function getDashboardProjects(
  status,
  healthStatus,
  signal,
) {
  return apiRequest(
    `/api/dashboard/projects${buildDashboardQuery(
      status,
      healthStatus,
    )}`,
    { signal },
  )
}

export function getDashboardRiskyWorkItems(
  status,
  healthStatus,
  signal,
) {
  return apiRequest(
    `/api/dashboard/risky-work-items${buildDashboardQuery(
      status,
      healthStatus,
    )}`,
    { signal },
  )
}

export function getDashboardCriticalRisks(
  status,
  healthStatus,
  signal,
) {
  return apiRequest(
    `/api/dashboard/critical-risks${buildDashboardQuery(
      status,
      healthStatus,
    )}`,
    { signal },
  )
}

export function getDashboardOverdueActions(
  status,
  healthStatus,
  signal,
) {
  return apiRequest(
    `/api/dashboard/overdue-actions${buildDashboardQuery(
      status,
      healthStatus,
    )}`,
    { signal },
  )
}

export function downloadDashboardPdf(status, healthStatus) {
  return apiDownload(
    `/api/dashboard/export/pdf${buildDashboardQuery(
      status,
      healthStatus,
    )}`,
    'cto-proje-raporu.pdf',
  )
}

export function downloadDashboardExcel(status, healthStatus) {
  return apiDownload(
    `/api/dashboard/export/excel${buildDashboardQuery(
      status,
      healthStatus,
    )}`,
    'cto-proje-raporu.xlsx',
  )
}
