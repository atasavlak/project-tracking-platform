import { useAuth } from '../auth/AuthContext.jsx'
import { ROLE_LABELS } from '../utils/roles.js'

export default function Topbar({ onMenuClick }) {
  const { user, logout } = useAuth()

  return (
    <header className="topbar">
      <button className="menu-button" type="button" onClick={onMenuClick} aria-label="Menüyü aç">
        ☰
      </button>

      <div className="topbar-spacer" />

      <div className="user-summary">
        <div className="avatar" aria-hidden="true">
          {user.fullName?.slice(0, 1).toUpperCase() ?? 'U'}
        </div>
        <div className="user-summary-text">
          <strong>{user.fullName}</strong>
          <span>{ROLE_LABELS[user.role] ?? user.role}</span>
        </div>
      </div>

      <button className="button button-ghost" type="button" onClick={logout}>
        Çıkış Yap
      </button>
    </header>
  )
}
