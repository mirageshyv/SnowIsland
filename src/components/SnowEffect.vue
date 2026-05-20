<template>
  <div ref="containerRef" class="absolute inset-0 pointer-events-none overflow-hidden"></div>
</template>

<script setup>
import { ref, watch, onMounted, onUnmounted } from 'vue'

const props = defineProps({
  intensity: {
    type: String,
    default: 'light',
    validator: (val) => ['light', 'medium', 'heavy'].includes(val)
  }
})

const containerRef = ref(null)
let animationFrameId = null
let flakes = []

const intensityConfig = {
  light: { count: 45, minSize: 4, maxSize: 8, minSpeed: 0.3, maxSpeed: 0.8, opacity: 0.5, swayRange: 40 },
  medium: { count: 75, minSize: 5, maxSize: 10, minSpeed: 0.5, maxSpeed: 1.0, opacity: 0.6, swayRange: 60 },
  heavy: { count: 120, minSize: 6, maxSize: 12, minSpeed: 0.7, maxSpeed: 1.4, opacity: 0.7, swayRange: 80 }
}

function createFlakeElement(cfg, containerHeight, containerWidth) {
  const size = cfg.minSize + Math.random() * (cfg.maxSize - cfg.minSize)
  const opacity = cfg.opacity * (0.5 + Math.random() * 0.5)
  const speed = cfg.minSpeed + Math.random() * (cfg.maxSpeed - cfg.minSpeed)
  const swayAmplitude = (20 + Math.random() * cfg.swayRange) * (Math.random() > 0.5 ? 1 : -1)
  const swaySpeed = 0.5 + Math.random() * 1.5
  const phase = Math.random() * Math.PI * 2

  const el = document.createElement('div')
  el.style.cssText = `
    position: absolute;
    left: 0;
    top: 0;
    width: ${size}px;
    height: ${size}px;
    border-radius: 50%;
    background: radial-gradient(circle, rgba(255,255,255,${opacity}) 0%, rgba(210,225,250,${opacity * 0.5}) 100%);
    box-shadow: 0 0 ${size * 0.8}px rgba(255,255,255,${opacity * 0.4});
    will-change: transform;
    pointer-events: none;
  `

  return {
    el,
    x: Math.random() * containerWidth,
    y: -size - Math.random() * containerHeight,
    speed,
    swayAmplitude,
    swaySpeed,
    phase,
    size
  }
}

function initFlakes() {
  if (!containerRef.value) return

  const container = containerRef.value
  container.innerHTML = ''
  flakes = []

  const rect = container.getBoundingClientRect()
  const cfg = intensityConfig[props.intensity]

  for (let i = 0; i < cfg.count; i++) {
    const flake = createFlakeElement(cfg, rect.height, rect.width)
    container.appendChild(flake.el)
    flakes.push(flake)
  }

  updatePositions(0)
}

function updatePositions(timestamp) {
  const container = containerRef.value
  if (!container) return

  const rect = container.getBoundingClientRect()
  const height = rect.height
  const width = rect.width
  const t = timestamp / 1000

  for (let i = 0; i < flakes.length; i++) {
    const f = flakes[i]

    f.y += f.speed
    const sway = Math.sin(t * f.swaySpeed + f.phase) * f.swayAmplitude

    if (f.y > height + f.size) {
      f.y = -f.size
      f.x = Math.random() * width
    }

    if (f.x + sway > width + f.size) {
      f.x -= width + f.size
    } else if (f.x + sway < -f.size) {
      f.x += width + f.size
    }

    f.el.style.transform = `translate(${f.x + sway}px, ${f.y}px)`
  }

  animationFrameId = requestAnimationFrame(updatePositions)
}

watch(() => props.intensity, () => {
  if (animationFrameId) {
    cancelAnimationFrame(animationFrameId)
  }
  initFlakes()
  animationFrameId = requestAnimationFrame(updatePositions)
})

onMounted(() => {
  initFlakes()
  animationFrameId = requestAnimationFrame(updatePositions)
})

onUnmounted(() => {
  if (animationFrameId) {
    cancelAnimationFrame(animationFrameId)
    animationFrameId = null
  }
  flakes = []
})
</script>
