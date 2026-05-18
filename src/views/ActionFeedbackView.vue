<script setup>
import { ref, computed, onMounted } from 'vue'
import { actionAPI, playerAPI } from '@/utils/api.js'
import {
  formatActionResultText,
  formatTransportItemLine,
  extractDmFeedback,
  stripDmFeedbackFromResult,
  actionShortLabel,
  buildPlayerFeedbackSummary,
} from '@/data/gameData.js'

const actions = ref([])
const players = ref([])
const loading = ref(true)
const filterGameDay = ref('1')
const resolving = ref(false)
const publishing = ref(false)
const resolveMessage = ref(null)

const modalMode = ref(null)
const modalAction = ref(null)
const modalPlayerRow = ref(null)
const feedbackText = ref('')
const summaryText = ref('')
const savingFeedback = ref(false)

function showResolveMessage(type, text) {
  resolveMessage.value = { type, text }
  setTimeout(() => { resolveMessage.value = null }, 4000)
}

async function fetchActions() {
  loading.value = true
  try {
    const result = await actionAPI.getAllActions({ gameDay: filterGameDay.value })
    actions.value = Array.isArray(result) ? result : []
  } catch (e) {
    console.error('获取行动列表失败:', e)
    actions.value = []
  } finally {
    loading.value = false
  }
}

async function fetchPlayers() {
  try {
    const list = await playerAPI.getAll()
    players.value = Array.isArray(list) ? list : []
  } catch (e) {
    console.error('获取玩家列表失败:', e)
    players.value = []
  }
}

const dayActions = computed(() =>
  actions.value.filter((a) => String(a.gameDay) === String(filterGameDay.value)),
)

const playerRows = computed(() => {
  const day = filterGameDay.value
  const byId = new Map()
  for (const p of players.value) {
    const id = p.id ?? p.playerId
    if (id == null) continue
    byId.set(id, {
      playerId: id,
      playerName: p.name || p.playerName || `玩家${id}`,
      faction: typeof p.faction === 'object' ? (p.faction?.name ?? '') : (p.faction ?? ''),
      slot1: null,
      slot2: null,
    })
  }
  for (const a of dayActions.value) {
    let row = byId.get(a.playerId)
    if (!row) {
      row = {
        playerId: a.playerId,
        playerName: a.playerName || `玩家${a.playerId}`,
        faction: a.playerFaction || '',
        slot1: null,
        slot2: null,
      }
      byId.set(a.playerId, row)
    }
    if (a.actionSlot === 1) row.slot1 = a
    if (a.actionSlot === 2) row.slot2 = a
  }
  return [...byId.values()].sort((a, b) => a.playerName.localeCompare(b.playerName, 'zh'))
})

const pendingCount = computed(() => dayActions.value.filter((a) => a.status === 'pending').length)
const savedCount = computed(() => dayActions.value.filter((a) => isActionSaved(a)).length)
const unpublishedCount = computed(() =>
  dayActions.value.filter((a) => a.status === 'feedbacked' && !a.feedbackPublished).length,
)
const allPublished = computed(() => {
  const feedbacked = dayActions.value.filter((a) => a.status === 'feedbacked')
  return feedbacked.length > 0 && feedbacked.every((a) => a.feedbackPublished)
})

function isActionSaved(action) {
  if (!action) return false
  if (action.status === 'feedbacked') return true
  return Boolean(extractDmFeedback(action.result) || action.dmFeedback)
}

function openActionModal(action) {
  if (!action) return
  modalMode.value = 'action'
  modalAction.value = action
  modalPlayerRow.value = null
  feedbackText.value = extractDmFeedback(action.result) || action.dmFeedback || ''
}

function openSummaryModal(row) {
  modalMode.value = 'summary'
  modalPlayerRow.value = row
  modalAction.value = null
  summaryText.value = buildPlayerFeedbackSummary(
    row.playerName,
    filterGameDay.value,
    row.slot1,
    row.slot2,
  )
}

function closeModal() {
  modalMode.value = null
  modalAction.value = null
  modalPlayerRow.value = null
  feedbackText.value = ''
}

function canApproveAction(action) {
  return action?.status === 'pending' && ['go_location', 'hide'].includes(action.actionType)
}

async function approveAction(actionId) {
  resolving.value = true
  try {
    const res = await actionAPI.approveAction(actionId)
    if (res?.success) {
      showResolveMessage('success', res.message || '行动已确认')
      await fetchActions()
      closeModal()
    } else {
      showResolveMessage('error', res?.message || '确认失败')
    }
  } catch {
    showResolveMessage('error', '确认行动异常')
  } finally {
    resolving.value = false
  }
}

async function resolveTransport(actionId) {
  resolving.value = true
  try {
    const res = await actionAPI.resolveTransport(actionId)
    if (res?.success) {
      showResolveMessage('success', '搬运结算完成')
      await fetchActions()
      if (modalAction.value?.id === actionId) {
        const updated = actions.value.find((a) => a.id === actionId)
        if (updated) openActionModal(updated)
      }
    } else {
      showResolveMessage('error', res?.message || '搬运结算失败')
    }
  } catch {
    showResolveMessage('error', '搬运结算异常')
  } finally {
    resolving.value = false
  }
}

async function saveModalFeedback() {
  if (!modalAction.value || !feedbackText.value.trim()) return
  savingFeedback.value = true
  try {
    const res = await actionAPI.feedbackAction(modalAction.value.id, feedbackText.value.trim())
    if (res?.success) {
      showResolveMessage('success', '反馈已保存')
      await fetchActions()
      closeModal()
    } else {
      showResolveMessage('error', res?.message || '保存失败')
    }
  } catch {
    showResolveMessage('error', '保存反馈异常')
  } finally {
    savingFeedback.value = false
  }
}

async function batchResolveInvestigate() {
  resolving.value = true
  try {
    const res = await actionAPI.batchResolveInvestigate(parseInt(filterGameDay.value, 10))
    showResolveMessage(res?.success ? 'success' : 'error', res?.message || (res?.success ? '调查结算完成' : '结算失败'))
    await fetchActions()
  } catch {
    showResolveMessage('error', '调查结算异常')
  } finally {
    resolving.value = false
  }
}

async function batchResolveProduce() {
  resolving.value = true
  try {
    const res = await actionAPI.batchResolveProduce(parseInt(filterGameDay.value, 10))
    showResolveMessage(res?.success ? 'success' : 'error', res?.message || (res?.success ? '生产结算完成' : '结算失败'))
    await fetchActions()
  } catch {
    showResolveMessage('error', '生产结算异常')
  } finally {
    resolving.value = false
  }
}

async function publishAllFeedback() {
  if (publishing.value) return
  if (!confirm(`确定发布第 ${filterGameDay.value} 天所有已保存的行动反馈？玩家将可在行动页查看结果。`)) return
  publishing.value = true
  try {
    const res = await actionAPI.publishFeedback(parseInt(filterGameDay.value, 10))
    showResolveMessage(res?.success ? 'success' : 'error', res?.message || (res?.success ? '已发布' : '发布失败'))
    await fetchActions()
  } catch {
    showResolveMessage('error', '发布反馈异常')
  } finally {
    publishing.value = false
  }
}

async function copySummary() {
  try {
    await navigator.clipboard.writeText(summaryText.value)
    showResolveMessage('success', '已复制到剪贴板')
  } catch {
    showResolveMessage('error', '复制失败，请手动选择文本复制')
  }
}

const transportNotesCache = new Map()

function parseTransportNotes(notes) {
  if (!notes) return null
  if (transportNotesCache.has(notes)) return transportNotesCache.get(notes)
  const info = { mode: '', source: '', dest: '', items: [] }
  for (const line of notes.split('\n')) {
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
        const parts = trimmed.substring(6, closeIdx).split('|')
        if (parts.length >= 4) {
          info.items.push({ itemType: parts[0], itemId: parts[1], quantity: parts[2], weight: parts[3] })
        }
      }
    }
  }
  transportNotesCache.set(notes, info)
  if (transportNotesCache.size > 200) {
    transportNotesCache.delete(transportNotesCache.keys().next().value)
  }
  return info
}

function systemResultPreview(action) {
  if (!action?.result) return ''
  return formatActionResultText(stripDmFeedbackFromResult(action.result))
}

onMounted(async () => {
  await Promise.all([fetchPlayers(), fetchActions()])
})
</script>

<template>
  <div class="min-h-screen bg-[#0a0e1a] py-6 px-4 md:px-8 pb-28">
    <div class="max-w-5xl mx-auto">
      <div class="flex flex-col sm:flex-row sm:items-end sm:justify-between gap-4 mb-6">
        <div>
          <h1 class="text-white text-2xl font-semibold tracking-tight">行动反馈</h1>
          <p class="text-gray-500 text-sm mt-1">按玩家处理两个行动，保存后可发布给玩家查看</p>
        </div>
        <div class="flex flex-wrap items-center gap-2">
          <select
            v-model="filterGameDay"
            class="bg-black/30 border border-white/10 rounded-lg px-3 py-2 text-sm text-gray-200"
            @change="fetchActions"
          >
            <option value="1">第1天</option>
            <option value="2">第2天</option>
            <option value="3">第3天</option>
          </select>
          <button
            type="button"
            class="px-3 py-2 text-xs rounded-lg bg-yellow-600/20 text-yellow-400 border border-yellow-500/30 hover:bg-yellow-600/30 disabled:opacity-50"
            :disabled="resolving"
            @click="batchResolveInvestigate"
          >
            结算调查
          </button>
          <button
            type="button"
            class="px-3 py-2 text-xs rounded-lg bg-blue-600/20 text-blue-400 border border-blue-500/30 hover:bg-blue-600/30 disabled:opacity-50"
            :disabled="resolving"
            @click="batchResolveProduce"
          >
            结算生产
          </button>
        </div>
      </div>

      <div class="flex flex-wrap gap-4 text-xs text-gray-500 mb-4">
        <span>待处理: <span class="text-amber-400 font-medium">{{ pendingCount }}</span></span>
        <span>已保存: <span class="text-emerald-400 font-medium">{{ savedCount }}</span></span>
        <span>待发布: <span class="text-cyan-400 font-medium">{{ unpublishedCount }}</span></span>
        <span v-if="allPublished" class="text-emerald-400">当日反馈已全部发布</span>
      </div>

      <div v-if="resolveMessage" class="mb-4">
        <div
          :class="[
            'px-4 py-2 rounded-xl text-sm inline-block',
            resolveMessage.type === 'success'
              ? 'bg-green-500/20 border border-green-500/30 text-green-400'
              : 'bg-red-500/20 border border-red-500/30 text-red-400',
          ]"
        >
          {{ resolveMessage.text }}
        </div>
      </div>

      <div v-if="loading" class="flex justify-center py-16">
        <div class="w-10 h-10 border-4 border-cyan-500 border-t-transparent rounded-full animate-spin" />
      </div>

      <div
        v-else-if="!playerRows.length"
        class="text-center py-16 text-gray-500 text-sm border border-dashed border-white/10 rounded-2xl"
      >
        第 {{ filterGameDay }} 天暂无玩家数据
      </div>

      <div v-else class="space-y-2">
        <div
          v-for="row in playerRows"
          :key="row.playerId"
          class="flex flex-col sm:flex-row sm:items-center gap-3 px-4 py-3 rounded-xl border border-white/10 bg-[#1a2332]/80"
        >
          <div class="min-w-[120px] shrink-0">
            <span class="text-white text-sm font-medium">{{ row.playerName }}</span>
            <span v-if="row.faction" class="text-gray-500 text-xs ml-1.5">{{ row.faction }}</span>
          </div>
          <div class="flex flex-wrap items-center gap-2 flex-1">
            <button
              type="button"
              class="relative inline-flex items-center gap-1.5 px-3 py-2 rounded-lg border text-xs font-medium transition-colors max-w-[200px]"
              :class="row.slot1
                ? 'border-white/15 bg-black/25 text-gray-200 hover:border-cyan-500/40 hover:bg-cyan-500/10'
                : 'border-white/5 bg-black/10 text-gray-600 cursor-not-allowed'"
              :disabled="!row.slot1"
              @click="row.slot1 && openActionModal(row.slot1)"
            >
              <span
                v-if="row.slot1 && isActionSaved(row.slot1)"
                class="text-emerald-400 shrink-0"
                title="已保存反馈"
              >✓</span>
              <span class="truncate">行动一{{ row.slot1 ? `：${actionShortLabel(row.slot1)}` : '' }}</span>
            </button>
            <button
              type="button"
              class="relative inline-flex items-center gap-1.5 px-3 py-2 rounded-lg border text-xs font-medium transition-colors max-w-[200px]"
              :class="row.slot2
                ? 'border-white/15 bg-black/25 text-gray-200 hover:border-cyan-500/40 hover:bg-cyan-500/10'
                : 'border-white/5 bg-black/10 text-gray-600 cursor-not-allowed'"
              :disabled="!row.slot2"
              @click="row.slot2 && openActionModal(row.slot2)"
            >
              <span
                v-if="row.slot2 && isActionSaved(row.slot2)"
                class="text-emerald-400 shrink-0"
                title="已保存反馈"
              >✓</span>
              <span class="truncate">行动二{{ row.slot2 ? `：${actionShortLabel(row.slot2)}` : '' }}</span>
            </button>
            <button
              type="button"
              class="px-3 py-2 rounded-lg border border-violet-500/30 bg-violet-500/10 text-violet-300 text-xs hover:bg-violet-500/20 transition-colors shrink-0"
              @click="openSummaryModal(row)"
            >
              反馈总结
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 发布反馈 -->
    <div class="fixed bottom-0 left-0 right-0 z-40 border-t border-white/10 bg-[#0f1419]/95 backdrop-blur-md px-4 py-4">
      <div class="max-w-5xl mx-auto flex flex-col sm:flex-row items-center justify-between gap-3">
        <p class="text-gray-500 text-xs text-center sm:text-left">
          保存各行动反馈后，点击下方按钮一次性向玩家公开第 {{ filterGameDay }} 天结果
        </p>
        <button
          type="button"
          class="w-full sm:w-auto px-8 py-3 rounded-xl bg-emerald-600 hover:bg-emerald-500 text-white font-medium text-sm disabled:opacity-50 transition-colors shadow-lg shadow-emerald-900/30"
          :disabled="publishing || unpublishedCount === 0"
          @click="publishAllFeedback"
        >
          {{ publishing ? '发布中…' : allPublished ? '已发布' : '发布反馈' }}
        </button>
      </div>
    </div>

    <!-- 行动详情 / 反馈弹窗 -->
    <div
      v-if="modalMode === 'action' && modalAction"
      class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/75"
      @click.self="closeModal"
    >
      <div class="bg-[#1a2332] border border-white/10 rounded-2xl w-full max-w-2xl max-h-[92vh] flex flex-col shadow-2xl">
        <div class="p-4 border-b border-white/10 flex justify-between items-start gap-3">
          <div>
            <h3 class="text-white font-medium">{{ modalAction.playerName }}</h3>
            <p class="text-cyan-400/90 text-sm mt-0.5">
              行动{{ modalAction.actionSlot }} · {{ actionShortLabel(modalAction) }}
            </p>
          </div>
          <button type="button" class="text-gray-500 hover:text-white shrink-0" @click="closeModal">✕</button>
        </div>

        <div class="flex-1 overflow-y-auto p-4 space-y-3 text-sm">
          <p
            v-if="modalAction.playerIsShelterLaborer"
            class="text-xs text-amber-300/95 rounded-lg border border-amber-500/30 bg-amber-500/10 px-3 py-2"
          >
            {{ modalAction.laborerDmWarning }}
          </p>

          <div v-if="modalAction.actionType === 'transport' && modalAction.notes" class="rounded-lg border border-teal-500/20 bg-teal-500/5 p-3 text-xs text-gray-400">
            <p class="text-teal-300 font-medium mb-1">搬运</p>
            <template v-if="parseTransportNotes(modalAction.notes)">
              <p>模式：{{ parseTransportNotes(modalAction.notes).mode === 'warehouse_to_warehouse' ? '仓库→仓库' : '仓库→个人' }}</p>
              <p>源：{{ parseTransportNotes(modalAction.notes).source }}</p>
              <p v-if="parseTransportNotes(modalAction.notes).dest">目标：{{ parseTransportNotes(modalAction.notes).dest }}</p>
              <p v-for="item in parseTransportNotes(modalAction.notes).items" :key="`${item.itemType}-${item.itemId}`" class="ml-2">
                {{ formatTransportItemLine(item) }}
              </p>
            </template>
          </div>
          <p v-else-if="modalAction.notes" class="text-gray-500 text-xs italic">备注：{{ modalAction.notes }}</p>

          <div v-if="systemResultPreview(modalAction)" class="text-gray-400 text-xs whitespace-pre-wrap bg-black/20 rounded-lg p-3 border border-white/5 max-h-40 overflow-y-auto">
            {{ systemResultPreview(modalAction) }}
          </div>

          <div>
            <label class="block text-gray-400 text-xs mb-1.5">DM 反馈（发给玩家）</label>
            <textarea
              v-model="feedbackText"
              rows="7"
              placeholder="输入反馈内容…"
              class="w-full resize-none bg-black/30 border border-white/10 rounded-xl px-3 py-2 text-sm text-gray-200 focus:outline-none focus:border-cyan-500/50"
            />
          </div>
        </div>

        <div class="p-4 border-t border-white/10 flex flex-wrap gap-2">
          <button
            v-if="canApproveAction(modalAction)"
            type="button"
            class="px-3 py-1.5 rounded-lg bg-emerald-600/20 text-emerald-400 border border-emerald-500/30 text-xs disabled:opacity-50"
            :disabled="resolving"
            @click="approveAction(modalAction.id)"
          >
            确认行动
          </button>
          <button
            v-if="modalAction.actionType === 'transport' && modalAction.status === 'pending'"
            type="button"
            class="px-3 py-1.5 rounded-lg bg-teal-600/20 text-teal-400 border border-teal-500/30 text-xs disabled:opacity-50"
            :disabled="resolving"
            @click="resolveTransport(modalAction.id)"
          >
            结算搬运
          </button>
          <div class="flex-1" />
          <button type="button" class="px-4 py-1.5 text-gray-400 text-sm rounded-lg hover:bg-white/5" @click="closeModal">
            取消
          </button>
          <button
            type="button"
            class="px-4 py-1.5 bg-cyan-600 text-white rounded-lg text-sm disabled:opacity-50"
            :disabled="savingFeedback || !feedbackText.trim()"
            @click="saveModalFeedback"
          >
            {{ savingFeedback ? '保存中…' : '保存反馈' }}
          </button>
        </div>
      </div>
    </div>

    <!-- 反馈总结弹窗 -->
    <div
      v-if="modalMode === 'summary' && modalPlayerRow"
      class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/75"
      @click.self="closeModal"
    >
      <div class="bg-[#1a2332] border border-white/10 rounded-2xl w-full max-w-2xl max-h-[88vh] flex flex-col shadow-2xl">
        <div class="p-4 border-b border-white/10 flex justify-between items-center">
          <h3 class="text-white font-medium">{{ modalPlayerRow.playerName }} · 反馈总结</h3>
          <button type="button" class="text-gray-500 hover:text-white" @click="closeModal">✕</button>
        </div>
        <div class="flex-1 overflow-y-auto p-4">
          <textarea
            v-model="summaryText"
            rows="16"
            readonly
            class="w-full resize-none bg-black/30 border border-white/10 rounded-xl px-3 py-2 text-sm text-gray-200 font-mono leading-relaxed"
          />
        </div>
        <div class="p-4 border-t border-white/10 flex gap-2 justify-end">
          <button type="button" class="px-4 py-2 text-gray-400 text-sm rounded-lg hover:bg-white/5" @click="closeModal">
            关闭
          </button>
          <button
            type="button"
            class="px-4 py-2 bg-violet-600 text-white rounded-lg text-sm hover:bg-violet-500"
            @click="copySummary"
          >
            复制全文
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
