import { NavLink } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext.jsx'
import { ROLES } from '../utils/roles.js'

function navClassName({ isActive }) {
  return isActive
    ? 'sidebar-link sidebar-link-active'
    : 'sidebar-link'
}

export default function Sidebar({
  isOpen,
  onClose,
}) {
  const { user } = useAuth()

  const canSeeDashboard = [
    ROLES.CTO,
    ROLES.ADMIN,
  ].includes(user.role)

  const canManageUsers = user.role === ROLES.ADMIN

  return (
    <>
      <button
        className={`sidebar-backdrop ${
          isOpen
            ? 'sidebar-backdrop-visible'
            : ''
        }`}
        type="button"
        aria-label="Menüyü kapat"
        onClick={onClose}
      />

      <aside
        className={`sidebar ${
          isOpen ? 'sidebar-open' : ''
        }`}
      >
        <div className="brand">
          <div className="brand-mark">PT</div>

          <div>
            <strong>Project Tracking</strong>
            <span>Yönetim Paneli</span>
          </div>
        </div>

        <nav
          className="sidebar-nav"
          aria-label="Ana menü"
        >
          <NavLink
            to="/"
            end
            className={navClassName}
            onClick={onClose}
          >
            <span aria-hidden="true">⌂</span>
            Ana Sayfa
          </NavLink>

          {canSeeDashboard ? (
            <NavLink
              to="/dashboard"
              className={navClassName}
              onClick={onClose}
            >
              <span aria-hidden="true">◫</span>
              Dashboard
            </NavLink>
          ) : null}

          {canManageUsers ? (
            <NavLink
              to="/admin/users"
              className={navClassName}
              onClick={onClose}
            >
              <span aria-hidden="true">♙</span>
              Kullanıcı Yönetimi
            </NavLink>
          ) : null}

          <NavLink
            to="/projects"
            className={navClassName}
            onClick={onClose}
          >
            <span aria-hidden="true">▣</span>
            Projeler
          </NavLink>

          <NavLink
            to="/weekly-reports"
            className={navClassName}
            onClick={onClose}
          >
            <span aria-hidden="true">▤</span>
            Haftalık Raporlar
          </NavLink>

          <NavLink
            to="/work-items"
            className={navClassName}
            onClick={onClose}
          >
            <span aria-hidden="true">✓</span>
            İş Kalemleri
          </NavLink>

          <NavLink
            to="/risk-issues"
            className={navClassName}
            onClick={onClose}
          >
            <span aria-hidden="true">!</span>
            Riskler ve Engeller
          </NavLink>

          <NavLink
            to="/decision-logs"
            className={navClassName}
            onClick={onClose}
          >
            <span aria-hidden="true">◆</span>
            Karar Kayıtları
          </NavLink>


          <NavLink
            to="/action-items"
            className={navClassName}
            onClick={onClose}
          >
            <span aria-hidden="true">→</span>
            Aksiyon Takibi
          </NavLink>
        </nav>

        <div className="sidebar-footer">
          <span>Backend</span>
          <strong>localhost:8080</strong>
        </div>
      </aside>
    </>
  )
}