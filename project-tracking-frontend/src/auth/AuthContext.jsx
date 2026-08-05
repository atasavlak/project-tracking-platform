import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react'
import { getCurrentUser } from '../api/authApi.js'
import {
  clearStoredAuth,
  createBasicToken,
  readStoredAuth,
  storeAuth,
} from '../api/httpClient.js'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const initialAuth = readStoredAuth()
  const [user, setUser] = useState(initialAuth?.user ?? null)
  const [isInitializing, setIsInitializing] = useState(Boolean(initialAuth?.token))

  const logout = useCallback(() => {
    clearStoredAuth()
    setUser(null)
  }, [])

  const login = useCallback(async (username, password) => {
    const normalizedUsername = username.trim()
    const token = createBasicToken(normalizedUsername, password)
    const currentUser = await getCurrentUser(token)

    const authData = {
      token,
      user: currentUser,
    }

    storeAuth(authData)
    setUser(currentUser)

    return currentUser
  }, [])

  useEffect(() => {
    const storedAuth = readStoredAuth()

    if (!storedAuth?.token) {
      setIsInitializing(false)
      return undefined
    }

    const controller = new AbortController()

    getCurrentUser(storedAuth.token, controller.signal)
      .then((currentUser) => {
        storeAuth({
          token: storedAuth.token,
          user: currentUser,
        })
        setUser(currentUser)
      })
      .catch((error) => {
        if (error.name !== 'AbortError') {
          logout()
        }
      })
      .finally(() => {
        setIsInitializing(false)
      })

    return () => controller.abort()
  }, [logout])

  useEffect(() => {
    const handleUnauthorized = () => logout()
    window.addEventListener('auth:unauthorized', handleUnauthorized)

    return () => {
      window.removeEventListener('auth:unauthorized', handleUnauthorized)
    }
  }, [logout])

  const value = useMemo(
    () => ({
      user,
      isAuthenticated: Boolean(user),
      isInitializing,
      login,
      logout,
    }),
    [user, isInitializing, login, logout],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const context = useContext(AuthContext)

  if (!context) {
    throw new Error('useAuth, AuthProvider içinde kullanılmalıdır.')
  }

  return context
}
