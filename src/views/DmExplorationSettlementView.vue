<script setup>
import { ref, computed, onMounted } from 'vue'
import { explorationAPI, gameStateAPI } from '@/utils/api.js'

const loading = ref(true)
const gameDay = ref(1)
const explorations = ref([])
const events = ref([])
const expandedId = ref(null)
const submitting = ref(false)

const showEventManager = ref(false)
const editingEvent = ref(null)
const eventForm = ref({
  name: '',
  description: '',
  rarity: 'normal',
  eventDifficulty: 5,
  locationDesc: '',
  loreFragment: '',
  isSpecial: false,
})

const selectedEventDetail = ref(null)
const reimporting = ref(false)

async function handleReimport() {
  if (!confirm('确定要重新导入所有事件数据吗？这将清空当前所有事件并重新从文件导入。')) return
  reimporting.value = true
  try {
    const res = await explorationAPI.reimportEvents()
    if (res?.success) {
      alert('重新导入成功')
      await loadPendingExplorations()
    } else {
      alert(res?.message || '导入失败')
    }
  } catch {
    alert('导入失败')
  } finally {
    reimporting.value = false
  }
}

function viewEventDetail(event) {
  selectedEventDetail.value = event
}

function closeEventDetail() {
  selectedEventDetail.value = null
}

// 难度筛选与排序
const difficultyFilter = ref('all') // all / 0-4 / 5-9 / 10-14 / 15-20
const sortBy = ref('difficulty') // difficulty / name / id

const MIN_DIFFICULTY = 0
const MAX_DIFFICULTY = 20

const filteredEvents = computed(() => {
  let list = [...events.value]
  if (difficultyFilter.value !== 'all') {
    const [min, max] = difficultyFilter.value.split('-').map(Number)
    list = list.filter((e) => {
      const d = e.eventDifficulty ?? 0
      return d >= min && d <= max
    })
  }
  if (sortBy.value === 'difficulty') {
    list.sort((a, b) => (b.eventDifficulty ?? 0) - (a.eventDifficulty ?? 0))
  } else if (sortBy.value === 'name') {
    list.sort((a, b) => String(a.name).localeCompare(String(b.name), 'zh-CN'))
  } else if (sortBy.value === 'id') {
    list.sort((a, b) => a.id - b.id)
  }
  return list
})

const pendingExplorations = computed(() =>
  explorations.value.filter((e) => e.status === 'pending')
)
const exploredExplorations = computed(() =>
  explorations.value.filter((e) => e.status === 'explored')
)
const settledExplorations = computed(() =>
  explorations.value.filter((e) => e.status === 'settled')
)

async function loadGameDay() {
  try {
    const state = await gameStateAPI.getState()
    gameDay.value = state?.currentDay || 1
  } catch (e) {
    console.error(e)
  }
}

async function loadPendingExplorations() {
  loading.value = true
  try {
    const [explRes, eventsRes] = await Promise.all([
      explorationAPI.getPendingExplorations(gameDay.value),
      explorationAPI.getAllEvents(),
    ])
    if (explRes?.success) {
      explorations.value = explRes.explorations || []
    }
    if (Array.isArray(eventsRes)) {
      events.value = eventsRes
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

function toggleExpand(id) {
  expandedId.value = expandedId.value === id ? null : id
}

async function triggerRandomEvent(explorationId) {
  submitting.value = true
  try {
    const res = await explorationAPI.triggerRandomEvent(explorationId)
    if (res?.success) {
      await loadPendingExplorations()
    } else {
      alert(res?.message || '触发失败')
    }
  } catch {
    alert('触发失败')
  } finally {
    submitting.value = false
  }
}

async function triggerSpecificEvent(explorationId, eventId) {
  submitting.value = true
  try {
    const res = await explorationAPI.triggerEvent(explorationId, eventId)
    if (res?.success) {
      await loadPendingExplorations()
    } else {
      alert(res?.message || '触发失败')
    }
  } catch {
    alert('触发失败')
  } finally {
    submitting.value = false
  }
}

async function settleExploration(exploration) {
  const rewards = exploration.event?.rewards || []
  if (!rewards.length) {
    alert('该事件没有奖励')
    return
  }
  submitting.value = true
  try {
    const res = await explorationAPI.settle(exploration.id, rewards)
    if (res?.success) {
      await loadPendingExplorations()
    } else {
      alert(res?.message || '结算失败')
    }
  } catch {
    alert('结算失败')
  } finally {
    submitting.value = false
  }
}

function getRarityLabel(rarity) {
  const labels = { common: '普通', rare: '稀有', epic: '史诗' }
  return labels[rarity] || rarity
}

function getRarityColor(rarity) {
  const colors = {
    common: 'bg-gray-500/20 text-gray-400 border-gray-500/30',
    rare: 'bg-blue-500/20 text-blue-400 border-blue-500/30',
    epic: 'bg-purple-500/20 text-purple-400 border-purple-500/30',
  }
  return colors[rarity] || colors.common
}

/**
 * 根据难度返回颜色 class（0-20）
 * 0-4绿色(简单) / 5-9蓝色(普通) / 10-14橙色(困难) / 15-20红色(极难)
 */
function getDifficultyColor(difficulty) {
  const d = Number(difficulty ?? 0)
  if (d <= 4) return 'bg-green-500/20 text-green-400 border-green-500/30'
  if (d <= 9) return 'bg-blue-500/20 text-blue-400 border-blue-500/30'
  if (d <= 14) return 'bg-orange-500/20 text-orange-400 border-orange-500/30'
  return 'bg-red-500/20 text-red-400 border-red-500/30'
}

/**
 * 根据难度返回图标 emoji
 */
function getDifficultyIcon(difficulty) {
  const d = Number(difficulty ?? 0)
  if (d <= 4) return '🟢'
  if (d <= 9) return '🔵'
  if (d <= 14) return '🟠'
  return '🔴'
}

function getDifficultyLabel(difficulty) {
  const d = Number(difficulty ?? 0)
  if (d <= 4) return '简单'
  if (d <= 9) return '普通'
  if (d <= 14) return '困难'
  return '极难'
}



// ============ 事件管理 ============
function openCreateEvent() {
  editingEvent.value = null
  eventForm.value = {
    name: '',
    description: '',
    rarity: 'normal',
    eventDifficulty: 5,
    locationDesc: '',
    loreFragment: '',
    isSpecial: false,
  }
  showEventManager.value = true
}

function openEditEvent(event) {
  editingEvent.value = event
  eventForm.value = {
    name: event.name || '',
    description: event.description || '',
    rarity: event.rarity || 'normal',
    eventDifficulty: event.eventDifficulty ?? 5,
    locationDesc: event.locationDesc || '',
    loreFragment: event.loreFragment || '',
    isSpecial: event.isSpecial || false,
  }
  showEventManager.value = true
}

function closeEventManager() {
  showEventManager.value = false
  editingEvent.value = null
}

async function saveEvent() {
  const difficulty = Number(eventForm.value.eventDifficulty)
  if (!Number.isInteger(difficulty) || difficulty < MIN_DIFFICULTY || difficulty > MAX_DIFFICULTY) {
    alert(`难度必须为 ${MIN_DIFFICULTY}-${MAX_DIFFICULTY} 之间的整数`)
    return
  }
  if (!eventForm.value.name?.trim()) {
    alert('事件名称不能为空')
    return
  }

  submitting.value = true
  try {
    let res
    if (editingEvent.value) {
      res = await explorationAPI.updateEvent(editingEvent.value.id, {
        name: eventForm.value.name,
        description: eventForm.value.description,
        rarity: eventForm.value.rarity,
        eventDifficulty: difficulty,
        locationDesc: eventForm.value.locationDesc,
        loreFragment: eventForm.value.loreFragment,
        isSpecial: eventForm.value.isSpecial,
      })
    } else {
      res = await explorationAPI.createEvent({
        name: eventForm.value.name,
        description: eventForm.value.description,
        rarity: eventForm.value.rarity,
        eventDifficulty: difficulty,
        locationDesc: eventForm.value.locationDesc,
        loreFragment: eventForm.value.loreFragment,
        isSpecial: eventForm.value.isSpecial,
      })
    }
    if (res?.success) {
      await loadPendingExplorations()
      closeEventManager()
    } else {
      alert(res?.message || '保存失败')
    }
  } catch {
    alert('保存失败')
  } finally {
    submitting.value = false
  }
}

async function deleteEvent(event) {
  if (!confirm(`确定要删除事件「${event.name}」吗？`)) return
  submitting.value = true
  try {
    const res = await explorationAPI.deleteEvent(event.id)
    if (res?.success) {
      await loadPendingExplorations()
    } else {
      alert(res?.message || '删除失败')
    }
  } catch {
    alert('删除失败')
  } finally {
    submitting.value = false
  }
}

function getStatusLabel(status) {
  const labels = { pending: '待探索', explored: '待结算', settled: '已结算' }
  return labels[status] || status
}

function getStatusColor(status) {
  const colors = {
    pending: 'bg-amber-500/20 text-amber-400',
    explored: 'bg-blue-500/20 text-blue-400',
    settled: 'bg-green-500/20 text-green-400',
  }
  return colors[status] || 'bg-gray-500/20 text-gray-400'
}

onMounted(async () => {
  await loadGameDay()
  await loadPendingExplorations()
})
</script>

<template>
  <div>
    <div class="text-center mb-8">
      <h1 class="text-white text-2xl font-semibold mb-2">探索岛屿结算</h1>
      <p class="text-gray-500 text-sm">处理玩家的探索岛屿行动，触发事件并发放奖励</p>
      <div class="mt-3">
        <span class="text-gray-400 text-sm">游戏第 {{ gameDay }} 天</span>
      </div>
    </div>

    <div v-if="loading" class="flex justify-center py-20">
      <div class="w-12 h-12 border-4 border-indigo-500 border-t-transparent rounded-full animate-spin" />
    </div>

    <template v-else>
      <div class="mb-6 flex items-center gap-3 flex-wrap">
        <button
          type="button"
          class="bg-gradient-to-r from-indigo-500 to-indigo-600 hover:from-indigo-600 hover:to-indigo-700 text-white px-6 py-2.5 rounded-xl text-sm font-medium"
          @click="loadPendingExplorations"
        >
          刷新探索列表
        </button>
        <button
          type="button"
          class="bg-gradient-to-r from-purple-500 to-purple-600 hover:from-purple-600 hover:to-purple-700 text-white px-6 py-2.5 rounded-xl text-sm font-medium"
          @click="openCreateEvent"
        >
          + 新建事件
        </button>
        <button
          type="button"
          :disabled="reimporting"
          class="bg-gradient-to-r from-amber-500 to-orange-500 hover:from-amber-600 hover:to-orange-600 disabled:from-gray-600 disabled:to-gray-600 text-white px-6 py-2.5 rounded-xl text-sm font-medium"
          @click="handleReimport"
        >
          {{ reimporting ? '导入中...' : '🔄 重新导入事件数据' }}
        </button>
        <div class="flex items-center gap-2 text-xs text-gray-400">
          <span>难度筛选：</span>
          <select
            v-model="difficultyFilter"
            class="bg-black/30 border border-white/10 rounded-lg px-2 py-1.5 text-sm text-gray-200"
          >
            <option value="all">全部</option>
            <option value="0-4">简单 (0-4)</option>
            <option value="5-9">普通 (5-9)</option>
            <option value="10-14">困难 (10-14)</option>
            <option value="15-20">极难 (15-20)</option>
          </select>
          <span class="ml-2">排序：</span>
          <select
            v-model="sortBy"
            class="bg-black/30 border border-white/10 rounded-lg px-2 py-1.5 text-sm text-gray-200"
          >
            <option value="difficulty">按难度</option>
            <option value="name">按名称</option>
            <option value="id">按ID</option>
          </select>
        </div>
      </div>

      <!-- 事件管理列表（始终显示） -->
      <div class="mb-6">
        <h2 class="text-white text-lg font-medium mb-4 flex items-center gap-2">
          <span class="w-2 h-2 rounded-full bg-purple-500"></span>
          事件库（{{ events.length }}）
          <span class="text-xs text-gray-500 ml-2">显示：{{ filteredEvents.length }}</span>
        </h2>
        <div class="space-y-2 max-h-96 overflow-y-auto">
          <div
            v-for="event in filteredEvents"
            :key="event.id"
            class="bg-gradient-to-br from-[#1a2332] to-[#0f1419] border border-white/10 rounded-xl p-3 flex items-start justify-between gap-3"
          >
            <div class="flex-1 min-w-0">
              <div class="flex items-center gap-2 flex-wrap mb-1">
                <span class="text-white text-sm font-medium">{{ event.name }}</span>
                <span
                  class="text-xs px-2 py-0.5 rounded-full border"
                  :class="getDifficultyColor(event.eventDifficulty)"
                >
                  {{ getDifficultyIcon(event.eventDifficulty) }} 难度 {{ event.eventDifficulty }}/{{ MAX_DIFFICULTY }}
                </span>
                <span
                  class="text-xs px-2 py-0.5 rounded-full border"
                  :class="getRarityColor(event.rarity)"
                >
                  {{ getRarityLabel(event.rarity) }}
                </span>
                <span v-if="event.isSpecial" class="text-xs px-2 py-0.5 rounded-full border border-pink-500/30 bg-pink-500/20 text-pink-400">
                  ⭐ 特殊事件
                </span>
                <span v-if="event.triggered" class="text-gray-600 text-xs">（已触发）</span>
              </div>
              <p class="text-gray-500 text-xs line-clamp-1">{{ event.description }}</p>
            </div>
            <div class="flex gap-2 shrink-0">
              <button
                type="button"
                class="text-xs px-3 py-1 rounded-lg border border-gray-500/30 text-gray-400 hover:bg-gray-500/10"
                @click="viewEventDetail(event)"
              >
                详情
              </button>
              <button
                type="button"
                class="text-xs px-3 py-1 rounded-lg border border-blue-500/30 text-blue-400 hover:bg-blue-500/10"
                @click="openEditEvent(event)"
              >
                编辑
              </button>
              <button
                type="button"
                class="text-xs px-3 py-1 rounded-lg border border-red-500/30 text-red-400 hover:bg-red-500/10"
                @click="deleteEvent(event)"
              >
                删除
              </button>
            </div>
          </div>
          <div v-if="filteredEvents.length === 0" class="text-center py-8 text-gray-500 text-sm">
            没有匹配的事件
          </div>
        </div>
      </div>

      <div v-if="explorations.length === 0" class="text-center py-16 text-gray-400">
        暂无待处理的探索行动
      </div>

      <div v-else class="space-y-6">
        <div v-if="pendingExplorations.length">
          <h2 class="text-white text-lg font-medium mb-4 flex items-center gap-2">
            <span class="w-2 h-2 rounded-full bg-amber-500"></span>
            待探索（{{ pendingExplorations.length }}）
          </h2>
          <div class="space-y-3">
            <div
              v-for="exp in pendingExplorations"
              :key="exp.id"
              class="bg-gradient-to-br from-[#1a2332] to-[#0f1419] border border-white/10 rounded-xl p-4"
            >
              <div class="flex items-center justify-between mb-3">
                <div>
                  <span class="text-white text-sm font-medium">{{ exp.playerName }}</span>
                  <span v-if="exp.faction" class="ml-2 text-gray-500 text-xs">（{{ exp.faction }}）</span>
                </div>
                <span
                  class="text-xs px-2 py-0.5 rounded-full"
                  :class="getStatusColor(exp.status)"
                >
                  {{ getStatusLabel(exp.status) }}
                </span>
              </div>

              <div class="flex gap-3">
                <button
                  type="button"
                  :disabled="submitting"
                  class="flex-1 bg-gradient-to-r from-green-500/20 to-green-600/20 hover:from-green-500/30 hover:to-green-600/30 border border-green-500/30 text-green-400 px-4 py-2 rounded-lg text-sm transition-colors"
                  @click="triggerRandomEvent(exp.id)"
                >
                  {{ submitting ? '处理中...' : '随机触发事件' }}
                </button>
                <button
                  type="button"
                  class="flex-1 bg-gradient-to-r from-blue-500/20 to-blue-600/20 hover:from-blue-500/30 hover:to-blue-600/30 border border-blue-500/30 text-blue-400 px-4 py-2 rounded-lg text-sm transition-colors"
                  @click="toggleExpand(exp.id)"
                >
                  {{ expandedId === exp.id ? '收起' : '选择事件' }}
                </button>
              </div>

              <div v-if="expandedId === exp.id" class="mt-4 border-t border-white/10 pt-4">
                <p class="text-gray-500 text-xs mb-2">选择要触发的事件：</p>
                <div class="space-y-2 max-h-60 overflow-y-auto">
                  <button
                    v-for="event in filteredEvents"
                    :key="event.id"
                    type="button"
                    :disabled="submitting"
                    class="w-full text-left p-3 rounded-lg border transition-colors"
                    :class="event.triggered
                      ? 'bg-gray-500/10 border-gray-500/20 text-gray-500'
                      : 'bg-white/5 border-white/10 hover:bg-white/10 text-gray-300'"
                    @click="event.triggered ? null : triggerSpecificEvent(exp.id, event.id)"
                  >
                    <div class="flex items-center justify-between mb-1 flex-wrap gap-1">
                      <span class="text-sm font-medium">{{ event.name }}</span>
                      <div class="flex items-center gap-1">
                        <span
                          class="text-xs px-2 py-0.5 rounded-full border"
                          :class="getDifficultyColor(event.eventDifficulty)"
                          :title="`难度 ${event.eventDifficulty}/${MAX_DIFFICULTY}`"
                        >
                          {{ getDifficultyIcon(event.eventDifficulty) }} 难度 {{ event.eventDifficulty }}/{{ MAX_DIFFICULTY }}
                        </span>
                        <span
                          class="text-xs px-2 py-0.5 rounded-full border"
                          :class="getRarityColor(event.rarity)"
                        >
                          {{ getRarityLabel(event.rarity) }}
                        </span>
                      </div>
                    </div>
                    <p class="text-gray-500 text-xs line-clamp-2">{{ event.description }}</p>
                    <span v-if="event.triggered" class="text-gray-600 text-xs">（已触发）</span>
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div v-if="exploredExplorations.length">
          <h2 class="text-white text-lg font-medium mb-4 flex items-center gap-2">
            <span class="w-2 h-2 rounded-full bg-blue-500"></span>
            待结算（{{ exploredExplorations.length }}）
          </h2>
          <div class="space-y-3">
            <div
              v-for="exp in exploredExplorations"
              :key="exp.id"
              class="bg-gradient-to-br from-[#1a2332] to-[#0f1419] border border-white/10 rounded-xl p-4"
            >
              <div class="flex items-center justify-between mb-3">
                <div>
                  <span class="text-white text-sm font-medium">{{ exp.playerName }}</span>
                  <span v-if="exp.faction" class="ml-2 text-gray-500 text-xs">（{{ exp.faction }}）</span>
                </div>
                <span
                  class="text-xs px-2 py-0.5 rounded-full"
                  :class="getStatusColor(exp.status)"
                >
                  {{ getStatusLabel(exp.status) }}
                </span>
              </div>

              <div v-if="exp.event" class="mb-3">
                <div class="flex items-center gap-2 mb-1 flex-wrap">
                  <span class="text-indigo-400 text-sm font-medium">{{ exp.event.name }}</span>
                  <span
                    class="text-xs px-2 py-0.5 rounded-full border"
                    :class="getDifficultyColor(exp.event.eventDifficulty)"
                    :title="`难度 ${exp.event.eventDifficulty}/${MAX_DIFFICULTY}`"
                  >
                    {{ getDifficultyIcon(exp.event.eventDifficulty) }} 难度 {{ exp.event.eventDifficulty }}/{{ MAX_DIFFICULTY }}
                  </span>
                  <span
                    class="text-xs px-2 py-0.5 rounded-full border"
                    :class="getRarityColor(exp.event.rarity)"
                  >
                    {{ getRarityLabel(exp.event.rarity) }}
                  </span>
                </div>
                <p class="text-gray-400 text-xs">{{ exp.event.description }}</p>

              </div>

              <div v-if="exp.rewards && exp.rewards.length" class="mb-4">
                <p class="text-gray-500 text-xs mb-2">奖励列表：</p>
                <div class="flex flex-wrap gap-2">
                  <span
                    v-for="reward in exp.rewards"
                    :key="reward.id"
                    class="bg-green-500/10 border border-green-500/20 text-green-400 px-3 py-1 rounded-lg text-xs"
                  >
                    +{{ reward.quantity }}{{ reward.unit }} {{ reward.name }}
                    <span v-if="reward.conditionDesc" class="text-gray-500 ml-1">（{{ reward.conditionDesc }}）</span>
                  </span>
                </div>
              </div>

              <button
                type="button"
                :disabled="submitting || !exp.rewards?.length"
                class="w-full bg-gradient-to-r from-green-500 to-green-600 hover:from-green-600 hover:to-green-700 disabled:from-gray-600 disabled:to-gray-600 text-white py-2.5 rounded-lg text-sm font-medium"
                @click="settleExploration(exp)"
              >
                {{ submitting ? '结算中...' : '发放奖励并结算' }}
              </button>
            </div>
          </div>
        </div>

        <div v-if="settledExplorations.length">
          <h2 class="text-white text-lg font-medium mb-4 flex items-center gap-2">
            <span class="w-2 h-2 rounded-full bg-green-500"></span>
            已结算（{{ settledExplorations.length }}）
          </h2>
          <div class="space-y-3">
            <div
              v-for="exp in settledExplorations"
              :key="exp.id"
              class="bg-gray-500/5 border border-gray-500/20 rounded-xl p-4"
            >
              <div class="flex items-center justify-between mb-2">
                <span class="text-gray-400 text-sm">{{ exp.playerName }}</span>
                <span
                  class="text-xs px-2 py-0.5 rounded-full"
                  :class="getStatusColor(exp.status)"
                >
                  {{ getStatusLabel(exp.status) }}
                </span>
              </div>
              <div v-if="exp.event" class="text-gray-500 text-xs">
                发现：{{ exp.event.name }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </template>

    <!-- 事件管理弹窗 -->
    <div
      v-if="showEventManager"
      class="fixed inset-0 bg-black/70 flex items-center justify-center z-50 p-4"
      @click.self="closeEventManager"
    >
      <div class="bg-gradient-to-br from-[#1a2332] to-[#0f1419] border border-white/20 rounded-2xl p-6 max-w-lg w-full max-h-[90vh] overflow-y-auto">
        <h3 class="text-white text-lg font-semibold mb-4">
          {{ editingEvent ? '编辑事件' : '新建事件' }}
        </h3>
        <div class="space-y-4">
          <div>
            <label class="block text-gray-400 text-xs mb-1">事件名称 <span class="text-red-400">*</span></label>
            <input
              v-model="eventForm.name"
              type="text"
              maxlength="100"
              class="w-full bg-black/30 border border-white/10 rounded-lg px-3 py-2 text-sm text-gray-200"
              placeholder="例：神秘洞穴"
            />
          </div>
          <div>
            <label class="block text-gray-400 text-xs mb-1">事件描述</label>
            <textarea
              v-model="eventForm.description"
              rows="3"
              class="w-full bg-black/30 border border-white/10 rounded-lg px-3 py-2 text-sm text-gray-200 resize-none"
              placeholder="事件的详细描述..."
            />
          </div>
          <div>
            <label class="block text-gray-400 text-xs mb-1">地点描述</label>
            <textarea
              v-model="eventForm.locationDesc"
              rows="2"
              class="w-full bg-black/30 border border-white/10 rounded-lg px-3 py-2 text-sm text-gray-200 resize-none"
              placeholder="地点的环境描述..."
            />
          </div>
          <div>
            <label class="block text-gray-400 text-xs mb-1">历史秘密碎片</label>
            <textarea
              v-model="eventForm.loreFragment"
              rows="3"
              class="w-full bg-black/30 border border-white/10 rounded-lg px-3 py-2 text-sm text-gray-200 resize-none"
              placeholder="探索时发现的历史秘密/线索..."
            />
          </div>
          <div>
            <label class="block text-gray-400 text-xs mb-1">稀有度</label>
            <select
              v-model="eventForm.rarity"
              class="w-full bg-black/30 border border-white/10 rounded-lg px-3 py-2 text-sm text-gray-200"
            >
              <option value="common">普通 (common)</option>
              <option value="rare">稀有 (rare)</option>
              <option value="epic">史诗 (epic)</option>
            </select>
          </div>
          <div>
            <label class="block text-gray-400 text-xs mb-1">
              难度 ({{ MIN_DIFFICULTY }}-{{ MAX_DIFFICULTY }})
              <span class="ml-2 px-2 py-0.5 rounded-full border text-xs" :class="getDifficultyColor(eventForm.eventDifficulty)">
                {{ getDifficultyIcon(eventForm.eventDifficulty) }} {{ getDifficultyLabel(eventForm.eventDifficulty) }} · {{ eventForm.eventDifficulty }}
              </span>
            </label>
            <div class="flex items-center gap-3">
              <input
                v-model.number="eventForm.eventDifficulty"
                type="range"
                :min="MIN_DIFFICULTY"
                :max="MAX_DIFFICULTY"
                step="1"
                class="flex-1"
              />
              <input
                v-model.number="eventForm.eventDifficulty"
                type="number"
                :min="MIN_DIFFICULTY"
                :max="MAX_DIFFICULTY"
                step="1"
                class="w-20 bg-black/30 border border-white/10 rounded-lg px-2 py-1 text-sm text-gray-200 text-center"
              />
            </div>

          </div>
          <div class="flex items-center gap-3">
            <label class="flex items-center gap-2 cursor-pointer">
              <input
                type="checkbox"
                v-model="eventForm.isSpecial"
                class="w-4 h-4"
              />
              <span class="text-gray-300 text-sm">特殊事件（永不锁定，可反复抽取）</span>
            </label>
          </div>
        </div>
        <div class="flex justify-end gap-3 mt-6">
          <button
            type="button"
            class="px-4 py-2 rounded-lg border border-white/10 text-gray-300 hover:bg-white/5 text-sm"
            @click="closeEventManager"
          >
            取消
          </button>
          <button
            type="button"
            :disabled="submitting"
            class="px-4 py-2 rounded-lg bg-gradient-to-r from-indigo-500 to-indigo-600 hover:from-indigo-600 hover:to-indigo-700 disabled:from-gray-600 disabled:to-gray-600 text-white text-sm"
            @click="saveEvent"
          >
            {{ submitting ? '保存中...' : '保存' }}
          </button>
        </div>
      </div>
    </div>

    <!-- 事件详情弹窗 -->
    <div
      v-if="selectedEventDetail"
      class="fixed inset-0 bg-black/70 flex items-center justify-center z-50 p-4"
      @click.self="closeEventDetail"
    >
      <div class="bg-gradient-to-br from-[#1a2332] to-[#0f1419] border border-white/20 rounded-2xl p-6 max-w-2xl w-full max-h-[90vh] overflow-y-auto">
        <div class="flex items-center justify-between mb-4">
          <h3 class="text-white text-lg font-semibold">
            {{ selectedEventDetail.name }}
          </h3>
          <button
            type="button"
            class="text-gray-400 hover:text-white text-xl"
            @click="closeEventDetail"
          >
            ×
          </button>
        </div>

        <div class="flex flex-wrap gap-2 mb-4">
          <span
            class="text-xs px-2 py-0.5 rounded-full border"
            :class="getDifficultyColor(selectedEventDetail.eventDifficulty)"
          >
            {{ getDifficultyIcon(selectedEventDetail.eventDifficulty) }} 难度 {{ selectedEventDetail.eventDifficulty }}/{{ MAX_DIFFICULTY }}
          </span>
          <span
            class="text-xs px-2 py-0.5 rounded-full border"
            :class="getRarityColor(selectedEventDetail.rarity)"
          >
            {{ getRarityLabel(selectedEventDetail.rarity) }}
          </span>
          <span v-if="selectedEventDetail.isSpecial" class="text-xs px-2 py-0.5 rounded-full border border-pink-500/30 bg-pink-500/20 text-pink-400">
            ⭐ 特殊事件
          </span>
          <span :class="selectedEventDetail.triggered ? 'text-gray-600' : 'text-green-400'" class="text-xs">
            {{ selectedEventDetail.triggered ? '已触发' : '未触发' }}
          </span>
        </div>

        <div class="space-y-4">
          <div v-if="selectedEventDetail.locationDesc">
            <h4 class="text-indigo-400 text-sm font-medium mb-1">📍 地点描述</h4>
            <p class="text-gray-300 text-sm whitespace-pre-wrap bg-black/20 rounded-lg p-3">
              {{ selectedEventDetail.locationDesc }}
            </p>
          </div>

          <div v-if="selectedEventDetail.description">
            <h4 class="text-indigo-400 text-sm font-medium mb-1">📜 事件描述</h4>
            <p class="text-gray-300 text-sm whitespace-pre-wrap bg-black/20 rounded-lg p-3">
              {{ selectedEventDetail.description }}
            </p>
          </div>

          <div v-if="selectedEventDetail.loreFragment">
            <h4 class="text-amber-400 text-sm font-medium mb-1">🔮 历史秘密碎片</h4>
            <p class="text-gray-300 text-sm whitespace-pre-wrap bg-amber-500/10 border border-amber-500/20 rounded-lg p-3">
              {{ selectedEventDetail.loreFragment }}
            </p>
          </div>

          <div v-if="selectedEventDetail.rewards && selectedEventDetail.rewards.length">
            <h4 class="text-green-400 text-sm font-medium mb-2">🎁 物资奖励</h4>
            <div class="flex flex-wrap gap-2">
              <span
                v-for="reward in selectedEventDetail.rewards"
                :key="reward.id"
                class="bg-green-500/10 border border-green-500/20 text-green-400 px-3 py-1 rounded-lg text-sm"
              >
                +{{ reward.quantity }}{{ reward.unit }} {{ reward.name }}
                <span v-if="reward.conditionDesc" class="text-gray-500 ml-1">（{{ reward.conditionDesc }}）</span>
              </span>
            </div>
          </div>


        </div>

        <div class="flex justify-end gap-3 mt-6">
          <button
            type="button"
            class="px-4 py-2 rounded-lg border border-white/10 text-gray-300 hover:bg-white/5 text-sm"
            @click="closeEventDetail"
          >
            关闭
          </button>
          <button
            type="button"
            class="px-4 py-2 rounded-lg bg-gradient-to-r from-blue-500 to-blue-600 hover:from-blue-600 hover:to-blue-700 text-white text-sm"
            @click="closeEventDetail(); openEditEvent(selectedEventDetail)"
          >
            编辑事件
          </button>
        </div>
      </div>
    </div>
  </div>
</template>