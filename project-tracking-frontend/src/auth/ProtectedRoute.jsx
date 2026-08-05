import { Navigate, Outlet, useLocation } from 'react-router-dom'
import LoadingState from '../components/LoadingState.jsx'
import { useAuth } from './AuthContext.jsx'

export default function ProtectedRoute() {
  const { isAuthenticated, isInitializing } = useAuth()
  const location = useLocation()

  if (isInitializing) {
    return <LoadingState fullPage message="Oturum kontrol ediliyor..." />
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: location }} />
  }

  return <Outlet />
}
