<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { playerAPI, dmPlayerAPI, locationAPI } from '@/utils/api.js'
import { GAME_ITEM_NAMES } from '@/data/gameData.js'
import {
  WEAPON_THREAT_BY_ID,
  ARMOR_ITEM_IDS,
  ARMOR_RULE_SUMMARY,
  EXPLOSIVE_WEAPON_ID,
  COMBAT_OUTCOMES,
  EXTRA_HIT_TABLE,
  THREAT_WEAPON_INDEX,
  parseCombatNumber,
  computeSkillBonus,
  getSkillBonusSuppression,
  fighterTotalPower,
  resolveCombatOutcome,
  computeAdjustedValue,
  shouldTriggerExtraHit,
  resolveExtraHit,
  createEmptyFighter,
  createDefaultBonusRow,
  settleCombatRound,
  applySettlementToFighters,
  statusLabel,
  effectiveWeaponThreatForFighter,
  formatCombatReportPublic,
  COMBAT_FLOW_STEPS,
} from '@/data/combatAssist.js'

const attackers = ref([])
const defenders = ref([])
const players = ref([])
const weapons = ref([])
const locations = ref([])
const loading = ref(true)

const insiderBetrayal = ref(false)
const defenseLocationId = ref('')
const locationDefenseValue = ref('')
const combatRound = ref(1)
const lastReport = ref(null)
/** 整场战斗的所有轮次报告（死战多轮） */
const roundReports = ref([])
const deathmatchActive = ref(false)

const BONUS_LABEL_PLACEHOLDER = '数值调整'


const hitTableHelpText = computed(() => {
  const lines = ['命中表（判定骰 1→6）：']
  for (const [threat, row] of Object.entries(EXTRA_HIT_TABLE)) {
    lines.push(`威胁值${threat}: ${row.join(' / ')}`)
  }
  lines.push('', '图鉴索引：')
  for (const [threat, names] of Object.entries(THREAT_WEAPON_INDEX)) {
    lines.push(`威胁值${threat}: ${names.join('，')}`)
  }
  lines.push('', ARMOR_RULE_SUMMARY)
  return lines.join('\n')
})

const OUTCOME_ROWS = [
  { range: '≥7', ...COMBAT_OUTCOMES[0] },
  { range: '4～6', ...COMBAT_OUTCOMES[1] },
  { range: '1～3', ...COMBAT_OUTCOMES[2] },
  { range: '0', ...COMBAT_OUTCOMES[3] },
  { range: '-1～-3', ...COMBAT_OUTCOMES[4] },
  { range: '-4～-6', ...COMBAT_OUTCOMES[5] },
  { range: '≤-7', ...COMBAT_OUTCOMES[6] },
]

const playerRoster = computed(() =>
  (players.value || []).map((p) => ({
    id: p.id,
    name: p.name || `玩家${p.id}`,
    job: p.jobName || p.job || '',
    title: [p.name, p.jobName || p.job].filter(Boolean).join(' · '),
  })),
)

function isPlayerInCombat(playerId) {
  const id = Number(playerId)
  return (
    attackers.value.some((f) => f.playerId === id) ||
    defenders.value.some((f) => f.playerId === id)
  )
}

function rosterForSide() {
  return playerRoster.value.filter((p) => !isPlayerInCombat(p.id))
}

function fightersRef(side) {
  return side === 'attack' ? attackers : defenders
}

function findFighterIndex(side, uid) {
  return fightersRef(side).value.findIndex((f) => f.uid === uid)
}

/** Replace fighter row so Vue always re-renders after async inventory load */
function setFighter(side, uid, patch) {
  const list = fightersRef(side)
  const i = findFighterIndex(side, uid)
  if (i < 0) return
  const prev = list.value[i]
  list.value.splice(i, 1, { ...prev, ...patch })
}

const attackSideTotal = computed(() =>
  attackers.value.reduce((s, f) => s + fighterTotalPower(f), 0),
)
const defensePlayersTotal = computed(() =>
  defenders.value.reduce((s, f) => s + fighterTotalPower(f), 0),
)
const defenseSideTotal = computed(() => {
  let t = defensePlayersTotal.value
  if (!insiderBetrayal.value) {
    t += parseCombatNumber(locationDefenseValue.value)
  }
  return t
})
const powerDiff = computed(() => attackSideTotal.value - defenseSideTotal.value)
const totalFighters = computed(() => attackers.value.length + defenders.value.length)
const adjustedValue = computed(() => computeAdjustedValue(powerDiff.value, totalFighters.value))
const outcome = computed(() => resolveCombatOutcome(powerDiff.value, totalFighters.value))

function effectiveLocationDefense() {
  if (insiderBetrayal.value) return 0
  return parseCombatNumber(locationDefenseValue.value)
}

function runSettlement({ nextDeathmatchRound = false } = {}) {
  if (!attackers.value.length || !defenders.value.length) {
    lastReport.value = { error: '请先为攻守双方各加入至少一名玩家' }
    return
  }
  if (nextDeathmatchRound) {
    combatRound.value += 1
  } else {
    combatRound.value = 1
    roundReports.value = []
    applyResultMsg.value = ''
    // 新开战斗时清除本场防具已用标记
    attackers.value = attackers.value.map((f) => ({ ...f, armorUsed: false }))
    defenders.value = defenders.value.map((f) => ({ ...f, armorUsed: false }))
  }

  const report = settleCombatRound({
    attackers: attackers.value,
    defenders: defenders.value,
    locationDefense: effectiveLocationDefense(),
    insiderBetrayal: insiderBetrayal.value,
    round: combatRound.value,
  })
  lastReport.value = report
  if (report.error) return

  roundReports.value = [...roundReports.value, report]
  attackers.value = applySettlementToFighters(attackers.value, report.attackers)
  defenders.value = applySettlementToFighters(defenders.value, report.defenders)

  // 规则：双方均愿意可继续死战；一方全员受伤/死亡则强制结束
  deathmatchActive.value = Boolean(report.deathmatchCanContinue)
}

function continueDeathmatch() {
  runSettlement({ nextDeathmatchRound: true })
}

function endBattle(reason) {
  deathmatchActive.value = false
  if (lastReport.value && !lastReport.value.error) {
    const note =
      reason === 'flee'
        ? '一方选择败逃，死战结束。整场胜负以最后一轮结果为准。'
        : '双方停战，战斗结束。整场胜负以最后一轮结果为准。'
    lastReport.value = { ...lastReport.value, fled: true, fleeNote: note }
    const idx = roundReports.value.length - 1
    if (idx >= 0) {
      const copy = [...roundReports.value]
      copy[idx] = { ...copy[idx], fled: true, fleeNote: note }
      roundReports.value = copy
    }
  }
}

function combatantLineBase(c) {
  return (c.basePower || 0) + (c.weaponThreat || 0) + (c.skillBonus || 0) + (c.bonusSum || 0)
}

/** 未填实体骰的人数（结算时将自动掷 1d6） */
const missingRollCount = computed(() =>
  [...attackers.value, ...defenders.value].filter((f) => {
    const r = Number(f.attackRoll)
    return !(r >= 1 && r <= 6)
  }).length,
)

const reportCopied = ref(false)
/** 公屏文本：整场战斗所有轮次 */
const publicReportText = computed(() =>
  roundReports.value.map((r) => formatCombatReportPublic(r)).filter(Boolean).join('\n\n'),
)

async function copyPublicReport() {
  if (!publicReportText.value) return
  try {
    await navigator.clipboard.writeText(publicReportText.value)
    reportCopied.value = true
    setTimeout(() => {
      reportCopied.value = false
    }, 2000)
  } catch (e) {
    console.error('复制失败', e)
  }
}

/** 恢复到战斗前状态（以加入时从数据库读到的基线为准） */
function restoreFighterBaseline(f) {
  const b = f.baseline || { dead: false, injured: false, severe: false, weak: false }
  return {
    ...f,
    armorUsed: false,
    attackRoll: '',
    isDead: b.dead,
    isInjured: b.injured,
    isSeverelyInjured: b.severe,
    isWeak: b.weak,
    dead: b.dead,
    injured: b.injured,
    severe: b.severe,
    weak: b.weak,
  }
}

function resetBattleStates() {
  combatRound.value = 1
  deathmatchActive.value = false
  lastReport.value = null
  roundReports.value = []
  applyResultMsg.value = ''
  attackers.value = attackers.value.map(restoreFighterBaseline)
  defenders.value = defenders.value.map(restoreFighterBaseline)
}

// ---------------------------------
// 落实伤害：将最终状态写回玩家（需确认）
// ---------------------------------
const showApplyConfirm = ref(false)
const applyingStatuses = ref(false)
const applyResultMsg = ref('')

function fighterCurrentHealth(f) {
  return {
    dead: Boolean(f.isDead || f.dead),
    injured: Boolean(f.isInjured || f.injured),
    severe: Boolean(f.isSeverelyInjured || f.severe),
    weak: Boolean(f.isWeak || f.weak),
  }
}

function healthLabel(h) {
  if (h.dead) return '死亡'
  const parts = []
  if (h.severe) parts.push('重伤')
  else if (h.injured) parts.push('受伤')
  if (h.weak) parts.push('虚弱')
  return parts.length ? parts.join('+') : '无伤'
}

/** 战斗后与基线不同的玩家（仅这些会写库） */
const statusChanges = computed(() => {
  const changes = []
  for (const f of [...attackers.value, ...defenders.value]) {
    if (!f.playerId) continue
    const baseline = f.baseline || { dead: false, injured: false, severe: false, weak: false }
    const after = fighterCurrentHealth(f)
    if (
      baseline.dead === after.dead &&
      baseline.injured === after.injured &&
      baseline.severe === after.severe &&
      baseline.weak === after.weak
    ) continue
    changes.push({
      playerId: f.playerId,
      name: f.playerName,
      beforeLabel: healthLabel(baseline),
      afterLabel: healthLabel(after),
      after,
    })
  }
  return changes
})

async function applyStatusesToPlayers() {
  if (!statusChanges.value.length || applyingStatuses.value) return
  applyingStatuses.value = true
  applyResultMsg.value = ''
  const failed = []
  for (const ch of statusChanges.value) {
    try {
      const res = await dmPlayerAPI.update(ch.playerId, {
        isInjured: ch.after.injured ? 1 : 0,
        isSeverelyInjured: ch.after.severe,
        isWeak: ch.after.weak,
        isDead: ch.after.dead,
      })
      if (res?.success === false) {
        failed.push(`${ch.name}（${res.message || '未知错误'}）`)
        continue
      }
      // 写库成功后更新基线，避免重复写入
      const patchBaseline = (list) =>
        list.value = list.value.map((f) =>
          f.playerId === ch.playerId ? { ...f, baseline: { ...ch.after } } : f,
        )
      patchBaseline(attackers)
      patchBaseline(defenders)
    } catch (e) {
      console.error('写入玩家状态失败', e)
      failed.push(ch.name)
    }
  }
  applyingStatuses.value = false
  showApplyConfirm.value = false
  applyResultMsg.value = failed.length
    ? `部分写入失败：${failed.join('、')}`
    : '已将伤害落实到玩家状态'
}
function combatOptionsForFighter(fighter) {
  return {
    meleeDisabled: Boolean(fighter.combatMeleeDisabled),
    rangedDisabled: Boolean(fighter.combatRangedDisabled),
  }
}

function skillBonusSuppression(fighter) {
  return getSkillBonusSuppression(fighter)
}

function applyWeaponChange(fighter) {
  const id = fighter.weaponId
  let weaponThreat = ''
  let catalogThreat = ''
  let threatHalved = false
  if (id !== '' && id != null) {
    // 图鉴为真相；炸药按外围/内围覆盖；无射击技能时远程武器减半（与自动结算一致）
    const info = effectiveWeaponThreatForFighter(fighter)
    weaponThreat = info.effective
    catalogThreat = info.catalog
    threatHalved = info.halved
    if (Number(id) !== EXPLOSIVE_WEAPON_ID && WEAPON_THREAT_BY_ID[Number(id)] == null) {
      const weaponFromCatalog = weapons.value.find((w) => w.id === Number(id))
      if (weaponFromCatalog?.threat != null) {
        weaponThreat = weaponFromCatalog.threat
        catalogThreat = weaponFromCatalog.threat
        threatHalved = false
      }
    }
  }
  return {
    weaponThreat,
    catalogThreat,
    threatHalved,
    skillBonus: computeSkillBonus(fighter.jobSkills, id, combatOptionsForFighter(fighter)),
  }
}

function onExplosiveZoneChange(side, uid, zone) {
  setFighter(side, uid, { explosiveZone: zone })
  onWeaponChange(side, uid)
}

function onArmorChoiceChange(side, uid, choice) {
  setFighter(side, uid, { armorChoice: choice })
}

/** 额外命中：选择攻击方一人 + 目标骰 */
const extraHitAttackerUid = ref('')
const extraHitDefendRoll = ref('')
const extraHitAlreadyInjured = ref(false)

const extraHitAttacker = computed(() => {
  const uid = extraHitAttackerUid.value
  return (
    attackers.value.find((f) => f.uid === uid) ||
    defenders.value.find((f) => f.uid === uid) ||
    null
  )
})

const extraHitPreview = computed(() => {
  const f = extraHitAttacker.value
  if (!f) return null
  const threat = parseCombatNumber(f.weaponThreat)
  const attackRoll = parseCombatNumber(f.attackRoll)
  if (!threat || !attackRoll) return { status: 'need_rolls', threat, attackRoll }
  if (!shouldTriggerExtraHit(attackRoll, threat)) {
    return { status: 'no_trigger', threat, attackRoll, note: `攻击骰 ${attackRoll} ≥ 威胁值 ${threat}，不触发额外命中` }
  }
  const defendRoll = parseCombatNumber(extraHitDefendRoll.value)
  if (!defendRoll) {
    return { status: 'need_defend', threat, attackRoll, note: '已触发：请为目标投 1d6 并填入判定值' }
  }
  const resolved = resolveExtraHit(threat, defendRoll, {
    alreadyInjuredThisRound: extraHitAlreadyInjured.value,
  })
  return {
    status: 'resolved',
    threat,
    attackRoll,
    defendRoll,
    result: resolved.result,
    note: resolved.note,
  }
})

function weaponsForFighter(fighter) {
  const owned = fighter.ownedWeaponIds
  if (Array.isArray(owned) && owned.length > 0) {
    return weapons.value.filter((w) => owned.includes(w.id))
  }
  return weapons.value
}

function onWeaponChange(side, uid) {
  const i = findFighterIndex(side, uid)
  if (i < 0) return
  const f = fightersRef(side).value[i]
  setFighter(side, uid, applyWeaponChange(f))
}

async function loadStaticData() {
  loading.value = true
  try {
    const [playerList, catalogRes, locList] = await Promise.all([
      playerAPI.getAll(),
      dmPlayerAPI.getCatalog(),
      locationAPI.getAll(),
    ])
    players.value = Array.isArray(playerList) ? playerList : []
    const items = catalogRes?.success ? catalogRes.items || [] : []
    weapons.value = items
      .filter((i) => i.itemType === 'weapon')
      .map((w) => ({
        id: w.itemId,
        name: w.name || GAME_ITEM_NAMES.weapon?.[w.itemId] || `武器#${w.itemId}`,
        threat: WEAPON_THREAT_BY_ID[w.itemId] ?? w.threatLevel ?? 0,
      }))
    locations.value = Array.isArray(locList) ? locList : []
  } catch (e) {
    console.error('战斗辅助加载失败', e)
    players.value = []
    weapons.value = []
    locations.value = []
  } finally {
    loading.value = false
  }
}

async function loadFighterInventory(side, uid) {
  const i = findFighterIndex(side, uid)
  if (i < 0) return
  const fighter = fightersRef(side).value[i]
  const playerId = fighter.playerId
  if (!playerId) return

  setFighter(side, uid, { inventoryLoading: true, inventoryError: null })

  try {
    const [inv, details] = await Promise.all([
      dmPlayerAPI.getInventory(playerId),
      playerAPI.getDetails(playerId),
    ])

    const jobSkills =
      details?.jobSkills ??
      (details?.success !== false ? details?.jobSkills : '') ??
      fighter.jobSkills ??
      ''

    const items = inv?.success !== false ? inv?.items || [] : []
    let bodyArmorCount = 0
    let shieldCount = 0
    for (const row of items) {
      if (row.itemType !== 'item') continue
      const id = Number(row.itemId)
      const qty = Number(row.quantity) || 0
      if (id === ARMOR_ITEM_IDS.bodyArmor) bodyArmorCount += qty
      if (id === ARMOR_ITEM_IDS.shield) shieldCount += qty
    }

    const ownedWeaponIds = items
      .filter((r) => r.itemType === 'weapon' && (Number(r.quantity) || 0) > 0)
      .map((r) => Number(r.itemId))

    let weaponId = fighter.weaponId
    let weaponThreat = fighter.weaponThreat
    let skillBonus = fighter.skillBonus

    if (ownedWeaponIds.length === 1 && (weaponId === '' || weaponId == null)) {
      weaponId = ownedWeaponIds[0]
    }

    const combatMeleeDisabled = Boolean(details?.combatMeleeDisabled)
    const combatRangedDisabled = Boolean(details?.combatRangedDisabled)
    const statuses = Array.isArray(details?.statuses) ? details.statuses : []
    const baseline = {
      dead: Boolean(details?.isDead),
      injured: Boolean(details?.isInjured),
      severe: Boolean(details?.isSeverelyInjured),
      weak: Boolean(details?.isWeak),
    }

    const merged = {
      ...fighter,
      jobSkills,
      statuses,
      isDead: Boolean(details?.isDead),
      isSeverelyInjured: Boolean(details?.isSeverelyInjured),
      isWeak: Boolean(details?.isWeak),
      isInjured: Boolean(details?.isInjured),
      isOverworked: Boolean(details?.isOverworked),
      combatMeleeDisabled,
      combatRangedDisabled,
      bodyArmorCount,
      shieldCount,
      ownedWeaponIds,
      weaponId,
      inventoryLoading: false,
      inventoryError: null,
    }
    const weaponPatch = applyWeaponChange(merged)
    setFighter(side, uid, {
      jobSkills,
      statuses,
      baseline,
      isDead: Boolean(details?.isDead),
      isSeverelyInjured: Boolean(details?.isSeverelyInjured),
      isWeak: Boolean(details?.isWeak),
      isInjured: Boolean(details?.isInjured),
      isOverworked: Boolean(details?.isOverworked),
      combatMeleeDisabled,
      combatRangedDisabled,
      bodyArmorCount,
      shieldCount,
      ownedWeaponIds,
      weaponId: weaponPatch.weaponId ?? weaponId,
      weaponThreat: weaponPatch.weaponThreat,
      catalogThreat: weaponPatch.catalogThreat,
      threatHalved: weaponPatch.threatHalved,
      skillBonus: weaponPatch.skillBonus,
      inventoryLoading: false,
      inventoryError: null,
    })
  } catch (e) {
    console.error('加载背包失败', e)
    setFighter(side, uid, {
      inventoryLoading: false,
      inventoryError: '背包加载失败',
    })
  }
}

function addFighter(side, playerId) {
  const id = Number(playerId)
  if (!id || isPlayerInCombat(id)) return
  const p = players.value.find((x) => x.id === id)
  if (!p) return

  const fighter = {
    ...createEmptyFighter(id, p.name, ''),
    inventoryLoading: true,
    inventoryError: null,
    ownedWeaponIds: [],
  }
  fightersRef(side).value.push(fighter)
  loadFighterInventory(side, fighter.uid)
}

function removeFighter(side, uid) {
  const list = fightersRef(side)
  list.value = list.value.filter((f) => f.uid !== uid)
}

function addBonusRow(side, uid) {
  const i = findFighterIndex(side, uid)
  if (i < 0) return
  const f = fightersRef(side).value[i]
  setFighter(side, uid, { bonuses: [...f.bonuses, createDefaultBonusRow()] })
}

function removeBonusRow(side, uid, bonusUid) {
  const i = findFighterIndex(side, uid)
  if (i < 0) return
  const f = fightersRef(side).value[i]
  setFighter(side, uid, { bonuses: f.bonuses.filter((b) => b.uid !== bonusUid) })
}

function onLocationPick() {
  const loc = locations.value.find((l) => String(l.id) === String(defenseLocationId.value))
  if (loc && loc.defenseValue != null) locationDefenseValue.value = loc.defenseValue
}

watch(defenseLocationId, () => {
  if (defenseLocationId.value) onLocationPick()
})

onMounted(loadStaticData)
</script>

<template>
  <div class="leading-snug text-gray-200">
    <div v-if="loading" class="text-base text-gray-400 py-8 text-center">加载中…</div>

    <template v-else>
      <details class="mb-3 rounded-lg border border-white/10 bg-[#0d0f14] px-3 py-2">
        <summary class="cursor-pointer text-sm font-semibold text-gray-200 select-none">
          DM 结算流程速查（7 步）
        </summary>
        <ol class="mt-2 space-y-0.5 text-sm text-gray-400 list-decimal list-inside">
          <li v-for="(s, i) in COMBAT_FLOW_STEPS" :key="i">{{ s }}</li>
        </ol>
      </details>

      <div class="flex flex-col xl:flex-row gap-3 xl:items-start">
        <!-- 主区域：攻防 + 底部对照表 -->
        <div class="flex-1 min-w-0 space-y-3">
          <div class="cat-combat-main grid grid-cols-1 lg:grid-cols-2 gap-3">
            <!-- 攻方 -->
            <section class="rounded-lg border border-red-500/30 bg-[#121018] p-3">
              <div class="flex items-center justify-between gap-2 mb-2">
                <h2 class="text-lg font-bold text-red-300">攻方</h2>
                <span class="text-base text-gray-400 tabular-nums">{{ attackSideTotal }} 战力</span>
              </div>

              <p class="text-sm text-gray-400 mb-1">点击姓名加入</p>
              <div class="flex flex-wrap gap-1 mb-2 p-1.5 rounded-md bg-white/5 border border-white/10">
                <span v-if="!rosterForSide().length" class="text-base text-gray-500 px-1">无可用玩家</span>
                <button
                  v-for="p in rosterForSide()"
                  :key="'a-' + p.id"
                  type="button"
                  class="px-2.5 py-1 rounded-md text-base font-medium text-gray-100 bg-white/8 hover:bg-red-500/25 border border-white/10 hover:border-red-400/40 transition-colors"
                  :title="p.title"
                  @click="addFighter('attack', p.id)"
                >
                  {{ p.name }}
                </button>
              </div>

              <div v-if="attackers.length" class="space-y-1.5">
                <article
                  v-for="f in attackers"
                  :key="f.uid"
                  class="cat-fighter-card cat-fighter-card--attack relative"
                >
                  <button
                    type="button"
                    class="cat-fighter-remove cat-fighter-remove--attack"
                    aria-label="移除"
                    @click="removeFighter('attack', f.uid)"
                  >
                    ×
                  </button>

                  <div class="pr-5 mb-1">
                    <div class="flex flex-wrap items-center gap-x-1.5 gap-y-0.5">
                      <span class="text-base font-semibold text-gray-100">{{ f.playerName }}</span>
                      <span v-if="f.inventoryLoading" class="text-sm text-amber-400/90">读取背包…</span>
                      <span v-else-if="f.inventoryError" class="text-sm text-red-400/90">{{ f.inventoryError }}</span>
                      <span v-else class="text-sm text-gray-400">衣{{ f.bodyArmorCount }} · 盾{{ f.shieldCount }}</span>
                    </div>
                    <p v-if="f.jobSkills && !f.inventoryLoading" class="text-sm text-gray-500 mt-0.5 leading-tight">{{ f.jobSkills }}</p>
                  </div>

                  <select
                    :value="f.weaponId"
                    class="cat-fighter-select w-full mb-1.5"
                    :disabled="f.inventoryLoading"
                    @change="(e) => { setFighter('attack', f.uid, { weaponId: e.target.value === '' ? '' : Number(e.target.value) }); onWeaponChange('attack', f.uid) }"
                  >
                    <option value="">武器：未装备</option>
                    <option v-for="w in weaponsForFighter(f)" :key="w.id" :value="w.id">{{ w.name }}（{{ w.threat }}）</option>
                  </select>

                  <div v-if="Number(f.weaponId) === EXPLOSIVE_WEAPON_ID" class="flex flex-wrap gap-2 mb-1.5 text-sm">
                    <label class="inline-flex items-center gap-1 text-gray-300">
                      <input type="radio" :checked="f.explosiveZone !== 'outer'" @change="onExplosiveZoneChange('attack', f.uid, 'inner')" />
                      炸弹内围(10)
                    </label>
                    <label class="inline-flex items-center gap-1 text-gray-300">
                      <input type="radio" :checked="f.explosiveZone === 'outer'" @change="onExplosiveZoneChange('attack', f.uid, 'outer')" />
                      炸弹外围(5)
                    </label>
                  </div>

                  <div class="flex flex-wrap items-center gap-2 mb-1.5 text-sm text-gray-300">
                    <span class="text-gray-500" :title="ARMOR_RULE_SUMMARY">防护</span>
                    <select
                      class="cat-fighter-select max-w-[9rem]"
                      :value="f.armorChoice || 'none'"
                      @change="(e) => onArmorChoiceChange('attack', f.uid, e.target.value)"
                    >
                      <option value="none">无</option>
                      <option value="body" :disabled="!(f.bodyArmorCount > 0)">防弹衣</option>
                      <option value="shield" :disabled="!(f.shieldCount > 0)">复合盾</option>
                    </select>
                    <label class="inline-flex items-center gap-1" title="每人 1d6：计入最终战力，并用于额外命中触发。填入桌面实体骰点数；留空则结算时自动掷">
                      <span class="text-gray-500">骰1d6</span>
                      <input v-model="f.attackRoll" type="number" min="1" max="6" class="cat-num-square" placeholder="自动" />
                    </label>
                  </div>

                  <div class="cat-stat-row cat-stat-row--wrap">
                    <label class="cat-stat-cell" title="基础战力">
                      <span class="cat-stat-label">基</span>
                      <input v-model.number="f.basePower" type="number" class="cat-num-square" />
                    </label>
                    <label class="cat-stat-cell" title="武器威胁（无射击技能时远程武器减半，向下取整）">
                      <span class="cat-stat-label">武</span>
                      <div class="cat-skill-box">
                        <input v-model.number="f.weaponThreat" type="number" class="cat-num-square" />
                        <span
                          v-if="f.threatHalved"
                          class="cat-skill-warn"
                          :title="`无射击技能：远程威胁减半（图鉴${f.catalogThreat} → ${f.weaponThreat}）`"
                          aria-label="威胁值已减半"
                        >½</span>
                      </div>
                    </label>
                    <label class="cat-stat-cell cat-stat-cell--skill" title="技能加成">
                      <span class="cat-stat-label">技能</span>
                      <div class="cat-skill-box">
                        <input v-model.number="f.skillBonus" type="number" class="cat-num-square" />
                        <span
                          v-if="skillBonusSuppression(f)"
                          class="cat-skill-warn"
                          :title="skillBonusSuppression(f).tooltip"
                          aria-label="技能加成被状态阻止"
                        >!</span>
                      </div>
                    </label>
                    <div
                      v-for="b in f.bonuses"
                      :key="b.uid"
                      class="cat-stat-cell cat-stat-cell--bonus"
                    >
                      <input
                        v-model="b.label"
                        type="text"
                        class="cat-stat-label-input"
                        :placeholder="BONUS_LABEL_PLACEHOLDER"
                      />
                      <input v-model.number="b.value" type="number" class="cat-num-square" />
                      <button
                        type="button"
                        class="cat-bonus-remove"
                        aria-label="删除此项"
                        @click="removeBonusRow('attack', f.uid, b.uid)"
                      >
                        ×
                      </button>
                    </div>
                    <div class="cat-stat-row__tail">
                      <div class="cat-stat-cell" title="合计">
                        <span class="cat-stat-label">计</span>
                        <div class="cat-num-square cat-num-square--total">{{ fighterTotalPower(f) }}</div>
                      </div>
                      <div class="cat-stat-cell">
                        <span class="cat-stat-label">额外</span>
                        <button
                          type="button"
                          class="cat-num-square cat-num-square--add cat-num-square--add-attack"
                          title="添加数值调整"
                          @click="addBonusRow('attack', f.uid)"
                        >
                          +
                        </button>
                      </div>
                    </div>
                  </div>
                </article>
              </div>
            </section>

            <!-- 守方 -->
            <section class="rounded-lg border border-cyan-500/30 bg-[#0c1418] p-3">
              <div class="flex items-center justify-between gap-2 mb-2">
                <h2 class="text-lg font-bold text-cyan-300">守方</h2>
                <span class="text-base text-gray-400 tabular-nums">{{ defenseSideTotal }} 战力</span>
              </div>

              <p class="text-sm text-gray-400 mb-1">点击姓名加入</p>
              <div class="flex flex-wrap gap-1 mb-2 p-1.5 rounded-md bg-white/5 border border-white/10">
                <span v-if="!rosterForSide().length" class="text-base text-gray-500 px-1">无可用玩家</span>
                <button
                  v-for="p in rosterForSide()"
                  :key="'d-' + p.id"
                  type="button"
                  class="px-2.5 py-1 rounded-md text-base font-medium text-gray-100 bg-white/8 hover:bg-cyan-500/25 border border-white/10 hover:border-cyan-400/40 transition-colors"
                  :title="p.title"
                  @click="addFighter('defense', p.id)"
                >
                  {{ p.name }}
                </button>
              </div>

              <div class="mb-2 p-1.5 rounded-md border border-cyan-500/20 bg-cyan-950/40 space-y-1.5">
                <div class="flex gap-1.5 items-center" :class="insiderBetrayal ? 'opacity-40' : ''">
                  <span class="text-sm text-gray-400 shrink-0">地点防御</span>
                  <select v-model="defenseLocationId" class="cat-fighter-select flex-1 min-w-0" :disabled="insiderBetrayal">
                    <option value="">选地点（野外填0）</option>
                    <option v-for="loc in locations" :key="loc.id" :value="loc.id">{{ loc.name }}（{{ loc.defenseValue ?? 0 }}）</option>
                  </select>
                  <input v-model="locationDefenseValue" type="number" class="cat-num-square shrink-0" title="地点防御值（可手动修改）" :disabled="insiderBetrayal" />
                </div>
                <label class="flex items-center gap-2 text-sm text-amber-200/90 cursor-pointer">
                  <input v-model="insiderBetrayal" type="checkbox" class="rounded" />
                  里应外合（地点防御按 0 计）
                </label>
              </div>

              <div v-if="defenders.length" class="space-y-1.5">
                <article v-for="f in defenders" :key="f.uid" class="cat-fighter-card cat-fighter-card--defense relative">
                  <button
                    type="button"
                    class="cat-fighter-remove cat-fighter-remove--defense"
                    aria-label="移除"
                    @click="removeFighter('defense', f.uid)"
                  >
                    ×
                  </button>

                  <div class="pr-5 mb-1">
                    <div class="flex flex-wrap items-center gap-x-1.5 gap-y-0.5">
                      <span class="text-base font-semibold text-gray-100">{{ f.playerName }}</span>
                      <span v-if="f.inventoryLoading" class="text-sm text-amber-400/90">读取背包…</span>
                      <span v-else-if="f.inventoryError" class="text-sm text-red-400/90">{{ f.inventoryError }}</span>
                      <span v-else class="text-sm text-gray-400">衣{{ f.bodyArmorCount }} · 盾{{ f.shieldCount }}</span>
                    </div>
                    <p v-if="f.jobSkills && !f.inventoryLoading" class="text-sm text-gray-500 mt-0.5 leading-tight">{{ f.jobSkills }}</p>
                  </div>
                  <select
                    :value="f.weaponId"
                    class="cat-fighter-select w-full mb-1.5"
                    :disabled="f.inventoryLoading"
                    @change="(e) => { setFighter('defense', f.uid, { weaponId: e.target.value === '' ? '' : Number(e.target.value) }); onWeaponChange('defense', f.uid) }"
                  >
                    <option value="">武器：未装备</option>
                    <option v-for="w in weaponsForFighter(f)" :key="w.id" :value="w.id">{{ w.name }}（{{ w.threat }}）</option>
                  </select>

                  <div v-if="Number(f.weaponId) === EXPLOSIVE_WEAPON_ID" class="flex flex-wrap gap-2 mb-1.5 text-sm">
                    <label class="inline-flex items-center gap-1 text-gray-300">
                      <input type="radio" :checked="f.explosiveZone !== 'outer'" @change="onExplosiveZoneChange('defense', f.uid, 'inner')" />
                      炸弹内围(10)
                    </label>
                    <label class="inline-flex items-center gap-1 text-gray-300">
                      <input type="radio" :checked="f.explosiveZone === 'outer'" @change="onExplosiveZoneChange('defense', f.uid, 'outer')" />
                      炸弹外围(5)
                    </label>
                  </div>

                  <div class="flex flex-wrap items-center gap-2 mb-1.5 text-sm text-gray-300">
                    <span class="text-gray-500" :title="ARMOR_RULE_SUMMARY">防护</span>
                    <select
                      class="cat-fighter-select max-w-[9rem]"
                      :value="f.armorChoice || 'none'"
                      @change="(e) => onArmorChoiceChange('defense', f.uid, e.target.value)"
                    >
                      <option value="none">无</option>
                      <option value="body" :disabled="!(f.bodyArmorCount > 0)">防弹衣</option>
                      <option value="shield" :disabled="!(f.shieldCount > 0)">复合盾</option>
                    </select>
                    <label class="inline-flex items-center gap-1" title="每人 1d6：计入最终战力，并用于额外命中触发。填入桌面实体骰点数；留空则结算时自动掷">
                      <span class="text-gray-500">骰1d6</span>
                      <input v-model="f.attackRoll" type="number" min="1" max="6" class="cat-num-square" placeholder="自动" />
                    </label>
                  </div>

                  <div class="cat-stat-row cat-stat-row--wrap">
                    <label class="cat-stat-cell" title="基础战力">
                      <span class="cat-stat-label">基</span>
                      <input v-model.number="f.basePower" type="number" class="cat-num-square" />
                    </label>
                    <label class="cat-stat-cell" title="武器威胁（无射击技能时远程武器减半，向下取整）">
                      <span class="cat-stat-label">武</span>
                      <div class="cat-skill-box">
                        <input v-model.number="f.weaponThreat" type="number" class="cat-num-square" />
                        <span
                          v-if="f.threatHalved"
                          class="cat-skill-warn"
                          :title="`无射击技能：远程威胁减半（图鉴${f.catalogThreat} → ${f.weaponThreat}）`"
                          aria-label="威胁值已减半"
                        >½</span>
                      </div>
                    </label>
                    <label class="cat-stat-cell cat-stat-cell--skill" title="技能加成">
                      <span class="cat-stat-label">技能</span>
                      <div class="cat-skill-box">
                        <input v-model.number="f.skillBonus" type="number" class="cat-num-square" />
                        <span
                          v-if="skillBonusSuppression(f)"
                          class="cat-skill-warn"
                          :title="skillBonusSuppression(f).tooltip"
                          aria-label="技能加成被状态阻止"
                        >!</span>
                      </div>
                    </label>
                    <div
                      v-for="b in f.bonuses"
                      :key="b.uid"
                      class="cat-stat-cell cat-stat-cell--bonus"
                    >
                      <input
                        v-model="b.label"
                        type="text"
                        class="cat-stat-label-input"
                        :placeholder="BONUS_LABEL_PLACEHOLDER"
                      />
                      <input v-model.number="b.value" type="number" class="cat-num-square" />
                      <button
                        type="button"
                        class="cat-bonus-remove"
                        aria-label="删除此项"
                        @click="removeBonusRow('defense', f.uid, b.uid)"
                      >
                        ×
                      </button>
                    </div>
                    <div class="cat-stat-row__tail">
                      <div class="cat-stat-cell" title="合计">
                        <span class="cat-stat-label">计</span>
                        <div class="cat-num-square cat-num-square--total">{{ fighterTotalPower(f) }}</div>
                      </div>
                      <div class="cat-stat-cell">
                        <span class="cat-stat-label">额外</span>
                        <button
                          type="button"
                          class="cat-num-square cat-num-square--add cat-num-square--add-defense"
                          title="添加数值调整"
                          @click="addBonusRow('defense', f.uid)"
                        >
                          +
                        </button>
                      </div>
                    </div>
                  </div>
                </article>
              </div>
            </section>
          </div>


          <!-- 手动额外命中检查（自动结算已包含此步骤，供实体骰对照用） -->
          <details class="rounded-lg border border-amber-500/25 bg-[#14110c] p-3 mb-3">
            <summary class="cursor-pointer select-none text-sm font-semibold text-amber-200/90">
              手动额外命中检查<span class="text-xs text-gray-500 font-normal">（可选；自动结算已包含此判定）</span>
            </summary>
            <div class="mt-2 space-y-2">
            <p class="text-xs text-gray-500">攻击骰 &lt; 武器威胁值时触发；目标再投 1d6 查表。本轮已在战力比拼中受伤则勾选下方选项。</p>
            <div class="flex flex-wrap gap-2 items-center text-sm">
              <select v-model="extraHitAttackerUid" class="cat-fighter-select max-w-xs">
                <option value="">选择攻击者</option>
                <option v-for="f in attackers" :key="'eh-a-'+f.uid" :value="f.uid">攻 · {{ f.playerName }}</option>
                <option v-for="f in defenders" :key="'eh-d-'+f.uid" :value="f.uid">守 · {{ f.playerName }}</option>
              </select>
              <label class="inline-flex items-center gap-1 text-gray-300">
                目标判定骰
                <input v-model="extraHitDefendRoll" type="number" min="1" max="6" class="cat-num-square" />
              </label>
              <label class="inline-flex items-center gap-1 text-gray-400 text-xs">
                <input v-model="extraHitAlreadyInjured" type="checkbox" class="rounded" />
                本轮已受伤
              </label>
            </div>
            <div v-if="extraHitPreview" class="text-sm text-gray-200 bg-black/30 rounded-md px-3 py-2 border border-white/5">
              <template v-if="extraHitPreview.status === 'need_rolls'">请为攻击者填写攻骰，并确认武器威胁值。</template>
              <template v-else-if="extraHitPreview.status === 'no_trigger'">{{ extraHitPreview.note }}</template>
              <template v-else-if="extraHitPreview.status === 'need_defend'">{{ extraHitPreview.note }}</template>
              <template v-else>
                威胁{{ extraHitPreview.threat }} · 攻骰{{ extraHitPreview.attackRoll }} · 判定{{ extraHitPreview.defendRoll }}
                → <span class="text-amber-300 font-semibold">{{ extraHitPreview.result }}</span>
                <span v-if="extraHitPreview.note" class="block text-xs text-gray-400 mt-1">{{ extraHitPreview.note }}</span>
              </template>
            </div>
            <details class="text-xs text-gray-500">
              <summary class="cursor-pointer text-gray-400">命中表 / 威胁值索引</summary>
              <pre class="mt-2 whitespace-pre-wrap leading-relaxed">{{ hitTableHelpText }}</pre>
            </details>
            </div>
          </details>

          <!-- 对照表（次要说明） -->
          <section class="cat-outcome-note rounded-lg border border-white/5 bg-[#0a0c10]/80 p-3 opacity-75">
            <p class="text-xs text-gray-500 mb-2 font-medium uppercase tracking-wide">战果对照表（按调整值：ceil(差值×3÷人数)）</p>
            <div class="overflow-x-auto">
              <table class="w-full text-xs text-gray-500">
                <thead>
                  <tr class="border-b border-white/5">
                    <th class="py-1 pr-2 text-left font-medium">差值</th>
                    <th class="py-1 pr-2 text-left font-medium">结果</th>
                    <th class="py-1 pr-2 text-left font-medium">攻方</th>
                    <th class="py-1 text-left font-medium">守方</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="row in OUTCOME_ROWS" :key="row.name" class="border-b border-white/[0.03]">
                    <td class="py-1 pr-2 tabular-nums">{{ row.range }}</td>
                    <td class="py-1 pr-2">{{ row.name }}</td>
                    <td class="py-1 pr-2">{{ row.attacker }}</td>
                    <td class="py-1">{{ row.defender }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </section>
        </div>

        <!-- 右侧：预览 + 自动结算 -->
        <aside class="cat-result-panel w-full xl:w-64 shrink-0 xl:sticky xl:top-4 rounded-lg border border-amber-500/35 bg-gradient-to-b from-[#1c1810] to-[#100e08] p-4 shadow-lg shadow-black/30 space-y-3">
          <h3 class="text-xs font-semibold uppercase tracking-wider text-amber-500/80">自动结算</h3>

          <p class="text-[11px] text-gray-500 leading-relaxed">
            一键完成：每人 1d6（已填实体骰优先，留空自动掷）→ 结果表 → 额外命中 → 防具抵消。结算后若双方仍可战，可选择继续死战（次轮起地点防御÷2向上取整）。
          </p>

          <button
            type="button"
            class="w-full py-2.5 rounded-md bg-amber-500 text-black font-bold text-sm hover:bg-amber-400 transition-colors disabled:opacity-40"
            :disabled="!attackers.length || !defenders.length"
            @click="runSettlement()"
          >
            结算战斗
          </button>
          <p
            v-if="attackers.length && defenders.length && missingRollCount > 0"
            class="text-[11px] text-gray-500 text-center"
          >
            {{ missingRollCount }} 人未填骰，将自动掷 1d6
          </p>
          <button
            type="button"
            class="w-full py-1.5 rounded-md border border-white/15 text-gray-300 text-xs hover:bg-white/5"
            @click="resetBattleStates"
          >
            重置本场状态
          </button>

          <div class="border-t border-amber-500/20 pt-3 space-y-2">
            <div class="text-xs text-gray-400">战力预览（不含骰子）</div>
            <div class="flex justify-between text-sm">
              <span class="text-red-300">攻 {{ attackSideTotal }}</span>
              <span class="text-cyan-300">守 {{ defenseSideTotal }}</span>
            </div>
            <div class="text-xs text-gray-500">
              若直接比差：调整值预估
              <span class="text-amber-200/90 tabular-nums">{{ adjustedValue > 0 ? '+' : '' }}{{ adjustedValue }}</span>
              → {{ outcome.name }}
            </div>
          </div>

          <div v-if="deathmatchActive && lastReport && !lastReport.error" class="border-t border-red-500/30 pt-3 space-y-2">
            <p class="text-xs text-red-200/90">已结算第 {{ combatRound }} 轮 · 双方仍可战</p>
            <button
              type="button"
              class="w-full py-2 rounded-md bg-red-600/80 text-white text-sm font-semibold hover:bg-red-500"
              @click="continueDeathmatch"
            >
              双方同意：继续死战（第 {{ combatRound + 1 }} 轮）
            </button>
            <button
              type="button"
              class="w-full py-2 rounded-md border border-white/20 text-gray-200 text-sm hover:bg-white/5"
              @click="endBattle('flee')"
            >
              败逃 / 停战，结束战斗
            </button>
          </div>
          <div v-else-if="!deathmatchActive && roundReports.length && lastReport && !lastReport.error && !lastReport.fled" class="border-t border-white/10 pt-3">
            <p class="text-xs text-gray-400">一方已全员受伤或死亡，死战条件不满足，战斗结束。</p>
          </div>
        </aside>
      </div>

      <!-- 结算报告：整场战斗所有轮次 -->
      <section
        v-if="lastReport"
        class="mt-4 rounded-lg border border-amber-500/30 bg-[#121018] p-4 space-y-3"
      >
        <div class="flex flex-wrap items-center justify-between gap-2">
          <h2 class="text-lg font-bold text-amber-200">战斗结算报告</h2>
          <div class="flex items-center gap-2">
            <span v-if="roundReports.length" class="text-sm text-gray-400">共 {{ roundReports.length }} 轮</span>
            <button
              v-if="publicReportText"
              type="button"
              class="px-3 py-1 rounded-md text-sm font-semibold border transition-colors"
              :class="reportCopied
                ? 'border-emerald-500/50 text-emerald-300 bg-emerald-950/40'
                : 'border-amber-500/40 text-amber-200 hover:bg-amber-500/15'"
              @click="copyPublicReport"
            >
              {{ reportCopied ? '已复制 ✓' : '复制公屏报告（整场）' }}
            </button>
          </div>
        </div>

        <p v-if="lastReport.error" class="text-red-300">{{ lastReport.error }}</p>

        <details
          v-for="(r, ri) in roundReports"
          :key="'round-' + ri"
          class="cat-round rounded-md border border-white/10 bg-black/20"
          :open="ri === roundReports.length - 1"
        >
          <summary class="cursor-pointer select-none px-3 py-2 text-sm font-semibold text-gray-200 flex flex-wrap items-center gap-2">
            <span class="text-amber-300">第 {{ r.round }} 轮</span>
            <span>攻方「{{ r.outcomeName }}」</span>
            <span class="text-gray-500 font-normal tabular-nums">A = {{ r.adjusted > 0 ? '+' : '' }}{{ r.adjusted }}</span>
            <span v-if="r.fled" class="text-amber-300/80 font-normal">· 战斗结束</span>
          </summary>
          <div class="px-3 pb-3 space-y-3">
            <p v-if="r.fled" class="text-sm text-amber-200/90">{{ r.fleeNote }}</p>

            <div class="grid grid-cols-1 md:grid-cols-2 gap-3 text-sm">
              <div class="rounded-md bg-red-950/30 border border-red-500/20 p-3 space-y-1">
                <div class="text-red-300 font-semibold mb-1">攻方</div>
                <p>基础战力合计：{{ r.atkBase }}</p>
                <p v-for="c in r.attackers" :key="'ra-'+ri+'-'+c.uid" class="text-gray-300">
                  {{ c.name }}：基{{ combatantLineBase(c) }} + 骰{{ c.roll ?? '—' }}<span v-if="c.roll != null && !c.rollWasManual" class="text-gray-500 text-xs">（自动）</span>
                  <span class="text-gray-500">（威胁{{ c.weaponThreat }}{{ c.threatHalved ? '↓减半' : '' }}）</span>
                </p>
                <p class="text-red-200 font-medium pt-1">最终战力 {{ r.atkFinal }}</p>
              </div>
              <div class="rounded-md bg-cyan-950/30 border border-cyan-500/20 p-3 space-y-1">
                <div class="text-cyan-300 font-semibold mb-1">守方</div>
                <p>玩家战力 {{ r.defBasePlayers }} + 地点防御 {{ r.locationDefenseUsed }}
                  <span v-if="r.insiderBetrayal" class="text-amber-300">（里应外合）</span>
                  <span v-else-if="r.locationDefenseHalved" class="text-amber-300">（死战次轮÷2）</span>
                </p>
                <p v-for="c in r.defenders" :key="'rd-'+ri+'-'+c.uid" class="text-gray-300">
                  {{ c.name }}：基{{ combatantLineBase(c) }} + 骰{{ c.roll ?? '—' }}<span v-if="c.roll != null && !c.rollWasManual" class="text-gray-500 text-xs">（自动）</span>
                  <span class="text-gray-500">（威胁{{ c.weaponThreat }}{{ c.threatHalved ? '↓减半' : '' }}）</span>
                </p>
                <p class="text-cyan-200 font-medium pt-1">最终战力 {{ r.defFinal }}</p>
              </div>
            </div>

            <div class="rounded-md bg-black/30 border border-white/10 p-3 text-sm space-y-1">
              <p>差值 D = {{ r.atkFinal }} − {{ r.defFinal }} = <strong class="text-amber-200">{{ r.diff > 0 ? '+' : '' }}{{ r.diff }}</strong></p>
              <p>调整值 A = ceil(({{ r.diff }} × 3) ÷ {{ r.totalFighters }}) = <strong class="text-amber-200">{{ r.adjusted > 0 ? '+' : '' }}{{ r.adjusted }}</strong></p>
              <p class="text-xl font-black text-amber-300 pt-1">结果：攻方「{{ r.outcomeName }}」</p>
              <p class="text-gray-400">攻方：{{ r.outcomeAttackText }}　｜　守方：{{ r.outcomeDefenseText }}</p>
            </div>

            <div v-if="r.extraHits?.length" class="space-y-2">
              <h3 class="text-sm font-semibold text-amber-200/90">额外命中</h3>
              <ul class="text-sm space-y-1.5">
                <li
                  v-for="(h, i) in r.extraHits"
                  :key="'eh-'+ri+'-'+i"
                  class="rounded-md bg-amber-950/20 border border-amber-500/15 px-3 py-2 text-gray-300"
                >
                  <template v-if="h.skipped">
                    {{ h.attacker }} 攻骰{{ h.attackRoll }} &lt; 威胁{{ h.threat }}，但{{ h.note }}
                  </template>
                  <template v-else>
                    {{ h.attacker }}（攻骰{{ h.attackRoll }} &lt; 威胁{{ h.threat }}）→ {{ h.target }}
                    判定骰{{ h.defendRoll }} → 表结果「{{ h.tableResult }}」→ 生效「{{ h.applied }}」
                    <span class="text-gray-500">（{{ h.targetBefore }} → {{ h.targetAfter }}）</span>
                    <span v-if="h.note" class="block text-xs text-gray-500">{{ h.note }}</span>
                  </template>
                </li>
              </ul>
            </div>
            <p v-else class="text-sm text-gray-500">本轮无额外命中触发。</p>

            <div v-if="r.armorLogs?.length" class="space-y-1">
              <h3 class="text-sm font-semibold text-emerald-300/90">防具抵消</h3>
              <p v-for="(log, i) in r.armorLogs" :key="'al-'+ri+'-'+i" class="text-sm text-emerald-200/80">{{ log }}</p>
            </div>

            <div class="grid grid-cols-1 md:grid-cols-2 gap-3">
              <div>
                <h3 class="text-sm font-semibold text-red-300 mb-1">攻方本轮后状态</h3>
                <p v-for="c in r.attackers" :key="'fas-'+ri+'-'+c.uid" class="text-sm text-gray-300">
                  {{ c.name }}：{{ statusLabel(c) }}
                  <span v-if="c.armorUsed" class="text-xs text-gray-500">（防具已用）</span>
                </p>
              </div>
              <div>
                <h3 class="text-sm font-semibold text-cyan-300 mb-1">守方本轮后状态</h3>
                <p v-for="c in r.defenders" :key="'fds-'+ri+'-'+c.uid" class="text-sm text-gray-300">
                  {{ c.name }}：{{ statusLabel(c) }}
                  <span v-if="c.armorUsed" class="text-xs text-gray-500">（防具已用）</span>
                </p>
              </div>
            </div>
          </div>
        </details>

        <!-- 完成战斗：将伤害写入玩家档案 -->
        <div v-if="roundReports.length" class="border-t border-white/10 pt-3 space-y-2">
          <div class="flex flex-wrap items-center gap-3">
            <button
              type="button"
              class="px-4 py-2 rounded-md bg-red-600 text-white text-sm font-bold hover:bg-red-500 transition-colors disabled:opacity-40 disabled:cursor-not-allowed"
              :disabled="!statusChanges.length || applyingStatuses"
              @click="showApplyConfirm = true"
            >
              完成战斗：落实伤害到玩家状态{{ statusChanges.length ? `（${statusChanges.length} 人）` : '' }}
            </button>
            <span v-if="!statusChanges.length && !applyResultMsg" class="text-sm text-gray-500">无状态变化需要写入</span>
            <span
              v-if="applyResultMsg"
              class="text-sm"
              :class="applyResultMsg.includes('失败') ? 'text-red-300' : 'text-emerald-300'"
            >{{ applyResultMsg }}</span>
          </div>
          <p class="text-xs text-gray-500">将本场战斗产生的受伤/重伤/虚弱/死亡写入玩家档案。写入前会再次确认。</p>
        </div>
      </section>

      <!-- 落实伤害确认弹窗 -->
      <div
        v-if="showApplyConfirm"
        class="fixed inset-0 z-50 flex items-center justify-center bg-black/70 p-4"
        @click.self="showApplyConfirm = false"
      >
        <div class="w-full max-w-md rounded-lg border border-red-500/40 bg-[#16121a] p-5 space-y-3 shadow-2xl">
          <h3 class="text-base font-bold text-red-300">确认落实伤害</h3>
          <p class="text-sm text-gray-400">以下状态将写入玩家档案（写入后需手动改回）：</p>
          <ul class="text-sm text-gray-200 space-y-1.5 max-h-60 overflow-y-auto pr-1">
            <li v-for="ch in statusChanges" :key="'chg-' + ch.playerId" class="flex items-center gap-2">
              <span class="font-medium">{{ ch.name }}</span>
              <span class="text-gray-500">{{ ch.beforeLabel }} →</span>
              <strong :class="ch.after.dead ? 'text-red-400' : ch.after.severe ? 'text-orange-300' : 'text-amber-200'">{{ ch.afterLabel }}</strong>
            </li>
          </ul>
          <div class="flex gap-2 justify-end pt-1">
            <button
              type="button"
              class="px-4 py-1.5 rounded-md border border-white/20 text-gray-300 text-sm hover:bg-white/5"
              :disabled="applyingStatuses"
              @click="showApplyConfirm = false"
            >
              取消
            </button>
            <button
              type="button"
              class="px-4 py-1.5 rounded-md bg-red-600 text-white text-sm font-bold hover:bg-red-500 disabled:opacity-50"
              :disabled="applyingStatuses"
              @click="applyStatusesToPlayers"
            >
              {{ applyingStatuses ? '写入中…' : '确认写入' }}
            </button>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.cat-combat-main {
  font-size: 1rem;
  line-height: 1.45;
}
.cat-combat-main .cat-fighter-select {
  font-size: 0.8125rem;
  padding: 0.25rem 0.4rem;
}
.cat-combat-main .cat-stat-label {
  font-size: 0.6875rem;
}
.cat-combat-main .cat-stat-label-input {
  font-size: 0.625rem;
  height: 1rem;
}
.cat-combat-main .cat-num-square {
  width: 2.625rem;
  height: 2.625rem;
  font-size: 0.9375rem;
}
.cat-combat-main .cat-num-square--add {
  font-size: 1.25rem;
}
.cat-combat-main .cat-fighter-remove {
  width: 1.45rem;
  height: 1.45rem;
  font-size: 1.0625rem;
}
.cat-combat-main .cat-bonus-remove {
  font-size: 0.7rem;
  width: 0.95rem;
  height: 0.95rem;
}
.cat-fighter-card {
  padding: 0.5rem 0.5rem 0.4rem;
  border-radius: 0.375rem;
  border-width: 1px;
  border-style: solid;
}
.cat-fighter-card--attack {
  background: #4a4454;
  border-color: rgba(248, 113, 113, 0.35);
}
.cat-fighter-card--defense {
  background: #44525a;
  border-color: rgba(34, 211, 238, 0.35);
}
.cat-fighter-remove {
  position: absolute;
  top: 0.2rem;
  right: 0.2rem;
  z-index: 1;
  width: 1.35rem;
  height: 1.35rem;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  font-size: 1rem;
  line-height: 1;
  font-weight: 600;
  color: #9ca3af;
  background: rgba(0, 0, 0, 0.35);
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 0.25rem;
  cursor: pointer;
  transition: color 0.15s, background 0.15s;
}
.cat-fighter-remove--attack:hover {
  color: #fca5a5;
  background: rgba(127, 29, 29, 0.55);
}
.cat-fighter-remove--defense:hover {
  color: #67e8f9;
  background: rgba(22, 78, 99, 0.55);
}
.cat-fighter-select {
  padding: 0.2rem 0.35rem;
  font-size: 0.75rem;
  line-height: 1.2;
  color: #e5e7eb;
  background: rgba(0, 0, 0, 0.35);
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 0.25rem;
}
.cat-fighter-select:disabled {
  opacity: 0.55;
}
.cat-stat-row {
  display: flex;
  gap: 0.4rem;
  align-items: flex-end;
}
.cat-stat-row--wrap {
  flex-wrap: wrap;
  margin-bottom: 0;
}
.cat-stat-row__tail {
  display: flex;
  gap: 0.4rem;
  align-items: flex-end;
  margin-left: auto;
  flex-shrink: 0;
}
.cat-stat-cell {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.12rem;
  min-width: 0;
}
.cat-stat-cell--bonus {
  position: relative;
}
.cat-stat-label {
  font-size: 0.625rem;
  line-height: 1;
  font-weight: 600;
  color: #9ca3af;
  letter-spacing: 0.02em;
  white-space: nowrap;
}
.cat-stat-label-input {
  width: 2.625rem;
  max-width: 2.625rem;
  height: 0.875rem;
  padding: 0 0.15rem;
  font-size: 0.5625rem;
  line-height: 1.1;
  font-weight: 600;
  text-align: center;
  color: #d1d5db;
  background: rgba(0, 0, 0, 0.25);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 0.2rem;
}
.cat-stat-label-input::placeholder {
  color: #6b7280;
  font-weight: 500;
}
.cat-num-square {
  width: 2.5rem;
  height: 2.5rem;
  padding: 0;
  font-size: 0.875rem;
  font-weight: 600;
  line-height: 1;
  text-align: center;
  color: #f3f4f6;
  background: rgba(0, 0, 0, 0.4);
  border: 1px solid rgba(255, 255, 255, 0.14);
  border-radius: 0.25rem;
  font-variant-numeric: tabular-nums;
  -moz-appearance: textfield;
}
.cat-num-square::-webkit-outer-spin-button,
.cat-num-square::-webkit-inner-spin-button {
  -webkit-appearance: none;
  margin: 0;
}
.cat-num-square--total {
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  color: #fcd34d;
  background: rgba(120, 53, 15, 0.35);
  border-color: rgba(245, 158, 11, 0.4);
}
.cat-num-square--add {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.125rem;
  font-weight: 700;
  color: #9ca3af;
  background: rgba(0, 0, 0, 0.25);
  border-style: dashed;
  cursor: pointer;
  transition: color 0.15s, border-color 0.15s, background 0.15s;
}
.cat-num-square--add-attack:hover {
  color: #fca5a5;
  border-color: rgba(248, 113, 113, 0.55);
  background: rgba(127, 29, 29, 0.35);
}
.cat-num-square--add-defense:hover {
  color: #67e8f9;
  border-color: rgba(34, 211, 238, 0.55);
  background: rgba(22, 78, 99, 0.35);
}
.cat-bonus-remove {
  position: absolute;
  top: -0.35rem;
  right: -0.35rem;
  z-index: 2;
  width: 0.9rem;
  height: 0.9rem;
  padding: 0;
  font-size: 0.65rem;
  line-height: 1;
  color: #9ca3af;
  background: #374151;
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: 9999px;
  cursor: pointer;
}
.cat-bonus-remove:hover {
  color: #f3f4f6;
  background: #4b5563;
}
.cat-skill-box {
  position: relative;
  display: inline-block;
}
.cat-skill-warn {
  position: absolute;
  right: -0.2rem;
  bottom: -0.15rem;
  z-index: 2;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 0.8rem;
  height: 0.8rem;
  font-size: 0.5625rem;
  font-weight: 800;
  line-height: 1;
  color: #1c1917;
  background: #fbbf24;
  border: 1px solid rgba(0, 0, 0, 0.35);
  border-radius: 9999px;
  cursor: help;
  pointer-events: auto;
  box-shadow: 0 0 0 1px rgba(251, 191, 36, 0.35);
}
.cat-skill-warn:hover {
  background: #fcd34d;
}
.cat-outcome-note {
  color: #6b7280;
}
</style>
