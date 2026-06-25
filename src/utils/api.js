const API_BASE = '/api'

async function request(url, options = {}) {
  try {
    const controller = new AbortController()
    const timeoutId = setTimeout(() => controller.abort(), 15000)
    const response = await fetch(url, {
      ...options,
      headers: {
        'Content-Type': 'application/json',
        ...options.headers
      },
      signal: controller.signal
    })
    clearTimeout(timeoutId)
    const text = await response.text()
    let data = null
    try {
      data = text ? JSON.parse(text) : {}
    } catch {
      data = { success: false, message: text || `请求失败 (${response.status})` }
    }
    if (!response.ok) {
      return {
        success: false,
        message: data?.message || data?.error || `请求失败 (${response.status})`
      }
    }
    return data
  } catch (error) {
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
  getForTrade: () => request(`${API_BASE}/players/for-trade`),
  get: (id) => request(`${API_BASE}/players/${id}`),
  getDetails: (id) => request(`${API_BASE}/players/${id}/details`),
  getItems: (id) => request(`${API_BASE}/players/${id}/items`),
  getResources: (id) => request(`${API_BASE}/players/${id}/resources`),
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
export const jobAPI = {
  getAll: () => request(`${API_BASE}/jobs`),
  getWithInitialItems: (id) => request(`${API_BASE}/jobs/${id}/items`)
}

export const skillAPI = {
  getAll: () => request(`${API_BASE}/skills`)
}

export const shelterAPI = {
  getSummary: (gameDay) => {
    const q = gameDay != null ? `?gameDay=${encodeURIComponent(gameDay)}` : ''
    return request(`${API_BASE}/shelter${q}`)
  },
  /** 统治者：提交劳工名单；laborers 为 { playerId, exploited? }[]，最多3人压榨 */
  setLaborRoster: (laborersOrIds, gameDay) => {
    const body = { ...(gameDay != null ? { gameDay } : {}) }
    if (Array.isArray(laborersOrIds) && laborersOrIds.length && typeof laborersOrIds[0] === 'object') {
      body.laborers = laborersOrIds
    } else {
      body.playerIds = laborersOrIds
    }
    return request(`${API_BASE}/shelter/labor/roster`, {
      method: 'PUT',
      body: JSON.stringify(body)
    })
  },
  /** DM：完整编辑劳工 */
  setDailyLabor: (laborers, gameDay) =>
    request(`${API_BASE}/shelter/labor`, {
      method: 'PUT',
      body: JSON.stringify({
        laborers,
        ...(gameDay != null ? { gameDay } : {})
      })
    }),
  previewLaborSettlement: (gameDay) => {
    const q = gameDay != null ? `?gameDay=${encodeURIComponent(gameDay)}` : ''
    return request(`${API_BASE}/shelter/labor/preview${q}`)
  },
  verifyLaborDay: (gameDay) =>
    request(`${API_BASE}/shelter/labor/verify`, {
      method: 'POST',
      body: JSON.stringify({ ...(gameDay != null ? { gameDay } : {}) })
    }),
  getItemCatalog: () => request(`${API_BASE}/shelter/catalog`),
  upsertStock: (itemType, itemId, quantity) =>
    request(`${API_BASE}/shelter/stock`, {
      method: 'PUT',
      body: JSON.stringify({ itemType, itemId, quantity })
    }),
  deleteStock: (itemType, itemId) =>
    request(`${API_BASE}/shelter/stock?itemType=${encodeURIComponent(itemType)}&itemId=${itemId}`, {
      method: 'DELETE'
    })
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

export const explorationAPI = {
  submit: (playerId, gameDay, investItems) => request(`${API_BASE}/exploration/submit`, {
    method: 'POST',
    body: JSON.stringify({ playerId, gameDay, investItems })
  }),
  getPlayerExplorations: (playerId) => request(`${API_BASE}/exploration/player/${playerId}`),
  getPendingExplorations: (gameDay) => request(`${API_BASE}/exploration/pending/${gameDay}`),
  getAllEvents: () => request(`${API_BASE}/exploration/events`),
  createEvent: (payload) => request(`${API_BASE}/exploration/events`, {
    method: 'POST',
    body: JSON.stringify(payload)
  }),
  updateEvent: (eventId, payload) => request(`${API_BASE}/exploration/events/${eventId}`, {
    method: 'PUT',
    body: JSON.stringify(payload)
  }),
  deleteEvent: (eventId) => request(`${API_BASE}/exploration/events/${eventId}`, {
    method: 'DELETE'
  }),
  triggerEvent: (explorationId, eventId) => request(`${API_BASE}/exploration/${explorationId}/trigger`, {
    method: 'POST',
    body: JSON.stringify({ eventId })
  }),
  triggerRandomEvent: (explorationId) => request(`${API_BASE}/exploration/${explorationId}/trigger`, {
    method: 'POST',
    body: JSON.stringify({})
  }),
  settle: (explorationId, rewards) => request(`${API_BASE}/exploration/${explorationId}/settle`, {
    method: 'POST',
    body: JSON.stringify({ rewards })
  }),
  reimportEvents: () => {
    const userId = localStorage.getItem('userId')
    return request(`${API_BASE}/exploration/admin/reimport`, {
      method: 'POST',
      headers: { userId }
    })
  },
  getEventRewards: (eventId) => request(`${API_BASE}/exploration/events/${eventId}/rewards`)
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

export const gameStateAPI = {
  get: () => request(`${API_BASE}/game-state`),
  update: (body, userRole) =>
    request(`${API_BASE}/game-state?userRole=${encodeURIComponent(userRole || '')}`, {
      method: 'PUT',
      body: JSON.stringify(body)
    })
}

export const loreAPI = {
  getCatalog: (userRole, playerId) => {
    const q = new URLSearchParams()
    if (userRole) q.set('userRole', userRole)
    if (playerId != null) q.set('playerId', String(playerId))
    return request(`${API_BASE}/lore/catalog?${q}`)
  },
  grantPlayer: (loreSlug, playerId, userRole) => {
    const q = new URLSearchParams({ loreSlug, playerId: String(playerId) })
    if (userRole) q.set('userRole', userRole)
    return request(`${API_BASE}/lore/grant-player?${q}`, { method: 'POST' })
  },
  revokePlayer: (loreSlug, playerId, userRole) => {
    const q = new URLSearchParams({ loreSlug, playerId: String(playerId) })
    if (userRole) q.set('userRole', userRole)
    return request(`${API_BASE}/lore/revoke-player?${q}`, { method: 'POST' })
  },
  canAccess: (loreSlug, userRole, playerId) => {
    const q = new URLSearchParams({ loreSlug })
    if (userRole) q.set('userRole', userRole)
    if (playerId != null) q.set('playerId', String(playerId))
    return request(`${API_BASE}/lore/access?${q}`)
  },
  acknowledge: (loreSlug, playerId) => {
    const q = new URLSearchParams({ loreSlug, playerId: String(playerId) })
    return request(`${API_BASE}/lore/acknowledge?${q}`, { method: 'POST' })
  },
}

export const dmActivityLogAPI = {
  list: ({ userRole = 'dm', gameDay, limit = 500, playerId, faction } = {}) => {
    const q = new URLSearchParams({ userRole })
    if (gameDay != null) q.set('gameDay', String(gameDay))
    if (limit != null) q.set('limit', String(limit))
    if (playerId != null) q.set('playerId', String(playerId))
    if (faction) q.set('faction', faction)
    return request(`${API_BASE}/dm/activity-log?${q}`)
  },
}

export const playerConsumptionAPI = {
  getContext: (playerId, gameDay) => {
    const q = new URLSearchParams({ playerId: String(playerId) })
    if (gameDay != null) q.set('gameDay', String(gameDay))
    return request(`${API_BASE}/player-consumption/context?${q}`)
  },
  submit: (body) =>
    request(`${API_BASE}/player-consumption/submit`, {
      method: 'POST',
      body: JSON.stringify(body)
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

export const dmPlayerAPI = {
  list: () => request(`${API_BASE}/dm/players?userRole=dm`),
  create: (body) =>
    request(`${API_BASE}/dm/players?userRole=dm`, {
      method: 'POST',
      body: JSON.stringify(body)
    }),
  update: (playerId, body) =>
    request(`${API_BASE}/dm/players/${playerId}?userRole=dm`, {
      method: 'PUT',
      body: JSON.stringify(body)
    }),
  delete: (playerId) =>
    request(`${API_BASE}/dm/players/${playerId}?userRole=dm`, { method: 'DELETE' }),
  previewStartingInventory: (jobId) =>
    request(`${API_BASE}/dm/jobs/${jobId}/starting-inventory-preview?userRole=dm`),
  grantStartingInventory: (playerId, mode = 'add') =>
    request(
      `${API_BASE}/dm/players/${playerId}/grant-starting-inventory?userRole=dm&mode=${encodeURIComponent(mode)}`,
      { method: 'POST' }
    ),
  applyInventoryBulk: (playerId, items, mode = 'set') =>
    request(`${API_BASE}/dm/players/${playerId}/inventory/bulk?userRole=dm&mode=${encodeURIComponent(mode)}`, {
      method: 'PUT',
      body: JSON.stringify({ items })
    }),
  getCatalog: () => request(`${API_BASE}/dm/item-catalog?userRole=dm`),
  getInventory: (playerId) =>
    request(`${API_BASE}/dm/players/${playerId}/inventory?userRole=dm`),
  setItemQuantity: (playerId, itemType, itemId, quantity) =>
    request(`${API_BASE}/dm/players/${playerId}/inventory?userRole=dm`, {
      method: 'PUT',
      body: JSON.stringify({ itemType, itemId, quantity })
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

export const nightActionAPI = {
  getContext: (playerId, gameDay = 1) =>
    request(`${API_BASE}/night-actions/context/${playerId}?gameDay=${gameDay}`),
  submitAction: (data) =>
    request(`${API_BASE}/night-actions/submit`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    }),
  getAllActions: (params = {}) => {
    const qs = Object.entries(params)
      .filter(([, v]) => v != null && v !== '')
      .map(([k, v]) => `${k}=${encodeURIComponent(v)}`)
      .join('&')
    return request(`${API_BASE}/night-actions/all${qs ? '?' + qs : ''}`)
  },
  feedbackAction: (actionId, feedback) =>
    request(`${API_BASE}/night-actions/${actionId}/feedback`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ feedback }),
    }),
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

export const quickInteractionAPI = {
  getContext: (playerId, gameDay) =>
    request(`${API_BASE}/quick-interactions/context/${playerId}${gameDay ? '?gameDay=' + gameDay : ''}`),
  submit: (data) =>
    request(`${API_BASE}/quick-interactions/submit`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    }),
  getAll: (params = {}) => {
    const qs = Object.entries(params)
      .filter(([, v]) => v != null && v !== '')
      .map(([k, v]) => `${k}=${encodeURIComponent(v)}`)
      .join('&')
    return request(`${API_BASE}/quick-interactions/all${qs ? '?' + qs : ''}`)
  },
  reply: (interactionId, reply) =>
    request(`${API_BASE}/quick-interactions/${interactionId}/reply`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', 'X-User-Role': localStorage.getItem('userRole') || '' },
      body: JSON.stringify({ reply }),
    }),
  updateStatus: (interactionId, status) =>
    request(`${API_BASE}/quick-interactions/${interactionId}/status`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', 'X-User-Role': localStorage.getItem('userRole') || '' },
      body: JSON.stringify({ status }),
    }),
}

export const actionAPI = {
  submitAction: (data) =>
    request(`${API_BASE}/actions/submit`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data)
    }),
  getSubmitContext: (playerId, gameDay = 1) =>
    request(`${API_BASE}/actions/submit-context?playerId=${encodeURIComponent(playerId)}&gameDay=${encodeURIComponent(gameDay)}`),
  getPlayerActions: (playerId, gameDay = 1) =>
    request(`${API_BASE}/actions/player/${playerId}?gameDay=${gameDay}`),
  approveAction: (actionId) =>
    request(`${API_BASE}/actions/${actionId}/approve`, { method: 'POST' }),
  getAllActions: (params = {}) => {
    const qs = Object.entries(params).filter(([, v]) => v != null && v !== '').map(([k, v]) => `${k}=${encodeURIComponent(v)}`).join('&')
    return request(`${API_BASE}/actions/all${qs ? '?' + qs : ''}`)
  },
  feedbackAction: (actionId, feedback, failed = false) =>
    request(`${API_BASE}/actions/${actionId}/feedback`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ feedback, failed: Boolean(failed) }),
    }),
  batchResolveInvestigate: (gameDay = 1) =>
    request(`${API_BASE}/actions/resolve/investigate?gameDay=${gameDay}`, { method: 'POST' }),
  batchResolveProduce: (gameDay = 1) =>
    request(`${API_BASE}/actions/resolve/produce?gameDay=${gameDay}`, { method: 'POST' }),
  batchResolveAll: (gameDay = 1) =>
    request(`${API_BASE}/actions/resolve/all?gameDay=${gameDay}`, { method: 'POST' }),
  updateAction: (actionId, data) =>
    request(`${API_BASE}/actions/${actionId}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    }),
  resolveTransport: (actionId) =>
    request(`${API_BASE}/actions/resolve/transport/${actionId}`, { method: 'POST' }),
  publishFeedback: (gameDay = 1) =>
    request(`${API_BASE}/actions/publish?gameDay=${encodeURIComponent(gameDay)}`, { method: 'POST' }),
  getProductionInfo: (playerId) =>
    request(`${API_BASE}/actions/production-info/${playerId}`),
  checkStealth: (playerId, gameDay = 1) =>
    request(`${API_BASE}/actions/stealth/${playerId}?gameDay=${gameDay}`)
}

export const endgameAPI = {
  drawShelterEvent: () =>
    request(`${API_BASE}/endgame/shelter/draw`),
  drawArkEvent: () =>
    request(`${API_BASE}/endgame/ark/draw`),
  getAllShelterEvents: () =>
    request(`${API_BASE}/endgame/shelter/all`),
  getAllArkEvents: () =>
    request(`${API_BASE}/endgame/ark/all`),
}

export const npcAPI = {
  getAllNpcs: (playerId) => {
    const q = playerId != null ? `?playerId=${playerId}` : ''
    return request(`${API_BASE}/npc/list${q}`)
  },
  getNpcsByLocation: (locationId, playerId) => {
    const q = playerId != null ? `?playerId=${playerId}` : ''
    return request(`${API_BASE}/npc/location/${locationId}${q}`)
  },
  getNpcDetail: (npcId, playerId) => {
    const q = playerId != null ? `?playerId=${playerId}` : ''
    return request(`${API_BASE}/npc/${npcId}${q}`)
  },
  sendMessage: (playerId, npcId, message) =>
    request(`${API_BASE}/npc/chat`, {
      method: 'POST',
      body: JSON.stringify({ playerId, npcId, message })
    }),
  getDialogueHistory: (playerId, npcId) => {
    const q = npcId != null ? `?playerId=${playerId}&npcId=${npcId}` : `?playerId=${playerId}`
    return request(`${API_BASE}/npc/dialogue/history${q}`)
  },
  getPlayerFavors: (playerId) =>
    request(`${API_BASE}/npc/favors/${playerId}`),
  setFavor: (npcId, playerId, favorValue) => {
    const userId = localStorage.getItem('userId')
    return request(`${API_BASE}/npc/favor/set`, {
      method: 'POST',
      headers: { userId },
      body: JSON.stringify({ npcId, playerId, favorValue })
    })
  },
  getTradeConfig: (playerId, npcId) =>
    request(`${API_BASE}/npc/trade/config?playerId=${playerId}&npcId=${npcId}`),
  executeTrade: (playerId, npcId) =>
    request(`${API_BASE}/npc/trade/execute`, {
      method: 'POST',
      body: JSON.stringify({ playerId, npcId })
    }),
  getTradeHistory: (playerId, npcId) => {
    const q = npcId != null ? `?playerId=${playerId}&npcId=${npcId}` : `?playerId=${playerId}`
    return request(`${API_BASE}/npc/trade/history${q}`)
  },
  getAllTradeConfigs: () =>
    request(`${API_BASE}/npc/trade/dm/configs`),
  saveTradeConfig: (npcId, demandItems, supplyItems) =>
    request(`${API_BASE}/npc/trade/dm/save-config`, {
      method: 'POST',
      body: JSON.stringify({ npcId, demandItems, supplyItems })
    }),
  setDailyTradeLimit: (npcId, limit) =>
    request(`${API_BASE}/npc/trade/dm/set-limit`, {
      method: 'POST',
      body: JSON.stringify({ npcId, limit })
    }),
  claimFreeReward: (playerId, npcId) =>
    request(`${API_BASE}/npc/trade/claim-free-reward`, {
      method: 'POST',
      body: JSON.stringify({ playerId, npcId })
    }),
  // 对话限制API
  getDialogueLimit: (playerId, npcId) =>
    request(`${API_BASE}/npc/dialogue/limit?playerId=${playerId}&npcId=${npcId}`),
  getAllDialogueCounts: (playerId) =>
    request(`${API_BASE}/npc/dialogue/counts?playerId=${playerId}`),
  resetDialogueCounts: (playerId, userRole) =>
    request(`${API_BASE}/npc/dialogue/reset`, {
      method: 'POST',
      body: JSON.stringify({ playerId, userRole })
    }),
  // 求助API
  getHelpOptions: (playerId, npcId) =>
    request(`${API_BASE}/npc/help/options?playerId=${playerId}&npcId=${npcId}`),
  requestHelp: (playerId, npcId, helpType) =>
    request(`${API_BASE}/npc/help/request`, {
      method: 'POST',
      body: JSON.stringify({ playerId, npcId, helpType })
    }),
  getHelpHistory: (playerId, npcId) => {
    const q = npcId != null ? `?playerId=${playerId}&npcId=${npcId}` : `?playerId=${playerId}`
    return request(`${API_BASE}/npc/help/history${q}`)
  },
  getPendingHelps: (playerId) =>
    request(`${API_BASE}/npc/help/pending?playerId=${playerId}`),
  // DM求助配置API
  getAllHelpConfigs: () =>
    request(`${API_BASE}/npc/help/dm/configs`),
  saveHelpConfig: (npcId, helpConfigs) =>
    request(`${API_BASE}/npc/help/dm/save-config`, {
      method: 'POST',
      body: JSON.stringify({ npcId, helpConfigs })
    }),
  deleteHelpConfig: (configId) =>
    request(`${API_BASE}/npc/help/dm/config/${configId}`, {
      method: 'DELETE'
    }),
  // 认知系统API
  getRecognizedNpcs: (playerId) =>
    request(`${API_BASE}/npc/cognition/recognized?playerId=${playerId}`),
  checkRecognition: (playerId, npcId) =>
    request(`${API_BASE}/npc/cognition/check?playerId=${playerId}&npcId=${npcId}`),
  getCognitionStats: (playerId) =>
    request(`${API_BASE}/npc/cognition/stats?playerId=${playerId}`),
  updateFavor: (playerId, npcId, delta) =>
    request(`${API_BASE}/npc/cognition/favor/update`, {
      method: 'POST',
      body: JSON.stringify({ playerId, npcId, delta })
    }),
  getFavor: (playerId, npcId) =>
    request(`${API_BASE}/npc/cognition/favor?playerId=${playerId}&npcId=${npcId}`),
  forceRecognizeNpc: (playerId, npcId) =>
    request(`${API_BASE}/npc/cognition/force-recognize`, {
      method: 'POST',
      body: JSON.stringify({ playerId, npcId })
    }),
  resetRecognition: (playerId, userRole) =>
    request(`${API_BASE}/npc/cognition/reset`, {
      method: 'POST',
      body: JSON.stringify({ playerId, userRole })
    }),
  // NPC管理API（DM用）
  getAllNpcsForDm: () => {
    const userId = localStorage.getItem('userId')
    return request(`${API_BASE}/npc/manage/all`, { headers: { userId } })
  },
  getNpcDetailForDm: (npcId) => {
    const userId = localStorage.getItem('userId')
    return request(`${API_BASE}/npc/manage/${npcId}`, { headers: { userId } })
  },
  updateNpc: (npcData) => {
    const userId = localStorage.getItem('userId')
    return request(`${API_BASE}/npc/manage/update`, {
      method: 'POST',
      headers: { userId },
      body: JSON.stringify(npcData)
    })
  },
  createNpc: (npcData) => {
    const userId = localStorage.getItem('userId')
    return request(`${API_BASE}/npc/manage/create`, {
      method: 'POST',
      headers: { userId },
      body: JSON.stringify(npcData)
    })
  },
  deleteNpc: (npcId) => {
    const userId = localStorage.getItem('userId')
    return request(`${API_BASE}/npc/manage/${npcId}`, {
      method: 'DELETE',
      headers: { userId }
    })
  },
  getAllLocations: () => {
    const userId = localStorage.getItem('userId')
    return request(`${API_BASE}/npc/manage/locations`, { headers: { userId } })
  },
  batchUpdateNpcStatus: (npcIds, status) => {
    const userId = localStorage.getItem('userId')
    return request(`${API_BASE}/npc/manage/batch-status`, {
      method: 'POST',
      headers: { userId },
      body: JSON.stringify({ npcIds, status })
    })
  },
  getNpcStats: () => {
    const userId = localStorage.getItem('userId')
    return request(`${API_BASE}/npc/manage/stats`, { headers: { userId } })
  },
  // 好感度管理API（DM用）
  getAllFavors: () => {
    const userId = localStorage.getItem('userId')
    return request(`${API_BASE}/npc/manage/favors/all`, { headers: { userId } })
  },
  getFavorsByNpc: (npcId) => {
    const userId = localStorage.getItem('userId')
    return request(`${API_BASE}/npc/manage/favors/npc/${npcId}`, { headers: { userId } })
  },
  getFavorsByPlayer: (playerId) => {
    const userId = localStorage.getItem('userId')
    return request(`${API_BASE}/npc/manage/favors/player/${playerId}`, { headers: { userId } })
  },
  adjustFavor: (npcId, playerId, newValue, reason, operatorId, operatorName) => {
    const userId = localStorage.getItem('userId')
    return request(`${API_BASE}/npc/manage/favor/adjust`, {
      method: 'POST',
      headers: { userId },
      body: JSON.stringify({ npcId, playerId, newValue, reason, operatorId, operatorName })
    })
  },
  getFavorAdjustments: (npcId, playerId, page, size) => {
    const userId = localStorage.getItem('userId')
    let url = `${API_BASE}/npc/manage/favor/adjustments?`
    if (npcId) url += `npcId=${npcId}&`
    if (playerId) url += `playerId=${playerId}&`
    url += `page=${page || 0}&size=${size || 50}`
    return request(url, { headers: { userId } })
  },
  getAllPlayers: () => {
    const userId = localStorage.getItem('userId')
    return request(`${API_BASE}/npc/manage/players`, { headers: { userId } })
  },
  resetAllFavors: (userRole) =>
    request(`${API_BASE}/npc/manage/favor/reset`, {
      method: 'POST',
      body: JSON.stringify({ userRole })
    }),
  getRecognizedPlayers: (npcId) => {
    const userId = localStorage.getItem('userId')
    return request(`${API_BASE}/npc/manage/recognition/npc/${npcId}`, { headers: { userId } })
  },
  createRecognition: (npcId, playerId) => {
    const userId = localStorage.getItem('userId')
    return request(`${API_BASE}/npc/manage/recognition/create`, {
      method: 'POST',
      body: JSON.stringify({ npcId, playerId }),
      headers: { userId }
    })
  },
  deleteRecognition: (npcId, playerId) => {
    const userId = localStorage.getItem('userId')
    return request(`${API_BASE}/npc/manage/recognition/delete`, {
      method: 'POST',
      body: JSON.stringify({ npcId, playerId }),
      headers: { userId }
    })
  }
}

export const specialClueAPI = {
  getAll: () => request(`${API_BASE}/special-clue/all`),
  get: (id) => request(`${API_BASE}/special-clue/${id}`),
  create: (data) => request(`${API_BASE}/special-clue/create`, {
    method: 'POST',
    body: JSON.stringify(data)
  }),
  update: (id, data) => request(`${API_BASE}/special-clue/update/${id}`, {
    method: 'POST',
    body: JSON.stringify(data)
  }),
  delete: (id) => request(`${API_BASE}/special-clue/${id}`, {
    method: 'DELETE'
  }),
  getLogs: (playerId, clueId) => {
    let url = `${API_BASE}/special-clue/logs`
    const params = []
    if (playerId) params.push(`playerId=${playerId}`)
    if (clueId) params.push(`clueId=${clueId}`)
    if (params.length) url += `?${params.join('&')}`
    return request(url)
  },
  export: () => request(`${API_BASE}/special-clue/export`),
  import: (data) => request(`${API_BASE}/special-clue/import`, {
    method: 'POST',
    body: JSON.stringify(data)
  })
}