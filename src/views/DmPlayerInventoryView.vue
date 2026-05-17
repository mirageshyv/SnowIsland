<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { playerAPI, dmPlayerAPI } from '../utils/api.js'
import { getMaterialImageUrlOrDefault, getTypeTabImage, preloadMaterialImages } from '../data/gameData.js'

const props = defineProps({
  initialPlayerId: { type: Number, default: null }
})

const players = ref([])
const selectedPlayerId = ref(null)
const inventoryItems = ref([])
const catalogItems = ref([])
const summary = ref({ foodKg: 0, fuelKg: 0, fuelLiters: 0 })
const playerName = ref('')
const loadingPlayers = ref(true)
const loadingInventory = ref(false)

const searchQuery = ref('')
const filterType = ref('all')
const selectedItem = ref(null)
const editingItem = ref(null)
const editQuantity = ref(0)

const showAddModal = ref(false)
const addSearch = ref('')
const addFilterType = ref('all')
const addSelected = ref(null)
const addQuantity = ref(1)
const saving = ref(false)

const typeLabels = {
  item: '道具',
  weapon: '武器',
  ammo: '弹药',
  material: '材料',
  food: '食物',
  energy: '燃料'
}

const displayRows = computed(() => {
  let items = inventoryItems.value.map((item) => ({
    ...item,
    imageUrl: getMaterialImageUrlOrDefault(item.itemType, item.itemId)
  }))
  if (filterType.value !== 'all') {
    items = items.filter((i) => i.itemType === filterType.value)
  }
  if (searchQuery.value.trim()) {
    const q = searchQuery.value.trim().toLowerCase()
    items = items.filter((i) => (i.name || '').toLowerCase().includes(q))
  }
  return items
})

const groupedRows = computed(() => {
  const groups = {}
  const order = ['food', 'energy', 'material', 'weapon', 'ammo', 'item']
  for (const item of displayRows.value) {
    const type = item.itemType
    if (!groups[type]) groups[type] = []
    groups[type].push(item)
  }
  const sorted = {}
  for (const t of order) {
    if (groups[t]) sorted[t] = groups[t]
  }
  return sorted
})

const selectedRow = computed(() => {
  if (!selectedItem.value) return null
  return displayRows.value.find(
    (i) =>
      i.itemType === selectedItem.value.itemType && i.itemId === selectedItem.value.itemId
  )
})

const catalogForAdd = computed(() => {
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

async function loadPlayers() {
  loadingPlayers.value = true
  try {
    const list = await playerAPI.getAll()
    players.value = Array.isArray(list) ? list : []
    if (props.initialPlayerId && players.value.some((p) => p.id === props.initialPlayerId)) {
      selectedPlayerId.value = props.initialPlayerId
    } else if (!selectedPlayerId.value && players.value.length > 0) {
      selectedPlayerId.value = players.value[0].id
    }
  } catch (e) {
    console.error(e)
    players.value = []
  } finally {
    loadingPlayers.value = false
  }
}

async function loadCatalog() {
  const result = await dmPlayerAPI.getCatalog()
  if (result?.success) {
    catalogItems.value = result.items || []
  }
}

async function loadInventory() {
  if (!selectedPlayerId.value) {
    inventoryItems.value = []
    return
  }
  loadingInventory.value = true
  selectedItem.value = null
  editingItem.value = null
  try {
    const result = await dmPlayerAPI.getInventory(selectedPlayerId.value)
    if (result?.success) {
      inventoryItems.value = result.items || []
      playerName.value = result.playerName || ''
      summary.value = {
        foodKg: result.foodKg ?? 0,
        fuelKg: result.fuelKg ?? 0,
        fuelLiters: result.fuelLiters ?? 0
      }
    } else {
      inventoryItems.value = []
      if (result?.message) alert(result.message)
    }
  } catch (e) {
    console.error(e)
    inventoryItems.value = []
  } finally {
    loadingInventory.value = false
  }
}

function selectItem(item) {
  selectedItem.value = { itemType: item.itemType, itemId: item.itemId }
  editingItem.value = null
}

function startEdit(item) {
  editingItem.value = { itemType: item.itemType, itemId: item.itemId }
  editQuantity.value = item.quantity
}

function cancelEdit() {
  editingItem.value = null
}

async function persistQuantity(itemType, itemId, quantity) {
  saving.value = true
  try {
    const result = await dmPlayerAPI.setItemQuantity(selectedPlayerId.value, itemType, itemId, quantity)
    if (result?.success) {
      await loadInventory()
      editingItem.value = null
      showAddModal.value = false
      addSelected.value = null
    } else {
      alert(result?.message || '保存失败')
    }
  } catch (e) {
    alert('保存失败: ' + e.message)
  } finally {
    saving.value = false
  }
}

async function saveEdit() {
  if (!editingItem.value) return
  const q = Math.max(0, Math.floor(Number(editQuantity.value) || 0))
  await persistQuantity(editingItem.value.itemType, editingItem.value.itemId, q)
}

async function adjustSelected(delta) {
  if (!selectedRow.value) return
  const next = Math.max(0, (selectedRow.value.quantity || 0) + delta)
  await persistQuantity(selectedRow.value.itemType, selectedRow.value.itemId, next)
}

async function removeSelected() {
  if (!selectedRow.value) return
  if (!confirm(`确定移除 ${selectedRow.value.name}？`)) return
  await persistQuantity(selectedRow.value.itemType, selectedRow.value.itemId, 0)
}

function openAddModal() {
  addSelected.value = null
  addQuantity.value = 1
  addSearch.value = ''
  addFilterType.value = 'all'
  showAddModal.value = true
}

async function confirmAdd() {
  if (!addSelected.value) {
    alert('请选择要添加的物品')
    return
  }
  const q = Math.max(1, Math.floor(Number(addQuantity.value) || 1))
  const existing = inventoryItems.value.find(
    (i) =>
      i.itemType === addSelected.value.itemType && i.itemId === addSelected.value.itemId
  )
  const total = (existing?.quantity || 0) + q
  await persistQuantity(addSelected.value.itemType, addSelected.value.itemId, total)
}

watch(selectedPlayerId, () => {
  loadInventory()
})

watch(
  () => props.initialPlayerId,
  (id) => {
    if (id != null && players.value.some((p) => p.id === id)) {
      selectedPlayerId.value = id
    }
  }
)

onMounted(async () => {
  preloadMaterialImages()
  await Promise.all([loadPlayers(), loadCatalog()])
  if (selectedPlayerId.value) await loadInventory()
})

defineExpose({ openPlayer: (id) => { selectedPlayerId.value = id } })
</script>

<template>
  <div>
    <div class="mb-6 flex flex-col sm:flex-row sm:items-end justify-between gap-4">
      <div>
        <h1 class="text-white mb-1 tracking-tight text-2xl">玩家背包</h1>
        <p class="text-gray-500 text-sm">查看并编辑各玩家的道具、武器、弹药、材料、食物与燃料</p>
      </div>
      <div class="flex flex-wrap items-end gap-3 min-w-[240px]">
        <div class="flex-1 min-w-[200px]">
          <label class="block text-gray-400 text-xs mb-1">选择玩家</label>
          <select
            v-model.number="selectedPlayerId"
            class="w-full bg-black/30 border border-white/10 rounded-xl px-4 py-2.5 text-gray-200 text-sm focus:outline-none focus:border-cyan-500/50"
            :disabled="loadingPlayers"
          >
            <option v-for="p in players" :key="p.id" :value="p.id">
              {{ p.name }}（ID {{ p.id }}）
            </option>
          </select>
        </div>
        <button
          type="button"
          class="bg-cyan-600/30 hover:bg-cyan-600/40 text-cyan-300 px-4 py-2.5 rounded-xl text-sm transition-colors disabled:opacity-50"
          :disabled="!selectedPlayerId || saving"
          @click="openAddModal"
        >
          添加物品
        </button>
      </div>
    </div>

    <div
      v-if="selectedPlayerId && !loadingInventory"
      class="mb-4 flex flex-wrap gap-4 text-sm text-gray-400"
    >
      <span>{{ playerName }} — 食物合计 <span class="text-amber-300">{{ summary.foodKg }}</span> kg</span>
      <span>固体燃料 <span class="text-yellow-300">{{ summary.fuelKg }}</span> kg</span>
      <span v-if="summary.fuelLiters > 0">油料 <span class="text-yellow-300">{{ summary.fuelLiters }}</span> L</span>
    </div>

    <div v-if="loadingPlayers || loadingInventory" class="flex justify-center py-20">
      <div class="w-10 h-10 border-4 border-cyan-500 border-t-transparent rounded-full animate-spin" />
    </div>

    <div
      v-else-if="!selectedPlayerId"
      class="text-center py-16 text-gray-500"
    >
      请选择玩家
    </div>

    <div
      v-else
      class="bg-gradient-to-br from-[#1a2332] to-[#0f1419] border border-white/10 rounded-3xl p-5 md:p-6"
    >
      <div class="flex flex-col sm:flex-row gap-3 items-start sm:items-center justify-between mb-5">
        <span class="text-gray-500 text-sm">共 {{ inventoryItems.length }} 种物品</span>
        <div class="flex gap-2 w-full sm:w-auto">
          <input
            v-model="searchQuery"
            type="text"
            placeholder="搜索物品..."
            class="flex-1 sm:w-44 bg-black/30 border border-white/10 rounded-lg px-3 py-1.5 text-sm text-gray-200 placeholder-gray-500 focus:outline-none focus:border-cyan-500/50"
          />
          <select
            v-model="filterType"
            class="bg-black/30 border border-white/10 rounded-lg px-3 py-1.5 text-sm text-gray-200 focus:outline-none focus:border-cyan-500/50"
          >
            <option value="all">全部</option>
            <option value="food">食物</option>
            <option value="energy">燃料</option>
            <option value="item">道具</option>
            <option value="weapon">武器</option>
            <option value="ammo">弹药</option>
            <option value="material">材料</option>
          </select>
        </div>
      </div>

      <div v-if="displayRows.length === 0" class="py-16 text-center text-gray-500">
        {{ searchQuery || filterType !== 'all' ? '没有匹配的物品' : '背包为空，可点击「添加物品」' }}
      </div>

      <div v-else class="flex flex-col lg:flex-row gap-6 lg:gap-8">
        <div class="flex-1 min-w-0">
          <div v-for="(items, type) in groupedRows" :key="type" class="mb-4 last:mb-0">
            <div class="flex items-center gap-2 mb-2">
              <img :src="getTypeTabImage(type)" class="w-4 h-4 object-contain" alt="" />
              <span class="text-xs font-semibold text-gray-400 uppercase tracking-wider">
                {{ typeLabels[type] }}
              </span>
              <span class="text-gray-600 text-xs">({{ items.length }})</span>
            </div>
            <div class="grid grid-cols-4 sm:grid-cols-5 md:grid-cols-6 gap-2">
              <button
                v-for="item in items"
                :key="`${item.itemType}-${item.itemId}`"
                type="button"
                class="relative aspect-square rounded-xl border-2 flex flex-col items-center justify-center p-1 transition-all"
                :class="
                  selectedItem &&
                  selectedItem.itemType === item.itemType &&
                  selectedItem.itemId === item.itemId
                    ? 'border-cyan-500 bg-white/10'
                    : 'border-white/10 bg-black/20 hover:border-white/20 hover:bg-white/5'
                "
                @click="selectItem(item)"
              >
                <img
                  :src="item.imageUrl"
                  :alt="item.name"
                  class="w-[68%] h-[68%] object-contain pointer-events-none"
                />
                <span
                  class="absolute bottom-0.5 right-1.5 text-white text-[10px] sm:text-xs font-semibold tabular-nums drop-shadow-lg"
                >
                  {{ item.quantity }}
                </span>
              </button>
            </div>
          </div>
        </div>

        <div
          class="w-full lg:w-72 shrink-0 rounded-2xl border border-white/10 bg-black/25 p-5 flex flex-col min-h-[280px]"
        >
          <template v-if="selectedRow">
            <div class="flex justify-center mb-3">
              <img :src="selectedRow.imageUrl" :alt="selectedRow.name" class="w-24 h-24 object-contain" />
            </div>
            <h3 class="text-xl text-white font-medium text-center mb-1">{{ selectedRow.name }}</h3>
            <p class="text-center text-gray-500 text-xs mb-3">
              {{ typeLabels[selectedRow.itemType] }} · ID {{ selectedRow.itemId }}
            </p>

            <template
              v-if="
                editingItem &&
                editingItem.itemType === selectedRow.itemType &&
                editingItem.itemId === selectedRow.itemId
              "
            >
              <div class="flex items-center justify-center gap-2 my-3">
                <span class="text-gray-400 text-sm">数量</span>
                <input
                  v-model.number="editQuantity"
                  type="number"
                  min="0"
                  class="w-24 bg-black/30 border border-cyan-500/50 rounded px-2 py-1 text-sm text-center text-white focus:outline-none"
                  @keyup.enter="saveEdit"
                />
                <span class="text-gray-500 text-sm">{{ selectedRow.unit }}</span>
              </div>
              <div class="flex justify-center gap-2">
                <button
                  type="button"
                  class="px-4 py-1.5 bg-cyan-600/30 text-cyan-300 rounded-lg text-sm"
                  :disabled="saving"
                  @click="saveEdit"
                >
                  保存
                </button>
                <button
                  type="button"
                  class="px-4 py-1.5 bg-white/5 text-gray-400 rounded-lg text-sm"
                  @click="cancelEdit"
                >
                  取消
                </button>
              </div>
            </template>
            <template v-else>
              <p class="text-cyan-400 text-center text-lg mb-4 tabular-nums">
                {{ selectedRow.quantity }} {{ selectedRow.unit }}
              </p>
              <div class="flex justify-center gap-2 mb-3">
                <button
                  type="button"
                  class="w-9 h-9 rounded-lg bg-white/5 text-gray-300 hover:bg-white/10"
                  :disabled="saving"
                  @click="adjustSelected(-1)"
                >
                  −
                </button>
                <button
                  type="button"
                  class="px-3 py-1.5 bg-white/5 text-gray-300 rounded-lg text-sm hover:bg-white/10"
                  :disabled="saving"
                  @click="startEdit(selectedRow)"
                >
                  编辑
                </button>
                <button
                  type="button"
                  class="w-9 h-9 rounded-lg bg-white/5 text-gray-300 hover:bg-white/10"
                  :disabled="saving"
                  @click="adjustSelected(1)"
                >
                  +
                </button>
              </div>
              <button
                type="button"
                class="w-full py-2 text-red-400/90 bg-red-500/10 rounded-lg text-sm hover:bg-red-500/20 mb-2"
                :disabled="saving"
                @click="removeSelected"
              >
                移除（数量归零）
              </button>
            </template>
          </template>
          <div v-else class="flex-1 flex items-center justify-center text-gray-600 text-sm text-center">
            点击左侧物品进行管理
          </div>
        </div>
      </div>
      </div>

    <!-- Add item modal -->
    <div
      v-if="showAddModal"
      class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/70"
      @click.self="showAddModal = false"
    >
      <div
        class="bg-[#1a2332] border border-white/10 rounded-2xl w-full max-w-2xl max-h-[85vh] flex flex-col shadow-xl"
      >
        <div class="p-5 border-b border-white/10 flex justify-between items-center">
          <h3 class="text-white text-lg font-medium">添加物品</h3>
          <button type="button" class="text-gray-500 hover:text-white" @click="showAddModal = false">✕</button>
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
            <option value="food">食物</option>
            <option value="energy">燃料</option>
            <option value="item">道具</option>
            <option value="weapon">武器</option>
            <option value="ammo">弹药</option>
            <option value="material">材料</option>
          </select>
        </div>
        <div class="flex-1 overflow-y-auto p-4 grid grid-cols-4 sm:grid-cols-5 gap-2 min-h-[200px]">
          <button
            v-for="item in catalogForAdd"
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
              @click="showAddModal = false"
            >
              取消
            </button>
            <button
              type="button"
              class="px-4 py-2 bg-cyan-600 text-white rounded-lg disabled:opacity-50"
              :disabled="!addSelected || saving"
              @click="confirmAdd"
            >
              添加
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
