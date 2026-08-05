import { useEffect, useMemo, useState } from 'react'
import {
  createActionItem,
  deactivateActionItem,
  getActionItems,
  updateActionItem,
} from '../api/actionItemApi.js'
import { getProjects } from '../api/projectApi.js'
import { getAssignableUsers } from '../api/userApi.js'
import { getWeeklyReports } from '../api/weeklyReportApi.js'
import { useAuth } from '../auth/AuthContext.jsx'
import ErrorState from '../components/ErrorState.jsx'
import LoadingState from '../components/LoadingState.jsx'
import StatusBadge from '../components/StatusBadge.jsx'
import { ROLES } from '../utils/roles.js'
import {
  ACTION_ITEM_PRIORITIES,
  ACTION_ITEM_PRIORITY_LABELS,
  ACTION_ITEM_STATUSES,
  ACTION_ITEM_STATUS_LABELS,
} from '../utils/statuses.js'

const INITIAL_FILTERS = {
  projectId: '',
  weeklyReportId: '',
  status: '',
  priority: '',
  responsibleUserId: '',
  targetDateFrom: '',
  targetDateTo: '',
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
    responsibleUserId: '',
    title: '',
    description: '',
    priority: 'MEDIUM',
    status: 'OPEN',
    targetDate: formatInputDate(new Date()),
    completionDate: '',
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

export default function ActionItemsPage() {
  const { user } = useAuth()

  const [projects, setProjects] = useState([])
  const [reports, setReports] = useState([])
  const [assignableUsers, setAssignableUsers] =
    useState([])
  const [actionItems, setActionItems] =
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
  const [selectedActionItem, setSelectedActionItem] =
    useState(null)
  const [form, setForm] = useState(
    createInitialForm,
  )
  const [formError, setFormError] = useState('')
  const [responsibleUserSearch, setResponsibleUserSearch] =
    useState('')
  const [isResponsibleOptionsOpen, setIsResponsibleOptionsOpen] =
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

  const filteredResponsibleOptions = useMemo(() => {
    const normalizedSearch = normalizeUserSearch(
      responsibleUserSearch,
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
  }, [assignableUsers, responsibleUserSearch])

  useEffect(() => {
    const controller = new AbortController()

    setIsLoading(true)
    setError('')

    Promise.all([
      getProjects(null, controller.signal),
      getWeeklyReports({}, controller.signal),
      getAssignableUsers(controller.signal),
      getActionItems(filters, controller.signal),
    ])
      .then(
        ([
          projectResponse,
          reportResponse,
          userResponse,
          actionItemResponse,
        ]) => {
          setProjects(projectResponse)
          setReports(reportResponse)
          setAssignableUsers(userResponse)
          setActionItems(actionItemResponse)
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
      return 'Projeye bağlı genel aksiyon'
    }

    const report = getReportById(weeklyReportId)

    if (!report) {
      return `Rapor #${weeklyReportId}`
    }

    return `${formatDate(
      report.weekStartDate,
    )} - ${formatDate(report.weekEndDate)}`
  }

  function isCurrentUserProjectOwner(actionItem) {
    if (user?.role !== ROLES.PROJECT_MANAGER) {
      return false
    }

    const project = getProjectById(
      actionItem.projectId,
    )

    return (
      Number(project?.projectManagerId) ===
      Number(user?.id)
    )
  }

  function canUpdateActionItem(actionItem) {
    if (user?.role === ROLES.ADMIN) {
      return true
    }

    const isResponsibleUser =
      Number(actionItem.responsibleUserId) ===
      Number(user?.id)

    if (user?.role === ROLES.TEAM_LEAD) {
      return isResponsibleUser
    }

    if (user?.role === ROLES.PROJECT_MANAGER) {
      return (
        isResponsibleUser ||
        isCurrentUserProjectOwner(actionItem)
      )
    }

    return false
  }

  function canDeleteActionItem(actionItem) {
    if (user?.role === ROLES.ADMIN) {
      return true
    }

    return isCurrentUserProjectOwner(actionItem)
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

      if (name === 'status') {
        if (value === 'COMPLETED') {
          updatedForm.completionDate =
            updatedForm.completionDate ||
            formatInputDate(new Date())
        } else {
          updatedForm.completionDate = ''
        }
      }

      return updatedForm
    })
  }

  function handleResponsibleUserSearchChange(event) {
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

    setResponsibleUserSearch(value)
    setIsResponsibleOptionsOpen(true)

    setForm((currentForm) => ({
      ...currentForm,
      responsibleUserId: selectedUser
        ? String(selectedUser.id)
        : '',
    }))
  }

  function selectResponsibleUser(assignableUser) {
    setForm((currentForm) => ({
      ...currentForm,
      responsibleUserId: assignableUser
        ? String(assignableUser.id)
        : '',
    }))
    setResponsibleUserSearch(
      assignableUser
        ? getAssignableUserLabel(assignableUser)
        : '',
    )
    setIsResponsibleOptionsOpen(false)
  }

  function resetModal() {
    setActiveModal(null)
    setSelectedActionItem(null)
    setForm(createInitialForm())
    setResponsibleUserSearch('')
    setIsResponsibleOptionsOpen(false)
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

    setSelectedActionItem(null)
    setResponsibleUserSearch('')
    setIsResponsibleOptionsOpen(false)
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

  function openDetailModal(actionItem) {
    setSelectedActionItem(actionItem)
    setFormError('')
    setActiveModal('detail')
  }

  function openEditModal(actionItem) {
    const selectedResponsibleUser = assignableUsers.find(
      (assignableUser) =>
        Number(assignableUser.id) ===
        Number(actionItem.responsibleUserId),
    )

    setSelectedActionItem(actionItem)
    setResponsibleUserSearch(
      selectedResponsibleUser
        ? getAssignableUserLabel(selectedResponsibleUser)
        : actionItem.responsibleFullName || '',
    )
    setIsResponsibleOptionsOpen(false)
    setForm({
      projectId: String(actionItem.projectId),
      weeklyReportId: actionItem.weeklyReportId
        ? String(actionItem.weeklyReportId)
        : '',
      responsibleUserId: actionItem.responsibleUserId
        ? String(actionItem.responsibleUserId)
        : '',
      title: actionItem.title || '',
      description: actionItem.description || '',
      priority: actionItem.priority || 'MEDIUM',
      status: actionItem.status || 'OPEN',
      targetDate: actionItem.targetDate || '',
      completionDate: actionItem.completionDate || '',
      note: actionItem.note || '',
    })
    setFormError('')
    setSuccessMessage('')
    setActiveModal('edit')
  }

  function openDeleteModal(actionItem) {
    setSelectedActionItem(actionItem)
    setFormError('')
    setSuccessMessage('')
    setActiveModal('delete')
  }

  function validateForm() {
    if (!form.projectId) {
      return 'Proje seçimi zorunludur.'
    }

    if (!form.title.trim()) {
      return 'Aksiyon başlığı zorunludur.'
    }

    if (!form.description.trim()) {
      return 'Aksiyon açıklaması zorunludur.'
    }

    if (!form.targetDate) {
      return 'Hedef tarih zorunludur.'
    }

    if (
      responsibleUserSearch.trim() &&
      !form.responsibleUserId
    ) {
      return 'Lütfen sorumlu kullanıcıyı listeden seçin.'
    }

    if (
      form.status === 'COMPLETED' &&
      form.completionDate &&
      form.completionDate > formatInputDate(new Date())
    ) {
      return 'Tamamlanma tarihi bugünden ileri olamaz.'
    }

    return ''
  }

  function createRequestBody() {
    return {
      projectId: Number(form.projectId),
      weeklyReportId: form.weeklyReportId
        ? Number(form.weeklyReportId)
        : null,
      responsibleUserId: form.responsibleUserId
        ? Number(form.responsibleUserId)
        : null,
      title: form.title.trim(),
      description: form.description.trim(),
      priority: form.priority,
      status: form.status,
      targetDate: form.targetDate,
      completionDate: form.completionDate || null,
      note: normalizeOptionalText(form.note),
    }
  }

  function createUpdateRequestBody() {
    return {
      responsibleUserId: form.responsibleUserId
        ? Number(form.responsibleUserId)
        : null,
      title: form.title.trim(),
      description: form.description.trim(),
      priority: form.priority,
      status: form.status,
      targetDate: form.targetDate,
      completionDate: form.completionDate || null,
      note: normalizeOptionalText(form.note),
    }
  }

  async function handleCreateActionItem(event) {
    event.preventDefault()
    setFormError('')

    const validationError = validateForm()

    if (validationError) {
      setFormError(validationError)
      return
    }

    setIsSubmitting(true)

    try {
      await createActionItem(createRequestBody())

      resetModal()
      setSuccessMessage(
        'Aksiyon kaydı başarıyla oluşturuldu.',
      )
      setReloadKey((value) => value + 1)
    } catch (requestError) {
      setFormError(getErrorMessage(requestError))
    } finally {
      setIsSubmitting(false)
    }
  }

  async function handleUpdateActionItem(event) {
    event.preventDefault()
    setFormError('')

    const validationError = validateForm()

    if (validationError) {
      setFormError(validationError)
      return
    }

    if (!selectedActionItem) {
      setFormError(
        'Güncellenecek aksiyon kaydı bulunamadı.',
      )
      return
    }

    setIsSubmitting(true)

    try {
      await updateActionItem(
        selectedActionItem.id,
        createUpdateRequestBody(),
      )

      resetModal()
      setSuccessMessage(
        'Aksiyon kaydı başarıyla güncellendi.',
      )
      setReloadKey((value) => value + 1)
    } catch (requestError) {
      setFormError(getErrorMessage(requestError))
    } finally {
      setIsSubmitting(false)
    }
  }

  async function handleDeleteActionItem() {
    if (!selectedActionItem) {
      setFormError(
        'Silinecek aksiyon kaydı bulunamadı.',
      )
      return
    }

    setFormError('')
    setIsSubmitting(true)

    try {
      await deactivateActionItem(
        selectedActionItem.id,
      )

      resetModal()
      setSuccessMessage(
        'Aksiyon kaydı başarıyla silindi.',
      )
      setReloadKey((value) => value + 1)
    } catch (requestError) {
      setFormError(getErrorMessage(requestError))
    } finally {
      setIsSubmitting(false)
    }
  }

  function renderResponsibleSelect() {
    return (
      <label className="field">
        <span>Sorumlu kullanıcı</span>

        <div
          className="searchable-user-select"
          onBlur={(event) => {
            if (
              !event.currentTarget.contains(
                event.relatedTarget,
              )
            ) {
              setIsResponsibleOptionsOpen(false)
            }
          }}
        >
          <input
            type="text"
            value={responsibleUserSearch}
            onChange={
              handleResponsibleUserSearchChange
            }
            onFocus={() =>
              setIsResponsibleOptionsOpen(true)
            }
            placeholder="İsim, kullanıcı adı veya e-posta yazın"
            autoComplete="off"
            aria-expanded={isResponsibleOptionsOpen}
            aria-controls="action-responsible-options"
          />

          {isResponsibleOptionsOpen ? (
            <div
              id="action-responsible-options"
              className="searchable-user-options"
              role="listbox"
            >
              <button
                type="button"
                className={`searchable-user-option searchable-user-option-automatic ${
                  !form.responsibleUserId
                    ? 'searchable-user-option-selected'
                    : ''
                }`}
                onMouseDown={(event) =>
                  event.preventDefault()
                }
                onClick={() =>
                  selectResponsibleUser(null)
                }
              >
                <strong>Proje yöneticisi</strong>
                <span>
                  Boş bırakıldığında otomatik atanır.
                </span>
              </button>

              {filteredResponsibleOptions.length > 0 ? (
                filteredResponsibleOptions.map(
                  (assignableUser) => (
                    <button
                      key={assignableUser.id}
                      type="button"
                      role="option"
                      aria-selected={
                        Number(
                          form.responsibleUserId,
                        ) ===
                        Number(assignableUser.id)
                      }
                      className={`searchable-user-option ${
                        Number(
                          form.responsibleUserId,
                        ) ===
                        Number(assignableUser.id)
                          ? 'searchable-user-option-selected'
                          : ''
                      }`}
                      onMouseDown={(event) =>
                        event.preventDefault()
                      }
                      onClick={() =>
                        selectResponsibleUser(
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
          Seçim yapmazsanız proje yöneticisi sorumlu kullanıcı olarak atanır.
        </small>
      </label>
    )
  }

  function renderActionItemForm({
    title,
    description,
    submitLabel,
    onSubmit,
    isEdit = false,
  }) {
    return (
      <section
        className="modal-card action-modal-card"
        role="dialog"
        aria-modal="true"
        aria-labelledby="action-form-title"
        onMouseDown={(event) =>
          event.stopPropagation()
        }
      >
        <div className="modal-heading">
          <div>
            <span className="eyebrow">
              AKSİYON KAYITLARI
            </span>

            <h2 id="action-form-title">{title}</h2>

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
          className="action-form"
          onSubmit={onSubmit}
        >
          {isEdit ? (
            <div className="form-grid">
              <div className="action-readonly-field">
                <span>Proje</span>
                <strong>
                  {selectedActionItem?.projectName ||
                    '-'}
                </strong>
              </div>

              <div className="action-readonly-field">
                <span>Haftalık rapor</span>
                <strong>
                  {getReportLabel(
                    selectedActionItem?.weeklyReportId,
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
                    Genel proje aksiyonu
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
            <span>Aksiyon başlığı</span>

            <input
              type="text"
              name="title"
              value={form.title}
              onChange={handleFormChange}
              maxLength={200}
              placeholder="Takip edilecek aksiyonun kısa başlığını girin"
              required
            />
          </label>

          <label className="field">
            <span>Aksiyon açıklaması</span>

            <textarea
              name="description"
              value={form.description}
              onChange={handleFormChange}
              maxLength={2000}
              rows={5}
              placeholder="Aksiyonun kapsamını ve beklenen sonucu açıklayın"
              required
            />
          </label>

          <div className="form-grid">
            <label className="field">
              <span>Öncelik</span>

              <select
                name="priority"
                value={form.priority}
                onChange={handleFormChange}
                required
              >
                {ACTION_ITEM_PRIORITIES.map(
                  (actionPriority) => (
                    <option
                      key={actionPriority}
                      value={actionPriority}
                    >
                      {
                        ACTION_ITEM_PRIORITY_LABELS[
                          actionPriority
                        ]
                      }
                    </option>
                  ),
                )}
              </select>
            </label>

            <label className="field">
              <span>Durum</span>

              <select
                name="status"
                value={form.status}
                onChange={handleFormChange}
                required
              >
                {ACTION_ITEM_STATUSES.map(
                  (actionStatus) => (
                    <option
                      key={actionStatus}
                      value={actionStatus}
                    >
                      {
                        ACTION_ITEM_STATUS_LABELS[
                          actionStatus
                        ]
                      }
                    </option>
                  ),
                )}
              </select>
            </label>
          </div>

          <div className="form-grid">
            <label className="field">
              <span>Hedef tarih</span>

              <input
                type="date"
                name="targetDate"
                value={form.targetDate}
                onChange={handleFormChange}
                required
              />
            </label>

            <label className="field">
              <span>Tamamlanma tarihi</span>

              <input
                type="date"
                name="completionDate"
                value={form.completionDate}
                onChange={handleFormChange}
                disabled={form.status !== 'COMPLETED'}
                max={formatInputDate(new Date())}
              />

              <small className="field-help-text">
                Yalnızca tamamlanan aksiyonlarda kullanılır.
              </small>
            </label>
          </div>

          {renderResponsibleSelect()}

          <label className="field">
            <span>Aksiyon notu</span>

            <textarea
              name="note"
              value={form.note}
              onChange={handleFormChange}
              maxLength={2000}
              rows={3}
              placeholder="Varsa takip veya ek açıklama notunu yazın"
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
            AKSİYON YÖNETİMİ
          </span>

          <h1>Aksiyon Kayıtları</h1>

          <p>
            Projelerdeki aksiyonları, sorumlu kullanıcıları, öncelikleri ve hedef tarihleri takip edin.
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
              + Yeni Aksiyon
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
        <LoadingState message="Aksiyon kayıtları yükleniyor..." />
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
            className="action-filter-toolbar"
            onSubmit={applyFilters}
          >
            <div className="action-filter-grid">
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

                  {ACTION_ITEM_STATUSES.map(
                    (status) => (
                      <option
                        key={status}
                        value={status}
                      >
                        {
                          ACTION_ITEM_STATUS_LABELS[
                            status
                          ]
                        }
                      </option>
                    ),
                  )}
                </select>
              </label>


              <label className="report-filter-field">
                <span>Öncelik</span>

                <select
                  name="priority"
                  value={filterDraft.priority}
                  onChange={handleFilterChange}
                >
                  <option value="">
                    Tüm Öncelikler
                  </option>

                  {ACTION_ITEM_PRIORITIES.map(
                    (priority) => (
                      <option
                        key={priority}
                        value={priority}
                      >
                        {
                          ACTION_ITEM_PRIORITY_LABELS[
                            priority
                          ]
                        }
                      </option>
                    ),
                  )}
                </select>
              </label>

              <label className="report-filter-field">
                <span>Sorumlu kullanıcı</span>

                <select
                  name="responsibleUserId"
                  value={filterDraft.responsibleUserId}
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
                <span>Hedef başlangıcı</span>

                <input
                  type="date"
                  name="targetDateFrom"
                  value={filterDraft.targetDateFrom}
                  onChange={handleFilterChange}
                />
              </label>

              <label className="report-filter-field">
                <span>Hedef bitişi</span>

                <input
                  type="date"
                  name="targetDateTo"
                  value={filterDraft.targetDateTo}
                  onChange={handleFilterChange}
                  min={
                    filterDraft.targetDateFrom ||
                    undefined
                  }
                />
              </label>
            </div>

            <div className="action-filter-actions">
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

          {actionItems.length === 0 ? (
            <div className="empty-state">
              Seçilen filtrelere uygun aksiyon kaydı
              bulunamadı.
            </div>
          ) : (
            <div className="table-scroll">
              <table>
                <thead>
                  <tr>
                    <th>Aksiyon</th>
                    <th>Proje / Rapor</th>
                    <th>Öncelik</th>
                    <th>Durum</th>
                    <th>Sorumlu Kullanıcı</th>
                    <th>Hedef Tarih</th>
                    <th>İşlemler</th>
                  </tr>
                </thead>

                <tbody>
                  {actionItems.map((actionItem) => (
                    <tr key={actionItem.id}>
                      <td>
                        <div className="table-primary-cell">
                          <strong>
                            {actionItem.title}
                          </strong>
                        </div>
                      </td>

                      <td>
                        <div className="work-report-cell">
                          <strong>
                            {actionItem.projectName}
                          </strong>

                          <span>
                            {getReportLabel(
                              actionItem.weeklyReportId,
                            )}
                          </span>
                        </div>
                      </td>

                      <td>
                        <span
                          className={`action-priority-badge action-priority-${actionItem.priority?.toLowerCase()}`}
                        >
                          {ACTION_ITEM_PRIORITY_LABELS[
                            actionItem.priority
                          ] || actionItem.priority}
                        </span>
                      </td>

                      <td>
                        <StatusBadge
                          status={actionItem.status}
                        />
                      </td>

                      <td>
                        <div className="action-owner-cell">
                          <strong>
                            {actionItem.responsibleFullName ||
                              '-'}
                          </strong>

                          <span>
                            {actionItem.responsibleUsername ||
                              '-'}
                          </span>
                        </div>
                      </td>

                      <td>
                        {formatDate(
                          actionItem.targetDate,
                        )}
                      </td>

                      <td>
                        <div className="table-actions">
                          <button
                            type="button"
                            className="table-action-button"
                            onClick={() =>
                              openDetailModal(
                                actionItem,
                              )
                            }
                          >
                            Detay
                          </button>

                          {canUpdateActionItem(
                            actionItem,
                          ) ? (
                            <button
                              type="button"
                              className="table-action-button"
                              onClick={() =>
                                openEditModal(
                                  actionItem,
                                )
                              }
                            >
                              Düzenle
                            </button>
                          ) : null}

                          {canDeleteActionItem(
                            actionItem,
                          ) ? (
                            <button
                              type="button"
                              className="table-action-button table-action-danger"
                              onClick={() =>
                                openDeleteModal(
                                  actionItem,
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
          {renderActionItemForm({
            title: 'Yeni Aksiyon Kaydı',
            description:
              'Projede takip edilecek yeni aksiyonu ve sorumluluk bilgilerini kaydedin.',
            submitLabel: 'Aksiyonu Oluştur',
            onSubmit: handleCreateActionItem,
          })}
        </div>
      ) : null}

      {activeModal === 'edit' ? (
        <div
          className="modal-backdrop"
          role="presentation"
          onMouseDown={closeModal}
        >
          {renderActionItemForm({
            title: 'Aksiyonu Düzenle',
            description:
              'Aksiyon kaydının sahibi, durumu ve açıklamalarını güncelleyin.',
            submitLabel: 'Değişiklikleri Kaydet',
            onSubmit: handleUpdateActionItem,
            isEdit: true,
          })}
        </div>
      ) : null}

      {activeModal === 'detail' &&
      selectedActionItem ? (
        <div
          className="modal-backdrop"
          role="presentation"
          onMouseDown={closeModal}
        >
          <section
            className="modal-card action-modal-card"
            role="dialog"
            aria-modal="true"
            aria-labelledby="action-detail-title"
            onMouseDown={(event) =>
              event.stopPropagation()
            }
          >
            <div className="modal-heading">
              <div>
                <span className="eyebrow">
                  AKSİYON DETAYI
                </span>

                <h2 id="action-detail-title">
                  {selectedActionItem.title}
                </h2>

                <p>
                  Aksiyona ait proje, sorumluluk ve takip bilgileri.
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

            <div className="action-detail-grid">
              <div className="action-detail-item">
                <span>Proje</span>
                <strong>
                  {selectedActionItem.projectName}
                </strong>
              </div>

              <div className="action-detail-item">
                <span>Haftalık rapor</span>
                <strong>
                  {getReportLabel(
                    selectedActionItem.weeklyReportId,
                  )}
                </strong>
              </div>

              <div className="action-detail-item">
                <span>Öncelik</span>
                <strong>
                  {ACTION_ITEM_PRIORITY_LABELS[
                    selectedActionItem.priority
                  ] || selectedActionItem.priority}
                </strong>
              </div>

              <div className="action-detail-item">
                <span>Durum</span>
                <StatusBadge
                  status={selectedActionItem.status}
                />
              </div>

              <div className="action-detail-item">
                <span>Hedef tarih</span>
                <strong>
                  {formatDate(
                    selectedActionItem.targetDate,
                  )}
                </strong>
              </div>

              <div className="action-detail-item">
                <span>Tamamlanma tarihi</span>
                <strong>
                  {formatDate(
                    selectedActionItem.completionDate,
                  )}
                </strong>
              </div>

              <div className="action-detail-item">
                <span>Sorumlu kullanıcı</span>
                <strong>
                  {selectedActionItem
                    .responsibleFullName || '-'}
                </strong>
              </div>

              <div className="action-detail-item">
                <span>Kullanıcı adı</span>
                <strong>
                  {selectedActionItem
                    .responsibleUsername || '-'}
                </strong>
              </div>

              <div className="action-detail-item">
                <span>Oluşturulma</span>
                <strong>
                  {formatDateTime(
                    selectedActionItem.createdAt,
                  )}
                </strong>
              </div>

              <div className="action-detail-item">
                <span>Son güncelleme</span>
                <strong>
                  {formatDateTime(
                    selectedActionItem.updatedAt,
                  )}
                </strong>
              </div>

              <div className="action-detail-item action-detail-item-wide">
                <span>Aksiyon açıklaması</span>
                <p>
                  {selectedActionItem.description}
                </p>
              </div>

              <div className="action-detail-item action-detail-item-wide">
                <span>Aksiyon notu</span>
                <p>
                  {selectedActionItem.note ||
                    'Ek aksiyon notu girilmemiş.'}
                </p>
              </div>
            </div>

            <div className="modal-actions">
              {canUpdateActionItem(
                selectedActionItem,
              ) ? (
                <button
                  type="button"
                  className="button button-primary"
                  onClick={() =>
                    openEditModal(
                      selectedActionItem,
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
      selectedActionItem ? (
        <div
          className="modal-backdrop"
          role="presentation"
          onMouseDown={closeModal}
        >
          <section
            className="modal-card delete-modal-card"
            role="dialog"
            aria-modal="true"
            aria-labelledby="delete-action-title"
            onMouseDown={(event) =>
              event.stopPropagation()
            }
          >
            <div className="modal-heading">
              <div>
                <span className="eyebrow danger-eyebrow">
                  AKSİYONU SİL
                </span>

                <h2 id="delete-action-title">
                  Aksiyon kaydı silinsin mi?
                </h2>

                <p>
                  <strong>
                    {selectedActionItem.title}
                  </strong>{' '}
                  aktif aksiyon kayıtları listesinden
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
                onClick={handleDeleteActionItem}
                disabled={isSubmitting}
              >
                {isSubmitting
                  ? 'Siliniyor...'
                  : 'Aksiyonu Sil'}
              </button>
            </div>
          </section>
        </div>
      ) : null}
    </div>
  )
}
