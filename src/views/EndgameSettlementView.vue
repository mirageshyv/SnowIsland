<script setup>
import { ref, computed } from 'vue'
import { endgameAPI } from '@/utils/api.js'

const activeMode = ref('shelter')
const drawing = ref(false)
const drawnEvent = ref(null)
const drawError = ref('')
const drawHistory = ref([])

const modeLabel = computed(() =>
  activeMode.value === 'shelter' ? '避难所' : '方舟'
)

const modeIcon = computed(() =>
  activeMode.value === 'shelter' ? '🏠' : '🚢'
)

async function drawEvent() {
  drawing.value = true
  drawError.value = ''
  drawnEvent.value = null
  try {
    const res = activeMode.value === 'shelter'
      ? await endgameAPI.drawShelterEvent()
      : await endgameAPI.drawArkEvent()
    if (res?.success && res.event) {
      drawnEvent.value = { ...res.event, mode: activeMode.value }
      drawHistory.value.unshift({ ...res.event, mode: activeMode.value })
    } else {
      drawError.value = res?.message || '抽取失败，事件池可能为空'
    }
  } catch {
    drawError.value = '抽取失败，请重试'
  } finally {
    drawing.value = false
  }
}

function switchMode(mode) {
  if (activeMode.value === mode) return
  activeMode.value = mode
  drawnEvent.value = null
  drawError.value = ''
}
</script>

<template>
  <div>
    <div class="text-center mb-8">
      <h1 class="text-white text-2xl md:text-3xl font-semibold tracking-tight mb-2">终局结算</h1>
      <p class="text-gray-500 text-sm">抽取终局事件，决定最终命运</p>
    </div>

    <!-- 模式切换 -->
    <div class="flex justify-center mb-8">
      <div class="inline-flex bg-white/5 border border-white/10 rounded-xl p-1">
        <button
          type="button"
          class="px-6 py-2.5 rounded-lg text-sm font-medium transition-all"
          :class="activeMode === 'shelter'
            ? 'bg-amber-500/20 text-amber-300 border border-amber-500/30'
            : 'text-gray-400 hover:text-gray-300'"
          @click="switchMode('shelter')"
        >
          🏠 避难所结算
        </button>
        <button
          type="button"
          class="px-6 py-2.5 rounded-lg text-sm font-medium transition-all"
          :class="activeMode === 'ark'
            ? 'bg-cyan-500/20 text-cyan-300 border border-cyan-500/30'
            : 'text-gray-400 hover:text-gray-300'"
          @click="switchMode('ark')"
        >
          🚢 方舟结算
        </button>
      </div>
    </div>

    <!-- 抽取按钮 -->
    <div class="flex justify-center mb-8">
      <button
        type="button"
        :disabled="drawing"
        class="relative min-w-[220px] bg-gradient-to-r from-violet-500 to-violet-600 hover:from-violet-600 hover:to-violet-700 disabled:from-gray-600 disabled:to-gray-600 text-white px-8 py-4 rounded-xl text-base font-medium transition-all hover:scale-105 active:scale-95"
        @click="drawEvent"
      >
        <span v-if="drawing" class="flex items-center justify-center gap-2">
          <span class="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
          抽取中...
        </span>
        <span v-else>{{ modeIcon }} 抽取{{ modeLabel }}事件</span>
      </button>
    </div>

    <!-- 错误提示 -->
    <div v-if="drawError" class="flex justify-center mb-6">
      <div class="px-5 py-2.5 rounded-xl text-sm bg-red-500/20 text-red-400 border border-red-500/30">
        {{ drawError }}
      </div>
    </div>

    <!-- 抽取结果 -->
    <div v-if="drawnEvent" class="max-w-2xl mx-auto mb-8">
      <div
        class="rounded-2xl border p-6 transition-all"
        :class="drawnEvent.mode === 'shelter'
          ? 'bg-gradient-to-br from-amber-500/10 to-amber-900/5 border-amber-500/20'
          : 'bg-gradient-to-br from-cyan-500/10 to-cyan-900/5 border-cyan-500/20'"
      >
        <div class="flex items-center gap-3 mb-4">
          <span
            class="inline-flex w-10 h-10 items-center justify-center rounded-xl text-lg"
            :class="drawnEvent.mode === 'shelter'
              ? 'bg-amber-500/20 border border-amber-500/30 text-amber-300'
              : 'bg-cyan-500/20 border border-cyan-500/30 text-cyan-300'"
          >
            {{ drawnEvent.mode === 'shelter' ? '🏠' : '🚢' }}
          </span>
          <div>
            <h2 class="text-white text-lg font-semibold">{{ drawnEvent.title }}</h2>
            <span
              class="text-xs px-2 py-0.5 rounded-full"
              :class="drawnEvent.mode === 'shelter'
                ? 'bg-amber-500/20 text-amber-400'
                : 'bg-cyan-500/20 text-cyan-400'"
            >
              {{ drawnEvent.mode === 'shelter' ? '避难所' : '方舟' }}事件
            </span>
            <span v-if="drawnEvent.category" class="text-xs text-gray-500 ml-2">{{ drawnEvent.category }}</span>
          </div>
        </div>
        <div class="text-gray-300 text-sm leading-relaxed whitespace-pre-wrap bg-black/20 rounded-xl p-4 border border-white/5">
          {{ drawnEvent.description }}
        </div>
      </div>
    </div>

    <!-- 抽取历史 -->
    <div v-if="drawHistory.length > 1" class="max-w-2xl mx-auto">
      <h3 class="text-white text-sm font-medium mb-3">抽取历史</h3>
      <div class="space-y-2">
        <div
          v-for="(evt, idx) in drawHistory.slice(1)"
          :key="idx"
          class="flex items-center gap-3 bg-white/5 border border-white/5 rounded-xl px-4 py-3"
        >
          <span class="text-sm">{{ evt.mode === 'shelter' ? '🏠' : '🚢' }}</span>
          <span class="text-gray-300 text-sm flex-1 truncate">{{ evt.title }}</span>
          <span
            class="text-xs px-2 py-0.5 rounded-full shrink-0"
            :class="evt.mode === 'shelter'
              ? 'bg-amber-500/10 text-amber-400/70'
              : 'bg-cyan-500/10 text-cyan-400/70'"
          >
            {{ evt.mode === 'shelter' ? '避难所' : '方舟' }}
          </span>
        </div>
      </div>
    </div>
  </div>
</template>
