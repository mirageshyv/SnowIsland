/**
 * Convert a material quantity to kilograms (food & fuel rows use kg/L/份 units).
 * @param {number|string} quantity
 * @param {string} [unit]
 */
export function quantityToKg(quantity, unit = 'kg') {
  const q = Number(quantity) || 0
  const u = String(unit || 'kg').trim().toLowerCase()
  if (u === 'g') return q / 1000
  if (u === 'kg' || u === '千克') return q
  return q
}

/**
 * Sum player inventory rows for given material ids (merges duplicate DB rows).
 * @param {Array<{ type?: string, id?: number|string, quantity?: number, unit?: string }>} items
 * @param {number[]} materialIds
 */
export function sumPlayerMaterialsKg(items, materialIds) {
  if (!Array.isArray(items) || !materialIds?.length) return 0
  const idSet = new Set(materialIds.map((id) => Number(id)))
  let total = 0
  for (const item of items) {
    if (item?.type !== 'material') continue
    const itemId = Number(item.id)
    if (!Number.isFinite(itemId) || !idSet.has(itemId)) continue
    total += quantityToKg(item.quantity, item.unit)
  }
  return total
}

/** Material id for 木材 (burnable as fuel). */
export const WOOD_MATERIAL_ID = 2
export const FUEL_MATERIAL_ID = 8

/**
 * Fallback when `/resources` is unavailable — sums food / fuel / wood from getItems.
 * @param {Array} items — from `playerAPI.getItems`
 * @returns {{ food: number, fuel: number, wood: number }}
 */
export function sumPersonalFoodAndFuel(items) {
  if (!Array.isArray(items)) return { food: 0, fuel: 0, wood: 0 }
  let food = 0
  let fuel = 0
  let wood = 0
  for (const item of items) {
    const q = quantityToKg(item.quantity, item.unit)
    const id = Number(item.id)
    if (item.type === 'material' && id === 5) food += q
    if (item.type === 'material' && id === FUEL_MATERIAL_ID) fuel += q
    if (item.type === 'material' && id === WOOD_MATERIAL_ID) wood += q
  }
  return { food, fuel, wood }
}

/**
 * @param {number} kg
 * @returns {number} whole kg for dashboard display
 */
export function formatKgForDisplay(kg) {
  return Math.round(kg)
}
