import {
  Navigate,
  Route,
  Routes,
} from 'react-router-dom'
import ProtectedRoute from './auth/ProtectedRoute.jsx'
import RoleRoute from './auth/RoleRoute.jsx'
import AppLayout from './layout/AppLayout.jsx'
import ActivateAccountPage from './pages/ActivateAccountPage.jsx'
import ActionItemsPage from './pages/ActionItemsPage.jsx'
import AdminUsersPage from './pages/AdminUsersPage.jsx'
import DashboardPage from './pages/DashboardPage.jsx'
import DecisionLogsPage from './pages/DecisionLogsPage.jsx'
import ForbiddenPage from './pages/ForbiddenPage.jsx'
import ForgotPasswordPage from './pages/ForgotPasswordPage.jsx'
import HomePage from './pages/HomePage.jsx'
import LoginPage from './pages/LoginPage.jsx'
import NotFoundPage from './pages/NotFoundPage.jsx'
import ProjectsPage from './pages/ProjectsPage.jsx'
import ResetPasswordPage from './pages/ResetPasswordPage.jsx'
import RiskIssuesPage from './pages/RiskIssuesPage.jsx'
import WeeklyReportsPage from './pages/WeeklyReportsPage.jsx'
import WorkItemsPage from './pages/WorkItemsPage.jsx'
import { ROLES } from './utils/roles.js'

export default function App() {
  return (
    <Routes>
      <Route
        path="/login"
        element={<LoginPage />}
      />

      <Route
        path="/activate"
        element={<ActivateAccountPage />}
      />

      <Route
        path="/forgot-password"
        element={<ForgotPasswordPage />}
      />

      <Route
        path="/reset-password"
        element={<ResetPasswordPage />}
      />

      <Route
        path="/forbidden"
        element={<ForbiddenPage />}
      />

      <Route element={<ProtectedRoute />}>
        <Route element={<AppLayout />}>
          <Route
            index
            element={<HomePage />}
          />

          <Route
            path="projects"
            element={<ProjectsPage />}
          />

          <Route
            path="weekly-reports"
            element={<WeeklyReportsPage />}
          />

          <Route
            path="work-items"
            element={<WorkItemsPage />}
          />

          <Route
            path="risk-issues"
            element={<RiskIssuesPage />}
          />

          <Route
            path="decision-logs"
            element={<DecisionLogsPage />}
          />

          <Route
            path="action-items"
            element={<ActionItemsPage />}
          />

          <Route
            element={
              <RoleRoute
                allowedRoles={[ROLES.ADMIN]}
              />
            }
          >
            <Route
              path="admin/users"
              element={<AdminUsersPage />}
            />
          </Route>

          <Route
            element={
              <RoleRoute
                allowedRoles={[
                  ROLES.CTO,
                  ROLES.ADMIN,
                ]}
              />
            }
          >
            <Route
              path="dashboard"
              element={<DashboardPage />}
            />
          </Route>
        </Route>
      </Route>

      <Route
        path="/home"
        element={
          <Navigate
            to="/"
            replace
          />
        }
      />

      <Route
        path="*"
        element={<NotFoundPage />}
      />
    </Routes>
  )
}