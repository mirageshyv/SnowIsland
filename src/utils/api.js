const API_BASE = 'http://localhost:8080/api'
let backendAvailable = true
let checkedBackend = false

async function checkBackend() {
  if (checkedBackend) return backendAvailable
  try {
    const controller = new AbortController()
    const timeoutId = setTimeout(() => controller.abort(), 1000)
    const response = await fetch(`${API_BASE}/players`, {
      method: 'GET',
      signal: controller.signal
    })
    clearTimeout(timeoutId)
    backendAvailable = response.ok
  } catch (error) {
    backendAvailable = false
  }
  checkedBackend = true
  return backendAvailable
}

async function request(url, options = {}) {
  try {
    const controller = new AbortController()
    const timeoutId = setTimeout(() => controller.abort(), 5000)
    const response = await fetch(url, {
      headers: {
        'Content-Type': 'application/json',
        ...options.headers
      },
      signal: controller.signal,
      ...options
    })
    clearTimeout(timeoutId)
    backendAvailable = true
    checkedBackend = true
    return await response.json()
  } catch (error) {
    backendAvailable = false
    checkedBackend = true
    if (error.name === 'AbortError') {
      console.log('请求超时:', url)
    } else if (error.message.includes('Failed to fetch')) {
      console.log('后端服务不可用:', url)
    } else {
      console.log('API请求失败:', error.message, '- URL:', url)
    }
    return null
  }
}

export const authAPI = {
  login: (username, password) => request(`${API_BASE}/auth/login`, {
    method: 'POST',
    body: JSON.stringify({ username, password })
  })
}

export const playerAPI = {
  getAll: () => request(`${API_BASE}/players`),
  get: (id) => request(`${API_BASE}/players/${id}`),
  getDetails: (id) => request(`${API_BASE}/players/${id}/details`),
  getItems: (id) => request(`${API_BASE}/players/${id}/items`),
  create: (player, loginUsername) =>
    request(`${API_BASE}/players?loginUsername=${encodeURIComponent(loginUsername)}`, {
      method: 'POST',
      body: JSON.stringify(player)
    }),
  update: (id, player) => request(`${API_BASE}/players/${id}`, {
    method: 'PUT',
    body: JSON.stringify(player)
  }),
  delete: (id) => request(`${API_BASE}/players/${id}`, {
    method: 'DELETE'
  })
}

/** 统治者避难所页：建造值、shelter 建材库存、避难所公共食物/能量（非玩家个人库存） */
export const shelterAPI = {
  getSummary: () => request(`${API_BASE}/shelter`),
}

export const tradeAPI = {
  getByPlayer: (playerId) => request(`${API_BASE}/trades/player/${playerId}`),
  /** 轻量：仅待处理 incoming 数量（轮询用，避免反复拉全量交易+明细） */
  getIncomingPendingCount: (playerId) =>
    request(`${API_BASE}/trades/incoming/${playerId}/pending-count`),
  getIncoming: (playerId) => request(`${API_BASE}/trades/incoming/${playerId}`),
  getDetail: (id) => request(`${API_BASE}/trades/${id}`),
  create: (tradeData) => request(`${API_BASE}/trades`, {
    method: 'POST',
    body: JSON.stringify(tradeData)
  }),
  accept: (id, playerId) => request(`${API_BASE}/trades/${id}/accept`, {
    method: 'POST',
    body: JSON.stringify({ playerId })
  }),
  reject: (id, playerId) => request(`${API_BASE}/trades/${id}/reject`, {
    method: 'POST',
    body: JSON.stringify({ playerId })
  })
}

export { checkBackend }