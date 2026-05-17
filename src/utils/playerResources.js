/** material.id in DB — 食物 */
export const PERSONAL_FOOD_MATERIAL_ID = 5
/** material.id in DB — 燃料/煤油 */
export const PERSONAL_FUEL_MATERIAL_ID = 8

/**
 * Convert a material quantity to kilograms (food & fuel are stored as kg in `material` table).
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

/**
 * @param {Array} items — from `playerAPI.getItems`
 * @returns {{ food: number, fuel: number }}
 */
export function sumPersonalFoodAndFuel(items) {
  return {
    food: sumPlayerMaterialsKg(items, [PERSONAL_FOOD_MATERIAL_ID]),
    fuel: sumPlayerMaterialsKg(items, [PERSONAL_FUEL_MATERIAL_ID])
  }
}

/**
 * @param {number} kg
 * @returns {number} whole kg for dashboard display
 */
export function formatKgForDisplay(kg) {
  return Math.round(kg)
}
