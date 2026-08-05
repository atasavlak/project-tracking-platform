import { useEffect, useState } from 'react'
import { Link, Navigate, useSearchParams } from 'react-router-dom'
import { resetPassword } from '../api/authApi.js'
import { useAuth } from '../auth/AuthContext.jsx'

export default function ResetPasswordPage() {
  const { isAuthenticated, isInitializing } = useAuth()
  const [searchParams] = useSearchParams()
  const [token, setToken] = useState(searchParams.get('token') ?? '')
  const [newPassword, setNewPassword] = useState('')
  const [newPasswordConfirmation, setNewPasswordConfirmation] = useState('')
  const [error, setError] = useState('')
  const [successMessage, setSuccessMessage] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)

  useEffect(() => {
    setError('')
  }, [token, newPassword, newPasswordConfirmation])

  if (!isInitializing && isAuthenticated) {
    return <Navigate to="/" replace />
  }

  const handleSubmit = async (event) => {
    event.preventDefault()
    setError('')
    setSuccessMessage('')

    if (newPassword.length < 8) {
      setError('Yeni şifre en az 8 karakter olmalıdır.')
      return
    }

    if (newPassword !== newPasswordConfirmation) {
      setError('Yeni şifre ve şifre tekrarı eşleşmiyor.')
      return
    }

    setIsSubmitting(true)

    try {
      const response = await resetPassword({
        token: token.trim(),
        newPassword,
        newPasswordConfirmation,
      })

      setSuccessMessage(
        response?.message ??
          'Şifreniz başarıyla değiştirildi. Yeni şifrenizle giriş yapabilirsiniz.',
      )
      setNewPassword('')
      setNewPasswordConfirmation('')
    } catch (requestError) {
      setError(requestError.message)
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div className="login-page">
      <section className="login-hero">
        <div className="login-hero-content">
          <div className="brand brand-light">
            <div className="brand-mark">PT</div>
            <div>
              <strong>Project Tracking</strong>
              <span>Haftalık proje takip sistemi</span>
            </div>
          </div>
          <h1>Yeni şifrenizi belirleyerek hesabınıza dönün.</h1>
          <p>
            E-posta adresinize gönderilen tek kullanımlık bağlantı üzerinden yeni şifrenizi
            güvenli şekilde oluşturabilirsiniz.
          </p>
          <div className="login-feature-list">
            <span>✓ Süreli bağlantı</span>
            <span>✓ Güvenli parola yenileme</span>
            <span>✓ Tek kullanımlık işlem</span>
          </div>
        </div>
      </section>

      <section className="login-form-side">
        <form className="login-card" onSubmit={handleSubmit}>
          <div className="login-card-heading">
            <span className="eyebrow">PAROLA SIFIRLAMA</span>
            <h2>Yeni şifrenizi belirleyin</h2>
            <p>Bağlantıdaki token otomatik doldurulur. Gerekirse tokenı elle girebilirsiniz.</p>
          </div>

          <label className="field">
            <span>Şifre sıfırlama tokenı</span>
            <input
              type="text"
              value={token}
              onChange={(event) => setToken(event.target.value)}
              placeholder="Şifre sıfırlama tokenını girin"
              autoComplete="off"
              required
            />
          </label>

          <label className="field">
            <span>Yeni şifre</span>
            <input
              type="password"
              value={newPassword}
              onChange={(event) => setNewPassword(event.target.value)}
              placeholder="En az 8 karakter"
              autoComplete="new-password"
              minLength={8}
              maxLength={100}
              required
            />
          </label>

          <label className="field">
            <span>Yeni şifre tekrarı</span>
            <input
              type="password"
              value={newPasswordConfirmation}
              onChange={(event) => setNewPasswordConfirmation(event.target.value)}
              placeholder="Yeni şifrenizi tekrar girin"
              autoComplete="new-password"
              required
            />
          </label>

          <p className="password-requirements">Yeni şifreniz 8 ile 100 karakter arasında olmalıdır.</p>

          {error ? <div className="form-error" role="alert">{error}</div> : null}
          {successMessage ? <div className="form-success" role="status">{successMessage}</div> : null}

          {successMessage ? (
            <Link className="button button-primary button-full" to="/login">
              Yeni Şifreyle Giriş Yap
            </Link>
          ) : (
            <button className="button button-primary button-full" type="submit" disabled={isSubmitting}>
              {isSubmitting ? 'Şifre güncelleniyor...' : 'Şifreyi Güncelle'}
            </button>
          )}

          <p className="login-hint">
            Bağlantınız yok mu? <Link className="auth-link" to="/forgot-password">Yeni bağlantı isteyin</Link>
          </p>
        </form>
      </section>
    </div>
  )
}
