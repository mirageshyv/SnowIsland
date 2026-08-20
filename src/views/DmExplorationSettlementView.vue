<script setup>
import { ref, computed, onMounted } from 'vue'
import { explorationAPI } from '@/utils/api.js'
import { useGameDayScope } from '@/composables/useGameDayScope.js'

const {
  currentGameDay,
  viewGameDay: gameDay,
  dayOptions,
  phaseLabel,
  loadGameState,
} = useGameDayScope()

const loading = ref(true)
const loadError = ref('')
const explorations = ref([])
const events = ref([])
const packs = ref([])
const packCollapsed = ref({})
const expandedId = ref(null)
const submitting = ref(false)

const PACK_IMPORT_EXAMPLE = `{5}{废弃哨站
地点描述：一座废墟。
可获得物资：绳索 (10米)， 火把 (1把)
历史碎片：柱子上有刻痕。
}{8}{冰封矿道
地点描述：被灌木掩盖的矿道入口。
可获得物资：金属制品 (1吨)
历史碎片：墙壁镶嵌着黑色金属。
特殊：是
}`

const newPackName = ref('')
const importRawText = ref('')
const previewing = ref(false)
const importing = ref(false)
const showPreview = ref(false)
const previewEvents = ref([])
const previewMessage = ref('')
const previewWarnings = ref([])
const previewOk = ref(false)

const showEventManager = ref(false)
const eventManagerTab = ref('single')
const editingEvent = ref(null)
const eventForm = ref({
  name: '',
  description: '',
  rarity: 'normal',
  eventDifficulty: 5,
  locationDesc: '',
  loreFragment: '',
  rewardsText: '',
  isSpecial: false,
  packId: null,
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
      await loadPacks()
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

const pickerEvents = computed(() =>
  filteredEvents.value.filter((e) => e.packEnabled !== false)
)

const eventsByPackId = computed(() => {
  const map = {}
  for (const event of filteredEvents.value) {
    const key = event.packId == null ? 'none' : event.packId
    if (!map[key]) map[key] = []
    map[key].push(event)
  }
  return map
})

const packTree = computed(() => {
  const byParent = {}
  for (const pack of packs.value) {
    const pid = pack.parentId == null ? 'root' : pack.parentId
    if (!byParent[pid]) byParent[pid] = []
    byParent[pid].push(pack)
  }
  function build(parentKey) {
    return (byParent[parentKey] || [])
      .slice()
      .sort((a, b) => (a.sortOrder ?? 0) - (b.sortOrder ?? 0) || a.id - b.id)
      .map((pack) => ({
        ...pack,
        events: eventsByPackId.value[pack.id] || [],
        children: build(pack.id),
      }))
  }
  const tree = build('root')
  const ungrouped = eventsByPackId.value.none || []
  if (ungrouped.length) {
    tree.push({
      id: 'none',
      name: '未分组',
      enabled: false,
      eventCount: ungrouped.length,
      parentId: null,
      events: ungrouped,
      children: [],
    })
  }
  return tree
})

const packRows = computed(() => {
  const collapsed = packCollapsed.value
  const rows = []
  function walk(nodes, depth) {
    for (const node of nodes) {
      rows.push({ kind: 'pack', key: 'p-' + node.id, pack: node, depth })
      if (!collapsed[node.id]) {
        for (const event of node.events || []) {
          rows.push({ kind: 'event', key: 'e-' + event.id, event, depth: depth + 1 })
        }
        walk(node.children || [], depth + 1)
      }
    }
  }
  walk(packTree.value, 0)
  return rows
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

async function onDayChange() {
  expandedId.value = null
  await loadPendingExplorations(true)
}

async function loadPacks() {
  try {
    const res = await explorationAPI.getPacks()
    if (res?.success && Array.isArray(res.packs)) {
      packs.value = res.packs
    }
  } catch (e) {
    console.error(e)
  }
}

async function loadPendingExplorations(silent = false) {
  if (!silent) loading.value = true
  loadError.value = ''
  try {
    const [explRes, eventsRes] = await Promise.all([
      explorationAPI.getPendingExplorations(gameDay.value),
      explorationAPI.getAllEvents(),
    ])
    if (explRes?.success) {
      explorations.value = explRes.explorations || []
    } else {
      explorations.value = []
      loadError.value = explRes?.message || '无法加载待结算探索'
    }
    if (Array.isArray(eventsRes)) {
      events.value = eventsRes
    }
  } catch (e) {
    console.error(e)
    loadError.value = '无法加载待结算探索'
  } finally {
    if (!silent) loading.value = false
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
  const rewards = exploration.event?.rewards || exploration.rewards || []
  submitting.value = true
  try {
    const res = await explorationAPI.settle(exploration.id, rewards)
    if (res?.success) {
      await loadPendingExplorations()
    } else {
      alert(res?.message || '发布失败')
    }
  } catch {
    alert('发布失败')
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
  eventManagerTab.value = 'single'
  const basePack = packs.value.find((p) => p.name === '基础包')
  eventForm.value = {
    name: '',
    description: '',
    rarity: 'normal',
    eventDifficulty: 5,
    locationDesc: '',
    loreFragment: '',
    rewardsText: '',
    isSpecial: false,
    packId: basePack?.id ?? null,
  }
  newPackName.value = ''
  importRawText.value = ''
  showEventManager.value = true
}

function openEditEvent(event) {
  editingEvent.value = event
  eventManagerTab.value = 'single'
  eventForm.value = {
    name: event.name || '',
    description: event.description || '',
    rarity: event.rarity || 'normal',
    eventDifficulty: event.eventDifficulty ?? 5,
    locationDesc: event.locationDesc || '',
    loreFragment: event.loreFragment || '',
    rewardsText: rewardsToText(event.rewards),
    isSpecial: event.isSpecial || false,
    packId: event.packId ?? null,
  }
  showEventManager.value = true
}

function rewardsToText(rewards) {
  if (!Array.isArray(rewards) || !rewards.length) return ''
  return rewards
    .map((r) => {
      const name = r.name || ''
      const qty = r.quantity ?? 1
      const unit = r.unit || ''
      return name ? `${name} (${qty}${unit})` : ''
    })
    .filter(Boolean)
    .join('，')
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
        rewardsText: eventForm.value.rewardsText,
        isSpecial: eventForm.value.isSpecial,
        packId: eventForm.value.packId,
      })
    } else {
      res = await explorationAPI.createEvent({
        name: eventForm.value.name,
        description: eventForm.value.description,
        rarity: eventForm.value.rarity,
        eventDifficulty: difficulty,
        locationDesc: eventForm.value.locationDesc,
        loreFragment: eventForm.value.loreFragment,
        rewardsText: eventForm.value.rewardsText,
        isSpecial: eventForm.value.isSpecial,
        packId: eventForm.value.packId,
      })
    }
    if (res?.success) {
      await loadPendingExplorations()
      await loadPacks()
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
      await loadPacks()
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
  const labels = { pending: '待生成', explored: '待发布', settled: '已发布' }
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
  await loadGameState()
  await loadPacks()
  await loadPendingExplorations()
})

function togglePackFolder(packId) {
  packCollapsed.value = { ...packCollapsed.value, [packId]: !packCollapsed.value[packId] }
}

function isPackCollapsed(packId) {
  return !!packCollapsed.value[packId]
}

async function togglePackEnabled(pack, enabled) {
  try {
    const res = await explorationAPI.setPackEnabled(pack.id, enabled)
    if (res?.success) {
      await loadPacks()
      await loadPendingExplorations(true)
    } else {
      alert(res?.message || '更新卡包失败')
    }
  } catch {
    alert('更新卡包失败')
  }
}

async function copyFormatExample() {
  try {
    await navigator.clipboard.writeText(PACK_IMPORT_EXAMPLE)
    alert('已复制格式示例')
  } catch {
    alert('复制失败，请手动选择文本复制')
  }
}

const PACK_NAME_MAX = 80
const RAW_IMPORT_MAX = 200000
const PACK_NAME_OK = /^[\u4e00-\u9fffA-Za-z0-9_\-·（）()\s]+$/

function validatePackNameClient(raw) {
  const name = String(raw || '').trim()
  if (!name) return '请填写卡包名称'
  if (name.length > PACK_NAME_MAX) return `卡包名称不能超过 ${PACK_NAME_MAX} 个字符`
  if (!PACK_NAME_OK.test(name)) return '卡包名称仅允许中文、字母、数字、空格与 ·_-（）'
  return null
}

function validateImportTextClient(raw) {
  const text = String(raw || '')
  if (!text.trim()) return '请粘贴事件文本'
  if (text.length > RAW_IMPORT_MAX) return `导入文本过长（上限 ${RAW_IMPORT_MAX} 字符）`
  if (text.includes('\0')) return '导入文本包含非法控制字符'
  return null
}

async function handlePreviewImport() {
  const nameErr = validatePackNameClient(newPackName.value)
  if (nameErr) {
    alert(nameErr)
    return
  }
  const textErr = validateImportTextClient(importRawText.value)
  if (textErr) {
    alert(textErr)
    return
  }
  previewing.value = true
  try {
    const res = await explorationAPI.previewPackImport(importRawText.value)
    previewEvents.value = res?.events || []
    previewMessage.value = res?.message || ''
    previewWarnings.value = Array.isArray(res?.warnings) ? res.warnings : []
    previewOk.value = !!res?.success
    if (res?.success) {
      showPreview.value = true
    } else {
      alert(res?.message || '解析失败')
      if (previewEvents.value.length) {
        showPreview.value = true
      }
    }
  } catch {
    alert('解析失败')
  } finally {
    previewing.value = false
  }
}

async function confirmImportPack() {
  const nameErr = validatePackNameClient(newPackName.value)
  if (nameErr) {
    alert(nameErr)
    return
  }
  const textErr = validateImportTextClient(importRawText.value)
  if (textErr) {
    alert(textErr)
    return
  }
  importing.value = true
  try {
    const res = await explorationAPI.importPack(newPackName.value.trim(), importRawText.value)
    if (res?.success) {
      alert(res.message || '导入成功')
      showPreview.value = false
      showEventManager.value = false
      newPackName.value = ''
      importRawText.value = ''
      previewEvents.value = []
      await loadPacks()
      await loadPendingExplorations()
    } else {
      alert(res?.message || '导入失败')
    }
  } catch {
    alert('导入失败')
  } finally {
    importing.value = false
  }
}
</script>

<template>
  <div>
    <div class="text-center mb-8">
      <h1 class="text-white text-2xl font-semibold mb-2">探索岛屿结算</h1>
      <p class="text-gray-500 text-sm">系统已自动生成探索结果，确认后发布给玩家并发放奖励</p>
      <div class="mt-4 flex flex-wrap items-center justify-center gap-3">
        <label class="text-gray-400 text-sm">查看天数</label>
        <select
          v-model.number="gameDay"
          class="bg-black/30 border border-white/10 rounded-lg px-3 py-1.5 text-sm text-gray-200"
          @change="onDayChange"
        >
          <option v-for="d in dayOptions" :key="d" :value="d">
            第 {{ d }} 天{{ d === currentGameDay ? '（当前）' : '' }}
          </option>
        </select>
        <span class="text-gray-500 text-sm">
          游戏第 {{ currentGameDay }} 天 · {{ phaseLabel }}
        </span>
      </div>
    </div>

    <div v-if="loading" class="flex justify-center py-20">
      <div class="w-12 h-12 border-4 border-indigo-500 border-t-transparent rounded-full animate-spin" />
    </div>

    <template v-else>
      <div class="mb-6 flex items-center gap-3 flex-wrap">
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
            v-for="row in packRows"
            :key="row.key"
            :style="{ marginLeft: (row.depth * 16) + 'px' }"
          >
            <div
              v-if="row.kind === 'pack'"
              class="bg-white/5 border border-white/10 rounded-xl p-3 flex items-center justify-between gap-3"
            >
              <button
                type="button"
                class="flex items-center gap-2 text-left min-w-0 flex-1"
                @click="togglePackFolder(row.pack.id)"
              >
                <span class="text-gray-400 text-xs w-3">{{ isPackCollapsed(row.pack.id) ? '▶' : '▼' }}</span>
                <span class="text-white text-sm font-medium truncate">{{ row.pack.name }}</span>
                <span class="text-gray-500 text-xs shrink-0">（{{ row.pack.eventCount ?? row.pack.events.length }}）</span>
              </button>
              <label
                v-if="row.pack.id !== 'none'"
                class="flex items-center gap-2 shrink-0 cursor-pointer text-xs text-gray-300"
                @click.stop
              >
                <input
                  type="checkbox"
                  :checked="!!row.pack.enabled"
                  class="w-4 h-4"
                  @change="togglePackEnabled(row.pack, $event.target.checked)"
                />
                加入本局
              </label>
            </div>
            <div
              v-else
              class="bg-gradient-to-br from-[#1a2332] to-[#0f1419] border border-white/10 rounded-xl p-3 flex items-start justify-between gap-3"
            >
              <div class="flex-1 min-w-0">
                <div class="flex items-center gap-2 flex-wrap mb-1">
                  <span class="text-white text-sm font-medium">{{ row.event.name }}</span>
                  <span
                    class="text-xs px-2 py-0.5 rounded-full border"
                    :class="getDifficultyColor(row.event.eventDifficulty)"
                  >
                    {{ getDifficultyIcon(row.event.eventDifficulty) }} 难度 {{ row.event.eventDifficulty }}/{{ MAX_DIFFICULTY }}
                  </span>
                  <span
                    class="text-xs px-2 py-0.5 rounded-full border"
                    :class="getRarityColor(row.event.rarity)"
                  >
                    {{ getRarityLabel(row.event.rarity) }}
                  </span>
                  <span v-if="row.event.isSpecial" class="text-xs px-2 py-0.5 rounded-full border border-pink-500/30 bg-pink-500/20 text-pink-400">
                    ⭐ 特殊事件
                  </span>
                  <span v-if="row.event.triggered" class="text-gray-600 text-xs">（已触发）</span>
                </div>
                <p class="text-gray-500 text-xs line-clamp-1">{{ row.event.description }}</p>
              </div>
              <div class="flex gap-2 shrink-0">
                <button
                  type="button"
                  class="text-xs px-3 py-1 rounded-lg border border-gray-500/30 text-gray-400 hover:bg-gray-500/10"
                  @click="viewEventDetail(row.event)"
                >
                  详情
                </button>
                <button
                  type="button"
                  class="text-xs px-3 py-1 rounded-lg border border-blue-500/30 text-blue-400 hover:bg-blue-500/10"
                  @click="openEditEvent(row.event)"
                >
                  编辑
                </button>
                <button
                  type="button"
                  class="text-xs px-3 py-1 rounded-lg border border-red-500/30 text-red-400 hover:bg-red-500/10"
                  @click="deleteEvent(row.event)"
                >
                  删除
                </button>
              </div>
            </div>
          </div>
          <div v-if="packRows.length === 0" class="text-center py-8 text-gray-500 text-sm">
            没有匹配的事件
          </div>
        </div>
      </div>

      <div v-if="loadError" class="text-center py-16 text-red-400">
        {{ loadError }}
      </div>
      <div v-else-if="explorations.length === 0" class="text-center py-16 text-gray-400">
        第 {{ gameDay }} 天暂无探索行动
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
                    v-for="event in pickerEvents"
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
            待发布（{{ exploredExplorations.length }}）
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
                :disabled="submitting"
                class="w-full bg-gradient-to-r from-green-500 to-green-600 hover:from-green-600 hover:to-green-700 disabled:from-gray-600 disabled:to-gray-600 text-white py-2.5 rounded-lg text-sm font-medium"
                @click="settleExploration(exp)"
              >
                {{ submitting ? '发布中...' : '确认发布给玩家' }}
              </button>
            </div>
          </div>
        </div>

        <div v-if="settledExplorations.length">
          <h2 class="text-white text-lg font-medium mb-4 flex items-center gap-2">
            <span class="w-2 h-2 rounded-full bg-green-500"></span>
            已发布（{{ settledExplorations.length }}）
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
      <div class="bg-gradient-to-br from-[#1a2332] to-[#0f1419] border border-white/20 rounded-2xl p-6 max-w-4xl w-full max-h-[92vh] overflow-y-auto">
        <h3 class="text-white text-lg font-semibold mb-4">
          {{ editingEvent ? '编辑事件' : '新建事件' }}
        </h3>
        <div v-if="!editingEvent" class="flex gap-2 mb-5">
          <button
            type="button"
            class="px-4 py-2 rounded-lg text-sm font-medium transition-colors"
            :class="eventManagerTab === 'single' ? 'bg-indigo-500/30 border border-indigo-400/50 text-white' : 'border border-white/10 text-gray-400 hover:bg-white/5'"
            @click="eventManagerTab = 'single'"
          >
            单个新建
          </button>
          <button
            type="button"
            class="px-4 py-2 rounded-lg text-sm font-medium transition-colors"
            :class="eventManagerTab === 'import' ? 'bg-indigo-500/30 border border-indigo-400/50 text-white' : 'border border-white/10 text-gray-400 hover:bg-white/5'"
            @click="eventManagerTab = 'import'"
          >
            一键导入
          </button>
        </div>

        <div v-if="editingEvent || eventManagerTab === 'single'" class="space-y-4">
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
            <label class="block text-gray-400 text-xs mb-1">所属卡包</label>
            <select
              v-model="eventForm.packId"
              class="w-full bg-black/30 border border-white/10 rounded-lg px-3 py-2 text-sm text-gray-200"
            >
              <option :value="null">未分组</option>
              <option v-for="pack in packs" :key="pack.id" :value="pack.id">{{ pack.name }}</option>
            </select>
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
            <label class="block text-gray-400 text-xs mb-1">历史碎片</label>
            <textarea
              v-model="eventForm.loreFragment"
              rows="3"
              class="w-full bg-black/30 border border-white/10 rounded-lg px-3 py-2 text-sm text-gray-200 resize-none"
              placeholder="探索时发现的历史秘密/线索..."
            />
          </div>
          <div>
            <label class="block text-gray-400 text-xs mb-1">可获得物资</label>
            <textarea
              v-model="eventForm.rewardsText"
              rows="2"
              class="w-full bg-black/30 border border-white/10 rounded-lg px-3 py-2 text-sm text-gray-200 resize-none"
              placeholder="例：绳索 (10米)， 火把 (1把)。留空表示无物资。"
            />
            <p class="text-red-400/80 text-xs mt-1">物资名称需与目录一致，无法识别的名称将阻止保存。</p>
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

        <div v-else class="space-y-4">
          <p class="text-gray-500 text-xs leading-relaxed">
            重复块 <code class="text-indigo-300">{难度}{正文}</code>。编号可选：也可写成 <code class="text-indigo-300">{编号}{难度}{正文}</code>。
            正文第一行是事件名，随后必须包含 <code class="text-indigo-300">地点描述</code>、<code class="text-indigo-300">可获得物资</code>（物品）、<code class="text-indigo-300">历史碎片</code>，可选「特殊：是」。正文中不能包含 <code class="text-indigo-300">}</code>。难度 0-20。
          </p>
          <div>
            <label class="block text-gray-400 text-xs mb-1">卡包名称</label>
            <input
              v-model="newPackName"
              type="text"
              maxlength="80"
              class="w-full bg-black/30 border border-white/10 rounded-lg px-3 py-2 text-sm text-gray-200"
              placeholder="例：扩展包·海岸"
            />
          </div>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <div class="flex items-center justify-between mb-1">
                <label class="text-gray-400 text-xs">可复制格式示例</label>
                <button
                  type="button"
                  class="text-xs px-2 py-1 rounded-lg border border-indigo-500/30 text-indigo-300 hover:bg-indigo-500/10"
                  @click="copyFormatExample"
                >
                  复制示例
                </button>
              </div>
              <textarea
                readonly
                :value="PACK_IMPORT_EXAMPLE"
                rows="14"
                class="w-full bg-black/40 border border-white/10 rounded-lg px-3 py-2 text-xs text-gray-400 font-mono resize-none"
              />
            </div>
            <div>
              <label class="block text-gray-400 text-xs mb-1">导入文本</label>
              <textarea
                v-model="importRawText"
                rows="14"
                maxlength="200000"
                class="w-full bg-black/30 border border-white/10 rounded-lg px-3 py-2 text-sm text-gray-200 font-mono"
                placeholder="粘贴 {难度}{正文}，正文含名称、地点描述、可获得物资、历史碎片..."
              />
            </div>
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
            v-if="editingEvent || eventManagerTab === 'single'"
            type="button"
            :disabled="submitting"
            class="px-4 py-2 rounded-lg bg-gradient-to-r from-indigo-500 to-indigo-600 hover:from-indigo-600 hover:to-indigo-700 disabled:from-gray-600 disabled:to-gray-600 text-white text-sm"
            @click="saveEvent"
          >
            {{ submitting ? '保存中...' : '保存' }}
          </button>
          <button
            v-else
            type="button"
            :disabled="previewing"
            class="px-4 py-2 rounded-lg bg-gradient-to-r from-indigo-500 to-indigo-600 hover:from-indigo-600 hover:to-indigo-700 disabled:from-gray-600 disabled:to-gray-600 text-white text-sm"
            @click="handlePreviewImport"
          >
            {{ previewing ? '解析中...' : '预览并导入' }}
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

    <!-- 导入预览弹窗 -->
    <div
      v-if="showPreview"
      class="fixed inset-0 bg-black/70 flex items-center justify-center z-[60] p-4"
      @click.self="showPreview = false"
    >
      <div class="bg-gradient-to-br from-[#1a2332] to-[#0f1419] border border-white/20 rounded-2xl p-6 max-w-3xl w-full max-h-[90vh] overflow-y-auto">
        <h3 class="text-white text-lg font-semibold mb-2">导入预览</h3>
        <p class="text-gray-400 text-xs mb-2">{{ previewMessage || ('卡包：' + newPackName) }}</p>
        <p v-if="previewWarnings.length && !previewOk" class="text-amber-400/80 text-xs mb-4">
          {{ previewWarnings.join('；') }}
        </p>
        <div class="space-y-2 mb-6">
          <div
            v-for="(ev, idx) in previewEvents"
            :key="idx"
            class="bg-black/20 border border-white/10 rounded-xl p-3"
          >
            <div class="flex items-center gap-2 flex-wrap mb-1">
              <span class="text-white text-sm font-medium">{{ ev.name }}</span>
              <span v-if="ev.sourceNumber != null" class="text-xs text-gray-500">#{{ ev.sourceNumber }}</span>
              <span
                class="text-xs px-2 py-0.5 rounded-full border"
                :class="getDifficultyColor(ev.difficulty ?? ev.eventDifficulty)"
              >
                难度 {{ ev.difficulty ?? ev.eventDifficulty }}
              </span>
              <span v-if="ev.isSpecial" class="text-xs px-2 py-0.5 rounded-full border border-pink-500/30 bg-pink-500/20 text-pink-400">
                ⭐ 特殊
              </span>
            </div>
            <p v-if="ev.locationDescSnippet || ev.locationDesc" class="text-gray-500 text-xs mb-1">
              地点：{{ ev.locationDescSnippet || ev.locationDesc }}
            </p>
            <p v-if="ev.loreSnippet || ev.loreFragment" class="text-amber-400/80 text-xs mb-1">
              历史碎片：{{ ev.loreSnippet || ev.loreFragment }}
            </p>
            <p v-if="ev.rewardLabels && ev.rewardLabels.length" class="text-green-400 text-xs">
              物资：{{ ev.rewardLabels.join('，') }}
            </p>
            <p v-if="ev.unmatchedRewards && ev.unmatchedRewards.length" class="text-red-400 text-xs">
              无法识别：{{ ev.unmatchedRewards.join('、') }}
            </p>
            <p
              v-for="(warn, wi) in (ev.qtyFallbackWarnings || [])"
              :key="'qty-' + idx + '-' + wi"
              class="text-amber-400/80 text-xs"
            >
              {{ warn }}
            </p>
            <p
              v-if="!(ev.rewardLabels && ev.rewardLabels.length) && !(ev.unmatchedRewards && ev.unmatchedRewards.length)"
              class="text-gray-600 text-xs"
            >
              物资：无
            </p>
          </div>
          <div v-if="previewEvents.length === 0" class="text-center py-6 text-gray-500 text-sm">没有解析到事件</div>
        </div>
        <div class="flex justify-end gap-3">
          <button
            type="button"
            class="px-4 py-2 rounded-lg border border-white/10 text-gray-300 hover:bg-white/5 text-sm"
            @click="showPreview = false"
          >
            取消
          </button>
          <button
            type="button"
            :disabled="importing || !previewOk || !previewEvents.length"
            class="px-4 py-2 rounded-lg bg-gradient-to-r from-indigo-500 to-indigo-600 hover:from-indigo-600 hover:to-indigo-700 disabled:from-gray-600 disabled:to-gray-600 text-white text-sm"
            @click="confirmImportPack"
          >
            {{ importing ? '导入中...' : '确认导入' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>