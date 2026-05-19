<script setup>
import { computed, ref, watch, onMounted } from 'vue'
import { resolveShelterInventoryRows, getMaterialImageUrlOrDefault } from '../data/gameData.js'
import { shelterAPI } from '../utils/api.js'
import ShelterSupplyCards from './ShelterSupplyCards.vue'

const props = defineProps({
  mode: {
    type: String,
    default: 'ruler',
    validator: (v) => ['dm', 'ruler'].includes(v)
  }
})

const isDm = computed(() => props.mode === 'dm')

const loading = ref(true)
const loadError = ref(null)

const currentBuildValue = ref(0)
const foodSupply = ref({ totalKg: 0, items: [] })
const energyReserve = ref({ items: [] })
const shelterInventory = ref([])
const currentGameDay = ref(1)
const editGameDay = ref(1)
const dayVerified = ref(false)
const laborCandidates = ref([])
const dailyLabor = ref([])
const buildLogs = ref([])
const laborDays = ref([])
const savingLabor = ref(false)
const verifying = ref(false)

const shelterDisplayRows = computed(() => resolveShelterInventoryRows(shelterInventory.value))
const rosterPlayerIds = computed(() => new Set((dailyLabor.value || []).filter((r) => r?.playerId != null).map((r) => r.playerId)))
function defaultLaborRow(playerId) {
  const p = laborCandidates.value.find((c) => c.id === playerId)
  const isProfessional = Boolean(p?.isProfessional)
  const baseBuildValue = Number(p?.baseBuildValue) || (isProfessional ? 5 : 4)
  return {
    playerId,
    name: p?.name ?? `玩家${playerId}`,
    jobName: p?.jobName ?? '—',
    isProfessional,
    baseBuildValue,
    buildValue: baseBuildValue,
    exploited: false,
    escaped: false
  }
}

function toggleLabor(playerId) {
  if (!isDm.value && dayVerified.value) return
  const idx = dailyLabor.value.findIndex((r) => r.playerId === playerId)
  if (idx >= 0) {
    dailyLabor.value = dailyLabor.value.filter((r) => r.playerId !== playerId)
  } else {
    dailyLabor.value = [...dailyLabor.value, defaultLaborRow(playerId)]
  }
}

function onExploitChange(row) {
  if (row.escaped) return
  if (row.exploited) {
    const count = dailyLabor.value.filter((r) => r.exploited).length
    if (count > 3) {
      row.exploited = false
      alert('最多压榨3名劳工')
      return
    }
  }
  recalcBuildValue(row)
}

function recalcBuildValue(row) {
  if (row.escaped) {
    row.buildValue = 0
    return
  }
  const base = Number(row.baseBuildValue) || (row.isProfessional ? 5 : 4)
  row.buildValue = base + (row.exploited ? 3 : 0)
}

function removeLaborRow(playerId) {
  dailyLabor.value = dailyLabor.value.filter((r) => r.playerId !== playerId)
}

function onEscapedChange(row) {
  if (row.escaped) {
    row.buildValue = 0
  } else {
    recalcBuildValue(row)
  }
}

function applyLaborResult(result) {
  if (!result?.success) return false
  currentBuildValue.value = Number(result.currentBuildValue) || 0
  currentGameDay.value = Number(result.currentGameDay) || currentGameDay.value
  dayVerified.value = Boolean(result.dayVerified)
  dailyLabor.value = Array.isArray(result.dailyLabor) 
    ? result.dailyLabor.filter((r) => r != null && r.playerId != null).map((r) => ({ ...r })) 
    : dailyLabor.value
  buildLogs.value = Array.isArray(result.buildLogs) 
    ? result.buildLogs.filter((l) => l != null && l.day != null && Array.isArray(l.workers))
      .map((log) => ({
        ...log,
        workers: log.workers.filter((w) => w != null && w.playerId != null && w.name != null)
      }))
    : buildLogs.value
  laborDays.value = Array.isArray(result.laborDays) ? result.laborDays.filter((d) => d != null) : laborDays.value
  return true
}

async function saveLaborRoster() {
  if (savingLabor.value) return
  savingLabor.value = true
  try {
    let result
    if (isDm.value) {
      const laborers = dailyLabor.value.map((r) => ({
        playerId: r.playerId,
        exploited: Boolean(r.exploited),
        escaped: Boolean(r.escaped)
      }))
      result = await shelterAPI.setDailyLabor(laborers, editGameDay.value)
    } else {
      const exploitedCount = dailyLabor.value.filter((r) => r.exploited).length
      if (exploitedCount > 3) {
        alert('最多压榨3名劳工')
        savingLabor.value = false
        return
      }
      const laborers = dailyLabor.value.map((r) => ({
        playerId: r.playerId,
        exploited: Boolean(r.exploited),
      }))
      result = await shelterAPI.setLaborRoster(laborers, currentGameDay.value)
    }
    if (applyLaborResult(result)) {
      alert(isDm.value ? '劳工名单已保存' : '今日劳工名单已提交')
    } else {
      alert(result?.message || '保存失败')
    }
  } catch (e) {
    alert('保存失败: ' + (e.message || '未知错误'))
  } finally {
    savingLabor.value = false
  }
}

async function verifyCurrentDay() {
  if (!isDm.value || verifying.value) return
  verifying.value = true
  try {
    const result = await shelterAPI.verifyLaborDay(editGameDay.value)
    if (applyLaborResult(result)) {
      alert(result?.message || '已结算')
    } else {
      alert(result?.message || '结算失败')
    }
  } catch (e) {
    alert('结算失败: ' + (e.message || '未知错误'))
  } finally {
    verifying.value = false
  }
}

const selectedShelterItemId = ref(null)
const selectedShelterRow = computed(() => {
  if (!selectedShelterItemId.value) return null
  return shelterDisplayRows.value.find((r) => r.id === selectedShelterItemId.value) || null
})

const savingStock = ref(false)
const stockEditQty = ref(0)
const showAddStockModal = ref(false)
const catalogItems = ref([])
const addSelected = ref(null)
const addQuantity = ref(1)
const addSearch = ref('')
const addFilterType = ref('all')

const addCatalogFiltered = computed(() => {
  let items = catalogItems.value
  if (addFilterType.value !== 'all') {
    items = items.filter((i) => i.itemType === addFilterType.value)
  }
  if (addSearch.value.trim()) {
    const q = addSearch.value.trim().toLowerCase()
    items = items.filter((i) => (i.name || '').toLowerCase().includes(q))
  }
  return items.map((item) => ({
    ...item,
    imageUrl: getMaterialImageUrlOrDefault(item.itemType, item.itemId)
  }))
})

watch(selectedShelterRow, (row) => {
  if (row) stockEditQty.value = row.quantity
})

function stockKeysFromRow(row) {
  if (row?.itemType != null && row?.itemId != null) {
    return { itemType: row.itemType, itemId: row.itemId }
  }
  const id = row?.id
  if (!id || !String(id).includes('_')) return null
  const [itemType, ...rest] = String(id).split('_')
  const itemId = parseInt(rest.join('_'), 10)
  if (!itemType || Number.isNaN(itemId)) return null
  return { itemType, itemId }
}

async function loadShelterCatalog() {
  if (!isDm.value) return
  try {
    const result = await shelterAPI.getItemCatalog()
    if (result?.success) catalogItems.value = result.items || []
  } catch (e) {
    console.error('加载物品目录失败:', e)
  }
}

async function persistShelterStock(itemType, itemId, quantity) {
  savingStock.value = true
  try {
    const result = await shelterAPI.upsertStock(itemType, itemId, quantity)
    if (!result?.success) {
      alert(result?.message || '更新失败')
      return false
    }
    await loadShelterFromApi()
    showAddStockModal.value = false
    addSelected.value = null
    return true
  } catch (e) {
    alert('更新库存失败: ' + (e.message || '未知错误'))
    return false
  } finally {
    savingStock.value = false
  }
}

async function saveShelterStockQty() {
  if (!isDm.value || !selectedShelterRow.value) return
  const keys = stockKeysFromRow(selectedShelterRow.value)
  if (!keys) return
  const q = Math.max(0, Math.floor(Number(stockEditQty.value) || 0))
  await persistShelterStock(keys.itemType, keys.itemId, q)
}

async function removeShelterStockItem() {
  if (!isDm.value || !selectedShelterRow.value) return
  const keys = stockKeysFromRow(selectedShelterRow.value)
  if (!keys) return
  if (!confirm(`确定从避难所移除「${selectedShelterRow.value.name}」？`)) return
  savingStock.value = true
  try {
    const result = await shelterAPI.deleteStock(keys.itemType, keys.itemId)
    if (!result?.success) {
      alert(result?.message || '移除失败')
      return
    }
    selectedShelterItemId.value = null
    await loadShelterFromApi()
  } catch (e) {
    alert('移除失败: ' + (e.message || '未知错误'))
  } finally {
    savingStock.value = false
  }
}

function openAddStockModal() {
  addSelected.value = null
  addQuantity.value = 1
  addSearch.value = ''
  addFilterType.value = 'all'
  showAddStockModal.value = true
}

async function confirmAddShelterStock() {
  if (!addSelected.value) {
    alert('请选择要添加的物品')
    return
  }
  const q = Math.max(1, Math.floor(Number(addQuantity.value) || 1))
  const { itemType, itemId } = addSelected.value
  const existing = shelterDisplayRows.value.find(
    (r) => r.itemType === itemType && r.itemId === itemId
  )
  const total = (existing?.quantity || 0) + q
  await persistShelterStock(itemType, itemId, total)
}

function shelterCategoryLabel(c) {
  return { prop: '道具', weapon: '武器', ammo: '弹药', material: '建材' }[c] || '未知'
}

function fail(message) {
  loadError.value = message
  currentBuildValue.value = 0
  shelterInventory.value = []
  foodSupply.value = { totalKg: 0, items: [] }
  energyReserve.value = { items: [] }
  selectedShelterItemId.value = null
}

async function loadShelterFromApi() {
  loading.value = true
  loadError.value = null
  try {
    const dayParam = isDm.value ? editGameDay.value : undefined
    const data = await shelterAPI.getSummary(dayParam)
    if (!data?.success) {
      fail(data?.message || '加载避难所数据失败')
      return
    }
    if (!data.foodSupply || !Array.isArray(data.foodSupply.items)) {
      fail('食物供应数据无效')
      return
    }
    if (!data.energyReserve || !Array.isArray(data.energyReserve.items)) {
      fail('能量储备数据无效')
      return
    }
    if (!Array.isArray(data.inventory)) {
      fail('物资库存数据无效')
      return
    }

    currentBuildValue.value = Number(data.currentBuildValue) || 0
    shelterInventory.value = data.inventory
      .filter((row) => row && (row.id != null || (row.itemType != null && row.itemId != null)))
      .map((row) => {
        if (row.itemType != null && row.itemId != null) {
          return {
            itemType: row.itemType,
            itemId: row.itemId,
            quantity: Number(row.quantity) || 0,
            name: row.name,
            description: row.description,
            unit: row.unit
          }
        }
        return { id: String(row.id), quantity: Number(row.quantity) || 0 }
      })
    foodSupply.value = data.foodSupply
    energyReserve.value = data.energyReserve
    currentGameDay.value = Number(data.currentGameDay ?? data.gameDay) || 1
    if (!isDm.value) {
      editGameDay.value = currentGameDay.value
    } else {
      editGameDay.value = Number(data.gameDay) || editGameDay.value
    }
    dayVerified.value = Boolean(data.dayVerified)
    laborCandidates.value = Array.isArray(data.laborCandidates) ? data.laborCandidates.filter((c) => c != null) : []
    dailyLabor.value = Array.isArray(data.dailyLabor) ? data.dailyLabor.filter((r) => r != null).map((r) => ({ ...r })) : []
    buildLogs.value = Array.isArray(data.buildLogs) ? data.buildLogs.filter((l) => l != null) : []
    laborDays.value = Array.isArray(data.laborDays) ? data.laborDays.filter((d) => d != null) : []
  } catch (err) {
    console.error('加载避难所数据失败:', err)
    fail('网络错误，无法加载避难所数据')
  } finally {
    loading.value = false
  }
}

watch(
  () => (isDm.value ? editGameDay.value : null),
  (day, prev) => {
    if (!isDm.value || day === prev || loading.value) return
    loadShelterFromApi()
  }
)

watch(shelterDisplayRows, (rows) => {
  if (loadError.value || !rows.length || selectedShelterItemId.value) return
  selectedShelterItemId.value = rows[0].id
}, { immediate: true })

onMounted(() => {
  loadShelterFromApi()
  if (isDm.value) loadShelterCatalog()
})
</script>

<template>
  <div class="max-w-5xl w-full mx-auto">
    <div class="text-center mb-10 pt-2">
      <h1 class="text-white text-3xl md:text-4xl font-semibold tracking-wide mb-3">统治者避难所建造进度</h1>
      <p class="text-gray-500 text-base md:text-lg">
        {{ isDm ? '管理每日劳工、建造值与结算' : '追踪避难所建造进展与库存物资' }}
      </p>
    </div>

    <div v-if="loading" class="flex justify-center py-16">
      <div class="text-center text-gray-400 text-sm">加载避难所数据中…</div>
    </div>

    <div
      v-else-if="loadError"
      class="rounded-xl border border-red-500/40 bg-red-500/10 px-6 py-10 text-center"
    >
      <p class="text-red-200 text-base mb-4">{{ loadError }}</p>
      <button
        type="button"
        class="px-4 py-2 rounded-lg bg-red-500/20 text-red-200 text-sm border border-red-500/30 hover:bg-red-500/30 transition-colors"
        @click="loadShelterFromApi"
      >
        重试
      </button>
    </div>

    <template v-else>
      <div class="bg-gradient-to-br from-[#1a2332] to-[#0f1419] border border-white/10 rounded-3xl p-8 mb-10 overflow-visible">
        <div class="text-center mb-2">
          <p class="text-gray-400 text-lg mb-2">总建造值</p>
          <p class="text-5xl md:text-6xl text-cyan-400 font-bold tabular-nums tracking-tight">
            {{ currentBuildValue }}
          </p>
        </div>
        <ShelterSupplyCards :food="foodSupply" :energy="energyReserve" />
      </div>

      <div class="bg-gradient-to-br from-[#1a2332] to-[#0f1419] border border-white/10 rounded-3xl p-6 md:p-8 mb-10">
        <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 mb-4">
          <div>
            <template v-if="isDm">
              <h2 class="text-xl text-white font-medium tracking-tight">劳工名单管理</h2>
              <div class="flex flex-wrap items-center gap-3 mt-2">
                <label class="text-gray-500 text-sm flex items-center gap-2">
                  游戏日
                  <select
                    v-model.number="editGameDay"
                    class="bg-black/40 border border-white/10 rounded-lg px-2 py-1 text-white text-sm"
                  >
                    <option v-for="d in laborDays" :key="d.gameDay" :value="d.gameDay">
                      第 {{ d.gameDay }} 天{{ d.verified ? '（已结算）' : '' }}
                    </option>
                    <option v-if="!laborDays.length" :value="currentGameDay">第 {{ currentGameDay }} 天</option>
                  </select>
                </label>
                <span
                  v-if="dayVerified"
                  class="px-2 py-0.5 rounded-full text-xs bg-emerald-500/20 text-emerald-300 border border-emerald-500/30"
                >
                  已结算
                </span>
                <span
                  v-else
                  class="px-2 py-0.5 rounded-full text-xs bg-amber-500/20 text-amber-300 border border-amber-500/30"
                >
                  待结算
                </span>
              </div>
              <p class="text-gray-500 text-sm mt-2">职业劳工基础值5，普通劳工基础值4；压榨+3；逃役归零；结算后玩家端建造日志才显示数值</p>
            </template>
            <template v-else>
              <h2 class="text-xl text-white font-medium tracking-tight">第 {{ currentGameDay }} 天 · 今日劳工</h2>
              <p class="text-gray-500 text-sm mt-1">
                选择今日在避难所劳作的玩家；职业劳工基础值5，普通劳工基础值4；可标记「压榨」（最多3人，+3建造值、受伤、无法生产）
              </p>
              <p v-if="dayVerified" class="text-amber-400/90 text-xs mt-1">今日名单已由主持人结算，无法再修改</p>
            </template>
          </div>
          <div v-if="isDm" class="flex flex-wrap gap-2 shrink-0">
            <button
              type="button"
              class="px-4 py-2 rounded-xl bg-cyan-600 hover:bg-cyan-500 text-white text-sm font-medium disabled:opacity-50 transition-colors"
              :disabled="savingLabor"
              @click="saveLaborRoster"
            >
              {{ savingLabor ? '保存中…' : '保存' }}
            </button>
            <button
              v-if="!dayVerified"
              type="button"
              class="px-4 py-2 rounded-xl bg-emerald-600 hover:bg-emerald-500 text-white text-sm font-medium disabled:opacity-50 transition-colors"
              :disabled="verifying || !dailyLabor.length"
              @click="verifyCurrentDay"
            >
              {{ verifying ? '结算中…' : '确认结算' }}
            </button>
          </div>
          <button
            v-else
            type="button"
            class="shrink-0 px-4 py-2 rounded-xl bg-cyan-600 hover:bg-cyan-500 text-white text-sm font-medium disabled:opacity-50 transition-colors"
            :disabled="savingLabor || dayVerified"
            @click="saveLaborRoster"
          >
            {{ savingLabor ? '保存中…' : '保存劳工名单' }}
          </button>
        </div>

        <div v-if="laborCandidates.length === 0" class="text-gray-500 text-sm py-6 text-center">
          暂无可选玩家
        </div>
        <div v-else-if="!dayVerified || isDm" class="flex flex-wrap gap-2">
          <button
            v-for="p in laborCandidates"
            :key="p.id"
            type="button"
            class="px-3 py-2 rounded-xl border text-sm transition-all text-left disabled:opacity-40"
            :class="rosterPlayerIds.has(p.id)
              ? 'border-cyan-500 bg-cyan-500/20 text-cyan-100'
              : 'border-white/10 bg-black/20 text-gray-300 hover:border-white/25'"
            :disabled="!isDm && dayVerified"
            @click="toggleLabor(p.id)"
          >
            <span class="font-medium">{{ p.name }}</span>
            <span class="text-gray-500 text-xs ml-1">{{ p.jobName }}</span>
            <span
              v-if="p.isProfessional"
              class="ml-1 px-1.5 py-0.5 rounded text-[10px] bg-blue-500/20 text-blue-400 border border-blue-500/30"
            >职业</span>
            <span class="text-gray-600 text-xs ml-1">({{ p.baseBuildValue || (p.isProfessional ? 5 : 4) }})</span>
          </button>
        </div>

        <!-- DM: full row editor -->
        <div v-if="isDm && dailyLabor.length" class="mt-4 space-y-2">
          <div
            v-for="row in dailyLabor"
            :key="row.playerId"
            class="flex flex-wrap items-center gap-3 px-3 py-3 rounded-xl border border-white/10 bg-black/20"
            :class="row.escaped ? 'opacity-60' : ''"
          >
            <div class="min-w-[100px] flex-1">
              <span class="text-white text-sm font-medium">{{ row.name }}</span>
              <span class="text-gray-500 text-xs ml-2">{{ row.jobName }}</span>
              <span
                v-if="row.isProfessional"
                class="ml-1 px-1.5 py-0.5 rounded text-[10px] bg-blue-500/20 text-blue-400 border border-blue-500/30"
              >职业</span>
            </div>
            <span class="text-emerald-400 font-semibold text-sm tabular-nums">
              {{ row.escaped ? 0 : (row.baseBuildValue || (row.isProfessional ? 5 : 4)) + (row.exploited ? 3 : 0) }}
              <span class="text-gray-500 text-xs font-normal">建造值</span>
            </span>
            <label class="flex items-center gap-1.5 text-xs text-red-300 cursor-pointer">
              <input v-model="row.exploited" type="checkbox" class="rounded" :disabled="dayVerified" @change="onExploitChange(row)" />
              压榨
            </label>
            <label class="flex items-center gap-1.5 text-xs text-amber-300 cursor-pointer">
              <input v-model="row.escaped" type="checkbox" class="rounded" :disabled="dayVerified" @change="onEscapedChange(row)" />
              逃役
            </label>
            <button
              v-if="!dayVerified"
              type="button"
              class="text-gray-500 hover:text-red-400 text-xs ml-auto"
              @click="removeLaborRow(row.playerId)"
            >
              移除
            </button>
          </div>
        </div>

        <!-- 统治者：已选劳工与压榨标记 -->
        <div v-else-if="!isDm && dailyLabor.length" class="mt-4 space-y-2">
          <div
            v-for="row in dailyLabor"
            :key="row.playerId"
            class="flex flex-wrap items-center gap-3 px-3 py-3 rounded-xl border border-white/10 bg-black/20"
          >
            <div class="min-w-[100px] flex-1">
              <span class="text-white text-sm font-medium">{{ row.name }}</span>
              <span class="text-gray-500 text-xs ml-2">{{ row.jobName }}</span>
              <span
                v-if="row.isProfessional"
                class="ml-1 px-1.5 py-0.5 rounded text-[10px] bg-blue-500/20 text-blue-400 border border-blue-500/30"
              >职业</span>
            </div>
            <span class="text-emerald-400 font-semibold text-sm tabular-nums">
              {{ row.escaped ? 0 : (row.baseBuildValue || (row.isProfessional ? 5 : 4)) + (row.exploited ? 3 : 0) }}
              <span class="text-gray-500 text-xs font-normal">建造值</span>
            </span>
            <label
              class="flex items-center gap-1.5 text-xs text-red-300 cursor-pointer"
              :class="dayVerified ? 'opacity-50 pointer-events-none' : ''"
            >
              <input
                v-model="row.exploited"
                type="checkbox"
                class="rounded"
                :disabled="dayVerified"
                @change="onExploitChange(row)"
              />
              压榨
            </label>
          </div>
          <p class="text-gray-500 text-xs">已压榨 {{ dailyLabor.filter(r => r.exploited).length }} / 3 人</p>
        </div>
      </div>

      <div class="mb-10">
        <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 mb-4">
          <h2 class="text-2xl text-white tracking-tight">物资库存</h2>
          <button
            v-if="isDm"
            type="button"
            class="shrink-0 px-4 py-2 rounded-xl bg-cyan-600/30 hover:bg-cyan-600/40 text-cyan-300 text-sm font-medium border border-cyan-500/30 disabled:opacity-50 transition-colors"
            :disabled="savingStock"
            @click="openAddStockModal"
          >
            添加物资
          </button>
        </div>
        <div class="bg-gradient-to-br from-[#1a2332] to-[#0f1419] border border-white/10 rounded-3xl p-5 md:p-6">
          <div v-if="!shelterDisplayRows.length" class="text-center text-gray-500 text-sm py-12">
            {{ isDm ? '暂无物资，可点击「添加物资」' : '暂无物资库存' }}
          </div>
          <div v-else class="flex flex-col lg:flex-row gap-6 lg:gap-8">
            <div class="flex-1 min-w-0">
              <div class="grid grid-cols-4 sm:grid-cols-5 md:grid-cols-6 gap-2 max-h-[min(420px,50vh)] overflow-y-auto pr-1">
                <button
                  v-for="row in shelterDisplayRows"
                  :key="row.id"
                  type="button"
                  class="relative aspect-square rounded-xl border-2 flex flex-col items-center justify-center p-1 transition-all duration-200"
                  :class="selectedShelterItemId === row.id ? 'border-cyan-500 bg-white/10' : 'border-white/10 bg-black/20'"
                  @click="selectedShelterItemId = row.id"
                >
                  <img :src="row.imageUrl" :alt="row.name" class="w-[72%] h-[72%] object-contain pointer-events-none select-none" />
                  <span class="absolute bottom-1 right-1.5 text-white text-[10px] sm:text-xs font-semibold tabular-nums">
                    {{ row.quantity }}
                  </span>
                </button>
              </div>
            </div>

            <div class="w-full lg:w-72 shrink-0 rounded-2xl border border-white/10 bg-black/25 p-5 flex flex-col min-h-[220px]">
              <template v-if="selectedShelterRow">
                <div class="flex justify-center mb-3">
                  <img :src="selectedShelterRow.imageUrl" :alt="selectedShelterRow.name" class="w-24 h-24 object-contain" />
                </div>
                <div class="flex items-center justify-center gap-2 mb-2 flex-wrap">
                  <h3 class="text-xl text-white font-medium text-center">{{ selectedShelterRow.name }}</h3>
                  <span class="px-2 py-0.5 rounded-full text-[10px] font-medium bg-white/10 text-gray-400 border border-white/10">
                    {{ shelterCategoryLabel(selectedShelterRow.category) }}
                  </span>
                </div>
                <p v-if="!isDm" class="text-cyan-400 text-center text-sm mb-3 tabular-nums">数量：{{ selectedShelterRow.quantity }}</p>
                <div v-else class="mb-3 space-y-2">
                  <label class="flex items-center justify-center gap-2 text-sm text-gray-400">
                    数量
                    <input
                      v-model.number="stockEditQty"
                      type="number"
                      min="0"
                      class="w-20 bg-black/40 border border-white/10 rounded px-2 py-1 text-white text-sm text-right"
                    />
                  </label>
                  <div class="flex gap-2 justify-center">
                    <button
                      type="button"
                      class="px-3 py-1.5 rounded-lg bg-cyan-600 hover:bg-cyan-500 text-white text-xs font-medium disabled:opacity-50"
                      :disabled="savingStock"
                      @click="saveShelterStockQty"
                    >
                      {{ savingStock ? '保存中…' : '保存数量' }}
                    </button>
                    <button
                      type="button"
                      class="px-3 py-1.5 rounded-lg bg-red-500/20 text-red-300 border border-red-500/30 text-xs disabled:opacity-50"
                      :disabled="savingStock"
                      @click="removeShelterStockItem"
                    >
                      移除
                    </button>
                  </div>
                </div>
                <p class="text-gray-400 text-sm leading-relaxed text-left flex-1 overflow-y-auto max-h-48">
                  {{ selectedShelterRow.description }}
                </p>
              </template>
              <div v-else class="flex items-center justify-center flex-1 text-gray-600 text-sm text-center">
                点击左侧物品查看详情
              </div>
            </div>
          </div>
        </div>
      </div>

      <div>
        <h2 class="text-2xl text-white mb-4 tracking-tight">建造日志</h2>
        <div v-if="!buildLogs.length" class="text-gray-500 text-sm py-8 text-center border border-dashed border-white/10 rounded-3xl">
          暂无建造记录
        </div>
        <div v-else class="space-y-4">
          <div
            v-for="log in buildLogs"
            :key="log.day"
            class="relative bg-gradient-to-br from-[#1a2332] to-[#0f1419] border border-white/10 rounded-3xl p-5"
          >
            <div class="flex flex-col sm:flex-row sm:items-start sm:justify-between gap-3 mb-4">
              <h3 class="text-xl text-white font-medium">第 {{ log.day }} 天</h3>
              <div class="text-left sm:text-right">
                <span
                  v-if="!log.verified"
                  class="inline-block px-3 py-1 rounded-full text-sm font-medium bg-amber-500/20 text-amber-300 border border-amber-500/30"
                >
                  结算中
                </span>
                <div v-else class="text-cyan-400 font-semibold tabular-nums">
                  +{{ log.dayTotal }} 建造值
                </div>
              </div>
            </div>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-2">
              <div
                v-for="(worker, wi) in (log.workers || [])"
                :key="`${log.day}-${worker.playerId ?? worker.name}-${wi}`"
                class="flex items-center justify-between px-4 py-2 rounded-xl border border-white/10 bg-black/20"
                :class="isDm && worker.escaped ? 'opacity-60' : ''"
              >
                <div class="flex items-center gap-2 flex-wrap min-w-0">
                  <span class="text-cyan-400 font-medium text-sm shrink-0">{{ worker.name }}</span>
                  <span v-if="isDm && worker.jobName" class="text-gray-500 text-xs shrink-0">{{ worker.jobName }}</span>
                  <span v-if="log.verified && worker.isProfessional" class="px-2 py-0.5 bg-blue-500/20 text-blue-400 text-xs rounded shrink-0">
                    职业
                  </span>
                  <span v-if="log.verified && worker.isOppressed" class="px-2 py-0.5 bg-red-500/20 text-red-400 text-xs rounded shrink-0">
                    压榨
                  </span>
                  <span v-if="isDm && log.verified && worker.escaped" class="px-2 py-0.5 bg-amber-500/20 text-amber-400 text-xs rounded shrink-0">
                    逃役
                  </span>
                </div>
                <span
                  v-if="isDm && log.verified"
                  class="text-emerald-400 font-semibold text-sm tabular-nums shrink-0"
                >
                  +{{ worker.value }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </template>

    <div
      v-if="showAddStockModal && isDm"
      class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/70"
      @click.self="showAddStockModal = false"
    >
      <div class="bg-[#1a2332] border border-white/10 rounded-2xl w-full max-w-2xl max-h-[85vh] flex flex-col shadow-xl">
        <div class="p-5 border-b border-white/10 flex justify-between items-center">
          <h3 class="text-white text-lg font-medium">添加物资到避难所</h3>
          <button type="button" class="text-gray-500 hover:text-white" @click="showAddStockModal = false">✕</button>
        </div>
        <div class="p-4 flex gap-2 border-b border-white/5">
          <input
            v-model="addSearch"
            type="text"
            placeholder="搜索..."
            class="flex-1 bg-black/30 border border-white/10 rounded-lg px-3 py-2 text-sm text-gray-200"
          />
          <select
            v-model="addFilterType"
            class="bg-black/30 border border-white/10 rounded-lg px-3 py-2 text-sm text-gray-200"
          >
            <option value="all">全部</option>
            <option value="item">道具</option>
            <option value="weapon">武器</option>
            <option value="ammo">弹药</option>
            <option value="material">材料</option>
          </select>
        </div>
        <div class="flex-1 overflow-y-auto p-4 grid grid-cols-4 sm:grid-cols-5 gap-2 min-h-[200px]">
          <button
            v-for="item in addCatalogFiltered"
            :key="`${item.itemType}-${item.itemId}`"
            type="button"
            class="aspect-square rounded-xl border-2 p-1 flex flex-col items-center justify-center transition-all"
            :class="
              addSelected &&
              addSelected.itemType === item.itemType &&
              addSelected.itemId === item.itemId
                ? 'border-cyan-500 bg-cyan-500/10'
                : 'border-white/10 bg-black/20 hover:border-white/20'
            "
            @click="addSelected = item"
          >
            <img :src="item.imageUrl" :alt="item.name" class="w-[70%] h-[70%] object-contain" />
            <span class="text-[9px] text-gray-400 mt-1 truncate w-full text-center px-0.5">{{ item.name }}</span>
          </button>
        </div>
        <div class="p-4 border-t border-white/10 flex items-center justify-between gap-4">
          <div class="flex items-center gap-2 text-sm text-gray-400">
            <span>数量</span>
            <input
              v-model.number="addQuantity"
              type="number"
              min="1"
              class="w-20 bg-black/30 border border-white/10 rounded px-2 py-1 text-white text-center"
            />
            <span v-if="addSelected">{{ addSelected.unit }}</span>
          </div>
          <div class="flex gap-2">
            <button
              type="button"
              class="px-4 py-2 text-gray-400 rounded-lg hover:bg-white/5"
              @click="showAddStockModal = false"
            >
              取消
            </button>
            <button
              type="button"
              class="px-4 py-2 bg-cyan-600 text-white rounded-lg disabled:opacity-50"
              :disabled="!addSelected || savingStock"
              @click="confirmAddShelterStock"
            >
              添加
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
