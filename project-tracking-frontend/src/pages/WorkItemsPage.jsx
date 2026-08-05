import { useEffect, useMemo, useState } from 'react'
import { getProjects } from '../api/projectApi.js'
import { getWeeklyReports } from '../api/weeklyReportApi.js'
import {
  createWorkItem,
  deactivateWorkItem,
  getWorkItems,
  updateWorkItem,
} from '../api/workItemApi.js'
import { useAuth } from '../auth/AuthContext.jsx'
import ErrorState from '../components/ErrorState.jsx'
import LoadingState from '../components/LoadingState.jsx'
import StatusBadge from '../components/StatusBadge.jsx'
import { ROLES } from '../utils/roles.js'
import {
  WORK_ITEM_STATUSES,
  WORK_ITEM_STATUS_LABELS,
} from '../utils/statuses.js'

const INITIAL_FILTERS = {
  projectId: '',
  weeklyReportId: '',
  status: '',
  responsiblePerson: '',
}

const INITIAL_FORM = {
  projectId: '',
  weeklyReportId: '',
  title: '',
  status: 'PLANNED',
  responsiblePerson: '',
  plannedWork: '',
  completedWork: '',
  description: '',
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

export default function WorkItemsPage() {
  const { user } = useAuth()

  const [projects, setProjects] = useState([])
  const [reports, setReports] = useState([])
  const [workItems, setWorkItems] = useState([])

  const [filterDraft, setFilterDraft] =
    useState(INITIAL_FILTERS)
  const [filters, setFilters] =
    useState(INITIAL_FILTERS)

  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState('')
  const [reloadKey, setReloadKey] = useState(0)
  const [successMessage, setSuccessMessage] =
    useState('')

  const [activeModal, setActiveModal] = useState(null)
  const [selectedWorkItem, setSelectedWorkItem] =
    useState(null)
  const [form, setForm] = useState(INITIAL_FORM)
  const [formError, setFormError] = useState('')
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
      getWorkItems(filters, controller.signal),
    ])
      .then(
        ([
          projectResponse,
          reportResponse,
          workItemResponse,
        ]) => {
          setProjects(projectResponse)
          setReports(reportResponse)
          setWorkItems(workItemResponse)
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
        Number(report.id) === Number(weeklyReportId),
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

    setFilters({
      ...filterDraft,
      responsiblePerson:
        filterDraft.responsiblePerson.trim(),
    })
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
            Number(report.projectId) === Number(value),
        )

        updatedForm.weeklyReportId =
          projectReports.length > 0
            ? String(projectReports[0].id)
            : ''
      }

      return updatedForm
    })
  }

  function resetModal() {
    setActiveModal(null)
    setSelectedWorkItem(null)
    setForm(INITIAL_FORM)
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

    const firstProjectReports = firstProject
      ? reports.filter(
          (report) =>
            Number(report.projectId) ===
            Number(firstProject.id),
        )
      : []

    setSelectedWorkItem(null)
    setForm({
      ...INITIAL_FORM,
      projectId: firstProject
        ? String(firstProject.id)
        : '',
      weeklyReportId:
        firstProjectReports.length > 0
          ? String(firstProjectReports[0].id)
          : '',
    })
    setFormError('')
    setSuccessMessage('')
    setActiveModal('create')
  }

  function openDetailModal(workItem) {
    setSelectedWorkItem(workItem)
    setFormError('')
    setActiveModal('detail')
  }

  function openEditModal(workItem) {
    setSelectedWorkItem(workItem)
    setForm({
      projectId: String(workItem.projectId),
      weeklyReportId: String(
        workItem.weeklyReportId,
      ),
      title: workItem.title || '',
      status: workItem.status || 'PLANNED',
      responsiblePerson:
        workItem.responsiblePerson || '',
      plannedWork: workItem.plannedWork || '',
      completedWork: workItem.completedWork || '',
      description: workItem.description || '',
    })
    setFormError('')
    setSuccessMessage('')
    setActiveModal('edit')
  }

  function openDeleteModal(workItem) {
    setSelectedWorkItem(workItem)
    setFormError('')
    setSuccessMessage('')
    setActiveModal('delete')
  }

  function validateForm(isEdit) {
    if (!isEdit && !form.weeklyReportId) {
      return 'Haftalık rapor seçimi zorunludur.'
    }

    if (!form.title.trim()) {
      return 'İş kalemi başlığı zorunludur.'
    }

    if (!form.responsiblePerson.trim()) {
      return 'Sorumlu kişi zorunludur.'
    }

    if (!form.plannedWork.trim()) {
      return 'Planlanan çalışma zorunludur.'
    }

    return ''
  }

  function createRequestBody() {
    return {
      weeklyReportId: Number(form.weeklyReportId),
      title: form.title.trim(),
      status: form.status,
      responsiblePerson:
        form.responsiblePerson.trim(),
      plannedWork: form.plannedWork.trim(),
      completedWork: normalizeOptionalText(
        form.completedWork,
      ),
      description: normalizeOptionalText(
        form.description,
      ),
    }
  }

  function createUpdateRequestBody() {
    return {
      title: form.title.trim(),
      status: form.status,
      responsiblePerson:
        form.responsiblePerson.trim(),
      plannedWork: form.plannedWork.trim(),
      completedWork: normalizeOptionalText(
        form.completedWork,
      ),
      description: normalizeOptionalText(
        form.description,
      ),
    }
  }

  async function handleCreateWorkItem(event) {
    event.preventDefault()
    setFormError('')

    const validationError = validateForm(false)

    if (validationError) {
      setFormError(validationError)
      return
    }

    setIsSubmitting(true)

    try {
      await createWorkItem(createRequestBody())

      resetModal()
      setSuccessMessage(
        'İş kalemi başarıyla oluşturuldu.',
      )
      setReloadKey((value) => value + 1)
    } catch (requestError) {
      setFormError(getErrorMessage(requestError))
    } finally {
      setIsSubmitting(false)
    }
  }

  async function handleUpdateWorkItem(event) {
    event.preventDefault()
    setFormError('')

    const validationError = validateForm(true)

    if (validationError) {
      setFormError(validationError)
      return
    }

    if (!selectedWorkItem) {
      setFormError(
        'Güncellenecek iş kalemi bulunamadı.',
      )
      return
    }

    setIsSubmitting(true)

    try {
      await updateWorkItem(
        selectedWorkItem.id,
        createUpdateRequestBody(),
      )

      resetModal()
      setSuccessMessage(
        'İş kalemi başarıyla güncellendi.',
      )
      setReloadKey((value) => value + 1)
    } catch (requestError) {
      setFormError(getErrorMessage(requestError))
    } finally {
      setIsSubmitting(false)
    }
  }

  async function handleDeleteWorkItem() {
    if (!selectedWorkItem) {
      setFormError('Silinecek iş kalemi bulunamadı.')
      return
    }

    setFormError('')
    setIsSubmitting(true)

    try {
      await deactivateWorkItem(selectedWorkItem.id)

      resetModal()
      setSuccessMessage(
        'İş kalemi başarıyla silindi.',
      )
      setReloadKey((value) => value + 1)
    } catch (requestError) {
      setFormError(getErrorMessage(requestError))
    } finally {
      setIsSubmitting(false)
    }
  }

  function renderWorkItemForm({
    title,
    description,
    submitLabel,
    onSubmit,
    isEdit = false,
  }) {
    return (
      <section
        className="modal-card work-modal-card"
        role="dialog"
        aria-modal="true"
        aria-labelledby="work-item-form-title"
        onMouseDown={(event) =>
          event.stopPropagation()
        }
      >
        <div className="modal-heading">
          <div>
            <span className="eyebrow">
              İŞ KALEMİ YÖNETİMİ
            </span>

            <h2 id="work-item-form-title">{title}</h2>

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
          className="work-item-form"
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

            <label className="field">
              <span>Haftalık rapor</span>

              <select
                name="weeklyReportId"
                value={form.weeklyReportId}
                onChange={handleFormChange}
                disabled={isEdit}
                required
              >
                <option value="">Rapor seçin</option>

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
              İş kaleminin bağlı olduğu proje ve rapor
              değiştirilemez.
            </p>
          ) : null}

          <label className="field">
            <span>İş kalemi başlığı</span>

            <input
              type="text"
              name="title"
              value={form.title}
              onChange={handleFormChange}
              maxLength={200}
              placeholder="İş kalemi başlığını girin"
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
                {WORK_ITEM_STATUSES.map(
                  (workItemStatus) => (
                    <option
                      key={workItemStatus}
                      value={workItemStatus}
                    >
                      {
                        WORK_ITEM_STATUS_LABELS[
                          workItemStatus
                        ]
                      }
                    </option>
                  ),
                )}
              </select>
            </label>

            <label className="field">
              <span>Sorumlu kişi</span>

              <input
                type="text"
                name="responsiblePerson"
                value={form.responsiblePerson}
                onChange={handleFormChange}
                maxLength={150}
                placeholder="Sorumlu kişinin adı"
                required
              />
            </label>
          </div>

          <label className="field">
            <span>Planlanan çalışma</span>

            <textarea
              name="plannedWork"
              value={form.plannedWork}
              onChange={handleFormChange}
              maxLength={2000}
              rows={4}
              placeholder="Planlanan çalışmayı açıklayın"
              required
            />
          </label>

          <label className="field">
            <span>Tamamlanan çalışma</span>

            <textarea
              name="completedWork"
              value={form.completedWork}
              onChange={handleFormChange}
              maxLength={2000}
              rows={4}
              placeholder="Tamamlanan çalışmayı açıklayın"
            />
          </label>

          <label className="field">
            <span>Ek açıklama</span>

            <textarea
              name="description"
              value={form.description}
              onChange={handleFormChange}
              maxLength={2000}
              rows={3}
              placeholder="Ek açıklama girin"
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
            İŞ KALEMİ YÖNETİMİ
          </span>

          <h1>İş Kalemleri</h1>

          <p>
            Haftalık raporlara bağlı planlanan,
            tamamlanan ve riskli işleri takip edin.
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
              + Yeni İş Kalemi
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
        <LoadingState message="İş kalemleri yükleniyor..." />
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
            className="work-filter-toolbar"
            onSubmit={applyFilters}
          >
            <div className="work-filter-grid">
              <label className="report-filter-field">
                <span>Proje</span>

                <select
                  name="projectId"
                  value={filterDraft.projectId}
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
                <span>Haftalık rapor</span>

                <select
                  name="weeklyReportId"
                  value={filterDraft.weeklyReportId}
                  onChange={handleFilterChange}
                >
                  <option value="">Tüm Raporlar</option>

                  {filteredReportOptions.map((report) => (
                    <option
                      key={report.id}
                      value={report.id}
                    >
                      {formatDate(report.weekStartDate)}
                    </option>
                  ))}
                </select>
              </label>

              <label className="report-filter-field">
                <span>Durum</span>

                <select
                  name="status"
                  value={filterDraft.status}
                  onChange={handleFilterChange}
                >
                  <option value="">Tüm Durumlar</option>

                  {WORK_ITEM_STATUSES.map(
                    (workItemStatus) => (
                      <option
                        key={workItemStatus}
                        value={workItemStatus}
                      >
                        {
                          WORK_ITEM_STATUS_LABELS[
                            workItemStatus
                          ]
                        }
                      </option>
                    ),
                  )}
                </select>
              </label>

              <label className="report-filter-field">
                <span>Sorumlu kişi</span>

                <input
                  type="text"
                  name="responsiblePerson"
                  value={filterDraft.responsiblePerson}
                  onChange={handleFilterChange}
                  placeholder="İsim ile ara"
                />
              </label>
            </div>

            <div className="work-filter-actions">
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

          {workItems.length === 0 ? (
            <div className="empty-state">
              Seçilen filtrelere uygun iş kalemi
              bulunamadı.
            </div>
          ) : (
            <div className="table-scroll">
              <table>
                <thead>
                  <tr>
                    <th>İş Kalemi</th>
                    <th>Proje / Rapor</th>
                    <th>Durum</th>
                    <th>Sorumlu</th>
                    <th>Planlanan Çalışma</th>
                    <th>İşlemler</th>
                  </tr>
                </thead>

                <tbody>
                  {workItems.map((workItem) => (
                    <tr key={workItem.id}>
                      <td>
                        <div className="table-primary-cell">
                          <strong>
                            {workItem.title}
                          </strong>
                        </div>
                      </td>

                      <td>
                        <div className="work-report-cell">
                          <strong>
                            {workItem.projectName}
                          </strong>

                          <span>
                            {getReportLabel(
                              workItem.weeklyReportId,
                            )}
                          </span>
                        </div>
                      </td>

                      <td>
                        <StatusBadge
                          status={workItem.status}
                        />
                      </td>

                      <td>
                        {workItem.responsiblePerson}
                      </td>

                      <td>
                        <p className="work-table-text">
                          {workItem.plannedWork}
                        </p>
                      </td>

                      <td>
                        <div className="table-actions">
                          <button
                            type="button"
                            className="table-action-button"
                            onClick={() =>
                              openDetailModal(workItem)
                            }
                          >
                            Detay
                          </button>

                          {canUpdate ? (
                            <button
                              type="button"
                              className="table-action-button"
                              onClick={() =>
                                openEditModal(workItem)
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
                                openDeleteModal(workItem)
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
          {renderWorkItemForm({
            title: 'Yeni İş Kalemi',
            description:
              'Haftalık rapora bağlı yeni bir iş kalemi oluşturun.',
            submitLabel: 'İş Kalemini Oluştur',
            onSubmit: handleCreateWorkItem,
          })}
        </div>
      ) : null}

      {activeModal === 'edit' ? (
        <div
          className="modal-backdrop"
          role="presentation"
          onMouseDown={closeModal}
        >
          {renderWorkItemForm({
            title: 'İş Kalemini Düzenle',
            description:
              'İş kaleminin mevcut bilgilerini güncelleyin.',
            submitLabel: 'Değişiklikleri Kaydet',
            onSubmit: handleUpdateWorkItem,
            isEdit: true,
          })}
        </div>
      ) : null}

      {activeModal === 'detail' &&
      selectedWorkItem ? (
        <div
          className="modal-backdrop"
          role="presentation"
          onMouseDown={closeModal}
        >
          <section
            className="modal-card work-modal-card"
            role="dialog"
            aria-modal="true"
            aria-labelledby="work-item-detail-title"
            onMouseDown={(event) =>
              event.stopPropagation()
            }
          >
            <div className="modal-heading">
              <div>
                <span className="eyebrow">
                  İŞ KALEMİ DETAYI
                </span>

                <h2 id="work-item-detail-title">
                  {selectedWorkItem.title}
                </h2>

                <p>
                  İş kalemine ait tüm bilgiler.
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

            <div className="work-detail-grid">
              <div className="work-detail-item">
                <span>Proje</span>
                <strong>
                  {selectedWorkItem.projectName}
                </strong>
              </div>

              <div className="work-detail-item">
                <span>Haftalık rapor</span>
                <strong>
                  {getReportLabel(
                    selectedWorkItem.weeklyReportId,
                  )}
                </strong>
              </div>

              <div className="work-detail-item">
                <span>Durum</span>
                <StatusBadge
                  status={selectedWorkItem.status}
                />
              </div>

              <div className="work-detail-item">
                <span>Sorumlu kişi</span>
                <strong>
                  {selectedWorkItem.responsiblePerson}
                </strong>
              </div>

              <div className="work-detail-item work-detail-item-wide">
                <span>Planlanan çalışma</span>
                <p>
                  {selectedWorkItem.plannedWork}
                </p>
              </div>

              <div className="work-detail-item work-detail-item-wide">
                <span>Tamamlanan çalışma</span>
                <p>
                  {selectedWorkItem.completedWork ||
                    'Bilgi girilmemiş.'}
                </p>
              </div>

              <div className="work-detail-item work-detail-item-wide">
                <span>Ek açıklama</span>
                <p>
                  {selectedWorkItem.description ||
                    'Bilgi girilmemiş.'}
                </p>
              </div>
            </div>

            <div className="modal-actions">
              {canUpdate ? (
                <button
                  type="button"
                  className="button button-primary"
                  onClick={() =>
                    openEditModal(selectedWorkItem)
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
      selectedWorkItem ? (
        <div
          className="modal-backdrop"
          role="presentation"
          onMouseDown={closeModal}
        >
          <section
            className="modal-card delete-modal-card"
            role="dialog"
            aria-modal="true"
            aria-labelledby="delete-work-item-title"
            onMouseDown={(event) =>
              event.stopPropagation()
            }
          >
            <div className="modal-heading">
              <div>
                <span className="eyebrow danger-eyebrow">
                  İŞ KALEMİNİ SİL
                </span>

                <h2 id="delete-work-item-title">
                  İş kalemi silinsin mi?
                </h2>

                <p>
                  <strong>
                    {selectedWorkItem.title}
                  </strong>{' '}
                  aktif iş kalemi listesinden
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
                onClick={handleDeleteWorkItem}
                disabled={isSubmitting}
              >
                {isSubmitting
                  ? 'Siliniyor...'
                  : 'İş Kalemini Sil'}
              </button>
            </div>
          </section>
        </div>
      ) : null}
    </div>
  )
}