<script setup>
import { ref, computed, onMounted, watch, reactive, nextTick } from 'vue'
import { actionAPI, playerAPI, locationAPI, warehouseAPI } from '@/utils/api.js'
import {
  formatPlayerActionResult,
  formatTransportNotesForDisplay,
  sanitizeNonNegativeInt,
} from '@/data/gameData.js'
import { useGameDayScope } from '@/composables/useGameDayScope.js'
import { parseTransportNotes, applyTransportQuantities } from '@/utils/actionFormHydration.js'
import {
  getTransportWeightPerUnit,
  resolveTransportMaxWeight,
  getTransportTotalWeight as sumTransportWeight,
  buildTransportNotes as encodeTransportNotes,
} from '@/utils/transportForm.js'
import StyledSelect from '@/components/StyledSelect.vue'

const playerId = localStorage.getItem('playerId') || ''
const {
  currentGameDay,
  currentPhase,
  viewGameDay: gameDay,
  dayOptions,
  phaseLabel,
  daytimeEditable,
  viewOnlyDaytimeReason,
  loadGameState,
  syncFromContext,
} = useGameDayScope()

const isHydrating = ref(false)

const actionData = reactive({
  1: { type: '', target: '', npc: '', notes: '', result: '' },
  2: { type: '', target: '', npc: '', notes: '', result: '' }
})

const submitting = ref(false)
const locations = ref([])
const players = ref([])
const playerInfo = ref(null)
const productionInfo = ref(null)
const submittedActions = ref([])
const showActionHelpModal = ref(false)
const submitMessage = ref(null)
const submitContext = ref({ isShelterLaborer: false, laborerMessage: '' })

const PENDING_RESULT_TEXT = '已提交，等待主持人确认。'

const LOCATION_QR_MAP = {
  '警察局': '警察站.jpg',
  '镇长厅': '镇长厅.jpg',
  '邮局': '邮局.jpg',
  '教堂': '教堂.jpg',
  '灯塔': '灯塔.jpg',
  '杂货店': '杂货店.jpg',
  '码头': '港口.jpg',
  '方舟': '方舟.png',
  '旅店': '旅店.jpg',
  '集市': '集市.jpg',
  '酒吧': '酒吧.jpg',
  '面包店': '面包店.jpg',
  '气象观测站': '气象观测站.jpg',
  '教室': '教室.jpg',
  '伐木营地': '伐木营地.jpg',
  '墓地': '墓地.jpg',
  '猎人小屋': '猎人小屋.jpg',
  '矿场': '矿场.jpg',
  '监狱': '监狱.jpg',
}

const isShelterLaborer = computed(() => Boolean(submitContext.value?.isShelterLaborer))

const submittedSlotSet = computed(
  () => new Set((submittedActions.value || []).map((a) => a.actionSlot).filter(Boolean))
)

function isSlotSubmitted(slot) {
  return submittedSlotSet.value.has(slot)
}

function isSlotEditable(slot) {
  return daytimeEditable.value && !isSlotSubmitted(slot)
}

const canSubmitAny = computed(
  () =>
    daytimeEditable.value &&
    [1, 2].some((s) => actionData[s].type && !isSlotSubmitted(s))
)

const warehouses = ref([])
const warehouseStock = ref([])
const transportMode = reactive({ 1: '', 2: '' })
const transportSource = reactive({ 1: '', 2: '' })
const transportDest = reactive({ 1: '', 2: '' })
const transportItems = reactive({ 1: [], 2: [] })
const overnightHere = reactive({ 1: false, 2: false })

const actionTypeOptions = computed(() => {
  const options = [
    { value: 'go_location', label: '前往地点' },
    { value: 'investigate_player', label: '调查玩家' },
    { value: 'use_trait', label: '使用特性' },
    { value: 'use_skill', label: '使用职业技能' },
    { value: 'hide', label: '隐藏' },
    { value: 'transport', label: '搬运' },
    { value: 'other', label: '其他' },
  ]
  if (productionInfo.value && productionInfo.value.canProduce) {
    options.splice(3, 0, { value: 'produce', label: '生产' })
  }
  return options
})

const actionHelpEntries = [
  { title: '前往地点', body: '选择一个地点进行探索，可以获得对应地点的信息（设施、防御值、NPC名单）。同时可以选择与地点NPC简单交互，NPC会根据你的阵营展现不同态度。' },
  { title: '调查玩家', body: '选择一名玩家，知晓对方的自由行动。无法调查拥有潜行状态的玩家。DM将统一结算调查结果。' },
  { title: '使用特性', body: '消耗行动点使用你的特性技能。必须在备注说明中详细描述所使用的特性名称及具体效果，DM将根据描述给予反馈。' },
  { title: '使用职业技能', body: '使用你的职业技能。必须在备注说明中详细描述所使用的职业技能及具体效果，DM将根据描述给予反馈。' },
  { title: '生产', body: '根据职业技能生产对应资源。需要DM结算后物资才会发放到背包中。' },
  { title: '搬运', body: '在仓库与个人背包间转移物资。须持有相关仓库钥匙（含避难所仓库）。个人→仓库在提交时即从背包扣除，入仓在主持人发布后生效。本页仅为行动档（占用行动槽1/2）：玩家↔仓库300kg；仓库↔仓库小镇500kg/海岛（含矿场仓库、避难所）300kg。免费搬运改在「快速交互」提交，占用快速行动额度（一天两条共用），玩家↔仓库上限50kg，仓库↔仓库小镇100/海岛50。装卸工职业额度×2（服务端）。' },
  { title: '隐藏', body: '隐藏自己：第二天不会被调查，也无法被私聊，无法成为统治者与密谋的行动目标。' },
  { title: '其他', body: '尝试执行系统未预先规划的行动。必须在描述中详细说明你想执行的具体行动内容，由DM判定是否成功及效果。' },
]

function getTargetOptions(actionType) {
  if (actionType === 'go_location') return Array.isArray(locations.value) ? locations.value.map(l => ({ value: l.id, label: `${l.name}（${l.area}）` })) : []
  if (actionType === 'investigate_player') {
    return Array.isArray(players.value)
      ? players.value.filter(p => p.id !== parseInt(playerId)).map(p => ({ value: p.id, label: p.name }))
      : []
  }
  return []
}

function getNpcOptions(targetId) {
  if (!targetId) return []
  const loc = Array.isArray(locations.value) ? locations.value.find(l => l.id === parseInt(targetId)) : null
  if (!loc || !Array.isArray(loc.npcs) || loc.npcs.length === 0) return []
  return loc.npcs.map(n => ({ value: n.id, label: `${n.name}（${n.job}）` }))
}

const warehouseOptions = computed(() => {
  return warehouses.value
    .filter((w) => w.accessible === true)
    .map((w) => ({ value: w.warehouseKey, label: w.warehouseName }))
})

const transportModeOptions = computed(() => [
  { value: 'warehouse_to_warehouse', label: '仓库 → 仓库' },
  { value: 'warehouse_to_player', label: '仓库 → 个人' },
  { value: 'player_to_warehouse', label: '个人 → 仓库（提交时扣除背包）' },
])

watch(() => actionData[1].type, () => {
  if (isHydrating.value) return
  actionData[1].target = ''
  actionData[1].npc = ''
  transportMode[1] = ''
  transportSource[1] = ''
  transportDest[1] = ''
  transportItems[1] = []
})
watch(() => actionData[2].type, () => {
  if (isHydrating.value) return
  actionData[2].target = ''
  actionData[2].npc = ''
  transportMode[2] = ''
  transportSource[2] = ''
  transportDest[2] = ''
  transportItems[2] = []
})
watch(() => actionData[1].target, () => { if (!isHydrating.value) actionData[1].npc = '' })
watch(() => actionData[2].target, () => { if (!isHydrating.value) actionData[2].npc = '' })

watch(() => transportMode[1], () => onTransportModeChange(1))
watch(() => transportMode[2], () => onTransportModeChange(2))
watch(() => transportSource[1], (nv) => {
  if (nv && ['warehouse_to_warehouse', 'warehouse_to_player'].includes(transportMode[1])) loadWarehouseStock(nv, 1)
})
watch(() => transportSource[2], (nv) => {
  if (nv && ['warehouse_to_warehouse', 'warehouse_to_player'].includes(transportMode[2])) loadWarehouseStock(nv, 2)
})

function onTransportModeChange(slot) {
  if (isHydrating.value) return
  transportSource[slot] = ''
  transportDest[slot] = ''
  transportItems[slot] = []
  const mode = transportMode[slot]
  if (mode === 'player_to_warehouse') {
    loadPlayerInventory(slot)
  }
}

async function loadPlayerInventory(slot) {
  try {
    const pid = parseInt(playerId)
    if (isNaN(pid)) return
    const list = await playerAPI.getItems(pid)
    transportItems[slot] = (Array.isArray(list) ? list : [])
      .filter((item) => (item.quantity || 0) > 0)
      .map((item) => ({
        itemType: item.type || item.itemType,
        itemId: item.id ?? item.itemId,
        name: item.name || '未知物品',
        unit: item.unit || '个',
        available: item.quantity,
        quantity: 0,
        weightPerUnit: getTransportWeightPerUnit(item.type || item.itemType, item.id ?? item.itemId),
      }))
  } catch (e) {
    console.error('加载个人背包失败:', e)
    transportItems[slot] = []
  }
}

async function loadWarehouseStock(warehouseKey, slot) {
  try {
    const userRole = localStorage.getItem('userRole') || ''
    const result = await warehouseAPI.getWarehouseStock(warehouseKey, parseInt(playerId), userRole)
    if (result && Array.isArray(result.items)) {
      warehouseStock.value = result.items
      transportItems[slot] = result.items.map(item => ({
        itemType: item.itemType,
        itemId: item.itemId,
        name: item.name,
        unit: item.unit,
        available: item.quantity,
        quantity: 0,
        weightPerUnit: getTransportWeightPerUnit(item.itemType, item.itemId)
      }))
    } else {
      transportItems[slot] = []
    }
  } catch (e) {
    console.error('加载仓库库存失败:', e)
    transportItems[slot] = []
  }
}

function getTransportTotalWeight(slot) {
  return sumTransportWeight(transportItems[slot])
}

function getTransportMaxWeight(slot) {
  return resolveTransportMaxWeight({
    mode: transportMode[slot],
    source: transportSource[slot],
    dest: transportDest[slot],
    free: false,
  })
}

function onTransportQuantityInput(item) {
  const max = item.available ?? 0
  item.quantity = sanitizeNonNegativeInt(item.quantity, max)
}

function buildTransportNotes(slot) {
  return encodeTransportNotes({
    mode: transportMode[slot],
    source: transportSource[slot],
    dest: transportDest[slot],
    items: transportItems[slot],
    tier: 'action',
  })
}

async function loadData() {
  try {
    const [locRes, playerRes] = await Promise.all([
      locationAPI ? locationAPI.getAll() : Promise.resolve([]),
      playerAPI ? playerAPI.getAll() : Promise.resolve([])
    ])
    locations.value = Array.isArray(locRes) ? locRes : []
    players.value = Array.isArray(playerRes) ? playerRes : []
  } catch (e) { console.error('加载数据失败:', e) }
}

async function loadPlayerData() {
  try {
    if (playerAPI && playerId) {
      const info = await playerAPI.get(playerId)
      playerInfo.value = info
    }
    if (actionAPI && playerId) {
      const prodInfo = await actionAPI.getProductionInfo(playerId)
      productionInfo.value = prodInfo
    }
  } catch (e) { console.error('加载玩家数据失败:', e) }
}

async function loadWarehouses() {
  try {
    const pid = parseInt(playerId)
    if (isNaN(pid)) return
    const userRole = localStorage.getItem('userRole') || ''
    const result = await warehouseAPI.getAccessibleWarehouses(pid, userRole)
    warehouses.value = Array.isArray(result) ? result : []
  } catch (e) { console.error('加载仓库列表失败:', e) }
}

async function loadSubmitContext() {
  try {
    const pid = parseInt(playerId)
    if (isNaN(pid)) return
    const ctx = await actionAPI.getSubmitContext(pid, gameDay.value)
    submitContext.value = ctx || { isShelterLaborer: false }
    syncFromContext(ctx)
  } catch (e) {
    console.error('加载行动上下文失败:', e)
    submitContext.value = { isShelterLaborer: false }
  }
}

async function loadSubmittedActions() {
  try {
    if (actionAPI && playerId) {
      const result = await actionAPI.getPlayerActions(playerId, gameDay.value)
      submittedActions.value = Array.isArray(result) ? result : []
      await hydrateFromSubmitted()
    }
  } catch (e) {
    console.error('加载已提交行动失败:', e)
    submittedActions.value = []
  }
}

function resetActionSlots() {
  for (const s of [1, 2]) {
    actionData[s].type = ''
    actionData[s].target = ''
    actionData[s].npc = ''
    actionData[s].notes = ''
    actionData[s].result = ''
    transportMode[s] = ''
    transportSource[s] = ''
    transportDest[s] = ''
    transportItems[s] = []
  }
}

async function hydrateFromSubmitted() {
  isHydrating.value = true
  resetActionSlots()
  const actions = submittedActions.value || []
  try {
    for (const action of actions) {
      const slot = action.actionSlot
      if (slot !== 1 && slot !== 2) continue
      const ad = actionData[slot]
      ad.type = action.actionType || ''
      ad.target = action.targetId != null ? String(action.targetId) : ''
      ad.npc = action.npcId != null ? String(action.npcId) : ''
      ad.result = displayActionResult(action)
      if (action.actionType === 'transport') {
        const parsed = parseTransportNotes(action.notes)
        transportMode[slot] = parsed.mode
        transportSource[slot] = parsed.source
        transportDest[slot] = parsed.dest
        if (parsed.mode === 'player_to_warehouse') {
          await loadPlayerInventory(slot)
        } else if (parsed.source) {
          await loadWarehouseStock(parsed.source, slot)
        }
        applyTransportQuantities(transportItems[slot], parsed.items)
      } else if (!['produce', 'hide'].includes(action.actionType)) {
        ad.notes = action.notes || ''
      }
    }
  } finally {
    await nextTick()
    isHydrating.value = false
  }
}

function validateAction(slot) {
  if (isSlotSubmitted(slot)) return true
  const ad = actionData[slot]
  if (!ad.type) return true
  if ((ad.type === 'go_location' || ad.type === 'investigate_player') && !ad.target) {
    alert(`行动${slot === 1 ? '一' : '二'}：请选择目标`)
    return false
  }
  if ((ad.type === 'use_trait' || ad.type === 'use_skill') && (!ad.notes || ad.notes.trim().length < 5)) {
    alert(`行动${slot === 1 ? '一' : '二'}：请在备注中详细描述所使用的技能/特性及效果`)
    return false
  }
  if (ad.type === 'other' && (!ad.notes || ad.notes.trim().length < 5)) {
    alert(`行动${slot === 1 ? '一' : '二'}：请详细描述你想执行的具体行动内容`)
    return false
  }
  if (ad.type === 'transport') {
    if (!transportMode[slot]) { alert(`行动${slot === 1 ? '一' : '二'}：请选择搬运模式`); return false }
    const mode = transportMode[slot]
    if (['warehouse_to_warehouse', 'warehouse_to_player'].includes(mode) && !transportSource[slot]) {
      alert(`行动${slot === 1 ? '一' : '二'}：请选择源仓库`); return false
    }
    if (['warehouse_to_warehouse', 'player_to_warehouse'].includes(mode) && !transportDest[slot]) {
      alert(`行动${slot === 1 ? '一' : '二'}：请选择目标仓库`); return false
    }
    const hasItems = Array.isArray(transportItems[slot]) && transportItems[slot].some(i => i.quantity > 0)
    if (!hasItems) { alert(`行动${slot === 1 ? '一' : '二'}：请至少选择一项搬运物资`); return false }
    const totalWeight = getTransportTotalWeight(slot)
    const maxWeight = getTransportMaxWeight(slot)
    if (totalWeight > maxWeight) { alert(`行动${slot === 1 ? '一' : '二'}：搬运总重量${totalWeight}kg超过上限${maxWeight}kg`); return false }
  }
  return true
}

const displayedLocationImages = computed(() => {
  const cards = []
  for (const s of [1, 2]) {
    const ad = actionData[s]
    if (!isSlotSubmitted(s) || ad.type !== 'go_location' || !ad.target) continue
    const loc = Array.isArray(locations.value)
      ? locations.value.find((l) => l.id === parseInt(ad.target))
      : null
    if (!loc?.name) continue
    const fileName = LOCATION_QR_MAP[loc.name]
    if (!fileName) continue
    cards.push({
      slot: s,
      locationName: loc.name,
      imageUrl: `/place/${fileName}`,
    })
  }
  return cards
})

async function submitActions() {
  if (!daytimeEditable.value) {
    alert(viewOnlyDaytimeReason.value || '当前不可提交')
    return
  }
  if (!actionData[1].type && !actionData[2].type) { alert('请至少选择一个行动'); return }
  if (!validateAction(1) || !validateAction(2)) return

  const pid = parseInt(playerId)
  if (isNaN(pid)) { alert('玩家信息异常，请重新登录'); return }

  submitting.value = true
  submitMessage.value = null
  try {
    let anySuccess = false
    for (const s of [1, 2]) {
      const ad = actionData[s]
      if (!ad.type || isSlotSubmitted(s)) continue
      let notes = ad.notes || ''
      if (ad.type === 'transport') {
        notes = buildTransportNotes(s)
      } else if (ad.type === 'go_location' && overnightHere[s]) {
        notes = (notes ? notes + '\n' : '') + '[overnight:1]'
      }
      const data = {
        playerId: pid,
        actionSlot: s,
        actionType: ad.type,
        targetId: ad.target ? parseInt(ad.target) : null,
        npcId: ad.npc ? parseInt(ad.npc) : null,
        notes,
        gameDay: parseInt(gameDay.value)
      }
      const res = await actionAPI.submitAction(data)
      if (res && res.success) {
        ad.result = res.data?.result || PENDING_RESULT_TEXT
        anySuccess = true
      } else {
        ad.result = '提交失败：' + (res?.message || '未知错误')
      }
    }
    if (anySuccess) {
      submitMessage.value = { type: 'success', text: '个人行动提交成功' }
    }
    await loadSubmittedActions()
  } catch (e) {
    console.error('提交行动失败:', e)
    submitMessage.value = { type: 'error', text: '提交失败，请重试' }
  } finally {
    submitting.value = false
    if (submitMessage.value) {
      setTimeout(() => { submitMessage.value = null }, 3000)
    }
  }
}

watch(gameDay, async () => {
  await loadSubmitContext()
  await loadSubmittedActions()
})

onMounted(async () => {
  await loadGameState()
  await Promise.all([loadData(), loadPlayerData(), loadWarehouses(), loadSubmitContext()])
  await loadSubmittedActions()
})

function displayActionResult(action) {
  if (!action) return ''
  if (action.status === 'pending' || action.resultPending) return PENDING_RESULT_TEXT
  if (action.result) {
    const formatted = formatPlayerActionResult(action.result)
    if (formatted) return formatted
  }
  if (action.actionType === 'transport' && action.notes?.includes('[mode:')) {
    return formatTransportNotesForDisplay(action.notes, {}, { actionSlot: action.actionSlot }) || ''
  }
  return ''
}
</script>

<template>
  <div class="action-submit">
    <div class="text-center mb-10">
      <h1 class="text-white text-2xl md:text-3xl font-semibold tracking-tight mb-2">个人行动提交</h1>
      <p class="text-slate-300 text-sm">选择你的两个个人行动并提交</p>
      <div class="mt-3 flex items-center justify-center gap-2">
        <label class="text-slate-200 text-sm">查看天数：</label>
        <select
          v-model.number="gameDay"
          class="bg-[#0b1220] border border-slate-500/50 rounded-lg px-3 py-1.5 text-sm text-slate-100 focus:outline-none focus:border-sky-400/70"
        >
          <option v-for="d in dayOptions" :key="d" :value="d">第 {{ d }} 天</option>
        </select>
        <span class="text-slate-400 text-sm">
          游戏第 {{ currentGameDay }} 天 · {{ phaseLabel }}
          <template v-if="gameDay === currentGameDay">（当前）</template>
        </span>
      </div>
    </div>

    <div
      v-if="viewOnlyDaytimeReason"
      class="mb-6 rounded-2xl border border-slate-500/40 bg-slate-500/10 px-5 py-3 text-center text-slate-300 text-sm"
    >
      {{ viewOnlyDaytimeReason }}
    </div>

    <div
      v-if="isShelterLaborer"
      class="mb-6 rounded-2xl border border-amber-500/40 bg-amber-500/10 px-5 py-4 text-center"
    >
      <p class="text-amber-200 text-sm font-medium">
        {{ submitContext.laborerMessage || '你今日被指定为避难所劳工，按规定当天不应提交个人行动；但不管怎么样，想要试试也是可以的。' }}
      </p>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-2 gap-5 mb-10">
      <div v-for="s in [1, 2]" :key="s"
        class="relative bg-gradient-to-br from-[#2a3a52] to-[#1a2638] border border-slate-400/25 rounded-3xl p-6 overflow-hidden">
        <div class="absolute top-0 right-0 w-48 h-48 bg-blue-500/5 rounded-full blur-3xl" />
        <div class="relative space-y-4">
          <div class="flex items-center gap-3">
            <span class="inline-flex items-center justify-center w-9 h-9 rounded-lg bg-white/10 border border-white/10 text-gray-300 text-sm font-medium">{{ s }}</span>
            <h2 class="text-white text-lg tracking-tight">行动{{ s === 1 ? '一' : '二' }}</h2>
          </div>

          <div>
            <div class="flex items-center gap-2 mb-2 ml-0.5">
              <label class="text-slate-300 text-sm">选择行动</label>
              <button type="button" class="inline-flex h-6 w-6 items-center justify-center rounded-full border border-slate-400/50 bg-slate-700/60 text-slate-100 hover:text-white hover:border-sky-400/60 hover:bg-sky-500/20 transition-colors text-xs font-semibold" @click="showActionHelpModal = true">?</button>
            </div>
            <select
              v-model="actionData[s].type"
              :disabled="!isSlotEditable(s)"
              class="action-field"
            >
              <option value="">请选择行动</option>
              <option v-for="opt in actionTypeOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
            </select>
          </div>

          <div v-if="actionData[s].type && actionData[s].type !== 'hide' && actionData[s].type !== 'produce' && actionData[s].type !== 'use_trait' && actionData[s].type !== 'use_skill' && actionData[s].type !== 'transport' && actionData[s].type !== 'other'">
            <label class="block text-slate-300 text-sm mb-2 ml-0.5">选择目标</label>
            <select
              v-model="actionData[s].target"
              :disabled="!isSlotEditable(s)"
              class="action-field"
            >
              <option value="">请选择目标</option>
              <option v-for="t in getTargetOptions(actionData[s].type)" :key="t.value" :value="t.value">{{ t.label }}</option>
            </select>
          </div>

            <div v-if="actionData[s].type === 'go_location' && actionData[s].target" class="space-y-3">
              <div v-if="getNpcOptions(actionData[s].target).length > 0">
                <label class="block text-slate-300 text-sm mb-2 ml-0.5">互动NPC（可选）</label>
                <select
                  v-model="actionData[s].npc"
                  :disabled="!isSlotEditable(s)"
                  class="action-field"
                >
                  <option value="">不互动</option>
                  <option v-for="n in getNpcOptions(actionData[s].target)" :key="n.value" :value="n.value">{{ n.label }}</option>
                </select>
              </div>
              <label class="flex items-start gap-2 text-sm text-slate-100 cursor-pointer">
                <input v-model="overnightHere[s]" type="checkbox" class="mt-1" :disabled="!isSlotEditable(s)" />
                <span>今晚在此过夜（默认在自己家；不勾选则不改变过夜地点）</span>
              </label>
            </div>

            <div v-if="actionData[s].type === 'produce' && productionInfo && productionInfo.canProduce">
              <div class="rounded-xl border border-cyan-500/20 bg-cyan-500/5 px-4 py-3">
                <p class="text-cyan-300 text-sm font-medium mb-1">{{ productionInfo.jobName }} - 生产</p>
                <p class="text-slate-200 text-sm leading-relaxed">{{ productionInfo.productionInfo?.description }}</p>
              </div>
            </div>

            <div v-if="actionData[s].type === 'use_trait'">
              <div class="rounded-xl border border-orange-500/20 bg-orange-500/5 px-4 py-3">
                <p class="text-orange-300 text-sm">请在下方备注中详细描述你使用的特性名称及具体效果，DM将根据描述给予反馈</p>
              </div>
            </div>

            <div v-if="actionData[s].type === 'use_skill'">
              <div class="rounded-xl border border-violet-500/20 bg-violet-500/5 px-4 py-3">
                <p class="text-violet-300 text-sm">请在下方备注中详细描述你使用的职业技能及具体效果，DM将根据描述给予反馈</p>
              </div>
            </div>

            <div v-if="actionData[s].type === 'hide'">
              <div class="rounded-xl border border-amber-500/20 bg-amber-500/5 px-4 py-3">
                <p class="text-amber-300 text-sm">隐藏自己，明天将无法被调查、私聊或成为统治者与密谋的行动目标</p>
              </div>
            </div>

            <div v-if="actionData[s].type === 'other'">
              <div class="rounded-xl border border-gray-500/20 bg-gray-500/5 px-4 py-3">
                <p class="text-gray-300 text-sm">请在下方描述中详细说明你想执行的具体行动内容，由DM判定是否成功及效果</p>
              </div>
            </div>

            <div v-if="actionData[s].type === 'transport'" class="space-y-3">
              <div class="rounded-xl border border-teal-500/20 bg-teal-500/5 px-4 py-3">
                <p class="text-teal-200 text-sm leading-relaxed">行动档：占用本行动槽。当前上限约 {{ getTransportMaxWeight(s) }}kg（装卸工×2由服务端结算）。免费搬运请到「快速交互」提交，与快速行动共用今日 2 条额度。</p>
              </div>
              <div>
                <label class="block text-slate-300 text-sm mb-2 ml-0.5">搬运模式</label>
                <StyledSelect
                  v-model="transportMode[s]"
                  tone="action"
                  :options="transportModeOptions"
                  placeholder="请选择模式"
                  :disabled="!isSlotEditable(s)"
                />
              </div>
              <div v-if="['warehouse_to_warehouse', 'warehouse_to_player'].includes(transportMode[s])" class="grid grid-cols-1 sm:grid-cols-2 gap-3">
                <div>
                  <label class="block text-slate-300 text-sm mb-2 ml-0.5">源仓库（需持有钥匙）</label>
                  <StyledSelect
                    v-model="transportSource[s]"
                    tone="action"
                    :options="warehouseOptions"
                    placeholder="选择仓库"
                  />
                </div>
                <div v-if="transportMode[s] === 'warehouse_to_warehouse'">
                  <label class="block text-slate-300 text-sm mb-2 ml-0.5">目标仓库（需持有钥匙）</label>
                  <StyledSelect
                    v-model="transportDest[s]"
                    tone="action"
                    :options="warehouseOptions.filter((o) => o.value !== transportSource[s])"
                    placeholder="选择仓库"
                  />
                </div>
              </div>
              <div v-if="transportMode[s] === 'player_to_warehouse'">
                <label class="block text-slate-300 text-sm mb-2 ml-0.5">目标仓库（需持有钥匙）</label>
                <StyledSelect
                  v-model="transportDest[s]"
                  tone="action"
                  :options="warehouseOptions"
                  placeholder="选择仓库"
                />
              </div>
              <div v-if="transportMode[s] && (
                (['warehouse_to_warehouse', 'warehouse_to_player'].includes(transportMode[s]) && transportSource[s]) ||
                transportMode[s] === 'player_to_warehouse'
              ) && Array.isArray(transportItems[s]) && transportItems[s].length > 0">
                <div class="flex items-center justify-between mb-2">
                  <label class="text-slate-300 text-sm">选择搬运物资</label>
                  <span class="text-sm" :class="getTransportTotalWeight(s) > getTransportMaxWeight(s) ? 'text-red-300' : 'text-slate-200'">
                    {{ getTransportTotalWeight(s) }}/{{ getTransportMaxWeight(s) }} kg
                  </span>
                </div>
                <div class="max-h-48 overflow-y-auto space-y-1.5 rounded-xl border border-white/10 bg-black/20 p-2">
                  <div v-for="item in transportItems[s]" :key="`${item.itemType}-${item.itemId}`"
                    class="flex items-center justify-between gap-2 px-2 py-1.5 rounded-lg hover:bg-white/5">
                    <div class="min-w-0 flex-1">
                      <span class="text-slate-100 text-sm">{{ item.name }}</span>
                      <span class="text-slate-300 text-sm ml-1">库存:{{ item.available }}{{ item.unit }}</span>
                    </div>
                    <input
                      v-model.number="item.quantity"
                      type="number"
                      min="0"
                      step="1"
                      inputmode="numeric"
                      :max="item.available"
                      class="w-16 bg-[#0b1220] border border-slate-500/50 rounded px-2 py-1 text-slate-100 text-sm text-center"
                      @input="onTransportQuantityInput(item)"
                    />
                    <span class="text-slate-300 text-sm w-12 text-right">{{ (item.quantity || 0) * item.weightPerUnit }}kg</span>
                  </div>
                </div>
              </div>
            </div>

            <div v-if="actionData[s].type !== 'transport'">
              <label class="block text-slate-300 text-sm mb-2 ml-0.5">
                备注说明
                <span v-if="actionData[s].type === 'use_trait' || actionData[s].type === 'use_skill' || actionData[s].type === 'other'" class="text-red-300">（必填）</span>
              </label>
              <textarea
                v-model="actionData[s].notes"
                rows="3"
                :disabled="!isSlotEditable(s)"
                :placeholder="actionData[s].type === 'use_trait' ? '请详细描述你使用的特性名称及具体效果...' : actionData[s].type === 'use_skill' ? '请详细描述你使用的职业技能及具体效果...' : actionData[s].type === 'other' ? '请详细描述你想执行的具体行动内容...' : '在此输入备注说明...'"
                class="action-field resize-none placeholder:text-slate-400"
              />
            </div>

            <div>
              <label class="block text-slate-300 text-sm mb-2 ml-0.5">行动结果</label>
              <div class="min-h-[5.5rem] rounded-xl border border-slate-500/40 bg-[#0b1220] px-4 py-3 text-sm text-slate-100 whitespace-pre-wrap leading-relaxed">
                {{ actionData[s].result || '提交后等待主持人确认，结果将在此显示' }}
              </div>
            </div>
          </div>
        </div>
      </div>

      <div v-if="submitMessage" class="flex justify-center mb-4">
        <div :class="['px-5 py-2.5 rounded-xl text-sm font-medium', submitMessage.type === 'success' ? 'bg-green-500/20 border border-green-500/30 text-green-400' : 'bg-red-500/20 border border-red-500/30 text-red-400']">
          {{ submitMessage.text }}
        </div>
      </div>

      <div class="flex justify-center pb-4">
        <button
          type="button"
          :disabled="submitting || !canSubmitAny"
          class="min-w-[200px] bg-gradient-to-r from-blue-500 to-blue-600 hover:from-blue-600 hover:to-blue-700 disabled:from-gray-600 disabled:to-gray-600 text-white px-8 py-3 rounded-xl text-sm font-medium shadow-lg shadow-blue-500/30 transition-all"
          @click="submitActions"
        >
          {{ submitting ? '提交中...' : '提交行动' }}
        </button>
      </div>

      <div
        v-if="displayedLocationImages.length"
        class="mt-6 grid gap-4"
        :class="displayedLocationImages.length > 1 ? 'grid-cols-2' : 'flex justify-center'"
      >
        <div
          v-for="card in displayedLocationImages"
          :key="card.slot"
          class="bg-gradient-to-br from-[#1a2332] to-[#0f1419] border border-white/10 rounded-2xl p-6 w-full"
          :class="displayedLocationImages.length === 1 ? 'max-w-sm' : ''"
        >
          <div class="text-center">
            <p class="text-slate-200 text-sm mb-3">
              行动{{ card.slot === 1 ? '一' : '二' }}：扫描查看
              <span class="text-sky-300">{{ card.locationName }}</span>
              详情
            </p>
            <div class="bg-white/5 rounded-xl p-4 flex justify-center">
              <img
                :src="card.imageUrl"
                :alt="card.locationName + '二维码'"
                class="max-w-[280px] max-h-[500px] w-full object-contain"
              />
            </div>
          </div>
        </div>
      </div>

      <div v-if="submittedActions && submittedActions.length > 0" class="mt-8">
        <h3 class="text-white text-lg font-medium mb-4">已提交的行动</h3>
        <div class="space-y-3">
          <div v-for="action in submittedActions" :key="action.id"
            class="bg-gradient-to-br from-[#2a3a52] to-[#1a2638] border border-slate-400/25 rounded-xl p-4">
            <div class="flex items-center justify-between mb-2">
              <div class="flex items-center gap-2">
                <span class="text-white text-sm font-medium">{{ action.actionSlot === 0 ? '免费搬运' : ('行动' + action.actionSlot) }}</span>
                <span class="text-xs px-2 py-0.5 rounded-full"
                  :class="{'bg-green-500/20 text-green-400': action.actionType === 'go_location', 'bg-yellow-500/20 text-yellow-400': action.actionType === 'investigate_player', 'bg-blue-500/20 text-blue-400': action.actionType === 'produce', 'bg-orange-500/20 text-orange-400': action.actionType === 'use_trait', 'bg-violet-500/20 text-violet-400': action.actionType === 'use_skill', 'bg-teal-500/20 text-teal-400': action.actionType === 'transport', 'bg-purple-500/20 text-purple-400': action.actionType === 'hide', 'bg-gray-500/20 text-gray-400': action.actionType === 'other'}">
                  {{ action.actionTypeLabel }}
                </span>
                <span v-if="action.targetName" class="text-slate-200 text-sm">→ {{ action.targetName }}</span>
              </div>
              <span class="text-xs px-2 py-0.5 rounded-full"
                :class="action.status === 'pending' ? 'bg-amber-500/20 text-amber-400' : 'bg-green-500/20 text-green-400'">
                {{ action.status === 'pending' ? '待确认' : '已确认' }}
              </span>
            </div>
            <div v-if="displayActionResult(action)" class="text-slate-100 text-sm leading-relaxed whitespace-pre-wrap bg-[#0b1220] border border-slate-500/30 rounded-lg p-3 mt-2">{{ displayActionResult(action) }}</div>
          </div>
        </div>
      </div>

      <Teleport to="body">
        <div v-if="showActionHelpModal" class="fixed inset-0 bg-black/75 flex items-center justify-center z-50 p-4" @click.self="showActionHelpModal = false">
          <div class="bg-[#161b22] border border-[#2a3444] rounded-[18px] max-w-lg w-full max-h-[85vh] overflow-hidden flex flex-col shadow-[0_24px_48px_-12px_rgba(0,0,0,0.55)]">
            <div class="flex items-start justify-between px-6 pt-6 pb-4 border-b border-[#252d3a] shrink-0">
              <div>
                <h2 class="text-white text-lg font-semibold tracking-tight">行动类型说明</h2>
                <p class="text-slate-300 text-sm mt-1.5">以下为各行动类型的详细规则</p>
              </div>
              <button type="button" class="text-slate-300 hover:text-white transition-colors shrink-0 p-1" @click="showActionHelpModal = false">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/></svg>
              </button>
            </div>
            <div class="overflow-y-auto px-6 py-5 space-y-3">
              <div v-for="(entry, idx) in actionHelpEntries" :key="entry.title" class="rounded-[10px] border border-[#253041] bg-[#1c2533] px-4 py-3.5">
                <p class="text-[#00d1ff] text-sm font-medium mb-2">{{ idx + 1 }}. {{ entry.title }}</p>
                <p class="text-slate-200 text-sm leading-relaxed">{{ entry.body }}</p>
              </div>
            </div>
            <div class="px-6 py-5 border-t border-[#252d3a] shrink-0 flex justify-center">
              <button type="button" class="min-w-[140px] px-10 py-2.5 rounded-full bg-[#303e55] hover:bg-[#3a4d68] text-white text-sm font-medium transition-colors" @click="showActionHelpModal = false">知道了</button>
            </div>
          </div>
        </div>
      </Teleport>
  </div>
</template>

<style scoped>
.action-submit {
  color-scheme: dark;
}
.action-field {
  @apply w-full appearance-none rounded-xl border px-4 py-3 pr-10 text-sm text-slate-100;
  color-scheme: dark;
  background: #0b1220;
  border-color: rgb(148 163 184 / 0.45);
}
.action-field option,
.action-field optgroup {
  background-color: #0b1220;
  color: #e2e8f0;
}
.action-field:focus {
  outline: none;
  border-color: rgb(56 189 248 / 0.7);
  box-shadow: 0 0 0 1px rgb(56 189 248 / 0.35);
}
.action-field:disabled {
  opacity: 0.65;
  cursor: not-allowed;
}
</style>
