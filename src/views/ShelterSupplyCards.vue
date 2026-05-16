<script setup>
import { computed, ref } from 'vue'

const props = defineProps({
  food: {
    type: Object,
    required: true,
  },
  energy: {
    type: Object,
    required: true,
  },
})

const showFood = ref(false)

const foodItemsWithStock = computed(() =>
  (props.food.items || []).filter((item) => Number(item.quantity) > 0),
)

function energyUnitLabel(unit) {
  const m = { kg: '千克', L: '升' }
  return m[unit] || unit || ''
}

function toggleFood() {
  showFood.value = !showFood.value
}
</script>

<template>
  <div class="grid grid-cols-1 sm:grid-cols-2 gap-4 pt-6 mt-2 border-t border-white/10">
    <div class="rounded-xl border border-white/10 bg-black/25 overflow-hidden">
      <button
        type="button"
        class="w-full p-4 text-left transition-colors hover:bg-black/35"
        @click="toggleFood"
      >
        <div class="flex items-center justify-between mb-3">
          <div class="flex items-center gap-2">
            <span class="text-2xl leading-none" aria-hidden="true">🍞</span>
            <span class="text-gray-400 text-sm">食物供应</span>
          </div>
          <svg
            class="w-4 h-4 text-gray-500 transition-transform duration-200 shrink-0"
            :class="{ 'rotate-180': showFood }"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
          </svg>
        </div>
        <div class="flex items-baseline gap-2">
          <span class="text-amber-400 text-3xl font-bold tabular-nums">{{ food.totalKg ?? 0 }}</span>
          <span class="text-gray-500 text-sm">千克</span>
        </div>
      </button>

      <div
        v-show="showFood"
        class="border-t border-white/10 bg-[#151b2e]/90 px-4 py-3 max-h-72 overflow-y-auto"
      >
        <h4 class="text-white font-medium mb-3 text-sm">食物明细</h4>
        <p v-if="!foodItemsWithStock.length" class="text-gray-500 text-sm">暂无食物库存</p>
        <div v-else class="space-y-2">
          <div
            v-for="item in foodItemsWithStock"
            :key="item.id"
            class="flex items-center justify-between gap-2 text-sm"
          >
            <span class="text-gray-400">{{ item.name }}</span>
            <span class="text-amber-400 font-medium tabular-nums shrink-0">{{ item.quantity }} 千克</span>
          </div>
        </div>
      </div>
    </div>

    <div class="rounded-xl border border-white/10 bg-black/25 p-4">
      <div class="flex items-center gap-2 mb-3">
        <span class="text-2xl leading-none" aria-hidden="true">⚡</span>
        <span class="text-gray-400 text-sm">能量储备</span>
      </div>
      <div class="space-y-2">
        <div
          v-for="item in (energy.items || [])"
          :key="item.id"
          class="flex items-baseline justify-between text-sm"
        >
          <span class="text-gray-400">{{ item.name }}</span>
          <div class="flex items-baseline gap-1">
            <span class="text-yellow-400 text-xl font-bold tabular-nums">{{ item.quantity ?? 0 }}</span>
            <span class="text-gray-500 text-xs">{{ energyUnitLabel(item.unit) }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
