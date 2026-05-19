<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { playerAPI, dmPlayerAPI, locationAPI } from '@/utils/api.js'
import { GAME_ITEM_NAMES } from '@/data/gameData.js'
import {
  WEAPON_THREAT_BY_ID,
  ARMOR_ITEM_IDS,
  COMBAT_OUTCOMES,
  parseCombatNumber,
  computeSkillBonus,
  getSkillBonusSuppression,
  fighterTotalPower,
  resolveCombatOutcome,
  createEmptyFighter,
  createDefaultBonusRow,
} from '@/data/combatAssist.js'

const attackers = ref([])
const defenders = ref([])
const players = ref([])
const weapons = ref([])
const locations = ref([])
const loading = ref(true)

const useLocationDefense = ref(false)
const defenseLocationId = ref('')
const locationDefenseValue = ref('')

const BONUS_LABEL_PLACEHOLDER = '数值调整'

const OUTCOME_ROWS = [
  { range: '≥5', ...COMBAT_OUTCOMES[0] },
  { range: '3～4', ...COMBAT_OUTCOMES[1] },
  { range: '1～2', ...COMBAT_OUTCOMES[2] },
  { range: '0', ...COMBAT_OUTCOMES[3] },
  { range: '-1～-2', ...COMBAT_OUTCOMES[4] },
  { range: '-3～-4', ...COMBAT_OUTCOMES[5] },
  { range: '≤-5', ...COMBAT_OUTCOMES[6] },
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
  if (useLocationDefense.value) t += parseCombatNumber(locationDefenseValue.value)
  return t
})
const powerDiff = computed(() => attackSideTotal.value - defenseSideTotal.value)
const outcome = computed(() => resolveCombatOutcome(powerDiff.value))

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
  let weaponThreat = fighter.weaponThreat
  if (id !== '' && id != null) {
    const threat = WEAPON_THREAT_BY_ID[Number(id)]
    if (threat != null) weaponThreat = threat
  } else {
    weaponThreat = ''
  }
  return {
    weaponThreat,
    skillBonus: computeSkillBonus(fighter.jobSkills, id, combatOptionsForFighter(fighter)),
  }
}

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
        threat: WEAPON_THREAT_BY_ID[w.itemId] ?? 0,
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
  if (f.bonuses.length <= 1) {
    setFighter(side, uid, { bonuses: [{ ...f.bonuses[0], value: '' }] })
    return
  }
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

                  <div class="cat-stat-row cat-stat-row--wrap">
                    <label class="cat-stat-cell" title="基础战力">
                      <span class="cat-stat-label">基</span>
                      <input v-model.number="f.basePower" type="number" class="cat-num-square" />
                    </label>
                    <label class="cat-stat-cell" title="武器威胁">
                      <span class="cat-stat-label">武</span>
                      <input v-model.number="f.weaponThreat" type="number" class="cat-num-square" />
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
                        v-if="f.bonuses.length > 1"
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
                <label class="flex items-center gap-2 text-sm text-gray-300 cursor-pointer">
                  <input v-model="useLocationDefense" type="checkbox" class="rounded" />
                  计入地点防御
                </label>
                <div v-if="useLocationDefense" class="flex gap-1.5 items-center">
                  <select v-model="defenseLocationId" class="cat-fighter-select flex-1 min-w-0">
                    <option value="">选地点</option>
                    <option v-for="loc in locations" :key="loc.id" :value="loc.id">{{ loc.name }}（{{ loc.defenseValue ?? 0 }}）</option>
                  </select>
                  <input v-model="locationDefenseValue" type="number" class="cat-num-square shrink-0" title="地点防御值" />
                </div>
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
                  <div class="cat-stat-row cat-stat-row--wrap">
                    <label class="cat-stat-cell" title="基础战力">
                      <span class="cat-stat-label">基</span>
                      <input v-model.number="f.basePower" type="number" class="cat-num-square" />
                    </label>
                    <label class="cat-stat-cell" title="武器威胁">
                      <span class="cat-stat-label">武</span>
                      <input v-model.number="f.weaponThreat" type="number" class="cat-num-square" />
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
                        v-if="f.bonuses.length > 1"
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

          <!-- 对照表（次要说明） -->
          <section class="cat-outcome-note rounded-lg border border-white/5 bg-[#0a0c10]/80 p-3 opacity-75">
            <p class="text-xs text-gray-500 mb-2 font-medium uppercase tracking-wide">战果对照表（攻方战力 − 守方战力）</p>
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

        <!-- 右侧：当前判定 -->
        <aside class="cat-result-panel w-full xl:w-56 shrink-0 xl:sticky xl:top-4 rounded-lg border border-amber-500/35 bg-gradient-to-b from-[#1c1810] to-[#100e08] p-4 shadow-lg shadow-black/30">
          <h3 class="text-xs font-semibold uppercase tracking-wider text-amber-500/80 mb-3">当前判定</h3>

          <div class="space-y-3 mb-4">
            <div>
              <div class="text-xs text-gray-400 mb-0.5">攻方最终战力</div>
              <div class="text-2xl font-black text-red-300 tabular-nums">{{ attackSideTotal }}</div>
            </div>
            <div>
              <div class="text-xs text-gray-400 mb-0.5">守方最终战力</div>
              <div class="text-2xl font-black text-cyan-300 tabular-nums">{{ defenseSideTotal }}</div>
              <p v-if="useLocationDefense" class="text-xs text-gray-500 mt-1">
                玩家 {{ defensePlayersTotal }} + 地点 {{ parseCombatNumber(locationDefenseValue) }}
              </p>
            </div>
          </div>

          <div class="border-t border-amber-500/20 pt-4 mb-4">
            <div class="text-xs text-gray-400 mb-1">战果</div>
            <div class="text-3xl font-black text-amber-300 tracking-wide">{{ outcome.name }}</div>
          </div>

          <div class="space-y-3 text-sm">
            <div class="rounded-md bg-red-950/25 border border-red-500/20 px-3 py-2">
              <div class="text-xs text-red-300/80 mb-1">攻方</div>
              <p class="text-gray-100 leading-relaxed">{{ outcome.attacker }}</p>
            </div>
            <div class="rounded-md bg-cyan-950/25 border border-cyan-500/20 px-3 py-2">
              <div class="text-xs text-cyan-300/80 mb-1">守方</div>
              <p class="text-gray-100 leading-relaxed">{{ outcome.defender }}</p>
            </div>
          </div>
        </aside>
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
