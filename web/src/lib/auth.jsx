import { createContext, useContext, useEffect, useMemo, useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { ACCESS_KEY, clearTokens, ownerAuth, owners, persistSession } from './api'
import { getErrorMessage } from './errors'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const queryClient = useQueryClient()
  const [hasSession, setHasSession] = useState(() => Boolean(sessionStorage.getItem(ACCESS_KEY)))

  const meQuery = useQuery({
    queryKey: ['owner', 'me'],
    queryFn: async () => (await owners.me()).data,
    enabled: hasSession,
    retry: false,
  })

  useEffect(() => {
    if (hasSession && meQuery.isError) {
      clearTokens()
      setHasSession(false)
    }
  }, [hasSession, meQuery.isError])

  const loginMutation = useMutation({
    mutationFn: async ({ email, password }) => persistSession((await ownerAuth.login(email, password)).data),
    onSuccess: async () => {
      setHasSession(true)
      await queryClient.invalidateQueries({ queryKey: ['owner', 'me'] })
    },
  })

  const googleMutation = useMutation({
    mutationFn: async (idToken) => persistSession((await ownerAuth.google(idToken)).data),
    onSuccess: async () => {
      setHasSession(true)
      await queryClient.invalidateQueries({ queryKey: ['owner', 'me'] })
    },
  })

  const logout = async () => {
    try {
      await ownerAuth.logout()
    } catch {
      /* session already gone */
    }
    clearTokens()
    setHasSession(false)
    queryClient.clear()
  }

  const value = useMemo(
    () => ({
      hasSession,
      owner: meQuery.data,
      loading: hasSession && meQuery.isLoading,
      login: loginMutation.mutateAsync,
      loginWithGoogle: googleMutation.mutateAsync,
      logout,
      error: loginMutation.error || googleMutation.error || meQuery.error,
    }),
    [hasSession, meQuery.data, meQuery.isLoading, meQuery.error, loginMutation.error, googleMutation.error],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider')
  }
  return context
}

export function authError(error) {
  return getErrorMessage(error)
}
