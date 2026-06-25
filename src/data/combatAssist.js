/** DM 战斗辅助系统：武器威胁、远近程、结算表 */

/** 与数据库 weapon.threat_level 一致 */
export const WEAPON_THREAT_BY_ID = {
  1: 4,
  2: 6,
  3: 1,
  4: 2,
  5: 2,
  6: 4,
  7: 4,
  8: 1,
  9: 2,
  10: 4,
  11: 1,
  12: 10,
  13: 4,
}

/** 远程武器 id（射击技能加成） */
export const RANGED_WEAPON_IDS = new Set([1, 2, 7])

export function isMeleeWeapon(weaponId) {
  if (weaponId === '' || weaponId == null) return false
  return !RANGED_WEAPON_IDS.has(Number(weaponId))
}

export function isRangedWeapon(weaponId) {
  if (weaponId === '' || weaponId == null) return false
  return RANGED_WEAPON_IDS.has(Number(weaponId))
}

export const ARMOR_ITEM_IDS = {
  bodyArmor: 5,
  shield: 6,
}

export const DEFAULT_BONUS_LABEL = '1d4'

export const COMBAT_OUTCOMES = [
  { name: '大胜', attacker: '无伤', defender: '1人死亡，其余受伤' },
  { name: '胜利', attacker: '无伤', defender: '1-2人重伤，其余受伤' },
  { name: '小胜', attacker: '无伤', defender: '全部受伤' },
  { name: '僵持', attacker: '1人受伤', defender: '1人受伤' },
  { name: '小败', attacker: '全部受伤', defender: '无伤' },
  { name: '失败', attacker: '1-2人受伤，其余受伤', defender: '无伤' },
  { name: '大败', attacker: '1人死亡，其余受伤', defender: '无伤' },
]

export function parseCombatNumber(val) {
  if (val === '' || val === null || val === undefined) return 0
  const n = Number(val)
  return Number.isFinite(n) ? n : 0
}

export function parseJobSkills(jobSkillsText) {
  return String(jobSkillsText || '')
    .split(/[,，、]/)
    .map((s) => s.trim())
    .filter(Boolean)
}

export function hasMeleeCombatSkill(skills) {
  return skills.some((s) => s === '格斗' || s === '斗殴')
}

export function hasRangedCombatSkill(skills) {
  return skills.some((s) => s === '射击')
}

const MELEE_BLOCKING_STATUS_KEYS = new Set(['dead', 'severely_injured', 'weak', 'injured'])
const RANGED_BLOCKING_STATUS_KEYS = new Set(['dead', 'severely_injured', 'weak'])

function blockingStatusNamesForCombat(fighter, kind) {
  const keySet = kind === 'melee' ? MELEE_BLOCKING_STATUS_KEYS : RANGED_BLOCKING_STATUS_KEYS
  const fromList = (fighter.statuses || [])
    .filter((s) => keySet.has(s.key))
    .map((s) => s.name)
    .filter(Boolean)
  if (fromList.length) return fromList
  const names = []
  if (fighter.isDead) names.push('死亡')
  if (fighter.isSeverelyInjured) names.push('重伤')
  if (fighter.isWeak) names.push('虚弱')
  if (kind === 'melee' && fighter.isInjured) names.push('受伤')
  return names
}

/** When skill bonus would apply but status blocks it — for combat assist UI warning */
export function getSkillBonusSuppression(fighter) {
  if (!fighter) return null
  const skills = parseJobSkills(fighter.jobSkills)
  const weaponId = fighter.weaponId
  const meleeWould = hasMeleeCombatSkill(skills) && isMeleeWeapon(weaponId)
  const rangedWould = hasRangedCombatSkill(skills) && isRangedWeapon(weaponId)
  if (!meleeWould && !rangedWould) return null

  const meleeDisabled = Boolean(fighter.combatMeleeDisabled ?? fighter.meleeDisabled)
  const rangedDisabled = Boolean(fighter.combatRangedDisabled ?? fighter.rangedDisabled)

  const lines = []
  if (meleeWould && meleeDisabled) {
    const names = blockingStatusNamesForCombat(fighter, 'melee')
    if (names.length) {
      lines.push(`${names.join('、')}状态使你无法获得格斗技能战力加成`)
    }
  }
  if (rangedWould && rangedDisabled) {
    const names = blockingStatusNamesForCombat(fighter, 'ranged')
    if (names.length) {
      lines.push(`${names.join('、')}状态使你无法获得射击技能战力加成`)
    }
  }
  if (!lines.length) return null
  return { tooltip: lines.join('；') }
}

export function computeSkillBonus(jobSkillsText, weaponId, options = {}) {
  const meleeDisabled = Boolean(options.meleeDisabled ?? options.combatMeleeDisabled)
  const rangedDisabled = Boolean(options.rangedDisabled ?? options.combatRangedDisabled)
  const skills = parseJobSkills(jobSkillsText)
  if (hasMeleeCombatSkill(skills) && isMeleeWeapon(weaponId) && !meleeDisabled) return 1
  if (hasRangedCombatSkill(skills) && isRangedWeapon(weaponId) && !rangedDisabled) return 1
  return 0
}

export function fighterTotalPower(fighter) {
  let sum =
    parseCombatNumber(fighter.basePower) +
    parseCombatNumber(fighter.weaponThreat) +
    parseCombatNumber(fighter.skillBonus)
  for (const b of fighter.bonuses || []) {
    sum += parseCombatNumber(b.value)
  }
  return sum
}

export function computeAdjustedValue(powerDiff, totalFighters) {
  if (!Number.isFinite(powerDiff)) return 0
  if (!Number.isFinite(totalFighters) || totalFighters <= 0) return powerDiff
  return Math.round((powerDiff * 3) / totalFighters * 10) / 10
}

export function resolveCombatOutcome(powerDiff, totalFighters) {
  const adjustedValue = computeAdjustedValue(powerDiff, totalFighters)
  if (!Number.isFinite(adjustedValue)) return { ...COMBAT_OUTCOMES[3], adjustedValue: 0 }
  
  if (adjustedValue > 0) {
    if (adjustedValue >= 7) return { ...COMBAT_OUTCOMES[0], adjustedValue }
    if (adjustedValue >= 4) return { ...COMBAT_OUTCOMES[1], adjustedValue }
    if (adjustedValue >= 1) return { ...COMBAT_OUTCOMES[2], adjustedValue }
  } else if (adjustedValue < 0) {
    if (adjustedValue <= -7) return { ...COMBAT_OUTCOMES[6], adjustedValue }
    if (adjustedValue <= -4) return { ...COMBAT_OUTCOMES[5], adjustedValue }
    if (adjustedValue <= -1) return { ...COMBAT_OUTCOMES[4], adjustedValue }
  }
  return { ...COMBAT_OUTCOMES[3], adjustedValue }
}

export function createEmptyFighter(playerId, playerName, jobSkills = '') {
  return {
    uid: `${Date.now()}-${Math.random().toString(36).slice(2, 9)}`,
    playerId,
    playerName,
    jobSkills,
    weaponId: '',
    weaponThreat: '',
    skillBonus: 0,
    basePower: 1,
    bodyArmorCount: 0,
    shieldCount: 0,
    bonuses: [{ uid: `b-${Date.now()}`, label: DEFAULT_BONUS_LABEL, value: '' }],
    inventoryLoading: false,
  }
}

export function createDefaultBonusRow() {
  return {
    uid: `b-${Date.now()}-${Math.random().toString(36).slice(2, 6)}`,
    label: '',
    value: '',
  }
}
