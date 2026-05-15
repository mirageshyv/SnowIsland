<script setup>
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
</script>

<template>
  <div class="grid grid-cols-1 sm:grid-cols-2 gap-4 pt-6 mt-2 border-t border-white/10">
    <div class="rounded-xl border border-white/10 bg-black/25 p-4">
      <div class="flex items-center gap-2 mb-3">
        <span class="text-2xl leading-none" aria-hidden="true">🍞</span>
        <span class="text-gray-400 text-sm">食物供应</span>
      </div>
      <div class="flex items-baseline gap-2">
        <span class="text-amber-400 text-3xl font-bold tabular-nums">{{ food.quantity || 0 }}</span>
        <span class="text-gray-500 text-sm">{{ food.unit || 'kg' }}</span>
      </div>
      <p class="text-gray-500 text-xs mt-1">物资表 · material-5</p>
    </div>

    <div class="rounded-xl border border-white/10 bg-black/25 p-4">
      <div class="flex items-center gap-2 mb-3">
        <span class="text-2xl leading-none" aria-hidden="true">⚡</span>
        <span class="text-gray-400 text-sm">能量储备</span>
      </div>
      <div class="space-y-2">
        <div v-for="item in (energy.items || [])" :key="item.itemId" class="flex items-baseline justify-between">
          <span class="text-gray-400 text-sm">{{ item.name }}</span>
          <div class="flex items-baseline gap-1">
            <span class="text-yellow-400 text-xl font-bold tabular-nums">{{ item.quantity || 0 }}</span>
            <span class="text-gray-500 text-xs">{{ item.unit || 'kg' }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
