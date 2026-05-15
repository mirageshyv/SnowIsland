<script setup>
import { ref, computed, onMounted } from 'vue'
import { actionAPI } from '@/utils/api.js'

const actions = ref([])
const loading = ref(true)
const filterGameDay = ref('1')
const filterActionType = ref('')
const filterStatus = ref('')
const filterPlayerId = ref('')
const feedbackText = ref('')
const selectedActionId = ref(null)
const resolving = ref(false)
const resolveMessage = ref(null)

const actionTypeOptions = [
  { value: '', label: '全部类型' },
  { value: 'go_location', label: '前往地点' },
  { value: 'investigate_player', label: '调查玩家' },
  { value: 'produce', label: '生产' },
  { value: 'use_trait', label: '使用特性' },
  { value: 'use_skill', label: '使用职业技能' },
  { value: 'transport', label: '搬运' },
  { value: 'hide', label: '隐藏' },
]

const statusOptions = [
  { value: '', label: '全部状态' },
  { value: 'pending', label: '待反馈' },
  { value: 'feedbacked', label: '已反馈' },
]

const filteredActions = computed(() => {
  if (!Array.isArray(actions.value)) return []
  let result = actions.value
  if (filterActionType.value) {
    result = result.filter(a => a.actionType === filterActionType.value)
  }
  if (filterStatus.value) {
    result = result.filter(a => a.status === filterStatus.value)
  }
  return result
})

const pendingCount = computed(() => {
  if (!Array.isArray(actions.value)) return 0
  return actions.value.filter(a => a.status === 'pending').length
})
const feedbackedCount = computed(() => {
  if (!Array.isArray(actions.value)) return 0
  return actions.value.filter(a => a.status === 'feedbacked').length
})
const pendingTransportActions = computed(() => {
  if (!Array.isArray(actions.value)) return []
  return actions.value.filter(a => a.actionType === 'transport' && a.status === 'pending')
})

function showResolveMessage(type, text) {
  resolveMessage.value = { type, text }
  setTimeout(() => { resolveMessage.value = null }, 4000)
}

async function fetchActions() {
  loading.value = true
  try {
    const params = {}
    if (filterGameDay.value) params.gameDay = filterGameDay.value
    if (filterActionType.value) params.actionType = filterActionType.value
    if (filterStatus.value) params.status = filterStatus.value
    if (filterPlayerId.value) params.playerId = filterPlayerId.value
    const result = await actionAPI.getAllActions(params)
    actions.value = Array.isArray(result) ? result : []
  } catch (e) {
    console.error('获取行动列表失败:', e)
    actions.value = []
  } finally {
    loading.value = false
  }
}

async function submitFeedback() {
  if (!selectedActionId.value || !feedbackText.value.trim()) return
  try {
    const res = await actionAPI.feedbackAction(selectedActionId.value, feedbackText.value.trim())
    if (res && res.success) {
      feedbackText.value = ''
      selectedActionId.value = null
      showResolveMessage('success', '反馈提交成功')
      await fetchActions()
    } else {
      showResolveMessage('error', '反馈失败：' + (res?.message || '未知错误'))
    }
  } catch (e) {
    console.error('反馈失败:', e)
    showResolveMessage('error', '反馈提交异常')
  }
}

async function batchResolveInvestigate() {
  resolving.value = true
  try {
    const res = await actionAPI.batchResolveInvestigate(parseInt(filterGameDay.value))
    if (res) {
      showResolveMessage(res.success ? 'success' : 'error', res.message || (res.success ? '调查结算完成' : '结算失败'))
    } else {
      showResolveMessage('error', '调查结算请求失败')
    }
    await fetchActions()
  } catch (e) {
    console.error('结算失败:', e)
    showResolveMessage('error', '调查结算异常')
  } finally {
    resolving.value = false
  }
}

async function batchResolveProduce() {
  resolving.value = true
  try {
    const res = await actionAPI.batchResolveProduce(parseInt(filterGameDay.value))
    if (res) {
      showResolveMessage(res.success ? 'success' : 'error', res.message || (res.success ? '生产结算完成' : '结算失败'))
    } else {
      showResolveMessage('error', '生产结算请求失败')
    }
    await fetchActions()
  } catch (e) {
    console.error('结算失败:', e)
    showResolveMessage('error', '生产结算异常')
  } finally {
    resolving.value = false
  }
}

async function resolveTransport(actionId) {
  resolving.value = true
  try {
    const res = await actionAPI.resolveTransport(actionId)
    if (res && res.success) {
      showResolveMessage('success', '搬运结算完成')
    } else {
      showResolveMessage('error', '搬运结算失败：' + (res?.message || '未知错误'))
    }
    await fetchActions()
  } catch (e) {
    console.error('搬运结算失败:', e)
    showResolveMessage('error', '搬运结算异常')
  } finally {
    resolving.value = false
  }
}

function selectForFeedback(action) {
  selectedActionId.value = action.id
  feedbackText.value = ''
}

const transportNotesCache = new Map()

function parseTransportNotes(notes) {
  if (!notes) return null
  if (transportNotesCache.has(notes)) return transportNotesCache.get(notes)
  const info = { mode: '', source: '', dest: '', items: [] }
  const lines = notes.split('\n')
  for (const line of lines) {
    const trimmed = line.trim()
    if (trimmed.startsWith('[mode:')) {
      const closeIdx = trimmed.indexOf(']')
      if (closeIdx > 6) info.mode = trimmed.substring(6, closeIdx)
    } else if (trimmed.startsWith('[source:')) {
      const closeIdx = trimmed.indexOf(']')
      if (closeIdx > 8) info.source = trimmed.substring(8, closeIdx)
    } else if (trimmed.startsWith('[dest:')) {
      const closeIdx = trimmed.indexOf(']')
      if (closeIdx > 6) info.dest = trimmed.substring(6, closeIdx)
    } else if (trimmed.startsWith('[item:')) {
      const closeIdx = trimmed.indexOf(']')
      if (closeIdx > 6) {
        const itemStr = trimmed.substring(6, closeIdx)
        const parts = itemStr.split('|')
        if (parts.length >= 4) {
          info.items.push({ itemType: parts[0], itemId: parts[1], quantity: parts[2], weight: parts[3] })
        }
      }
    }
  }
  transportNotesCache.set(notes, info)
  if (transportNotesCache.size > 200) {
    const firstKey = transportNotesCache.keys().next().value
    transportNotesCache.delete(firstKey)
  }
  return info
}

function getTypeColor(type) {
  const colors = {
    go_location: 'bg-green-500/20 text-green-400 border-green-500/30',
    investigate_player: 'bg-yellow-500/20 text-yellow-400 border-yellow-500/30',
    produce: 'bg-blue-500/20 text-blue-400 border-blue-500/30',
    use_trait: 'bg-orange-500/20 text-orange-400 border-orange-500/30',
    use_skill: 'bg-violet-500/20 text-violet-400 border-violet-500/30',
    transport: 'bg-teal-500/20 text-teal-400 border-teal-500/30',
    hide: 'bg-purple-500/20 text-purple-400 border-purple-500/30',
  }
  return colors[type] || 'bg-gray-500/20 text-gray-400 border-gray-500/30'
}

onMounted(() => { fetchActions() })
</script>

<template>
  <div class="min-h-screen bg-[#0a0e1a] py-8 px-4 md:px-8">
    <div class="max-w-6xl mx-auto">
      <div class="text-center mb-8">
        <h1 class="text-white text-2xl md:text-3xl font-semibold tracking-tight mb-2">行动反馈</h1>
        <p class="text-gray-500 text-sm">查看玩家提交的行动并给予反馈</p>
      </div>

      <div class="bg-gradient-to-br from-[#1a2332] to-[#0f1419] border border-white/10 rounded-2xl p-5 mb-6">
        <div class="flex flex-wrap gap-3 items-end">
          <div>
            <label class="block text-gray-500 text-xs mb-1.5">天数</label>
            <select v-model="filterGameDay" @change="fetchActions" class="bg-black/30 border border-white/10 rounded-lg px-3 py-2 text-sm text-gray-200 focus:outline-none">
              <option value="1">第1天</option>
              <option value="2">第2天</option>
              <option value="3">第3天</option>
            </select>
          </div>
          <div>
            <label class="block text-gray-500 text-xs mb-1.5">行动类型</label>
            <select v-model="filterActionType" @change="fetchActions" class="bg-black/30 border border-white/10 rounded-lg px-3 py-2 text-sm text-gray-200 focus:outline-none">
              <option v-for="opt in actionTypeOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
            </select>
          </div>
          <div>
            <label class="block text-gray-500 text-xs mb-1.5">反馈状态</label>
            <select v-model="filterStatus" @change="fetchActions" class="bg-black/30 border border-white/10 rounded-lg px-3 py-2 text-sm text-gray-200 focus:outline-none">
              <option v-for="opt in statusOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
            </select>
          </div>
          <div class="flex gap-2 ml-auto">
            <button @click="batchResolveInvestigate" :disabled="resolving"
              class="px-4 py-2 bg-yellow-600/20 text-yellow-400 border border-yellow-500/30 rounded-lg text-sm hover:bg-yellow-600/30 disabled:opacity-50 transition-colors">
              结算调查
            </button>
            <button @click="batchResolveProduce" :disabled="resolving"
              class="px-4 py-2 bg-blue-600/20 text-blue-400 border border-blue-500/30 rounded-lg text-sm hover:bg-blue-600/30 disabled:opacity-50 transition-colors">
              结算生产
            </button>
          </div>
        </div>
        <div class="flex gap-4 mt-3 text-xs text-gray-500">
          <span>待反馈: <span class="text-amber-400 font-medium">{{ pendingCount }}</span></span>
          <span>已反馈: <span class="text-green-400 font-medium">{{ feedbackedCount }}</span></span>
          <span v-if="pendingTransportActions.length > 0">待结算搬运: <span class="text-teal-400 font-medium">{{ pendingTransportActions.length }}</span></span>
        </div>
      </div>

      <div v-if="resolveMessage" class="flex justify-center mb-4">
        <div :class="['px-5 py-2.5 rounded-xl text-sm font-medium transition-all', resolveMessage.type === 'success' ? 'bg-green-500/20 border border-green-500/30 text-green-400' : 'bg-red-500/20 border border-red-500/30 text-red-400']">
          {{ resolveMessage.text }}
        </div>
      </div>

      <div v-if="loading" class="flex items-center justify-center py-20">
        <div class="w-12 h-12 border-4 border-blue-500 border-t-transparent rounded-full animate-spin"></div>
      </div>

      <div v-else-if="filteredActions.length === 0" class="flex flex-col items-center justify-center py-20">
        <svg class="w-12 h-12 text-gray-600 mb-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"/>
        </svg>
        <p class="text-gray-500">暂无行动记录</p>
      </div>

      <div v-else class="space-y-4">
        <div v-for="action in filteredActions" :key="action.id"
          class="bg-gradient-to-br from-[#1a2332] to-[#0f1419] border border-white/10 rounded-2xl p-5 transition-all"
          :class="{ 'ring-1 ring-cyan-500/30': selectedActionId === action.id }">

          <div class="flex flex-wrap items-center gap-2 mb-3">
            <span class="text-white font-medium text-sm">{{ action.playerName || '未知' }}</span>
            <span class="text-xs px-1.5 py-0.5 rounded bg-white/10 text-gray-400">{{ action.playerFaction || '' }}</span>
            <span class="text-xs px-2 py-0.5 rounded-full border" :class="getTypeColor(action.actionType)">
              {{ action.actionTypeLabel || action.actionType }}
            </span>
            <span v-if="action.targetName" class="text-gray-400 text-xs">→ {{ action.targetName }}</span>
            <span v-if="action.npcName" class="text-gray-400 text-xs">💬 {{ action.npcName }}</span>
            <span class="text-gray-600 text-xs ml-1">行动{{ action.actionSlot }} · 第{{ action.gameDay }}天</span>
            <span class="ml-auto text-xs px-2 py-0.5 rounded-full"
              :class="action.status === 'pending' ? 'bg-amber-500/20 text-amber-400' : 'bg-green-500/20 text-green-400'">
              {{ action.status === 'pending' ? '待反馈' : '已反馈' }}
            </span>
          </div>

          <div v-if="action.actionType === 'transport' && action.notes" class="mb-3 rounded-xl border border-teal-500/20 bg-teal-500/5 p-3">
            <p class="text-teal-300 text-xs font-medium mb-2">搬运详情</p>
            <template v-if="parseTransportNotes(action.notes)">
              <div class="text-xs text-gray-400 space-y-1">
                <p>模式：{{ parseTransportNotes(action.notes).mode === 'warehouse_to_warehouse' ? '仓库→仓库' : '仓库→个人' }}</p>
                <p>源仓库：{{ parseTransportNotes(action.notes).source }}</p>
                <p v-if="parseTransportNotes(action.notes).dest">目标仓库：{{ parseTransportNotes(action.notes).dest }}</p>
                <div v-if="parseTransportNotes(action.notes).items && parseTransportNotes(action.notes).items.length > 0">
                  <p class="text-gray-500 mt-1">搬运物资：</p>
                  <p v-for="item in parseTransportNotes(action.notes).items" :key="`${item.itemType}-${item.itemId}`" class="ml-2">
                    {{ item.itemType }}-{{ item.itemId }} × {{ item.quantity }} ({{ item.weight }}kg/单位)
                  </p>
                </div>
              </div>
            </template>
          </div>

          <div v-else-if="action.notes" class="text-gray-500 text-xs mb-2 italic">备注：{{ action.notes }}</div>

          <div v-if="action.result" class="text-gray-300 text-sm whitespace-pre-wrap bg-black/20 rounded-xl p-4 mb-3 border border-white/5">
            {{ action.result }}
          </div>

          <div v-if="action.status === 'pending'" class="flex gap-2">
            <button v-if="action.actionType !== 'transport'" @click="selectForFeedback(action)"
              class="px-4 py-1.5 bg-cyan-600/20 text-cyan-400 border border-cyan-500/30 rounded-lg text-sm hover:bg-cyan-600/30 transition-colors">
              {{ selectedActionId === action.id ? '编辑反馈中...' : '给予反馈' }}
            </button>
            <button v-if="action.actionType === 'transport'" @click="resolveTransport(action.id)" :disabled="resolving"
              class="px-4 py-1.5 bg-teal-600/20 text-teal-400 border border-teal-500/30 rounded-lg text-sm hover:bg-teal-600/30 disabled:opacity-50 transition-colors">
              {{ resolving ? '结算中...' : '结算搬运' }}
            </button>
            <button v-if="action.actionType === 'transport'" @click="selectForFeedback(action)"
              class="px-4 py-1.5 bg-cyan-600/20 text-cyan-400 border border-cyan-500/30 rounded-lg text-sm hover:bg-cyan-600/30 transition-colors">
              手动反馈
            </button>
          </div>

          <div v-if="selectedActionId === action.id" class="mt-3 pt-3 border-t border-white/10">
            <textarea v-model="feedbackText" rows="3" placeholder="输入反馈内容..."
              class="w-full resize-none bg-black/30 border border-white/10 rounded-xl px-4 py-3 text-sm text-gray-200 placeholder:text-gray-600 focus:outline-none focus:border-cyan-500/50 mb-2"></textarea>
            <div class="flex gap-2">
              <button @click="submitFeedback"
                class="px-4 py-1.5 bg-cyan-600 text-white rounded-lg text-sm hover:bg-cyan-700 transition-colors">
                提交反馈
              </button>
              <button @click="selectedActionId = null; feedbackText = ''"
                class="px-4 py-1.5 bg-white/5 text-gray-400 rounded-lg text-sm hover:bg-white/10 transition-colors">
                取消
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
