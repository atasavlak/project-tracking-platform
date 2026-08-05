import { useEffect, useMemo, useState } from 'react'
import { getProjects } from '../api/projectApi.js'
import {
  createRiskIssue,
  deactivateRiskIssue,
  getRiskIssues,
  updateRiskIssue,
} from '../api/riskIssueApi.js'
import { getAssignableUsers } from '../api/userApi.js'
import { getWeeklyReports } from '../api/weeklyReportApi.js'
import { useAuth } from '../auth/AuthContext.jsx'
import ErrorState from '../components/ErrorState.jsx'
import LoadingState from '../components/LoadingState.jsx'
import StatusBadge from '../components/StatusBadge.jsx'
import { ROLES } from '../utils/roles.js'
import {
  RISK_ISSUE_SEVERITIES,
  RISK_ISSUE_SEVERITY_LABELS,
  RISK_ISSUE_STATUSES,
  RISK_ISSUE_STATUS_LABELS,
  RISK_ISSUE_TYPES,
  RISK_ISSUE_TYPE_LABELS,
} from '../utils/statuses.js'

const INITIAL_FILTERS = {
  projectId: '',
  weeklyReportId: '',
  type: '',
  severity: '',
  status: '',
  responsibleUserId: '',
  followUpDateFrom: '',
  followUpDateTo: '',
}

function formatInputDate(date) {
  const year = date.getFullYear()
  const month = String(
    date.getMonth() + 1,
  ).padStart(2, '0')
  const day = String(date.getDate()).padStart(
    2,
    '0',
  )

  return `${year}-${month}-${day}`
}

function getDefaultFollowUpDate() {
  const date = new Date()
  date.setDate(date.getDate() + 7)

  return formatInputDate(date)
}

function createInitialForm() {
  return {
    projectId: '',
    weeklyReportId: '',
    responsibleUserId: '',
    type: 'RISK',
    title: '',
    description: '',
    severity: 'MEDIUM',
    status: 'OPEN',
    followUpDate: getDefaultFollowUpDate(),
    resolutionNote: '',
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

function normalizeUserSearch(value) {
  return value
    .trim()
    .toLocaleLowerCase('tr-TR')
}

function getAssignableUserLabel(assignableUser) {
  if (!assignableUser) {
    return ''
  }

  const fullName =
    assignableUser.fullName ||
    assignableUser.username ||
    'Kullanıcı'

  return assignableUser.username
    ? `${fullName} (${assignableUser.username})`
    : fullName
}

export default function RiskIssuesPage() {
  const { user } = useAuth()

  const [projects, setProjects] = useState([])
  const [reports, setReports] = useState([])
  const [assignableUsers, setAssignableUsers] =
    useState([])
  const [riskIssues, setRiskIssues] = useState([])

  const [filterDraft, setFilterDraft] =
    useState(INITIAL_FILTERS)
  const [filters, setFilters] =
    useState(INITIAL_FILTERS)

  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState('')
  const [reloadKey, setReloadKey] = useState(0)
  const [successMessage, setSuccessMessage] =
    useState('')

  const [activeModal, setActiveModal] =
    useState(null)
  const [selectedRiskIssue, setSelectedRiskIssue] =
    useState(null)
  const [form, setForm] = useState(
    createInitialForm,
  )
  const [formError, setFormError] = useState('')
  const [
    responsibleUserSearch,
    setResponsibleUserSearch,
  ] = useState('')
  const [isSubmitting, setIsSubmitting] =
    useState(false)

  const canCreateOrDelete =
    user?.role === ROLES.PROJECT_MANAGER ||
    user?.role === ROLES.ADMIN

  const canUpdate =
    user?.role === ROLES.PROJECT_MANAGER ||
    user?.role === ROLES.TEAM_LEAD ||
    user?.role === ROLES.ADMIN

  const filteredReportOptions = useMemo(() => {
    if (!filterDraft.projectId) {
      return reports
    }

    return reports.filter(
      (report) =>
        Number(report.projectId) ===
        Number(filterDraft.projectId),
    )
  }, [filterDraft.projectId, reports])

  const formReportOptions = useMemo(() => {
    if (!form.projectId) {
      return reports
    }

    return reports.filter(
      (report) =>
        Number(report.projectId) ===
        Number(form.projectId),
    )
  }, [form.projectId, reports])

  useEffect(() => {
    const controller = new AbortController()

    setIsLoading(true)
    setError('')

    Promise.all([
      getProjects(null, controller.signal),
      getWeeklyReports({}, controller.signal),
      getAssignableUsers(controller.signal),
      getRiskIssues(filters, controller.signal),
    ])
      .then(
        ([
          projectResponse,
          reportResponse,
          userResponse,
          riskIssueResponse,
        ]) => {
          setProjects(projectResponse)
          setReports(reportResponse)
          setAssignableUsers(userResponse)
          setRiskIssues(riskIssueResponse)
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

  function getReportById(weeklyReportId) {
    return reports.find(
      (report) =>
        Number(report.id) ===
        Number(weeklyReportId),
    )
  }

  function getReportLabel(weeklyReportId) {
    const report = getReportById(weeklyReportId)

    if (!report) {
      return `Rapor #${weeklyReportId}`
    }

    return `${formatDate(
      report.weekStartDate,
    )} - ${formatDate(report.weekEndDate)}`
  }

  function handleFilterChange(event) {
    const { name, value } = event.target

    setFilterDraft((currentFilters) => {
      const updatedFilters = {
        ...currentFilters,
        [name]: value,
      }

      if (name === 'projectId') {
        updatedFilters.weeklyReportId = ''
      }

      return updatedFilters
    })
  }

  function applyFilters(event) {
    event.preventDefault()
    setFilters({ ...filterDraft })
  }

  function clearFilters() {
    setFilterDraft(INITIAL_FILTERS)
    setFilters(INITIAL_FILTERS)
  }

  function handleFormChange(event) {
    const { name, value } = event.target

    setForm((currentForm) => {
      const updatedForm = {
        ...currentForm,
        [name]: value,
      }

      if (name === 'projectId') {
        const projectReports = reports.filter(
          (report) =>
            Number(report.projectId) ===
            Number(value),
        )

        updatedForm.weeklyReportId =
          projectReports.length > 0
            ? String(projectReports[0].id)
            : ''
      }

      return updatedForm
    })
  }

  function handleResponsibleUserSearchChange(
    event,
  ) {
    const value = event.target.value
    const normalizedValue =
      normalizeUserSearch(value)

    const selectedUser = assignableUsers.find(
      (assignableUser) => {
        const searchableValues = [
          getAssignableUserLabel(assignableUser),
          assignableUser.fullName,
          assignableUser.username,
          assignableUser.email,
        ]

        return searchableValues.some(
          (searchableValue) =>
            searchableValue &&
            normalizeUserSearch(searchableValue) ===
              normalizedValue,
        )
      },
    )

    setResponsibleUserSearch(value)

    setForm((currentForm) => ({
      ...currentForm,
      responsibleUserId: selectedUser
        ? String(selectedUser.id)
        : '',
    }))
  }

  function resetModal() {
    setActiveModal(null)
    setSelectedRiskIssue(null)
    setForm(createInitialForm())
    setResponsibleUserSearch('')
    setFormError('')
  }

  function closeModal() {
    if (isSubmitting) {
      return
    }

    resetModal()
  }

  function openCreateModal() {
    const firstProject = projects[0]

    const projectReports = firstProject
      ? reports.filter(
          (report) =>
            Number(report.projectId) ===
            Number(firstProject.id),
        )
      : []

    setSelectedRiskIssue(null)
    setResponsibleUserSearch('')
    setForm({
      ...createInitialForm(),
      projectId: firstProject
        ? String(firstProject.id)
        : '',
      weeklyReportId:
        projectReports.length > 0
          ? String(projectReports[0].id)
          : '',
    })
    setFormError('')
    setSuccessMessage('')
    setActiveModal('create')
  }

  function openDetailModal(riskIssue) {
    setSelectedRiskIssue(riskIssue)
    setFormError('')
    setActiveModal('detail')
  }

  function openEditModal(riskIssue) {
    const selectedAssignableUser =
      assignableUsers.find(
        (assignableUser) =>
          Number(assignableUser.id) ===
          Number(riskIssue.responsibleUserId),
      )

    setSelectedRiskIssue(riskIssue)
    setResponsibleUserSearch(
      selectedAssignableUser
        ? getAssignableUserLabel(
            selectedAssignableUser,
          )
        : riskIssue.responsibleFullName || '',
    )
    setForm({
      projectId: String(riskIssue.projectId),
      weeklyReportId: String(
        riskIssue.weeklyReportId,
      ),
      responsibleUserId: riskIssue.responsibleUserId
        ? String(riskIssue.responsibleUserId)
        : '',
      type: riskIssue.type || 'RISK',
      title: riskIssue.title || '',
      description: riskIssue.description || '',
      severity: riskIssue.severity || 'MEDIUM',
      status: riskIssue.status || 'OPEN',
      followUpDate: riskIssue.followUpDate || '',
      resolutionNote:
        riskIssue.resolutionNote || '',
    })
    setFormError('')
    setSuccessMessage('')
    setActiveModal('edit')
  }

  function openDeleteModal(riskIssue) {
    setSelectedRiskIssue(riskIssue)
    setFormError('')
    setSuccessMessage('')
    setActiveModal('delete')
  }

  function validateForm(isEdit) {
    if (!isEdit && !form.weeklyReportId) {
      return 'Haftalık rapor seçimi zorunludur.'
    }

    if (!form.title.trim()) {
      return 'Risk veya engel başlığı zorunludur.'
    }

    if (!form.description.trim()) {
      return 'Risk veya engel açıklaması zorunludur.'
    }

    if (!form.followUpDate) {
      return 'Takip tarihi zorunludur.'
    }

    if (
      responsibleUserSearch.trim() &&
      !form.responsibleUserId
    ) {
      return 'Lütfen sorumlu kullanıcıyı listeden seçin.'
    }

    return ''
  }

  function createRequestBody() {
    return {
      weeklyReportId: Number(form.weeklyReportId),
      responsibleUserId: form.responsibleUserId
        ? Number(form.responsibleUserId)
        : null,
      type: form.type,
      title: form.title.trim(),
      description: form.description.trim(),
      severity: form.severity,
      status: form.status,
      followUpDate: form.followUpDate,
      resolutionNote: normalizeOptionalText(
        form.resolutionNote,
      ),
    }
  }

  function createUpdateRequestBody() {
    return {
      responsibleUserId: form.responsibleUserId
        ? Number(form.responsibleUserId)
        : null,
      type: form.type,
      title: form.title.trim(),
      description: form.description.trim(),
      severity: form.severity,
      status: form.status,
      followUpDate: form.followUpDate,
      resolutionNote: normalizeOptionalText(
        form.resolutionNote,
      ),
    }
  }

  async function handleCreateRiskIssue(event) {
    event.preventDefault()
    setFormError('')

    const validationError = validateForm(false)

    if (validationError) {
      setFormError(validationError)
      return
    }

    setIsSubmitting(true)

    try {
      await createRiskIssue(createRequestBody())

      resetModal()
      setSuccessMessage(
        'Risk veya engel kaydı başarıyla oluşturuldu.',
      )
      setReloadKey((value) => value + 1)
    } catch (requestError) {
      setFormError(getErrorMessage(requestError))
    } finally {
      setIsSubmitting(false)
    }
  }

  async function handleUpdateRiskIssue(event) {
    event.preventDefault()
    setFormError('')

    const validationError = validateForm(true)

    if (validationError) {
      setFormError(validationError)
      return
    }

    if (!selectedRiskIssue) {
      setFormError(
        'Güncellenecek kayıt bulunamadı.',
      )
      return
    }

    setIsSubmitting(true)

    try {
      await updateRiskIssue(
        selectedRiskIssue.id,
        createUpdateRequestBody(),
      )

      resetModal()
      setSuccessMessage(
        'Risk veya engel kaydı başarıyla güncellendi.',
      )
      setReloadKey((value) => value + 1)
    } catch (requestError) {
      setFormError(getErrorMessage(requestError))
    } finally {
      setIsSubmitting(false)
    }
  }

  async function handleDeleteRiskIssue() {
    if (!selectedRiskIssue) {
      setFormError('Silinecek kayıt bulunamadı.')
      return
    }

    setFormError('')
    setIsSubmitting(true)

    try {
      await deactivateRiskIssue(
        selectedRiskIssue.id,
      )

      resetModal()
      setSuccessMessage(
        'Risk veya engel kaydı başarıyla silindi.',
      )
      setReloadKey((value) => value + 1)
    } catch (requestError) {
      setFormError(getErrorMessage(requestError))
    } finally {
      setIsSubmitting(false)
    }
  }

  function renderRiskIssueForm({
    title,
    description,
    submitLabel,
    onSubmit,
    isEdit = false,
  }) {
    return (
      <section
        className="modal-card risk-modal-card"
        role="dialog"
        aria-modal="true"
        aria-labelledby="risk-form-title"
        onMouseDown={(event) =>
          event.stopPropagation()
        }
      >
        <div className="modal-heading">
          <div>
            <span className="eyebrow">
              RİSK VE ENGEL YÖNETİMİ
            </span>

            <h2 id="risk-form-title">{title}</h2>

            <p>{description}</p>
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

        <form
          className="risk-form"
          onSubmit={onSubmit}
        >
          <div className="form-grid">
            <label className="field">
              <span>Proje</span>

              <select
                name="projectId"
                value={form.projectId}
                onChange={handleFormChange}
                disabled={isEdit}
                required
              >
                <option value="">
                  Proje seçin
                </option>

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

            <label className="field">
              <span>Haftalık rapor</span>

              <select
                name="weeklyReportId"
                value={form.weeklyReportId}
                onChange={handleFormChange}
                disabled={isEdit}
                required
              >
                <option value="">
                  Rapor seçin
                </option>

                {formReportOptions.map((report) => (
                  <option
                    key={report.id}
                    value={report.id}
                  >
                    {formatDate(report.weekStartDate)}
                    {' - '}
                    {formatDate(report.weekEndDate)}
                  </option>
                ))}
              </select>
            </label>
          </div>

          {isEdit ? (
            <p className="report-project-note">
              Kaydın bağlı olduğu proje ve haftalık
              rapor değiştirilemez.
            </p>
          ) : null}

          <div className="form-grid">
            <label className="field">
              <span>Kayıt tipi</span>

              <select
                name="type"
                value={form.type}
                onChange={handleFormChange}
                required
              >
                {RISK_ISSUE_TYPES.map(
                  (riskIssueType) => (
                    <option
                      key={riskIssueType}
                      value={riskIssueType}
                    >
                      {
                        RISK_ISSUE_TYPE_LABELS[
                          riskIssueType
                        ]
                      }
                    </option>
                  ),
                )}
              </select>
            </label>

            <label className="field">
              <span>Önem seviyesi</span>

              <select
                name="severity"
                value={form.severity}
                onChange={handleFormChange}
                required
              >
                {RISK_ISSUE_SEVERITIES.map(
                  (severity) => (
                    <option
                      key={severity}
                      value={severity}
                    >
                      {
                        RISK_ISSUE_SEVERITY_LABELS[
                          severity
                        ]
                      }
                    </option>
                  ),
                )}
              </select>
            </label>
          </div>

          <label className="field">
            <span>Başlık</span>

            <input
              type="text"
              name="title"
              value={form.title}
              onChange={handleFormChange}
              maxLength={200}
              placeholder="Risk veya engel başlığını girin"
              required
            />
          </label>

          <label className="field">
            <span>Açıklama</span>

            <textarea
              name="description"
              value={form.description}
              onChange={handleFormChange}
              maxLength={2000}
              rows={4}
              placeholder="Risk veya engeli açıklayın"
              required
            />
          </label>

          <div className="form-grid">
            <label className="field">
              <span>Durum</span>

              <select
                name="status"
                value={form.status}
                onChange={handleFormChange}
                required
              >
                {RISK_ISSUE_STATUSES.map(
                  (riskIssueStatus) => (
                    <option
                      key={riskIssueStatus}
                      value={riskIssueStatus}
                    >
                      {
                        RISK_ISSUE_STATUS_LABELS[
                          riskIssueStatus
                        ]
                      }
                    </option>
                  ),
                )}
              </select>
            </label>

            <label className="field">
              <span>Takip tarihi</span>

              <input
                type="date"
                name="followUpDate"
                value={form.followUpDate}
                onChange={handleFormChange}
                required
              />
            </label>
          </div>

          <label className="field">
            <span>Sorumlu kullanıcı</span>

            <input
              type="text"
              list="assignable-user-options"
              value={responsibleUserSearch}
              onChange={
                handleResponsibleUserSearchChange
              }
              placeholder="İsim veya kullanıcı adı yazın"
              autoComplete="off"
              aria-describedby="responsible-user-help"
            />

            <datalist id="assignable-user-options">
              {assignableUsers.map(
                (assignableUser) => (
                  <option
                    key={assignableUser.id}
                    value={getAssignableUserLabel(
                      assignableUser,
                    )}
                  />
                ),
              )}
            </datalist>

            <small
              id="responsible-user-help"
              className="field-help-text"
            >
              Kullanıcıyı listeden seçin. Boş
              bırakırsanız proje yöneticisi otomatik
              atanır.
            </small>
          </label>

          <label className="field">
            <span>Çözüm notu</span>

            <textarea
              name="resolutionNote"
              value={form.resolutionNote}
              onChange={handleFormChange}
              maxLength={2000}
              rows={3}
              placeholder="Varsa çözüm veya aksiyon notunu yazın"
            />
          </label>

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
            RİSK VE ENGEL YÖNETİMİ
          </span>

          <h1>Riskler ve Engeller</h1>

          <p>
            Projelerdeki risk, engel, önem ve takip
            bilgilerini yönetin.
          </p>
        </div>

        <div className="page-heading-actions">
          {canCreateOrDelete ? (
            <button
              type="button"
              className="button button-primary"
              onClick={openCreateModal}
              disabled={reports.length === 0}
            >
              + Yeni Kayıt
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
        <LoadingState message="Risk ve engel kayıtları yükleniyor..." />
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
          <form
            className="risk-filter-toolbar"
            onSubmit={applyFilters}
          >
            <div className="risk-filter-grid">
              <label className="report-filter-field">
                <span>Proje</span>

                <select
                  name="projectId"
                  value={filterDraft.projectId}
                  onChange={handleFilterChange}
                >
                  <option value="">
                    Tüm Projeler
                  </option>

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
                <span>Haftalık rapor</span>

                <select
                  name="weeklyReportId"
                  value={filterDraft.weeklyReportId}
                  onChange={handleFilterChange}
                >
                  <option value="">
                    Tüm Raporlar
                  </option>

                  {filteredReportOptions.map(
                    (report) => (
                      <option
                        key={report.id}
                        value={report.id}
                      >
                        {formatDate(
                          report.weekStartDate,
                        )}
                      </option>
                    ),
                  )}
                </select>
              </label>

              <label className="report-filter-field">
                <span>Tür</span>

                <select
                  name="type"
                  value={filterDraft.type}
                  onChange={handleFilterChange}
                >
                  <option value="">Tüm Türler</option>

                  {RISK_ISSUE_TYPES.map((type) => (
                    <option key={type} value={type}>
                      {RISK_ISSUE_TYPE_LABELS[type]}
                    </option>
                  ))}
                </select>
              </label>

              <label className="report-filter-field">
                <span>Önem</span>

                <select
                  name="severity"
                  value={filterDraft.severity}
                  onChange={handleFilterChange}
                >
                  <option value="">
                    Tüm Seviyeler
                  </option>

                  {RISK_ISSUE_SEVERITIES.map(
                    (severity) => (
                      <option
                        key={severity}
                        value={severity}
                      >
                        {
                          RISK_ISSUE_SEVERITY_LABELS[
                            severity
                          ]
                        }
                      </option>
                    ),
                  )}
                </select>
              </label>

              <label className="report-filter-field">
                <span>Durum</span>

                <select
                  name="status"
                  value={filterDraft.status}
                  onChange={handleFilterChange}
                >
                  <option value="">
                    Tüm Durumlar
                  </option>

                  {RISK_ISSUE_STATUSES.map(
                    (status) => (
                      <option
                        key={status}
                        value={status}
                      >
                        {
                          RISK_ISSUE_STATUS_LABELS[
                            status
                          ]
                        }
                      </option>
                    ),
                  )}
                </select>
              </label>

              <label className="report-filter-field">
                <span>Sorumlu</span>

                <select
                  name="responsibleUserId"
                  value={
                    filterDraft.responsibleUserId
                  }
                  onChange={handleFilterChange}
                >
                  <option value="">
                    Tüm Kullanıcılar
                  </option>

                  {assignableUsers.map(
                    (assignableUser) => (
                      <option
                        key={assignableUser.id}
                        value={assignableUser.id}
                      >
                        {assignableUser.fullName}
                      </option>
                    ),
                  )}
                </select>
              </label>

              <label className="report-filter-field">
                <span>Takip başlangıcı</span>

                <input
                  type="date"
                  name="followUpDateFrom"
                  value={filterDraft.followUpDateFrom}
                  onChange={handleFilterChange}
                />
              </label>

              <label className="report-filter-field">
                <span>Takip bitişi</span>

                <input
                  type="date"
                  name="followUpDateTo"
                  value={filterDraft.followUpDateTo}
                  onChange={handleFilterChange}
                  min={
                    filterDraft.followUpDateFrom ||
                    undefined
                  }
                />
              </label>
            </div>

            <div className="risk-filter-actions">
              <button
                type="submit"
                className="button button-primary"
              >
                Filtrele
              </button>

              <button
                type="button"
                className="button button-secondary"
                onClick={clearFilters}
              >
                Temizle
              </button>
            </div>
          </form>

          {riskIssues.length === 0 ? (
            <div className="empty-state">
              Seçilen filtrelere uygun risk veya engel
              kaydı bulunamadı.
            </div>
          ) : (
            <div className="table-scroll">
              <table>
                <thead>
                  <tr>
                    <th>Kayıt</th>
                    <th>Proje / Rapor</th>
                    <th>Tür / Önem</th>
                    <th>Durum</th>
                    <th>Sorumlu</th>
                    <th>Takip Tarihi</th>
                    <th>İşlemler</th>
                  </tr>
                </thead>

                <tbody>
                  {riskIssues.map((riskIssue) => (
                    <tr key={riskIssue.id}>
                      <td>
                        <div className="table-primary-cell">
                          <strong>
                            {riskIssue.title}
                          </strong>
                        </div>
                      </td>

                      <td>
                        <div className="work-report-cell">
                          <strong>
                            {riskIssue.projectName}
                          </strong>

                          <span>
                            {getReportLabel(
                              riskIssue.weeklyReportId,
                            )}
                          </span>
                        </div>
                      </td>

                      <td>
                        <div className="risk-label-stack">
                          <span
                            className={`risk-type-badge risk-type-${riskIssue.type?.toLowerCase()}`}
                          >
                            {
                              RISK_ISSUE_TYPE_LABELS[
                                riskIssue.type
                              ]
                            }
                          </span>

                          <span
                            className={`severity-badge severity-${riskIssue.severity?.toLowerCase()}`}
                          >
                            {
                              RISK_ISSUE_SEVERITY_LABELS[
                                riskIssue.severity
                              ]
                            }
                          </span>
                        </div>
                      </td>

                      <td>
                        <StatusBadge
                          status={riskIssue.status}
                        />
                      </td>

                      <td>
                        {riskIssue.responsibleFullName ||
                          '-'}
                      </td>

                      <td>
                        {formatDate(
                          riskIssue.followUpDate,
                        )}
                      </td>

                      <td>
                        <div className="table-actions">
                          <button
                            type="button"
                            className="table-action-button"
                            onClick={() =>
                              openDetailModal(riskIssue)
                            }
                          >
                            Detay
                          </button>

                          {canUpdate ? (
                            <button
                              type="button"
                              className="table-action-button"
                              onClick={() =>
                                openEditModal(riskIssue)
                              }
                            >
                              Düzenle
                            </button>
                          ) : null}

                          {canCreateOrDelete ? (
                            <button
                              type="button"
                              className="table-action-button table-action-danger"
                              onClick={() =>
                                openDeleteModal(riskIssue)
                              }
                            >
                              Sil
                            </button>
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
          {renderRiskIssueForm({
            title: 'Yeni Risk veya Engel',
            description:
              'Haftalık rapora bağlı yeni bir takip kaydı oluşturun.',
            submitLabel: 'Kaydı Oluştur',
            onSubmit: handleCreateRiskIssue,
          })}
        </div>
      ) : null}

      {activeModal === 'edit' ? (
        <div
          className="modal-backdrop"
          role="presentation"
          onMouseDown={closeModal}
        >
          {renderRiskIssueForm({
            title: 'Kaydı Düzenle',
            description:
              'Risk veya engel kaydının bilgilerini güncelleyin.',
            submitLabel: 'Değişiklikleri Kaydet',
            onSubmit: handleUpdateRiskIssue,
            isEdit: true,
          })}
        </div>
      ) : null}

      {activeModal === 'detail' &&
      selectedRiskIssue ? (
        <div
          className="modal-backdrop"
          role="presentation"
          onMouseDown={closeModal}
        >
          <section
            className="modal-card risk-modal-card"
            role="dialog"
            aria-modal="true"
            aria-labelledby="risk-detail-title"
            onMouseDown={(event) =>
              event.stopPropagation()
            }
          >
            <div className="modal-heading">
              <div>
                <span className="eyebrow">
                  RİSK VE ENGEL DETAYI
                </span>

                <h2 id="risk-detail-title">
                  {selectedRiskIssue.title}
                </h2>

                <p>
                  Takip kaydına ait tüm bilgiler.
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

            <div className="risk-detail-grid">
              <div className="risk-detail-item">
                <span>Proje</span>
                <strong>
                  {selectedRiskIssue.projectName}
                </strong>
              </div>

              <div className="risk-detail-item">
                <span>Haftalık rapor</span>
                <strong>
                  {getReportLabel(
                    selectedRiskIssue.weeklyReportId,
                  )}
                </strong>
              </div>

              <div className="risk-detail-item">
                <span>Tür</span>
                <strong>
                  {
                    RISK_ISSUE_TYPE_LABELS[
                      selectedRiskIssue.type
                    ]
                  }
                </strong>
              </div>

              <div className="risk-detail-item">
                <span>Önem seviyesi</span>
                <strong>
                  {
                    RISK_ISSUE_SEVERITY_LABELS[
                      selectedRiskIssue.severity
                    ]
                  }
                </strong>
              </div>

              <div className="risk-detail-item">
                <span>Durum</span>
                <StatusBadge
                  status={selectedRiskIssue.status}
                />
              </div>

              <div className="risk-detail-item">
                <span>Sorumlu kullanıcı</span>
                <strong>
                  {selectedRiskIssue
                    .responsibleFullName || '-'}
                </strong>
              </div>

              <div className="risk-detail-item">
                <span>Takip tarihi</span>
                <strong>
                  {formatDate(
                    selectedRiskIssue.followUpDate,
                  )}
                </strong>
              </div>

              <div className="risk-detail-item">
                <span>Kullanıcı adı</span>
                <strong>
                  {selectedRiskIssue
                    .responsibleUsername || '-'}
                </strong>
              </div>

              <div className="risk-detail-item risk-detail-item-wide">
                <span>Açıklama</span>
                <p>
                  {selectedRiskIssue.description}
                </p>
              </div>

              <div className="risk-detail-item risk-detail-item-wide">
                <span>Çözüm notu</span>
                <p>
                  {selectedRiskIssue.resolutionNote ||
                    'Çözüm notu girilmemiş.'}
                </p>
              </div>
            </div>

            <div className="modal-actions">
              {canUpdate ? (
                <button
                  type="button"
                  className="button button-primary"
                  onClick={() =>
                    openEditModal(selectedRiskIssue)
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
      selectedRiskIssue ? (
        <div
          className="modal-backdrop"
          role="presentation"
          onMouseDown={closeModal}
        >
          <section
            className="modal-card delete-modal-card"
            role="dialog"
            aria-modal="true"
            aria-labelledby="delete-risk-title"
            onMouseDown={(event) =>
              event.stopPropagation()
            }
          >
            <div className="modal-heading">
              <div>
                <span className="eyebrow danger-eyebrow">
                  KAYDI SİL
                </span>

                <h2 id="delete-risk-title">
                  Kayıt silinsin mi?
                </h2>

                <p>
                  <strong>
                    {selectedRiskIssue.title}
                  </strong>{' '}
                  aktif risk ve engel listesinden
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
                onClick={handleDeleteRiskIssue}
                disabled={isSubmitting}
              >
                {isSubmitting
                  ? 'Siliniyor...'
                  : 'Kaydı Sil'}
              </button>
            </div>
          </section>
        </div>
      ) : null}
    </div>
  )
}