import { useEffect, useState } from 'react'
import { Link, Navigate, useSearchParams } from 'react-router-dom'
import { activateAccount } from '../api/authApi.js'
import { useAuth } from '../auth/AuthContext.jsx'

export default function ActivateAccountPage() {
  const { isAuthenticated, isInitializing } = useAuth()
  const [searchParams] = useSearchParams()
  const [token, setToken] = useState(searchParams.get('token') ?? '')
  const [password, setPassword] = useState('')
  const [passwordConfirmation, setPasswordConfirmation] = useState('')
  const [error, setError] = useState('')
  const [successMessage, setSuccessMessage] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)

  useEffect(() => {
    setError('')
  }, [token, password, passwordConfirmation])

  if (!isInitializing && isAuthenticated) {
    return <Navigate to="/" replace />
  }

  const handleSubmit = async (event) => {
    event.preventDefault()
    setError('')
    setSuccessMessage('')

    if (password.length < 8) {
      setError('Şifre en az 8 karakter olmalıdır.')
      return
    }

    if (password !== passwordConfirmation) {
      setError('Şifre ve şifre tekrarı eşleşmiyor.')
      return
    }

    setIsSubmitting(true)

    try {
      const response = await activateAccount({
        token: token.trim(),
        password,
        passwordConfirmation,
      })

      setSuccessMessage(
        response?.message ?? 'Hesabınız başarıyla aktive edildi. Sisteme giriş yapabilirsiniz.',
      )
      setPassword('')
      setPasswordConfirmation('')
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
          <h1>Hesabınızı aktive edin ve proje takibine başlayın.</h1>
          <p>
            E-posta adresinize gönderilen aktivasyon bağlantısını kullanarak ilk şifrenizi
            güvenli şekilde belirleyebilirsiniz.
          </p>
          <div className="login-feature-list">
            <span>✓ Tek kullanımlık bağlantı</span>
            <span>✓ Güvenli parola belirleme</span>
            <span>✓ Rol bazlı erişim</span>
          </div>
        </div>
      </section>

      <section className="login-form-side">
        <form className="login-card" onSubmit={handleSubmit}>
          <div className="login-card-heading">
            <span className="eyebrow">HESAP AKTİVASYONU</span>
            <h2>İlk şifrenizi belirleyin</h2>
            <p>Bağlantıdaki token otomatik olarak doldurulur. Gerekirse tokenı elle de girebilirsiniz.</p>
          </div>

          <label className="field">
            <span>Aktivasyon tokenı</span>
            <input
              type="text"
              value={token}
              onChange={(event) => setToken(event.target.value)}
              placeholder="Aktivasyon tokenını girin"
              autoComplete="off"
              required
            />
          </label>

          <label className="field">
            <span>Şifre</span>
            <input
              type="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              placeholder="En az 8 karakter"
              autoComplete="new-password"
              minLength={8}
              maxLength={100}
              required
            />
          </label>

          <label className="field">
            <span>Şifre tekrarı</span>
            <input
              type="password"
              value={passwordConfirmation}
              onChange={(event) => setPasswordConfirmation(event.target.value)}
              placeholder="Şifrenizi tekrar girin"
              autoComplete="new-password"
              required
            />
          </label>

          <p className="password-requirements">Şifreniz 8 ile 100 karakter arasında olmalıdır.</p>

          {error ? <div className="form-error" role="alert">{error}</div> : null}
          {successMessage ? <div className="form-success" role="status">{successMessage}</div> : null}

          {successMessage ? (
            <Link className="button button-primary button-full" to="/login">
              Giriş Ekranına Git
            </Link>
          ) : (
            <button className="button button-primary button-full" type="submit" disabled={isSubmitting}>
              {isSubmitting ? 'Hesap aktive ediliyor...' : 'Hesabı Aktive Et'}
            </button>
          )}

          <p className="login-hint">
            Zaten aktif bir hesabınız mı var? <Link className="auth-link" to="/login">Giriş yapın</Link>
          </p>
        </form>
      </section>
    </div>
  )
}
