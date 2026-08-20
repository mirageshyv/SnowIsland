/** Shared daytime transport weight preview + notes encoding (server resolveMaxWeight is authoritative). */

export const TRANSPORT_WEIGHT_MAP = {
  material: { 1: 1, 2: 1, 3: 0.5, 4: 1, 5: 1, 7: 1, 8: 1, 12: 50 },
  item: { 1: 0.5, 2: 0.3, 3: 0.5, 4: 0.2, 5: 2, 6: 3, 7: 1, 8: 1, 9: 0.1, 10: 1, 11: 0.5, 12: 0.5, 13: 0.1, 14: 1, 15: 0.2, 16: 0.2, 17: 0.3, 18: 0.5, 19: 0.1, 20: 0.1, 21: 0.1, 22: 0.1, 23: 0.1, 24: 0.1, 55: 0.1 },
  weapon: { 1: 2, 2: 3, 3: 1, 4: 1, 5: 1, 6: 2, 7: 2, 8: 3, 9: 2, 10: 5, 11: 0.5, 12: 1, 13: 3 },
  ammo: { 1: 0.1, 2: 0.1, 3: 0.1, 4: 0.1 },
}

export function getTransportWeightPerUnit(itemType, itemId) {
  return (TRANSPORT_WEIGHT_MAP[itemType] && TRANSPORT_WEIGHT_MAP[itemType][itemId]) || 1
}

export function warehouseAreaKey(key) {
  if (!key) return '小镇'
  if (key === 'shelter' || key === 'general') return '海岛'
  return '小镇'
}

/** Map legacy 避难所-only modes onto ordinary warehouse routes. Mutates and returns `parsed`. */
export function normalizeShelterTransportAlias(parsed) {
  if (!parsed || !parsed.mode) return parsed
  switch (parsed.mode) {
    case 'warehouse_to_shelter':
      parsed.mode = 'warehouse_to_warehouse'
      if (!parsed.dest) parsed.dest = 'shelter'
      break
    case 'shelter_to_warehouse':
      parsed.mode = 'warehouse_to_warehouse'
      if (!parsed.source) parsed.source = 'shelter'
      break
    case 'shelter_to_player':
      parsed.mode = 'warehouse_to_player'
      if (!parsed.source) parsed.source = 'shelter'
      break
    case 'player_to_shelter':
      parsed.mode = 'player_to_warehouse'
      if (!parsed.dest) parsed.dest = 'shelter'
      break
    default:
      break
  }
  return parsed
}

/** Frontend preview only. 装卸工 ×2 is server-only. Player↔warehouse free is 50kg both 小镇 and 海岛. */
export function resolveTransportMaxWeight({ mode, source, dest, free }) {
  const normalized = normalizeShelterTransportAlias({ mode, source, dest })
  const warehouseLink = normalized.mode === 'warehouse_to_warehouse'
  let area = '小镇'
  if (['warehouse_to_warehouse', 'warehouse_to_player'].includes(normalized.mode)) {
    area = warehouseAreaKey(normalized.source)
  } else if (normalized.mode === 'player_to_warehouse') {
    area = warehouseAreaKey(normalized.dest)
  }
  if (warehouseLink && (warehouseAreaKey(normalized.source) === '海岛' || warehouseAreaKey(normalized.dest) === '海岛')) {
    area = '海岛'
  }
  const island = area === '海岛'
  if (warehouseLink) return free ? (island ? 50 : 100) : (island ? 300 : 500)
  return free ? 50 : 300
}

export function getTransportTotalWeight(items) {
  if (!Array.isArray(items)) return 0
  return items.reduce((sum, item) => sum + (item.quantity || 0) * (item.weightPerUnit || 0), 0)
}

export function buildTransportNotes({ mode, source, dest, items, tier }) {
  const lines = []
  const normalized = normalizeShelterTransportAlias({ mode, source, dest })
  const m = normalized.mode || ''
  lines.push(`[mode:${m}]`)
  lines.push(`[tier:${tier || 'action'}]`)
  if (['warehouse_to_warehouse', 'warehouse_to_player'].includes(m) && normalized.source) {
    lines.push(`[source:${normalized.source}]`)
  }
  if (['warehouse_to_warehouse', 'player_to_warehouse'].includes(m) && normalized.dest) {
    lines.push(`[dest:${normalized.dest}]`)
  }
  if (Array.isArray(items)) {
    for (const item of items) {
      if (item.quantity > 0) {
        lines.push(`[item:${item.itemType}|${item.itemId}|${item.quantity}|${item.weightPerUnit}]`)
      }
    }
  }
  return lines.join('\n')
}
