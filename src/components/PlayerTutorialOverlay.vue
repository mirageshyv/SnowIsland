<template>
  <Teleport to="body">
    <Transition name="tutorial-fade">
      <div
        v-if="prompting || (playing && currentStep)"
        class="fixed inset-0 z-[80] overflow-hidden"
        role="dialog"
        aria-modal="true"
        aria-labelledby="player-tutorial-title"
      >
        <div
          class="absolute inset-0 transition-colors duration-500"
          :class="prompting || isCardStep ? 'bg-black/70' : 'bg-transparent'"
        />

        <template v-if="prompting">
          <div class="absolute inset-0 z-[81] flex items-center justify-center p-4">
            <div class="relative w-full max-w-md overflow-hidden rounded-2xl bg-slate-900/90 border border-cyan-500/30 shadow-xl shadow-cyan-900/30 px-8 py-10 text-center">
              <SnowEffect intensity="light" />
              <div class="relative z-10">
                <h3 id="player-tutorial-title" class="text-white text-2xl font-bold tracking-tight mb-3">要观看界面引导吗？</h3>
                <p class="text-slate-300 text-sm leading-relaxed mb-8">大约一分钟，带你认识玩家中心的常用功能。之后也可以在「个人信息」里重新观看。</p>
                <div class="flex flex-col sm:flex-row gap-3 justify-center">
                  <button
                    type="button"
                    class="px-5 py-2.5 rounded-lg text-sm text-slate-300 bg-slate-800/80 border border-slate-600 hover:bg-slate-700 hover:text-white transition-colors"
                    @click="onDecline"
                  >
                    暂不需要
                  </button>
                  <button
                    type="button"
                    class="px-5 py-2.5 rounded-lg text-sm text-white bg-cyan-600 hover:bg-cyan-500 border border-cyan-400/50 shadow-lg shadow-cyan-500/20 transition-colors"
                    @click="onAccept"
                  >
                    开始引导
                  </button>
                </div>
              </div>
            </div>
          </div>
        </template>

        <template v-else>
          <div class="absolute top-0 left-0 right-0 h-0.5 bg-slate-800/80 z-[82]">
            <div
              class="h-full w-full bg-gradient-to-r from-cyan-400 to-sky-500 origin-left transition-transform duration-300"
              :style="{ transform: `scaleX(${stepCount ? (stepIndex + 1) / stepCount : 0})` }"
            />
          </div>

          <button
            type="button"
            class="absolute top-4 right-4 z-[83] px-4 py-2 rounded-lg text-sm text-white bg-slate-800/90 border border-cyan-500/40 hover:bg-slate-700 hover:border-cyan-400/60 shadow-lg shadow-cyan-500/10 transition-colors"
            @click="onSkip"
          >
            跳过
          </button>

          <div
            v-if="!isCardStep && spotlight"
            class="tutorial-spot absolute pointer-events-none rounded-2xl border-2 border-cyan-400/80"
            :style="spotStyle"
          />

          <div
            v-if="!isCardStep && spotlight"
            class="absolute z-[81] max-w-sm"
            :style="captionStyle"
          >
            <div class="rounded-2xl bg-slate-900/90 border border-cyan-500/30 p-4 shadow-xl shadow-cyan-900/20">
              <div class="text-cyan-300/80 text-xs mb-1 tabular-nums">{{ stepIndex + 1 }} / {{ stepCount }}</div>
              <h3 id="player-tutorial-title" class="text-white font-semibold mb-1">{{ currentStep.title }}</h3>
              <p class="text-slate-300 text-sm leading-relaxed mb-4">{{ currentStep.body }}</p>
              <div class="flex items-center justify-between gap-3">
                <button
                  type="button"
                  class="px-4 py-2 rounded-lg text-sm border transition-colors disabled:opacity-35 disabled:cursor-not-allowed"
                  :class="canPrev
                    ? 'text-white bg-slate-800/80 border-slate-600 hover:bg-slate-700'
                    : 'text-slate-500 bg-slate-800/40 border-slate-700'"
                  :disabled="!canPrev"
                  @click="onPrev"
                >
                  上一步
                </button>
                <button
                  type="button"
                  class="px-4 py-2 rounded-lg text-sm text-white bg-cyan-600 hover:bg-cyan-500 border border-cyan-400/50 transition-colors"
                  @click="onNext"
                >
                  {{ isLast ? '完成' : '下一步' }}
                </button>
              </div>
            </div>
          </div>

          <Transition name="tutorial-card" mode="out-in">
            <div
              v-if="isCardStep"
              :key="currentStep.id"
              class="absolute inset-0 z-[81] flex items-center justify-center p-4 pointer-events-none"
            >
              <div class="relative w-full max-w-md overflow-hidden rounded-2xl bg-slate-900/90 border border-cyan-500/30 shadow-xl shadow-cyan-900/30 px-8 py-10 text-center pointer-events-auto">
                <SnowEffect intensity="light" />
                <div class="relative z-10">
                  <div class="text-cyan-300/80 text-xs mb-3 tabular-nums">{{ stepIndex + 1 }} / {{ stepCount }}</div>
                  <h3 id="player-tutorial-title" class="text-white text-2xl font-bold tracking-tight mb-3">{{ currentStep.title }}</h3>
                  <p class="text-slate-300 text-sm leading-relaxed mb-6">{{ currentStep.body }}</p>
                  <div class="flex items-center justify-between gap-3">
                    <button
                      type="button"
                      class="px-4 py-2 rounded-lg text-sm border transition-colors disabled:opacity-35 disabled:cursor-not-allowed"
                      :class="canPrev
                        ? 'text-white bg-slate-800/80 border-slate-600 hover:bg-slate-700'
                        : 'text-slate-500 bg-slate-800/40 border-slate-700'"
                      :disabled="!canPrev"
                      @click="onPrev"
                    >
                      上一步
                    </button>
                    <button
                      type="button"
                      class="px-4 py-2 rounded-lg text-sm text-white bg-cyan-600 hover:bg-cyan-500 border border-cyan-400/50 transition-colors"
                      @click="onNext"
                    >
                      {{ isLast ? '完成' : '下一步' }}
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </Transition>
        </template>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import SnowEffect from './SnowEffect.vue'

const props = defineProps({
  prompting: { type: Boolean, default: false },
  playing: { type: Boolean, default: false },
  currentStep: { type: Object, default: null },
  stepIndex: { type: Number, default: 0 },
  stepCount: { type: Number, default: 0 },
  isCardStep: { type: Boolean, default: false },
  canPrev: { type: Boolean, default: false },
  isLast: { type: Boolean, default: false },
  measureGen: { type: Number, default: 0 },
})

const emit = defineEmits(['skip', 'prev', 'next', 'accept', 'decline'])

const spotlight = ref(null)
const captionBox = ref({ top: 0, left: 0 })

const CAPTION_W = 320
const CAPTION_H = 220
const GAP = 16
const PAD = 10

const spotStyle = computed(() => {
  const s = spotlight.value
  if (!s) return {}
  return {
    top: `${s.top}px`,
    left: `${s.left}px`,
    width: `${s.width}px`,
    height: `${s.height}px`,
  }
})

const captionStyle = computed(() => ({
  top: `${captionBox.value.top}px`,
  left: `${captionBox.value.left}px`,
  width: `${CAPTION_W}px`,
}))

function clamp(n, min, max) {
  return Math.max(min, Math.min(max, n))
}

function placeCaption(s) {
  const vw = window.innerWidth
  const vh = window.innerHeight
  let top
  let left

  if (s.height > vh * 0.45) {
    left = s.left + s.width + GAP
    if (left + CAPTION_W > vw - 16) {
      left = clamp(s.left - CAPTION_W - GAP, 16, vw - CAPTION_W - 16)
    }
    top = clamp(s.top + 24, 64, vh - CAPTION_H - 16)
  } else if (s.top + s.height + GAP + CAPTION_H < vh - 16) {
    top = s.top + s.height + GAP
    left = clamp(s.left, 16, vw - CAPTION_W - 16)
  } else if (s.top - GAP - CAPTION_H > 64) {
    top = s.top - GAP - CAPTION_H
    left = clamp(s.left, 16, vw - CAPTION_W - 16)
  } else {
    top = vh - CAPTION_H - 24
    left = clamp((vw - CAPTION_W) / 2, 16, vw - CAPTION_W - 16)
  }

  captionBox.value = { top, left }
}

function queryTarget(target) {
  if (!target || target === 'welcome' || target === 'close') return null
  const el = document.querySelector(`[data-tutorial="${target}"]`)
  if (el) return el
  return document.querySelector('[data-tutorial="hamburger"]')
}

function measure() {
  if (!props.playing || !props.currentStep || props.isCardStep || props.prompting) {
    spotlight.value = null
    return
  }
  const el = queryTarget(props.currentStep.target)
  if (!el) {
    spotlight.value = null
    return
  }
  el.scrollIntoView({ block: 'nearest', inline: 'nearest', behavior: 'auto' })
  const r = el.getBoundingClientRect()
  const next = {
    top: Math.max(8, r.top - PAD),
    left: Math.max(8, r.left - PAD),
    width: Math.min(window.innerWidth - 16, r.width + PAD * 2),
    height: Math.min(window.innerHeight - 16, r.height + PAD * 2),
  }
  spotlight.value = next
  placeCaption(next)
}

let measureTimer = 0
let remMeasureTimer = 0

function scheduleMeasure() {
  clearTimeout(measureTimer)
  clearTimeout(remMeasureTimer)
  measureTimer = window.setTimeout(() => {
    requestAnimationFrame(measure)
    remMeasureTimer = window.setTimeout(measure, 400)
  }, 40)
}

watch(
  () => [props.playing, props.prompting, props.currentStep, props.measureGen, props.isCardStep],
  () => {
    if (!props.playing || props.prompting || props.isCardStep) {
      spotlight.value = null
      return
    }
    scheduleMeasure()
  },
  { immediate: true }
)

function onResize() {
  if (props.playing && !props.isCardStep && !props.prompting) measure()
}

onMounted(() => {
  window.addEventListener('resize', onResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', onResize)
  clearTimeout(measureTimer)
  clearTimeout(remMeasureTimer)
})

function onSkip() {
  emit('skip')
}

function onPrev() {
  emit('prev')
}

function onNext() {
  emit('next')
}

function onAccept() {
  emit('accept')
}

function onDecline() {
  emit('decline')
}
</script>

<style scoped>
.tutorial-spot {
  box-shadow: 0 0 0 9999px rgba(0, 0, 0, 0.72), 0 0 18px 2px rgba(34, 211, 238, 0.35);
  transition:
    top 0.5s cubic-bezier(0.22, 1, 0.36, 1),
    left 0.5s cubic-bezier(0.22, 1, 0.36, 1),
    width 0.5s cubic-bezier(0.22, 1, 0.36, 1),
    height 0.5s cubic-bezier(0.22, 1, 0.36, 1);
  animation: tutorial-pulse 2.2s ease-in-out infinite;
}

@keyframes tutorial-pulse {
  0%,
  100% {
    box-shadow: 0 0 0 9999px rgba(0, 0, 0, 0.72), 0 0 12px 0 rgba(34, 211, 238, 0.28);
  }
  50% {
    box-shadow: 0 0 0 9999px rgba(0, 0, 0, 0.72), 0 0 28px 6px rgba(34, 211, 238, 0.4);
  }
}

.tutorial-fade-enter-active,
.tutorial-fade-leave-active {
  transition: opacity 280ms ease;
}
.tutorial-fade-enter-from,
.tutorial-fade-leave-to {
  opacity: 0;
}

.tutorial-card-enter-active,
.tutorial-card-leave-active {
  transition: opacity 280ms ease, transform 280ms ease;
}
.tutorial-card-enter-from,
.tutorial-card-leave-to {
  opacity: 0;
  transform: scale(0.96) translateY(8px);
}
</style>
