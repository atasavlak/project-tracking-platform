import { getAiDashboardSummary } from '../api/aiAnalysisApi.js'
import { useEffect, useMemo, useState } from 'react'
import {
  downloadDashboardExcel,
  downloadDashboardPdf,
  getDashboardCriticalRisks,
  getDashboardOverdueActions,
  getDashboardProjects,
  getDashboardRiskyWorkItems,
  getDashboardSummary,
} from '../api/dashboardApi.js'
import ErrorState from '../components/ErrorState.jsx'
import LoadingState from '../components/LoadingState.jsx'
import StatusBadge from '../components/StatusBadge.jsx'
import {
  ACTION_ITEM_PRIORITY_LABELS,
  PROJECT_HEALTH_STATUSES,
  PROJECT_HEALTH_STATUS_LABELS,
  PROJECT_STATUSES,
  PROJECT_STATUS_LABELS,
  RISK_ISSUE_SEVERITY_LABELS,
  RISK_ISSUE_TYPE_LABELS,
} from '../utils/statuses.js'

function formatDate(value) {
  if (!value) {
    return '-'
  }

  return new Intl.DateTimeFormat('tr-TR').format(
    new Date(`${value}T00:00:00`),
  )
}


function formatDateTime(value) {
  if (!value) {
    return '-'
  }

  return new Intl.DateTimeFormat('tr-TR', {
    dateStyle: 'short',
    timeStyle: 'short',
  }).format(new Date(value))
}

function getErrorMessage(error) {
  return error?.message || 'Dashboard bilgileri alınamadı.'
}

function getCompletionRate(project) {
  if (Number.isFinite(project.completionRate)) {
    return project.completionRate
  }

  if (!project.totalWorkItems) {
    return 0
  }

  return Math.round(
    (project.completedWorkItems / project.totalWorkItems) * 100,
  )
}

function HealthBadge({ status, score }) {
  return (
    <span
      className={`health-badge health-${
        status?.toLowerCase().replaceAll('_', '-') ?? 'unknown'
      }`}
    >
      <span>{PROJECT_HEALTH_STATUS_LABELS[status] ?? status}</span>
      {Number.isFinite(score) ? <strong>{score}</strong> : null}
    </span>
  )
}


function AiDashboardSummaryPanel({
  analysis,
  isLoading,
  error,
  onRefresh,
}) {
  const appliedFilters = [
    analysis?.appliedProjectStatus
      ? PROJECT_STATUS_LABELS[analysis.appliedProjectStatus]
      : null,
    analysis?.appliedHealthStatus
      ? PROJECT_HEALTH_STATUS_LABELS[analysis.appliedHealthStatus]
      : null,
  ].filter(Boolean)

  return (
    <section className="content-card dashboard-ai-summary-card">
      <div className="dashboard-ai-summary-heading">
        <div>
          <span className="dashboard-ai-eyebrow">AI PORTFÖY ANALİZİ</span>
          <h2>Haftalık Yönetici Özeti</h2>
          <p>
            Son haftalık raporlar, proje sağlığı, riskler, aksiyonlar
            ve kritik iş kalemleri birlikte değerlendirilir.
          </p>
        </div>

        <div className="dashboard-ai-heading-actions">
          {analysis?.overallStatus ? (
            <HealthBadge status={analysis.overallStatus} />
          ) : null}

          <button
            type="button"
            className="button button-secondary"
            onClick={onRefresh}
            disabled={isLoading}
          >
            {isLoading ? 'AI özeti hazırlanıyor...' : 'AI Özetini Yenile'}
          </button>
        </div>
      </div>

      <div className="dashboard-ai-disclaimer">
        Bu çıktı kural tabanlı prototip sağlayıcı tarafından üretilir.
        Yönetim kararı verilmeden önce kaynak kayıtlar kontrol edilmelidir.
      </div>

      {error ? (
        <div className="dashboard-ai-error">{error}</div>
      ) : null}

      {isLoading && !analysis ? (
        <div className="dashboard-ai-loading">
          Haftalık raporlar ve kritik göstergeler analiz ediliyor...
        </div>
      ) : null}

      {analysis ? (
        <div className="dashboard-ai-content">
          <article className="dashboard-ai-executive-summary">
            <span>Yönetici özeti</span>
            <p>{analysis.executiveSummary}</p>
          </article>

          <div className="dashboard-ai-indicators">
            <span>{analysis.analyzedProjectCount} proje</span>
            <span>{analysis.analyzedWeeklyReportCount} haftalık rapor</span>
            <span>{analysis.criticalProjectCount} kritik proje</span>
            <span>{analysis.criticalRiskIssueCount} kritik risk</span>
            <span>{analysis.overdueActionItemCount} gecikmiş aksiyon</span>
            <span>{analysis.criticalWorkItemCount} kritik iş kalemi</span>
          </div>

          <div className="dashboard-ai-grid">
            <article className="dashboard-ai-list-card">
              <h3>Öne Çıkan Göstergeler</h3>
              <ul>
                {analysis.highlights.map((item) => (
                  <li key={item}>{item}</li>
                ))}
              </ul>
            </article>

            <article className="dashboard-ai-list-card">
              <h3>Önerilen Yönetim Aksiyonları</h3>
              <ul>
                {analysis.recommendations.map((item) => (
                  <li key={item}>{item}</li>
                ))}
              </ul>
            </article>
          </div>

          <div className="dashboard-ai-grid">
            <article className="dashboard-ai-list-card">
              <h3>Müdahale Gereken Projeler</h3>
              {analysis.attentionProjects.length === 0 ? (
                <p className="dashboard-ai-empty">
                  Müdahale gerektiren proje tespit edilmedi.
                </p>
              ) : (
                <div className="dashboard-ai-project-list">
                  {analysis.attentionProjects.map((project) => (
                    <div
                      key={project.projectId}
                      className="dashboard-ai-project-item"
                    >
                      <div>
                        <strong>{project.projectName}</strong>
                        <span>
                          Son rapor: {formatDate(project.latestReportWeekStartDate)}
                        </span>
                      </div>
                      <HealthBadge
                        status={project.healthStatus}
                        score={project.healthScore}
                      />
                      <p>{project.reason}</p>
                    </div>
                  ))}
                </div>
              )}
            </article>

            <article className="dashboard-ai-list-card">
              <h3>Son Raporlardan Sinyaller</h3>
              <ul>
                {analysis.weeklyReportInsights.map((item) => (
                  <li key={item}>{item}</li>
                ))}
              </ul>
            </article>
          </div>

          <div className="dashboard-ai-footer">
            <span>Sağlayıcı: {analysis.provider}</span>
            <span>Analiz zamanı: {formatDateTime(analysis.analyzedAt)}</span>
            <span>
              Filtre: {appliedFilters.length > 0
                ? appliedFilters.join(' / ')
                : 'Tüm aktif projeler'}
            </span>
          </div>
        </div>
      ) : null}
    </section>
  )
}

export default function DashboardPage() {
  const [summary, setSummary] = useState(null)
  const [projects, setProjects] = useState([])
  const [riskyWorkItems, setRiskyWorkItems] = useState([])
  const [criticalRisks, setCriticalRisks] = useState([])
  const [overdueActions, setOverdueActions] = useState([])
  const [aiSummary, setAiSummary] = useState(null)
  const [aiError, setAiError] = useState('')
  const [isAiLoading, setIsAiLoading] = useState(true)
  const [aiReloadKey, setAiReloadKey] = useState(0)
  const [projectStatus, setProjectStatus] = useState('')
  const [healthStatus, setHealthStatus] = useState('')
  const [error, setError] = useState('')
  const [exportError, setExportError] = useState('')
  const [isLoading, setIsLoading] = useState(true)
  const [exportType, setExportType] = useState('')
  const [reloadKey, setReloadKey] = useState(0)

  useEffect(() => {
    const controller = new AbortController()

    setIsLoading(true)
    setError('')

    Promise.all([
      getDashboardSummary(
        projectStatus || null,
        healthStatus || null,
        controller.signal,
      ),
      getDashboardProjects(
        projectStatus || null,
        healthStatus || null,
        controller.signal,
      ),
      getDashboardRiskyWorkItems(
        projectStatus || null,
        healthStatus || null,
        controller.signal,
      ),
      getDashboardCriticalRisks(
        projectStatus || null,
        healthStatus || null,
        controller.signal,
      ),
      getDashboardOverdueActions(
        projectStatus || null,
        healthStatus || null,
        controller.signal,
      ),
    ])
      .then(
        ([
          summaryResponse,
          projectResponse,
          riskyResponse,
          criticalRiskResponse,
          overdueActionResponse,
        ]) => {
          setSummary(summaryResponse)
          setProjects(projectResponse)
          setRiskyWorkItems(riskyResponse)
          setCriticalRisks(criticalRiskResponse)
          setOverdueActions(overdueActionResponse)
        },
      )
      .catch((requestError) => {
        if (requestError.name !== 'AbortError') {
          setError(getErrorMessage(requestError))
        }
      })
      .finally(() => {
        if (!controller.signal.aborted) {
          setIsLoading(false)
        }
      })

    return () => controller.abort()
  }, [projectStatus, healthStatus, reloadKey])

  useEffect(() => {
    const controller = new AbortController()

    setIsAiLoading(true)
    setAiError('')

    getAiDashboardSummary(
      projectStatus || null,
      healthStatus || null,
      controller.signal,
    )
      .then((response) => {
        setAiSummary(response)
      })
      .catch((requestError) => {
        if (requestError.name !== 'AbortError') {
          setAiError(
            requestError?.message ||
              'AI yönetici özeti oluşturulamadı.',
          )
        }
      })
      .finally(() => {
        if (!controller.signal.aborted) {
          setIsAiLoading(false)
        }
      })

    return () => controller.abort()
  }, [projectStatus, healthStatus, reloadKey, aiReloadKey])

  const criticalWorkItemCount = useMemo(
    () =>
      riskyWorkItems.filter(
        (workItem) =>
          workItem.status === 'BLOCKED' ||
          workItem.status === 'DELAYED',
      ).length,
    [riskyWorkItems],
  )

  const healthyProjectRate = useMemo(() => {
    if (!summary?.totalActiveProjects) {
      return 0
    }

    return Math.round(
      (summary.healthyProjects / summary.totalActiveProjects) * 100,
    )
  }, [summary])

  async function handleExport(type) {
    setExportType(type)
    setExportError('')

    try {
      if (type === 'pdf') {
        await downloadDashboardPdf(
          projectStatus || null,
          healthStatus || null,
        )
      } else {
        await downloadDashboardExcel(
          projectStatus || null,
          healthStatus || null,
        )
      }
    } catch (requestError) {
      setExportError(
        requestError?.message || 'Rapor indirilemedi.',
      )
    } finally {
      setExportType('')
    }
  }

  return (
    <div className="page-stack">
      <section className="page-heading dashboard-page-heading">
        <div>
          <span className="eyebrow">YÖNETİCİ GÖRÜNÜMÜ</span>
          <h1>CTO Dashboard</h1>
          <p>
            Proje sağlığını, riskleri, kararları, aksiyonları ve
            kritik iş kalemlerini tek ekrandan takip edin.
          </p>
        </div>

        <div className="page-heading-actions dashboard-heading-actions">
          <button
            type="button"
            className="button button-secondary"
            onClick={() => handleExport('pdf')}
            disabled={Boolean(exportType)}
          >
            {exportType === 'pdf' ? 'PDF hazırlanıyor...' : 'PDF İndir'}
          </button>

          <button
            type="button"
            className="button button-secondary"
            onClick={() => handleExport('excel')}
            disabled={Boolean(exportType)}
          >
            {exportType === 'excel'
              ? 'Excel hazırlanıyor...'
              : 'Excel İndir'}
          </button>

          <button
            type="button"
            className="button button-primary"
            onClick={() =>
              setReloadKey((currentValue) => currentValue + 1)
            }
            disabled={isLoading}
          >
            Yenile
          </button>
        </div>
      </section>

      {exportError ? (
        <div className="dashboard-export-error">{exportError}</div>
      ) : null}

      {isLoading ? (
        <LoadingState message="Dashboard yükleniyor..." />
      ) : null}

      {error ? (
        <ErrorState
          message={error}
          onRetry={() =>
            setReloadKey((currentValue) => currentValue + 1)
          }
        />
      ) : null}

      {summary && !isLoading && !error ? (
        <>
          <section className="metric-grid dashboard-metric-grid">
            <article className="metric-card metric-card-success">
              <span>Sağlıklı Proje</span>
              <strong>{summary.healthyProjects}</strong>
              <small>Aktif projelerin %{healthyProjectRate} kadarı</small>
            </article>

            <article className="metric-card metric-card-warning">
              <span>Dikkat Gerektiren</span>
              <strong>{summary.needsAttentionProjects}</strong>
              <small>Risk veya takip ihtiyacı bulunan projeler</small>
            </article>

            <article className="metric-card metric-card-danger">
              <span>Kritik Proje</span>
              <strong>{summary.criticalProjects}</strong>
              <small>Gecikme veya kritik kayıt bulunan projeler</small>
            </article>

            <article className="metric-card">
              <span>Raporsuz Proje</span>
              <strong>{summary.projectsWithoutReport}</strong>
              <small>Aktif haftalık raporu bulunmayan projeler</small>
            </article>

            <article className="metric-card metric-card-warning">
              <span>Açık Risk / Engel</span>
              <strong>{summary.openRiskIssues}</strong>
              <small>{summary.criticalRiskIssues} kritik kayıt</small>
            </article>

            <article className="metric-card">
              <span>Karar Kaydı</span>
              <strong>{summary.totalActiveDecisions}</strong>
              <small>
                {summary.implementedDecisions} karar uygulandı
              </small>
            </article>

            <article className="metric-card">
              <span>Açık Aksiyon</span>
              <strong>{summary.openActionItems}</strong>
              <small>{summary.completedActionItems} tamamlandı</small>
            </article>

            <article className="metric-card metric-card-danger">
              <span>Gecikmiş Aksiyon</span>
              <strong>{summary.overdueActionItems}</strong>
              <small>Hedef tarihi geçmiş aktif aksiyonlar</small>
            </article>

            <article className="metric-card">
              <span>Aktif İş Kalemi</span>
              <strong>{summary.totalActiveWorkItems}</strong>
              <small>{summary.inProgressWorkItems} devam ediyor</small>
            </article>

            <article className="metric-card metric-card-danger">
              <span>Kritik İş Kalemi</span>
              <strong>{criticalWorkItemCount}</strong>
              <small>Blokeli veya gecikmiş kayıtlar</small>
            </article>
          </section>


          <AiDashboardSummaryPanel
            analysis={aiSummary}
            isLoading={isAiLoading}
            error={aiError}
            onRefresh={() =>
              setAiReloadKey((currentValue) => currentValue + 1)
            }
          />

          <section className="content-card">
            <div className="section-heading dashboard-section-heading">
              <div>
                <h2>Proje Sağlık Görünümü</h2>
                <p>
                  Durum, son rapor, iş kalemleri, riskler ve
                  aksiyonlardan hesaplanan sağlık skoru
                </p>
              </div>

              <div className="dashboard-filter-group">
                <label className="compact-field dashboard-status-filter">
                  <span>Proje durumu</span>
                  <select
                    value={projectStatus}
                    onChange={(event) =>
                      setProjectStatus(event.target.value)
                    }
                  >
                    <option value="">Tüm Durumlar</option>
                    {PROJECT_STATUSES.map((status) => (
                      <option key={status} value={status}>
                        {PROJECT_STATUS_LABELS[status]}
                      </option>
                    ))}
                  </select>
                </label>

                <label className="compact-field dashboard-status-filter">
                  <span>Sağlık durumu</span>
                  <select
                    value={healthStatus}
                    onChange={(event) =>
                      setHealthStatus(event.target.value)
                    }
                  >
                    <option value="">Tüm Sağlık Durumları</option>
                    {PROJECT_HEALTH_STATUSES.map((status) => (
                      <option key={status} value={status}>
                        {PROJECT_HEALTH_STATUS_LABELS[status]}
                      </option>
                    ))}
                  </select>
                </label>
              </div>
            </div>

            {projects.length === 0 ? (
              <div className="dashboard-empty-state">
                Seçilen filtrelere uygun aktif proje bulunamadı.
              </div>
            ) : (
              <div className="dashboard-project-grid dashboard-health-grid">
                {projects.map((project) => {
                  const completionRate = getCompletionRate(project)

                  return (
                    <article
                      key={project.projectId}
                      className="dashboard-project-card"
                    >
                      <div className="dashboard-project-card-header">
                        <div>
                          <span>Proje #{project.projectId}</span>
                          <h3>{project.projectName}</h3>
                        </div>

                        <div className="dashboard-project-badges">
                          <HealthBadge
                            status={project.healthStatus}
                            score={project.healthScore}
                          />
                          <StatusBadge status={project.projectStatus} />
                        </div>
                      </div>

                      <div className="dashboard-project-manager">
                        <span>Proje Yöneticisi</span>
                        <strong>
                          {project.projectManagerFullName || '-'}
                        </strong>
                      </div>

                      <div className="dashboard-progress-block">
                        <div>
                          <span>Tamamlanma oranı</span>
                          <strong>%{completionRate}</strong>
                        </div>

                        <div className="dashboard-progress-track">
                          <span style={{ width: `${completionRate}%` }} />
                        </div>
                      </div>

                      <div className="dashboard-project-counts dashboard-health-counts">
                        <div>
                          <span>Açık Risk</span>
                          <strong>{project.openRiskIssues}</strong>
                        </div>
                        <div>
                          <span>Kritik Risk</span>
                          <strong>{project.criticalRiskIssues}</strong>
                        </div>
                        <div>
                          <span>Karar</span>
                          <strong>{project.decisionCount}</strong>
                        </div>
                        <div>
                          <span>Açık Aksiyon</span>
                          <strong>{project.openActionItems}</strong>
                        </div>
                        <div>
                          <span>Gecikmiş Aksiyon</span>
                          <strong>{project.overdueActionItems}</strong>
                        </div>
                        <div>
                          <span>Blokeli / Gecikmiş İş</span>
                          <strong>
                            {project.blockedWorkItems +
                              project.delayedWorkItems}
                          </strong>
                        </div>
                      </div>

                      <div className="dashboard-latest-report">
                        <div className="dashboard-latest-report-heading">
                          <div>
                            <span>Son Haftalık Rapor</span>
                            <strong>
                              {formatDate(
                                project.latestReportWeekStartDate,
                              )}
                            </strong>
                          </div>

                          {project.latestReportStatus ? (
                            <StatusBadge
                              status={project.latestReportStatus}
                            />
                          ) : null}
                        </div>

                        <p>
                          {project.latestReportSummary ||
                            'Bu proje için aktif haftalık rapor bulunmuyor.'}
                        </p>

                        {project.latestReportRisks ? (
                          <div className="dashboard-report-risk">
                            <span>Rapordaki risk</span>
                            <p>{project.latestReportRisks}</p>
                          </div>
                        ) : null}
                      </div>
                    </article>
                  )
                })}
              </div>
            )}
          </section>

          <section className="dashboard-attention-grid">
            <article className="content-card table-card">
              <div className="section-heading dashboard-table-heading">
                <div>
                  <h2>Kritik Risk ve Engeller</h2>
                  <p>Açık, yüksek veya kritik önemdeki kayıtlar</p>
                </div>
                <strong className="section-total">
                  {criticalRisks.length} kayıt
                </strong>
              </div>

              {criticalRisks.length === 0 ? (
                <div className="dashboard-empty-state">
                  Kritik risk veya engel bulunmuyor.
                </div>
              ) : (
                <div className="table-scroll dashboard-compact-table">
                  <table>
                    <thead>
                      <tr>
                        <th>Kayıt</th>
                        <th>Proje</th>
                        <th>Önem</th>
                        <th>Sorumlu</th>
                        <th>Takip</th>
                      </tr>
                    </thead>
                    <tbody>
                      {criticalRisks.map((risk) => (
                        <tr key={risk.riskIssueId}>
                          <td>
                            <div className="table-primary-cell">
                              <strong>{risk.title}</strong>
                              <span>
                                {RISK_ISSUE_TYPE_LABELS[risk.type]}
                              </span>
                            </div>
                          </td>
                          <td>{risk.projectName}</td>
                          <td>
                            <span
                              className={`risk-severity risk-severity-${risk.severity.toLowerCase()}`}
                            >
                              {RISK_ISSUE_SEVERITY_LABELS[
                                risk.severity
                              ]}
                            </span>
                          </td>
                          <td>
                            {risk.responsibleUserFullName || '-'}
                          </td>
                          <td>{formatDate(risk.followUpDate)}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </article>

            <article className="content-card table-card">
              <div className="section-heading dashboard-table-heading">
                <div>
                  <h2>Gecikmiş Aksiyonlar</h2>
                  <p>Hedef tarihi geçmiş açık aksiyonlar</p>
                </div>
                <strong className="section-total">
                  {overdueActions.length} kayıt
                </strong>
              </div>

              {overdueActions.length === 0 ? (
                <div className="dashboard-empty-state">
                  Gecikmiş aksiyon bulunmuyor.
                </div>
              ) : (
                <div className="table-scroll dashboard-compact-table">
                  <table>
                    <thead>
                      <tr>
                        <th>Aksiyon</th>
                        <th>Proje</th>
                        <th>Öncelik</th>
                        <th>Sorumlu</th>
                        <th>Gecikme</th>
                      </tr>
                    </thead>
                    <tbody>
                      {overdueActions.map((action) => (
                        <tr key={action.actionItemId}>
                          <td>
                            <div className="table-primary-cell">
                              <strong>{action.title}</strong>
                              <span>
                                Hedef: {formatDate(action.targetDate)}
                              </span>
                            </div>
                          </td>
                          <td>{action.projectName}</td>
                          <td>
                            <span
                              className={`action-priority-badge action-priority-${action.priority.toLowerCase()}`}
                            >
                              {ACTION_ITEM_PRIORITY_LABELS[
                                action.priority
                              ]}
                            </span>
                          </td>
                          <td>
                            {action.responsibleUserFullName || '-'}
                          </td>
                          <td>
                            <strong>{action.overdueDays} gün</strong>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </article>
          </section>

          <section className="content-card table-card dashboard-risky-section">
            <div className="section-heading dashboard-table-heading">
              <div>
                <h2>Kritik İş Kalemleri</h2>
                <p>Riskli, blokeli veya gecikmiş aktif iş kalemleri</p>
              </div>

              <strong className="section-total">
                {riskyWorkItems.length} kayıt
              </strong>
            </div>

            {riskyWorkItems.length === 0 ? (
              <div className="dashboard-empty-state">
                Kritik durumda aktif iş kalemi bulunmuyor.
              </div>
            ) : (
              <div className="table-scroll">
                <table>
                  <thead>
                    <tr>
                      <th>İş Kalemi</th>
                      <th>Proje</th>
                      <th>Durum</th>
                      <th>Sorumlu</th>
                      <th>Rapor Haftası</th>
                    </tr>
                  </thead>

                  <tbody>
                    {riskyWorkItems.map((workItem) => (
                      <tr key={workItem.workItemId}>
                        <td>
                          <div className="table-primary-cell">
                            <strong>{workItem.title}</strong>
                            <span>
                              {workItem.description ||
                                'Açıklama bulunmuyor.'}
                            </span>
                          </div>
                        </td>
                        <td>
                          <div className="dashboard-project-table-cell">
                            <strong>{workItem.projectName}</strong>
                            <StatusBadge
                              status={workItem.projectStatus}
                            />
                          </div>
                        </td>
                        <td>
                          <StatusBadge status={workItem.status} />
                        </td>
                        <td>{workItem.responsiblePerson || '-'}</td>
                        <td>
                          {formatDate(workItem.reportWeekStartDate)}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </section>
        </>
      ) : null}
    </div>
  )
}
