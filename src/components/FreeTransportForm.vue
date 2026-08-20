<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { playerAPI, warehouseAPI } from '@/utils/api.js'
import StyledSelect from '@/components/StyledSelect.vue'
import { sanitizeNonNegativeInt } from '@/data/gameData.js'
import {
  getTransportWeightPerUnit,
  resolveTransportMaxWeight,
  getTransportTotalWeight,
  buildTransportNotes,
} from '@/utils/transportForm.js'

const props = defineProps({
  disabled: { type: Boolean, default: false },
  playerId: { type: [Number, String], required: true },
})

const warehouses = ref([])
const warehouseStock = ref([])
const transportMode = ref('')
const transportSource = ref('')
const transportDest = ref('')
const transportItems = ref([])

const warehouseOptions = computed(() =>
  warehouses.value
    .filter((w) => w.accessible === true)
    .map((w) => ({ value: w.warehouseKey, label: w.warehouseName })),
)

const destWarehouseOptions = computed(() =>
  warehouseOptions.value.filter((o) => o.value !== transportSource.value),
)

const totalWeight = computed(() => getTransportTotalWeight(transportItems.value))
const maxWeight = computed(() =>
  resolveTransportMaxWeight({
    mode: transportMode.value,
    source: transportSource.value,
    dest: transportDest.value,
    free: true,
  }),
)

const showItems = computed(() => {
  const mode = transportMode.value
  if (!mode) return false
  if (['warehouse_to_warehouse', 'warehouse_to_player'].includes(mode) && transportSource.value) return true
  if (mode === 'player_to_warehouse') return true
  return false
})

const weightOver = computed(() => totalWeight.value > maxWeight.value)
const weightPct = computed(() => {
  if (!maxWeight.value) return 0
  return Math.min(100, (totalWeight.value / maxWeight.value) * 100)
})

const modes = [
  { value: 'warehouse_to_warehouse', label: '仓库 → 仓库' },
  { value: 'warehouse_to_player', label: '仓库 → 个人' },
  { value: 'player_to_warehouse', label: '个人 → 仓库' },
]

watch(transportMode, () => onTransportModeChange())
watch(transportSource, (nv) => {
  if (nv && ['warehouse_to_warehouse', 'warehouse_to_player'].includes(transportMode.value)) {
    loadWarehouseStock(nv)
  }
})

function onTransportModeChange() {
  transportSource.value = ''
  transportDest.value = ''
  transportItems.value = []
  const mode = transportMode.value
  if (mode === 'player_to_warehouse') {
    loadPlayerInventory()
  }
}

async function loadPlayerInventory() {
  try {
    const pid = parseInt(props.playerId, 10)
    if (isNaN(pid)) return
    const list = await playerAPI.getItems(pid)
    transportItems.value = (Array.isArray(list) ? list : [])
      .filter((item) => (item.quantity || 0) > 0)
      .map((item) => ({
        itemType: item.type || item.itemType,
        itemId: item.id ?? item.itemId,
        name: item.name || '未知物品',
        unit: item.unit || '个',
        available: item.quantity,
        quantity: 0,
        weightPerUnit: getTransportWeightPerUnit(item.type || item.itemType, item.id ?? item.itemId),
      }))
  } catch (e) {
    console.error('加载个人背包失败:', e)
    transportItems.value = []
  }
}

async function loadWarehouseStock(warehouseKey) {
  try {
    const userRole = localStorage.getItem('userRole') || ''
    const result = await warehouseAPI.getWarehouseStock(warehouseKey, parseInt(props.playerId, 10), userRole)
    if (result && Array.isArray(result.items)) {
      warehouseStock.value = result.items
      transportItems.value = result.items.map((item) => ({
        itemType: item.itemType,
        itemId: item.itemId,
        name: item.name,
        unit: item.unit,
        available: item.quantity,
        quantity: 0,
        weightPerUnit: getTransportWeightPerUnit(item.itemType, item.itemId),
      }))
    } else {
      transportItems.value = []
    }
  } catch (e) {
    console.error('加载仓库库存失败:', e)
    transportItems.value = []
  }
}

async function loadWarehouses() {
  try {
    const pid = parseInt(props.playerId, 10)
    if (isNaN(pid)) return
    const userRole = localStorage.getItem('userRole') || ''
    const result = await warehouseAPI.getAccessibleWarehouses(pid, userRole)
    warehouses.value = Array.isArray(result) ? result : []
  } catch (e) {
    console.error('加载仓库列表失败:', e)
  }
}

function onTransportQuantityInput(item) {
  const max = item.available ?? 0
  item.quantity = sanitizeNonNegativeInt(item.quantity, max)
}

function validate() {
  if (!transportMode.value) return '请选择搬运模式'
  const mode = transportMode.value
  if (['warehouse_to_warehouse', 'warehouse_to_player'].includes(mode) && !transportSource.value) {
    return '请选择源仓库'
  }
  if (['warehouse_to_warehouse', 'player_to_warehouse'].includes(mode) && !transportDest.value) {
    return '请选择目标仓库'
  }
  const hasItems = Array.isArray(transportItems.value) && transportItems.value.some((i) => i.quantity > 0)
  if (!hasItems) return '请至少选择一项搬运物资'
  if (totalWeight.value > maxWeight.value) {
    return `搬运总重量${totalWeight.value}kg超过上限${maxWeight.value}kg`
  }
  return null
}

function buildNotes() {
  return buildTransportNotes({
    mode: transportMode.value,
    source: transportSource.value,
    dest: transportDest.value,
    items: transportItems.value,
    tier: 'free',
  })
}

function reset() {
  transportMode.value = ''
  transportSource.value = ''
  transportDest.value = ''
  transportItems.value = []
}

onMounted(() => loadWarehouses())

defineExpose({ validate, buildNotes, reset })
</script>

<template>
  <div class="haul">
    <p class="haul-lead">
      占用 1 条共用额度。玩家↔仓库 50kg；仓库↔仓库小镇 100kg / 海岛 50kg。装卸工由服务端 ×2。
    </p>

    <div>
      <p class="haul-label">搬运路线</p>
      <div class="route-grid">
        <button
          v-for="m in modes"
          :key="m.value"
          type="button"
          class="route"
          :class="{ 'route-on': transportMode === m.value }"
          :disabled="disabled"
          @click="transportMode = m.value"
        >
          {{ m.label }}
        </button>
      </div>
    </div>

    <div v-if="['warehouse_to_warehouse', 'warehouse_to_player'].includes(transportMode)" class="haul-pair">
      <label class="haul-field">
        <span>源仓库（需钥匙）</span>
        <StyledSelect v-model="transportSource" :options="warehouseOptions" placeholder="选择仓库" :disabled="disabled" />
      </label>
      <label v-if="transportMode === 'warehouse_to_warehouse'" class="haul-field">
        <span>目标仓库（需钥匙）</span>
        <StyledSelect v-model="transportDest" :options="destWarehouseOptions" placeholder="选择仓库" :disabled="disabled" />
      </label>
    </div>

    <label v-if="transportMode === 'player_to_warehouse'" class="haul-field">
      <span>目标仓库（需钥匙）</span>
      <StyledSelect v-model="transportDest" :options="warehouseOptions" placeholder="选择仓库" :disabled="disabled" />
    </label>

    <div v-if="showItems && transportItems.length > 0" class="crate">
      <div class="crate-head">
        <span>搬运物资</span>
        <span class="crate-kg" :class="{ 'crate-over': weightOver }">{{ totalWeight }} / {{ maxWeight }} kg</span>
      </div>
      <div class="crate-meter">
        <div class="crate-meter-fill" :class="{ 'is-over': weightOver }" :style="{ width: `${weightPct}%` }" />
      </div>
      <ul class="crate-list">
        <li v-for="item in transportItems" :key="`${item.itemType}-${item.itemId}`" class="crate-row">
          <div class="crate-name">
            <strong>{{ item.name }}</strong>
            <em>库存 {{ item.available }}{{ item.unit }}</em>
          </div>
          <input
            v-model.number="item.quantity"
            type="number"
            min="0"
            step="1"
            inputmode="numeric"
            :max="item.available"
            :disabled="disabled"
            @input="onTransportQuantityInput(item)"
          />
          <span class="crate-w">{{ (item.quantity || 0) * item.weightPerUnit }}kg</span>
        </li>
      </ul>
    </div>
  </div>
</template>

<style scoped>
.haul {
  display: flex;
  flex-direction: column;
  gap: 0.85rem;
  color: #f3ead7;
  font-family: ui-sans-serif, "PingFang SC", "Hiragino Sans GB", "Noto Sans SC", "Microsoft YaHei", system-ui, sans-serif;
}

.haul-lead {
  margin: 0;
  font-size: 0.78rem;
  line-height: 1.55;
  color: rgba(243, 234, 215, 0.62);
}

.haul-label,
.haul-field span {
  display: block;
  margin-bottom: 0.4rem;
  font-size: 0.72rem;
  letter-spacing: 0.12em;
  color: #c9a36a;
}

.route-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0.4rem;
}

.route {
  padding: 0.55rem 0.5rem;
  border: 1px solid rgba(243, 234, 215, 0.12);
  border-radius: 0.25rem;
  background: rgba(26, 20, 16, 0.4);
  color: rgba(243, 234, 215, 0.78);
  font: inherit;
  font-size: 0.78rem;
  cursor: pointer;
}

.route:hover:not(:disabled) {
  border-color: rgba(201, 163, 106, 0.5);
}

.route-on {
  border-color: #c9a36a;
  background: rgba(201, 163, 106, 0.16);
  color: #f3ead7;
  box-shadow: inset 0 0 0 1px rgba(201, 163, 106, 0.25);
}

.route:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.haul-pair {
  display: grid;
  grid-template-columns: 1fr;
  gap: 0.65rem;
}

@media (min-width: 640px) {
  .haul-pair {
    grid-template-columns: 1fr 1fr;
  }
}

.haul-stack {
  display: flex;
  flex-direction: column;
  gap: 0.65rem;
}

.haul-note {
  margin: 0;
  padding: 0.6rem 0.75rem;
  border-radius: 0.2rem;
  font-size: 0.78rem;
  line-height: 1.5;
}

.haul-note-in {
  border: 1px solid rgba(111, 155, 116, 0.35);
  background: rgba(111, 155, 116, 0.1);
  color: #c5e0c6;
}

.haul-note-out {
  border: 1px solid rgba(212, 160, 23, 0.35);
  background: rgba(212, 160, 23, 0.1);
  color: #f0d48a;
}

.crate {
  padding: 0.7rem 0.75rem 0.55rem;
  border: 1px solid rgba(201, 163, 106, 0.22);
  border-radius: 0.25rem;
  background: rgba(18, 14, 12, 0.45);
}

.crate-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 0.45rem;
  font-size: 0.78rem;
  color: rgba(243, 234, 215, 0.75);
}

.crate-kg {
  font-variant-numeric: tabular-nums;
  color: #e0c08a;
}

.crate-over {
  color: #f0b49a;
}

.crate-meter {
  height: 3px;
  margin-bottom: 0.55rem;
  overflow: hidden;
  background: rgba(243, 234, 215, 0.08);
}

.crate-meter-fill {
  height: 100%;
  background: #c9a36a;
  transition: width 0.2s ease;
}

.crate-meter-fill.is-over {
  background: #c45c2a;
}

.crate-list {
  max-height: 13rem;
  margin: 0;
  padding: 0;
  overflow: auto;
  list-style: none;
}

.crate-row {
  display: grid;
  grid-template-columns: 1fr 3.4rem 2.6rem;
  align-items: center;
  gap: 0.45rem;
  padding: 0.4rem 0.15rem;
  border-bottom: 1px dashed rgba(201, 163, 106, 0.12);
}

.crate-row:last-child {
  border-bottom: 0;
}

.crate-name strong {
  display: block;
  font-size: 0.82rem;
  font-weight: 600;
}

.crate-name em {
  font-style: normal;
  font-size: 0.68rem;
  color: rgba(243, 234, 215, 0.5);
}

.crate-row input {
  width: 100%;
  padding: 0.28rem 0.2rem;
  border: 1px solid rgba(201, 163, 106, 0.3);
  border-radius: 0.15rem;
  background: rgba(18, 14, 12, 0.8);
  color: #f3ead7;
  font: inherit;
  font-size: 0.8rem;
  text-align: center;
}

.crate-row input:focus {
  outline: none;
  border-color: #c9a36a;
}

.crate-w {
  font-size: 0.72rem;
  text-align: right;
  color: rgba(243, 234, 215, 0.55);
  font-variant-numeric: tabular-nums;
}
</style>
