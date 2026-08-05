import {
  useEffect,
  useMemo,
  useState,
} from 'react'
import {
  createAdminUser,
  getAdminUsers,
  resendAdminUserActivation,
  updateAdminUserRole,
  updateAdminUserStatus,
} from '../api/adminUserApi.js'
import { useAuth } from '../auth/AuthContext.jsx'
import ErrorState from '../components/ErrorState.jsx'
import LoadingState from '../components/LoadingState.jsx'
import {
  ROLES,
  ROLE_LABELS,
} from '../utils/roles.js'

const INITIAL_FORM = {
  username: '',
  fullName: '',
  email: '',
  phoneNumber: '',
  role: ROLES.PROJECT_MANAGER,
}

const ROLE_OPTIONS = Object.values(ROLES)

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

function formatDateTime(value) {
  if (!value) {
    return '-'
  }

  return new Intl.DateTimeFormat('tr-TR', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(new Date(value))
}

function getAccountStatus(user) {
  if (user.active) {
    return {
      label: 'Aktif',
      className: 'account-status-active',
    }
  }

  if (!user.activationCompleted) {
    return {
      label: 'Aktivasyon Bekliyor',
      className: 'account-status-pending',
    }
  }

  return {
    label: 'Pasif',
    className: 'account-status-inactive',
  }
}

function getActivationStatus(user) {
  if (user.activationCompleted) {
    return {
      label: 'Tamamlandı',
      className: 'activation-status-completed',
    }
  }

  return {
    label: 'Bekliyor',
    className: 'activation-status-pending',
  }
}

export default function AdminUsersPage() {
  const { user: currentUser } = useAuth()

  const [users, setUsers] = useState([])
  const [searchText, setSearchText] = useState('')
  const [roleFilter, setRoleFilter] = useState('')
  const [statusFilter, setStatusFilter] = useState('')
  const [activationFilter, setActivationFilter] =
    useState('')
  const [error, setError] = useState('')
  const [successMessage, setSuccessMessage] =
    useState('')
  const [isLoading, setIsLoading] = useState(true)
  const [reloadKey, setReloadKey] = useState(0)

  const [activeModal, setActiveModal] = useState(null)
  const [selectedUser, setSelectedUser] = useState(null)
  const [form, setForm] = useState(INITIAL_FORM)
  const [selectedRole, setSelectedRole] = useState('')
  const [formError, setFormError] = useState('')
  const [isSubmitting, setIsSubmitting] =
    useState(false)

  useEffect(() => {
    const controller = new AbortController()

    setIsLoading(true)
    setError('')

    getAdminUsers(controller.signal)
      .then(setUsers)
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
  }, [reloadKey])

  useEffect(() => {
    if (!successMessage) {
      return undefined
    }

    const timeoutId = window.setTimeout(() => {
      setSuccessMessage('')
    }, 4000)

    return () => window.clearTimeout(timeoutId)
  }, [successMessage])

  const filteredUsers = useMemo(() => {
    const normalizedSearch = searchText
      .trim()
      .toLocaleLowerCase('tr-TR')

    return users.filter((user) => {
      const matchesSearch =
        !normalizedSearch ||
        [
          user.fullName,
          user.username,
          user.email,
          user.phoneNumber,
        ]
          .filter(Boolean)
          .some((value) =>
            String(value)
              .toLocaleLowerCase('tr-TR')
              .includes(normalizedSearch),
          )

      const matchesRole =
        !roleFilter || user.role === roleFilter

      const matchesStatus =
        !statusFilter ||
        (statusFilter === 'ACTIVE' && user.active) ||
        (statusFilter === 'INACTIVE' && !user.active)

      const matchesActivation =
        !activationFilter ||
        (activationFilter === 'COMPLETED' &&
          user.activationCompleted) ||
        (activationFilter === 'PENDING' &&
          !user.activationCompleted)

      return (
        matchesSearch &&
        matchesRole &&
        matchesStatus &&
        matchesActivation
      )
    })
  }, [
    activationFilter,
    roleFilter,
    searchText,
    statusFilter,
    users,
  ])

  const metrics = useMemo(
    () => ({
      total: users.length,
      active: users.filter((user) => user.active).length,
      pendingActivation: users.filter(
        (user) => !user.activationCompleted,
      ).length,
      admins: users.filter(
        (user) => user.role === ROLES.ADMIN,
      ).length,
    }),
    [users],
  )

  function resetModal() {
    setActiveModal(null)
    setSelectedUser(null)
    setForm(INITIAL_FORM)
    setSelectedRole('')
    setFormError('')
  }

  function closeModal() {
    if (!isSubmitting) {
      resetModal()
    }
  }

  function openCreateModal() {
    setForm(INITIAL_FORM)
    setSelectedUser(null)
    setFormError('')
    setSuccessMessage('')
    setActiveModal('create')
  }

  function openDetailModal(user) {
    setSelectedUser(user)
    setFormError('')
    setActiveModal('detail')
  }

  function openRoleModal(user) {
    setSelectedUser(user)
    setSelectedRole(user.role)
    setFormError('')
    setSuccessMessage('')
    setActiveModal('role')
  }

  function openStatusModal(user) {
    setSelectedUser(user)
    setFormError('')
    setSuccessMessage('')
    setActiveModal('status')
  }

  function openActivationModal(user) {
    setSelectedUser(user)
    setFormError('')
    setSuccessMessage('')
    setActiveModal('activation')
  }

  function handleFormChange(event) {
    const { name, value } = event.target

    setForm((currentForm) => ({
      ...currentForm,
      [name]: value,
    }))
  }

  function clearFilters() {
    setSearchText('')
    setRoleFilter('')
    setStatusFilter('')
    setActivationFilter('')
  }

  function validateCreateForm() {
    if (form.username.trim().length < 3) {
      return 'Kullanıcı adı en az 3 karakter olmalıdır.'
    }

    if (form.fullName.trim().length < 2) {
      return 'Ad soyad en az 2 karakter olmalıdır.'
    }

    if (!/^\S+@\S+\.\S+$/.test(form.email.trim())) {
      return 'Geçerli bir e-posta adresi giriniz.'
    }

    if (
      form.phoneNumber.trim() &&
      !/^\+?[0-9]{10,15}$/.test(
        form.phoneNumber.trim(),
      )
    ) {
      return 'Telefon numarası 10 ile 15 rakam arasında olmalıdır.'
    }

    if (!form.role) {
      return 'Kullanıcı rolü zorunludur.'
    }

    return ''
  }

  async function handleCreateUser(event) {
    event.preventDefault()
    setFormError('')

    const validationError = validateCreateForm()

    if (validationError) {
      setFormError(validationError)
      return
    }

    setIsSubmitting(true)

    try {
      await createAdminUser({
        username: form.username.trim(),
        fullName: form.fullName.trim(),
        email: form.email.trim().toLowerCase(),
        phoneNumber: form.phoneNumber.trim() || null,
        role: form.role,
      })

      resetModal()
      setSuccessMessage(
        'Kullanıcı oluşturuldu ve aktivasyon e-postası gönderildi.',
      )
      setReloadKey((value) => value + 1)
    } catch (requestError) {
      setFormError(getErrorMessage(requestError))
    } finally {
      setIsSubmitting(false)
    }
  }

  async function handleRoleUpdate(event) {
    event.preventDefault()
    setFormError('')

    if (!selectedUser) {
      setFormError('Güncellenecek kullanıcı bulunamadı.')
      return
    }

    if (!selectedRole) {
      setFormError('Kullanıcı rolü zorunludur.')
      return
    }

    if (selectedRole === selectedUser.role) {
      setFormError('Yeni rol mevcut rolden farklı olmalıdır.')
      return
    }

    setIsSubmitting(true)

    try {
      await updateAdminUserRole(
        selectedUser.id,
        selectedRole,
      )

      resetModal()
      setSuccessMessage(
        'Kullanıcı rolü başarıyla güncellendi.',
      )
      setReloadKey((value) => value + 1)
    } catch (requestError) {
      setFormError(getErrorMessage(requestError))
    } finally {
      setIsSubmitting(false)
    }
  }

  async function handleStatusUpdate() {
    if (!selectedUser) {
      setFormError('Güncellenecek kullanıcı bulunamadı.')
      return
    }

    setFormError('')
    setIsSubmitting(true)

    try {
      await updateAdminUserStatus(
        selectedUser.id,
        !selectedUser.active,
      )

      resetModal()
      setSuccessMessage(
        selectedUser.active
          ? 'Kullanıcı hesabı pasife alındı.'
          : 'Kullanıcı hesabı aktifleştirildi.',
      )
      setReloadKey((value) => value + 1)
    } catch (requestError) {
      setFormError(getErrorMessage(requestError))
    } finally {
      setIsSubmitting(false)
    }
  }

  async function handleResendActivation() {
    if (!selectedUser) {
      setFormError('Aktivasyon gönderilecek kullanıcı bulunamadı.')
      return
    }

    setFormError('')
    setIsSubmitting(true)

    try {
      await resendAdminUserActivation(selectedUser.id)

      resetModal()
      setSuccessMessage(
        'Aktivasyon e-postası yeniden gönderildi.',
      )
      setReloadKey((value) => value + 1)
    } catch (requestError) {
      setFormError(getErrorMessage(requestError))
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div className="page-stack">
      <section className="page-heading">
        <div>
          <span className="eyebrow">ADMIN PANELİ</span>
          <h1>Kullanıcı Yönetimi</h1>
          <p>
            Kullanıcıları oluşturun, rollerini ve hesap
            durumlarını yönetin.
          </p>
        </div>

        <div className="page-heading-actions">
          <button
            className="button button-primary"
            type="button"
            onClick={openCreateModal}
          >
            Yeni Kullanıcı
          </button>
        </div>
      </section>

      {successMessage ? (
        <div className="page-success-message">
          {successMessage}
        </div>
      ) : null}

      <section className="metric-grid admin-user-metric-grid">
        <article className="metric-card">
          <span>Toplam Kullanıcı</span>
          <strong>{metrics.total}</strong>
          <small>Sistemdeki bütün hesaplar</small>
        </article>
        <article className="metric-card">
          <span>Aktif Kullanıcı</span>
          <strong>{metrics.active}</strong>
          <small>Giriş yapabilen hesaplar</small>
        </article>
        <article className="metric-card metric-card-warning">
          <span>Aktivasyon Bekliyor</span>
          <strong>{metrics.pendingActivation}</strong>
          <small>İlk parola henüz belirlenmedi</small>
        </article>
        <article className="metric-card">
          <span>Admin</span>
          <strong>{metrics.admins}</strong>
          <small>Tam yetkili kullanıcılar</small>
        </article>
      </section>

      <section className="content-card table-card">
        <div className="admin-user-filter-toolbar">
          <div className="admin-user-filter-grid">
            <label className="compact-field admin-user-search-field">
              <span>Ara</span>
              <input
                type="search"
                placeholder="Ad, kullanıcı adı veya e-posta"
                value={searchText}
                onChange={(event) =>
                  setSearchText(event.target.value)
                }
              />
            </label>

            <label className="compact-field">
              <span>Rol</span>
              <select
                value={roleFilter}
                onChange={(event) =>
                  setRoleFilter(event.target.value)
                }
              >
                <option value="">Tümü</option>
                {ROLE_OPTIONS.map((role) => (
                  <option key={role} value={role}>
                    {ROLE_LABELS[role]}
                  </option>
                ))}
              </select>
            </label>

            <label className="compact-field">
              <span>Hesap Durumu</span>
              <select
                value={statusFilter}
                onChange={(event) =>
                  setStatusFilter(event.target.value)
                }
              >
                <option value="">Tümü</option>
                <option value="ACTIVE">Aktif</option>
                <option value="INACTIVE">Pasif</option>
              </select>
            </label>

            <label className="compact-field">
              <span>Aktivasyon</span>
              <select
                value={activationFilter}
                onChange={(event) =>
                  setActivationFilter(event.target.value)
                }
              >
                <option value="">Tümü</option>
                <option value="COMPLETED">Tamamlandı</option>
                <option value="PENDING">Bekliyor</option>
              </select>
            </label>
          </div>

          <div className="admin-user-filter-actions">
            <span className="admin-user-result-count">
              {filteredUsers.length} kayıt
            </span>
            <button
              className="button button-secondary"
              type="button"
              onClick={clearFilters}
            >
              Temizle
            </button>
          </div>
        </div>

        {isLoading ? (
          <LoadingState message="Kullanıcılar yükleniyor..." />
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
          <div className="table-scroll">
            <table>
              <thead>
                <tr>
                  <th>Kullanıcı</th>
                  <th>İletişim</th>
                  <th>Rol</th>
                  <th>Hesap Durumu</th>
                  <th>Aktivasyon</th>
                  <th>Oluşturulma</th>
                  <th>İşlemler</th>
                </tr>
              </thead>
              <tbody>
                {filteredUsers.map((user) => {
                  const accountStatus =
                    getAccountStatus(user)
                  const activationStatus =
                    getActivationStatus(user)
                  const isCurrentUser =
                    Number(currentUser?.id) ===
                    Number(user.id)

                  return (
                    <tr key={user.id}>
                      <td className="table-primary-cell">
                        <strong>{user.fullName}</strong>
                        <span>@{user.username}</span>
                      </td>
                      <td className="admin-user-contact-cell">
                        <strong>{user.email}</strong>
                        <span>{user.phoneNumber || '-'}</span>
                      </td>
                      <td>
                        <span
                          className={`user-role-badge user-role-${user.role.toLowerCase()}`}
                        >
                          {ROLE_LABELS[user.role] || user.role}
                        </span>
                      </td>
                      <td>
                        <span
                          className={`account-status-badge ${accountStatus.className}`}
                        >
                          {accountStatus.label}
                        </span>
                      </td>
                      <td>
                        <span
                          className={`activation-status-badge ${activationStatus.className}`}
                        >
                          {activationStatus.label}
                        </span>
                      </td>
                      <td>{formatDateTime(user.createdAt)}</td>
                      <td>
                        <div className="table-actions admin-user-table-actions">
                          <button
                            className="table-action-button"
                            type="button"
                            onClick={() => openDetailModal(user)}
                          >
                            Detay
                          </button>
                          <button
                            className="table-action-button"
                            type="button"
                            disabled={isCurrentUser}
                            title={
                              isCurrentUser
                                ? 'Kendi ADMIN rolünüzü değiştiremezsiniz.'
                                : undefined
                            }
                            onClick={() => openRoleModal(user)}
                          >
                            Rol
                          </button>
                          <button
                            className={
                              user.active
                                ? 'table-action-button table-action-danger'
                                : 'table-action-button'
                            }
                            type="button"
                            disabled={
                              isCurrentUser && user.active
                            }
                            title={
                              isCurrentUser && user.active
                                ? 'Kendi hesabınızı pasife alamazsınız.'
                                : undefined
                            }
                            onClick={() => openStatusModal(user)}
                          >
                            {user.active
                              ? 'Pasife Al'
                              : 'Aktifleştir'}
                          </button>
                          {!user.activationCompleted ? (
                            <button
                              className="table-action-button"
                              type="button"
                              onClick={() =>
                                openActivationModal(user)
                              }
                            >
                              Aktivasyon
                            </button>
                          ) : null}
                        </div>
                      </td>
                    </tr>
                  )
                })}

                {filteredUsers.length === 0 ? (
                  <tr>
                    <td colSpan="7">
                      <div className="empty-state">
                        Filtrelere uygun kullanıcı bulunamadı.
                      </div>
                    </td>
                  </tr>
                ) : null}
              </tbody>
            </table>
          </div>
        ) : null}
      </section>

      {activeModal === 'create' ? (
        <div className="modal-backdrop" role="presentation">
          <section
            className="modal-card admin-user-modal-card"
            role="dialog"
            aria-modal="true"
            aria-labelledby="create-user-title"
          >
            <div className="modal-heading">
              <div>
                <span className="eyebrow">YENİ HESAP</span>
                <h2 id="create-user-title">
                  Kullanıcı Oluştur
                </h2>
                <p>
                  Kullanıcıya aktivasyon e-postası otomatik
                  olarak gönderilir.
                </p>
              </div>
              <button
                className="modal-close-button"
                type="button"
                disabled={isSubmitting}
                onClick={closeModal}
                aria-label="Pencereyi kapat"
              >
                ×
              </button>
            </div>

            <form
              className="admin-user-form"
              onSubmit={handleCreateUser}
            >
              <div className="form-grid">
                <label className="field">
                  <span>Kullanıcı Adı</span>
                  <input
                    name="username"
                    type="text"
                    minLength="3"
                    maxLength="100"
                    autoComplete="off"
                    value={form.username}
                    onChange={handleFormChange}
                    required
                  />
                </label>

                <label className="field">
                  <span>Ad Soyad</span>
                  <input
                    name="fullName"
                    type="text"
                    minLength="2"
                    maxLength="150"
                    autoComplete="name"
                    value={form.fullName}
                    onChange={handleFormChange}
                    required
                  />
                </label>

                <label className="field">
                  <span>E-posta</span>
                  <input
                    name="email"
                    type="email"
                    maxLength="150"
                    autoComplete="email"
                    value={form.email}
                    onChange={handleFormChange}
                    required
                  />
                </label>

                <label className="field">
                  <span>Telefon</span>
                  <input
                    name="phoneNumber"
                    type="tel"
                    maxLength="20"
                    placeholder="+905551112233"
                    autoComplete="tel"
                    value={form.phoneNumber}
                    onChange={handleFormChange}
                  />
                </label>
              </div>

              <label className="field">
                <span>Rol</span>
                <select
                  name="role"
                  value={form.role}
                  onChange={handleFormChange}
                  required
                >
                  {ROLE_OPTIONS.map((role) => (
                    <option key={role} value={role}>
                      {ROLE_LABELS[role]}
                    </option>
                  ))}
                </select>
              </label>

              {formError ? (
                <div className="form-error">{formError}</div>
              ) : null}

              <div className="modal-actions">
                <button
                  className="button button-secondary"
                  type="button"
                  disabled={isSubmitting}
                  onClick={closeModal}
                >
                  Vazgeç
                </button>
                <button
                  className="button button-primary"
                  type="submit"
                  disabled={isSubmitting}
                >
                  {isSubmitting
                    ? 'Oluşturuluyor...'
                    : 'Kullanıcı Oluştur'}
                </button>
              </div>
            </form>
          </section>
        </div>
      ) : null}

      {activeModal === 'detail' && selectedUser ? (
        <div className="modal-backdrop" role="presentation">
          <section
            className="modal-card admin-user-modal-card"
            role="dialog"
            aria-modal="true"
            aria-labelledby="user-detail-title"
          >
            <div className="modal-heading">
              <div>
                <span className="eyebrow">KULLANICI DETAYI</span>
                <h2 id="user-detail-title">
                  {selectedUser.fullName}
                </h2>
                <p>@{selectedUser.username}</p>
              </div>
              <button
                className="modal-close-button"
                type="button"
                onClick={closeModal}
                aria-label="Pencereyi kapat"
              >
                ×
              </button>
            </div>

            <div className="admin-user-detail-grid">
              <div className="admin-user-detail-item">
                <span>E-posta</span>
                <strong>{selectedUser.email}</strong>
              </div>
              <div className="admin-user-detail-item">
                <span>Telefon</span>
                <strong>
                  {selectedUser.phoneNumber || '-'}
                </strong>
              </div>
              <div className="admin-user-detail-item">
                <span>Rol</span>
                <strong>
                  {ROLE_LABELS[selectedUser.role] ||
                    selectedUser.role}
                </strong>
              </div>
              <div className="admin-user-detail-item">
                <span>Hesap Durumu</span>
                <strong>
                  {getAccountStatus(selectedUser).label}
                </strong>
              </div>
              <div className="admin-user-detail-item">
                <span>E-posta Doğrulama</span>
                <strong>
                  {selectedUser.emailVerified
                    ? 'Doğrulandı'
                    : 'Doğrulanmadı'}
                </strong>
              </div>
              <div className="admin-user-detail-item">
                <span>Aktivasyon</span>
                <strong>
                  {getActivationStatus(selectedUser).label}
                </strong>
              </div>
              <div className="admin-user-detail-item">
                <span>Oluşturulma</span>
                <strong>
                  {formatDateTime(selectedUser.createdAt)}
                </strong>
              </div>
              <div className="admin-user-detail-item">
                <span>Son Güncelleme</span>
                <strong>
                  {formatDateTime(selectedUser.updatedAt)}
                </strong>
              </div>
              <div className="admin-user-detail-item admin-user-detail-item-wide">
                <span>Son Parola Değişikliği</span>
                <strong>
                  {formatDateTime(
                    selectedUser.passwordChangedAt,
                  )}
                </strong>
              </div>
            </div>

            <div className="modal-actions">
              <button
                className="button button-primary"
                type="button"
                onClick={closeModal}
              >
                Kapat
              </button>
            </div>
          </section>
        </div>
      ) : null}

      {activeModal === 'role' && selectedUser ? (
        <div className="modal-backdrop" role="presentation">
          <section
            className="modal-card admin-user-action-modal"
            role="dialog"
            aria-modal="true"
            aria-labelledby="user-role-title"
          >
            <div className="modal-heading">
              <div>
                <span className="eyebrow">ROL GÜNCELLEME</span>
                <h2 id="user-role-title">
                  Kullanıcı Rolünü Değiştir
                </h2>
                <p>
                  {selectedUser.fullName} için yeni rolü seçin.
                </p>
              </div>
              <button
                className="modal-close-button"
                type="button"
                disabled={isSubmitting}
                onClick={closeModal}
                aria-label="Pencereyi kapat"
              >
                ×
              </button>
            </div>

            <form
              className="admin-user-form"
              onSubmit={handleRoleUpdate}
            >
              <label className="field">
                <span>Yeni Rol</span>
                <select
                  value={selectedRole}
                  onChange={(event) =>
                    setSelectedRole(event.target.value)
                  }
                  required
                >
                  {ROLE_OPTIONS.map((role) => (
                    <option key={role} value={role}>
                      {ROLE_LABELS[role]}
                    </option>
                  ))}
                </select>
              </label>

              {formError ? (
                <div className="form-error">{formError}</div>
              ) : null}

              <div className="modal-actions">
                <button
                  className="button button-secondary"
                  type="button"
                  disabled={isSubmitting}
                  onClick={closeModal}
                >
                  Vazgeç
                </button>
                <button
                  className="button button-primary"
                  type="submit"
                  disabled={isSubmitting}
                >
                  {isSubmitting
                    ? 'Güncelleniyor...'
                    : 'Rolü Güncelle'}
                </button>
              </div>
            </form>
          </section>
        </div>
      ) : null}

      {activeModal === 'status' && selectedUser ? (
        <div className="modal-backdrop" role="presentation">
          <section
            className="modal-card admin-user-action-modal"
            role="dialog"
            aria-modal="true"
            aria-labelledby="user-status-title"
          >
            <div className="modal-heading">
              <div>
                <span
                  className={
                    selectedUser.active
                      ? 'eyebrow danger-eyebrow'
                      : 'eyebrow'
                  }
                >
                  HESAP DURUMU
                </span>
                <h2 id="user-status-title">
                  {selectedUser.active
                    ? 'Kullanıcıyı Pasife Al'
                    : 'Kullanıcıyı Aktifleştir'}
                </h2>
                <p>
                  {selectedUser.fullName} hesabının durumunu
                  değiştirmek üzeresiniz.
                </p>
              </div>
              <button
                className="modal-close-button"
                type="button"
                disabled={isSubmitting}
                onClick={closeModal}
                aria-label="Pencereyi kapat"
              >
                ×
              </button>
            </div>

            {!selectedUser.active &&
            !selectedUser.activationCompleted ? (
              <div className="form-error">
                Aktivasyonu tamamlanmamış kullanıcı manuel
                olarak aktifleştirilemez.
              </div>
            ) : null}

            {formError ? (
              <div className="form-error">{formError}</div>
            ) : null}

            <div className="modal-actions">
              <button
                className="button button-secondary"
                type="button"
                disabled={isSubmitting}
                onClick={closeModal}
              >
                Vazgeç
              </button>
              <button
                className={
                  selectedUser.active
                    ? 'button button-danger'
                    : 'button button-primary'
                }
                type="button"
                disabled={
                  isSubmitting ||
                  (!selectedUser.active &&
                    !selectedUser.activationCompleted)
                }
                onClick={handleStatusUpdate}
              >
                {isSubmitting
                  ? 'Güncelleniyor...'
                  : selectedUser.active
                    ? 'Pasife Al'
                    : 'Aktifleştir'}
              </button>
            </div>
          </section>
        </div>
      ) : null}

      {activeModal === 'activation' && selectedUser ? (
        <div className="modal-backdrop" role="presentation">
          <section
            className="modal-card admin-user-action-modal"
            role="dialog"
            aria-modal="true"
            aria-labelledby="resend-activation-title"
          >
            <div className="modal-heading">
              <div>
                <span className="eyebrow">AKTİVASYON</span>
                <h2 id="resend-activation-title">
                  Aktivasyon E-postasını Gönder
                </h2>
                <p>
                  {selectedUser.email} adresine yeni bir
                  aktivasyon e-postası gönderilecek.
                </p>
              </div>
              <button
                className="modal-close-button"
                type="button"
                disabled={isSubmitting}
                onClick={closeModal}
                aria-label="Pencereyi kapat"
              >
                ×
              </button>
            </div>

            {formError ? (
              <div className="form-error">{formError}</div>
            ) : null}

            <div className="modal-actions">
              <button
                className="button button-secondary"
                type="button"
                disabled={isSubmitting}
                onClick={closeModal}
              >
                Vazgeç
              </button>
              <button
                className="button button-primary"
                type="button"
                disabled={isSubmitting}
                onClick={handleResendActivation}
              >
                {isSubmitting
                  ? 'Gönderiliyor...'
                  : 'E-postayı Gönder'}
              </button>
            </div>
          </section>
        </div>
      ) : null}
    </div>
  )
}
