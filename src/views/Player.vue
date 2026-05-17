<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import MaterialsPanel from './MaterialsPanel.vue'
import TradePanel from './TradePanel.vue'
import ArkProgressView from './ArkProgressView.vue'
import ShelterProgressView from './ShelterProgressView.vue'
import ActionSubmitView from './ActionSubmitView.vue'
import FactionActionSubmitView from './FactionActionSubmitView.vue'
import RebelMilestoneView from './RebelMilestoneView.vue'
import CatastrophePanel from '../components/CatastrophePanel.vue'
import WarehouseView from './WarehouseView.vue'
import { tradeAPI, playerAPI, milestoneAPI } from '../utils/api.js'

const router = useRouter()
const username = localStorage.getItem('username') || ''
const playerId = parseInt(localStorage.getItem('playerId') || '1')
const activeTab = ref('info')

const tradePanelRef = ref(null)

const pendingTradesCount = ref(0)
const loading = ref(false)
const error = ref(null)
const playerInfo = ref(null)
const playerItems = ref(null)
const isEditing = ref(false)
const editForm = ref(null)
const saving = ref(false)
const saveMessage = ref(null)
const showStatusPopover = ref(false)

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

watch([showArkTab, showShelterTab, showMilestoneTab, showCatastropheTab, showFactionActionsTab, activeTab], () => {
  if (activeTab.value === 'ark' && !showArkTab.value) activeTab.value = 'info'
  if (activeTab.value === 'shelter' && !showShelterTab.value) activeTab.value = 'info'
  if (activeTab.value === 'milestone' && !showMilestoneTab.value) activeTab.value = 'info'
  if (activeTab.value === 'catastrophe' && !showCatastropheTab.value) activeTab.value = 'info'
  if (activeTab.value === 'factionActions' && !showFactionActionsTab.value) activeTab.value = 'info'
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
    const [infoResult, itemsResult] = await Promise.all([
      playerAPI.getDetails(playerId),
      playerAPI.getItems(playerId)
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
  } catch (err) {
    error.value = '网络请求失败，请稍后重试'
    console.error('Failed to fetch player info:', err)
  } finally {
    loading.value = false
  }
}

const startPolling = () => {
  fetchPendingTradesCount()
  pollTimer = setInterval(fetchPendingTradesCount, 3000)
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

const getStatusColor = () => {
  const faction = playerInfo.value?.faction
  const colors = {
    '统治者': 'text-amber-400 bg-amber-500/20',
    '反叛者': 'text-red-400 bg-red-500/20',
    '冒险者': 'text-emerald-400 bg-emerald-500/20',
    '天灾使者': 'text-purple-400 bg-purple-500/20',
    '平民': 'text-gray-400 bg-gray-500/20'
  }
  return colors[faction] || colors['平民']
}

const negativeStatuses = computed(() => {
  const p = playerInfo.value
  if (!p) return []

  /** severity: 1-3（UI 用） */
  const list = []
  if (p.isWeak) {
    list.push({
      name: '虚弱',
      severity: 2,
      description: '体力大幅下降，所有体力相关活动效率降低。需要充足休息和营养补充才能恢复。（占位文案，可由后端返回替换）'
    })
  }
  if (p.isOverworked) {
    list.push({
      name: '过劳',
      severity: 1,
      description: '长时间高强度工作导致精神疲惫，注意力不集中，工作效率下降。（占位文案，可由后端返回替换）'
    })
  }
  if (p.isInjured) {
    list.push({
      name: '受伤',
      severity: 3,
      description: '行动受限，部分高强度行动可能失败或效率下降。需要治疗或休养恢复。（占位文案，可由后端返回替换）'
    })
  }
  return list
})

const playerResources = computed(() => {
  if (!playerItems.value) {
    return { food: 0, energy: 0 }
  }
  
  const items = Array.isArray(playerItems.value) ? playerItems.value : (playerItems.value.value || [])
  
  let foodTotal = 0
  let energyTotal = 0
  
  items.forEach(item => {
    if (item.type === 'material') {
      const itemId = item.id
      const quantity = item.quantity || 0
      const unit = item.unit || ''
      
      let kgQuantity = quantity
      if (unit === 'kg') {
        kgQuantity = quantity
      } else if (unit === 'g') {
        kgQuantity = quantity / 1000
      }
      
      if (itemId === 5) {
        foodTotal += kgQuantity
      } else if (itemId === 8) {
        energyTotal += kgQuantity
      }
    }
  })
  
  return {
    food: Math.round(foodTotal),
    energy: Math.round(energyTotal)
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

  const dummy = {
    currentDay: 7,
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
    currentDay: dummy.currentDay,
    foodQuantity: resources.food,
    energyQuantity: resources.energy,
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

const startEdit = () => {
  editForm.value = {
    name: playerInfo.value?.name || '',
    isWeak: playerInfo.value?.isWeak || false,
    isOverworked: playerInfo.value?.isOverworked || false,
    isInjured: playerInfo.value?.isInjured || false
  }
  isEditing.value = true
  saveMessage.value = null
}

const cancelEdit = () => {
  isEditing.value = false
  editForm.value = null
  saveMessage.value = null
}

const saveEdit = async () => {
  if (!editForm.value) return
  saving.value = true
  saveMessage.value = null
  try {
    const result = await playerAPI.update(playerId, editForm.value)
    if (result && result.success) {
      saveMessage.value = { type: 'success', text: '保存成功！' }
      await fetchPlayerInfo()
      isEditing.value = false
      editForm.value = null
      setTimeout(() => { saveMessage.value = null }, 3000)
    } else {
      saveMessage.value = { type: 'error', text: result?.message || '保存失败' }
    }
  } catch (err) {
    saveMessage.value = { type: 'error', text: '网络请求失败，请稍后重试' }
    console.error('Failed to save player info:', err)
  } finally {
    saving.value = false
  }
}

const handleLogout = () => {
  localStorage.removeItem('userRole')
  localStorage.removeItem('username')
  localStorage.removeItem('playerId')
  router.push('/')
}

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
          :class="activeTab === 'status' ? 'bg-[#2d4263] text-white' : 'text-gray-400 hover:bg-[#151b2e] hover:text-gray-300'"
          @click="activeTab = 'status'"
        >
          游戏状态
        </button>
        <button
          type="button"
          class="w-full text-left px-4 py-3 rounded-xl mb-2 transition-colors font-medium"
          :class="activeTab === 'tasks' ? 'bg-[#2d4263] text-white' : 'text-gray-400 hover:bg-[#151b2e] hover:text-gray-300'"
          @click="activeTab = 'tasks'"
        >
          任务列表
        </button>
        <button
          type="button"
          class="w-full text-left px-4 py-3 rounded-xl mb-2 transition-colors font-medium"
          :class="activeTab === 'actions' ? 'bg-[#2d4263] text-white' : 'text-gray-400 hover:bg-[#151b2e] hover:text-gray-300'"
          @click="activeTab = 'actions'"
        >
          行动提交
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
          <div v-if="saveMessage" class="mb-4 max-w-4xl">
            <div :class="['px-4 py-3 rounded-lg flex items-center gap-3', saveMessage.type === 'success' ? 'bg-green-500/20 border border-green-500/30' : 'bg-red-500/20 border border-red-500/30']">
              <svg v-if="saveMessage.type === 'success'" class="w-5 h-5 text-green-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
              </svg>
              <svg v-else class="w-5 h-5 text-red-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              <span :class="saveMessage.type === 'success' ? 'text-green-400' : 'text-red-400'">{{ saveMessage.text }}</span>
            </div>
          </div>

          <div>
            <template v-if="isEditing">
              <div class="max-w-4xl bg-slate-900/60 border border-slate-700/50 rounded-2xl p-6 md:p-8">
                <div class="flex items-center justify-between gap-4 mb-6">
                  <div>
                    <h2 class="text-white text-xl font-semibold tracking-tight">编辑资料</h2>
                    <p class="text-slate-500 text-sm mt-1">修改姓名与状态后保存</p>
                  </div>
                  <button
                    type="button"
                    class="px-4 py-2 text-sm text-gray-300 hover:text-white bg-white/5 hover:bg-white/10 rounded-lg transition-colors"
                    @click="cancelEdit"
                    :disabled="saving"
                  >
                    关闭
                  </button>
                </div>

                <div class="space-y-6">
                <div>
                  <label class="block text-gray-400 text-sm mb-2">姓名</label>
                  <input
                    v-model="editForm.name"
                    type="text"
                    class="w-full px-4 py-3 bg-white/5 border border-white/10 rounded-xl text-white placeholder-gray-500 focus:outline-none focus:border-blue-500 transition-colors"
                    placeholder="请输入姓名"
                  />
                </div>

                <div>
                  <label class="block text-gray-400 text-sm mb-3">状态</label>
                  <div class="space-y-3">
                    <label class="flex items-center gap-3 cursor-pointer">
                      <input
                        v-model="editForm.isWeak"
                        type="checkbox"
                        class="w-5 h-5 rounded bg-white/5 border-white/10 text-amber-500 focus:ring-amber-500/50"
                      />
                      <span class="text-gray-300">虚弱</span>
                    </label>
                    <label class="flex items-center gap-3 cursor-pointer">
                      <input
                        v-model="editForm.isOverworked"
                        type="checkbox"
                        class="w-5 h-5 rounded bg-white/5 border-white/10 text-blue-500 focus:ring-blue-500/50"
                      />
                      <span class="text-gray-300">过劳</span>
                    </label>
                    <label class="flex items-center gap-3 cursor-pointer">
                      <input
                        v-model="editForm.isInjured"
                        type="checkbox"
                        class="w-5 h-5 rounded bg-white/5 border-white/10 text-red-500 focus:ring-red-500/50"
                      />
                      <span class="text-gray-300">受伤</span>
                    </label>
                  </div>
                </div>

                <div class="flex gap-3 pt-4">
                  <button
                    @click="cancelEdit"
                    :disabled="saving"
                    class="px-6 py-3 text-gray-300 bg-white/5 hover:bg-white/10 rounded-xl transition-colors disabled:opacity-50"
                  >
                    取消
                  </button>
                  <button
                    @click="saveEdit"
                    :disabled="saving"
                    class="px-6 py-3 text-white bg-blue-600 hover:bg-blue-700 rounded-xl transition-colors disabled:opacity-50 flex items-center gap-2"
                  >
                    <div v-if="saving" class="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                    {{ saving ? '保存中...' : '保存' }}
                  </button>
                </div>
              </div>
              </div>
            </template>

            <template v-else>
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
                        <button
                          type="button"
                          class="px-3 py-1.5 text-xs md:text-sm text-white bg-blue-600 hover:bg-blue-700 rounded-lg transition-colors flex items-center gap-2 will-change-transform transform-gpu"
                          @click="startEdit"
                        >
                          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                          </svg>
                          编辑
                        </button>
                      </div>

                      <div class="text-slate-500 text-xs md:text-sm mb-1">游戏进度</div>
                      <div class="text-2xl md:text-3xl text-cyan-300 font-bold">第 {{ dashboardProfile.currentDay }} 天</div>
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
                            <div class="w-12 h-12 bg-amber-500/20 rounded-lg flex items-center justify-center mx-auto mb-3">
                              <span class="text-2xl">🍞</span>
                            </div>
                            <div class="text-slate-400 text-xs mb-1">食物</div>
                            <div class="text-amber-300 text-2xl font-bold">{{ dashboardProfile.foodQuantity }}</div>
                            <div class="text-slate-500 text-xs mt-1">千克</div>
                          </div>
                          <div class="text-center transition-transform duration-200 ease-out hover:scale-[1.02] will-change-transform transform-gpu">
                            <div class="w-12 h-12 bg-yellow-500/20 rounded-lg flex items-center justify-center mx-auto mb-3">
                              <span class="text-2xl">⚡</span>
                            </div>
                            <div class="text-slate-400 text-xs mb-1">能量</div>
                            <div class="text-yellow-300 text-2xl font-bold">{{ dashboardProfile.energyQuantity }}</div>
                            <div class="text-slate-500 text-xs mt-1">千克</div>
                          </div>
                        </div>
                      </div>
                    </div>

                    <!-- Profession -->
                    <div class="relative group">
                      <div class="absolute inset-0 bg-gradient-to-br from-amber-500/10 to-orange-500/10 rounded-2xl blur-xl transition-all duration-300 ease-out group-hover:blur-2xl"></div>
                      <div class="relative bg-slate-900/80 border border-slate-700/50 rounded-2xl p-8 md:p-10 transition-all duration-200 ease-out hover:border-amber-500/50 will-change-transform transform-gpu">
                        <div class="text-slate-400 text-xs md:text-sm mb-3 tracking-wider">职业</div>
                        <div class="text-3xl md:text-4xl text-white font-bold">{{ dashboardProfile.profession }}</div>
                      </div>
                    </div>

                    <!-- Faction -->
                    <div class="relative group">
                      <div class="absolute inset-0 bg-gradient-to-br from-blue-500/10 to-purple-500/10 rounded-2xl blur-xl transition-all duration-300 ease-out group-hover:blur-2xl"></div>
                      <div class="relative bg-slate-900/80 border border-slate-700/50 rounded-2xl p-8 md:p-10 transition-all duration-200 ease-out hover:border-blue-500/50 will-change-transform transform-gpu">
                        <div class="text-slate-400 text-xs md:text-sm mb-3 tracking-wider">所属阵营</div>
                        <div class="text-3xl md:text-4xl text-white font-bold">{{ dashboardProfile.faction }}</div>
                      </div>
                    </div>
                  </div>

                  <!-- Right Column -->
                  <div class="col-span-12 lg:col-span-7 space-y-6 md:space-y-8">
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
            </template>
          </div>
        </div>
      </div>

      <div v-else-if="activeTab === 'status'">
        <h1 class="text-white mb-6 tracking-tight text-2xl">游戏状态</h1>
        <div class="bg-[#0f1419] border border-[#1f2937] rounded-xl p-6">
          <p class="text-gray-500 font-normal">游戏状态功能开发中...</p>
        </div>
      </div>

      <div v-else-if="activeTab === 'tasks'">
        <h1 class="text-white mb-6 tracking-tight text-2xl">任务列表</h1>
        <div class="bg-[#0f1419] border border-[#1f2937] rounded-xl p-6">
          <p class="text-gray-500 font-normal">任务列表功能开发中...</p>
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

      <div v-else-if="activeTab === 'ark' && showArkTab">
        <ArkProgressView embedded />
      </div>

      <div v-else-if="activeTab === 'shelter' && showShelterTab">
        <ShelterProgressView />
      </div>

      <div v-else-if="activeTab === 'milestone' && showMilestoneTab">
        <RebelMilestoneView embedded />
      </div>

      <div v-else-if="activeTab === 'catastrophe' && showCatastropheTab">
        <div class="mb-6">
          <h1 class="text-white mb-1 tracking-tight text-2xl">天灾降临</h1>
          <p class="text-gray-500 text-sm">掌控天灾的力量，决定岛屿的命运</p>
        </div>
        <CatastrophePanel :is-dm="false" />
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
