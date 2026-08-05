import { Navigate, Outlet } from 'react-router-dom'
import { useAuth } from './AuthContext.jsx'

export default function RoleRoute({ allowedRoles }) {
  const { user } = useAuth()

  if (!user || !allowedRoles.includes(user.role)) {
    return <Navigate to="/forbidden" replace />
  }

  return <Outlet />
}
