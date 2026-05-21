<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { dmActivityLogAPI, gameStateAPI, playerAPI } from '@/utils/api.js'
import { formatTransportNotesForDisplay } from '@/data/gameData.js'

const userRole = (localStorage.getItem('userRole') || '').toLowerCase()
const entries = ref([])
const loading = ref(false)
const error = ref('')
const filterDay = ref(null)
const filterPlayerId = ref('')
const filterFaction = ref('')
const gameDay = ref(1)
const players = ref([])
let pollTimer = null

const FACTIONS = ['统治者', '反叛者', '冒险者', '天灾使者', '平民']

const CATEGORY_LABELS = {
  action: '自由',
  faction: '阵营',
  night: '夜晚',
  consume: '消耗',
  trade: '交易',
  quick: '快交',
}

function categoryLabel(cat) {
  return CATEGORY_LABELS[cat] || cat || '—'
}

function formatTime(iso) {
  if (!iso) return '—'
  const d = new Date(iso)
  if (Number.isNaN(d.getTime())) return String(iso).slice(11, 19)
  const h = String(d.getHours()).padStart(2, '0')
  const m = String(d.getMinutes()).padStart(2, '0')
  const s = String(d.getSeconds()).padStart(2, '0')
  return `${h}:${m}:${s}`
}

function formatDetail(text) {
  if (!text) return ''
  if (String(text).includes('[mode:')) {
    const zh = formatTransportNotesForDisplay(text)
    if (zh) return zh
  }
  return text
}

const displayEntries = computed(() => entries.value)

async function fetchLogs() {
  loading.value = true
  error.value = ''
  try {
    const data = await dmActivityLogAPI.list({
      userRole,
      gameDay: filterDay.value || undefined,
      playerId: filterPlayerId.value ? parseInt(filterPlayerId.value, 10) : undefined,
      faction: filterFaction.value || undefined,
      limit: 500,
    })
    if (data?.success) {
      entries.value = data.entries || []
    } else {
      error.value = data?.message || '加载失败'
      entries.value = []
    }
  } catch {
    error.value = '无法连接后端'
    entries.value = []
  } finally {
    loading.value = false
  }
}

async function loadGameDay() {
  try {
    const gs = await gameStateAPI.get()
    if (gs?.currentDay) gameDay.value = gs.currentDay
  } catch {
    /* ignore */
  }
}

async function loadPlayers() {
  try {
    const list = await playerAPI.getAll()
    players.value = Array.isArray(list) ? list : []
  } catch {
    players.value = []
  }
}

onMounted(async () => {
  await loadGameDay()
  await loadPlayers()
  filterDay.value = gameDay.value
  await fetchLogs()
  pollTimer = setInterval(fetchLogs, 12000)
})

onUnmounted(() => {
  if (pollTimer) clearInterval(pollTimer)
})
</script>

<template>
  <div>
    <div class="flex flex-wrap items-center justify-between gap-3 mb-4">
      <div>
        <h1 class="text-white text-xl font-semibold tracking-tight">系统日志</h1>
        <p class="text-gray-500 text-xs mt-0.5">玩家提交并写入数据库的操作（自动刷新）</p>
      </div>
      <div class="flex flex-wrap items-center gap-2 text-xs">
        <label class="text-gray-500">天数</label>
        <input
          v-model.number="filterDay"
          type="number"
          min="1"
          class="w-16 bg-[#0f1419] border border-[#1f2937] rounded px-2 py-1 text-white tabular-nums"
        />
        <button
          type="button"
          class="px-2 py-1 rounded bg-[#2d4263] text-white hover:bg-[#3d5273]"
          @click="filterDay = null"
        >
          全部
        </button>
        <label class="text-gray-500 ml-1">玩家</label>
        <select
          v-model="filterPlayerId"
          class="bg-[#0f1419] border border-[#1f2937] rounded px-2 py-1 text-white max-w-[120px]"
        >
          <option value="">全部</option>
          <option v-for="p in players" :key="p.id" :value="p.id">{{ p.name }}</option>
        </select>
        <label class="text-gray-500">阵营</label>
        <select
          v-model="filterFaction"
          class="bg-[#0f1419] border border-[#1f2937] rounded px-2 py-1 text-white"
        >
          <option value="">全部</option>
          <option v-for="f in FACTIONS" :key="f" :value="f">{{ f }}</option>
        </select>
        <button
          type="button"
          class="px-3 py-1 rounded bg-cyan-700/80 text-white hover:bg-cyan-600 disabled:opacity-50"
          :disabled="loading"
          @click="fetchLogs"
        >
          {{ loading ? '…' : '刷新' }}
        </button>
      </div>
    </div>

    <p v-if="error" class="text-red-400 text-xs mb-2">{{ error }}</p>

    <div
      class="bg-[#0a0e14] border border-[#1f2937] rounded-lg overflow-hidden font-mono text-[10px] leading-[1.35] max-h-[calc(100vh-220px)] overflow-y-auto"
    >
      <div
        v-if="!loading && displayEntries.length === 0"
        class="text-gray-500 text-center py-8 text-xs font-sans"
      >
        暂无日志记录
      </div>

      <div
        v-for="e in displayEntries"
        :key="e.id"
        class="border-b border-[#151b24] px-2 py-[3px] hover:bg-[#111820] group"
      >
        <div class="flex gap-1.5 items-baseline min-w-0">
          <span class="text-gray-600 shrink-0 w-[52px]">{{ formatTime(e.createdAt) }}</span>
          <span class="text-cyan-700/90 shrink-0 w-[22px]">D{{ e.gameDay }}</span>
          <span class="text-amber-400/90 shrink-0 max-w-[72px] truncate" :title="e.playerName">{{ e.playerName || '—' }}</span>
          <span class="text-slate-500 shrink-0 max-w-[40px] truncate" :title="e.playerFaction">{{ e.playerFaction || '—' }}</span>
          <span class="text-violet-400/70 shrink-0 w-[28px]">{{ categoryLabel(e.category) }}</span>
          <span class="text-gray-300 flex-1 min-w-0 truncate" :title="e.summary">{{ e.summary }}</span>
        </div>
        <div
          v-if="e.detail"
          class="text-gray-600 pl-[106px] truncate group-hover:whitespace-normal group-hover:break-words group-hover:text-gray-500"
          :title="formatDetail(e.detail)"
        >
          {{ formatDetail(e.detail) }}
        </div>
      </div>
    </div>
  </div>
</template>
