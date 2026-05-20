<template>
  <div class="fixed bottom-4 right-4 z-50 group">
    <!-- 雪花粒子 -->
    <div v-if="isPlaying" class="snow-container">
      <div v-for="i in 8" :key="i" class="snowflake" :style="getSnowStyle(i)"></div>
    </div>

    <!-- 展开面板 -->
    <Transition name="slide-up">
      <div
        v-show="expanded"
        class="absolute bottom-full right-0 mb-2 bg-slate-800/95 backdrop-blur-md rounded-xl p-3 shadow-xl border border-slate-600/40 w-48"
      >
        <div class="flex items-center gap-2 mb-2">
          <svg xmlns="http://www.w3.org/2000/svg" class="w-3 h-3 text-cyan-400" fill="currentColor" viewBox="0 0 24 24">
            <path d="M3 9v6h4l5 5V4L7 9H3zm13.5 3c0-1.77-1.02-3.29-2.5-4.03v8.05c1.48-.73 2.5-2.25 2.5-4.02z"/>
          </svg>
          <input
            type="range"
            min="0"
            max="100"
            :value="volumeVal"
            @input="onVolumeChange"
            class="flex-1 h-1 bg-slate-600 rounded-full appearance-none cursor-pointer slider"
          />
          <span class="text-[10px] text-slate-400 w-7 text-right">{{ volumeVal }}%</span>
        </div>
        <p class="text-[10px] text-slate-500 truncate">Winter's Last Breath</p>
      </div>
    </Transition>

    <!-- 主按钮 -->
    <button
      @click="onMainClick"
      class="relative w-10 h-10 rounded-full flex items-center justify-center transition-all duration-300 shadow-lg border"
      :class="isPlaying
        ? 'bg-gradient-to-br from-blue-600 to-cyan-600 border-blue-400/40 shadow-blue-500/30 hover:shadow-blue-500/50'
        : 'bg-slate-800 border-slate-600/40 shadow-slate-900/50 hover:bg-slate-700'"
    >
      <!-- 雪花装饰 -->
      <svg v-if="isPlaying" class="absolute -top-1 -right-1 w-3 h-3 text-white/80 animate-pulse" viewBox="0 0 24 24" fill="currentColor">
        <path d="M12 0l2.1 4.5L18 3l-1.5 3.9L21 6l-3 3 3 3-4.5-1.1L18 15l-3.9-1.5L12 18l-2.1-4.5L6 15l1.5-3.9L3 12l3-3-3-3 4.5 1.1L6 9l3.9 1.5L12 0z"/>
      </svg>

      <svg v-if="isPlaying" xmlns="http://www.w3.org/2000/svg" class="w-4 h-4 text-white" fill="currentColor" viewBox="0 0 24 24">
        <path d="M6 4h4v16H6V4zm8 0h4v16h-4V4z"/>
      </svg>
      <svg v-else xmlns="http://www.w3.org/2000/svg" class="w-4 h-4 text-slate-300 ml-0.5" fill="currentColor" viewBox="0 0 24 24">
        <path d="M8 5v14l11-7z"/>
      </svg>
    </button>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useMusicStore } from '../../composables/useMusicStore'

const { isPlaying, volume: volumeVal, toggle, setVolume, tryAutoPlay } = useMusicStore()
const expanded = ref(false)
let expandTimer = null

function onMainClick() {
  toggle()
  // 切换播放时短暂展开面板
  if (!expanded.value) {
    expanded.value = true
    clearTimeout(expandTimer)
    expandTimer = setTimeout(() => { expanded.value = false }, 3000)
  }
}

function onVolumeChange(e) {
  setVolume(parseInt(e.target.value))
}

function getSnowStyle(index) {
  const left = Math.random() * 100
  const delay = Math.random() * 3
  const duration = 2.5 + Math.random() * 3
  const size = 3 + Math.random() * 4
  return {
    left: `${left}%`,
    animationDelay: `${delay}s`,
    animationDuration: `${duration}s`,
    width: `${size}px`,
    height: `${size}px`
  }
}

onMounted(() => {
  tryAutoPlay()
  // 鼠标悬停展开
})
</script>

<style scoped>
.slider::-webkit-slider-thumb {
  appearance: none;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: linear-gradient(135deg, #3b82f6, #06b6d4);
  cursor: pointer;
  box-shadow: 0 1px 4px rgba(59, 130, 246, 0.4);
}
.slider::-moz-range-thumb {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: linear-gradient(135deg, #3b82f6, #06b6d4);
  cursor: pointer;
  border: none;
  box-shadow: 0 1px 4px rgba(59, 130, 246, 0.4);
}

.snow-container {
  position: absolute;
  bottom: 100%;
  left: 50%;
  transform: translateX(-50%);
  width: 80px;
  height: 60px;
  pointer-events: none;
  overflow: visible;
}

.snowflake {
  position: absolute;
  bottom: 0;
  background: radial-gradient(circle, rgba(255,255,255,0.9) 0%, rgba(255,255,255,0.2) 100%);
  border-radius: 50%;
  animation: snowfall linear infinite;
  opacity: 0.6;
}

@keyframes snowfall {
  0% { transform: translateY(0) rotate(0deg); opacity: 0.6; }
  50% { opacity: 0.9; }
  100% { transform: translateY(60px) rotate(360deg); opacity: 0; }
}

.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.2s ease;
}
.slide-up-enter-from,
.slide-up-leave-to {
  opacity: 0;
  transform: translateY(8px);
}
</style>
