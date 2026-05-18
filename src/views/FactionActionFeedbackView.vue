<script setup>
import { ref, computed, onMounted } from 'vue'
import { factionActionAPI } from '@/utils/api.js'
import { FACTION_LABELS, GM_FACTION_TABS, PAYLOAD_FIELD_LABELS } from '@/data/factionActions.js'

const actions = ref([])
const loading = ref(true)
const filterGameDay = ref('1')
const filterFaction = ref('')
const filterStatus = ref('')
const feedbackText = ref('')
const selectedActionId = ref(null)
const toast = ref(null)

const statusOptions = [
  { value: '', label: '全部状态' },
  { value: 'pending', label: '待反馈' },
  { value: 'feedbacked', label: '已反馈' },
]

const filteredActions = computed(() => (Array.isArray(actions.value) ? actions.value : []))
const pendingCount = computed(() => actions.value.filter(a => a.status === 'pending').length)

function showToast(type, text) {
  toast.value = { type, text }
  setTimeout(() => { toast.value = null }, 3500)
}

async function fetchActions() {
  loading.value = true
  try {
    const params = {}
    if (filterGameDay.value) params.gameDay = filterGameDay.value
    if (filterFaction.value) params.faction = filterFaction.value
    if (filterStatus.value) params.status = filterStatus.value
    const result = await factionActionAPI.getAllActions(params)
    actions.value = Array.isArray(result) ? result : []
  } catch (e) {
    actions.value = []
  } finally {
    loading.value = false
  }
}

async function submitFeedback() {
  if (!selectedActionId.value || !feedbackText.value.trim()) return
  try {
    const res = await factionActionAPI.feedbackAction(selectedActionId.value, feedbackText.value.trim())
    if (res?.success) {
      feedbackText.value = ''
      selectedActionId.value = null
      showToast('success', '反馈已保存')
      await fetchActions()
    } else {
      showToast('error', res?.message || '反馈失败')
    }
  } catch (e) {
    showToast('error', '反馈提交异常')
  }
}

function formatPayload(payload) {
  if (!payload || typeof payload !== 'object') return ''
  return Object.entries(payload)
    .filter(([, v]) => v != null && v !== '')
    .map(([k, v]) => {
      const label = PAYLOAD_FIELD_LABELS[k] || k
      const val = Array.isArray(v) ? v.join('、') : (v === true ? '是' : v === false ? '否' : v)
      return `${label}：${val}`
    })
    .join('\n')
}

onMounted(() => fetchActions())
</script>

<template>
  <div class="min-h-screen bg-[#0a0e1a] py-8 px-4 md:px-8">
    <div class="max-w-6xl mx-auto">
      <div class="text-center mb-8">
        <h1 class="text-white text-2xl md:text-3xl font-semibold tracking-tight mb-2">阵营行动反馈</h1>
        <p class="text-gray-500 text-sm">查看玩家提交的阵营行动并给予反馈</p>
      </div>

      <div class="bg-gradient-to-br from-[#1a2332] to-[#0f1419] border border-white/10 rounded-2xl p-5 mb-6">
        <div class="flex flex-wrap gap-3 items-end">
          <div>
            <label class="block text-gray-500 text-xs mb-1.5">天数</label>
            <select v-model="filterGameDay" class="filter-select" @change="fetchActions">
              <option value="1">第1天</option>
              <option value="2">第2天</option>
              <option value="3">第3天</option>
            </select>
          </div>
          <div>
            <label class="block text-gray-500 text-xs mb-1.5">阵营</label>
            <select v-model="filterFaction" class="filter-select" @change="fetchActions">
              <option value="">全部阵营</option>
              <option v-for="f in GM_FACTION_TABS" :key="f" :value="f">{{ FACTION_LABELS[f]?.label || f }}</option>
            </select>
          </div>
          <div>
            <label class="block text-gray-500 text-xs mb-1.5">状态</label>
            <select v-model="filterStatus" class="filter-select" @change="fetchActions">
              <option v-for="opt in statusOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
            </select>
          </div>
        </div>
        <p class="text-xs text-gray-500 mt-3">待反馈：<span class="text-amber-400 font-medium">{{ pendingCount }}</span></p>
      </div>

      <div v-if="loading" class="flex justify-center py-20">
        <div class="w-12 h-12 border-4 border-blue-500 border-t-transparent rounded-full animate-spin" />
      </div>

      <div v-else-if="filteredActions.length === 0" class="text-center py-20 text-gray-500">暂无阵营行动记录</div>

      <div v-else class="space-y-4">
        <div
          v-for="action in filteredActions"
          :key="action.id"
          class="bg-gradient-to-br from-[#1a2332] to-[#0f1419] border border-white/10 rounded-2xl p-5"
          :class="{ 'ring-1 ring-cyan-500/30': selectedActionId === action.id }"
        >
          <div class="flex flex-wrap items-center gap-2 mb-3">
            <span class="text-white font-medium text-sm">{{ action.playerName }}</span>
            <span class="text-xs px-2 py-0.5 rounded-full border" :class="FACTION_LABELS[action.faction]?.color || 'bg-white/10 text-gray-400'">
              {{ FACTION_LABELS[action.faction]?.label || action.faction }}
            </span>
            <span class="text-xs px-2 py-0.5 rounded-full bg-white/10 text-gray-300">{{ action.actionTypeLabel }}</span>
            <span class="text-gray-600 text-xs">第{{ action.gameDay }}天</span>
            <span
              class="ml-auto text-xs px-2 py-0.5 rounded-full"
              :class="action.status === 'pending' ? 'bg-amber-500/20 text-amber-400' : 'bg-green-500/20 text-green-400'"
            >
              {{ action.status === 'pending' ? '待反馈' : '已反馈' }}
            </span>
          </div>

          <pre v-if="action.payload && Object.keys(action.payload).length" class="text-gray-500 text-xs mb-2 whitespace-pre-wrap font-sans">输入详情：
{{ formatPayload(action.payload) }}</pre>

          <div v-if="action.result" class="text-gray-300 text-sm whitespace-pre-wrap bg-black/20 rounded-xl p-4 mb-3 border border-white/5">
            {{ action.result }}
          </div>

          <div v-if="action.status === 'pending'" class="flex gap-2">
            <button
              type="button"
              class="px-4 py-1.5 bg-cyan-600/20 text-cyan-400 border border-cyan-500/30 rounded-lg text-sm hover:bg-cyan-600/30"
              @click="selectedActionId = action.id; feedbackText = ''"
            >
              {{ selectedActionId === action.id ? '编辑反馈中...' : '给予反馈' }}
            </button>
          </div>

          <div v-if="selectedActionId === action.id" class="mt-3 pt-3 border-t border-white/10">
            <textarea v-model="feedbackText" rows="3" placeholder="输入反馈内容..." class="w-full resize-none bg-black/30 border border-white/10 rounded-xl px-4 py-3 text-sm text-gray-200 mb-2 focus:outline-none focus:border-cyan-500/50" />
            <div class="flex gap-2">
              <button type="button" class="px-4 py-1.5 bg-cyan-600 text-white rounded-lg text-sm" @click="submitFeedback">提交反馈</button>
              <button type="button" class="px-4 py-1.5 bg-white/5 text-gray-400 rounded-lg text-sm" @click="selectedActionId = null; feedbackText = ''">取消</button>
            </div>
          </div>
        </div>
      </div>

      <div v-if="toast" class="fixed bottom-6 left-1/2 -translate-x-1/2 z-50 px-5 py-2.5 rounded-xl text-sm text-white" :class="toast.type === 'success' ? 'bg-green-600' : 'bg-red-600'">
        {{ toast.text }}
      </div>
    </div>
  </div>
</template>

<style scoped>
.filter-select {
  @apply bg-black/30 border border-white/10 rounded-lg px-3 py-2 text-sm text-gray-200 focus:outline-none;
}
</style>
