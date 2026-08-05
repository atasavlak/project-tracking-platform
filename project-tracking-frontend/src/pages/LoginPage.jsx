import { useEffect, useState } from 'react'
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext.jsx'

export default function LoginPage() {
  const { isAuthenticated, isInitializing, login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)

  useEffect(() => {
    setError('')
  }, [username, password])

  if (!isInitializing && isAuthenticated) {
    return <Navigate to="/" replace />
  }

  const handleSubmit = async (event) => {
    event.preventDefault()
    setError('')
    setIsSubmitting(true)

    try {
      await login(username, password)
      const destination = location.state?.from?.pathname ?? '/'
      navigate(destination, { replace: true })
    } catch (requestError) {
      if (requestError.status === 401) {
        setError('Kullanıcı adı veya şifre hatalı.')
      } else {
        setError(requestError.message)
      }
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
          <h1>Projeleri, raporları ve riskleri tek noktadan takip edin.</h1>
          <p>
            Proje yöneticileri haftalık ilerlemeyi kaydeder, yönetici roller ise genel durumu
            güvenli ve anlaşılır bir panel üzerinden izler.
          </p>
          <div className="login-feature-list">
            <span>✓ Rol bazlı erişim</span>
            <span>✓ Gerçek backend bağlantısı</span>
            <span>✓ Proje ve durum takibi</span>
          </div>
        </div>
      </section>

      <section className="login-form-side">
        <form className="login-card" onSubmit={handleSubmit}>
          <div className="login-card-heading">
            <span className="eyebrow">HOŞ GELDİNİZ</span>
            <h2>Hesabınıza giriş yapın</h2>
            <p>Backend’de tanımlı kullanıcı adı ve şifrenizi kullanın.</p>
          </div>

          <label className="field">
            <span>Kullanıcı adı</span>
            <input
              type="text"
              value={username}
              onChange={(event) => setUsername(event.target.value)}
              placeholder="Kullanıcı adı veya e-posta"
              autoComplete="username"
              required
            />
          </label>

          <label className="field">
            <span>Şifre</span>
            <input
              autoComplete="current-password"
              required
              type="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              placeholder="••••••••"
            />
          </label>

          <div className="auth-link-row">
            <Link className="auth-link" to="/forgot-password">Şifremi unuttum</Link>
          </div>

          {error ? <div className="form-error" role="alert">{error}</div> : null}

          <button className="button button-primary button-full" type="submit" disabled={isSubmitting}>
            {isSubmitting ? 'Giriş yapılıyor...' : 'Giriş Yap'}
          </button>

          <p className="login-hint">
            Backend adresi: <code>http://localhost:8080</code>
          </p>
        </form>
      </section>
    </div>
  )
}
