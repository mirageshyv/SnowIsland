<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import MaterialsPanel from './MaterialsPanel.vue'
import TradePanel from './TradePanel.vue'
import ArkProgressView from './ArkProgressView.vue'
import ShelterProgressView from './ShelterProgressView.vue'
import ActionSubmitView from './ActionSubmitView.vue'
import FactionActionSubmitView from './FactionActionSubmitView.vue'
import NightActionSubmitView from './NightActionSubmitView.vue'
import RebelMilestoneView from './RebelMilestoneView.vue'
import CatastrophePanel from '../components/CatastrophePanel.vue'
import WarehouseView from './WarehouseView.vue'
import { tradeAPI, playerAPI, milestoneAPI, gameStateAPI, playerConsumptionAPI } from '../utils/api.js'
import { sumPersonalFoodAndFuel, formatKgForDisplay } from '../utils/playerResources.js'
import { getMaterialImageUrlOrDefault } from '../data/gameData.js'

const foodIconUrl = getMaterialImageUrlOrDefault('material', 5)
const fuelIconUrl = getMaterialImageUrlOrDefault('material', 8)

const router = useRouter()
const username = localStorage.getItem('username') || ''
const playerId = parseInt(localStorage.getItem('playerId') || '1')
const activeTab = ref('info')

const tradePanelRef = ref(null)

const pendingTradesCount = ref(0)
const loading = ref(false)
const error = ref(null)
const playerInfo = ref(null)
const gameState = ref({ currentDay: 1, currentPhase: 'DAY' })
const playerItems = ref(null)
const personalResources = ref(null)
const showStatusPopover = ref(false)
const consumptionCtx = ref(null)
const consumptionForm = ref({ foodUnits: 0, woodKg: 0, fuelKg: 0 })
const consumptionSaving = ref(false)
const consumptionMessage = ref(null)

/** 仅冒险者可见「方舟建造进度」 */
const showArkTab = computed(() => playerInfo.value?.faction === '冒险者')
/** 仅统治者可见「统治者避难所」 */
const showShelterTab = computed(() => playerInfo.value?.faction === '统治者')
/** 仅反叛者可见「反叛者里程碑」 */
const showMilestoneTab = computed(() => playerInfo.value?.faction === '反叛者')
/** 仅天灾使者可见「天灾降临」 */
const showCatastropheTab = computed(() => playerInfo.value?.faction === '天灾使者')
/** Faction strategic actions — not available to civilians */
const showFactionActionsTab = computed(() => {
  const f = playerInfo.value?.faction
  return f && f !== '平民'
})
const showNightActionsTab = showFactionActionsTab

watch([showArkTab, showShelterTab, showMilestoneTab, showCatastropheTab, showFactionActionsTab, activeTab], () => {
  if (activeTab.value === 'ark' && !showArkTab.value) activeTab.value = 'info'
  if (activeTab.value === 'shelter' && !showShelterTab.value) activeTab.value = 'info'
  if (activeTab.value === 'milestone' && !showMilestoneTab.value) activeTab.value = 'info'
  if (activeTab.value === 'catastrophe' && !showCatastropheTab.value) activeTab.value = 'info'
  if (activeTab.value === 'factionActions' && !showFactionActionsTab.value) activeTab.value = 'info'
  if (activeTab.value === 'nightActions' && !showNightActionsTab.value) activeTab.value = 'info'
})

let pollTimer = null

const fetchPendingTradesCount = async () => {
  try {
    const result = await tradeAPI.getIncomingPendingCount(playerId)
    if (result != null && typeof result.count === 'number') {
      const pendingCount = result.count
      if (pendingCount !== pendingTradesCount.value) {
        pendingTradesCount.value = pendingCount
      }
    }
  } catch (error) {
    console.log('Failed to fetch pending trades:', error.message)
  }
}

const fetchPlayerInfo = async () => {
  loading.value = true
  error.value = null
  try {
    const [infoResult, itemsResult, resourcesResult, stateResult] = await Promise.all([
      playerAPI.getDetails(playerId),
      playerAPI.getItems(playerId),
      playerAPI.getResources(playerId),
      gameStateAPI.get().catch(() => null)
    ])
    
    if (infoResult && infoResult.success) {
      playerInfo.value = infoResult
    } else {
      error.value = infoResult?.message || '获取玩家信息失败'
    }
    
    if (itemsResult && Array.isArray(itemsResult)) {
      playerItems.value = itemsResult
    } else {
      console.log('获取玩家物品失败:', itemsResult?.message)
      playerItems.value = null
    }

    if (stateResult && stateResult.success !== false) {
      gameState.value = {
        currentDay: Number(stateResult.currentDay) || 1,
        currentPhase: stateResult.currentPhase === 'NIGHT' ? 'NIGHT' : 'DAY'
      }
    }

    if (resourcesResult && resourcesResult.success) {
      personalResources.value = resourcesResult
    } else {
      personalResources.value = null
    }

    await fetchConsumption()
  } catch (err) {
    error.value = '网络请求失败，请稍后重试'
    console.error('Failed to fetch player info:', err)
  } finally {
    loading.value = false
  }
}

const fetchGameState = async () => {
  try {
    const stateResult = await gameStateAPI.get()
    if (stateResult && stateResult.success !== false) {
      gameState.value = {
        currentDay: Number(stateResult.currentDay) || 1,
        currentPhase: stateResult.currentPhase === 'NIGHT' ? 'NIGHT' : 'DAY'
      }
    }
  } catch (e) {
    console.log('Failed to fetch game state:', e.message)
  }
}

const startPolling = () => {
  fetchPendingTradesCount()
  fetchGameState()
  pollTimer = setInterval(() => {
    fetchPendingTradesCount()
    fetchGameState()
  }, 5000)
}

const stopPolling = () => {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
}

const handleTradeTabClick = () => {
  activeTab.value = 'trade'
  fetchPendingTradesCount()
}

const negativeStatuses = computed(() => {
  const p = playerInfo.value
  if (!p) return []
  if (Array.isArray(p.statuses) && p.statuses.length) {
    return p.statuses.map((s) => ({
      name: s.name,
      severity: Math.min(5, Math.max(1, Number(s.severity) || 1)),
      description: s.description
    }))
  }
  const list = []
  if (p.isWeak) {
    list.push({
      name: '虚弱',
      severity: 2,
      description: '你的血肉在低语，诉说着某种迟缓的终结。生产的仪式已非你所能负担，更不必说与阴影中的敌手角力。（无法生产，格斗射击技能无效，可喝酒消除）'
    })
  }
  if (p.isOverworked) {
    list.push({
      name: '过劳',
      severity: 1,
      description: '骨骼在重复的磨损中发出暗哑的呻吟。你知晓那矿洞深处——那该死的避难所残骸——不可再踏足。否则，另一种结局会比夜幕更先降临。（无法执行生产行动，调查玩家和隐匿。在当天夜晚行动和第二天进行需要行动点的生产行动时，投1d6骰子，判定为1则死亡，可使用5瓶朗姆酒消除过劳）'
    })
  }
  const injured = Number(p.isInjured) || 0
  if (injured >= 3) {
    list.push({
      name: '死亡',
      severity: 5,
      description: '天灾的舌锋舔舐过这具躯壳。所有的门扉都已阖上，再无应答。（无额外效果）'
    })
  } else if (injured >= 2) {
    list.push({
      name: '重伤',
      severity: 4,
      description: '你的沙漏已近枯竭。你不知今夜是否便是最后一页。某个披着白袍的影或许能挽留你——又或许，还有另一个……更幽暗的。（无法行动，每夜阶段结束时若不进行急救，则死亡，急救消耗5医疗资源，可将重伤转为受伤）'
    })
  } else if (injured >= 1) {
    list.push({
      name: '受伤',
      severity: 3,
      description: '一道痕迹。它尚未决心吞噬你的命数——至少此刻，它还在犹豫。（你已经受伤了，再次受伤会恶化，无法生产，格斗技能无效）'
    })
  }
  return list
})

function resetConsumptionForm() {
  consumptionForm.value = { foodUnits: 0, woodKg: 0, fuelKg: 0 }
}

async function fetchConsumption() {
  const day = gameState.value.currentDay ?? 1
  try {
    const ctx = await playerConsumptionAPI.getContext(playerId, day)
    if (ctx?.success) {
      consumptionCtx.value = ctx
      resetConsumptionForm()
    }
  } catch (e) {
    console.log('Failed to fetch consumption:', e.message)
  }
}

const consumptionNeedsSubmit = computed(() => {
  const ctx = consumptionCtx.value
  if (!ctx) return false
  return !ctx.foodMet || !ctx.fuelMet
})

async function refreshPersonalInventory() {
  try {
    const [itemsResult, resourcesResult] = await Promise.all([
      playerAPI.getItems(playerId),
      playerAPI.getResources(playerId)
    ])
    if (itemsResult && Array.isArray(itemsResult)) {
      playerItems.value = itemsResult
    }
    if (resourcesResult?.success) {
      personalResources.value = resourcesResult
    }
  } catch (e) {
    console.log('Failed to refresh inventory:', e.message)
  }
}

async function submitConsumption() {
  consumptionSaving.value = true
  consumptionMessage.value = null
  try {
    const result = await playerConsumptionAPI.submit({
      playerId,
      gameDay: gameState.value.currentDay ?? 1,
      foodUnits: Math.max(0, Math.floor(Number(consumptionForm.value.foodUnits) || 0)),
      woodKg: Math.max(0, Math.floor(Number(consumptionForm.value.woodKg) || 0)),
      fuelKg: Math.max(0, Math.floor(Number(consumptionForm.value.fuelKg) || 0))
    })
    if (result?.success) {
      consumptionCtx.value = result
      resetConsumptionForm()
      consumptionMessage.value = { type: 'success', text: result.message || '消耗已记录' }
      await refreshPersonalInventory()
    } else {
      consumptionMessage.value = { type: 'error', text: result?.message || '提交失败' }
    }
  } catch (e) {
    consumptionMessage.value = { type: 'error', text: '网络请求失败' }
  } finally {
    consumptionSaving.value = false
  }
}

const playerResources = computed(() => {
  const api = personalResources.value
  if (api?.success) {
    return {
      food: formatKgForDisplay(api.foodKg ?? 0),
      fuel: formatKgForDisplay(api.fuelKg ?? 0),
      wood: formatKgForDisplay(api.woodKg ?? 0),
      fuelLiters: api.fuelLiters ?? 0
    }
  }
  if (!playerItems.value) {
    return { food: 0, fuel: 0, wood: 0, fuelLiters: 0 }
  }
  const items = Array.isArray(playerItems.value) ? playerItems.value : []
  const totals = sumPersonalFoodAndFuel(items)
  return {
    food: formatKgForDisplay(totals.food),
    fuel: formatKgForDisplay(totals.fuel),
    wood: formatKgForDisplay(totals.wood),
    fuelLiters: 0
  }
})

const dashboardProfile = computed(() => {
  const p = playerInfo.value
  if (!p) return null

  const formatMultiSkillParagraphs = (text) => {
    if (!text || typeof text !== 'string') return text
    // Insert newline after a full stop when the next part looks like "技能名："
    // e.g. "格斗：...。急救：..." -> "格斗：...。\n急救：..."
    return text.replace(/。(?=[^。\n]{1,6}：)/g, '。\n')
  }

  const resources = playerResources.value

  const phaseLabel = gameState.value.currentPhase === 'NIGHT' ? '夜晚' : '白天'

  const dummy = {
    professionalSkillDescription:
      formatMultiSkillParagraphs(p.jobDescription) ||
      p.jobSkills ||
      '拥有深厚的专业知识与实践经验，能够在关键时刻提供高质量的解决方案。（占位文案，可由后端返回替换）',
    traitDescription:
      p.personalSkillFunction ||
      '经历过艰难困苦磨练出的意志与韧性，在逆境下仍能保持行动能力。（占位文案，可由后端返回替换）'
  }

  return {
    name: p.name,
    faction: p.faction || '未设定阵营',
    profession: p.job || '未设定职业',
    negativeStatus: negativeStatuses.value,
    currentDay: gameState.value.currentDay ?? 1,
    currentPhase: phaseLabel,
    foodQuantity: resources.food,
    fuelQuantity: resources.fuel,
    woodFuelQuantity: resources.wood,
    professionalSkill: {
      name: p.jobSkills ? `${p.professionSkill || '职业技能'}` : (p.job ? '职业技能' : '职业技能'),
      description: dummy.professionalSkillDescription
    },
    trait: {
      name: p.personalSkill || '特性',
      description: dummy.traitDescription
    }
  }
})

const handleLogout = () => {
  localStorage.removeItem('userRole')
  localStorage.removeItem('username')
  localStorage.removeItem('playerId')
  router.push('/')
}

watch(activeTab, (tab) => {
  if (tab === 'info' && playerInfo.value) {
    fetchPlayerInfo()
  }
})

onMounted(() => {
  console.log('Player page mounted, playerId:', playerId)
  fetchPlayerInfo()
  startPolling()
})

onUnmounted(() => {
  stopPolling()
})
</script>

<template>
  <!-- h-screen + overflow-hidden：侧栏固定；仅右侧 main 纵向滚动 -->
  <div class="flex h-screen max-h-[100dvh] overflow-hidden bg-[#0a0e1a]">
    <aside class="flex h-full w-64 shrink-0 flex-col border-r border-[#1f2937] bg-[#0f1419]">
      <div class="shrink-0 border-b border-[#1f2937] p-6">
        <h2 class="text-white tracking-tight text-lg">玩家中心</h2>
      </div>

      <nav class="min-h-0 flex-1 overflow-y-auto p-4">
        <button
          type="button"
          class="w-full text-left px-4 py-3 rounded-xl mb-2 transition-colors font-medium"
          :class="activeTab === 'info' ? 'bg-[#2d4263] text-white' : 'text-gray-400 hover:bg-[#151b2e] hover:text-gray-300'"
          @click="activeTab = 'info'"
        >
          个人信息
        </button>
        <button
          type="button"
          class="w-full text-left px-4 py-3 rounded-xl mb-2 transition-colors font-medium"
          :class="activeTab === 'actions' ? 'bg-[#2d4263] text-white' : 'text-gray-400 hover:bg-[#151b2e] hover:text-gray-300'"
          @click="activeTab = 'actions'"
        >
          个人行动提交
        </button>
        <button
          v-if="showFactionActionsTab"
          type="button"
          class="w-full text-left px-4 py-3 rounded-xl mb-2 transition-colors font-medium"
          :class="activeTab === 'factionActions' ? 'bg-[#2d4263] text-white' : 'text-gray-400 hover:bg-[#151b2e] hover:text-gray-300'"
          @click="activeTab = 'factionActions'"
        >
          阵营行动提交
        </button>
        <button
          v-if="showNightActionsTab"
          type="button"
          class="w-full text-left px-4 py-3 rounded-xl mb-2 transition-colors font-medium"
          :class="activeTab === 'nightActions' ? 'bg-[#2d4263] text-white' : 'text-gray-400 hover:bg-[#151b2e] hover:text-gray-300'"
          @click="activeTab = 'nightActions'"
        >
          夜晚行动提交
        </button>
        <button
          type="button"
          class="w-full text-left px-4 py-3 rounded-xl mb-2 transition-colors font-medium"
          :class="activeTab === 'materials' ? 'bg-[#2d4263] text-white' : 'text-gray-400 hover:bg-[#151b2e] hover:text-gray-300'"
          @click="activeTab = 'materials'"
        >
          物资管理
        </button>
        <button
          v-if="showArkTab"
          type="button"
          class="w-full text-left px-4 py-3 rounded-xl mb-2 transition-colors font-medium"
          :class="activeTab === 'ark' ? 'bg-[#2d4263] text-white' : 'text-gray-400 hover:bg-[#151b2e] hover:text-gray-300'"
          @click="activeTab = 'ark'"
        >
          方舟建造进度
        </button>
        <button
          v-if="showShelterTab"
          type="button"
          class="w-full text-left px-4 py-3 rounded-xl mb-2 transition-colors font-medium"
          :class="activeTab === 'shelter' ? 'bg-[#2d4263] text-white' : 'text-gray-400 hover:bg-[#151b2e] hover:text-gray-300'"
          @click="activeTab = 'shelter'"
        >
          统治者避难所
        </button>
        <button
          v-if="showMilestoneTab"
          type="button"
          class="w-full text-left px-4 py-3 rounded-xl mb-2 transition-colors font-medium"
          :class="activeTab === 'milestone' ? 'bg-[#2d4263] text-white' : 'text-gray-400 hover:bg-[#151b2e] hover:text-gray-300'"
          @click="activeTab = 'milestone'"
        >
          反叛者里程碑
        </button>
        <button
          v-if="showCatastropheTab"
          type="button"
          class="w-full text-left px-4 py-3 rounded-xl mb-2 transition-colors font-medium"
          :class="activeTab === 'catastrophe' ? 'bg-[#2d4263] text-white' : 'text-gray-400 hover:bg-[#151b2e] hover:text-gray-300'"
          @click="activeTab = 'catastrophe'"
        >
          天灾降临
        </button>
        <button
          type="button"
          class="w-full text-left px-4 py-3 rounded-xl mb-2 transition-colors font-medium"
          :class="activeTab === 'warehouse' ? 'bg-[#2d4263] text-white' : 'text-gray-400 hover:bg-[#151b2e] hover:text-gray-300'"
          @click="activeTab = 'warehouse'"
        >
          📦 仓库管理
        </button>
        <button
          type="button"
          class="w-full text-left px-4 py-3 rounded-xl mb-2 transition-colors font-medium relative group"
          :class="activeTab === 'trade' ? 'bg-[#2d4263] text-white' : 'text-gray-400 hover:bg-[#151b2e] hover:text-gray-300'"
          @click="handleTradeTabClick"
        >
          <span class="flex items-center justify-between">
            <span class="flex items-center gap-2">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7h12m0 0l-4-4m4 4l-4 4m0 6H4m0 0l4 4m-4-4l4-4" />
              </svg>
              <span>交易管理</span>
            </span>
            <span
              v-if="pendingTradesCount > 0"
              class="bg-gradient-to-r from-red-600 to-red-500 text-white text-xs font-bold px-2.5 py-1 rounded-full shadow-lg shadow-red-500/50 animate-pulse min-w-[20px] text-center"
            >
              {{ pendingTradesCount > 9 ? '9+' : pendingTradesCount }}
            </span>
          </span>
        </button>
      </nav>

      <div class="shrink-0 border-t border-[#1f2937] p-4">
        <div class="flex items-center justify-between">
          <span class="text-gray-400 text-sm">{{ username }}</span>
          <button
            type="button"
            class="text-gray-500 hover:text-white text-sm transition-colors"
            @click="handleLogout"
          >
            退出
          </button>
        </div>
      </div>
    </aside>

    <main class="min-h-0 min-w-0 flex-1 overflow-y-auto p-8">
      <div
        v-if="activeTab === 'info'"
        class="-m-8 min-h-full bg-gradient-to-br from-slate-950 via-slate-900 to-slate-950 p-8"
      >

        <div v-if="loading" class="flex items-center justify-center py-20">
          <div class="text-center">
            <div class="inline-block animate-spin rounded-full h-12 w-12 border-4 border-blue-500 border-t-transparent mb-4"></div>
            <p class="text-gray-400">加载中...</p>
          </div>
        </div>

        <div v-else-if="error" class="bg-red-500/10 border border-red-500/30 rounded-xl p-6 max-w-3xl">
          <div class="flex items-center gap-3 mb-4">
            <svg class="w-6 h-6 text-red-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            <p class="text-red-400 font-medium">{{ error }}</p>
          </div>
          <button
            @click="fetchPlayerInfo"
            class="px-4 py-2 text-sm text-white bg-red-600 hover:bg-red-700 rounded-lg transition-colors"
          >
            重试
          </button>
        </div>

        <div v-else-if="playerInfo">
          <div v-if="dashboardProfile" class="min-h-full">

                <!-- Header -->
                <div class="mb-10 relative" style="z-index: 10;">
                  <div class="flex items-end justify-between mb-6 gap-6">
                    <div class="min-w-0">
                      <div class="flex flex-wrap items-center gap-3">
                        <div class="w-full">
                          <div class="text-slate-500 text-xs md:text-sm mb-1 tracking-wider">
                            玩家档案
                          </div>
                        </div>
                        <h2 class="text-4xl md:text-5xl text-white font-bold tracking-tight truncate">
                          {{ dashboardProfile.name }}
                        </h2>

                        <div v-if="dashboardProfile.negativeStatus.length > 0" class="relative inline-block">
                          <button
                            type="button"
                            class="px-3 py-1.5 bg-red-900/40 border border-red-700/50 rounded-lg cursor-pointer transition-all duration-200 hover:bg-red-900/60"
                            @mouseenter="showStatusPopover = true"
                            @mouseleave="showStatusPopover = false"
                            @focus="showStatusPopover = true"
                            @blur="showStatusPopover = false"
                          >
                            <span class="text-red-300 text-xs md:text-sm">
                              负面状态：{{ dashboardProfile.negativeStatus.map(s => s.name).join('、') }}
                            </span>
                          </button>

                          <Transition name="fade-pop">
                            <div
                              v-show="showStatusPopover"
                              class="absolute top-full left-0 mt-2 w-[22rem] md:w-96 bg-slate-900/95 border border-red-700/50 rounded-xl p-4 shadow-2xl will-change-transform transform-gpu"
                              style="z-index: 9999;"
                              @mouseenter="showStatusPopover = true"
                              @mouseleave="showStatusPopover = false"
                            >
                              <h3 class="text-white font-semibold mb-3">负面状态详情</h3>
                              <div class="space-y-3">
                                <div
                                  v-for="(status, idx) in dashboardProfile.negativeStatus"
                                  :key="`${status.name}-${idx}`"
                                  class="bg-slate-800/60 rounded-lg p-3 border border-red-900/30"
                                >
                                  <div class="flex items-center justify-between mb-2">
                                    <span class="text-red-300 font-medium">{{ status.name }}</span>
                                    <div class="flex gap-1">
                                      <span
                                        v-for="i in 3"
                                        :key="i"
                                        :class="['w-2 h-4 rounded-full', i <= status.severity ? 'bg-red-500' : 'bg-slate-600']"
                                      />
                                    </div>
                                  </div>
                                  <p class="text-slate-300 text-sm leading-relaxed">{{ status.description }}</p>
                                </div>
                              </div>
                            </div>
                          </Transition>
                        </div>

                        <div v-else class="relative inline-block">
                          <button
                            type="button"
                            class="px-3 py-1.5 bg-emerald-900/40 border border-emerald-700/50 rounded-lg cursor-pointer transition-all duration-200 hover:bg-emerald-900/60"
                            @mouseenter="showStatusPopover = true"
                            @mouseleave="showStatusPopover = false"
                            @focus="showStatusPopover = true"
                            @blur="showStatusPopover = false"
                          >
                            <span class="text-emerald-300 text-xs md:text-sm">
                              状态：健康
                            </span>
                          </button>

                          <Transition name="fade-pop">
                            <div
                              v-show="showStatusPopover"
                              class="absolute top-full left-0 mt-2 w-[22rem] md:w-96 bg-slate-900/95 border border-emerald-700/50 rounded-xl p-4 shadow-2xl will-change-transform transform-gpu"
                              style="z-index: 9999;"
                              @mouseenter="showStatusPopover = true"
                              @mouseleave="showStatusPopover = false"
                            >
                              <h3 class="text-white font-semibold mb-3">状态详情</h3>
                              <div class="bg-slate-800/60 rounded-lg p-3 border border-emerald-900/30">
                                <div class="flex items-center justify-between mb-2">
                                  <span class="text-emerald-300 font-medium">健康</span>
                                </div>
                                <p class="text-slate-300 text-sm leading-relaxed">健康？不，这只是"尚未被选定"。就像祭坛上那只还未被指尖指向的羔羊，它的平静不值得羡慕。心跳均匀如无人敲击的钟——这种状态不会长久，但它此刻真实得可疑（无负面效果）。</p>
                              </div>
                            </div>
                          </Transition>
                        </div>
                      </div>
                    </div>

                    <div class="text-right shrink-0">
                      <div class="flex items-center justify-end gap-2 mb-2">
                        <button
                          type="button"
                          class="px-3 py-1.5 text-xs md:text-sm text-gray-200 hover:text-white bg-white/5 hover:bg-white/10 rounded-lg transition-colors flex items-center gap-2 will-change-transform transform-gpu"
                          @click="fetchPlayerInfo"
                        >
                          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
                          </svg>
                          刷新
                        </button>
                      </div>

                      <div class="text-slate-500 text-xs md:text-sm mb-1">游戏进度</div>
                      <div class="text-2xl md:text-3xl text-cyan-300 font-bold">第 {{ dashboardProfile.currentDay }} 天</div>
                      <div v-if="dashboardProfile.currentPhase" class="text-slate-500 text-xs mt-1">{{ dashboardProfile.currentPhase }}</div>
                    </div>
                  </div>
                  <div class="h-1 bg-gradient-to-r from-cyan-500 via-blue-500 to-transparent"></div>
                </div>

                <!-- Main Grid -->
                <div class="grid grid-cols-12 gap-6 md:gap-8">
                  <!-- Left Column -->
                  <div class="col-span-12 lg:col-span-5 space-y-6 md:space-y-8">
                    <!-- Resources -->
                    <div class="relative group">
                      <div class="absolute inset-0 bg-gradient-to-br from-amber-500/10 to-yellow-500/10 rounded-2xl blur-xl transition-all duration-300 ease-out group-hover:blur-2xl"></div>
                      <div class="relative bg-slate-900/80 border border-slate-700/50 rounded-2xl p-6 transition-all duration-200 ease-out hover:border-amber-500/50 will-change-transform transform-gpu">
                        <div class="text-slate-400 text-xs md:text-sm mb-4 tracking-wider">个人资源</div>
                        <div class="grid grid-cols-2 gap-4">
                          <div class="text-center transition-transform duration-200 ease-out hover:scale-[1.02] will-change-transform transform-gpu">
                            <div class="w-14 h-14 bg-amber-500/20 rounded-lg flex items-center justify-center mx-auto mb-3">
                              <img :src="foodIconUrl" alt="" class="w-10 h-10 object-contain" aria-hidden="true" />
                            </div>
                            <div class="text-slate-400 text-xs mb-1">食物</div>
                            <div class="text-amber-300 text-2xl font-bold">{{ dashboardProfile.foodQuantity }}</div>
                            <div class="text-slate-500 text-xs mt-1">千克</div>
                          </div>
                          <div class="text-center transition-transform duration-200 ease-out hover:scale-[1.02] will-change-transform transform-gpu">
                            <div class="w-14 h-14 bg-yellow-500/20 rounded-lg flex items-center justify-center mx-auto mb-3">
                              <img :src="fuelIconUrl" alt="" class="w-10 h-10 object-contain" aria-hidden="true" />
                            </div>
                            <div class="text-slate-400 text-xs mb-1">燃料</div>
                            <div class="text-yellow-300 text-2xl font-bold">{{ dashboardProfile.fuelQuantity }}</div>
                            <div class="text-slate-500 text-xs mt-1">千克</div>
                            <div
                              v-if="dashboardProfile.woodFuelQuantity > 0"
                              class="text-amber-400/90 text-xs mt-1 font-medium"
                            >
                              + {{ dashboardProfile.woodFuelQuantity }} 千克 木材
                            </div>
                          </div>
                        </div>

                        <div v-if="consumptionCtx" class="mt-6 pt-5 border-t border-slate-700/50 space-y-4">
                          <div class="text-slate-400 text-xs tracking-wider">进食与生活取暖（第 {{ consumptionCtx.gameDay }} 天）</div>
                          <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
                            <div class="rounded-xl bg-slate-800/50 p-4 border border-slate-700/40">
                              <div class="text-slate-400 text-xs mb-2">进食</div>
                              <div class="text-amber-300 text-lg font-bold tabular-nums">
                                {{ consumptionCtx.consumedFoodUnits }} / {{ consumptionCtx.requiredFoodUnits }} 单位
                              </div>
                              <p v-if="consumptionCtx.foodMet" class="text-emerald-400 text-sm font-medium mt-3">已满足</p>
                              <template v-else>
                                <label class="block text-slate-500 text-xs mt-3 mb-1">
                                  本次提交食物（单位，库存 {{ consumptionCtx.availableFoodUnits }}，还需 {{ consumptionCtx.remainingFoodUnits ?? (consumptionCtx.requiredFoodUnits - consumptionCtx.consumedFoodUnits) }}）
                                </label>
                                <input
                                  v-model.number="consumptionForm.foodUnits"
                                  type="number"
                                  min="0"
                                  :max="consumptionCtx.remainingFoodUnits ?? (consumptionCtx.requiredFoodUnits - consumptionCtx.consumedFoodUnits)"
                                  step="1"
                                  class="w-full bg-slate-900/80 border border-slate-600 rounded-lg px-3 py-2 text-white text-sm tabular-nums"
                                />
                              </template>
                            </div>
                            <div class="rounded-xl bg-slate-800/50 p-4 border border-slate-700/40">
                              <div class="text-slate-400 text-xs mb-2">生活取暖</div>
                              <div class="text-yellow-300 text-lg font-bold tabular-nums">
                                {{ consumptionCtx.consumedFuelKg }} / {{ consumptionCtx.requiredFuelKg }} 千克
                              </div>
                              <p v-if="consumptionCtx.fuelMet" class="text-emerald-400 text-sm font-medium mt-3">已满足</p>
                              <template v-else>
                                <label class="block text-slate-500 text-xs mt-3 mb-1">
                                  木材（kg，库存 {{ consumptionCtx.availableWoodKg }}）
                                </label>
                                <input
                                  v-model.number="consumptionForm.woodKg"
                                  type="number"
                                  min="0"
                                  step="1"
                                  class="w-full bg-slate-900/80 border border-slate-600 rounded-lg px-3 py-2 text-white text-sm tabular-nums mb-2"
                                />
                                <label class="block text-slate-500 text-xs mb-1">
                                  燃料（kg，库存 {{ consumptionCtx.availableFuelKg }}，取暖还需 {{ consumptionCtx.remainingFuelKg ?? (consumptionCtx.requiredFuelKg - consumptionCtx.consumedFuelKg) }} kg）
                                </label>
                                <input
                                  v-model.number="consumptionForm.fuelKg"
                                  type="number"
                                  min="0"
                                  step="1"
                                  class="w-full bg-slate-900/80 border border-slate-600 rounded-lg px-3 py-2 text-white text-sm tabular-nums"
                                />
                              </template>
                            </div>
                          </div>
                          <p v-if="!consumptionCtx.requirementsMet" class="text-amber-200/70 text-xs leading-relaxed">
                            {{ consumptionCtx.weakWarning || '若当日未满足进食与取暖需求，次日将陷入「虚弱」状态。' }}
                          </p>
                          <p v-else class="text-emerald-400 text-sm font-medium">当日进食与取暖均已满足。</p>
                          <div v-if="consumptionNeedsSubmit" class="flex flex-wrap items-center gap-3">
                            <button
                              type="button"
                              class="px-4 py-2 rounded-lg bg-cyan-600 hover:bg-cyan-500 text-white text-sm font-medium disabled:opacity-50"
                              :disabled="consumptionSaving"
                              @click="submitConsumption"
                            >
                              {{ consumptionSaving ? '提交中…' : '确认消耗' }}
                            </button>
                            <span v-if="consumptionMessage" :class="consumptionMessage.type === 'success' ? 'text-emerald-400' : 'text-red-400'" class="text-xs">
                              {{ consumptionMessage.text }}
                            </span>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>

                  <!-- Right Column -->
                  <div class="col-span-12 lg:col-span-7 space-y-6 md:space-y-8">
                    <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
                      <div class="bg-slate-900/60 border border-slate-700/50 rounded-xl px-5 py-4">
                        <div class="text-slate-400 text-xs tracking-wider mb-1">职业</div>
                        <div class="text-xl md:text-2xl text-white font-bold">{{ dashboardProfile.profession }}</div>
                      </div>
                      <div class="bg-slate-900/60 border border-slate-700/50 rounded-xl px-5 py-4">
                        <div class="text-slate-400 text-xs tracking-wider mb-1">所属阵营</div>
                        <div class="text-xl md:text-2xl text-white font-bold">{{ dashboardProfile.faction }}</div>
                      </div>
                    </div>

                    <!-- Professional Skill -->
                    <div class="relative group">
                      <div class="absolute inset-0 bg-gradient-to-br from-cyan-500/5 to-blue-500/5 rounded-2xl transition-all duration-300 ease-out group-hover:from-cyan-500/10 group-hover:to-blue-500/10"></div>
                      <div class="relative bg-slate-900/60 border-l-4 border-cyan-500 rounded-2xl p-8 md:p-10 transition-all duration-200 ease-out hover:border-cyan-400 will-change-transform transform-gpu">
                        <div class="text-slate-400 text-xs uppercase tracking-wider mb-3">职业技能</div>
                        <h3 class="text-3xl md:text-4xl text-cyan-300 font-bold mb-4">{{ playerInfo.jobSkills || '职业技能' }}</h3>
                        <p class="text-slate-300 text-sm md:text-base leading-relaxed whitespace-pre-line">
                          {{ dashboardProfile.professionalSkill.description }}
                        </p>
                      </div>
                    </div>

                    <!-- Trait -->
                    <div class="relative group">
                      <div class="absolute inset-0 bg-gradient-to-br from-purple-500/5 to-pink-500/5 rounded-2xl transition-all duration-300 ease-out group-hover:from-purple-500/10 group-hover:to-pink-500/10"></div>
                      <div class="relative bg-slate-900/60 border-l-4 border-purple-500 rounded-2xl p-8 md:p-10 transition-all duration-200 ease-out hover:border-purple-400 will-change-transform transform-gpu">
                        <div class="text-slate-400 text-xs uppercase tracking-wider mb-3">特性</div>
                        <h3 class="text-3xl md:text-4xl text-purple-300 font-bold mb-4">{{ dashboardProfile.trait.name }}</h3>
                        <p class="text-slate-300 text-sm md:text-base leading-relaxed whitespace-pre-line">
                          {{ dashboardProfile.trait.description }}
                        </p>
                      </div>
                    </div>

                    <div class="pt-2">
                      <p class="text-slate-500 text-xs">
                        最后更新: {{ playerInfo.updatedAt ? new Date(playerInfo.updatedAt).toLocaleString('zh-CN') : '未知' }}
                      </p>
                    </div>
                  </div>
                </div>
          </div>
        </div>
      </div>

      <div v-else-if="activeTab === 'materials'">
        <MaterialsPanel />
      </div>

      <div v-else-if="activeTab === 'actions'">
        <ActionSubmitView embedded />
      </div>

      <div v-else-if="activeTab === 'factionActions' && showFactionActionsTab">
        <FactionActionSubmitView />
      </div>

      <div v-else-if="activeTab === 'nightActions' && showNightActionsTab">
        <NightActionSubmitView />
      </div>

      <div v-else-if="activeTab === 'ark' && showArkTab">
        <ArkProgressView embedded />
      </div>

      <div v-else-if="activeTab === 'shelter' && showShelterTab">
        <ShelterProgressView mode="ruler" />
      </div>

      <div v-else-if="activeTab === 'milestone' && showMilestoneTab">
        <RebelMilestoneView embedded />
      </div>

      <div v-else-if="activeTab === 'catastrophe' && showCatastropheTab">
        <CatastrophePanel :is-dm="false" embedded />
      </div>

      <div v-else-if="activeTab === 'warehouse'">
        <div class="mb-6">
          <h1 class="text-white mb-1 tracking-tight text-2xl">仓库管理</h1>
          <p class="text-gray-500 text-sm">查看和管理您有权限访问的仓库</p>
        </div>
        <WarehouseView />
      </div>

      <div v-else-if="activeTab === 'trade'">
        <TradePanel ref="tradePanelRef" />
      </div>
    </main>
  </div>
</template>

<style scoped>
.fade-pop-enter-active,
.fade-pop-leave-active {
  transition: opacity 140ms ease-out, transform 140ms ease-out;
}
.fade-pop-enter-from,
.fade-pop-leave-to {
  opacity: 0;
  transform: translateY(6px) scale(0.99);
}
</style>
