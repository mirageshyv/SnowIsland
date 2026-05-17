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
  /** DM：待处理 + 已完成交易全表 */
  getDmOverview: () => request(`${API_BASE}/trades/dm/overview`),
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

export const arkAPI = {
  getStatus: () => request(`${API_BASE}/ark/status`),
  invest: (wood, metal, sealant) => {
    const userId = localStorage.getItem('userId')
    return request(`${API_BASE}/ark/invest?wood=${wood}&metal=${metal}&sealant=${sealant}`, {
      method: 'POST',
      headers: { userId }
    })
  },
  installComponent: (type, count) => {
    const userId = localStorage.getItem('userId')
    return request(`${API_BASE}/ark/component?componentType=${type}&count=${count}`, {
      method: 'POST',
      headers: { userId }
    })
  },
  buildSail: () => {
    const userId = localStorage.getItem('userId')
    return request(`${API_BASE}/ark/sail`, { 
      method: 'POST',
      headers: { userId }
    })
  },
  reset: () => {
    const userId = localStorage.getItem('userId')
    return request(`${API_BASE}/ark/reset`, { 
      method: 'POST',
      headers: { userId }
    })
  }
}

export const milestoneAPI = {
  getAll: (playerId, userRole) => 
    request(`${API_BASE}/milestones?playerId=${playerId}&userRole=${encodeURIComponent(userRole)}`),
  getProgress: (playerId, userRole) => 
    request(`${API_BASE}/milestones/progress?playerId=${playerId}&userRole=${encodeURIComponent(userRole)}`),
  complete: (milestoneId, playerId, userRole) => 
    request(`${API_BASE}/milestones/${milestoneId}/complete?playerId=${playerId}&userRole=${encodeURIComponent(userRole)}`, {
      method: 'POST'
    }),
  toggle: (milestoneId, playerId, userRole) => 
    request(`${API_BASE}/milestones/${milestoneId}/toggle?playerId=${playerId}&userRole=${encodeURIComponent(userRole)}`, {
      method: 'POST'
    })
}

export const catastropheAPI = {
  getProgress: () => request(`${API_BASE}/catastrophe/progress`),
  updateProgress: (value, userRole) => 
    request(`${API_BASE}/catastrophe/progress?value=${value}&userRole=${encodeURIComponent(userRole)}`, {
      method: 'POST'
    }),
  advanceDay: () => request(`${API_BASE}/catastrophe/advance-day`, {
    method: 'POST'
  }),
  drawCards: (userRole) => 
    request(`${API_BASE}/catastrophe/draw-cards?userRole=${encodeURIComponent(userRole)}`, {
      method: 'POST'
    }),
  getDrawnCards: (round) => {
    const url = round ? `${API_BASE}/catastrophe/drawn-cards?round=${round}` : `${API_BASE}/catastrophe/drawn-cards`
    return request(url)
  },
  confirmCards: (userRole) => 
    request(`${API_BASE}/catastrophe/confirm-cards?userRole=${encodeURIComponent(userRole)}`, {
      method: 'POST'
    }),
  getSelectableCards: () => request(`${API_BASE}/catastrophe/selectable-cards`),
  selectCard: (selectedId, playerId, userRole) => 
    request(`${API_BASE}/catastrophe/select-card?selectedId=${selectedId}&playerId=${playerId}&userRole=${encodeURIComponent(userRole)}`, {
      method: 'POST'
    }),
  getGameState: () => request(`${API_BASE}/catastrophe/game-state`),
  setExtraCardDue: (extraCardDue, userRole) => 
    request(`${API_BASE}/catastrophe/extra-card-due?extraCardDue=${extraCardDue}&userRole=${encodeURIComponent(userRole)}`, {
      method: 'POST'
    }),
  getAllCards: () => request(`${API_BASE}/catastrophe/cards`),
  resetCatastrophe: (userRole) =>
    request(`${API_BASE}/catastrophe/reset?userRole=${encodeURIComponent(userRole)}`, {
      method: 'POST'
    })
}

export const warehouseAPI = {
  getAccessibleWarehouses: (playerId, userRole) =>
    request(`${API_BASE}/warehouses?playerId=${playerId || ''}&userRole=${encodeURIComponent(userRole || '')}`),
  getWarehouseStock: (warehouseKey, playerId, userRole) =>
    request(`${API_BASE}/warehouses/${warehouseKey}/stock?playerId=${playerId || ''}&userRole=${encodeURIComponent(userRole || '')}`),
  updateWarehouseStock: (warehouseKey, itemType, itemId, quantity, userRole) =>
    request(`${API_BASE}/warehouses/${warehouseKey}/stock?userRole=${encodeURIComponent(userRole)}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ itemType, itemId, quantity })
    })
}

export const locationAPI = {
  getAll: (area) => {
    const qs = area ? `?area=${encodeURIComponent(area)}` : ''
    return request(`${API_BASE}/locations${qs}`)
  },
  getById: (id) => request(`${API_BASE}/locations/${id}`),
}

export const factionActionAPI = {
  getContext: (playerId, gameDay = 1) =>
    request(`${API_BASE}/faction-actions/context/${playerId}?gameDay=${gameDay}`),
  submitAction: (data) =>
    request(`${API_BASE}/faction-actions/submit`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    }),
  getPlayerHistory: (playerId, gameDay = 1) =>
    request(`${API_BASE}/faction-actions/player/${playerId}?gameDay=${gameDay}`),
  getAllActions: (params = {}) => {
    const qs = Object.entries(params)
      .filter(([, v]) => v != null && v !== '')
      .map(([k, v]) => `${k}=${encodeURIComponent(v)}`)
      .join('&')
    return request(`${API_BASE}/faction-actions/all${qs ? '?' + qs : ''}`)
  },
  feedbackAction: (actionId, feedback) =>
    request(`${API_BASE}/faction-actions/${actionId}/feedback`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ feedback }),
    }),
}

export const actionAPI = {
  submitAction: (data) =>
    request(`${API_BASE}/actions/submit`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data)
    }),
  getPlayerActions: (playerId, gameDay = 1) =>
    request(`${API_BASE}/actions/player/${playerId}?gameDay=${gameDay}`),
  getAllActions: (params = {}) => {
    const qs = Object.entries(params).filter(([, v]) => v != null && v !== '').map(([k, v]) => `${k}=${encodeURIComponent(v)}`).join('&')
    return request(`${API_BASE}/actions/all${qs ? '?' + qs : ''}`)
  },
  feedbackAction: (actionId, feedback) =>
    request(`${API_BASE}/actions/${actionId}/feedback`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ feedback })
    }),
  batchResolveInvestigate: (gameDay = 1) =>
    request(`${API_BASE}/actions/resolve/investigate?gameDay=${gameDay}`, { method: 'POST' }),
  batchResolveProduce: (gameDay = 1) =>
    request(`${API_BASE}/actions/resolve/produce?gameDay=${gameDay}`, { method: 'POST' }),
  resolveTransport: (actionId) =>
    request(`${API_BASE}/actions/resolve/transport/${actionId}`, { method: 'POST' }),
  getProductionInfo: (playerId) =>
    request(`${API_BASE}/actions/production-info/${playerId}`),
  checkStealth: (playerId, gameDay = 1) =>
    request(`${API_BASE}/actions/stealth/${playerId}?gameDay=${gameDay}`)
}

export { checkBackend }