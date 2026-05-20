<script setup>
import { ref, computed, onMounted } from 'vue'
import { quickInteractionAPI } from '@/utils/api.js'
import { FACTION_LABELS, GM_FACTION_TABS } from '@/data/factionActions.js'
import {
  INTERACTION_STATUS_OPTIONS,
  INTERACTION_TYPE_FILTER_OPTIONS,
  STATUS_BADGE_MAP,
  TYPE_BADGE_MAP,
} from '@/data/quickInteraction.js'

const interactions = ref([])
const loading = ref(true)
const filterGameDay = ref('1')
const filterFaction = ref('')
const filterStatus = ref('')
const filterType = ref('')
const replyText = ref('')
const selectedId = ref(null)
const toast = ref(null)

const filteredInteractions = computed(() =>
  Array.isArray(interactions.value) ? interactions.value : []
)

const pendingCount = computed(() =>
  interactions.value.filter(i => i.status === 'pending').length
)

function showToast(type, text) {
  toast.value = { type, text }
  setTimeout(() => { toast.value = null }, 3500)
}

async function fetchInteractions() {
  loading.value = true
  try {
    const params = {}
    if (filterGameDay.value) params.gameDay = filterGameDay.value
    if (filterFaction.value) params.faction = filterFaction.value
    if (filterStatus.value) params.status = filterStatus.value
    if (filterType.value) params.interactionType = filterType.value
    const result = await quickInteractionAPI.getAll(params)
    interactions.value = Array.isArray(result) ? result : []
  } catch {
    interactions.value = []
  } finally {
    loading.value = false
  }
}

async function submitReply() {
  if (!selectedId.value || !replyText.value.trim()) return
  try {
    const res = await quickInteractionAPI.reply(selectedId.value, replyText.value.trim())
    if (res?.success) {
      replyText.value = ''
      selectedId.value = null
      showToast('success', '回复已保存')
      await fetchInteractions()
    } else {
      showToast('error', res?.message || '回复失败')
    }
  } catch {
    showToast('error', '提交异常')
  }
}

async function markProcessed(interactionId) {
  try {
    const res = await quickInteractionAPI.updateStatus(interactionId, 'processed')
    if (res?.success) {
      showToast('success', '已标记为已处理')
      await fetchInteractions()
    } else {
      showToast('error', res?.message || '操作失败')
    }
  } catch {
    showToast('error', '操作异常')
  }
}

function formatTime(ts) {
  if (!ts) return ''
  return new Date(ts).toLocaleString('zh-CN')
}

onMounted(() => fetchInteractions())
</script>

<template>
  <div>
      <div class="text-center mb-8">
        <h1 class="text-white text-2xl md:text-3xl font-semibold tracking-tight mb-2">快速交互反馈</h1>
        <p class="text-gray-500 text-sm">查看玩家提交的快速交互并给予回复</p>
      </div>

      <div class="bg-gradient-to-br from-[#1a2332] to-[#0f1419] border border-white/10 rounded-2xl p-5 mb-6">
        <div class="flex flex-wrap gap-3 items-end">
          <div>
            <label class="block text-gray-500 text-xs mb-1.5">天数</label>
            <select v-model="filterGameDay" class="filter-select" @change="fetchInteractions">
              <option value="1">第1天</option>
              <option value="2">第2天</option>
              <option value="3">第3天</option>
            </select>
          </div>
          <div>
            <label class="block text-gray-500 text-xs mb-1.5">阵营</label>
            <select v-model="filterFaction" class="filter-select" @change="fetchInteractions">
              <option value="">全部阵营</option>
              <option v-for="f in GM_FACTION_TABS" :key="f" :value="f">{{ FACTION_LABELS[f]?.label || f }}</option>
            </select>
          </div>
          <div>
            <label class="block text-gray-500 text-xs mb-1.5">状态</label>
            <select v-model="filterStatus" class="filter-select" @change="fetchInteractions">
              <option v-for="opt in INTERACTION_STATUS_OPTIONS" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
            </select>
          </div>
          <div>
            <label class="block text-gray-500 text-xs mb-1.5">类型</label>
            <select v-model="filterType" class="filter-select" @change="fetchInteractions">
              <option v-for="opt in INTERACTION_TYPE_FILTER_OPTIONS" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
            </select>
          </div>
        </div>
        <p class="text-xs text-gray-500 mt-3">未处理：<span class="text-amber-400 font-medium">{{ pendingCount }}</span></p>
      </div>

      <div v-if="loading" class="flex justify-center py-20">
        <div class="w-12 h-12 border-4 border-violet-500 border-t-transparent rounded-full animate-spin" />
      </div>

      <div v-else-if="filteredInteractions.length === 0" class="text-center py-20 text-gray-500">暂无快速交互记录</div>

      <div v-else class="space-y-4">
        <div
          v-for="item in filteredInteractions"
          :key="item.id"
          class="bg-gradient-to-br from-[#1a2332] to-[#0f1419] border border-white/10 rounded-2xl p-5"
          :class="{ 'ring-1 ring-violet-500/30': selectedId === item.id }"
        >
          <div class="flex flex-wrap items-center gap-2 mb-3">
            <span class="text-white font-medium text-sm">{{ item.playerName }}</span>
            <span
              class="text-xs px-2 py-0.5 rounded-full border"
              :class="FACTION_LABELS[item.faction]?.color || 'bg-white/10 text-gray-400'"
            >
              {{ FACTION_LABELS[item.faction]?.label || item.faction }}
            </span>
            <span
              class="text-xs px-2 py-0.5 rounded-full"
              :class="TYPE_BADGE_MAP[item.interactionType]?.color || 'bg-white/10 text-gray-400'"
            >
              {{ item.interactionTypeLabel || TYPE_BADGE_MAP[item.interactionType]?.text || item.interactionType }}
            </span>
            <span class="text-gray-600 text-xs">第{{ item.gameDay }}天</span>
            <span class="text-gray-600 text-xs">{{ formatTime(item.createdAt) }}</span>
            <span
              class="ml-auto text-xs px-2 py-0.5 rounded-full"
              :class="STATUS_BADGE_MAP[item.status]?.color || 'bg-gray-500/20 text-gray-400'"
            >
              {{ STATUS_BADGE_MAP[item.status]?.text || item.status }}
            </span>
          </div>

          <div class="text-gray-300 text-sm whitespace-pre-wrap bg-black/20 rounded-xl p-4 mb-3 border border-white/5">
            {{ item.content }}
          </div>

          <div
            v-if="item.dmReply"
            class="text-green-300 text-sm whitespace-pre-wrap bg-green-500/5 border border-green-500/20 rounded-xl p-4 mb-3"
          >
            <span class="text-green-400 font-medium">DM回复：</span>{{ item.dmReply }}
            <p v-if="item.repliedAt" class="text-gray-600 text-xs mt-1">{{ formatTime(item.repliedAt) }}</p>
          </div>

          <div v-if="item.status !== 'replied'" class="flex gap-2">
            <button
              v-if="item.status === 'pending'"
              type="button"
              class="px-4 py-1.5 bg-blue-600/20 text-blue-300 border border-blue-500/30 rounded-lg text-sm hover:bg-blue-600/30"
              @click="markProcessed(item.id)"
            >
              标记已处理
            </button>
            <button
              type="button"
              class="px-4 py-1.5 bg-violet-600/20 text-violet-300 border border-violet-500/30 rounded-lg text-sm hover:bg-violet-600/30"
              @click="selectedId = item.id; replyText = item.dmReply || ''"
            >
              {{ selectedId === item.id ? '填写回复中...' : '回复' }}
            </button>
          </div>

          <div v-if="selectedId === item.id" class="mt-3 pt-3 border-t border-white/10">
            <textarea
              v-model="replyText"
              rows="4"
              placeholder="填写DM回复内容..."
              class="w-full resize-none bg-black/30 border border-white/10 rounded-xl px-4 py-3 text-sm text-gray-200 mb-2 focus:outline-none focus:border-violet-500/50"
            />
            <div class="flex gap-2">
              <button type="button" class="px-4 py-1.5 bg-violet-600 text-white rounded-lg text-sm" @click="submitReply">
                保存回复
              </button>
              <button
                type="button"
                class="px-4 py-1.5 bg-white/5 text-gray-400 rounded-lg text-sm"
                @click="selectedId = null; replyText = ''"
              >
                取消
              </button>
            </div>
          </div>
        </div>
      </div>

      <div
        v-if="toast"
        class="fixed bottom-6 left-1/2 -translate-x-1/2 z-50 px-5 py-2.5 rounded-xl text-sm text-white"
        :class="toast.type === 'success' ? 'bg-green-600' : 'bg-red-600'"
      >
        {{ toast.text }}
      </div>
  </div>
</template>

<style scoped>
.filter-select {
  @apply bg-black/30 border border-white/10 rounded-lg px-3 py-2 text-sm text-gray-200 focus:outline-none;
}
</style>
