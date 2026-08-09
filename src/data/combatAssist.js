/** DM 战斗辅助：图鉴威胁值为机械真相；规则按威胁值查命中表 */

/**
 * 硬编码回退表（数据库图鉴 weapon.threat_level 为真相；
 * 页面加载图鉴后通过 setCatalogWeaponThreats 覆盖本表）。
 */
export const WEAPON_THREAT_BY_ID = {
  1: 5, // 制式手枪
  2: 6, // 猎枪
  3: 1, // 警棍
  4: 2, // 刺刀
  5: 2, // 水手刀（规则索引可称水果刀）
  6: 3, // 鱼叉/矛
  7: 4, // 猎弓
  8: 1, // 十字镐
  9: 2, // 斧头
  10: 4, // 电锯
  11: 1, // 手术刀
  12: 10, // 炸药（内围默认）
  13: 1, // 电钻
}

/** 图鉴威胁值覆盖（运行时从数据库加载） */
const catalogThreatById = {}

/**
 * 注入数据库图鉴威胁值（真相来源）。
 * @param {Array<{id:number, threat:number}>|Record<number, number>} weapons
 */
export function setCatalogWeaponThreats(weapons) {
  const entries = Array.isArray(weapons)
    ? weapons.map((w) => [w.id, w.threat])
    : Object.entries(weapons || {})
  for (const [id, threat] of entries) {
    const key = Number(id)
    const val = Number(threat)
    if (Number.isFinite(key) && key > 0 && Number.isFinite(val) && val >= 0) {
      catalogThreatById[key] = val
    }
  }
}

/** 图鉴优先，硬编码回退 */
export function baseWeaponThreat(weaponId) {
  const id = Number(weaponId)
  return catalogThreatById[id] ?? WEAPON_THREAT_BY_ID[id] ?? 0
}

export const EXPLOSIVE_WEAPON_ID = 12
export const EXPLOSIVE_THREAT = { outer: 5, inner: 10 }

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

/** 衣/盾：不加战力；一人仅能用其一；可将一次重伤降为受伤，或将一次受伤无效；每场冲突限一次 */
export const ARMOR_RULE_SUMMARY =
  '防弹衣/复合盾不加战力。一人仅能穿戴其中之一；可将一次「重伤」降级为「受伤」，或将一次「受伤」无效化；每场冲突限用一次。'

/** 数值调整行默认标签（旧版误用 1d4；攻防掷骰一律 1d6） */
export const DEFAULT_BONUS_LABEL = ''

/** 结算步骤速查（DM） */
export const COMBAT_FLOW_STEPS = [
  '确认攻守双方：人数、武器、格斗/射击、防具',
  '守方确认地点防御；是否里应外合（防御=0）；是否死战',
  '基础战力 = 人数 + 武器威胁之和 + 地点防御(守) + 技能(+1/+1)',
  '每人投 1d6，加到基础战力',
  'D = 攻最终 − 守最终；A = ceil(D × 3 ÷ 总人数)，查表',
  '攻骰 < 武器威胁 → 额外命中：目标 1d6 查表',
  '防具可抵消一次：重伤→受伤，或受伤→无效',
]

/** 额外命中表：威胁值 → 判定骰 1..6 的结果 */
export const EXTRA_HIT_TABLE = {
  1: ['未命中', '未命中', '受伤', '受伤', '受伤', '受伤'],
  2: ['未命中', '未命中', '未命中', '受伤', '受伤', '重伤'],
  3: ['未命中', '未命中', '未命中', '受伤', '受伤', '重伤'],
  4: ['未命中', '未命中', '未命中', '未命中', '重伤', '重伤'],
  5: ['未命中', '未命中', '未命中', '受伤', '受伤', '重伤'],
  6: ['未命中', '未命中', '未命中', '未命中', '重伤', '重伤+虚弱'],
  10: ['未命中', '受伤', '受伤', '重伤', '重伤', '死亡'],
}

/** 威胁值 → 图鉴武器名（规则索引；水果刀为水手刀别名） */
export const THREAT_WEAPON_INDEX = {
  1: ['警棍', '十字镐', '手术刀', '电钻'],
  2: ['斧头', '刺刀', '水手刀（水果刀）'],
  3: ['鱼叉/矛'],
  4: ['电锯', '猎弓'],
  5: ['制式手枪', '炸弹外围'],
  6: ['猎枪'],
  10: ['炸弹内围'],
}

export const COMBAT_OUTCOMES = [
  { name: '大胜', attacker: '无伤', defender: '1人死亡，其余受伤' },
  { name: '胜利', attacker: '无伤', defender: '1-2人重伤，其余受伤' },
  { name: '小胜', attacker: '无伤', defender: '全部受伤' },
  { name: '僵持', attacker: '1人受伤', defender: '1人受伤' },
  { name: '小败', attacker: '全部受伤', defender: '无伤' },
  { name: '失败', attacker: '1-2人重伤，其余受伤', defender: '无伤' },
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

/**
 * 图鉴威胁值。options.applyNoShootHalving + jobSkills：无射击技能时远程威胁向下取半（规则书150）。
 */
export function resolveWeaponThreat(weaponId, explosiveZone = 'inner', options = {}) {
  const id = Number(weaponId)
  if (!Number.isFinite(id) || id <= 0) return 0
  let threat
  if (id === EXPLOSIVE_WEAPON_ID) {
    // 内围跟随图鉴（默认10），外围按规则固定5
    threat = explosiveZone === 'outer' ? EXPLOSIVE_THREAT.outer : (baseWeaponThreat(id) || EXPLOSIVE_THREAT.inner)
  } else {
    threat = baseWeaponThreat(id)
  }
  if (
    options.applyNoShootHalving &&
    isRangedWeapon(id) &&
    !hasRangedCombatSkill(parseJobSkills(options.jobSkills))
  ) {
    threat = Math.floor(threat / 2)
  }
  return threat
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

/** 调整值 = ceil(差值 × 3 ÷ 人数)；人数无效时退回差值 */
export function computeAdjustedValue(powerDiff, totalFighters) {
  if (!Number.isFinite(powerDiff)) return 0
  if (!Number.isFinite(totalFighters) || totalFighters <= 0) return powerDiff
  return Math.ceil((powerDiff * 3) / totalFighters)
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

/**
 * 攻击骰是否触发额外命中：投掷值 < 武器威胁值（加值）
 */
export function shouldTriggerExtraHit(attackRoll, weaponThreat) {
  const roll = Number(attackRoll)
  const threat = Number(weaponThreat)
  if (!Number.isFinite(roll) || !Number.isFinite(threat) || threat <= 0) return false
  return roll >= 1 && roll <= 6 && roll < threat
}

/**
 * 查额外命中结果。defendRoll 为 1..6。
 * alreadyInjuredThisRound：本轮已在战力比拼中受伤则额外命中不再叠成重伤（未命中/死亡/重伤+虚弱仍按表；
 * 表结果为「受伤」且已受伤 → 保持受伤；「重伤」可覆盖受伤）。
 */
export function resolveExtraHit(weaponThreat, defendRoll, options = {}) {
  const threat = Number(weaponThreat)
  const roll = Number(defendRoll)
  // 命中表定义威胁档 1-6 与 10；非标准值（如管理员自定义 7）按最接近的较低档结算
  const tierKeys = Object.keys(EXTRA_HIT_TABLE).map(Number).sort((a, b) => a - b)
  const tier = tierKeys.filter((k) => k <= threat).pop()
  const table = tier != null ? EXTRA_HIT_TABLE[tier] : null
  if (!table || !Number.isFinite(roll) || roll < 1 || roll > 6) {
    return { result: null, note: '无效的威胁值或判定骰' }
  }
  const tierNote = tier !== threat ? `威胁${threat}无对应命中档，按威胁${tier}档结算` : ''
  const withTier = (note) => [tierNote, note].filter(Boolean).join('；')
  let result = table[roll - 1]
  const alreadyInjured = Boolean(options.alreadyInjuredThisRound)
  if (alreadyInjured && result === '受伤') {
    return {
      result: '受伤',
      note: withTier('本轮已在战力比拼中受伤：额外命中不再叠成重伤，维持受伤'),
    }
  }
  if (alreadyInjured && result === '重伤') {
    return { result: '重伤', note: withTier('额外命中重伤可覆盖此前的受伤') }
  }
  if (alreadyInjured && result === '重伤+虚弱') {
    return { result: '重伤+虚弱', note: withTier('额外命中重伤可覆盖此前的受伤，并附加虚弱') }
  }
  return { result, note: tierNote }
}

export function createEmptyFighter(playerId, playerName, jobSkills = '') {
  return {
    uid: `${Date.now()}-${Math.random().toString(36).slice(2, 9)}`,
    playerId,
    playerName,
    jobSkills,
    weaponId: '',
    weaponThreat: '',
    catalogThreat: '',
    threatHalved: false,
    explosiveZone: 'inner',
    skillBonus: 0,
    basePower: 1,
    bodyArmorCount: 0,
    shieldCount: 0,
    armorChoice: 'none', // none | body | shield — 一人仅能其一
    attackRoll: '',
    bonuses: [],
    inventoryLoading: false,
  }
}

/** 有效威胁值（含无射击远程减半），供 UI 与结算一致 */
export function effectiveWeaponThreatForFighter(fighter) {
  const weaponId = fighter.weaponId === '' || fighter.weaponId == null ? null : Number(fighter.weaponId)
  const zone = fighter.explosiveZone === 'outer' ? 'outer' : 'inner'
  if (weaponId != null) {
    const catalog = resolveWeaponThreat(weaponId, zone)
    const effective = resolveWeaponThreat(weaponId, zone, {
      applyNoShootHalving: true,
      jobSkills: fighter.jobSkills,
    })
    return {
      catalog,
      effective,
      halved: catalog > 0 && effective < catalog,
    }
  }
  const t = parseCombatNumber(fighter.weaponThreat)
  return { catalog: t, effective: t, halved: false }
}

export function createDefaultBonusRow() {
  return {
    uid: `b-${Date.now()}-${Math.random().toString(36).slice(2, 6)}`,
    label: '',
    value: '',
  }
}

// -----------------------------
// 自动结算引擎
// -----------------------------

export function rollD6(rng = Math.random) {
  return Math.floor(rng() * 6) + 1
}

function shuffleInPlace(arr, rng) {
  for (let i = arr.length - 1; i > 0; i--) {
    const j = Math.floor(rng() * (i + 1))
    ;[arr[i], arr[j]] = [arr[j], arr[i]]
  }
  return arr
}

function pickRandom(arr, rng) {
  if (!arr.length) return null
  return arr[Math.floor(rng() * arr.length)]
}

/** 参战单位快照（结算用） */
export function toCombatant(fighter, side) {
  const weaponId = fighter.weaponId === '' || fighter.weaponId == null ? null : Number(fighter.weaponId)
  const zone = fighter.explosiveZone === 'outer' ? 'outer' : 'inner'
  const threatInfo = effectiveWeaponThreatForFighter(fighter)
  const threat = threatInfo.effective
  const skillBonus = computeSkillBonus(fighter.jobSkills, weaponId, {
    meleeDisabled: fighter.combatMeleeDisabled,
    rangedDisabled: fighter.combatRangedDisabled,
  })
  const armorType =
    fighter.armorChoice === 'body' || fighter.armorChoice === 'shield'
      ? fighter.armorChoice
      : 'none'
  const preRoll = parseCombatNumber(fighter.attackRoll)
  const hasPreRoll = preRoll >= 1 && preRoll <= 6

  return {
    uid: fighter.uid,
    side,
    name: fighter.playerName || `单位${fighter.uid}`,
    weaponId,
    weaponThreat: threat,
    catalogThreat: threatInfo.catalog,
    threatHalved: threatInfo.halved,
    skillBonus:
      fighter.skillBonus != null && fighter.skillBonus !== ''
        ? parseCombatNumber(fighter.skillBonus)
        : skillBonus,
    basePower: parseCombatNumber(fighter.basePower) || 1,
    bonusSum: (fighter.bonuses || []).reduce((s, b) => s + parseCombatNumber(b.value), 0),
    armorType,
    armorUsed: Boolean(fighter.armorUsed),
    dead: Boolean(fighter.isDead || fighter.dead),
    injured: Boolean(fighter.isInjured || fighter.injured),
    severe: Boolean(fighter.isSeverelyInjured || fighter.severe),
    weak: Boolean(fighter.isWeak || fighter.weak),
    injuredThisRound: false,
    roll: hasPreRoll ? preRoll : null,
    rollWasManual: hasPreRoll,
  }
}

export function combatantBasePower(c) {
  if (c.dead) return 0
  return c.basePower + c.weaponThreat + c.skillBonus + c.bonusSum
}

export function statusLabel(c) {
  if (c.dead) return '死亡'
  const parts = []
  if (c.severe) parts.push('重伤')
  else if (c.injured) parts.push('受伤')
  if (c.weak) parts.push('虚弱')
  return parts.length ? parts.join('+') : '无伤'
}

export function isHealthy(c) {
  return !c.dead && !c.injured && !c.severe
}

export function isDowned(c) {
  // 死战结束条件：受伤/重伤/死亡均视为已受伤出局
  return c.dead || c.injured || c.severe
}

/**
 * 尝试用防具抵消一次伤害。
 * @returns {{ applied: string, log: string|null }} applied 为实际生效的伤害类型
 */
export function applyDamageWithArmor(combatant, damage, logs) {
  if (!damage || damage === '未命中' || combatant.dead) {
    return { applied: damage || '未命中', log: null }
  }

  let kind = damage
  let weakExtra = false
  if (damage === '重伤+虚弱') {
    kind = '重伤'
    weakExtra = true
  }

  const hasArmor = combatant.armorType !== 'none' && !combatant.armorUsed
  const armorName = combatant.armorType === 'body' ? '防弹衣' : combatant.armorType === 'shield' ? '复合盾' : ''

  if (hasArmor && kind === '受伤') {
    combatant.armorUsed = true
    const log = `${combatant.name} 的${armorName}将一次「受伤」无效化`
    if (logs) logs.push(log)
    return { applied: '未命中', log }
  }
  if (hasArmor && kind === '重伤') {
    combatant.armorUsed = true
    combatant.severe = false
    combatant.injured = true
    combatant.injuredThisRound = true
    if (weakExtra) combatant.weak = true
    const log = `${combatant.name} 的${armorName}将一次「重伤」降级为「受伤」${weakExtra ? '（仍附加虚弱）' : ''}`
    if (logs) logs.push(log)
    return { applied: weakExtra ? '受伤+虚弱' : '受伤', log }
  }

  if (kind === '死亡') {
    combatant.dead = true
    combatant.severe = false
    combatant.injured = false
    return { applied: '死亡', log: null }
  }
  if (kind === '重伤') {
    combatant.severe = true
    combatant.injured = true
    combatant.injuredThisRound = true
    if (weakExtra) combatant.weak = true
    return { applied: weakExtra ? '重伤+虚弱' : '重伤', log: null }
  }
  if (kind === '受伤') {
    combatant.injured = true
    combatant.injuredThisRound = true
    return { applied: '受伤', log: null }
  }
  return { applied: kind, log: null }
}

function assignOutcomeInjuries(sideCombatants, mode, rng, armorLogs) {
  // mode: none | all_injured | one_injured | one_dead_rest_injured | one_two_severe_rest_injured
  const living = sideCombatants.filter((c) => !c.dead)
  if (!living.length || mode === 'none') return

  const shuffled = shuffleInPlace([...living], rng)

  if (mode === 'all_injured') {
    for (const c of living) applyDamageWithArmor(c, '受伤', armorLogs)
    return
  }
  if (mode === 'one_injured') {
    const t = shuffled[0]
    if (t) applyDamageWithArmor(t, '受伤', armorLogs)
    return
  }
  if (mode === 'one_dead_rest_injured') {
    const victim = shuffled[0]
    if (victim) applyDamageWithArmor(victim, '死亡', armorLogs)
    for (const c of living) {
      if (c === victim || c.dead) continue
      applyDamageWithArmor(c, '受伤', armorLogs)
    }
    return
  }
  if (mode === 'one_two_severe_rest_injured') {
    const n = Math.min(2, Math.max(1, living.length))
    const severeTargets = shuffled.slice(0, n)
    const severeSet = new Set(severeTargets)
    for (const c of severeTargets) applyDamageWithArmor(c, '重伤', armorLogs)
    for (const c of living) {
      if (severeSet.has(c) || c.dead) continue
      applyDamageWithArmor(c, '受伤', armorLogs)
    }
  }
}

function outcomeModesForAdjusted(adjustedValue) {
  const A = adjustedValue
  if (A >= 7) {
    return {
      name: '大胜',
      attackMode: 'none',
      defenseMode: 'one_dead_rest_injured',
      attackText: '无伤',
      defenseText: '1人死亡，其余受伤',
    }
  }
  if (A >= 4) {
    return {
      name: '胜利',
      attackMode: 'none',
      defenseMode: 'one_two_severe_rest_injured',
      attackText: '无伤',
      defenseText: '1-2人重伤，其余受伤',
    }
  }
  if (A >= 1) {
    return {
      name: '小胜',
      attackMode: 'none',
      defenseMode: 'all_injured',
      attackText: '无伤',
      defenseText: '全部受伤',
    }
  }
  if (A === 0) {
    return {
      name: '僵持',
      attackMode: 'one_injured',
      defenseMode: 'one_injured',
      attackText: '1人受伤',
      defenseText: '1人受伤',
    }
  }
  if (A >= -3) {
    return {
      name: '小败',
      attackMode: 'all_injured',
      defenseMode: 'none',
      attackText: '全部受伤',
      defenseText: '无伤',
    }
  }
  if (A >= -6) {
    return {
      name: '失败',
      attackMode: 'one_two_severe_rest_injured',
      defenseMode: 'none',
      attackText: '1-2人重伤，其余受伤',
      defenseText: '无伤',
    }
  }
  return {
    name: '大败',
    attackMode: 'one_dead_rest_injured',
    defenseMode: 'none',
    attackText: '1人死亡，其余受伤',
    defenseText: '无伤',
  }
}

/**
 * 结算一轮战斗。
 * @param {object} input
 * @param {object[]} input.attackers - fighter UI objects
 * @param {object[]} input.defenders
 * @param {number} input.locationDefense
 * @param {boolean} input.insiderBetrayal - 里应外合 → 地点防御=0
 * @param {number} input.round - 从 1 开始
 * @param {function} [input.rng]
 */
export function settleCombatRound(input) {
  const rng = input.rng || Math.random
  const round = input.round || 1
  const insider = Boolean(input.insiderBetrayal)
  let locDef = insider ? 0 : parseCombatNumber(input.locationDefense)
  if (round > 1 && locDef > 0) {
    locDef = Math.ceil(locDef / 2)
  }

  const attackers = (input.attackers || []).map((f) => toCombatant(f, 'attack'))
  const defenders = (input.defenders || []).map((f) => toCombatant(f, 'defense'))

  const livingAtk = attackers.filter((c) => !c.dead)
  const livingDef = defenders.filter((c) => !c.dead)
  if (!livingAtk.length || !livingDef.length) {
    return {
      error: '攻守双方都需要至少一名存活参战者',
      round,
    }
  }

  const atkBase = livingAtk.reduce((s, c) => s + combatantBasePower(c), 0)
  const defBasePlayers = livingDef.reduce((s, c) => s + combatantBasePower(c), 0)
  const defBase = defBasePlayers + locDef

  // 每人掷 1d6：已填写 1–6 的攻骰优先采用（桌面实体骰），否则自动掷
  for (const c of [...livingAtk, ...livingDef]) {
    if (c.roll == null) {
      c.roll = rollD6(rng)
      c.rollWasManual = false
    }
  }
  const atkRollSum = livingAtk.reduce((s, c) => s + c.roll, 0)
  const defRollSum = livingDef.reduce((s, c) => s + c.roll, 0)
  const atkFinal = atkBase + atkRollSum
  const defFinal = defBase + defRollSum
  const diff = atkFinal - defFinal
  const totalFighters = livingAtk.length + livingDef.length
  const adjusted = computeAdjustedValue(diff, totalFighters)
  const outcome = outcomeModesForAdjusted(adjusted)

  const armorLogs = []
  assignOutcomeInjuries(attackers, outcome.attackMode, rng, armorLogs)
  assignOutcomeInjuries(defenders, outcome.defenseMode, rng, armorLogs)

  // 额外命中：任一存活且有武器威胁的单位，若攻骰 < 威胁值
  const extraHits = []
  const allLiving = [...livingAtk, ...livingDef]
  for (const attacker of allLiving) {
    if (attacker.dead || !attacker.weaponThreat) continue
    if (!shouldTriggerExtraHit(attacker.roll, attacker.weaponThreat)) continue
    const enemySide = attacker.side === 'attack' ? defenders : attackers
    const healthyEnemies = enemySide.filter((c) => isHealthy(c))
    if (!healthyEnemies.length) {
      extraHits.push({
        attacker: attacker.name,
        side: attacker.side,
        attackRoll: attacker.roll,
        threat: attacker.weaponThreat,
        skipped: true,
        note: '无可选的健康敌方目标',
      })
      continue
    }
    const target = pickRandom(healthyEnemies, rng)
    const defendRoll = rollD6(rng)
    const hit = resolveExtraHit(attacker.weaponThreat, defendRoll, {
      alreadyInjuredThisRound: target.injuredThisRound,
    })
    const before = statusLabel(target)
    const { applied, log } = applyDamageWithArmor(target, hit.result, armorLogs)
    if (log) {
      // already in armorLogs
    }
    extraHits.push({
      attacker: attacker.name,
      side: attacker.side,
      attackRoll: attacker.roll,
      threat: attacker.weaponThreat,
      target: target.name,
      defendRoll,
      tableResult: hit.result,
      applied,
      note: hit.note || '',
      targetBefore: before,
      targetAfter: statusLabel(target),
    })
  }

  const atkAllDown = attackers.filter((c) => !c.dead).every(isDowned) && attackers.some((c) => !c.dead)
  const defAllDown = defenders.filter((c) => !c.dead).every(isDowned) && defenders.some((c) => !c.dead)
  // 若一方全部死亡也算结束
  const atkWiped = attackers.every((c) => c.dead)
  const defWiped = defenders.every((c) => c.dead)

  return {
    round,
    locationDefenseUsed: locDef,
    insiderBetrayal: insider,
    locationDefenseHalved: round > 1 && parseCombatNumber(input.locationDefense) > 0 && !insider,
    attackers,
    defenders,
    atkBase,
    defBasePlayers,
    defBase,
    atkRollSum,
    defRollSum,
    atkFinal,
    defFinal,
    diff,
    totalFighters,
    adjusted,
    outcomeName: outcome.name,
    outcomeAttackText: outcome.attackText,
    outcomeDefenseText: outcome.defenseText,
    armorLogs,
    extraHits,
    deathmatchCanContinue: !atkAllDown && !defAllDown && !atkWiped && !defWiped,
    attackAllInjured: atkAllDown || atkWiped,
    defenseAllInjured: defAllDown || defWiped,
  }
}

/** 将结算后的 combatant 状态写回 UI fighter（用于死战带入下一轮） */
export function applySettlementToFighters(fighters, combatants) {
  const byUid = new Map(combatants.map((c) => [c.uid, c]))
  return fighters.map((f) => {
    const c = byUid.get(f.uid)
    if (!c) return f
    return {
      ...f,
      // 清空骰输入，避免下一轮误把上一轮点数当作实体骰重用
      attackRoll: '',
      weaponThreat: c.weaponThreat,
      catalogThreat: c.catalogThreat,
      threatHalved: c.threatHalved,
      armorUsed: c.armorUsed,
      isDead: c.dead,
      isInjured: c.injured && !c.severe ? 1 : c.injured ? 1 : 0,
      isSeverelyInjured: c.severe,
      isWeak: c.weak,
      dead: c.dead,
      injured: c.injured,
      severe: c.severe,
      weak: c.weak,
    }
  })
}

function diceGlyph(n) {
  const glyphs = ['', '⚀', '⚁', '⚂', '⚃', '⚄', '⚅']
  return glyphs[n] || String(n)
}

function fighterLineForReport(c) {
  const parts = [c.name]
  if (c.weaponThreat) parts.push(`威胁${c.weaponThreat}${c.threatHalved ? '↓' : ''}`)
  if (c.skillBonus) parts.push(`技能+${c.skillBonus}`)
  return parts.join('·')
}

/**
 * 生成可粘贴到公屏的结算报告（对齐 DM 模板）。
 */
export function formatCombatReportPublic(report) {
  if (!report || report.error) return report?.error || ''
  if (report.fled) return report.fleeNote || '一方败逃，死战结束。'

  const atkNames = (report.attackers || []).map(fighterLineForReport).join('、')
  const defNames = (report.defenders || []).map(fighterLineForReport).join('、')
  const lines = []
  lines.push('═══════════════════ 战斗结算 ═══════════════════')
  lines.push('')
  lines.push(`【攻方】${atkNames}  vs  【守方】${defNames}`)
  if (report.round > 1) lines.push(`（死战第 ${report.round} 轮）`)
  lines.push('')
  lines.push('① 基础战力计算')
  lines.push(`　攻方：${report.atkBase}`)
  {
    let defLine = `　守方：玩家 ${report.defBasePlayers} + 地点防御 ${report.locationDefenseUsed}`
    if (report.insiderBetrayal) defLine += '（里应外合→0）'
    else if (report.locationDefenseHalved) defLine += '（死战次轮÷2↑）'
    defLine += ` = ${report.defBase}`
    lines.push(defLine)
  }
  lines.push('')
  lines.push('② 掷骰结果（每人 1d6）')
  for (const c of report.attackers || []) {
    const tag = c.rollWasManual ? '' : '·自动'
    lines.push(`　攻·${c.name}：${diceGlyph(c.roll)}${c.roll}${tag}`)
  }
  for (const c of report.defenders || []) {
    const tag = c.rollWasManual ? '' : '·自动'
    lines.push(`　守·${c.name}：${diceGlyph(c.roll)}${c.roll}${tag}`)
  }
  lines.push(`　攻方最终战力：${report.atkBase} + ${report.atkRollSum} = ${report.atkFinal}`)
  lines.push(`　守方最终战力：${report.defBase} + ${report.defRollSum} = ${report.defFinal}`)
  lines.push('')
  lines.push('③ 调整值')
  lines.push(`　D = ${report.atkFinal} − ${report.defFinal} = ${report.diff > 0 ? '+' : ''}${report.diff}`)
  lines.push(`　A = ceil(${report.diff} × 3 ÷ ${report.totalFighters}) = ${report.adjusted > 0 ? '+' : ''}${report.adjusted}`)
  lines.push('')
  lines.push(`④ 战斗结果：攻方「${report.outcomeName}」`)
  lines.push(`　攻方：${report.outcomeAttackText}`)
  lines.push(`　守方：${report.outcomeDefenseText}`)
  lines.push('')
  lines.push('⑤ 额外命中判定')
  if (!report.extraHits?.length) {
    lines.push('　本轮无额外命中触发')
  } else {
    for (const h of report.extraHits) {
      if (h.skipped) {
        lines.push(`　${h.attacker} 攻骰${h.attackRoll} < 威胁${h.threat}，但${h.note}`)
      } else {
        lines.push(
          `　${h.attacker}（攻骰${h.attackRoll} < 威胁${h.threat}）→ ${h.target} 判定${h.defendRoll} → ${h.tableResult} → 生效「${h.applied}」`,
        )
      }
    }
  }
  if (report.armorLogs?.length) {
    lines.push('')
    lines.push('⑥ 防具抵消')
    for (const log of report.armorLogs) lines.push(`　${log}`)
  }
  lines.push('')
  lines.push('最终状态')
  for (const c of report.attackers || []) {
    lines.push(`　攻·${c.name}：${statusLabel(c)}${c.armorUsed ? '（防具已用）' : ''}`)
  }
  for (const c of report.defenders || []) {
    lines.push(`　守·${c.name}：${statusLabel(c)}${c.armorUsed ? '（防具已用）' : ''}`)
  }
  lines.push('')
  lines.push('══════════════════ 结算完毕 ══════════════════')
  return lines.join('\n')
}
