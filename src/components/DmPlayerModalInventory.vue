<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { dmPlayerAPI } from '../utils/api.js'
import { getMaterialImageUrlOrDefault } from '../data/gameData.js'

/** 仅用于「添加玩家」时预览/编辑初始背包，不支持已创建玩家的 live 编辑 */
const props = defineProps({
  modelValue: { type: Array, default: () => [] },
  jobId: { type: [Number, String], default: null },
  compact: { type: Boolean, default: false }
})

const emit = defineEmits(['update:modelValue'])

const typeLabels = {
  item: '道具',
  weapon: '武器',
  ammo: '弹药',
  material: '材料',
}

const loading = ref(false)
const catalogItems = ref([])
const showAdd = ref(false)
const addSearch = ref('')
const addFilter = ref('all')
const addPick = ref(null)
const addQty = ref(1)

const displayRows = computed(() =>
  (props.modelValue || []).map((item) => ({
    ...item,
    imageUrl: getMaterialImageUrlOrDefault(item.itemType, item.itemId),
    label: item.name || catalogLabel(item.itemType, item.itemId) || `${item.itemType}#${item.itemId}`
  }))
)

function catalogLabel(type, id) {
  const hit = catalogItems.value.find((c) => c.itemType === type && c.itemId === id)
  return hit?.name
}

async function loadCatalog() {
  const result = await dmPlayerAPI.getCatalog()
  if (result?.success) catalogItems.value = result.items || []
}

async function loadJobPreview() {
  if (!props.jobId) return
  loading.value = true
  try {
    const result = await dmPlayerAPI.previewStartingInventory(Number(props.jobId))
    if (result?.success) {
      const items = (result.items || []).map(enrichFromCatalog)
      emit('update:modelValue', items)
    }
  } finally {
    loading.value = false
  }
}

function enrichFromCatalog(item) {
  const name = catalogLabel(item.itemType, item.itemId)
  const cat = catalogItems.value.find(
    (c) => c.itemType === item.itemType && c.itemId === item.itemId
  )
  return {
    ...item,
    name: name || cat?.name,
    unit: item.unit || cat?.unit
  }
}

function updateRowQuantity(index, qty) {
  const next = [...(props.modelValue || [])]
  next[index] = { ...next[index], quantity: Math.max(0, Math.floor(Number(qty) || 0)) }
  emit('update:modelValue', next.filter((r) => r.quantity > 0))
}

function removeRow(index) {
  const next = (props.modelValue || []).filter((_, i) => i !== index)
  emit('update:modelValue', next)
}

const catalogForAdd = computed(() => {
  let items = catalogItems.value
  if (addFilter.value !== 'all') items = items.filter((i) => i.itemType === addFilter.value)
  if (addSearch.value.trim()) {
    const q = addSearch.value.trim().toLowerCase()
    items = items.filter((i) => (i.name || '').toLowerCase().includes(q))
  }
  return items
})

function confirmAdd() {
  if (!addPick.value) {
    alert('请选择物品')
    return
  }
  const q = Math.max(1, Math.floor(Number(addQty.value) || 1))
  const list = props.modelValue || []
  const existing = list.find(
    (i) => i.itemType === addPick.value.itemType && i.itemId === addPick.value.itemId
  )
  const next = [...list]
  if (existing) {
    const idx = next.indexOf(existing)
    next[idx] = { ...existing, quantity: (existing.quantity || 0) + q }
  } else {
    next.push({
      itemType: addPick.value.itemType,
      itemId: addPick.value.itemId,
      quantity: q,
      name: addPick.value.name,
      unit: addPick.value.unit
    })
  }
  emit('update:modelValue', next)
  showAdd.value = false
  addPick.value = null
}

defineExpose({ loadJobPreview })

watch(
  () => props.jobId,
  () => {
    if (props.jobId) loadJobPreview()
  }
)

onMounted(async () => {
  await loadCatalog()
  if (props.jobId) await loadJobPreview()
})
</script>

<template>
  <div class="border border-white/10 rounded-xl overflow-hidden bg-black/20">
    <div class="flex flex-wrap items-center justify-between gap-2 px-3 py-2 border-b border-white/10 bg-white/5">
      <span class="text-gray-400 text-xs">初始背包</span>
      <div class="flex flex-wrap gap-2">
        <button
          v-if="jobId"
          type="button"
          class="text-xs px-2 py-1 rounded-lg bg-white/10 text-gray-300 hover:bg-white/15 disabled:opacity-50"
          :disabled="loading"
          @click="loadJobPreview"
        >
          从职业刷新
        </button>
        <button
          type="button"
          class="text-xs px-2 py-1 rounded-lg bg-blue-500/20 text-blue-300 hover:bg-blue-500/30"
          @click="showAdd = true"
        >
          添加物品
        </button>
      </div>
    </div>

    <div v-if="loading" class="py-8 flex justify-center">
      <div class="w-8 h-8 border-2 border-cyan-500 border-t-transparent rounded-full animate-spin" />
    </div>
    <div v-else-if="displayRows.length === 0" class="py-6 text-center text-gray-500 text-xs">
      {{ jobId ? '选择职业后点「从职业刷新」，或手动添加' : '暂无物品' }}
    </div>
    <div v-else :class="compact ? 'max-h-48' : 'max-h-64'" class="overflow-y-auto divide-y divide-white/5">
      <div
        v-for="(row, idx) in displayRows"
        :key="`${row.itemType}-${row.itemId}-${idx}`"
        class="flex items-center gap-2 px-3 py-2 hover:bg-white/5"
      >
        <img v-if="row.imageUrl" :src="row.imageUrl" class="w-8 h-8 object-contain shrink-0" alt="" />
        <div class="flex-1 min-w-0">
          <div class="text-white text-xs truncate">{{ row.label }}</div>
          <div class="text-gray-500 text-[10px]">{{ typeLabels[row.itemType] || row.itemType }}</div>
        </div>
        <input
          :value="row.quantity"
          type="number"
          min="0"
          class="w-16 bg-black/40 border border-white/10 rounded px-2 py-1 text-white text-xs text-right"
          @change="updateRowQuantity(idx, $event.target.value)"
        />
        <span class="text-gray-500 text-[10px] w-6">{{ row.unit || '个' }}</span>
        <button type="button" class="text-red-400 text-xs hover:text-red-300" @click="removeRow(idx)">删</button>
      </div>
    </div>

    <div
      v-if="showAdd"
      class="fixed inset-0 z-[60] flex items-center justify-center p-4 bg-black/70"
      @click.self="showAdd = false"
    >
      <div class="bg-[#1a2332] border border-white/10 rounded-2xl w-full max-w-lg max-h-[80vh] flex flex-col p-4" @click.stop>
        <h4 class="text-white text-sm font-medium mb-3">添加物品</h4>
        <div class="flex gap-2 mb-3">
          <input
            v-model="addSearch"
            type="text"
            placeholder="搜索…"
            class="flex-1 bg-black/30 border border-white/10 rounded-lg px-3 py-2 text-white text-xs"
          />
          <select v-model="addFilter" class="bg-black/30 border border-white/10 rounded-lg px-2 text-white text-xs">
            <option value="all">全部</option>
            <option v-for="(label, key) in typeLabels" :key="key" :value="key">{{ label }}</option>
          </select>
        </div>
        <div class="flex-1 overflow-y-auto space-y-1 mb-3 min-h-[120px]">
          <button
            v-for="c in catalogForAdd"
            :key="`${c.itemType}-${c.itemId}`"
            type="button"
            class="w-full text-left px-3 py-2 rounded-lg text-xs transition-colors"
            :class="addPick?.itemType === c.itemType && addPick?.itemId === c.itemId ? 'bg-cyan-500/30 text-white' : 'hover:bg-white/5 text-gray-300'"
            @click="addPick = c"
          >
            {{ c.name }} <span class="text-gray-500">({{ typeLabels[c.itemType] }})</span>
          </button>
        </div>
        <div class="flex items-center gap-2 mb-3">
          <label class="text-gray-400 text-xs">数量</label>
          <input v-model.number="addQty" type="number" min="1" class="w-20 bg-black/30 border border-white/10 rounded px-2 py-1 text-white text-xs" />
        </div>
        <div class="flex justify-end gap-2">
          <button type="button" class="px-3 py-1.5 text-gray-400 text-xs rounded-lg hover:bg-white/5" @click="showAdd = false">取消</button>
          <button type="button" class="px-3 py-1.5 bg-cyan-600 text-white text-xs rounded-lg" @click="confirmAdd">确定</button>
        </div>
      </div>
    </div>
  </div>
</template>
