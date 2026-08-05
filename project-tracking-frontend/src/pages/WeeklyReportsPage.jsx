import { useEffect, useState } from 'react'
import { analyzeWeeklyReport } from '../api/aiAnalysisApi.js'
import { getProjects } from '../api/projectApi.js'
import {
  createWeeklyReport,
  deactivateWeeklyReport,
  getWeeklyReports,
  updateWeeklyReport,
} from '../api/weeklyReportApi.js'
import { useAuth } from '../auth/AuthContext.jsx'
import ErrorState from '../components/ErrorState.jsx'
import LoadingState from '../components/LoadingState.jsx'
import StatusBadge from '../components/StatusBadge.jsx'
import { ROLES } from '../utils/roles.js'
import {
  WEEKLY_REPORT_STATUSES,
  WEEKLY_REPORT_STATUS_LABELS,
} from '../utils/statuses.js'

const INITIAL_FILTERS = {
  projectId: '',
  status: '',
  weekStartDate: '',
  weekEndDate: '',
}

function formatInputDate(date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')

  return `${year}-${month}-${day}`
}

function getCurrentWeek() {
  const today = new Date()
  const day = today.getDay()
  const differenceToMonday = day === 0 ? -6 : 1 - day

  const monday = new Date(today)
  monday.setDate(today.getDate() + differenceToMonday)

  const sunday = new Date(monday)
  sunday.setDate(monday.getDate() + 6)

  return {
    weekStartDate: formatInputDate(monday),
    weekEndDate: formatInputDate(sunday),
  }
}

function createInitialForm() {
  const currentWeek = getCurrentWeek()

  return {
    projectId: '',
    weekStartDate: currentWeek.weekStartDate,
    weekEndDate: currentWeek.weekEndDate,
    status: 'ON_TRACK',
    summary: '',
    completedWork: '',
    nextWeekPlan: '',
    risks: '',
  }
}

function formatDate(value) {
  if (!value) {
    return '-'
  }

  return new Intl.DateTimeFormat('tr-TR').format(
    new Date(`${value}T00:00:00`),
  )
}

function getErrorMessage(error) {
  const fieldErrors = error?.payload?.fieldErrors

  if (
    fieldErrors &&
    Object.keys(fieldErrors).length > 0
  ) {
    return Object.values(fieldErrors).join(' ')
  }

  return error?.message || 'İşlem tamamlanamadı.'
}

function normalizeOptionalText(value) {
  return value.trim() || null
}

export default function WeeklyReportsPage() {
  const { user } = useAuth()

  const [projects, setProjects] = useState([])
  const [reports, setReports] = useState([])
  const [filters, setFilters] = useState(INITIAL_FILTERS)

  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState('')
  const [reloadKey, setReloadKey] = useState(0)
  const [successMessage, setSuccessMessage] = useState('')

  const [activeModal, setActiveModal] = useState(null)
  const [selectedReport, setSelectedReport] = useState(null)
  const [form, setForm] = useState(createInitialForm)
  const [formError, setFormError] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [aiAnalysis, setAiAnalysis] = useState(null)
  const [aiError, setAiError] = useState('')
  const [isAnalyzing, setIsAnalyzing] = useState(false)
  const [aiApplyMessage, setAiApplyMessage] = useState('')

  const canManageReports =
    user?.role === ROLES.PROJECT_MANAGER ||
    user?.role === ROLES.ADMIN

  useEffect(() => {
    const controller = new AbortController()

    setIsLoading(true)
    setError('')

    Promise.all([
      getProjects(null, controller.signal),
      getWeeklyReports(filters, controller.signal),
    ])
      .then(([projectResponse, reportResponse]) => {
        setProjects(projectResponse)
        setReports(reportResponse)
      })
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
  }, [filters, reloadKey])

  useEffect(() => {
    if (!successMessage) {
      return undefined
    }

    const timeoutId = window.setTimeout(() => {
      setSuccessMessage('')
    }, 4000)

    return () => window.clearTimeout(timeoutId)
  }, [successMessage])

  function handleFilterChange(event) {
    const { name, value } = event.target

    setFilters((currentFilters) => ({
      ...currentFilters,
      [name]: value,
    }))
  }

  function clearFilters() {
    setFilters(INITIAL_FILTERS)
  }

  function handleFormChange(event) {
    const { name, value } = event.target

    setForm((currentForm) => ({
      ...currentForm,
      [name]: value,
    }))

    if (name === 'projectId') {
      resetAiAnalysis()
    }
  }

  function resetAiAnalysis() {
    setAiAnalysis(null)
    setAiError('')
    setAiApplyMessage('')
  }

  function resetModal() {
    setActiveModal(null)
    setSelectedReport(null)
    setForm(createInitialForm())
    setFormError('')
    resetAiAnalysis()
  }

  function closeModal() {
    if (isSubmitting || isAnalyzing) {
      return
    }

    resetModal()
  }

  function openCreateModal() {
    const initialForm = createInitialForm()

    setSelectedReport(null)
    setForm({
      ...initialForm,
      projectId:
        projects.length > 0
          ? String(projects[0].id)
          : '',
    })
    setFormError('')
    setSuccessMessage('')
    resetAiAnalysis()
    setActiveModal('create')
  }

  function openDetailModal(report) {
    setSelectedReport(report)
    setFormError('')
    setActiveModal('detail')
  }

  function openEditModal(report) {
    setSelectedReport(report)
    setForm({
      projectId: String(report.projectId),
      weekStartDate: report.weekStartDate || '',
      weekEndDate: report.weekEndDate || '',
      status: report.status || 'ON_TRACK',
      summary: report.summary || '',
      completedWork: report.completedWork || '',
      nextWeekPlan: report.nextWeekPlan || '',
      risks: report.risks || '',
    })
    setFormError('')
    setSuccessMessage('')
    resetAiAnalysis()
    setActiveModal('edit')
  }

  function openDeleteModal(report) {
    setSelectedReport(report)
    setFormError('')
    setSuccessMessage('')
    setActiveModal('delete')
  }

  function validateForm() {
    if (!form.projectId) {
      return 'Proje seçimi zorunludur.'
    }

    if (!form.weekStartDate) {
      return 'Hafta başlangıç tarihi zorunludur.'
    }

    if (!form.weekEndDate) {
      return 'Hafta bitiş tarihi zorunludur.'
    }

    if (form.weekEndDate < form.weekStartDate) {
      return 'Hafta bitiş tarihi başlangıç tarihinden önce olamaz.'
    }

    if (!form.summary.trim()) {
      return 'Haftalık rapor özeti zorunludur.'
    }

    return ''
  }

  function createRequestBody() {
    return {
      projectId: Number(form.projectId),
      weekStartDate: form.weekStartDate,
      weekEndDate: form.weekEndDate,
      status: form.status,
      summary: form.summary.trim(),
      completedWork: normalizeOptionalText(
        form.completedWork,
      ),
      nextWeekPlan: normalizeOptionalText(
        form.nextWeekPlan,
      ),
      risks: normalizeOptionalText(form.risks),
    }
  }

  function createAiRequestBody() {
    return {
      projectId: Number(form.projectId),
      currentStatus: form.status || null,
      summary: normalizeOptionalText(form.summary),
      completedWork: normalizeOptionalText(
        form.completedWork,
      ),
      nextWeekPlan: normalizeOptionalText(
        form.nextWeekPlan,
      ),
      risks: normalizeOptionalText(form.risks),
    }
  }

  function hasAiInput() {
    return [
      form.summary,
      form.completedWork,
      form.nextWeekPlan,
      form.risks,
    ].some((value) => value.trim())
  }

  function mergeSuggestionText(currentValue, suggestions) {
    const currentLines = currentValue
      .split('\n')
      .map((value) => value.trim())
      .filter(Boolean)

    const suggestionLines = suggestions
      .map((value) => value.trim())
      .filter(Boolean)
      .map((value) =>
        value.startsWith('- ') ? value : `- ${value}`,
      )

    return [...new Set([...currentLines, ...suggestionLines])]
      .join('\n')
      .slice(0, 2000)
  }

  async function handleAiAnalysis() {
    setAiError('')
    setAiApplyMessage('')

    if (!form.projectId) {
      setAiError('AI analizi için proje seçimi zorunludur.')
      return
    }

    if (!hasAiInput()) {
      setAiError(
        'Analiz için rapor alanlarından en az birini doldurun.',
      )
      return
    }

    setIsAnalyzing(true)

    try {
      const response = await analyzeWeeklyReport(
        createAiRequestBody(),
      )

      setAiAnalysis(response)
    } catch (requestError) {
      setAiError(getErrorMessage(requestError))
    } finally {
      setIsAnalyzing(false)
    }
  }

  function applySuggestedStatus() {
    if (!aiAnalysis?.suggestedStatus) {
      return
    }

    setForm((currentForm) => ({
      ...currentForm,
      status: aiAnalysis.suggestedStatus,
    }))
    setAiApplyMessage('Önerilen durum forma aktarıldı.')
  }

  function applyExecutiveSummary() {
    if (!aiAnalysis?.executiveSummary) {
      return
    }

    setForm((currentForm) => ({
      ...currentForm,
      summary: aiAnalysis.executiveSummary,
    }))
    setAiApplyMessage('Yönetici özeti forma aktarıldı.')
  }

  function applyDetectedRisks() {
    if (!aiAnalysis?.detectedRisks?.length) {
      return
    }

    setForm((currentForm) => ({
      ...currentForm,
      risks: mergeSuggestionText(
        currentForm.risks,
        aiAnalysis.detectedRisks,
      ),
    }))
    setAiApplyMessage('Risk önerileri forma aktarıldı.')
  }

  function applySuggestedActions() {
    if (!aiAnalysis?.suggestedActions?.length) {
      return
    }

    setForm((currentForm) => ({
      ...currentForm,
      nextWeekPlan: mergeSuggestionText(
        currentForm.nextWeekPlan,
        aiAnalysis.suggestedActions,
      ),
    }))
    setAiApplyMessage(
      'Aksiyon önerileri gelecek hafta planına aktarıldı.',
    )
  }

  function applyAllAiSuggestions() {
    if (!aiAnalysis) {
      return
    }

    setForm((currentForm) => ({
      ...currentForm,
      status:
        aiAnalysis.suggestedStatus ||
        currentForm.status,
      summary:
        aiAnalysis.executiveSummary ||
        currentForm.summary,
      risks: mergeSuggestionText(
        currentForm.risks,
        aiAnalysis.detectedRisks || [],
      ),
      nextWeekPlan: mergeSuggestionText(
        currentForm.nextWeekPlan,
        aiAnalysis.suggestedActions || [],
      ),
    }))
    setAiApplyMessage(
      'AI asistanı önerilerinin tamamı forma aktarıldı.',
    )
  }

  async function handleCreateReport(event) {
    event.preventDefault()
    setFormError('')

    const validationError = validateForm()

    if (validationError) {
      setFormError(validationError)
      return
    }

    setIsSubmitting(true)

    try {
      await createWeeklyReport(createRequestBody())

      resetModal()
      setSuccessMessage(
        'Haftalık rapor başarıyla oluşturuldu.',
      )
      setReloadKey((value) => value + 1)
    } catch (requestError) {
      setFormError(getErrorMessage(requestError))
    } finally {
      setIsSubmitting(false)
    }
  }

  async function handleUpdateReport(event) {
    event.preventDefault()
    setFormError('')

    const validationError = validateForm()

    if (validationError) {
      setFormError(validationError)
      return
    }

    if (!selectedReport) {
      setFormError('Güncellenecek rapor bulunamadı.')
      return
    }

    setIsSubmitting(true)

    try {
      await updateWeeklyReport(
        selectedReport.id,
        createRequestBody(),
      )

      resetModal()
      setSuccessMessage(
        'Haftalık rapor başarıyla güncellendi.',
      )
      setReloadKey((value) => value + 1)
    } catch (requestError) {
      setFormError(getErrorMessage(requestError))
    } finally {
      setIsSubmitting(false)
    }
  }

  async function handleDeleteReport() {
    if (!selectedReport) {
      setFormError('Silinecek rapor bulunamadı.')
      return
    }

    setFormError('')
    setIsSubmitting(true)

    try {
      await deactivateWeeklyReport(selectedReport.id)

      resetModal()
      setSuccessMessage(
        'Haftalık rapor başarıyla silindi.',
      )
      setReloadKey((value) => value + 1)
    } catch (requestError) {
      setFormError(getErrorMessage(requestError))
    } finally {
      setIsSubmitting(false)
    }
  }

  function renderReportForm({
    title,
    description,
    submitLabel,
    onSubmit,
    isEdit = false,
  }) {
    return (
      <section
        className="modal-card report-modal-card"
        role="dialog"
        aria-modal="true"
        aria-labelledby="report-form-title"
        onMouseDown={(event) => event.stopPropagation()}
      >
        <div className="modal-heading">
          <div>
            <span className="eyebrow">
              RAPOR YÖNETİMİ
            </span>

            <h2 id="report-form-title">{title}</h2>

            <p>{description}</p>
          </div>

          <button
            type="button"
            className="modal-close-button"
            onClick={closeModal}
            disabled={isSubmitting || isAnalyzing}
            aria-label="Pencereyi kapat"
          >
            ×
          </button>
        </div>

        <form
          className="report-form"
          onSubmit={onSubmit}
        >
          <label className="field">
            <span>Proje</span>

            <select
              name="projectId"
              value={form.projectId}
              onChange={handleFormChange}
              disabled={isEdit}
              required
            >
              <option value="">Proje seçin</option>

              {projects.map((project) => (
                <option
                  key={project.id}
                  value={project.id}
                >
                  {project.name}
                </option>
              ))}
            </select>
          </label>

          {isEdit ? (
            <p className="report-project-note">
              Raporun bağlı olduğu proje değiştirilemez.
            </p>
          ) : null}

          <div className="form-grid">
            <label className="field">
              <span>Hafta başlangıcı</span>

              <input
                type="date"
                name="weekStartDate"
                value={form.weekStartDate}
                onChange={handleFormChange}
                required
              />
            </label>

            <label className="field">
              <span>Hafta bitişi</span>

              <input
                type="date"
                name="weekEndDate"
                value={form.weekEndDate}
                onChange={handleFormChange}
                min={form.weekStartDate || undefined}
                required
              />
            </label>
          </div>

          <label className="field">
            <span>Rapor durumu</span>

            <select
              name="status"
              value={form.status}
              onChange={handleFormChange}
              required
            >
              {WEEKLY_REPORT_STATUSES.map(
                (reportStatus) => (
                  <option
                    key={reportStatus}
                    value={reportStatus}
                  >
                    {
                      WEEKLY_REPORT_STATUS_LABELS[
                        reportStatus
                      ]
                    }
                  </option>
                ),
              )}
            </select>
          </label>

          <label className="field">
            <span>Haftalık özet</span>

            <textarea
              name="summary"
              value={form.summary}
              onChange={handleFormChange}
              maxLength={2000}
              rows={4}
              placeholder="Haftalık gelişmeleri özetleyin"
              required
            />
          </label>

          <label className="field">
            <span>Tamamlanan işler</span>

            <textarea
              name="completedWork"
              value={form.completedWork}
              onChange={handleFormChange}
              maxLength={2000}
              rows={3}
              placeholder="Bu hafta tamamlanan işleri yazın"
            />
          </label>

          <label className="field">
            <span>Gelecek hafta planı</span>

            <textarea
              name="nextWeekPlan"
              value={form.nextWeekPlan}
              onChange={handleFormChange}
              maxLength={2000}
              rows={3}
              placeholder="Gelecek hafta yapılacak işleri yazın"
            />
          </label>

          <label className="field">
            <span>Riskler</span>

            <textarea
              name="risks"
              value={form.risks}
              onChange={handleFormChange}
              maxLength={2000}
              rows={3}
              placeholder="Varsa riskleri ve engelleri yazın"
            />
          </label>

          <section className="ai-assistant-panel">
            <div className="ai-assistant-heading">
              <div>
                <span className="ai-assistant-eyebrow">
                  T18 PROTOTİP
                </span>

                <h3>AI Haftalık Rapor Asistanı</h3>

                <p>
                  Formdaki metinleri ve projedeki iş,
                  risk ve aksiyon kayıtlarını analiz eder.
                </p>
              </div>

              <button
                type="button"
                className="button button-secondary"
                onClick={handleAiAnalysis}
                disabled={isAnalyzing || isSubmitting}
              >
                {isAnalyzing
                  ? 'Analiz Ediliyor...'
                  : 'AI ile Analiz Et'}
              </button>
            </div>

            <p className="ai-assistant-disclaimer">
              Bu sürüm harici bir yapay zekâ servisi
              kullanmayan, kural tabanlı bir demo
              sağlayıcısıdır.
            </p>

            {aiError ? (
              <div className="ai-assistant-error">
                {aiError}
              </div>
            ) : null}

            {aiAnalysis ? (
              <div className="ai-analysis-result">
                <div className="ai-analysis-status-row">
                  <div>
                    <span>Önerilen durum</span>
                    <StatusBadge
                      status={aiAnalysis.suggestedStatus}
                    />
                  </div>

                  <button
                    type="button"
                    className="ai-apply-button"
                    onClick={applySuggestedStatus}
                  >
                    Durumu Uygula
                  </button>
                </div>

                <div className="ai-indicator-list">
                  {aiAnalysis.indicators?.map(
                    (indicator) => (
                      <span key={indicator}>
                        {indicator}
                      </span>
                    ),
                  )}
                </div>

                <article className="ai-suggestion-card">
                  <div className="ai-suggestion-heading">
                    <h4>Yönetici özeti</h4>

                    <button
                      type="button"
                      className="ai-apply-button"
                      onClick={applyExecutiveSummary}
                    >
                      Özeti Uygula
                    </button>
                  </div>

                  <p>{aiAnalysis.executiveSummary}</p>
                </article>

                <div className="ai-suggestion-grid">
                  <article className="ai-suggestion-card">
                    <div className="ai-suggestion-heading">
                      <h4>Tespit edilen riskler</h4>

                      <button
                        type="button"
                        className="ai-apply-button"
                        onClick={applyDetectedRisks}
                      >
                        Risklere Ekle
                      </button>
                    </div>

                    <ul>
                      {aiAnalysis.detectedRisks?.map(
                        (risk) => (
                          <li key={risk}>{risk}</li>
                        ),
                      )}
                    </ul>
                  </article>

                  <article className="ai-suggestion-card">
                    <div className="ai-suggestion-heading">
                      <h4>Önerilen aksiyonlar</h4>

                      <button
                        type="button"
                        className="ai-apply-button"
                        onClick={applySuggestedActions}
                      >
                        Plana Ekle
                      </button>
                    </div>

                    <ul>
                      {aiAnalysis.suggestedActions?.map(
                        (action) => (
                          <li key={action}>{action}</li>
                        ),
                      )}
                    </ul>
                  </article>
                </div>

                {aiApplyMessage ? (
                  <div className="ai-apply-message">
                    {aiApplyMessage}
                  </div>
                ) : null}

                <div className="ai-analysis-footer">
                  <span>
                    Sağlayıcı: {aiAnalysis.provider}
                  </span>

                  <button
                    type="button"
                    className="button button-primary"
                    onClick={applyAllAiSuggestions}
                  >
                    Tüm Önerileri Uygula
                  </button>
                </div>
              </div>
            ) : null}
          </section>

          {formError ? (
            <div className="form-error">
              {formError}
            </div>
          ) : null}

          <div className="modal-actions">
            <button
              type="button"
              className="button button-secondary"
              onClick={closeModal}
              disabled={isSubmitting}
            >
              Vazgeç
            </button>

            <button
              type="submit"
              className="button button-primary"
              disabled={isSubmitting}
            >
              {isSubmitting
                ? 'Kaydediliyor...'
                : submitLabel}
            </button>
          </div>
        </form>
      </section>
    )
  }

  return (
    <div className="page-stack">
      <section className="page-heading">
        <div>
          <span className="eyebrow">
            RAPOR YÖNETİMİ
          </span>

          <h1>Haftalık Raporlar</h1>

          <p>
            Projelerin haftalık durumlarını,
            tamamlanan işlerini ve risklerini takip edin.
          </p>
        </div>

        <div className="page-heading-actions">
          {canManageReports ? (
            <button
              type="button"
              className="button button-primary"
              onClick={openCreateModal}
              disabled={projects.length === 0}
            >
              + Yeni Rapor
            </button>
          ) : null}
        </div>
      </section>

      {successMessage ? (
        <div className="page-success-message">
          {successMessage}
        </div>
      ) : null}

      {isLoading ? (
        <LoadingState message="Haftalık raporlar yükleniyor..." />
      ) : null}

      {error ? (
        <ErrorState
          message={error}
          onRetry={() =>
            setReloadKey((value) => value + 1)
          }
        />
      ) : null}

      {!isLoading && !error ? (
        <section className="content-card table-card">
          <div className="report-filter-toolbar">
            <div className="report-filter-grid">
              <label className="report-filter-field">
                <span>Proje</span>

                <select
                  name="projectId"
                  value={filters.projectId}
                  onChange={handleFilterChange}
                >
                  <option value="">Tüm Projeler</option>

                  {projects.map((project) => (
                    <option
                      key={project.id}
                      value={project.id}
                    >
                      {project.name}
                    </option>
                  ))}
                </select>
              </label>

              <label className="report-filter-field">
                <span>Durum</span>

                <select
                  name="status"
                  value={filters.status}
                  onChange={handleFilterChange}
                >
                  <option value="">Tüm Durumlar</option>

                  {WEEKLY_REPORT_STATUSES.map(
                    (reportStatus) => (
                      <option
                        key={reportStatus}
                        value={reportStatus}
                      >
                        {
                          WEEKLY_REPORT_STATUS_LABELS[
                            reportStatus
                          ]
                        }
                      </option>
                    ),
                  )}
                </select>
              </label>

              <label className="report-filter-field">
                <span>Başlangıç</span>

                <input
                  type="date"
                  name="weekStartDate"
                  value={filters.weekStartDate}
                  onChange={handleFilterChange}
                />
              </label>

              <label className="report-filter-field">
                <span>Bitiş</span>

                <input
                  type="date"
                  name="weekEndDate"
                  value={filters.weekEndDate}
                  onChange={handleFilterChange}
                  min={filters.weekStartDate || undefined}
                />
              </label>
            </div>

            <button
              type="button"
              className="button button-secondary report-filter-clear"
              onClick={clearFilters}
            >
              Temizle
            </button>
          </div>

          {reports.length === 0 ? (
            <div className="empty-state">
              Seçilen filtrelere uygun haftalık rapor
              bulunamadı.
            </div>
          ) : (
            <div className="table-scroll">
              <table>
                <thead>
                  <tr>
                    <th>Proje</th>
                    <th>Hafta</th>
                    <th>Durum</th>
                    <th>Özet</th>
                    <th>Risk</th>
                    <th>İşlemler</th>
                  </tr>
                </thead>

                <tbody>
                  {reports.map((report) => (
                    <tr key={report.id}>
                      <td>
                        <div className="table-primary-cell">
                          <strong>
                            {report.projectName}
                          </strong>
                        </div>
                      </td>

                      <td>
                        <div className="report-week-cell">
                          <strong>
                            {formatDate(
                              report.weekStartDate,
                            )}
                          </strong>

                          <span>
                            {formatDate(
                              report.weekEndDate,
                            )}
                          </span>
                        </div>
                      </td>

                      <td>
                        <StatusBadge
                          status={report.status}
                        />
                      </td>

                      <td>
                        <p className="report-table-summary">
                          {report.summary}
                        </p>
                      </td>

                      <td>
                        {report.risks ? (
                          <span className="report-risk-text">
                            {report.risks}
                          </span>
                        ) : (
                          <span className="report-no-risk">
                            Risk girilmemiş
                          </span>
                        )}
                      </td>

                      <td>
                        <div className="table-actions">
                          <button
                            type="button"
                            className="table-action-button"
                            onClick={() =>
                              openDetailModal(report)
                            }
                          >
                            Detay
                          </button>

                          {canManageReports ? (
                            <>
                              <button
                                type="button"
                                className="table-action-button"
                                onClick={() =>
                                  openEditModal(report)
                                }
                              >
                                Düzenle
                              </button>

                              <button
                                type="button"
                                className="table-action-button table-action-danger"
                                onClick={() =>
                                  openDeleteModal(report)
                                }
                              >
                                Sil
                              </button>
                            </>
                          ) : null}
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </section>
      ) : null}

      {activeModal === 'create' ? (
        <div
          className="modal-backdrop"
          role="presentation"
          onMouseDown={closeModal}
        >
          {renderReportForm({
            title: 'Yeni Haftalık Rapor',
            description:
              'Projenin haftalık ilerleme ve durum bilgilerini girin.',
            submitLabel: 'Raporu Oluştur',
            onSubmit: handleCreateReport,
          })}
        </div>
      ) : null}

      {activeModal === 'edit' ? (
        <div
          className="modal-backdrop"
          role="presentation"
          onMouseDown={closeModal}
        >
          {renderReportForm({
            title: 'Haftalık Raporu Düzenle',
            description:
              'Raporun mevcut bilgilerini güncelleyin.',
            submitLabel: 'Değişiklikleri Kaydet',
            onSubmit: handleUpdateReport,
            isEdit: true,
          })}
        </div>
      ) : null}

      {activeModal === 'detail' &&
      selectedReport ? (
        <div
          className="modal-backdrop"
          role="presentation"
          onMouseDown={closeModal}
        >
          <section
            className="modal-card report-modal-card"
            role="dialog"
            aria-modal="true"
            aria-labelledby="report-detail-title"
            onMouseDown={(event) =>
              event.stopPropagation()
            }
          >
            <div className="modal-heading">
              <div>
                <span className="eyebrow">
                  RAPOR DETAYI
                </span>

                <h2 id="report-detail-title">
                  {selectedReport.projectName}
                </h2>

                <p>
                  Haftalık rapora ait tüm bilgiler.
                </p>
              </div>

              <button
                type="button"
                className="modal-close-button"
                onClick={closeModal}
                aria-label="Pencereyi kapat"
              >
                ×
              </button>
            </div>

            <div className="report-detail-grid">
              <div className="report-detail-item">
                <span>Proje</span>
                <strong>
                  {selectedReport.projectName}
                </strong>
              </div>

              <div className="report-detail-item">
                <span>Durum</span>
                <StatusBadge
                  status={selectedReport.status}
                />
              </div>

              <div className="report-detail-item">
                <span>Hafta başlangıcı</span>
                <strong>
                  {formatDate(
                    selectedReport.weekStartDate,
                  )}
                </strong>
              </div>

              <div className="report-detail-item">
                <span>Hafta bitişi</span>
                <strong>
                  {formatDate(
                    selectedReport.weekEndDate,
                  )}
                </strong>
              </div>

              <div className="report-detail-item report-detail-item-wide">
                <span>Haftalık özet</span>
                <p>
                  {selectedReport.summary ||
                    'Bilgi girilmemiş.'}
                </p>
              </div>

              <div className="report-detail-item report-detail-item-wide">
                <span>Tamamlanan işler</span>
                <p>
                  {selectedReport.completedWork ||
                    'Bilgi girilmemiş.'}
                </p>
              </div>

              <div className="report-detail-item report-detail-item-wide">
                <span>Gelecek hafta planı</span>
                <p>
                  {selectedReport.nextWeekPlan ||
                    'Bilgi girilmemiş.'}
                </p>
              </div>

              <div className="report-detail-item report-detail-item-wide report-detail-risk">
                <span>Riskler</span>
                <p>
                  {selectedReport.risks ||
                    'Risk girilmemiş.'}
                </p>
              </div>
            </div>

            <div className="modal-actions">
              {canManageReports ? (
                <button
                  type="button"
                  className="button button-primary"
                  onClick={() =>
                    openEditModal(selectedReport)
                  }
                >
                  Düzenle
                </button>
              ) : null}

              <button
                type="button"
                className="button button-secondary"
                onClick={closeModal}
              >
                Kapat
              </button>
            </div>
          </section>
        </div>
      ) : null}

      {activeModal === 'delete' &&
      selectedReport ? (
        <div
          className="modal-backdrop"
          role="presentation"
          onMouseDown={closeModal}
        >
          <section
            className="modal-card delete-modal-card"
            role="dialog"
            aria-modal="true"
            aria-labelledby="delete-report-title"
            onMouseDown={(event) =>
              event.stopPropagation()
            }
          >
            <div className="modal-heading">
              <div>
                <span className="eyebrow danger-eyebrow">
                  RAPORU SİL
                </span>

                <h2 id="delete-report-title">
                  Haftalık rapor silinsin mi?
                </h2>

                <p>
                  <strong>
                    {selectedReport.projectName}
                  </strong>{' '}
                  projesinin{' '}
                  <strong>
                    {formatDate(
                      selectedReport.weekStartDate,
                    )}
                  </strong>{' '}
                  tarihli raporu aktif listeden
                  kaldırılacaktır.
                </p>
              </div>

              <button
                type="button"
                className="modal-close-button"
                onClick={closeModal}
                disabled={isSubmitting}
                aria-label="Pencereyi kapat"
              >
                ×
              </button>
            </div>

            {formError ? (
              <div className="form-error">
                {formError}
              </div>
            ) : null}

            <div className="modal-actions">
              <button
                type="button"
                className="button button-secondary"
                onClick={closeModal}
                disabled={isSubmitting}
              >
                Vazgeç
              </button>

              <button
                type="button"
                className="button button-danger"
                onClick={handleDeleteReport}
                disabled={isSubmitting}
              >
                {isSubmitting
                  ? 'Siliniyor...'
                  : 'Raporu Sil'}
              </button>
            </div>
          </section>
        </div>
      ) : null}
    </div>
  )
}