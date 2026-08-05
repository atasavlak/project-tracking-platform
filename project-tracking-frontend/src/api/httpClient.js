const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? ''
const AUTH_STORAGE_KEY = 'project_tracking_auth'

export class ApiError extends Error {
  constructor(message, status, payload = null) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.payload = payload
  }
}

export function createBasicToken(username, password) {
  return window.btoa(`${username}:${password}`)
}

export function readStoredAuth() {
  const storedValue = sessionStorage.getItem(AUTH_STORAGE_KEY)

  if (!storedValue) {
    return null
  }

  try {
    return JSON.parse(storedValue)
  } catch {
    sessionStorage.removeItem(AUTH_STORAGE_KEY)
    return null
  }
}

export function storeAuth(authData) {
  sessionStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(authData))
}

export function clearStoredAuth() {
  sessionStorage.removeItem(AUTH_STORAGE_KEY)
}

async function parseResponse(response) {
  if (response.status === 204) {
    return null
  }

  const contentType = response.headers.get('content-type') ?? ''

  if (contentType.includes('application/json')) {
    return response.json()
  }

  const text = await response.text()
  return text || null
}

function createRequestHeaders({
  token,
  headers,
  skipAuth,
  accept,
}) {
  const storedAuth = readStoredAuth()
  const authToken = skipAuth ? null : token ?? storedAuth?.token

  const requestHeaders = {
    Accept: accept,
    ...headers,
  }

  if (authToken) {
    requestHeaders.Authorization = `Basic ${authToken}`
  }

  return requestHeaders
}

function handleUnauthorized(response, token, skipAuth) {
  if (response.status === 401 && !token && !skipAuth) {
    clearStoredAuth()
    window.dispatchEvent(new CustomEvent('auth:unauthorized'))
  }
}

function extractFileName(contentDisposition, fallbackFileName) {
  if (!contentDisposition) {
    return fallbackFileName
  }

  const utf8Match = contentDisposition.match(
    /filename\*=UTF-8''([^;]+)/i,
  )

  if (utf8Match?.[1]) {
    return decodeURIComponent(utf8Match[1].replace(/["']/g, ''))
  }

  const basicMatch = contentDisposition.match(/filename="?([^";]+)"?/i)
  return basicMatch?.[1] ?? fallbackFileName
}

export async function apiRequest(path, options = {}) {
  const {
    method = 'GET',
    body,
    token,
    headers = {},
    signal,
    skipAuth = false,
  } = options

  const requestHeaders = createRequestHeaders({
    token,
    headers,
    skipAuth,
    accept: 'application/json',
  })

  if (body !== undefined) {
    requestHeaders['Content-Type'] = 'application/json'
  }

  let response

  try {
    response = await fetch(`${API_BASE_URL}${path}`, {
      method,
      headers: requestHeaders,
      body: body === undefined ? undefined : JSON.stringify(body),
      signal,
    })
  } catch (error) {
    if (error.name === 'AbortError') {
      throw error
    }

    throw new ApiError(
      'Backend servisine ulaşılamadı. Backend uygulamasının çalıştığını kontrol edin.',
      0,
    )
  }

  const payload = await parseResponse(response)

  if (!response.ok) {
    handleUnauthorized(response, token, skipAuth)

    const message =
      payload?.message ??
      payload?.error ??
      (typeof payload === 'string' ? payload : null) ??
      'İşlem tamamlanamadı.'

    throw new ApiError(message, response.status, payload)
  }

  return payload
}

export async function apiDownload(
  path,
  fallbackFileName,
  options = {},
) {
  const {
    token,
    headers = {},
    signal,
    skipAuth = false,
  } = options

  const requestHeaders = createRequestHeaders({
    token,
    headers,
    skipAuth,
    accept: '*/*',
  })

  let response

  try {
    response = await fetch(`${API_BASE_URL}${path}`, {
      headers: requestHeaders,
      signal,
    })
  } catch (error) {
    if (error.name === 'AbortError') {
      throw error
    }

    throw new ApiError(
      'Rapor indirilemedi. Backend uygulamasının çalıştığını kontrol edin.',
      0,
    )
  }

  if (!response.ok) {
    handleUnauthorized(response, token, skipAuth)
    const payload = await parseResponse(response)
    const message =
      payload?.message ??
      payload?.error ??
      (typeof payload === 'string' ? payload : null) ??
      'Rapor indirilemedi.'

    throw new ApiError(message, response.status, payload)
  }

  const blob = await response.blob()
  const fileName = extractFileName(
    response.headers.get('content-disposition'),
    fallbackFileName,
  )
  const objectUrl = URL.createObjectURL(blob)
  const anchor = document.createElement('a')

  anchor.href = objectUrl
  anchor.download = fileName
  document.body.appendChild(anchor)
  anchor.click()
  anchor.remove()
  URL.revokeObjectURL(objectUrl)

  return fileName
}
