<template>
  <div
    class="cat-page relative overflow-hidden"
    :class="embedded ? 'cat-page--embedded' : 'min-h-screen cat-page--standalone'"
  >
    <div class="cat-ambient pointer-events-none absolute inset-0 overflow-hidden" aria-hidden="true">
      <div class="cat-ambient-violet" />
      <div class="cat-ambient-ember" />
    </div>

    <div class="relative mx-auto max-w-5xl px-4 py-5 md:px-6 md:py-8">
      <header class="cat-hero mb-8 md:mb-10" :class="entranceClass">
        <div class="cat-hero-glow" aria-hidden="true" />
        <p class="cat-hero-eyebrow">CATASTROPHE PROTOCOL</p>
        <h1 class="cat-hero-title">天灾降临</h1>
        <p class="cat-hero-subtitle">
          {{ isDm ? '管理天灾进度与牌组，推动命运之轮' : '掌控天灾之力，为雪岛写下终局' }}
        </p>
        <div class="cat-hero-rule" />
      </header>

      <div
        class="grid grid-cols-1 gap-5"
        :class="isDm ? 'lg:grid-cols-3' : 'max-w-3xl lg:mx-auto'"
      >
        <!-- Main column -->
        <div class="space-y-5" :class="isDm ? 'lg:col-span-2' : ''">
          <!-- Progress -->
          <section :class="entranceClass" :style="embedded ? undefined : { animationDelay: '0.1s' }">
            <div class="cat-panel cat-panel--purple group">
              <div class="cat-panel-hover" />

              <div class="relative">
                <div class="mb-5 flex items-start justify-between gap-4">
                  <div>
                    <p class="cat-section-eyebrow">进度</p>
                    <h2 class="cat-section-title">命运之轮 · 天灾</h2>
                  </div>
                  <div
                    class="cat-progress-pct text-3xl font-black bg-clip-text text-transparent md:text-4xl"
                    :class="progressDisplayClass"
                  >
                    {{ progress }}%
                  </div>
                </div>

                <!-- Progress bar -->
                <div class="mb-4">
                  <div class="cat-progress-track relative h-9 overflow-hidden rounded-2xl">
                    <div
                      class="cat-progress-fill relative h-full transition-[width] duration-500 ease-out"
                      :class="progressBarFillClass"
                      :style="{ width: progress + '%' }"
                    />
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
                        class="text-xs font-bold transition-colors duration-200"
                        :class="progress >= stage.value ? 'text-purple-300' : 'text-gray-600'"
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
                    class="cat-btn cat-btn--purple w-full"
                    @click="submitProgress"
                  >
                    <span>更新天灾进度</span>
                  </button>
                </div>

                <div
                  v-if="progress >= 100"
                  class="mt-4 rounded-xl border border-red-500/50 bg-red-500/10 p-4"
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
          <section v-if="isDm" :class="entranceClass" :style="embedded ? undefined : { animationDelay: '0.2s' }">
            <div class="cat-panel cat-panel--red group">
              <div class="cat-panel-hover" />

              <div class="relative">
                <p class="cat-section-eyebrow">牌组</p>
                <h2 class="cat-section-title mb-5">DM 抽取表</h2>

                <button
                  type="button"
                  :disabled="isDrawing"
                  class="cat-btn cat-btn--red mb-4 w-full disabled:cursor-not-allowed disabled:opacity-50"
                  @click="drawCards"
                >
                  <span class="flex items-center justify-center gap-2">
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
                    class="cat-card-tile cat-card-tile--red"
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
                    class="cat-btn cat-btn--green w-full"
                    @click="confirmCards"
                  >
                    确认并发送牌组
                  </button>
                </div>

                <div
                  v-else
                  class="cat-empty-state cat-empty-state--red"
                >
                  <div class="mb-4 text-5xl opacity-50">🃏</div>
                  <p class="font-medium text-gray-400">尚未抽取卡牌</p>
                  <p class="mt-1 text-xs text-gray-500">点击上方按钮抽取</p>
                </div>

                <div class="mt-4 border-t border-gray-700/50 pt-4">
                  <button
                    type="button"
                    :disabled="isResetting"
                    class="w-full rounded-xl bg-gradient-to-r from-amber-600 via-yellow-600 to-amber-600 py-3 font-bold text-white shadow-lg transition-colors duration-200 disabled:cursor-not-allowed disabled:opacity-50"
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
          <section :class="entranceClass" :style="embedded ? undefined : { animationDelay: isDm ? '0.3s' : '0.2s' }">
            <div class="cat-panel cat-panel--cyan group">
              <div class="cat-panel-hover" />

              <div class="relative">
                <div class="mb-5 flex flex-wrap items-end justify-between gap-2">
                  <div>
                    <p class="cat-section-eyebrow">抉择</p>
                    <h2 class="cat-section-title">天灾牌选择</h2>
                  </div>
                  <span class="cat-section-hint">选择一张天灾牌触发</span>
                </div>

                <div
                  v-if="selectableCards.length === 0"
                  class="cat-empty-state cat-empty-state--cyan"
                >
                  <div class="mb-4 text-5xl opacity-50">🗂️</div>
                  <p class="font-medium text-gray-400">暂无可用的天灾牌</p>
                  <p class="mt-1 text-xs text-gray-500">等待DM发送新的牌组</p>
                </div>

                <div v-else class="grid grid-cols-1 gap-4 md:grid-cols-3">
                  <div
                    v-for="(card, index) in selectableCards"
                    :key="card.selectedId"
                    class="cat-play-card relative p-4 transition-[border-color,box-shadow,background] duration-150"
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
                      ? 'cat-btn cat-btn--muted'
                      : 'cat-btn cat-btn--purple'"
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
          <section :class="entranceClass" :style="embedded ? undefined : { animationDelay: '0.15s' }">
            <div class="cat-panel cat-panel--slate">
              <div class="relative">
                <p class="cat-section-eyebrow">概览</p>
                <h2 class="cat-section-title mb-5">游戏状态</h2>

                <div class="space-y-2.5">
                  <div class="cat-stat-tile">
                    <div class="mb-1.5 text-xs uppercase tracking-wider text-gray-400">当前天数</div>
                    <div class="text-2xl font-black bg-gradient-to-br from-white to-cyan-100 bg-clip-text text-transparent">
                      {{ gameState.currentDay }}
                    </div>
                  </div>
                  <div class="cat-stat-tile">
                    <div class="mb-1.5 text-xs uppercase tracking-wider text-gray-400">当前阶段</div>
                    <div class="text-2xl font-black bg-gradient-to-br from-white to-blue-100 bg-clip-text text-transparent">
                      {{ gameState.currentPhase === 'DAY' ? '白天' : '夜晚' }}
                    </div>
                  </div>
                  <div class="cat-stat-tile">
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
                  <div class="cat-stat-tile">
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
      <section v-if="isDm" class="mt-4" :class="entranceClass" :style="embedded ? undefined : { animationDelay: '0.35s' }">
        <button
          type="button"
          class="cat-btn cat-btn--blue w-full py-4"
          @click="advanceDay"
        >
          推进一天 (+{{ gameState.currentDay < 3 ? 33 : 34 }}进度)
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

/** Skip entrance animations in embedded tabs (less layout work on mount). */
const entranceClass = computed(() => (props.embedded ? '' : 'cat-slide-up'))

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
  if (card.isSelected) return 'cat-play-card--selected cursor-default'
  if (selectedCardId.value === card.selectedId) return 'cat-play-card--active cursor-pointer'
  if (!props.isDm && !hasConfirmedSelection.value) return 'cursor-pointer'
  return 'cursor-default opacity-90'
}

const fetchProgress = async () => {
  try {
    const response = await catastropheAPI.getProgress()
    progress.value = Number(response?.progress) || 0
    progressInput.value = progress.value
  } catch (error) {
    console.error('获取进度失败:', error)
  }
}

const fetchSelectableCards = async () => {
  try {
    const response = await catastropheAPI.getSelectableCards()
    const cards = Array.isArray(response?.cards) 
      ? response.cards.filter((c) => c != null && c.selectedId != null && c.name != null) 
      : []
    selectableCards.value = cards
    const confirmedCard = cards.find(c => Boolean(c.isSelected))
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
    gameState.value = {
      currentDay: response?.currentDay ?? 1,
      currentPhase: response?.currentPhase ?? 'DAY',
      isGameOver: Boolean(response?.isGameOver),
      catastropheTriggered: Boolean(response?.catastropheTriggered),
      extraCardDue: Boolean(response?.extraCardDue),
    }
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
      drawnCards.value = Array.isArray(response.cards) ? response.cards.filter((c) => c != null) : []
      currentDrawRound.value = response.drawRound || 0
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

onMounted(async () => {
  const tasks = [fetchProgress(), fetchSelectableCards(), fetchGameState()]
  if (props.isDm) {
    tasks.push(
      catastropheAPI.getDrawnCards().then((response) => {
        if (response?.success && Array.isArray(response.cards) && response.cards.length > 0) {
          drawnCards.value = response.cards.filter((c) => c != null)
          currentDrawRound.value = response.drawRound || 0
        }
      }).catch(() => {}),
    )
  }
  await Promise.all(tasks)
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

/* —— Page shell —— */
.cat-page--standalone {
  background: #08080f;
}

.cat-page--embedded {
  background: linear-gradient(180deg, rgba(12, 10, 22, 0.6) 0%, transparent 12rem);
}

.cat-ambient-violet {
  position: absolute;
  inset: -20% 0 auto;
  height: 55%;
  background: radial-gradient(ellipse 80% 60% at 50% 0%, rgba(124, 58, 237, 0.18), transparent 70%);
}

.cat-ambient-ember {
  position: absolute;
  inset: auto 0 -10% 0;
  height: 45%;
  background: radial-gradient(ellipse 60% 50% at 80% 100%, rgba(220, 38, 38, 0.1), transparent 70%);
}

/* —— Hero —— */
.cat-hero {
  position: relative;
  text-align: center;
  padding-top: 0.25rem;
}

.cat-hero-glow {
  position: absolute;
  left: 50%;
  top: 0.5rem;
  width: min(100%, 32rem);
  height: 7rem;
  transform: translateX(-50%);
  background: radial-gradient(ellipse at center, rgba(192, 38, 211, 0.22), rgba(239, 68, 68, 0.08) 45%, transparent 72%);
  pointer-events: none;
}

.cat-hero-eyebrow {
  position: relative;
  margin-bottom: 0.75rem;
  font-size: 0.65rem;
  font-weight: 700;
  letter-spacing: 0.35em;
  color: rgba(216, 180, 254, 0.65);
}

.cat-hero-title {
  position: relative;
  margin: 0;
  font-size: clamp(2.75rem, 9vw, 4.75rem);
  font-weight: 900;
  line-height: 1.05;
  letter-spacing: 0.12em;
  padding-left: 0.12em;
  background: linear-gradient(165deg, #fecaca 0%, #fdba74 28%, #f0abfc 58%, #c4b5fd 100%);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  filter: drop-shadow(0 4px 28px rgba(239, 68, 68, 0.35));
}

.cat-hero-subtitle {
  position: relative;
  margin: 1.25rem auto 0;
  max-width: 26rem;
  font-size: 0.9375rem;
  line-height: 1.6;
  color: rgba(203, 213, 225, 0.75);
}

.cat-hero-rule {
  position: relative;
  margin: 1.75rem auto 0;
  width: min(12rem, 40%);
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(168, 85, 247, 0.55), rgba(239, 68, 68, 0.45), transparent);
}

/* —— Panels —— */
.cat-panel {
  position: relative;
  overflow: hidden;
  border-radius: 1rem;
  padding: 1.35rem 1.25rem;
  border: 1px solid rgba(255, 255, 255, 0.07);
  background: linear-gradient(165deg, rgba(22, 26, 38, 0.98) 0%, rgba(12, 14, 22, 0.98) 100%);
  box-shadow:
    0 8px 32px rgba(0, 0, 0, 0.35),
    inset 0 1px 0 rgba(255, 255, 255, 0.05);
}

.cat-panel::before {
  content: '';
  position: absolute;
  top: 0;
  left: 10%;
  right: 10%;
  height: 1px;
  background: linear-gradient(90deg, transparent, var(--cat-accent, rgba(168, 85, 247, 0.7)), transparent);
}

.cat-panel--purple {
  --cat-accent: rgba(192, 132, 252, 0.85);
  --cat-accent-soft: rgba(168, 85, 247, 0.08);
}
.cat-panel--red {
  --cat-accent: rgba(248, 113, 113, 0.85);
  --cat-accent-soft: rgba(239, 68, 68, 0.08);
}
.cat-panel--cyan {
  --cat-accent: rgba(34, 211, 238, 0.85);
  --cat-accent-soft: rgba(34, 211, 238, 0.08);
}
.cat-panel--slate { --cat-accent: rgba(148, 163, 184, 0.5); }

.cat-panel-hover {
  position: absolute;
  inset: 0;
  opacity: 0;
  transition: opacity 0.25s ease;
  background: linear-gradient(135deg, var(--cat-accent-soft, rgba(168, 85, 247, 0.06)), transparent 55%);
  pointer-events: none;
}

.group:hover .cat-panel-hover {
  opacity: 1;
}

.cat-section-eyebrow {
  margin-bottom: 0.25rem;
  font-size: 0.65rem;
  font-weight: 700;
  letter-spacing: 0.2em;
  text-transform: uppercase;
  color: rgba(148, 163, 184, 0.75);
}

.cat-section-title {
  font-size: 1.125rem;
  font-weight: 800;
  letter-spacing: 0.02em;
  color: #f8fafc;
}

.cat-section-hint {
  font-size: 0.7rem;
  font-weight: 600;
  letter-spacing: 0.04em;
  color: rgba(34, 211, 238, 0.75);
}

/* —— Progress bar —— */
.cat-progress-track {
  border: 1px solid rgba(168, 85, 247, 0.25);
  background: linear-gradient(180deg, rgba(0, 0, 0, 0.55), rgba(15, 15, 25, 0.9));
  box-shadow: inset 0 2px 12px rgba(0, 0, 0, 0.65);
}

.cat-progress-fill {
  box-shadow: 0 0 20px rgba(168, 85, 247, 0.4);
  border-radius: inherit;
}

.cat-progress-fill::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.22), transparent 45%);
  border-radius: inherit;
  pointer-events: none;
}

.cat-progress-pct {
  line-height: 1;
  text-shadow: 0 0 24px rgba(168, 85, 247, 0.25);
}

/* —— Cards & tiles —— */
.cat-play-card {
  border-radius: 0.875rem;
  border: 1px solid rgba(71, 85, 105, 0.55);
  background: linear-gradient(160deg, rgba(15, 18, 28, 0.95), rgba(22, 26, 38, 0.85));
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.25);
}

.cat-play-card:not(.cat-play-card--selected):not(.cat-play-card--active):hover {
  border-color: rgba(34, 211, 238, 0.45);
  box-shadow: 0 0 20px rgba(34, 211, 238, 0.12);
}

.cat-play-card--active {
  border-color: rgba(192, 132, 252, 0.65);
  background: linear-gradient(160deg, rgba(88, 28, 135, 0.25), rgba(15, 18, 28, 0.95));
  box-shadow: 0 0 24px rgba(168, 85, 247, 0.2);
}

.cat-play-card--selected {
  border-color: rgba(52, 211, 153, 0.55);
  background: linear-gradient(160deg, rgba(6, 78, 59, 0.35), rgba(15, 18, 28, 0.95));
  box-shadow: 0 0 20px rgba(16, 185, 129, 0.15);
}

.cat-card-tile {
  border-radius: 0.75rem;
  border: 1px solid rgba(248, 113, 113, 0.2);
  background: rgba(15, 18, 28, 0.85);
  padding: 0.85rem;
}

.cat-card-tile--red {
  border-color: rgba(248, 113, 113, 0.2);
}

.cat-empty-state {
  display: flex;
  min-height: 7.5rem;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border-radius: 1rem;
  border: 1px dashed rgba(100, 116, 139, 0.35);
  padding: 2rem;
  background: rgba(10, 12, 20, 0.5);
}

.cat-empty-state--red {
  border-color: rgba(248, 113, 113, 0.25);
  background: rgba(69, 10, 10, 0.12);
}

.cat-empty-state--cyan {
  border-color: rgba(34, 211, 238, 0.25);
  background: rgba(8, 51, 68, 0.12);
}

.cat-stat-tile {
  border-radius: 0.75rem;
  border: 1px solid rgba(71, 85, 105, 0.35);
  background: linear-gradient(160deg, rgba(15, 18, 28, 0.9), rgba(22, 26, 38, 0.75));
  padding: 0.85rem;
  transition: border-color 0.2s ease;
}

.cat-stat-tile:hover {
  border-color: rgba(100, 116, 139, 0.5);
}

/* —— Buttons —— */
.cat-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 0.75rem;
  padding: 0.75rem 1.25rem;
  font-weight: 700;
  color: #fff;
  transition: filter 0.2s ease, box-shadow 0.2s ease;
}

.cat-btn:hover:not(:disabled) {
  filter: brightness(1.08);
}

.cat-btn--purple {
  background: linear-gradient(135deg, #7c3aed, #a855f7, #9333ea);
  box-shadow: 0 4px 20px rgba(124, 58, 237, 0.35);
}

.cat-btn--red {
  background: linear-gradient(135deg, #dc2626, #ef4444, #dc2626);
  box-shadow: 0 4px 20px rgba(220, 38, 38, 0.35);
}

.cat-btn--green {
  background: linear-gradient(135deg, #059669, #22c55e, #059669);
  box-shadow: 0 4px 20px rgba(34, 197, 94, 0.3);
}

.cat-btn--blue {
  background: linear-gradient(135deg, #2563eb, #3b82f6, #2563eb);
  box-shadow: 0 4px 20px rgba(37, 99, 235, 0.35);
}

.cat-btn--muted {
  background: #374151;
  box-shadow: none;
}

/* —— Slider —— */
.cat-slider {
  height: 0.5rem;
  appearance: none;
  cursor: pointer;
  border-radius: 9999px;
  background: linear-gradient(to right, rgba(55, 65, 81, 0.4), rgba(75, 85, 99, 0.4));
}

.cat-slider::-webkit-slider-thumb {
  appearance: none;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  border: 2px solid rgba(255, 255, 255, 0.35);
  background: linear-gradient(135deg, #a855f7, #ec4899);
  box-shadow: 0 0 10px rgba(168, 85, 247, 0.55);
}

.cat-slider::-moz-range-thumb {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  border: 2px solid rgba(255, 255, 255, 0.35);
  background: linear-gradient(135deg, #a855f7, #ec4899);
  box-shadow: 0 0 10px rgba(168, 85, 247, 0.55);
}

@keyframes cat-slideUp {
  from { opacity: 0; transform: translateY(16px); }
  to { opacity: 1; transform: translateY(0); }
}

.cat-slide-up {
  animation: cat-slideUp 0.45s ease-out forwards;
  opacity: 0;
}

@media (prefers-reduced-motion: reduce) {
  .cat-slide-up {
    animation: none;
    opacity: 1;
    transform: none;
  }
}
</style>
