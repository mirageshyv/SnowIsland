<script setup>
import { getMaterialImageUrlOrDefault } from '../data/gameData.js'

const foodIconUrl = getMaterialImageUrlOrDefault('material', 5)
const fuelIconUrl = getMaterialImageUrlOrDefault('material', 8)

defineProps({
  food: { type: Object, required: true },
  energy: { type: Object, required: true },
  embedded: { type: Boolean, default: false },
  foodTitle: { type: String, default: '食物供应' },
  energyTitle: { type: String, default: '能量储备' },
})

function fuelQty(energy) {
  const items = energy?.items || []
  if (items.length === 0) return energy?.totalKg ?? 0
  return items.reduce((sum, item) => sum + Number(item.quantity || 0), 0)
}
</script>

<template>
  <div
    class="grid grid-cols-1 sm:grid-cols-2 gap-4"
    :class="embedded ? '' : 'pt-6 mt-2 border-t border-white/10'"
  >
    <div class="rounded-xl border border-white/10 bg-black/25 p-4">
      <div class="flex items-center gap-2 mb-3">
        <img :src="foodIconUrl" alt="" class="w-7 h-7 object-contain shrink-0" aria-hidden="true" />
        <span class="text-gray-400 text-sm">{{ foodTitle }}</span>
      </div>
      <div class="flex items-baseline gap-2">
        <span class="text-amber-400 text-3xl font-bold tabular-nums">{{ food.totalKg ?? 0 }}</span>
        <span class="text-gray-500 text-sm">千克</span>
      </div>
    </div>

    <div class="rounded-xl border border-white/10 bg-black/25 p-4">
      <div class="flex items-center gap-2 mb-3">
        <img :src="fuelIconUrl" alt="" class="w-7 h-7 object-contain shrink-0" aria-hidden="true" />
        <span class="text-gray-400 text-sm">{{ energyTitle }}</span>
      </div>
      <div class="flex items-baseline gap-2">
        <span class="text-yellow-400 text-3xl font-bold tabular-nums">{{ fuelQty(energy) }}</span>
        <span class="text-gray-500 text-sm">千克</span>
      </div>
    </div>
  </div>
</template>
