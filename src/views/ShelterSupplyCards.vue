<script setup>
import { computed, ref } from 'vue'

const props = defineProps({
  food: {
    type: Object,
    required: true,
    /** { totalKcal, personDayDivisor, personDays, items: [...] } */
  },
  energy: {
    type: Object,
    required: true,
  },
})

const foodItemsNonZero = computed(() =>
  (props.food.items || []).filter((i) => Number(i.quantity) > 0),
)
const energyItemsNonZero = computed(() =>
  (props.energy.items || []).filter((i) => Number(i.quantity) > 0),
)

const showFood = ref(false)
const showEnergy = ref(false)

function unitLabel(u) {
  const m = { kg: '千克', portion: '份', L: '升' }
  return m[u] || u || ''
}

function toggleFood() {
  showFood.value = !showFood.value
  if (showFood.value) showEnergy.value = false
}

function toggleEnergy() {
  showEnergy.value = !showEnergy.value
  if (showEnergy.value) showFood.value = false
}
</script>

<template>
  <div class="grid grid-cols-1 sm:grid-cols-2 gap-4 pt-6 mt-2 border-t border-white/10">
    <!-- 食物供应 -->
    <div class="relative">
      <button
        type="button"
        class="w-full rounded-xl border border-white/10 bg-black/25 p-4 text-left transition-colors hover:bg-black/35"
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
        <div class="flex items-baseline gap-2 mb-2">
          <span class="text-amber-400 text-3xl font-bold tabular-nums">{{ food.personDays }}</span>
          <span class="text-gray-500 text-sm">天（1人·{{ food.personDayDivisor }} 大卡/天）</span>
        </div>
        <div class="text-gray-300 text-base tabular-nums">{{ food.totalKcal?.toLocaleString?.() ?? food.totalKcal }} 大卡</div>
      </button>

      <div
        v-show="showFood"
        class="absolute top-full left-0 right-0 z-10 mt-2 rounded-xl border border-white/10 bg-[#151b2e] p-4 shadow-xl max-h-72 overflow-y-auto"
      >
        <h4 class="text-white font-medium mb-3 text-sm">食物明细</h4>
        <div class="space-y-2">
          <p v-if="foodItemsNonZero.length === 0" class="text-gray-500 text-sm">当前无库存</p>
          <div
            v-for="item in foodItemsNonZero"
            :key="item.id"
            class="flex items-center justify-between gap-2 text-sm"
          >
            <div class="min-w-0">
              <span class="text-gray-400">{{ item.name }}</span>
              <span class="text-gray-600"> ×{{ item.quantity }}{{ unitLabel(item.unit) ? ` ${unitLabel(item.unit)}` : '' }}</span>
            </div>
            <span class="text-amber-400 font-medium tabular-nums shrink-0">{{ item.lineKcal?.toLocaleString?.() ?? item.lineKcal }} 大卡</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 能量储备 -->
    <div class="relative">
      <button
        type="button"
        class="w-full rounded-xl border border-white/10 bg-black/25 p-4 text-left transition-colors hover:bg-black/35"
        @click="toggleEnergy"
      >
        <div class="flex items-center justify-between mb-3">
          <div class="flex items-center gap-2">
            <span class="text-2xl leading-none" aria-hidden="true">⚡</span>
            <span class="text-gray-400 text-sm">能量储备</span>
          </div>
          <svg
            class="w-4 h-4 text-gray-500 transition-transform duration-200 shrink-0"
            :class="{ 'rotate-180': showEnergy }"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
          </svg>
        </div>
        <div class="flex items-baseline gap-2 mb-2">
          <span class="text-yellow-400 text-3xl font-bold tabular-nums">{{ energy.personDays }}</span>
          <span class="text-gray-500 text-sm">天（折算·{{ energy.personDayDivisor }} 大卡/天）</span>
        </div>
        <div class="text-gray-300 text-base tabular-nums">{{ energy.totalKcal?.toLocaleString?.() ?? energy.totalKcal }} 大卡（热量）</div>
      </button>

      <div
        v-show="showEnergy"
        class="absolute top-full left-0 right-0 z-10 mt-2 rounded-xl border border-white/10 bg-[#151b2e] p-4 shadow-xl"
      >
        <h4 class="text-white font-medium mb-3 text-sm">能量明细</h4>
        <div class="space-y-2">
          <p v-if="energyItemsNonZero.length === 0" class="text-gray-500 text-sm">当前无库存</p>
          <div
            v-for="item in energyItemsNonZero"
            :key="item.id"
            class="flex items-center justify-between text-sm"
          >
            <div class="flex items-center gap-2 min-w-0">
              <span class="text-gray-400">{{ item.name }}</span>
              <span class="text-gray-600">×{{ item.quantity }}{{ unitLabel(item.unit) ? ` ${unitLabel(item.unit)}` : '' }}</span>
            </div>
            <span class="text-yellow-400 font-medium tabular-nums shrink-0">{{ item.lineKcal?.toLocaleString?.() ?? item.lineKcal }} 大卡</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
