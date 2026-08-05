import { useEffect, useMemo, useState } from 'react'
import {
  createDecisionLog,
  deactivateDecisionLog,
  getDecisionLogs,
  updateDecisionLog,
} from '../api/decisionLogApi.js'
import { getProjects } from '../api/projectApi.js'
import { getAssignableUsers } from '../api/userApi.js'
import { getWeeklyReports } from '../api/weeklyReportApi.js'
import { useAuth } from '../auth/AuthContext.jsx'
import ErrorState from '../components/ErrorState.jsx'
import LoadingState from '../components/LoadingState.jsx'
import StatusBadge from '../components/StatusBadge.jsx'
import { ROLES } from '../utils/roles.js'
import {
  DECISION_STATUSES,
  DECISION_STATUS_LABELS,
} from '../utils/statuses.js'

const INITIAL_FILTERS = {
  projectId: '',
  weeklyReportId: '',
  status: '',
  decisionOwnerId: '',
  decisionDateFrom: '',
  decisionDateTo: '',
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

function createInitialForm() {
  return {
    projectId: '',
    weeklyReportId: '',
    decisionOwnerId: '',
    title: '',
    description: '',
    decisionDate: formatInputDate(new Date()),
    status: 'DRAFT',
    note: '',
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

function formatDateTime(value) {
  if (!value) {
    return '-'
  }

  return new Intl.DateTimeFormat('tr-TR', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(new Date(value))
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

export default function DecisionLogsPage() {
  const { user } = useAuth()

  const [projects, setProjects] = useState([])
  const [reports, setReports] = useState([])
  const [assignableUsers, setAssignableUsers] =
    useState([])
  const [decisionLogs, setDecisionLogs] =
    useState([])

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
  const [selectedDecisionLog, setSelectedDecisionLog] =
    useState(null)
  const [form, setForm] = useState(
    createInitialForm,
  )
  const [formError, setFormError] = useState('')
  const [decisionOwnerSearch, setDecisionOwnerSearch] =
    useState('')
  const [isOwnerOptionsOpen, setIsOwnerOptionsOpen] =
    useState(false)
  const [isSubmitting, setIsSubmitting] =
    useState(false)

  const canCreate =
    user?.role === ROLES.PROJECT_MANAGER ||
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
      return []
    }

    return reports.filter(
      (report) =>
        Number(report.projectId) ===
        Number(form.projectId),
    )
  }, [form.projectId, reports])

  const filteredOwnerOptions = useMemo(() => {
    const normalizedSearch = normalizeUserSearch(
      decisionOwnerSearch,
    )

    if (!normalizedSearch) {
      return assignableUsers
    }

    return assignableUsers.filter(
      (assignableUser) => {
        const searchableValues = [
          assignableUser.fullName,
          assignableUser.username,
          assignableUser.email,
          getAssignableUserLabel(assignableUser),
        ]

        return searchableValues.some(
          (searchableValue) =>
            searchableValue &&
            normalizeUserSearch(
              searchableValue,
            ).includes(normalizedSearch),
        )
      },
    )
  }, [assignableUsers, decisionOwnerSearch])

  useEffect(() => {
    const controller = new AbortController()

    setIsLoading(true)
    setError('')

    Promise.all([
      getProjects(null, controller.signal),
      getWeeklyReports({}, controller.signal),
      getAssignableUsers(controller.signal),
      getDecisionLogs(filters, controller.signal),
    ])
      .then(
        ([
          projectResponse,
          reportResponse,
          userResponse,
          decisionLogResponse,
        ]) => {
          setProjects(projectResponse)
          setReports(reportResponse)
          setAssignableUsers(userResponse)
          setDecisionLogs(decisionLogResponse)
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

  function getProjectById(projectId) {
    return projects.find(
      (project) =>
        Number(project.id) === Number(projectId),
    )
  }

  function getReportById(weeklyReportId) {
    return reports.find(
      (report) =>
        Number(report.id) ===
        Number(weeklyReportId),
    )
  }

  function getReportLabel(weeklyReportId) {
    if (!weeklyReportId) {
      return 'Projeye bağlı genel karar'
    }

    const report = getReportById(weeklyReportId)

    if (!report) {
      return `Rapor #${weeklyReportId}`
    }

    return `${formatDate(
      report.weekStartDate,
    )} - ${formatDate(report.weekEndDate)}`
  }

  function isCurrentUserProjectOwner(decisionLog) {
    if (user?.role !== ROLES.PROJECT_MANAGER) {
      return false
    }

    const project = getProjectById(
      decisionLog.projectId,
    )

    return (
      Number(project?.projectManagerId) ===
      Number(user?.id)
    )
  }

  function canUpdateDecisionLog(decisionLog) {
    if (user?.role === ROLES.ADMIN) {
      return true
    }

    const isDecisionOwner =
      Number(decisionLog.decisionOwnerId) ===
      Number(user?.id)

    if (user?.role === ROLES.TEAM_LEAD) {
      return isDecisionOwner
    }

    if (user?.role === ROLES.PROJECT_MANAGER) {
      return (
        isDecisionOwner ||
        isCurrentUserProjectOwner(decisionLog)
      )
    }

    return false
  }

  function canDeleteDecisionLog(decisionLog) {
    if (user?.role === ROLES.ADMIN) {
      return true
    }

    return isCurrentUserProjectOwner(decisionLog)
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
        updatedForm.weeklyReportId = ''
      }

      return updatedForm
    })
  }

  function handleDecisionOwnerSearchChange(event) {
    const value = event.target.value
    const normalizedValue = normalizeUserSearch(value)

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

    setDecisionOwnerSearch(value)
    setIsOwnerOptionsOpen(true)

    setForm((currentForm) => ({
      ...currentForm,
      decisionOwnerId: selectedUser
        ? String(selectedUser.id)
        : '',
    }))
  }

  function selectDecisionOwner(assignableUser) {
    setForm((currentForm) => ({
      ...currentForm,
      decisionOwnerId: assignableUser
        ? String(assignableUser.id)
        : '',
    }))
    setDecisionOwnerSearch(
      assignableUser
        ? getAssignableUserLabel(assignableUser)
        : '',
    )
    setIsOwnerOptionsOpen(false)
  }

  function resetModal() {
    setActiveModal(null)
    setSelectedDecisionLog(null)
    setForm(createInitialForm())
    setDecisionOwnerSearch('')
    setIsOwnerOptionsOpen(false)
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

    setSelectedDecisionLog(null)
    setDecisionOwnerSearch('')
    setIsOwnerOptionsOpen(false)
    setForm({
      ...createInitialForm(),
      projectId: firstProject
        ? String(firstProject.id)
        : '',
    })
    setFormError('')
    setSuccessMessage('')
    setActiveModal('create')
  }

  function openDetailModal(decisionLog) {
    setSelectedDecisionLog(decisionLog)
    setFormError('')
    setActiveModal('detail')
  }

  function openEditModal(decisionLog) {
    const selectedOwner = assignableUsers.find(
      (assignableUser) =>
        Number(assignableUser.id) ===
        Number(decisionLog.decisionOwnerId),
    )

    setSelectedDecisionLog(decisionLog)
    setDecisionOwnerSearch(
      selectedOwner
        ? getAssignableUserLabel(selectedOwner)
        : decisionLog.decisionOwnerFullName || '',
    )
    setIsOwnerOptionsOpen(false)
    setForm({
      projectId: String(decisionLog.projectId),
      weeklyReportId: decisionLog.weeklyReportId
        ? String(decisionLog.weeklyReportId)
        : '',
      decisionOwnerId: decisionLog.decisionOwnerId
        ? String(decisionLog.decisionOwnerId)
        : '',
      title: decisionLog.title || '',
      description: decisionLog.description || '',
      decisionDate: decisionLog.decisionDate || '',
      status: decisionLog.status || 'DRAFT',
      note: decisionLog.note || '',
    })
    setFormError('')
    setSuccessMessage('')
    setActiveModal('edit')
  }

  function openDeleteModal(decisionLog) {
    setSelectedDecisionLog(decisionLog)
    setFormError('')
    setSuccessMessage('')
    setActiveModal('delete')
  }

  function validateForm() {
    if (!form.projectId) {
      return 'Proje seçimi zorunludur.'
    }

    if (!form.title.trim()) {
      return 'Karar başlığı zorunludur.'
    }

    if (!form.description.trim()) {
      return 'Karar açıklaması zorunludur.'
    }

    if (!form.decisionDate) {
      return 'Karar tarihi zorunludur.'
    }

    if (
      decisionOwnerSearch.trim() &&
      !form.decisionOwnerId
    ) {
      return 'Lütfen karar sahibini listeden seçin.'
    }

    return ''
  }

  function createRequestBody() {
    return {
      projectId: Number(form.projectId),
      weeklyReportId: form.weeklyReportId
        ? Number(form.weeklyReportId)
        : null,
      decisionOwnerId: form.decisionOwnerId
        ? Number(form.decisionOwnerId)
        : null,
      title: form.title.trim(),
      description: form.description.trim(),
      decisionDate: form.decisionDate,
      status: form.status,
      note: normalizeOptionalText(form.note),
    }
  }

  function createUpdateRequestBody() {
    return {
      decisionOwnerId: form.decisionOwnerId
        ? Number(form.decisionOwnerId)
        : null,
      title: form.title.trim(),
      description: form.description.trim(),
      decisionDate: form.decisionDate,
      status: form.status,
      note: normalizeOptionalText(form.note),
    }
  }

  async function handleCreateDecisionLog(event) {
    event.preventDefault()
    setFormError('')

    const validationError = validateForm()

    if (validationError) {
      setFormError(validationError)
      return
    }

    setIsSubmitting(true)

    try {
      await createDecisionLog(createRequestBody())

      resetModal()
      setSuccessMessage(
        'Karar kaydı başarıyla oluşturuldu.',
      )
      setReloadKey((value) => value + 1)
    } catch (requestError) {
      setFormError(getErrorMessage(requestError))
    } finally {
      setIsSubmitting(false)
    }
  }

  async function handleUpdateDecisionLog(event) {
    event.preventDefault()
    setFormError('')

    const validationError = validateForm()

    if (validationError) {
      setFormError(validationError)
      return
    }

    if (!selectedDecisionLog) {
      setFormError(
        'Güncellenecek karar kaydı bulunamadı.',
      )
      return
    }

    setIsSubmitting(true)

    try {
      await updateDecisionLog(
        selectedDecisionLog.id,
        createUpdateRequestBody(),
      )

      resetModal()
      setSuccessMessage(
        'Karar kaydı başarıyla güncellendi.',
      )
      setReloadKey((value) => value + 1)
    } catch (requestError) {
      setFormError(getErrorMessage(requestError))
    } finally {
      setIsSubmitting(false)
    }
  }

  async function handleDeleteDecisionLog() {
    if (!selectedDecisionLog) {
      setFormError(
        'Silinecek karar kaydı bulunamadı.',
      )
      return
    }

    setFormError('')
    setIsSubmitting(true)

    try {
      await deactivateDecisionLog(
        selectedDecisionLog.id,
      )

      resetModal()
      setSuccessMessage(
        'Karar kaydı başarıyla silindi.',
      )
      setReloadKey((value) => value + 1)
    } catch (requestError) {
      setFormError(getErrorMessage(requestError))
    } finally {
      setIsSubmitting(false)
    }
  }

  function renderOwnerSelect() {
    return (
      <label className="field">
        <span>Karar sahibi</span>

        <div
          className="searchable-user-select"
          onBlur={(event) => {
            if (
              !event.currentTarget.contains(
                event.relatedTarget,
              )
            ) {
              setIsOwnerOptionsOpen(false)
            }
          }}
        >
          <input
            type="text"
            value={decisionOwnerSearch}
            onChange={
              handleDecisionOwnerSearchChange
            }
            onFocus={() =>
              setIsOwnerOptionsOpen(true)
            }
            placeholder="İsim, kullanıcı adı veya e-posta yazın"
            autoComplete="off"
            aria-expanded={isOwnerOptionsOpen}
            aria-controls="decision-owner-options"
          />

          {isOwnerOptionsOpen ? (
            <div
              id="decision-owner-options"
              className="searchable-user-options"
              role="listbox"
            >
              <button
                type="button"
                className={`searchable-user-option searchable-user-option-automatic ${
                  !form.decisionOwnerId
                    ? 'searchable-user-option-selected'
                    : ''
                }`}
                onMouseDown={(event) =>
                  event.preventDefault()
                }
                onClick={() =>
                  selectDecisionOwner(null)
                }
              >
                <strong>Proje yöneticisi</strong>
                <span>
                  Boş bırakıldığında otomatik atanır.
                </span>
              </button>

              {filteredOwnerOptions.length > 0 ? (
                filteredOwnerOptions.map(
                  (assignableUser) => (
                    <button
                      key={assignableUser.id}
                      type="button"
                      role="option"
                      aria-selected={
                        Number(
                          form.decisionOwnerId,
                        ) ===
                        Number(assignableUser.id)
                      }
                      className={`searchable-user-option ${
                        Number(
                          form.decisionOwnerId,
                        ) ===
                        Number(assignableUser.id)
                          ? 'searchable-user-option-selected'
                          : ''
                      }`}
                      onMouseDown={(event) =>
                        event.preventDefault()
                      }
                      onClick={() =>
                        selectDecisionOwner(
                          assignableUser,
                        )
                      }
                    >
                      <strong>
                        {assignableUser.fullName ||
                          assignableUser.username}
                      </strong>

                      <span>
                        {assignableUser.username}
                        {assignableUser.email
                          ? ` · ${assignableUser.email}`
                          : ''}
                      </span>
                    </button>
                  ),
                )
              ) : (
                <div className="searchable-user-empty">
                  Aramaya uygun kullanıcı bulunamadı.
                </div>
              )}
            </div>
          ) : null}
        </div>

        <small className="field-help-text">
          Seçim yapmazsanız proje yöneticisi karar
          sahibi olarak atanır.
        </small>
      </label>
    )
  }

  function renderDecisionLogForm({
    title,
    description,
    submitLabel,
    onSubmit,
    isEdit = false,
  }) {
    return (
      <section
        className="modal-card decision-modal-card"
        role="dialog"
        aria-modal="true"
        aria-labelledby="decision-form-title"
        onMouseDown={(event) =>
          event.stopPropagation()
        }
      >
        <div className="modal-heading">
          <div>
            <span className="eyebrow">
              KARAR KAYITLARI
            </span>

            <h2 id="decision-form-title">{title}</h2>

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
          className="decision-form"
          onSubmit={onSubmit}
        >
          {isEdit ? (
            <div className="form-grid">
              <div className="decision-readonly-field">
                <span>Proje</span>
                <strong>
                  {selectedDecisionLog?.projectName ||
                    '-'}
                </strong>
              </div>

              <div className="decision-readonly-field">
                <span>Haftalık rapor</span>
                <strong>
                  {getReportLabel(
                    selectedDecisionLog?.weeklyReportId,
                  )}
                </strong>
              </div>
            </div>
          ) : (
            <div className="form-grid">
              <label className="field">
                <span>Proje</span>

                <select
                  name="projectId"
                  value={form.projectId}
                  onChange={handleFormChange}
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
                  disabled={!form.projectId}
                >
                  <option value="">
                    Genel proje kararı
                  </option>

                  {formReportOptions.map((report) => (
                    <option
                      key={report.id}
                      value={report.id}
                    >
                      {formatDate(
                        report.weekStartDate,
                      )}
                      {' - '}
                      {formatDate(report.weekEndDate)}
                    </option>
                  ))}
                </select>
              </label>
            </div>
          )}

          <label className="field">
            <span>Karar başlığı</span>

            <input
              type="text"
              name="title"
              value={form.title}
              onChange={handleFormChange}
              maxLength={200}
              placeholder="Alınan kararın kısa başlığını girin"
              required
            />
          </label>

          <label className="field">
            <span>Karar açıklaması</span>

            <textarea
              name="description"
              value={form.description}
              onChange={handleFormChange}
              maxLength={2000}
              rows={5}
              placeholder="Kararın içeriğini ve gerekçesini açıklayın"
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
                {DECISION_STATUSES.map(
                  (decisionStatus) => (
                    <option
                      key={decisionStatus}
                      value={decisionStatus}
                    >
                      {
                        DECISION_STATUS_LABELS[
                          decisionStatus
                        ]
                      }
                    </option>
                  ),
                )}
              </select>
            </label>

            <label className="field">
              <span>Karar tarihi</span>

              <input
                type="date"
                name="decisionDate"
                value={form.decisionDate}
                onChange={handleFormChange}
                required
              />
            </label>
          </div>

          {renderOwnerSelect()}

          <label className="field">
            <span>Karar notu</span>

            <textarea
              name="note"
              value={form.note}
              onChange={handleFormChange}
              maxLength={2000}
              rows={3}
              placeholder="Varsa uygulama, takip veya ek açıklama notunu yazın"
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
            KARAR YÖNETİMİ
          </span>

          <h1>Karar Kayıtları</h1>

          <p>
            Projelerde alınan kararları, karar
            sahiplerini ve uygulama durumlarını takip
            edin.
          </p>
        </div>

        <div className="page-heading-actions">
          {canCreate ? (
            <button
              type="button"
              className="button button-primary"
              onClick={openCreateModal}
              disabled={projects.length === 0}
            >
              + Yeni Karar
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
        <LoadingState message="Karar kayıtları yükleniyor..." />
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
            className="decision-filter-toolbar"
            onSubmit={applyFilters}
          >
            <div className="decision-filter-grid">
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
                <span>Durum</span>

                <select
                  name="status"
                  value={filterDraft.status}
                  onChange={handleFilterChange}
                >
                  <option value="">
                    Tüm Durumlar
                  </option>

                  {DECISION_STATUSES.map(
                    (status) => (
                      <option
                        key={status}
                        value={status}
                      >
                        {
                          DECISION_STATUS_LABELS[
                            status
                          ]
                        }
                      </option>
                    ),
                  )}
                </select>
              </label>

              <label className="report-filter-field">
                <span>Karar sahibi</span>

                <select
                  name="decisionOwnerId"
                  value={filterDraft.decisionOwnerId}
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
                        {assignableUser.fullName ||
                          assignableUser.username}
                      </option>
                    ),
                  )}
                </select>
              </label>

              <label className="report-filter-field">
                <span>Karar başlangıcı</span>

                <input
                  type="date"
                  name="decisionDateFrom"
                  value={filterDraft.decisionDateFrom}
                  onChange={handleFilterChange}
                />
              </label>

              <label className="report-filter-field">
                <span>Karar bitişi</span>

                <input
                  type="date"
                  name="decisionDateTo"
                  value={filterDraft.decisionDateTo}
                  onChange={handleFilterChange}
                  min={
                    filterDraft.decisionDateFrom ||
                    undefined
                  }
                />
              </label>
            </div>

            <div className="decision-filter-actions">
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

          {decisionLogs.length === 0 ? (
            <div className="empty-state">
              Seçilen filtrelere uygun karar kaydı
              bulunamadı.
            </div>
          ) : (
            <div className="table-scroll">
              <table>
                <thead>
                  <tr>
                    <th>Karar</th>
                    <th>Proje / Rapor</th>
                    <th>Durum</th>
                    <th>Karar Sahibi</th>
                    <th>Karar Tarihi</th>
                    <th>İşlemler</th>
                  </tr>
                </thead>

                <tbody>
                  {decisionLogs.map((decisionLog) => (
                    <tr key={decisionLog.id}>
                      <td>
                        <div className="table-primary-cell">
                          <strong>
                            {decisionLog.title}
                          </strong>
                        </div>
                      </td>

                      <td>
                        <div className="work-report-cell">
                          <strong>
                            {decisionLog.projectName}
                          </strong>

                          <span>
                            {getReportLabel(
                              decisionLog.weeklyReportId,
                            )}
                          </span>
                        </div>
                      </td>

                      <td>
                        <StatusBadge
                          status={decisionLog.status}
                        />
                      </td>

                      <td>
                        <div className="decision-owner-cell">
                          <strong>
                            {decisionLog.decisionOwnerFullName ||
                              '-'}
                          </strong>

                          <span>
                            {decisionLog.decisionOwnerUsername ||
                              '-'}
                          </span>
                        </div>
                      </td>

                      <td>
                        {formatDate(
                          decisionLog.decisionDate,
                        )}
                      </td>

                      <td>
                        <div className="table-actions">
                          <button
                            type="button"
                            className="table-action-button"
                            onClick={() =>
                              openDetailModal(
                                decisionLog,
                              )
                            }
                          >
                            Detay
                          </button>

                          {canUpdateDecisionLog(
                            decisionLog,
                          ) ? (
                            <button
                              type="button"
                              className="table-action-button"
                              onClick={() =>
                                openEditModal(
                                  decisionLog,
                                )
                              }
                            >
                              Düzenle
                            </button>
                          ) : null}

                          {canDeleteDecisionLog(
                            decisionLog,
                          ) ? (
                            <button
                              type="button"
                              className="table-action-button table-action-danger"
                              onClick={() =>
                                openDeleteModal(
                                  decisionLog,
                                )
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
          {renderDecisionLogForm({
            title: 'Yeni Karar Kaydı',
            description:
              'Projede alınan yeni kararı ve takip bilgilerini kaydedin.',
            submitLabel: 'Kararı Oluştur',
            onSubmit: handleCreateDecisionLog,
          })}
        </div>
      ) : null}

      {activeModal === 'edit' ? (
        <div
          className="modal-backdrop"
          role="presentation"
          onMouseDown={closeModal}
        >
          {renderDecisionLogForm({
            title: 'Kararı Düzenle',
            description:
              'Karar kaydının sahibi, durumu ve açıklamalarını güncelleyin.',
            submitLabel: 'Değişiklikleri Kaydet',
            onSubmit: handleUpdateDecisionLog,
            isEdit: true,
          })}
        </div>
      ) : null}

      {activeModal === 'detail' &&
      selectedDecisionLog ? (
        <div
          className="modal-backdrop"
          role="presentation"
          onMouseDown={closeModal}
        >
          <section
            className="modal-card decision-modal-card"
            role="dialog"
            aria-modal="true"
            aria-labelledby="decision-detail-title"
            onMouseDown={(event) =>
              event.stopPropagation()
            }
          >
            <div className="modal-heading">
              <div>
                <span className="eyebrow">
                  KARAR DETAYI
                </span>

                <h2 id="decision-detail-title">
                  {selectedDecisionLog.title}
                </h2>

                <p>
                  Karara ait proje, sahiplik ve takip
                  bilgileri.
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

            <div className="decision-detail-grid">
              <div className="decision-detail-item">
                <span>Proje</span>
                <strong>
                  {selectedDecisionLog.projectName}
                </strong>
              </div>

              <div className="decision-detail-item">
                <span>Haftalık rapor</span>
                <strong>
                  {getReportLabel(
                    selectedDecisionLog.weeklyReportId,
                  )}
                </strong>
              </div>

              <div className="decision-detail-item">
                <span>Durum</span>
                <StatusBadge
                  status={selectedDecisionLog.status}
                />
              </div>

              <div className="decision-detail-item">
                <span>Karar tarihi</span>
                <strong>
                  {formatDate(
                    selectedDecisionLog.decisionDate,
                  )}
                </strong>
              </div>

              <div className="decision-detail-item">
                <span>Karar sahibi</span>
                <strong>
                  {selectedDecisionLog
                    .decisionOwnerFullName || '-'}
                </strong>
              </div>

              <div className="decision-detail-item">
                <span>Kullanıcı adı</span>
                <strong>
                  {selectedDecisionLog
                    .decisionOwnerUsername || '-'}
                </strong>
              </div>

              <div className="decision-detail-item">
                <span>Oluşturulma</span>
                <strong>
                  {formatDateTime(
                    selectedDecisionLog.createdAt,
                  )}
                </strong>
              </div>

              <div className="decision-detail-item">
                <span>Son güncelleme</span>
                <strong>
                  {formatDateTime(
                    selectedDecisionLog.updatedAt,
                  )}
                </strong>
              </div>

              <div className="decision-detail-item decision-detail-item-wide">
                <span>Karar açıklaması</span>
                <p>
                  {selectedDecisionLog.description}
                </p>
              </div>

              <div className="decision-detail-item decision-detail-item-wide">
                <span>Karar notu</span>
                <p>
                  {selectedDecisionLog.note ||
                    'Ek karar notu girilmemiş.'}
                </p>
              </div>
            </div>

            <div className="modal-actions">
              {canUpdateDecisionLog(
                selectedDecisionLog,
              ) ? (
                <button
                  type="button"
                  className="button button-primary"
                  onClick={() =>
                    openEditModal(
                      selectedDecisionLog,
                    )
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
      selectedDecisionLog ? (
        <div
          className="modal-backdrop"
          role="presentation"
          onMouseDown={closeModal}
        >
          <section
            className="modal-card delete-modal-card"
            role="dialog"
            aria-modal="true"
            aria-labelledby="delete-decision-title"
            onMouseDown={(event) =>
              event.stopPropagation()
            }
          >
            <div className="modal-heading">
              <div>
                <span className="eyebrow danger-eyebrow">
                  KARARI SİL
                </span>

                <h2 id="delete-decision-title">
                  Karar kaydı silinsin mi?
                </h2>

                <p>
                  <strong>
                    {selectedDecisionLog.title}
                  </strong>{' '}
                  aktif karar kayıtları listesinden
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
                onClick={handleDeleteDecisionLog}
                disabled={isSubmitting}
              >
                {isSubmitting
                  ? 'Siliniyor...'
                  : 'Kararı Sil'}
              </button>
            </div>
          </section>
        </div>
      ) : null}
    </div>
  )
}
