import { useEffect, useState } from 'react'
import { Link, Navigate } from 'react-router-dom'
import { forgotPassword } from '../api/authApi.js'
import { useAuth } from '../auth/AuthContext.jsx'

export default function ForgotPasswordPage() {
  const { isAuthenticated, isInitializing } = useAuth()
  const [email, setEmail] = useState('')
  const [error, setError] = useState('')
  const [successMessage, setSuccessMessage] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)

  useEffect(() => {
    setError('')
  }, [email])

  if (!isInitializing && isAuthenticated) {
    return <Navigate to="/" replace />
  }

  const handleSubmit = async (event) => {
    event.preventDefault()
    setError('')
    setSuccessMessage('')
    setIsSubmitting(true)

    try {
      const response = await forgotPassword({ email: email.trim() })
      setSuccessMessage(
        response?.message ??
          'E-posta adresi sistemde kayıtlıysa şifre sıfırlama bağlantısı gönderilmiştir.',
      )
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
          <h1>Hesabınıza yeniden erişmek için bağlantı isteyin.</h1>
          <p>
            Kayıtlı e-posta adresinizi girin. Hesabınız uygunsa parola sıfırlama bağlantısı
            e-posta adresinize gönderilir.
          </p>
          <div className="login-feature-list">
            <span>✓ Güvenli bağlantı</span>
            <span>✓ Tek kullanımlık token</span>
            <span>✓ Hesap gizliliği</span>
          </div>
        </div>
      </section>

      <section className="login-form-side">
        <form className="login-card" onSubmit={handleSubmit}>
          <div className="login-card-heading">
            <span className="eyebrow">ŞİFREMİ UNUTTUM</span>
            <h2>Parolanızı sıfırlayın</h2>
            <p>Kullanıcı hesabınıza bağlı e-posta adresini girin.</p>
          </div>

          <label className="field">
            <span>E-posta adresi</span>
            <input
              type="email"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              placeholder="ornek@firma.com"
              autoComplete="email"
              required
            />
          </label>

          {error ? <div className="form-error" role="alert">{error}</div> : null}
          {successMessage ? <div className="form-success" role="status">{successMessage}</div> : null}

          <button className="button button-primary button-full" type="submit" disabled={isSubmitting}>
            {isSubmitting ? 'Bağlantı gönderiliyor...' : 'Sıfırlama Bağlantısı Gönder'}
          </button>

          <p className="login-hint">
            Şifrenizi hatırladınız mı? <Link className="auth-link" to="/login">Giriş ekranına dönün</Link>
          </p>
        </form>
      </section>
    </div>
  )
}
