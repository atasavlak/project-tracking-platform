import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { getProjects } from '../api/projectApi.js'
import ErrorState from '../components/ErrorState.jsx'
import LoadingState from '../components/LoadingState.jsx'
import StatusBadge from '../components/StatusBadge.jsx'
import { useAuth } from '../auth/AuthContext.jsx'
import { ROLE_LABELS, ROLES } from '../utils/roles.js'

export default function HomePage() {
  const { user } = useAuth()
  const [projects, setProjects] = useState([])
  const [error, setError] = useState('')
  const [isLoading, setIsLoading] = useState(true)
  const [reloadKey, setReloadKey] = useState(0)

  useEffect(() => {
    const controller = new AbortController()
    setIsLoading(true)
    setError('')

    getProjects(null, controller.signal)
      .then(setProjects)
      .catch((requestError) => {
        if (requestError.name !== 'AbortError') {
          setError(requestError.message)
        }
      })
      .finally(() => setIsLoading(false))

    return () => controller.abort()
  }, [reloadKey])

  const statusCounts = projects.reduce((accumulator, project) => {
    accumulator[project.status] = (accumulator[project.status] ?? 0) + 1
    return accumulator
  }, {})

  return (
    <div className="page-stack">
      <section className="welcome-panel">
        <div>
          <span className="eyebrow">{ROLE_LABELS[user.role] ?? user.role}</span>
          <h1>Merhaba, {user.fullName}</h1>
          <p>Proje takip sisteminin güncel durumuna buradan ulaşabilirsiniz.</p>
        </div>
        <Link className="button button-primary" to="/projects">
          Projeleri Görüntüle
        </Link>
      </section>

      {isLoading ? <LoadingState message="Projeler yükleniyor..." /> : null}
      {error ? <ErrorState message={error} onRetry={() => setReloadKey((value) => value + 1)} /> : null}

      {!isLoading && !error ? (
        <>
          <section className="metric-grid">
            <article className="metric-card">
              <span>Aktif Proje</span>
              <strong>{projects.length}</strong>
              <small>Erişebildiğiniz toplam proje</small>
            </article>
            <article className="metric-card">
              <span>Planında</span>
              <strong>{statusCounts.ON_TRACK ?? 0}</strong>
              <small>Sorunsuz ilerleyen projeler</small>
            </article>
            <article className="metric-card metric-card-warning">
              <span>Riskli</span>
              <strong>{statusCounts.AT_RISK ?? 0}</strong>
              <small>Yakın takip gereken projeler</small>
            </article>
            <article className="metric-card metric-card-danger">
              <span>Gecikmiş</span>
              <strong>{statusCounts.DELAYED ?? 0}</strong>
              <small>Planın gerisinde kalan projeler</small>
            </article>
          </section>

          <section className="content-card">
            <div className="section-heading">
              <div>
                <h2>Son Projeler</h2>
                <p>Oluşturulma tarihine göre ilk beş kayıt</p>
              </div>
              {[ROLES.CTO, ROLES.ADMIN].includes(user.role) ? (
                <Link className="button button-secondary" to="/dashboard">
                  Dashboard’a Git
                </Link>
              ) : null}
            </div>

            {projects.length === 0 ? (
              <div className="empty-state">Görüntülenecek aktif proje bulunmuyor.</div>
            ) : (
              <div className="simple-list">
                {projects.slice(0, 5).map((project) => (
                  <div className="simple-list-row" key={project.id}>
                    <div>
                      <strong>{project.name}</strong>
                      <span>{project.description || 'Açıklama bulunmuyor.'}</span>
                    </div>
                    <StatusBadge status={project.status} />
                  </div>
                ))}
              </div>
            )}
          </section>
        </>
      ) : null}
    </div>
  )
}
