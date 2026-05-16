<script setup>
import { computed, ref, watch, onMounted } from 'vue'
import {
  SHELTER_DAILY_LOGS,
  resolveShelterInventoryRows,
} from '../data/gameData.js'
import { shelterAPI } from '../utils/api.js'
import ShelterSupplyCards from './ShelterSupplyCards.vue'

const loading = ref(true)
const loadError = ref(null)

const currentBuildValue = ref(0)
const foodSupply = ref({ totalKg: 0, items: [] })
const energyReserve = ref({ items: [] })
const shelterInventory = ref([])

const shelterDisplayRows = computed(() => resolveShelterInventoryRows(shelterInventory.value))

const selectedShelterItemId = ref(null)
const selectedShelterRow = computed(() => {
  if (!selectedShelterItemId.value) return null
  return shelterDisplayRows.value.find((r) => r.id === selectedShelterItemId.value) || null
})

function shelterCategoryLabel(c) {
  return { prop: '道具', weapon: '武器', material: '建材' }[c] || '未知'
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
    const data = await shelterAPI.getSummary()
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
          return { itemType: row.itemType, itemId: row.itemId, quantity: Number(row.quantity) || 0 }
        }
        return { id: String(row.id), quantity: Number(row.quantity) || 0 }
      })
    foodSupply.value = data.foodSupply
    energyReserve.value = data.energyReserve
  } catch (err) {
    console.error('加载避难所数据失败:', err)
    fail('网络错误，无法加载避难所数据')
  } finally {
    loading.value = false
  }
}

watch(shelterDisplayRows, (rows) => {
  if (loadError.value || !rows.length || selectedShelterItemId.value) return
  selectedShelterItemId.value = rows[0].id
}, { immediate: true })

onMounted(() => {
  loadShelterFromApi()
})
</script>

<template>
  <div class="max-w-5xl w-full mx-auto">
    <div class="text-center mb-10 pt-2">
      <h1 class="text-white text-3xl md:text-4xl font-semibold tracking-wide mb-3">统治者避难所建造进度</h1>
      <p class="text-gray-500 text-base md:text-lg">追踪避难所建造进展与库存物资</p>
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
          <p class="text-gray-400 text-lg mb-2">当前建造值</p>
          <p class="text-5xl md:text-6xl text-cyan-400 font-bold tabular-nums tracking-tight">
            {{ currentBuildValue }}
          </p>
        </div>
        <ShelterSupplyCards :food="foodSupply" :energy="energyReserve" />
      </div>

      <div class="mb-10">
        <h2 class="text-2xl text-white mb-4 tracking-tight">物资库存</h2>
        <div class="bg-gradient-to-br from-[#1a2332] to-[#0f1419] border border-white/10 rounded-3xl p-5 md:p-6">
          <div v-if="!shelterDisplayRows.length" class="text-center text-gray-500 text-sm py-12">
            暂无物资库存
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
                <p class="text-cyan-400 text-center text-sm mb-3 tabular-nums">数量：{{ selectedShelterRow.quantity }}</p>
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
        <p class="text-gray-600 text-xs mb-4">以下为演示数据；若需入库可再扩展 shelter_daily_log 表</p>
        <div class="space-y-4">
          <div
            v-for="log in SHELTER_DAILY_LOGS"
            :key="log.day"
            class="relative bg-gradient-to-br from-[#1a2332] to-[#0f1419] border border-white/10 rounded-3xl p-5"
          >
            <div class="flex flex-col sm:flex-row sm:items-start sm:justify-between gap-3 mb-4">
              <h3 class="text-xl text-white font-medium">第 {{ log.day }} 天</h3>
              <div class="text-left sm:text-right">
                <div class="text-cyan-400 font-semibold tabular-nums">
                  +{{ log.workers.reduce((s, w) => s + w.value, 0) }} 建造值
                </div>
              </div>
            </div>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-2">
              <div
                v-for="(worker, wi) in log.workers"
                :key="`${log.day}-${worker.name}-${wi}`"
                class="flex items-center justify-between px-4 py-2 rounded-xl border border-white/10 bg-black/20"
              >
                <div class="flex items-center gap-2 flex-wrap min-w-0">
                  <span class="text-cyan-400 font-medium text-sm shrink-0">{{ worker.name }}</span>
                  <span v-if="worker.isProfessional" class="px-2 py-0.5 bg-blue-500/20 text-blue-400 text-xs rounded shrink-0">
                    职业
                  </span>
                  <span v-if="worker.isOppressed" class="px-2 py-0.5 bg-red-500/20 text-red-400 text-xs rounded shrink-0">
                    压榨
                  </span>
                </div>
                <span class="text-emerald-400 font-semibold text-sm tabular-nums shrink-0">+{{ worker.value }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>
