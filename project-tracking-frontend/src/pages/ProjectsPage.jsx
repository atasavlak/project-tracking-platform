import { useEffect, useState } from 'react'
import {
  createProject,
  deactivateProject,
  getProjects,
  updateProject,
} from '../api/projectApi.js'
import { useAuth } from '../auth/AuthContext.jsx'
import ErrorState from '../components/ErrorState.jsx'
import LoadingState from '../components/LoadingState.jsx'
import StatusBadge from '../components/StatusBadge.jsx'
import { ROLES } from '../utils/roles.js'
import {
  PROJECT_STATUSES,
  PROJECT_STATUS_LABELS,
} from '../utils/statuses.js'

const INITIAL_FORM = {
  name: '',
  description: '',
  startDate: '',
  endDate: '',
  status: 'PLANNED',
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

export default function ProjectsPage() {
  const { user } = useAuth()

  const [status, setStatus] = useState('')
  const [projects, setProjects] = useState([])
  const [error, setError] = useState('')
  const [successMessage, setSuccessMessage] =
    useState('')
  const [isLoading, setIsLoading] = useState(true)
  const [reloadKey, setReloadKey] = useState(0)

  const [activeModal, setActiveModal] = useState(null)
  const [selectedProject, setSelectedProject] =
    useState(null)
  const [form, setForm] = useState(INITIAL_FORM)
  const [formError, setFormError] = useState('')
  const [isSubmitting, setIsSubmitting] =
    useState(false)

  const canCreateProject =
    user?.role === ROLES.PROJECT_MANAGER ||
    user?.role === ROLES.ADMIN

  useEffect(() => {
    const controller = new AbortController()

    setIsLoading(true)
    setError('')

    getProjects(status || null, controller.signal)
      .then(setProjects)
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
  }, [status, reloadKey])

  useEffect(() => {
    if (!successMessage) {
      return undefined
    }

    const timeoutId = window.setTimeout(() => {
      setSuccessMessage('')
    }, 4000)

    return () => window.clearTimeout(timeoutId)
  }, [successMessage])

  function canManageProject(project) {
    if (user?.role === ROLES.ADMIN) {
      return true
    }

    return (
      user?.role === ROLES.PROJECT_MANAGER &&
      Number(user?.id) ===
        Number(project.projectManagerId)
    )
  }

  function handleFormChange(event) {
    const { name, value } = event.target

    setForm((currentForm) => ({
      ...currentForm,
      [name]: value,
    }))
  }

  function openCreateModal() {
    setSelectedProject(null)
    setForm(INITIAL_FORM)
    setFormError('')
    setSuccessMessage('')
    setActiveModal('create')
  }

  function openDetailModal(project) {
    setSelectedProject(project)
    setFormError('')
    setActiveModal('detail')
  }

  function openEditModal(project) {
    setSelectedProject(project)
    setForm({
      name: project.name || '',
      description: project.description || '',
      startDate: project.startDate || '',
      endDate: project.endDate || '',
      status: project.status || 'PLANNED',
    })
    setFormError('')
    setSuccessMessage('')
    setActiveModal('edit')
  }

  function openDeleteModal(project) {
    setSelectedProject(project)
    setFormError('')
    setSuccessMessage('')
    setActiveModal('delete')
  }

  function closeModal() {
    if (isSubmitting) {
      return
    }

    setActiveModal(null)
    setSelectedProject(null)
    setForm(INITIAL_FORM)
    setFormError('')
  }

  function validateForm() {
    const normalizedName = form.name.trim()

    if (!normalizedName) {
      return 'Proje adı zorunludur.'
    }

    if (!form.startDate) {
      return 'Başlangıç tarihi zorunludur.'
    }

    if (
      form.endDate &&
      form.endDate < form.startDate
    ) {
      return 'Bitiş tarihi başlangıç tarihinden önce olamaz.'
    }

    return ''
  }

  function createRequestBody() {
    return {
      name: form.name.trim(),
      description: form.description.trim() || null,
      startDate: form.startDate,
      endDate: form.endDate || null,
      status: form.status,
    }
  }

  async function handleCreateProject(event) {
    event.preventDefault()
    setFormError('')

    const validationError = validateForm()

    if (validationError) {
      setFormError(validationError)
      return
    }

    setIsSubmitting(true)

    try {
      await createProject(createRequestBody())

      closeModal()
      setSuccessMessage(
        'Proje başarıyla oluşturuldu.',
      )
      setReloadKey((value) => value + 1)
    } catch (requestError) {
      setFormError(getErrorMessage(requestError))
    } finally {
      setIsSubmitting(false)
    }
  }

  async function handleUpdateProject(event) {
    event.preventDefault()
    setFormError('')

    const validationError = validateForm()

    if (validationError) {
      setFormError(validationError)
      return
    }

    if (!selectedProject) {
      setFormError('Güncellenecek proje bulunamadı.')
      return
    }

    setIsSubmitting(true)

    try {
      await updateProject(
        selectedProject.id,
        createRequestBody(),
      )

      closeModal()
      setSuccessMessage(
        'Proje başarıyla güncellendi.',
      )
      setReloadKey((value) => value + 1)
    } catch (requestError) {
      setFormError(getErrorMessage(requestError))
    } finally {
      setIsSubmitting(false)
    }
  }

  async function handleDeleteProject() {
    if (!selectedProject) {
      setFormError('Silinecek proje bulunamadı.')
      return
    }

    setFormError('')
    setIsSubmitting(true)

    try {
      await deactivateProject(selectedProject.id)

      closeModal()
      setSuccessMessage(
        'Proje başarıyla silindi.',
      )
      setReloadKey((value) => value + 1)
    } catch (requestError) {
      setFormError(getErrorMessage(requestError))
    } finally {
      setIsSubmitting(false)
    }
  }

  function renderProjectForm(
    title,
    description,
    onSubmit,
    submitLabel,
  ) {
    return (
      <section
        className="modal-card"
        role="dialog"
        aria-modal="true"
        aria-labelledby="project-form-title"
        onMouseDown={(event) =>
          event.stopPropagation()
        }
      >
        <div className="modal-heading">
          <div>
            <span className="eyebrow">
              PROJE YÖNETİMİ
            </span>

            <h2 id="project-form-title">{title}</h2>

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
          className="project-form"
          onSubmit={onSubmit}
        >
          <label className="field">
            <span>Proje adı</span>

            <input
              type="text"
              name="name"
              value={form.name}
              onChange={handleFormChange}
              maxLength={200}
              placeholder="Proje adını girin"
              required
              autoFocus
            />
          </label>

          <label className="field">
            <span>Açıklama</span>

            <textarea
              name="description"
              value={form.description}
              onChange={handleFormChange}
              maxLength={2000}
              rows={5}
              placeholder="Proje açıklamasını girin"
            />
          </label>

          <div className="form-grid">
            <label className="field">
              <span>Başlangıç tarihi</span>

              <input
                type="date"
                name="startDate"
                value={form.startDate}
                onChange={handleFormChange}
                required
              />
            </label>

            <label className="field">
              <span>Bitiş tarihi</span>

              <input
                type="date"
                name="endDate"
                value={form.endDate}
                onChange={handleFormChange}
                min={form.startDate || undefined}
              />
            </label>
          </div>

          <label className="field">
            <span>Proje durumu</span>

            <select
              name="status"
              value={form.status}
              onChange={handleFormChange}
              required
            >
              {PROJECT_STATUSES.map(
                (projectStatus) => (
                  <option
                    value={projectStatus}
                    key={projectStatus}
                  >
                    {
                      PROJECT_STATUS_LABELS[
                        projectStatus
                      ]
                    }
                  </option>
                ),
              )}
            </select>
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
            PROJE YÖNETİMİ
          </span>

          <h1>Projeler</h1>

          <p>
            Rolünüze göre erişebildiğiniz aktif
            projeleri görüntüleyin.
          </p>
        </div>

        <div className="page-heading-actions">
          {canCreateProject ? (
            <button
              type="button"
              className="button button-primary"
              onClick={openCreateModal}
            >
              + Yeni Proje
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
        <LoadingState message="Projeler yükleniyor..." />
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
          <div className="table-toolbar">
            <label className="compact-field">
              <span>Duruma Göre Filtrele</span>

              <select
                value={status}
                onChange={(event) =>
                  setStatus(event.target.value)
                }
              >
                <option value="">
                  Tüm Durumlar
                </option>

                {PROJECT_STATUSES.map(
                  (projectStatus) => (
                    <option
                      value={projectStatus}
                      key={projectStatus}
                    >
                      {
                        PROJECT_STATUS_LABELS[
                          projectStatus
                        ]
                      }
                    </option>
                  ),
                )}
              </select>
            </label>
          </div>

          {projects.length === 0 ? (
            <div className="empty-state">
              Seçilen filtreye uygun proje bulunamadı.
            </div>
          ) : (
            <div className="table-scroll">
              <table>
                <thead>
                  <tr>
                    <th>Proje</th>
                    <th>Durum</th>
                    <th>Başlangıç</th>
                    <th>Bitiş</th>
                    <th>Proje Yöneticisi</th>
                    <th>İşlemler</th>
                  </tr>
                </thead>

                <tbody>
                  {projects.map((project) => (
                    <tr key={project.id}>
                      <td>
                        <div className="table-primary-cell">
                          <strong>
                            {project.name}
                          </strong>

                          <span>
                            {project.description ||
                              'Açıklama bulunmuyor.'}
                          </span>
                        </div>
                      </td>

                      <td>
                        <StatusBadge
                          status={project.status}
                        />
                      </td>

                      <td>
                        {formatDate(project.startDate)}
                      </td>

                      <td>
                        {formatDate(project.endDate)}
                      </td>

                      <td>
                        {project.projectManagerFullName ||
                          'Kullanıcı bulunamadı'}
                      </td>

                      <td>
                        <div className="table-actions">
                          <button
                            type="button"
                            className="table-action-button"
                            onClick={() =>
                              openDetailModal(project)
                            }
                          >
                            Detay
                          </button>

                          {canManageProject(project) ? (
                            <>
                              <button
                                type="button"
                                className="table-action-button"
                                onClick={() =>
                                  openEditModal(project)
                                }
                              >
                                Düzenle
                              </button>

                              <button
                                type="button"
                                className="table-action-button table-action-danger"
                                onClick={() =>
                                  openDeleteModal(project)
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
          {renderProjectForm(
            'Yeni Proje',
            'Projenin temel bilgilerini girerek yeni kayıt oluşturun.',
            handleCreateProject,
            'Projeyi Oluştur',
          )}
        </div>
      ) : null}

      {activeModal === 'edit' ? (
        <div
          className="modal-backdrop"
          role="presentation"
          onMouseDown={closeModal}
        >
          {renderProjectForm(
            'Projeyi Düzenle',
            'Projenin mevcut bilgilerini güncelleyin.',
            handleUpdateProject,
            'Değişiklikleri Kaydet',
          )}
        </div>
      ) : null}

      {activeModal === 'detail' &&
      selectedProject ? (
        <div
          className="modal-backdrop"
          role="presentation"
          onMouseDown={closeModal}
        >
          <section
            className="modal-card"
            role="dialog"
            aria-modal="true"
            aria-labelledby="project-detail-title"
            onMouseDown={(event) =>
              event.stopPropagation()
            }
          >
            <div className="modal-heading">
              <div>
                <span className="eyebrow">
                  PROJE DETAYI
                </span>

                <h2 id="project-detail-title">
                  {selectedProject.name}
                </h2>

                <p>
                  Projeye ait temel bilgiler.
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

            <div className="project-detail-grid">
              <div className="project-detail-item">
                <span>Proje yöneticisi</span>
                <strong>
                  {selectedProject
                    .projectManagerFullName ||
                    'Kullanıcı bulunamadı'}
                </strong>
              </div>

              <div className="project-detail-item">
                <span>Durum</span>
                <StatusBadge
                  status={selectedProject.status}
                />
              </div>

              <div className="project-detail-item">
                <span>Başlangıç tarihi</span>
                <strong>
                  {formatDate(
                    selectedProject.startDate,
                  )}
                </strong>
              </div>

              <div className="project-detail-item">
                <span>Bitiş tarihi</span>
                <strong>
                  {formatDate(
                    selectedProject.endDate,
                  )}
                </strong>
              </div>

              <div className="project-detail-item project-detail-item-wide">
                <span>Açıklama</span>
                <p>
                  {selectedProject.description ||
                    'Bu proje için açıklama girilmemiş.'}
                </p>
              </div>
            </div>

            <div className="modal-actions">
              {canManageProject(selectedProject) ? (
                <button
                  type="button"
                  className="button button-primary"
                  onClick={() =>
                    openEditModal(selectedProject)
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
      selectedProject ? (
        <div
          className="modal-backdrop"
          role="presentation"
          onMouseDown={closeModal}
        >
          <section
            className="modal-card delete-modal-card"
            role="dialog"
            aria-modal="true"
            aria-labelledby="delete-project-title"
            onMouseDown={(event) =>
              event.stopPropagation()
            }
          >
            <div className="modal-heading">
              <div>
                <span className="eyebrow danger-eyebrow">
                  PROJEYİ SİL
                </span>

                <h2 id="delete-project-title">
                  Proje silinsin mi?
                </h2>

                <p>
                  <strong>
                    {selectedProject.name}
                  </strong>{' '}
                  projesi aktif proje listesinden
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
                onClick={handleDeleteProject}
                disabled={isSubmitting}
              >
                {isSubmitting
                  ? 'Siliniyor...'
                  : 'Projeyi Sil'}
              </button>
            </div>
          </section>
        </div>
      ) : null}
    </div>
  )
}