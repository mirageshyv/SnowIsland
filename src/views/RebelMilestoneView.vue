<script setup>
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue';
import { useRouter } from 'vue-router';
import { milestoneAPI } from '@/utils/api.js';
import { ElMessage } from 'element-plus';

const router = useRouter();
const milestones = ref([]);
const progress = ref({ completed: 0, total: 0, percentage: 0 });
const isDm = ref(false);
const loading = ref(true);
const error = ref('');
const hasAccess = ref(true);
const userRole = localStorage.getItem('userRole');
const playerId = localStorage.getItem('playerId') || '1';
const username = localStorage.getItem('username');

const isRevolutionReady = computed(() => {
  return progress.value.percentage >= 100;
});

const progressColor = computed(() => {
  const p = progress.value.percentage;
  if (p < 30) return 'from-gray-600 to-gray-400';
  if (p < 60) return 'from-yellow-600 to-yellow-400';
  if (p < 100) return 'from-orange-500 to-orange-400';
  return 'from-red-600 to-red-500';
});

const progressTextColor = computed(() => {
  const p = progress.value.percentage;
  if (p < 30) return 'text-gray-400';
  if (p < 60) return 'text-yellow-400';
  if (p < 100) return 'text-orange-400';
  return 'text-red-400';
});

const flameCanvas = ref(null);
const progressBarRef = ref(null);
let animationId = null;
let particles = [];
let smokeParticles = [];
let emberParticles = [];

class FlameParticle {
  constructor(canvasWidth, canvasHeight, barWidth) {
    this.x = Math.random() * barWidth;
    this.y = canvasHeight;
    this.baseX = this.x;
    this.vx = (Math.random() - 0.5) * 1.2;
    this.vy = -(Math.random() * 2.5 + 1.5);
    this.size = Math.random() * 18 + 8;
    this.maxLife = Math.random() * 40 + 30;
    this.life = this.maxLife;
    this.drift = Math.random() * 0.8 - 0.4;
    this.wobbleSpeed = Math.random() * 0.15 + 0.05;
    this.wobbleAmp = Math.random() * 6 + 3;
    this.phase = Math.random() * Math.PI * 2;
  }

  update() {
    this.life--;
    this.phase += this.wobbleSpeed;
    this.x = this.baseX + Math.sin(this.phase) * this.wobbleAmp + this.drift * (this.maxLife - this.life);
    this.y += this.vy;
    this.vy *= 0.98;
    this.baseX += this.drift * 0.3;
    this.size *= 0.985;
  }

  draw(ctx) {
    const lifeRatio = this.life / this.maxLife;
    const alpha = lifeRatio * 0.85;

    let r, g, b;
    if (lifeRatio > 0.7) {
      r = 255; g = 240 + (1 - lifeRatio) * 15; b = 180 + (1 - lifeRatio) * 40;
    } else if (lifeRatio > 0.4) {
      const t = (lifeRatio - 0.4) / 0.3;
      r = 255; g = Math.floor(100 + t * 140); b = Math.floor(t * 60);
    } else {
      const t = lifeRatio / 0.4;
      r = Math.floor(180 + t * 75); g = Math.floor(t * 100); b = 0;
    }

    const gradient = ctx.createRadialGradient(this.x, this.y, 0, this.x, this.y, this.size);
    gradient.addColorStop(0, `rgba(${r}, ${g}, ${b}, ${alpha})`);
    gradient.addColorStop(0.4, `rgba(${r}, ${Math.floor(g * 0.7)}, ${Math.floor(b * 0.3)}, ${alpha * 0.6})`);
    gradient.addColorStop(1, `rgba(${Math.floor(r * 0.5)}, 0, 0, 0)`);

    ctx.beginPath();
    ctx.arc(this.x, this.y, this.size, 0, Math.PI * 2);
    ctx.fillStyle = gradient;
    ctx.fill();
  }
}

class SmokeParticle {
  constructor(canvasWidth, canvasHeight, barWidth) {
    this.x = Math.random() * barWidth;
    this.y = canvasHeight - Math.random() * 10;
    this.vx = (Math.random() - 0.5) * 0.6;
    this.vy = -(Math.random() * 1.0 + 0.5);
    this.size = Math.random() * 12 + 6;
    this.maxLife = Math.random() * 60 + 40;
    this.life = this.maxLife;
    this.rotation = Math.random() * Math.PI * 2;
    this.rotSpeed = (Math.random() - 0.5) * 0.03;
  }

  update() {
    this.life--;
    this.x += this.vx;
    this.y += this.vy;
    this.vy *= 0.995;
    this.size += 0.3;
    this.rotation += this.rotSpeed;
  }

  draw(ctx) {
    const lifeRatio = this.life / this.maxLife;
    const alpha = lifeRatio * 0.15;

    ctx.save();
    ctx.translate(this.x, this.y);
    ctx.rotate(this.rotation);

    const gradient = ctx.createRadialGradient(0, 0, 0, 0, 0, this.size);
    gradient.addColorStop(0, `rgba(80, 60, 50, ${alpha})`);
    gradient.addColorStop(0.5, `rgba(50, 40, 35, ${alpha * 0.5})`);
    gradient.addColorStop(1, `rgba(30, 25, 20, 0)`);

    ctx.beginPath();
    ctx.arc(0, 0, this.size, 0, Math.PI * 2);
    ctx.fillStyle = gradient;
    ctx.fill();
    ctx.restore();
  }
}

class EmberParticle {
  constructor(canvasWidth, canvasHeight, barWidth) {
    this.x = Math.random() * barWidth;
    this.y = canvasHeight;
    this.vx = (Math.random() - 0.5) * 2;
    this.vy = -(Math.random() * 3 + 2);
    this.size = Math.random() * 2.5 + 0.5;
    this.maxLife = Math.random() * 50 + 20;
    this.life = this.maxLife;
    this.brightness = Math.random() * 0.5 + 0.5;
  }

  update() {
    this.life--;
    this.x += this.vx;
    this.y += this.vy;
    this.vy += 0.02;
    this.vx *= 0.99;
  }

  draw(ctx) {
    const lifeRatio = this.life / this.maxLife;
    const alpha = lifeRatio * this.brightness;
    const flicker = 0.7 + Math.random() * 0.3;

    ctx.beginPath();
    ctx.arc(this.x, this.y, this.size, 0, Math.PI * 2);
    ctx.fillStyle = `rgba(255, ${Math.floor(180 * flicker)}, ${Math.floor(50 * flicker)}, ${alpha})`;
    ctx.fill();

    ctx.beginPath();
    ctx.arc(this.x, this.y, this.size * 3, 0, Math.PI * 2);
    ctx.fillStyle = `rgba(255, 100, 0, ${alpha * 0.15})`;
    ctx.fill();
  }
}

function getBarWidth() {
  if (!progressBarRef.value) return 0;
  return progressBarRef.value.offsetWidth;
}

function animateFlames() {
  const canvas = flameCanvas.value;
  if (!canvas) return;

  const ctx = canvas.getContext('2d');
  const barWidth = getBarWidth();
  const container = canvas.parentElement;
  if (!container) return;

  const rect = container.getBoundingClientRect();
  canvas.width = rect.width;
  canvas.height = rect.height;

  ctx.clearRect(0, 0, canvas.width, canvas.height);

  if (Math.random() < 0.6) {
    particles.push(new FlameParticle(canvas.width, canvas.height, barWidth));
  }
  if (Math.random() < 0.3) {
    particles.push(new FlameParticle(canvas.width, canvas.height, barWidth));
  }
  if (Math.random() < 0.15) {
    smokeParticles.push(new SmokeParticle(canvas.width, canvas.height, barWidth));
  }
  if (Math.random() < 0.25) {
    emberParticles.push(new EmberParticle(canvas.width, canvas.height, barWidth));
  }

  smokeParticles = smokeParticles.filter(p => p.life > 0);
  particles = particles.filter(p => p.life > 0);
  emberParticles = emberParticles.filter(p => p.life > 0);

  ctx.globalCompositeOperation = 'source-over';
  smokeParticles.forEach(p => { p.update(); p.draw(ctx); });

  ctx.globalCompositeOperation = 'lighter';
  particles.forEach(p => { p.update(); p.draw(ctx); });
  emberParticles.forEach(p => { p.update(); p.draw(ctx); });

  const baseGlow = ctx.createLinearGradient(0, canvas.height, 0, canvas.height - 30);
  baseGlow.addColorStop(0, 'rgba(255, 80, 0, 0.25)');
  baseGlow.addColorStop(0.5, 'rgba(255, 40, 0, 0.1)');
  baseGlow.addColorStop(1, 'rgba(255, 20, 0, 0)');
  ctx.fillStyle = baseGlow;
  ctx.fillRect(0, canvas.height - 30, barWidth, 30);

  ctx.globalCompositeOperation = 'source-over';

  animationId = requestAnimationFrame(animateFlames);
}

function startFlameAnimation() {
  if (animationId) return;
  nextTick(() => {
    animateFlames();
  });
}

function stopFlameAnimation() {
  if (animationId) {
    cancelAnimationFrame(animationId);
    animationId = null;
  }
  particles = [];
  smokeParticles = [];
  emberParticles = [];
  if (flameCanvas.value) {
    const ctx = flameCanvas.value.getContext('2d');
    if (ctx) ctx.clearRect(0, 0, flameCanvas.value.width, flameCanvas.value.height);
  }
}

watch(isRevolutionReady, (newVal) => {
  if (newVal) {
    startFlameAnimation();
  } else {
    stopFlameAnimation();
  }
});

async function fetchMilestones() {
  try {
    loading.value = true;
    error.value = '';
    const result = await milestoneAPI.getAll(playerId, userRole);
    if (result?.success) {
      milestones.value = result.data;
      progress.value = result.progress;
      isDm.value = result.isDm;
      hasAccess.value = true;
    } else {
      hasAccess.value = false;
      error.value = result?.message || '无访问权限';
    }
  } catch (e) {
    error.value = '获取里程碑数据失败，请检查后端服务';
    console.error('Error fetching milestones:', e);
  } finally {
    loading.value = false;
  }
}

async function toggleMilestone(milestoneId) {
  if (!isDm.value) return;
  try {
    const result = await milestoneAPI.toggle(milestoneId, playerId, userRole);
    if (result?.success) {
      const index = milestones.value.findIndex(m => m.id === milestoneId);
      if (index !== -1) {
        milestones.value[index] = result.data;
      }
      progress.value = result.progress;
      ElMessage.success(result.message);
    } else {
      ElMessage.error(result?.message || '操作失败');
    }
  } catch (e) {
    ElMessage.error('操作失败');
    console.error('Error toggling milestone:', e);
  }
}

onMounted(() => {
  if (!userRole || !username) {
    router.push('/');
    return;
  }
  fetchMilestones().then(() => {
    if (isRevolutionReady.value) {
      startFlameAnimation();
    }
  });
});

onUnmounted(() => {
  stopFlameAnimation();
});
</script>

<template>
  <div v-if="loading" class="min-h-screen bg-[#0a0e1a] flex items-center justify-center">
    <div class="text-center">
      <div class="w-16 h-16 border-4 border-red-500 border-t-transparent rounded-full animate-spin mx-auto mb-4"></div>
      <p class="text-gray-400">加载中...</p>
    </div>
  </div>

  <div v-else-if="!hasAccess" class="min-h-screen bg-[#0a0e1a] flex items-center justify-center px-4">
    <div class="text-center max-w-md">
      <div class="w-24 h-24 bg-red-500/10 rounded-full flex items-center justify-center mx-auto mb-6">
        <svg class="w-12 h-12 text-red-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
        </svg>
      </div>
      <h2 class="text-2xl text-white font-bold mb-4">访问被拒绝</h2>
      <p class="text-gray-400 mb-6">{{ error }}</p>
      <button
        @click="router.push('/')"
        class="px-6 py-3 bg-red-600/20 text-red-400 rounded-xl hover:bg-red-600/30 transition-colors"
      >
        返回首页
      </button>
    </div>
  </div>

  <div v-else class="min-h-screen bg-[#0a0e1a]">
    <div class="max-w-4xl mx-auto px-4 py-8">
      <div class="text-center mb-10">
        <h1 class="text-3xl md:text-4xl font-bold text-white mb-3 tracking-wide">反抗者里程碑</h1>
        <p class="text-gray-400">追踪反抗者阵营的革命进展</p>
      </div>

      <div class="relative bg-gradient-to-br from-[#1a1a2e] to-[#16213e] border border-red-500/20 rounded-3xl p-6 mb-10 overflow-hidden shadow-2xl"
        :class="{ 'revolution-ready-card': isRevolutionReady }">
        <div class="absolute top-0 right-0 w-64 h-64 bg-red-500/10 rounded-full blur-3xl"></div>

        <div class="relative">
          <div class="flex items-center justify-between mb-4">
            <span class="text-gray-300 text-lg">革命进度</span>
            <span :class="['text-3xl font-semibold tabular-nums', progressTextColor]">
              {{ progress.percentage.toFixed(1) }}%
            </span>
          </div>

          <div class="flame-wrapper relative w-full rounded-full overflow-visible">
            <div class="relative w-full h-8 rounded-full bg-black/30 border border-white/10 overflow-visible"
              :class="{ 'flame-border': isRevolutionReady }">
              <div
                ref="progressBarRef"
                class="h-full rounded-full bg-gradient-to-r transition-all duration-1000 ease-out relative"
                :class="progressColor"
                :style="{ width: `${Math.min(100, progress.percentage)}%` }"
              >
                <div v-if="isRevolutionReady" class="absolute inset-0 rounded-full flame-inner-glow"></div>
              </div>
              <div class="absolute inset-0 flex items-center justify-center z-10">
                <span class="text-white font-bold text-sm drop-shadow-lg">
                  {{ progress.completed }} / {{ progress.total }}
                </span>
              </div>
            </div>

            <canvas
              v-if="isRevolutionReady"
              ref="flameCanvas"
              class="flame-canvas"
            ></canvas>
          </div>

          <div v-if="isRevolutionReady" class="mt-3 text-right">
            <span class="text-red-500 text-sm font-semibold revolution-text">起义已经准备好了，只需要一个时机，或许也可以准备的更加充分</span>
          </div>
        </div>
      </div>

      <div class="space-y-4">
        <h2 class="text-xl text-white font-semibold mb-2">里程碑列表</h2>

        <div
          v-for="(milestone, index) in milestones"
          :key="milestone.id"
          :class="[
            'relative border-l-4 pl-6 pb-6 transition-all duration-300',
            milestone.isCompleted ? 'border-red-500' : 'border-white/20'
          ]"
        >
          <div :class="[
            'bg-gradient-to-br rounded-2xl p-5 transition-all duration-300',
            milestone.isCompleted
              ? 'from-[#1a0f0f] to-[#2d1818] border border-red-500/30'
              : 'from-[#1a2332] to-[#0f1419] border border-white/10'
          ]">
            <div class="flex items-start justify-between gap-4">
              <div class="flex-1">
                <div class="flex items-center gap-3 mb-3">
                  <span :class="[
                    'flex-shrink-0 w-8 h-8 rounded-full flex items-center justify-center text-xs font-bold',
                    milestone.isCompleted
                      ? 'bg-red-500 text-white'
                      : 'bg-gray-600 text-gray-300'
                  ]">
                    {{ index + 1 }}
                  </span>
                  <h3 :class="[
                    'text-xl font-bold tracking-tight',
                    milestone.isCompleted ? 'text-red-400' : 'text-white'
                  ]">
                    {{ milestone.name }}
                  </h3>
                </div>

                <p :class="[
                  'text-sm leading-relaxed',
                  milestone.isCompleted ? 'text-gray-300' : 'text-gray-400'
                ]">
                  {{ milestone.description }}
                </p>

                <div v-if="milestone.completedAt" class="mt-3 flex items-center gap-2 text-xs text-gray-500">
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"></path>
                  </svg>
                  <span>完成时间: {{ milestone.completedAt }}</span>
                </div>
              </div>

              <div class="flex-shrink-0 flex items-center gap-3">
                <div :class="[
                  'w-12 h-12 rounded-full flex items-center justify-center transition-all duration-500',
                  milestone.isCompleted
                    ? 'bg-red-500/20 animate-pulse'
                    : 'bg-gray-700/50'
                ]">
                  <svg v-if="milestone.isCompleted" class="w-7 h-7 text-red-500" fill="currentColor" viewBox="0 0 24 24">
                    <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/>
                  </svg>
                  <svg v-else class="w-6 h-6 text-gray-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"/>
                  </svg>
                </div>

                <button
                  v-if="isDm"
                  @click="toggleMilestone(milestone.id)"
                  :class="[
                    'px-4 py-2 rounded-lg text-sm font-medium transition-all duration-200',
                    milestone.isCompleted
                      ? 'bg-gray-600/50 text-gray-300 hover:bg-gray-600/70'
                      : 'bg-red-500/20 text-red-400 hover:bg-red-500/30'
                  ]"
                >
                  {{ milestone.isCompleted ? '取消点亮' : '点亮里程碑' }}
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="mt-12 text-center">
        <p class="text-gray-500 text-sm">
          {{ isDm ? '作为DM，您可以点击按钮点亮或取消点亮里程碑。' : '您可以查看反抗者阵营的里程碑进展。' }}
        </p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.flame-wrapper {
  position: relative;
  padding-top: 40px;
  margin-top: -40px;
  padding-bottom: 4px;
}

.flame-canvas {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 5;
}

.flame-border {
  border-color: rgba(255, 80, 0, 0.5) !important;
  box-shadow:
    0 0 8px rgba(255, 60, 0, 0.4),
    0 0 20px rgba(255, 40, 0, 0.2),
    0 0 40px rgba(255, 20, 0, 0.1),
    inset 0 0 8px rgba(255, 80, 0, 0.15);
  animation: flame-border-pulse 1.5s ease-in-out infinite alternate;
}

.flame-inner-glow {
  background: linear-gradient(
    to top,
    rgba(255, 200, 50, 0.3) 0%,
    rgba(255, 120, 0, 0.15) 40%,
    rgba(255, 60, 0, 0.05) 70%,
    transparent 100%
  );
  animation: inner-glow-pulse 0.8s ease-in-out infinite alternate;
}

.revolution-ready-card {
  border-color: rgba(255, 60, 0, 0.4) !important;
  box-shadow:
    0 0 30px rgba(255, 40, 0, 0.15),
    0 0 60px rgba(255, 20, 0, 0.08);
  animation: card-fire-glow 2s ease-in-out infinite alternate;
}

.revolution-text {
  text-shadow: 0 0 8px rgba(255, 60, 0, 0.5), 0 0 16px rgba(255, 30, 0, 0.3);
  animation: text-flicker 2s ease-in-out infinite;
}

@keyframes flame-border-pulse {
  0% {
    box-shadow:
      0 0 8px rgba(255, 60, 0, 0.4),
      0 0 20px rgba(255, 40, 0, 0.2),
      0 0 40px rgba(255, 20, 0, 0.1),
      inset 0 0 8px rgba(255, 80, 0, 0.15);
  }
  100% {
    box-shadow:
      0 0 12px rgba(255, 80, 0, 0.6),
      0 0 30px rgba(255, 50, 0, 0.3),
      0 0 60px rgba(255, 30, 0, 0.15),
      inset 0 0 12px rgba(255, 100, 0, 0.2);
  }
}

@keyframes inner-glow-pulse {
  0% { opacity: 0.7; }
  100% { opacity: 1; }
}

@keyframes card-fire-glow {
  0% {
    box-shadow:
      0 0 30px rgba(255, 40, 0, 0.15),
      0 0 60px rgba(255, 20, 0, 0.08);
  }
  100% {
    box-shadow:
      0 0 40px rgba(255, 60, 0, 0.25),
      0 0 80px rgba(255, 30, 0, 0.12);
  }
}

@keyframes text-flicker {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.85; }
  75% { opacity: 0.95; }
}
</style>
