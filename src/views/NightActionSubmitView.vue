<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { nightActionAPI, locationAPI, actionAPI, explorationAPI, playerAPI } from '@/utils/api.js'
import { useGameDayScope } from '@/composables/useGameDayScope.js'
import { applyNightPayload } from '@/utils/actionFormHydration.js'
import {
  FACTION_LABELS,
  NIGHT_ACTION_DEFS,
  NIGHT_PERSONAL_ACTION_TYPES,
  PRESSURE_DEMAND_OPTIONS,
  RAID_OUTCOME_OPTIONS,
  getConspiracySubtypes,
} from '@/data/nightActions.js'

const playerId = parseInt(localStorage.getItem('playerId') || '0')
const {
  currentGameDay,
  phaseLabel,
  viewGameDay: gameDay,
  dayOptions,
  nightEditable,
  viewOnlyNightReason,
  loadGameState,
  syncFromContext,
} = useGameDayScope()

const isHydrating = ref(false)
const context = ref(null)
const locations = ref([])
const productionInfo = ref(null)
const loading = ref(true)
const submitting = ref(false)
const submitMessage = ref(null)
const selectedType = ref('')
const actionResult = ref('')
const explorationDetails = ref(null)

const forms = reactive({
  night_personal_action: { actionType: '', targetId: '', npcId: '', notes: '' },
  public_trial: { targetPlayerId: '', note: '' },
  pressure_ruler: { demand: '', note: '' },
  publicity: { message: '', note: '' },
  conspiracy: {
    conspiracySubtype: '',
    targetLocationId: '',
    targetPlayerId: '',
    participantIds: [],
    raidOutcome: '',
    note: '',
  },
  explore_island: { investItems: {} },
  other: { note: '' },
})

const EXPLORATION_ITEMS = [
  { itemId: 26, itemType: 'item', name: '火把', bonus: 7, icon: '🔥' },
  { itemId: 2, itemType: 'item', name: '手电筒', bonus: 5, icon: '🔦' },
  { itemId: 13, itemType: 'item', name: '蜡烛', bonus: 2, icon: '🕯️' },
  { itemId: 3, itemType: 'material', name: '绳索', bonus: 1, icon: '🪢' },
]

const MAX_EXPLORATION_POINTS = 15
const playerInventory = ref([])

async function loadPlayerInventory() {
  try {
    const result = await playerAPI.getItems(playerId)
    if (Array.isArray(result)) {
      // 过滤出探索可用的物品类型（item和material）
      playerInventory.value = result.filter((item) => item.type === 'item' || item.type === 'material')
    }
  } catch (e) {
    console.error('加载玩家物资失败:', e)
  }
}

function getItemQuantity(itemId) {
  const item = playerInventory.value.find((i) => i.id === itemId && i.type === EXPLORATION_ITEMS.find(e => e.itemId === itemId)?.itemType)
  return item ? item.quantity || 0 : 0
}

function getInvestQuantity(itemId) {
  return forms.explore_island.investItems[itemId] || 0
}

function setInvestQuantity(itemId, quantity) {
  const max = getItemQuantity(itemId)
  const qty = Math.max(0, Math.min(max, quantity))
  if (qty > 0) {
    forms.explore_island.investItems[itemId] = qty
  } else {
    delete forms.explore_island.investItems[itemId]
  }
}

const totalInvestPoints = computed(() => {
  let total = 0
  for (const item of EXPLORATION_ITEMS) {
    const qty = getInvestQuantity(item.itemId)
    total += qty * item.bonus
  }
  return Math.min(total, MAX_EXPLORATION_POINTS)
})

const investItemsForSubmit = computed(() => {
  const items = {}
  for (const [itemId, qty] of Object.entries(forms.explore_island.investItems)) {
    if (qty > 0) {
      items[itemId] = qty
    }
  }
  return items
})

const playerFaction = computed(() => context.value?.faction || '')
const actionDefs = computed(() => NIGHT_ACTION_DEFS[playerFaction.value] || [])
const unlimitedActions = computed(() => !!context.value?.unlimitedActions)
const hasSubmittedToday = computed(() => !!context.value?.hasSubmittedToday)
const usedTypes = computed(() => new Set(context.value?.usedActionTypes || []))
const allPlayers = computed(() => context.value?.allPlayers || [])
const conspiracyOptions = computed(() => getConspiracySubtypes(playerFaction.value))

const locationOptions = computed(() =>
  (locations.value || []).map((l) => ({ value: l.id, label: `${l.name}（${l.area || ''}）` }))
)

const personalActionOptions = computed(() =>
  NIGHT_PERSONAL_ACTION_TYPES.filter(
    (o) => o.value !== 'produce' || productionInfo.value?.canProduce
  )
)

const personalTargetOptions = computed(() => {
  const at = forms.night_personal_action.actionType
  if (at === 'go_location') return locationOptions.value
  if (at === 'investigate_player') {
    return allPlayers.value
      .filter((p) => p.id !== playerId)
      .map((p) => ({ value: p.id, label: p.name }))
  }
  return []
})

const personalNpcOptions = computed(() => {
  const locId = parseInt(forms.night_personal_action.targetId)
  if (!locId) return []
  const loc = (locations.value || []).find((l) => l.id === locId)
  return (loc?.npcs || []).map((n) => ({ value: n.id, label: `${n.name}（${n.job}）` }))
})

const selectedDef = computed(() => actionDefs.value.find((d) => d.type === selectedType.value) || null)

const actionTypeOptions = computed(() =>
  actionDefs.value.map((def) => {
    const used = unlimitedActions.value && usedTypes.value.has(def.type)
    const quota = !unlimitedActions.value && hasSubmittedToday.value
    return {
      value: def.type,
      label: def.title,
      disabled: used || quota,
      statusLabel: used ? '今日已使用' : quota ? '今日已提交' : '可执行',
    }
  })
)

const currentStatus = computed(() => {
  if (!selectedType.value) return { btn: 'disabled', label: '' }
  const opt = actionTypeOptions.value.find((o) => o.value === selectedType.value)
  if (!opt) return { btn: 'disabled', label: '' }
  return {
    btn: opt.disabled ? 'disabled' : 'enabled',
    label: opt.statusLabel,
    key: opt.disabled ? 'blocked' : 'ready',
  }
})

const canSubmit = computed(
  () =>
    nightEditable.value &&
    selectedType.value &&
    currentStatus.value?.btn === 'enabled' &&
    !submitting.value
)

const formReadOnly = computed(() => !nightEditable.value)

const showConspiracyLocation = computed(() => {
  const sub = forms.conspiracy.conspiracySubtype
  return sub && sub !== 'spread_terror'
})

const showConspiracyTerrorTarget = computed(() => forms.conspiracy.conspiracySubtype === 'spread_terror')
const showRaidOutcome = computed(() => forms.conspiracy.conspiracySubtype === 'raid_location')

function isTypeUsed(type) {
  return unlimitedActions.value && usedTypes.value.has(type)
}

function toggleParticipant(id) {
  const ids = forms.conspiracy.participantIds
  const i = ids.indexOf(id)
  if (i >= 0) ids.splice(i, 1)
  else ids.push(id)
}

function resetForm(type) {
  const defaults = {
    night_personal_action: { actionType: '', targetId: '', npcId: '', notes: '' },
    public_trial: { targetPlayerId: '', note: '' },
    pressure_ruler: { demand: '', note: '' },
    publicity: { message: '', note: '' },
    conspiracy: {
      conspiracySubtype: '',
      targetLocationId: '',
      targetPlayerId: '',
      participantIds: [],
      raidOutcome: '',
      note: '',
    },
    explore_island: { investItems: {} },
    other: { note: '' },
  }
  if (defaults[type]) Object.assign(forms[type], defaults[type])
}

watch(selectedType, (type, prev) => {
  if (isHydrating.value) return
  if (prev) resetForm(prev)
  if (!isHydrating.value) actionResult.value = ''
})

watch(() => forms.night_personal_action.actionType, () => {
  forms.night_personal_action.targetId = ''
  forms.night_personal_action.npcId = ''
})

watch(() => forms.conspiracy.conspiracySubtype, () => {
  forms.conspiracy.targetLocationId = ''
  forms.conspiracy.targetPlayerId = ''
  forms.conspiracy.raidOutcome = ''
})

async function loadContext() {
  loading.value = true
  try {
    const [ctx, locs, prod] = await Promise.all([
      nightActionAPI.getContext(playerId, gameDay.value),
      locationAPI.getAll(),
      actionAPI.getProductionInfo(playerId),
    ])
    context.value = ctx
    syncFromContext(ctx)
    locations.value = Array.isArray(locs) ? locs : []
    productionInfo.value = prod
    hydrateNightFromHistory()
    await loadPlayerInventory()
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

function hydrateNightFromHistory() {
  const history = context.value?.history
  if (!history?.length) {
    if (!isHydrating.value) {
      selectedType.value = ''
      actionResult.value = ''
      explorationDetails.value = null
    }
    return
  }
  isHydrating.value = true
  try {
    const entry = history[0]
    const type = entry.actionType
    if (type) {
      resetForm(type)
      applyNightPayload(type, entry.payload || {}, forms)
      selectedType.value = type
      actionResult.value = entry.result || ''

      // 如果是探索行动，恢复探索详情
      if (type === 'explore_island') {
        explorationDetails.value = {
          investPoints: entry.investPoints,
          diceResult: entry.diceResult,
          totalExplorationValue: entry.totalExplorationValue,
        }
      } else {
        explorationDetails.value = null
      }
    }
  } finally {
    isHydrating.value = false
  }
}

watch(gameDay, () => {
  if (!isHydrating.value) {
    selectedType.value = ''
    actionResult.value = ''
    explorationDetails.value = null
  }
  loadContext()
})

function buildPayload(type) {
  const f = forms[type]
  switch (type) {
    case 'night_personal_action':
      return {
        actionType: f.actionType,
        targetId: f.targetId ? parseInt(f.targetId) : null,
        npcId: f.npcId ? parseInt(f.npcId) : null,
        notes: f.notes || '',
      }
    case 'public_trial':
      return { targetPlayerId: parseInt(f.targetPlayerId), note: f.note || '' }
    case 'pressure_ruler':
      return { demand: f.demand, note: f.note || '' }
    case 'publicity':
      return { message: f.message, note: f.note || '' }
    case 'conspiracy':
      return {
        conspiracySubtype: f.conspiracySubtype,
        targetLocationId: f.targetLocationId ? parseInt(f.targetLocationId) : null,
        targetPlayerId: f.targetPlayerId ? parseInt(f.targetPlayerId) : null,
        participantIds: f.participantIds.map(Number),
        raidOutcome: f.raidOutcome || null,
        note: f.note || '',
      }
    case 'other':
      return { note: f.note || '' }
    case 'explore_island':
      return {}
    default:
      return {}
  }
}

function validateClient(type) {
  const f = forms[type]
  switch (type) {
    case 'night_personal_action':
      if (!f.actionType) return '请选择个人行动'
      if (['go_location', 'investigate_player'].includes(f.actionType) && !f.targetId) return '请选择目标'
      if (['use_trait', 'use_skill'].includes(f.actionType) && (!f.notes || f.notes.trim().length < 5)) {
        return '使用特性/技能时请在备注中详细描述'
      }
      if (f.actionType === 'other' && (!f.notes || f.notes.trim().length < 5)) {
        return '请详细描述你想执行的具体行动内容'
      }
      break
    case 'public_trial':
      if (!f.targetPlayerId) return '请选择审判目标'
      break
    case 'pressure_ruler':
      if (!f.demand) return '请选择施压诉求'
      break
    case 'publicity':
      if (!f.message?.trim()) return '请填写宣传内容'
      break
    case 'conspiracy':
      if (!f.conspiracySubtype) return '请选择密谋类型'
      if (showConspiracyLocation.value && !f.targetLocationId) return '请选择目标地点'
      if (showConspiracyTerrorTarget.value && !f.targetLocationId && !f.targetPlayerId) {
        return '请选择目标地点或玩家'
      }
      if (!f.participantIds.length) return '请至少选择一名参与玩家'
      if (showRaidOutcome.value && !f.raidOutcome) return '请选择袭击成功后的意向'
      break
    case 'explore_island':
      break
    case 'other':
      if (!f.note || f.note.trim().length < 5) return '请详细描述你想执行的具体行动内容'
      break
    default:
      break
  }
  return null
}

async function submitAction() {
  if (!nightEditable.value) {
    alert(viewOnlyNightReason.value || '当前不可提交')
    return
  }
  if (!selectedType.value) {
    alert('请选择夜晚行动')
    return
  }
  const err = validateClient(selectedType.value)
  if (err) {
    alert(err)
    return
  }
  if (currentStatus.value?.btn === 'disabled') return

  submitting.value = true
  submitMessage.value = null
  try {
    let res
    if (selectedType.value === 'explore_island') {
      res = await explorationAPI.submit(playerId, gameDay.value, investItemsForSubmit.value)
    } else {
      res = await nightActionAPI.submitAction({
        playerId,
        actionType: selectedType.value,
        gameDay: gameDay.value,
        payload: buildPayload(selectedType.value),
      })
    }
    if (res?.success) {
      if (selectedType.value === 'explore_island' && res.data) {
        const data = res.data
        explorationDetails.value = {
          investPoints: data.investPoints,
          diceResult: data.diceResult,
          totalExplorationValue: data.totalExplorationValue,
          targetDifficulty: res.targetDifficulty,
        }
        // 生成友好的探索结果显示
        let detailText = `✓ 已提交【探索岛屿】\n\n`
        detailText += `📊 探索统计\n`
        detailText += `• 投入探索值: ${data.investPoints}\n`
        detailText += `• 骰子结果: ${data.diceResult}\n`
        detailText += `• 总探索值: ${data.totalExplorationValue}\n`
        if (res.targetDifficulty) {
          detailText += `• 目标难度: ${res.targetDifficulty} (最高20)\n`
        }
        
        if (res.eventTriggered && data.event) {
          detailText += `\n🎯 探索事件\n`
          detailText += `发现: ${data.event.name}\n`
          detailText += `难度: ${data.event.eventDifficulty}\n`
          
          if (res.rewards && res.rewards.length > 0) {
            detailText += `\n🎁 探索奖励\n`
            res.rewards.forEach(r => {
              detailText += `+${r.quantity}${r.unit} ${r.name}\n`
            })
          }
        } else {
          detailText += `\n等待主持人在夜晚阶段结算探索结果。`
        }
        
        actionResult.value = detailText
      } else {
        explorationDetails.value = null
        actionResult.value = res.data?.result || '已提交'
      }
      submitMessage.value = { type: 'success', text: '夜晚行动提交成功' }
      await loadContext()
    } else {
      submitMessage.value = { type: 'error', text: res?.message || '提交失败' }
    }
  } catch {
    submitMessage.value = { type: 'error', text: '提交失败，请重试' }
  } finally {
    submitting.value = false
    if (submitMessage.value) setTimeout(() => { submitMessage.value = null }, 3000)
  }
}

const selectClass =
  'w-full appearance-none bg-white/5 border border-white/10 rounded-xl px-4 py-3 pr-10 text-gray-200 text-sm focus:outline-none focus:border-indigo-500/50'
const textareaClass =
  'w-full resize-none bg-white/5 border border-white/10 rounded-xl px-4 py-3 text-gray-200 text-sm placeholder:text-gray-600 focus:outline-none focus:border-indigo-500/50'

onMounted(async () => {
  await loadGameState()
  await loadContext()
})
</script>

<template>
  <div>
      <div class="text-center mb-10">
        <h1 class="text-white text-2xl md:text-3xl font-semibold tracking-tight mb-2">夜晚行动</h1>
        <p class="text-gray-500 text-sm">根据阵营选择夜间行动并提交，由主持人在夜晚阶段结算</p>
        <div class="mt-3 flex items-center justify-center gap-2">
          <label class="text-gray-400 text-sm">查看天数：</label>
          <select
            v-model.number="gameDay"
            class="bg-black/30 border border-white/10 rounded-lg px-3 py-1 text-sm text-gray-200"
          >
            <option v-for="d in dayOptions" :key="d" :value="d">第 {{ d }} 天</option>
          </select>
          <span class="text-gray-600 text-xs">
            游戏第 {{ currentGameDay }} 天 · {{ phaseLabel }}
            <template v-if="gameDay === currentGameDay">（当前）</template>
          </span>
        </div>
      </div>

      <div
        v-if="viewOnlyNightReason"
        class="mb-6 max-w-3xl mx-auto rounded-2xl border border-slate-500/40 bg-slate-500/10 px-5 py-3 text-center text-slate-300 text-sm"
      >
        {{ viewOnlyNightReason }}
      </div>

      <div v-if="loading" class="flex justify-center py-20">
        <div class="w-12 h-12 border-4 border-indigo-500 border-t-transparent rounded-full animate-spin" />
      </div>

      <div v-else-if="context && !context.success" class="text-center py-16 text-gray-400">
        {{ context.message || '暂无可用夜晚行动' }}
      </div>

      <template v-else>
        <div
          class="relative bg-gradient-to-br from-[#1a2332] to-[#0f1419] border border-white/10 rounded-3xl p-6 mb-8"
        >
          <fieldset class="space-y-4 border-0 p-0 m-0 min-w-0" :disabled="formReadOnly">
            <div class="flex items-center gap-3">
              <span
                class="inline-flex w-9 h-9 items-center justify-center rounded-lg bg-indigo-500/20 border border-indigo-500/30 text-indigo-300 text-sm"
              >🌙</span>
              <h2 class="text-white text-lg">夜晚行动提交</h2>
              <span
                v-if="currentStatus.label"
                class="ml-auto text-xs px-2 py-0.5 rounded-full"
                :class="currentStatus.key === 'ready' ? 'bg-green-500/20 text-green-400' : 'bg-gray-500/20 text-gray-400'"
              >
                {{ currentStatus.label }}
              </span>
            </div>

            <div>
              <label class="block text-gray-500 text-xs mb-2">选择行动</label>
              <select v-model="selectedType" :class="selectClass">
                <option value="">请选择行动</option>
                <option
                  v-for="opt in actionTypeOptions"
                  :key="opt.value"
                  :value="opt.value"
                  :disabled="opt.disabled"
                >
                  {{ opt.label }}{{ opt.disabled ? `（${opt.statusLabel}）` : '' }}
                </option>
              </select>
            </div>

            <div v-if="selectedDef" class="rounded-xl border border-white/10 bg-black/20 px-4 py-3">
              <p class="text-gray-300 text-sm">{{ selectedDef.description }}</p>
            </div>

            <!-- 夜晚个人行动 -->
            <template v-if="selectedType === 'night_personal_action'">
              <p class="text-indigo-300/90 text-xs">与白天个人行动相同，选择一项行动提交。每日一次。</p>
              <div>
                <label class="block text-gray-500 text-xs mb-2">行动类型</label>
                <select v-model="forms.night_personal_action.actionType" :class="selectClass">
                  <option value="">请选择</option>
                  <option v-for="a in personalActionOptions" :key="a.value" :value="a.value">{{ a.label }}</option>
                </select>
              </div>
              <div v-if="['go_location', 'investigate_player'].includes(forms.night_personal_action.actionType)">
                <label class="block text-gray-500 text-xs mb-2">目标</label>
                <select v-model="forms.night_personal_action.targetId" :class="selectClass">
                  <option value="">请选择</option>
                  <option v-for="o in personalTargetOptions" :key="o.value" :value="o.value">{{ o.label }}</option>
                </select>
              </div>
              <div
                v-if="forms.night_personal_action.actionType === 'go_location' && personalNpcOptions.length"
              >
                <label class="block text-gray-500 text-xs mb-2">交互 NPC（可选）</label>
                <select v-model="forms.night_personal_action.npcId" :class="selectClass">
                  <option value="">不交互</option>
                  <option v-for="n in personalNpcOptions" :key="n.value" :value="n.value">{{ n.label }}</option>
                </select>
              </div>
              <div v-if="forms.night_personal_action.actionType === 'other'">
                <div class="rounded-xl border border-gray-500/20 bg-gray-500/5 px-4 py-3">
                  <p class="text-gray-300 text-sm">请在下方描述中详细说明你想执行的具体行动内容，由DM判定是否成功及效果</p>
                </div>
              </div>
              <div>
                <label class="block text-gray-500 text-xs mb-2">
                  备注
                  <span
                    v-if="['use_trait', 'use_skill', 'other'].includes(forms.night_personal_action.actionType)"
                    class="text-red-400"
                  >（必填）</span>
                </label>
                <textarea v-model="forms.night_personal_action.notes" rows="3" :class="textareaClass"
                  :placeholder="forms.night_personal_action.actionType === 'other' ? '请详细描述你想执行的具体行动内容...' : '在此输入备注说明...'" />
              </div>
            </template>

            <!-- 公开审判 -->
            <template v-else-if="selectedType === 'public_trial'">
              <div>
                <label class="block text-gray-500 text-xs mb-2">审判对象</label>
                <select v-model="forms.public_trial.targetPlayerId" :class="selectClass">
                  <option value="">请选择玩家</option>
                  <option v-for="p in allPlayers.filter(x => x.id !== playerId)" :key="p.id" :value="p.id">
                    {{ p.name }}
                  </option>
                </select>
              </div>
              <textarea v-model="forms.public_trial.note" rows="2" :class="textareaClass" placeholder="审判事由（可选）" />
            </template>

            <!-- 施压 -->
            <template v-else-if="selectedType === 'pressure_ruler'">
              <div>
                <label class="block text-gray-500 text-xs mb-2">施压诉求</label>
                <select v-model="forms.pressure_ruler.demand" :class="selectClass">
                  <option value="">请选择</option>
                  <option v-for="d in PRESSURE_DEMAND_OPTIONS" :key="d.value" :value="d.value">{{ d.label }}</option>
                </select>
              </div>
              <textarea v-model="forms.pressure_ruler.note" rows="2" :class="textareaClass" placeholder="理由与说明（建议来自白天调查）" />
            </template>

            <!-- 公开宣传 -->
            <template v-else-if="selectedType === 'publicity'">
              <textarea
                v-model="forms.publicity.message"
                rows="3"
                :class="textareaClass"
                placeholder="公屏宣传内容..."
              />
            </template>

            <!-- 探索岛屿 -->
            <template v-else-if="selectedType === 'explore_island'">
              <div class="rounded-xl border border-indigo-500/20 bg-indigo-500/5 px-4 py-3 mb-4">
                <p class="text-indigo-300 text-sm">
                  投入物资增加探索值，探索值越高越可能发现高难度地点和珍贵物资。
                  最终探索值 = 投入探索值 + 1d6（1-6随机）。
                </p>
              </div>

              <div class="mb-4">
                <div class="flex items-center justify-between mb-2">
                  <span class="text-gray-400 text-sm">投入探索值</span>
                  <span class="text-white font-bold">
                    {{ totalInvestPoints }} / {{ MAX_EXPLORATION_POINTS }}
                  </span>
                </div>
                <div class="w-full bg-black/30 rounded-full h-3">
                  <div
                    class="bg-gradient-to-r from-indigo-500 to-purple-500 h-3 rounded-full transition-all duration-300"
                    :style="{ width: `${(totalInvestPoints / MAX_EXPLORATION_POINTS) * 100}%` }"
                  />
                </div>
                <p v-if="totalInvestPoints >= MAX_EXPLORATION_POINTS" class="text-amber-400 text-xs mt-1">
                  已达到探索值上限，继续投入不会增加探索值
                </p>
              </div>

              <div class="space-y-3">
                <div
                  v-for="item in EXPLORATION_ITEMS"
                  :key="item.itemId"
                  class="bg-black/20 border border-white/10 rounded-xl p-3"
                >
                  <div class="flex items-center justify-between mb-2">
                    <div class="flex items-center gap-2">
                      <span class="text-xl">{{ item.icon }}</span>
                      <div>
                        <span class="text-white text-sm font-medium">{{ item.name }}</span>
                        <span class="text-indigo-400 text-xs ml-2">+{{ item.bonus }} 探索值/个</span>
                      </div>
                    </div>
                    <span class="text-gray-400 text-xs">
                      拥有: {{ getItemQuantity(item.itemId) }}
                    </span>
                  </div>
                  <div class="flex items-center gap-3">
                    <button
                      type="button"
                      :disabled="getInvestQuantity(item.itemId) <= 0"
                      class="w-8 h-8 rounded-lg bg-white/5 border border-white/10 text-white text-sm font-medium disabled:opacity-30 disabled:cursor-not-allowed hover:bg-white/10 transition-colors"
                      @click="setInvestQuantity(item.itemId, getInvestQuantity(item.itemId) - 1)"
                    >
                      -
                    </button>
                    <input
                      type="number"
                      :value="getInvestQuantity(item.itemId)"
                      :min="0"
                      :max="getItemQuantity(item.itemId)"
                      class="flex-1 bg-black/30 border border-white/10 rounded-lg px-3 py-1.5 text-sm text-center text-white focus:outline-none focus:border-indigo-500/50"
                      @input="setInvestQuantity(item.itemId, parseInt($event.target.value) || 0)"
                    />
                    <button
                      type="button"
                      :disabled="getInvestQuantity(item.itemId) >= getItemQuantity(item.itemId)"
                      class="w-8 h-8 rounded-lg bg-white/5 border border-white/10 text-white text-sm font-medium disabled:opacity-30 disabled:cursor-not-allowed hover:bg-white/10 transition-colors"
                      @click="setInvestQuantity(item.itemId, getInvestQuantity(item.itemId) + 1)"
                    >
                      +
                    </button>
                    <button
                      type="button"
                      :disabled="getItemQuantity(item.itemId) === 0"
                      class="px-3 py-1 text-xs rounded-lg bg-indigo-500/20 border border-indigo-500/30 text-indigo-300 hover:bg-indigo-500/30 transition-colors disabled:opacity-30 disabled:cursor-not-allowed"
                      @click="setInvestQuantity(item.itemId, getItemQuantity(item.itemId))"
                    >
                      全部
                    </button>
                  </div>
                  <p v-if="getInvestQuantity(item.itemId) > 0" class="text-indigo-400 text-xs mt-2">
                    贡献探索值: +{{ getInvestQuantity(item.itemId) * item.bonus }}
                  </p>
                </div>
              </div>

              <div class="mt-4 p-3 rounded-xl bg-black/20 border border-white/10">
                <p class="text-gray-400 text-xs mb-1">📊 预估探索结果</p>
                <p class="text-gray-300 text-sm">
                  投入探索值: <span class="text-white font-medium">{{ totalInvestPoints }}</span>
                  + 骰子 1d6 (1-6)
                </p>
                <p class="text-gray-300 text-sm">
                  最终探索值范围:
                  <span class="text-amber-400 font-medium">{{ totalInvestPoints + 1 }} - {{ totalInvestPoints + 6 }}</span>
                </p>
              </div>
            </template>

            <!-- 其他 -->
            <template v-else-if="selectedType === 'other'">
              <div class="rounded-xl border border-gray-500/20 bg-gray-500/5 px-4 py-3">
                <p class="text-gray-300 text-sm">请在下方详细描述你想执行的具体行动内容，由DM判定是否成功及效果</p>
              </div>
              <div>
                <label class="block text-gray-500 text-xs mb-2">
                  行动描述
                  <span class="text-red-400">（必填）</span>
                </label>
                <textarea v-model="forms.other.note" rows="4" :class="textareaClass" placeholder="请详细描述你想执行的具体行动内容..." />
              </div>
            </template>

            <!-- 密谋 -->
            <template v-else-if="selectedType === 'conspiracy'">
              <div>
                <label class="block text-gray-500 text-xs mb-2">密谋类型</label>
                <select v-model="forms.conspiracy.conspiracySubtype" :class="selectClass">
                  <option value="">请选择</option>
                  <option v-for="c in conspiracyOptions" :key="c.value" :value="c.value">{{ c.label }}</option>
                </select>
              </div>
              <div v-if="showConspiracyLocation">
                <label class="block text-gray-500 text-xs mb-2">目标地点</label>
                <select v-model="forms.conspiracy.targetLocationId" :class="selectClass">
                  <option value="">请选择地点</option>
                  <option v-for="o in locationOptions" :key="o.value" :value="o.value">{{ o.label }}</option>
                </select>
              </div>
              <template v-if="showConspiracyTerrorTarget">
                <div>
                  <label class="block text-gray-500 text-xs mb-2">目标地点（可选）</label>
                  <select v-model="forms.conspiracy.targetLocationId" :class="selectClass">
                    <option value="">不选地点</option>
                    <option v-for="o in locationOptions" :key="'t-'+o.value" :value="o.value">{{ o.label }}</option>
                  </select>
                </div>
                <div>
                  <label class="block text-gray-500 text-xs mb-2">目标玩家（可选）</label>
                  <select v-model="forms.conspiracy.targetPlayerId" :class="selectClass">
                    <option value="">不选玩家</option>
                    <option v-for="p in allPlayers" :key="'tp-'+p.id" :value="p.id">{{ p.name }}</option>
                  </select>
                </div>
              </template>
              <div v-if="showRaidOutcome">
                <label class="block text-gray-500 text-xs mb-2">成功后意向</label>
                <select v-model="forms.conspiracy.raidOutcome" :class="selectClass">
                  <option value="">请选择</option>
                  <option v-for="r in RAID_OUTCOME_OPTIONS" :key="r.value" :value="r.value">{{ r.label }}</option>
                </select>
              </div>
              <div>
                <label class="block text-gray-500 text-xs mb-2">参与玩家</label>
                <div class="flex flex-wrap gap-2">
                  <button
                    v-for="p in allPlayers.filter(x => x.id !== playerId)"
                    :key="p.id"
                    type="button"
                    class="px-3 py-1.5 rounded-lg text-xs border transition-colors"
                    :class="forms.conspiracy.participantIds.includes(p.id)
                      ? 'bg-purple-500/20 border-purple-500/40 text-purple-200'
                      : 'bg-white/5 border-white/10 text-gray-400'"
                    @click="toggleParticipant(p.id)"
                  >
                    {{ p.name }}
                  </button>
                </div>
              </div>
              <textarea v-model="forms.conspiracy.note" rows="2" :class="textareaClass" placeholder="密谋说明（可选）" />
            </template>

            <div>
              <label class="block text-gray-500 text-xs mb-2">行动结果</label>
              <div
                class="min-h-[5rem] rounded-xl border border-white/10 bg-black/25 px-4 py-3 text-sm text-gray-400 whitespace-pre-wrap"
              >
                <!-- 探索详情卡片 -->
                <div v-if="explorationDetails && selectedType === 'explore_island'" class="mb-3">
                  <div class="flex flex-wrap gap-2 text-xs">
                    <span class="bg-indigo-500/20 text-indigo-300 px-2 py-1 rounded">
                      投入: {{ explorationDetails.investPoints }}
                    </span>
                    <span class="bg-amber-500/20 text-amber-300 px-2 py-1 rounded">
                      骰子: {{ explorationDetails.diceResult }}
                    </span>
                    <span class="bg-emerald-500/20 text-emerald-300 px-2 py-1 rounded">
                      总计: {{ explorationDetails.totalExplorationValue }}
                    </span>
                    <span v-if="explorationDetails.targetDifficulty" class="bg-purple-500/20 text-purple-300 px-2 py-1 rounded">
                      难度: {{ explorationDetails.targetDifficulty }}
                    </span>
                  </div>
                </div>
                {{ actionResult || '结果将在此显示' }}
              </div>
            </div>
          </fieldset>
        </div>

        <div v-if="submitMessage" class="flex justify-center mb-4">
          <div
            :class="[
              'px-5 py-2.5 rounded-xl text-sm',
              submitMessage.type === 'success' ? 'bg-green-500/20 text-green-400 border border-green-500/30' : 'bg-red-500/20 text-red-400 border border-red-500/30',
            ]"
          >
            {{ submitMessage.text }}
          </div>
        </div>

        <div class="flex justify-center pb-4">
          <button
            type="button"
            :disabled="!canSubmit"
            class="min-w-[200px] bg-gradient-to-r from-indigo-500 to-indigo-600 hover:from-indigo-600 hover:to-indigo-700 disabled:from-gray-600 disabled:to-gray-600 text-white px-8 py-3 rounded-xl text-sm font-medium"
            @click="submitAction"
          >
            {{ submitting ? '提交中...' : '提交夜晚行动' }}
          </button>
        </div>

        <div v-if="context?.history?.length" class="mt-8 space-y-3">
          <h3 class="text-white text-lg font-medium">已提交的夜晚行动</h3>
          <div
            v-for="item in context.history"
            :key="item.id"
            class="bg-gradient-to-br from-[#1a2332] to-[#0f1419] border border-white/10 rounded-xl p-4"
          >
            <div class="flex items-center justify-between mb-2">
              <span class="text-white text-sm font-medium">{{ item.actionTypeLabel }}</span>
              <span
                class="text-xs px-2 py-0.5 rounded-full"
                :class="item.status === 'pending' ? 'bg-amber-500/20 text-amber-400' : 'bg-green-500/20 text-green-400'"
              >
                {{ item.status === 'pending' ? '待结算' : '已结算' }}
              </span>
            </div>
            <div
            v-if="item.result"
            class="text-gray-400 text-xs whitespace-pre-wrap bg-black/20 rounded-lg p-3"
          >
            <!-- 探索详情 -->
            <div v-if="item.actionType === 'explore_island' && item.investPoints !== undefined" class="mb-2 flex flex-wrap gap-2">
              <span class="bg-indigo-500/20 text-indigo-300 px-2 py-1 rounded">
                投入: {{ item.investPoints }}
              </span>
              <span class="bg-amber-500/20 text-amber-300 px-2 py-1 rounded">
                骰子: {{ item.diceResult }}
              </span>
              <span class="bg-emerald-500/20 text-emerald-300 px-2 py-1 rounded">
                总计: {{ item.totalExplorationValue }}
              </span>
            </div>
            {{ item.result }}
          </div>
          </div>
        </div>
      </template>
  </div>
</template>
