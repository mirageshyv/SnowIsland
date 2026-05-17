<template>
  <div
    class="relative overflow-hidden"
    :class="embedded ? '' : 'min-h-screen bg-[#0a0a0f]'"
  >
    <!-- Animated background -->
    <div class="pointer-events-none absolute inset-0 overflow-hidden" :class="embedded ? '' : 'fixed'">
      <div class="cat-bg-orb cat-bg-orb-1 absolute top-0 left-1/4 h-[800px] w-[800px] rounded-full bg-gradient-to-br from-purple-600/15 via-pink-500/10 to-transparent blur-[120px]" />
      <div class="cat-bg-orb cat-bg-orb-2 absolute bottom-0 right-1/4 h-[700px] w-[700px] rounded-full bg-gradient-to-tl from-red-500/15 via-orange-500/10 to-transparent blur-[120px]" />
      <div class="cat-bg-orb cat-bg-orb-3 absolute top-1/2 left-1/2 h-[600px] w-[600px] -translate-x-1/2 -translate-y-1/2 rounded-full bg-gradient-to-r from-cyan-500/10 to-blue-500/10 blur-[100px]" />
    </div>

    <div class="relative mx-auto max-w-5xl px-4 py-4 md:px-6 md:py-6" :class="embedded ? '' : 'py-6'">
      <!-- Header -->
      <header class="cat-fade-in mb-6 mt-2 text-center md:mt-6">
        <div class="relative mb-2 inline-block">
          <div class="absolute -inset-4 animate-pulse rounded-full bg-gradient-to-r from-red-500/20 via-orange-500/30 to-purple-500/20 opacity-70 blur-3xl" />
          <h1 class="relative text-3xl font-black tracking-tight text-white md:text-4xl">
            <span class="bg-gradient-to-r from-red-300 via-orange-300 to-purple-300 bg-clip-text text-transparent drop-shadow-[0_0_50px_rgba(239,68,68,0.5)]">
              天灾降临
            </span>
          </h1>
        </div>
        <p class="text-sm tracking-wide text-gray-400/90">
          {{ isDm ? '管理天灾进度和天灾牌系统' : '掌控天灾的力量，决定岛屿的命运' }}
        </p>
      </header>

      <div
        class="grid grid-cols-1 gap-4"
        :class="isDm ? 'lg:grid-cols-3' : 'max-w-3xl lg:mx-auto'"
      >
        <!-- Main column -->
        <div class="space-y-4" :class="isDm ? 'lg:col-span-2' : ''">
          <!-- Progress -->
          <section class="cat-slide-up" style="animation-delay: 0.1s">
            <div class="group relative overflow-hidden rounded-2xl border border-purple-500/30 bg-gradient-to-br from-gray-900/90 to-gray-800/90 p-5 shadow-2xl backdrop-blur-2xl transition-all duration-500 hover:border-purple-400/50 hover:shadow-purple-500/20">
              <div class="absolute inset-0 bg-gradient-to-br from-purple-500/5 via-pink-500/5 to-transparent opacity-0 transition-opacity duration-500 group-hover:opacity-100" />
              <div class="absolute -right-20 -top-20 h-40 w-40 rounded-full bg-purple-500/20 blur-3xl" />

              <div class="relative">
                <div class="mb-4 flex items-center justify-between">
                  <h2 class="text-lg font-black tracking-tight text-white">命运之轮：天灾</h2>
                  <div
                    class="text-3xl font-black bg-clip-text text-transparent drop-shadow-[0_0_20px_rgba(16,185,129,0.4)]"
                    :class="progressDisplayClass"
                  >
                    {{ progress }}%
                  </div>
                </div>

                <!-- Progress bar -->
                <div class="mb-4">
                  <div class="relative h-8 overflow-hidden rounded-2xl border-2 border-purple-500/20 bg-gradient-to-r from-black/70 via-gray-900/70 to-black/70 shadow-[inset_0_4px_30px_rgba(0,0,0,0.9)]">
                    <div
                      class="absolute inset-0 blur-lg transition-all duration-1000 bg-gradient-to-r from-purple-500/20 via-pink-500/20 to-purple-500/20"
                      :style="{ width: progress + '%' }"
                    />
                    <div
                      class="relative h-full transition-all duration-1000 shadow-[0_0_40px_rgba(168,85,247,0.7)]"
                      :class="progressBarFillClass"
                      :style="{ width: progress + '%' }"
                    >
                      <div class="cat-shine absolute inset-0 bg-gradient-to-r from-transparent via-white/40 to-transparent" />
                      <div class="absolute inset-0 animate-pulse bg-white/10" />
                      <div v-if="progress > 0" class="absolute bottom-0 right-0 top-0 w-1 bg-white shadow-[0_0_30px_rgba(255,255,255,1)]" />
                    </div>
                    <div
                      v-for="marker in [33, 67]"
                      :key="marker"
                      class="absolute bottom-0 top-0 w-px bg-gray-600/50"
                      :style="{ left: marker + '%' }"
                    />
                  </div>
                  <div class="mt-3 flex justify-between px-1">
                    <div
                      v-for="stage in progressStages"
                      :key="stage.value"
                      class="text-center"
                    >
                      <div
                        class="text-xs font-bold transition-all duration-300"
                        :class="progress >= stage.value ? 'scale-110 text-purple-400' : 'text-gray-600'"
                      >
                        {{ stage.label }}
                      </div>
                    </div>
                  </div>
                </div>

                <div v-if="isDm" class="space-y-4">
                  <div class="flex gap-2">
                    <button
                      v-for="preset in progressStages"
                      :key="preset.value"
                      type="button"
                      class="flex-1 rounded-xl px-3 py-2 text-sm font-bold transition-all"
                      :class="progressInput === preset.value
                        ? 'bg-gradient-to-r from-purple-600 to-purple-500 text-white shadow-lg shadow-purple-500/30'
                        : 'border border-gray-700/60 bg-gray-900/80 text-gray-400 hover:border-purple-500/40 hover:text-white'"
                      @click="updateProgressValue(preset.value)"
                    >
                      {{ preset.label }}
                    </button>
                  </div>
                  <div class="flex items-center gap-3">
                    <input
                      v-model.number="progressInput"
                      type="range"
                      min="0"
                      max="100"
                      class="cat-slider flex-1"
                    />
                    <div class="min-w-[60px] rounded-xl border border-purple-500/30 bg-gradient-to-br from-gray-800/90 to-gray-900/90 px-4 py-1.5 text-center text-lg font-black text-white shadow-lg">
                      {{ progressInput }}
                    </div>
                  </div>
                  <button
                    type="button"
                    class="group relative w-full overflow-hidden rounded-xl bg-gradient-to-r from-purple-600 via-purple-500 to-purple-600 py-3 font-bold text-white shadow-xl transition-all duration-300 hover:scale-[1.02] hover:from-purple-500 hover:via-purple-400 hover:to-purple-500 hover:shadow-purple-500/50"
                    @click="submitProgress"
                  >
                    <div class="cat-shine absolute inset-0 bg-gradient-to-r from-transparent via-white/20 to-transparent opacity-0 group-hover:opacity-100" />
                    <span class="relative">更新天灾进度</span>
                  </button>
                </div>

                <div
                  v-if="progress >= 100"
                  class="mt-4 rounded-xl border border-red-500/50 bg-red-500/10 p-4 backdrop-blur-sm"
                >
                  <div class="flex items-center gap-2 font-semibold text-red-400">
                    <span class="text-xl">🔥</span>
                    <span>天灾已触发！游戏进入结算阶段</span>
                  </div>
                </div>
              </div>
            </div>
          </section>

          <!-- DM draw -->
          <section v-if="isDm" class="cat-slide-up" style="animation-delay: 0.2s">
            <div class="group relative overflow-hidden rounded-2xl border border-red-500/30 bg-gradient-to-br from-gray-900/90 to-gray-800/90 p-5 shadow-2xl backdrop-blur-2xl transition-all duration-500 hover:border-red-400/50 hover:shadow-red-500/20">
              <div class="absolute inset-0 bg-gradient-to-br from-red-500/5 to-orange-500/5 opacity-0 transition-opacity duration-500 group-hover:opacity-100" />
              <div class="absolute -bottom-20 -left-20 h-40 w-40 rounded-full bg-red-500/20 blur-3xl" />

              <div class="relative">
                <h2 class="mb-4 text-lg font-black tracking-tight text-white">DM抽取表</h2>

                <button
                  type="button"
                  :disabled="isDrawing"
                  class="group relative mb-4 w-full overflow-hidden rounded-xl bg-gradient-to-r from-red-600 via-red-500 to-red-600 py-3 font-bold text-white shadow-xl transition-all duration-300 hover:scale-[1.02] hover:from-red-500 hover:via-red-400 hover:to-red-500 hover:shadow-red-500/50 disabled:cursor-not-allowed disabled:opacity-50 disabled:hover:scale-100"
                  @click="drawCards"
                >
                  <div class="cat-shine absolute inset-0 bg-gradient-to-r from-transparent via-white/20 to-transparent opacity-0 group-hover:opacity-100" />
                  <span class="relative flex items-center justify-center gap-2">
                    <span v-if="isDrawing">⏳</span>
                    <span>抽取3张天灾牌</span>
                  </span>
                </button>

                <div v-if="drawnCards.length > 0" class="space-y-3">
                  <div class="flex items-center justify-between">
                    <span class="text-sm text-gray-400">抽取轮次: {{ currentDrawRound }}</span>
                    <button
                      type="button"
                      class="text-sm font-semibold text-purple-400 transition-colors hover:text-purple-300"
                      @click="drawCards"
                    >
                      重新抽取
                    </button>
                  </div>

                  <div
                    v-for="(card, index) in drawnCards"
                    :key="card.deckId"
                    class="rounded-xl border border-red-500/20 bg-gradient-to-br from-gray-900/70 to-gray-800/70 p-3 backdrop-blur-sm"
                  >
                    <div class="mb-1 flex items-start justify-between">
                      <span class="text-xs text-gray-500">#{{ index + 1 }}</span>
                      <span class="text-xs text-gray-500">ID: {{ card.cardNumber }}</span>
                    </div>
                    <h4 class="text-sm font-medium text-white">{{ card.name }}</h4>
                    <p class="mt-1 line-clamp-2 text-xs text-gray-400">{{ card.description }}</p>
                  </div>

                  <button
                    type="button"
                    class="w-full rounded-xl bg-gradient-to-r from-emerald-600 via-green-500 to-emerald-600 py-3 font-bold text-white shadow-xl transition-all duration-300 hover:scale-[1.02] hover:shadow-green-500/40"
                    @click="confirmCards"
                  >
                    确认并发送牌组
                  </button>
                </div>

                <div
                  v-else
                  class="flex min-h-[120px] flex-col items-center justify-center rounded-2xl border border-red-500/20 bg-gradient-to-br from-gray-900/70 to-gray-800/70 p-8 backdrop-blur-sm"
                >
                  <div class="mb-4 text-5xl opacity-50">🃏</div>
                  <p class="font-medium text-gray-400">尚未抽取卡牌</p>
                  <p class="mt-1 text-xs text-gray-500">点击上方按钮抽取</p>
                </div>

                <div class="mt-4 border-t border-gray-700/50 pt-4">
                  <button
                    type="button"
                    :disabled="isResetting"
                    class="w-full rounded-xl bg-gradient-to-r from-amber-600 via-yellow-600 to-amber-600 py-3 font-bold text-white shadow-lg transition-all hover:scale-[1.01] disabled:cursor-not-allowed disabled:opacity-50"
                    @click="resetCatastrophe"
                  >
                    <span class="flex items-center justify-center gap-2">
                      <span v-if="isResetting">⏳</span>
                      <span>🔄 天灾卡复原</span>
                    </span>
                  </button>
                  <p class="mt-2 text-center text-xs text-gray-500">复原将清除所有天灾牌操作记录，恢复到初始状态</p>
                </div>
              </div>
            </div>
          </section>

          <!-- Card selection -->
          <section class="cat-slide-up" :style="{ animationDelay: isDm ? '0.3s' : '0.2s' }">
            <div class="group relative overflow-hidden rounded-2xl border border-cyan-500/30 bg-gradient-to-br from-gray-900/90 to-gray-800/90 p-5 shadow-2xl backdrop-blur-2xl transition-all duration-500 hover:border-cyan-400/50 hover:shadow-cyan-500/20">
              <div class="absolute inset-0 bg-gradient-to-br from-cyan-500/5 to-blue-500/5 opacity-0 transition-opacity duration-500 group-hover:opacity-100" />
              <div class="absolute -left-20 -top-20 h-40 w-40 rounded-full bg-cyan-500/20 blur-3xl" />

              <div class="relative">
                <div class="mb-4 flex items-center justify-between gap-2">
                  <h2 class="text-lg font-black tracking-tight text-white">天灾牌选择</h2>
                  <span class="text-xs font-semibold text-cyan-400/70">选择一张天灾牌触发</span>
                </div>

                <div
                  v-if="selectableCards.length === 0"
                  class="flex min-h-[120px] flex-col items-center justify-center rounded-2xl border border-cyan-500/20 bg-gradient-to-br from-gray-900/70 to-gray-800/70 p-8 backdrop-blur-sm"
                >
                  <div class="mb-4 text-5xl opacity-50">🗂️</div>
                  <p class="font-medium text-gray-400">暂无可用的天灾牌</p>
                  <p class="mt-1 text-xs text-gray-500">等待DM发送新的牌组</p>
                </div>

                <div v-else class="grid grid-cols-1 gap-4 md:grid-cols-3">
                  <div
                    v-for="(card, index) in selectableCards"
                    :key="card.selectedId"
                    class="relative rounded-xl border-2 p-4 transition-all duration-300"
                    :class="cardSelectionClass(card)"
                    @click="selectCard(card)"
                  >
                    <div class="absolute -right-3 -top-3 flex h-8 w-8 items-center justify-center rounded-full border border-gray-600 bg-gray-800 text-sm font-bold text-gray-400">
                      {{ index + 1 }}
                    </div>
                    <h3 class="mb-2 text-lg font-bold text-white">{{ card.name }}</h3>
                    <p class="text-sm leading-relaxed text-gray-400">{{ card.description }}</p>
                    <div v-if="card.isSelected" class="mt-3 flex items-center gap-2 text-sm text-emerald-400">
                      <span>✓</span>
                      已选中
                    </div>
                  </div>
                </div>

                <div v-if="selectableCards.length > 0 && !isDm" class="mt-4 text-center">
                  <button
                    type="button"
                    :disabled="selectedCardId === null || hasConfirmedSelection"
                    class="group relative overflow-hidden rounded-xl px-8 py-3 font-bold text-white shadow-xl transition-all duration-300 disabled:cursor-not-allowed disabled:opacity-50"
                    :class="hasConfirmedSelection || selectedCardId === null
                      ? 'bg-gray-700'
                      : 'bg-gradient-to-r from-purple-600 via-purple-500 to-purple-600 hover:scale-[1.02] hover:shadow-purple-500/40'"
                    @click="confirmSelection"
                  >
                    <span class="relative">
                      {{ hasConfirmedSelection ? '今日天灾已确认' : '确认选择' }}
                    </span>
                  </button>
                  <p v-if="hasConfirmedSelection" class="mt-2 text-xs text-gray-500">等待DM发送新的天灾牌后可再次选择</p>
                </div>
              </div>
            </div>
          </section>
        </div>

        <!-- Sidebar: game state (DM) -->
        <div v-if="isDm" class="space-y-4">
          <section class="cat-slide-up" style="animation-delay: 0.15s">
            <div class="relative overflow-hidden rounded-2xl border border-gray-700/40 bg-gradient-to-br from-gray-900/90 to-gray-800/90 p-5 shadow-2xl backdrop-blur-2xl">
              <div class="absolute -right-10 -top-10 h-32 w-32 rounded-full bg-blue-500/10 blur-2xl" />

              <div class="relative">
                <h2 class="mb-4 flex items-center gap-2 text-base font-black text-white">
                  <div class="h-5 w-1 rounded-full bg-gradient-to-b from-cyan-500 to-blue-500" />
                  游戏状态
                </h2>

                <div class="space-y-2.5">
                  <div class="rounded-xl border border-gray-700/40 bg-gradient-to-br from-gray-900/80 to-gray-800/80 p-3 transition-all hover:border-gray-600/60">
                    <div class="mb-1.5 text-xs uppercase tracking-wider text-gray-400">当前天数</div>
                    <div class="text-2xl font-black bg-gradient-to-br from-white to-cyan-100 bg-clip-text text-transparent">
                      {{ gameState.currentDay }}
                    </div>
                  </div>
                  <div class="rounded-xl border border-gray-700/40 bg-gradient-to-br from-gray-900/80 to-gray-800/80 p-3 transition-all hover:border-gray-600/60">
                    <div class="mb-1.5 text-xs uppercase tracking-wider text-gray-400">当前阶段</div>
                    <div class="text-2xl font-black bg-gradient-to-br from-white to-blue-100 bg-clip-text text-transparent">
                      {{ gameState.currentPhase === 'DAY' ? '白天' : '夜晚' }}
                    </div>
                  </div>
                  <div class="rounded-xl border border-gray-700/40 bg-gradient-to-br from-gray-900/80 to-gray-800/80 p-3 transition-all hover:border-gray-600/60">
                    <div class="mb-1.5 text-xs uppercase tracking-wider text-gray-400">天灾触发</div>
                    <div
                      class="text-2xl font-black bg-clip-text text-transparent"
                      :class="gameState.catastropheTriggered
                        ? 'bg-gradient-to-br from-red-300 to-orange-300 text-red-400'
                        : 'bg-gradient-to-br from-white to-purple-100 text-gray-300'"
                    >
                      {{ gameState.catastropheTriggered ? '是' : '否' }}
                    </div>
                  </div>
                  <div class="rounded-xl border border-gray-700/40 bg-gradient-to-br from-gray-900/80 to-gray-800/80 p-3 transition-all hover:border-gray-600/60">
                    <div class="mb-1.5 text-xs uppercase tracking-wider text-gray-400">额外卡牌待触发</div>
                    <div
                      class="text-2xl font-black bg-clip-text text-transparent"
                      :class="gameState.extraCardDue
                        ? 'bg-gradient-to-br from-amber-300 to-yellow-200 text-amber-400'
                        : 'bg-gradient-to-br from-white to-pink-100 text-gray-300'"
                    >
                      {{ gameState.extraCardDue ? '是' : '否' }}
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </section>
        </div>
      </div>

      <!-- Advance day -->
      <section v-if="isDm" class="cat-slide-up mt-4" style="animation-delay: 0.35s">
        <button
          type="button"
          class="group relative w-full overflow-hidden rounded-xl bg-gradient-to-r from-blue-600 via-blue-500 to-blue-600 py-4 font-bold text-white shadow-2xl transition-all duration-300 hover:scale-[1.01] hover:from-blue-500 hover:via-blue-400 hover:to-blue-500 hover:shadow-blue-500/50"
          @click="advanceDay"
        >
          <div class="cat-shine absolute inset-0 bg-gradient-to-r from-transparent via-white/20 to-transparent opacity-0 group-hover:opacity-100" />
          <span class="relative">推进一天 (+{{ gameState.currentDay < 3 ? 33 : 34 }}进度)</span>
        </button>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { catastropheAPI } from '../utils/api.js'

const props = defineProps({
  isDm: {
    type: Boolean,
    default: false,
  },
  embedded: {
    type: Boolean,
    default: true,
  },
})

const userRole = props.isDm ? 'dm' : (localStorage.getItem('userRole') || '').toLowerCase()
const playerId = localStorage.getItem('playerId') || null

const progressStages = [
  { label: '0%', value: 0 },
  { label: '33%', value: 33 },
  { label: '67%', value: 67 },
  { label: '100%', value: 100 },
]

const progress = ref(0)
const progressInput = ref(0)
const selectableCards = ref([])
const selectedCardId = ref(null)
const drawnCards = ref([])
const currentDrawRound = ref(0)
const isDrawing = ref(false)
const isConfirming = ref(false)
const isResetting = ref(false)
const gameState = ref({
  currentDay: 1,
  currentPhase: 'DAY',
  isGameOver: false,
  catastropheTriggered: false,
  extraCardDue: false,
})

const progressDisplayClass = computed(() => {
  if (progress.value >= 100) return 'bg-gradient-to-br from-red-400 to-orange-500'
  if (progress.value >= 67) return 'bg-gradient-to-br from-orange-400 to-amber-500'
  if (progress.value >= 33) return 'bg-gradient-to-br from-yellow-400 to-lime-500'
  return 'bg-gradient-to-br from-green-400 to-emerald-500'
})

const progressBarFillClass = computed(() => {
  if (progress.value >= 100) return 'bg-gradient-to-r from-red-600 via-red-500 to-red-400'
  if (progress.value >= 67) return 'bg-gradient-to-r from-orange-600 via-orange-500 to-amber-500'
  if (progress.value >= 33) return 'bg-gradient-to-r from-yellow-600 via-amber-500 to-yellow-500'
  return 'bg-gradient-to-r from-purple-500 via-pink-500 to-purple-600'
})

const hasConfirmedSelection = computed(() => {
  return isConfirming.value || selectableCards.value.some(card => card.isSelected)
})

function cardSelectionClass(card) {
  if (card.isSelected) {
    return 'cursor-default border-emerald-500/60 bg-emerald-500/10 shadow-lg shadow-emerald-500/20'
  }
  if (selectedCardId.value === card.selectedId) {
    return 'cursor-pointer border-purple-500/60 bg-purple-500/15 shadow-lg shadow-purple-500/30 hover:scale-[1.02]'
  }
  if (!props.isDm && !hasConfirmedSelection.value) {
    return 'cursor-pointer border-gray-600/60 bg-gray-900/50 hover:scale-[1.02] hover:border-cyan-500/40'
  }
  return 'cursor-default border-gray-600/60 bg-gray-900/50'
}

const fetchProgress = async () => {
  try {
    const response = await catastropheAPI.getProgress()
    progress.value = response.progress
    progressInput.value = response.progress
  } catch (error) {
    console.error('获取进度失败:', error)
  }
}

const fetchSelectableCards = async () => {
  try {
    const response = await catastropheAPI.getSelectableCards()
    selectableCards.value = response.cards
    const confirmedCard = response.cards.find(c => c.isSelected)
    selectedCardId.value = confirmedCard?.selectedId || null
    if (!confirmedCard) {
      isConfirming.value = false
    }
  } catch (error) {
    console.error('获取可选择卡牌失败:', error)
  }
}

const fetchGameState = async () => {
  try {
    const response = await catastropheAPI.getGameState()
    gameState.value = response
  } catch (error) {
    console.error('获取游戏状态失败:', error)
  }
}

const updateProgressValue = (value) => {
  progressInput.value = value
}

const submitProgress = async () => {
  try {
    const response = await catastropheAPI.updateProgress(progressInput.value, userRole)
    if (response.success) {
      progress.value = response.progress
      alert(response.message)
      if (response.catastropheTriggered) {
        fetchGameState()
      }
    } else {
      alert(response.message)
    }
  } catch (error) {
    console.error('更新进度失败:', error)
  }
}

const drawCards = async () => {
  isDrawing.value = true
  try {
    const response = await catastropheAPI.drawCards(userRole)
    if (response.success) {
      drawnCards.value = response.cards
      currentDrawRound.value = response.drawRound
    } else {
      alert(response.message)
    }
  } catch (error) {
    console.error('抽取卡牌失败:', error)
  } finally {
    isDrawing.value = false
  }
}

const confirmCards = async () => {
  try {
    const response = await catastropheAPI.confirmCards(userRole)
    if (response.success) {
      alert(response.message)
      await fetchSelectableCards()
    } else {
      alert(response.message)
    }
  } catch (error) {
    console.error('确认牌组失败:', error)
  }
}

const selectCard = (card) => {
  if (!props.isDm && !hasConfirmedSelection.value) {
    selectedCardId.value = card.selectedId
  }
}

const confirmSelection = async () => {
  if (selectedCardId.value === null || hasConfirmedSelection.value) return

  isConfirming.value = true

  try {
    const response = await catastropheAPI.selectCard(selectedCardId.value, playerId, userRole)
    if (response.success) {
      await fetchSelectableCards()
    } else {
      isConfirming.value = false
      alert(response.message)
    }
  } catch (error) {
    isConfirming.value = false
    console.error('选择卡牌失败:', error)
  }
}

const advanceDay = async () => {
  try {
    const response = await catastropheAPI.advanceDay()
    if (response.success) {
      progress.value = response.progress
      progressInput.value = response.progress
      await fetchGameState()
      alert(`已推进到第${response.currentDay}天，天灾进度+${response.advanceAmount}`)
    } else {
      alert(response.message)
    }
  } catch (error) {
    console.error('推进天数失败:', error)
  }
}

const resetCatastrophe = async () => {
  if (!confirm('确定要复原天灾牌吗？这将清除所有天灾牌操作记录，恢复到初始状态。')) return

  isResetting.value = true
  try {
    const response = await catastropheAPI.resetCatastrophe(userRole)
    if (response.success) {
      drawnCards.value = []
      currentDrawRound.value = 0
      selectableCards.value = []
      selectedCardId.value = null
      isConfirming.value = false
      await fetchProgress()
      await fetchGameState()
      alert(response.message)
    } else {
      alert(response.message)
    }
  } catch (error) {
    console.error('天灾卡复原失败:', error)
  } finally {
    isResetting.value = false
  }
}

onMounted(() => {
  fetchProgress()
  fetchSelectableCards()
  fetchGameState()

  if (props.isDm) {
    catastropheAPI.getDrawnCards().then(response => {
      if (response.success && response.cards.length > 0) {
        drawnCards.value = response.cards
        currentDrawRound.value = response.drawRound
      }
    })
  }
})
</script>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

@keyframes cat-fadeIn {
  from { opacity: 0; transform: translateY(-20px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes cat-slideUp {
  from { opacity: 0; transform: translateY(40px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes cat-slowFloat {
  0%, 100% { opacity: 0.3; transform: translate(0, 0) scale(1); }
  33% { opacity: 0.5; transform: translate(30px, -30px) scale(1.05); }
  66% { opacity: 0.4; transform: translate(-20px, 20px) scale(0.98); }
}

@keyframes cat-shine {
  0% { transform: translateX(-100%) skewX(-15deg); }
  100% { transform: translateX(200%) skewX(-15deg); }
}

.cat-fade-in {
  animation: cat-fadeIn 1.2s ease-out;
}

.cat-slide-up {
  animation: cat-slideUp 0.9s ease-out forwards;
  opacity: 0;
}

.cat-bg-orb {
  animation: cat-slowFloat 12s ease-in-out infinite;
}

.cat-bg-orb-2 {
  animation-delay: 3s;
}

.cat-bg-orb-3 {
  animation-delay: 6s;
}

.cat-shine {
  animation: cat-shine 3s ease-in-out infinite;
}

.cat-slider {
  height: 0.5rem;
  appearance: none;
  cursor: pointer;
  border-radius: 9999px;
  background: linear-gradient(to right, rgba(55, 65, 81, 0.3), rgba(75, 85, 99, 0.3), rgba(55, 65, 81, 0.3));
}

.cat-slider::-webkit-slider-thumb {
  appearance: none;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  border: 2px solid rgba(255, 255, 255, 0.3);
  background: linear-gradient(135deg, #a855f7, #ec4899);
  box-shadow: 0 0 20px rgba(168, 85, 247, 0.7), 0 0 40px rgba(168, 85, 247, 0.4);
  transition: all 0.3s;
}

.cat-slider::-webkit-slider-thumb:hover {
  transform: scale(1.3);
  box-shadow: 0 0 30px rgba(168, 85, 247, 1), 0 0 60px rgba(168, 85, 247, 0.6);
}

.cat-slider::-moz-range-thumb {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  border: 2px solid rgba(255, 255, 255, 0.3);
  background: linear-gradient(135deg, #a855f7, #ec4899);
  box-shadow: 0 0 20px rgba(168, 85, 247, 0.7), 0 0 40px rgba(168, 85, 247, 0.4);
  transition: all 0.3s;
}

.cat-slider::-moz-range-thumb:hover {
  transform: scale(1.3);
}
</style>
