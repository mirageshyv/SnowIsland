<script setup>
import { ref, computed, onMounted, watch, reactive } from 'vue'
import { actionAPI, playerAPI, locationAPI, warehouseAPI } from '@/utils/api.js'

const playerId = localStorage.getItem('playerId') || ''
const gameDay = ref(1)

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

const warehouses = ref([])
const warehouseStock = ref([])
const transportMode = reactive({ 1: '', 2: '' })
const transportSource = reactive({ 1: '', 2: '' })
const transportDest = reactive({ 1: '', 2: '' })
const transportItems = reactive({ 1: [], 2: [] })

const WEIGHT_MAP = {
  material: { 1: 1, 2: 1, 3: 0.5, 4: 1, 5: 1, 7: 1, 8: 1, 12: 50 },
  item: { 1: 0.5, 2: 0.3, 3: 0.5, 4: 0.2, 5: 2, 6: 3, 7: 1, 8: 1, 9: 0.1, 10: 1, 11: 0.5, 12: 0.5, 13: 0.1, 14: 1, 15: 0.2, 16: 0.2, 17: 0.3, 18: 0.5, 19: 0.1, 20: 0.1, 21: 0.1, 22: 0.1 },
  weapon: { 1: 2, 2: 3, 3: 1, 4: 1, 5: 1, 6: 2, 7: 2, 8: 3, 9: 2, 10: 5, 11: 0.5, 12: 1 },
  ammo: { 1: 0.1, 2: 0.1, 3: 0.1, 4: 0.1 }
}

function getWeightPerUnit(itemType, itemId) {
  return (WEIGHT_MAP[itemType] && WEIGHT_MAP[itemType][itemId]) || 1
}

const actionTypeOptions = computed(() => {
  const options = [
    { value: 'go_location', label: '前往地点' },
    { value: 'investigate_player', label: '调查玩家' },
    { value: 'use_trait', label: '使用特性' },
    { value: 'use_skill', label: '使用职业技能' },
    { value: 'hide', label: '隐藏' },
    { value: 'transport', label: '搬运' },
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
  { title: '搬运', body: '在仓库间转移物资或将仓库物资搬运到个人背包。需要持有源仓库的钥匙。仓库→仓库上限500kg，仓库→个人上限300kg。未标注重量的物资按1kg/单位计算。' },
  { title: '隐藏', body: '隐藏自己：第二天不会被调查，也无法被私聊，无法成为统治者与密谋的行动目标。' },
]

function getTargetOptions(actionType) {
  if (actionType === 'go_location') return Array.isArray(locations.value) ? locations.value.map(l => ({ value: l.id, label: `${l.name}（${l.area}）` })) : []
  if (actionType === 'investigate_player') return Array.isArray(players.value) ? players.value.filter(p => p.id !== parseInt(playerId)).map(p => ({ value: p.id, label: p.name })) : []
  return []
}

function getNpcOptions(targetId) {
  if (!targetId) return []
  const loc = Array.isArray(locations.value) ? locations.value.find(l => l.id === parseInt(targetId)) : null
  if (!loc || !Array.isArray(loc.npcs) || loc.npcs.length === 0) return []
  return loc.npcs.map(n => ({ value: n.id, label: `${n.name}（${n.job}）` }))
}

const warehouseOptions = computed(() => {
  return warehouses.value.map(w => ({ value: w.warehouseKey, label: w.warehouseName }))
})

watch(() => actionData[1].type, (nv) => { actionData[1].target = ''; actionData[1].npc = ''; transportMode[1] = ''; transportSource[1] = ''; transportDest[1] = ''; transportItems[1] = [] })
watch(() => actionData[2].type, (nv) => { actionData[2].target = ''; actionData[2].npc = ''; transportMode[2] = ''; transportSource[2] = ''; transportDest[2] = ''; transportItems[2] = [] })
watch(() => actionData[1].target, () => { actionData[1].npc = '' })
watch(() => actionData[2].target, () => { actionData[2].npc = '' })

watch(() => transportSource[1], (nv) => { if (nv) loadWarehouseStock(nv, 1) })
watch(() => transportSource[2], (nv) => { if (nv) loadWarehouseStock(nv, 2) })

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
        weightPerUnit: getWeightPerUnit(item.itemType, item.itemId)
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
  const items = transportItems[slot]
  if (!Array.isArray(items)) return 0
  return items.reduce((sum, item) => sum + (item.quantity || 0) * item.weightPerUnit, 0)
}

function getTransportMaxWeight(slot) {
  return transportMode[slot] === 'warehouse_to_warehouse' ? 500 : 300
}

function buildTransportNotes(slot) {
  const lines = []
  lines.push(`[mode:${transportMode[slot] || ''}]`)
  lines.push(`[source:${transportSource[slot] || ''}]`)
  if (transportMode[slot] === 'warehouse_to_warehouse' && transportDest[slot]) {
    lines.push(`[dest:${transportDest[slot]}]`)
  }
  const items = transportItems[slot]
  if (Array.isArray(items)) {
    for (const item of items) {
      if (item.quantity > 0) {
        lines.push(`[item:${item.itemType}|${item.itemId}|${item.quantity}|${item.weightPerUnit}]`)
      }
    }
  }
  return lines.join('\n')
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

async function loadSubmittedActions() {
  try {
    if (actionAPI && playerId) {
      const result = await actionAPI.getPlayerActions(playerId, gameDay.value)
      submittedActions.value = Array.isArray(result) ? result : []
    }
  } catch (e) {
    console.error('加载已提交行动失败:', e)
    submittedActions.value = []
  }
}

function validateAction(slot) {
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
  if (ad.type === 'transport') {
    if (!transportMode[slot]) { alert(`行动${slot === 1 ? '一' : '二'}：请选择搬运模式`); return false }
    if (!transportSource[slot]) { alert(`行动${slot === 1 ? '一' : '二'}：请选择源仓库`); return false }
    if (transportMode[slot] === 'warehouse_to_warehouse' && !transportDest[slot]) { alert(`行动${slot === 1 ? '一' : '二'}：请选择目标仓库`); return false }
    const hasItems = Array.isArray(transportItems[slot]) && transportItems[slot].some(i => i.quantity > 0)
    if (!hasItems) { alert(`行动${slot === 1 ? '一' : '二'}：请至少选择一项搬运物资`); return false }
    const totalWeight = getTransportTotalWeight(slot)
    const maxWeight = getTransportMaxWeight(slot)
    if (totalWeight > maxWeight) { alert(`行动${slot === 1 ? '一' : '二'}：搬运总重量${totalWeight}kg超过上限${maxWeight}kg`); return false }
  }
  return true
}

async function submitActions() {
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
      if (!ad.type) continue
      let notes = ad.notes || ''
      if (ad.type === 'transport') {
        notes = buildTransportNotes(s) + (ad.notes ? '\n' + ad.notes : '')
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
        ad.result = res.data?.result || '已提交'
        anySuccess = true
      } else {
        ad.result = '提交失败：' + (res?.message || '未知错误')
      }
    }
    if (anySuccess) {
      submitMessage.value = { type: 'success', text: '行动提交成功' }
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

onMounted(async () => {
  await Promise.all([loadData(), loadPlayerData(), loadWarehouses()])
  await loadSubmittedActions()
})
</script>

<template>
  <div class="min-h-screen bg-[#0a0e1a] py-8 px-4 md:px-8">
    <div class="max-w-6xl mx-auto">
      <div class="text-center mb-10">
        <h1 class="text-white text-2xl md:text-3xl font-semibold tracking-tight mb-2">角色行动</h1>
        <p class="text-gray-500 text-sm">选择你的两个行动并提交</p>
        <div class="mt-3 flex items-center justify-center gap-2">
          <label class="text-gray-400 text-sm">当前天数：</label>
          <select v-model="gameDay" @change="loadSubmittedActions" class="bg-black/30 border border-white/10 rounded-lg px-3 py-1 text-sm text-gray-200 focus:outline-none">
            <option :value="1">第1天</option>
            <option :value="2">第2天</option>
            <option :value="3">第3天</option>
          </select>
        </div>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-2 gap-5 mb-10">
        <div v-for="s in [1, 2]" :key="s"
          class="relative bg-gradient-to-br from-[#1a2332] to-[#0f1419] border border-white/10 rounded-3xl p-6 overflow-hidden">
          <div class="absolute top-0 right-0 w-48 h-48 bg-blue-500/5 rounded-full blur-3xl" />
          <div class="relative space-y-4">
            <div class="flex items-center gap-3">
              <span class="inline-flex items-center justify-center w-9 h-9 rounded-lg bg-white/10 border border-white/10 text-gray-300 text-sm font-medium">{{ s }}</span>
              <h2 class="text-white text-lg tracking-tight">行动{{ s === 1 ? '一' : '二' }}</h2>
            </div>

            <div>
              <div class="flex items-center gap-2 mb-2 ml-0.5">
                <label class="text-gray-500 text-xs">选择行动</label>
                <button type="button" class="inline-flex h-5 w-5 items-center justify-center rounded-full border border-white/20 bg-white/5 text-gray-400 hover:text-white hover:border-blue-500/40 hover:bg-blue-500/10 transition-colors text-xs font-semibold" @click="showActionHelpModal = true">?</button>
              </div>
              <select v-model="actionData[s].type"
                class="w-full appearance-none bg-white/5 border border-white/10 rounded-xl px-4 py-3 pr-10 text-gray-200 text-sm focus:outline-none focus:border-blue-500/50">
                <option value="">请选择行动</option>
                <option v-for="opt in actionTypeOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
              </select>
            </div>

            <div v-if="actionData[s].type && actionData[s].type !== 'hide' && actionData[s].type !== 'produce' && actionData[s].type !== 'use_trait' && actionData[s].type !== 'use_skill' && actionData[s].type !== 'transport'">
              <label class="block text-gray-500 text-xs mb-2 ml-0.5">选择目标</label>
              <select v-model="actionData[s].target"
                class="w-full appearance-none bg-white/5 border border-white/10 rounded-xl px-4 py-3 pr-10 text-gray-200 text-sm focus:outline-none focus:border-blue-500/50">
                <option value="">请选择目标</option>
                <option v-for="t in getTargetOptions(actionData[s].type)" :key="t.value" :value="t.value">{{ t.label }}</option>
              </select>
            </div>

            <div v-if="actionData[s].type === 'go_location' && actionData[s].target && getNpcOptions(actionData[s].target).length > 0">
              <label class="block text-gray-500 text-xs mb-2 ml-0.5">互动NPC（可选）</label>
              <select v-model="actionData[s].npc"
                class="w-full appearance-none bg-white/5 border border-white/10 rounded-xl px-4 py-3 pr-10 text-gray-200 text-sm focus:outline-none focus:border-blue-500/50">
                <option value="">不互动</option>
                <option v-for="n in getNpcOptions(actionData[s].target)" :key="n.value" :value="n.value">{{ n.label }}</option>
              </select>
            </div>

            <div v-if="actionData[s].type === 'produce' && productionInfo && productionInfo.canProduce">
              <div class="rounded-xl border border-cyan-500/20 bg-cyan-500/5 px-4 py-3">
                <p class="text-cyan-300 text-sm font-medium mb-1">{{ productionInfo.jobName }} - 生产</p>
                <p class="text-gray-400 text-xs">{{ productionInfo.productionInfo?.description }}</p>
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

            <div v-if="actionData[s].type === 'transport'" class="space-y-3">
              <div>
                <label class="block text-gray-500 text-xs mb-2 ml-0.5">搬运模式</label>
                <select v-model="transportMode[s]"
                  class="w-full appearance-none bg-white/5 border border-white/10 rounded-xl px-4 py-3 pr-10 text-gray-200 text-sm focus:outline-none focus:border-blue-500/50">
                  <option value="">请选择模式</option>
                  <option value="warehouse_to_warehouse">仓库 → 仓库（上限500kg）</option>
                  <option value="warehouse_to_player">仓库 → 个人（上限300kg）</option>
                </select>
              </div>
              <div class="grid grid-cols-2 gap-3">
                <div>
                  <label class="block text-gray-500 text-xs mb-2 ml-0.5">源仓库（需持有钥匙）</label>
                  <select v-model="transportSource[s]"
                    class="w-full appearance-none bg-white/5 border border-white/10 rounded-xl px-3 py-2.5 pr-10 text-gray-200 text-sm focus:outline-none focus:border-blue-500/50">
                    <option value="">选择仓库</option>
                    <option v-for="w in warehouseOptions" :key="w.value" :value="w.value">{{ w.label }}</option>
                  </select>
                </div>
                <div v-if="transportMode[s] === 'warehouse_to_warehouse'">
                  <label class="block text-gray-500 text-xs mb-2 ml-0.5">目标仓库</label>
                  <select v-model="transportDest[s]"
                    class="w-full appearance-none bg-white/5 border border-white/10 rounded-xl px-3 py-2.5 pr-10 text-gray-200 text-sm focus:outline-none focus:border-blue-500/50">
                    <option value="">选择仓库</option>
                    <option v-for="w in (warehouseOptions || []).filter(o => o.value !== transportSource[s])" :key="w.value" :value="w.value">{{ w.label }}</option>
                  </select>
                </div>
              </div>
              <div v-if="transportSource[s] && Array.isArray(transportItems[s]) && transportItems[s].length > 0">
                <div class="flex items-center justify-between mb-2">
                  <label class="text-gray-500 text-xs">选择搬运物资</label>
                  <span class="text-xs" :class="getTransportTotalWeight(s) > getTransportMaxWeight(s) ? 'text-red-400' : 'text-gray-400'">
                    {{ getTransportTotalWeight(s) }}/{{ getTransportMaxWeight(s) }} kg
                  </span>
                </div>
                <div class="max-h-48 overflow-y-auto space-y-1.5 rounded-xl border border-white/10 bg-black/20 p-2">
                  <div v-for="item in transportItems[s]" :key="`${item.itemType}-${item.itemId}`"
                    class="flex items-center justify-between gap-2 px-2 py-1.5 rounded-lg hover:bg-white/5">
                    <div class="min-w-0 flex-1">
                      <span class="text-gray-200 text-xs">{{ item.name }}</span>
                      <span class="text-gray-500 text-xs ml-1">库存:{{ item.available }}{{ item.unit }}</span>
                    </div>
                    <input v-model.number="item.quantity" type="number" min="0" :max="item.available"
                      class="w-16 bg-white/10 rounded px-2 py-1 text-gray-200 text-xs text-center" />
                    <span class="text-gray-500 text-xs w-12 text-right">{{ (item.quantity || 0) * item.weightPerUnit }}kg</span>
                  </div>
                </div>
              </div>
            </div>

            <div>
              <label class="block text-gray-500 text-xs mb-2 ml-0.5">
                备注说明
                <span v-if="actionData[s].type === 'use_trait' || actionData[s].type === 'use_skill'" class="text-red-400">（必填）</span>
              </label>
              <textarea v-model="actionData[s].notes" rows="3"
                :placeholder="actionData[s].type === 'use_trait' ? '请详细描述你使用的特性名称及具体效果...' : actionData[s].type === 'use_skill' ? '请详细描述你使用的职业技能及具体效果...' : '在此输入备注说明...'"
                class="w-full resize-none bg-white/5 border border-white/10 rounded-xl px-4 py-3 text-gray-200 text-sm placeholder:text-gray-600 focus:outline-none focus:border-blue-500/50" />
            </div>

            <div>
              <label class="block text-gray-500 text-xs mb-2 ml-0.5">行动结果</label>
              <div class="min-h-[5.5rem] rounded-xl border border-white/10 bg-black/25 px-4 py-3 text-sm text-gray-400 whitespace-pre-wrap">
                {{ actionData[s].result || '结果将在此显示' }}
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
        <button type="button" :disabled="submitting"
          class="min-w-[200px] bg-gradient-to-r from-blue-500 to-blue-600 hover:from-blue-600 hover:to-blue-700 disabled:from-gray-600 disabled:to-gray-600 text-white px-8 py-3 rounded-xl text-sm font-medium shadow-lg shadow-blue-500/30 transition-all"
          @click="submitActions">
          {{ submitting ? '提交中...' : '提交行动' }}
        </button>
      </div>

      <div v-if="submittedActions && submittedActions.length > 0" class="mt-8">
        <h3 class="text-white text-lg font-medium mb-4">已提交的行动</h3>
        <div class="space-y-3">
          <div v-for="action in submittedActions" :key="action.id"
            class="bg-gradient-to-br from-[#1a2332] to-[#0f1419] border border-white/10 rounded-xl p-4">
            <div class="flex items-center justify-between mb-2">
              <div class="flex items-center gap-2">
                <span class="text-white text-sm font-medium">行动{{ action.actionSlot }}</span>
                <span class="text-xs px-2 py-0.5 rounded-full"
                  :class="{'bg-green-500/20 text-green-400': action.actionType === 'go_location', 'bg-yellow-500/20 text-yellow-400': action.actionType === 'investigate_player', 'bg-blue-500/20 text-blue-400': action.actionType === 'produce', 'bg-orange-500/20 text-orange-400': action.actionType === 'use_trait', 'bg-violet-500/20 text-violet-400': action.actionType === 'use_skill', 'bg-teal-500/20 text-teal-400': action.actionType === 'transport', 'bg-purple-500/20 text-purple-400': action.actionType === 'hide'}">
                  {{ action.actionTypeLabel }}
                </span>
                <span v-if="action.targetName" class="text-gray-400 text-xs">→ {{ action.targetName }}</span>
              </div>
              <span class="text-xs px-2 py-0.5 rounded-full"
                :class="action.status === 'pending' ? 'bg-amber-500/20 text-amber-400' : 'bg-green-500/20 text-green-400'">
                {{ action.status === 'pending' ? '待反馈' : '已反馈' }}
              </span>
            </div>
            <div v-if="action.result" class="text-gray-400 text-xs whitespace-pre-wrap bg-black/20 rounded-lg p-3 mt-2">{{ action.result }}</div>
          </div>
        </div>
      </div>

      <Teleport to="body">
        <div v-if="showActionHelpModal" class="fixed inset-0 bg-black/75 flex items-center justify-center z-50 p-4" @click.self="showActionHelpModal = false">
          <div class="bg-[#161b22] border border-[#2a3444] rounded-[18px] max-w-lg w-full max-h-[85vh] overflow-hidden flex flex-col shadow-[0_24px_48px_-12px_rgba(0,0,0,0.55)]">
            <div class="flex items-start justify-between px-6 pt-6 pb-4 border-b border-[#252d3a] shrink-0">
              <div>
                <h2 class="text-white text-lg font-semibold tracking-tight">行动类型说明</h2>
                <p class="text-[#a0aab7] text-xs mt-1.5">以下为各行动类型的详细规则</p>
              </div>
              <button type="button" class="text-[#a0aab7] hover:text-white transition-colors shrink-0 p-1" @click="showActionHelpModal = false">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/></svg>
              </button>
            </div>
            <div class="overflow-y-auto px-6 py-5 space-y-3">
              <div v-for="(entry, idx) in actionHelpEntries" :key="entry.title" class="rounded-[10px] border border-[#253041] bg-[#1c2533] px-4 py-3.5">
                <p class="text-[#00d1ff] text-sm font-medium mb-2">{{ idx + 1 }}. {{ entry.title }}</p>
                <p class="text-[#a0aab7] text-sm leading-relaxed">{{ entry.body }}</p>
              </div>
            </div>
            <div class="px-6 py-5 border-t border-[#252d3a] shrink-0 flex justify-center">
              <button type="button" class="min-w-[140px] px-10 py-2.5 rounded-full bg-[#303e55] hover:bg-[#3a4d68] text-white text-sm font-medium transition-colors" @click="showActionHelpModal = false">知道了</button>
            </div>
          </div>
        </div>
      </Teleport>
    </div>
  </div>
</template>
