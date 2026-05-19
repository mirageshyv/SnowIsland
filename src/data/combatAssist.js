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

export function computeSkillBonus(jobSkillsText, weaponId) {
  const skills = String(jobSkillsText || '')
    .split(/[,，、]/)
    .map((s) => s.trim())
    .filter(Boolean)
  const hasMeleeSkill = skills.some((s) => s === '格斗' || s === '斗殴')
  const hasRangedSkill = skills.some((s) => s === '射击')
  if (hasMeleeSkill && isMeleeWeapon(weaponId)) return 1
  if (hasRangedSkill && isRangedWeapon(weaponId)) return 1
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

export function resolveCombatOutcome(diff) {
  const d = Number(diff)
  if (!Number.isFinite(d)) return COMBAT_OUTCOMES[3]
  if (d >= 5) return COMBAT_OUTCOMES[0]
  if (d >= 3) return COMBAT_OUTCOMES[1]
  if (d >= 1) return COMBAT_OUTCOMES[2]
  if (d === 0) return COMBAT_OUTCOMES[3]
  if (d >= -2) return COMBAT_OUTCOMES[4]
  if (d >= -4) return COMBAT_OUTCOMES[5]
  return COMBAT_OUTCOMES[6]
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
