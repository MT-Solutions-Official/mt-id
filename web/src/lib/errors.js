export function getErrorMessage(error) {
  const data = error?.response?.data
  if (!data) {
    return error?.message || 'Não foi possível completar a operação.'
  }
  if (typeof data === 'string') {
    return data
  }
  if (data.message) {
    return data.message
  }
  if (data.detail) {
    return data.detail
  }
  if (Array.isArray(data.violations) && data.violations[0]?.message) {
    return data.violations.map((item) => item.message).join(' ')
  }
  if (data.errorCode) {
    return data.errorCode
  }
  return 'Não foi possível completar a operação.'
}

export function isRateLimited(error) {
  return error?.response?.status === 429
}
