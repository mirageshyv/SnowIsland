<script setup>
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue';
import { useRouter } from 'vue-router';
import { milestoneAPI } from '@/utils/api.js';
import { ElMessage } from 'element-plus';

const props = defineProps({
  embedded: { type: Boolean, default: false },
  showHeader: { type: Boolean, default: true },
});

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

const isRevolutionReady = computed(() => progress.value.percentage >= 100);

const progressLevel = computed(() => {
  const p = progress.value.percentage;
  if (p >= 100) return 'complete';
  if (p >= 60) return 'high';
  if (p >= 30) return 'medium';
  return 'low';
});

const progressCardClass = computed(() => {
  const map = {
    complete: 'from-red-900/30 to-red-800/30 border-red-500/40',
    high: 'from-orange-900/20 to-orange-800/20 border-orange-500/30',
    medium: 'from-yellow-900/20 to-yellow-800/20 border-yellow-500/30',
    low: 'from-gray-800/30 to-gray-700/30 border-gray-600/20',
  };
  return map[progressLevel.value];
});

const progressGlowClass = computed(() => {
  const map = {
    complete: 'bg-red-500/5',
    high: 'bg-orange-500/5',
    medium: 'bg-yellow-500/5',
    low: 'bg-gray-500/5',
  };
  return map[progressLevel.value];
});

const progressPercentClass = computed(() => {
  const map = {
    complete: 'from-red-400 to-red-500',
    high: 'from-orange-400 to-red-500',
    medium: 'from-yellow-400 to-orange-500',
    low: 'from-gray-400 to-gray-500',
  };
  return map[progressLevel.value];
});

const progressBarFillClass = computed(() => {
  const map = {
    complete: 'bg-gradient-to-r from-red-500 via-red-400 to-red-500',
    high: 'bg-gradient-to-r from-orange-500 via-red-400 to-orange-500',
    medium: 'bg-gradient-to-r from-yellow-500 via-orange-400 to-yellow-500',
    low: 'bg-gradient-to-r from-gray-500 via-gray-400 to-gray-500',
  };
  return map[progressLevel.value];
});

const progressBarGlowClass = computed(() => {
  const map = {
    complete: 'bg-red-500/20',
    high: 'bg-orange-500/20',
    medium: 'bg-yellow-500/20',
    low: 'bg-gray-500/20',
  };
  return map[progressLevel.value];
});

const progressEdgeGlowClass = computed(() => {
  const map = {
    complete: 'shadow-[0_0_20px_rgba(248,113,113,0.8)]',
    high: 'shadow-[0_0_20px_rgba(251,146,60,0.8)]',
    medium: 'shadow-[0_0_20px_rgba(250,204,21,0.8)]',
    low: 'shadow-[0_0_20px_rgba(156,163,175,0.6)]',
  };
  return map[progressLevel.value];
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
    hasAccess.value = true;
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
  <!-- Loading -->
  <div
    v-if="loading"
    :class="embedded ? 'py-20' : 'min-h-screen'"
    class="flex items-center justify-center p-4"
    :style="embedded ? undefined : { background: '#0a0a0f' }"
  >
    <div class="text-center">
      <div class="relative w-16 h-16 mx-auto mb-6">
        <div class="absolute inset-0 border-4 border-red-500/20 rounded-full" />
        <div class="absolute inset-0 border-4 border-transparent border-t-red-500 rounded-full animate-spin" />
      </div>
      <p class="text-gray-400 text-lg">加载中...</p>
    </div>
  </div>

  <!-- No access -->
  <div
    v-else-if="!hasAccess"
    :class="embedded ? 'py-16' : 'min-h-screen'"
    class="relative overflow-hidden flex items-center justify-center p-4"
    :style="embedded ? undefined : { background: '#0a0a0f' }"
  >
    <div class="absolute inset-0 bg-gradient-to-br from-red-500/5 via-transparent to-red-500/5" />
    <div class="relative max-w-md w-full bg-gradient-to-br from-gray-900/90 to-gray-800/90 backdrop-blur-xl border border-red-500/20 rounded-3xl p-10 text-center shadow-2xl">
      <div class="w-20 h-20 mx-auto mb-6 rounded-full bg-gradient-to-br from-red-500/20 to-red-600/20 flex items-center justify-center border border-red-500/30 shadow-lg shadow-red-500/20">
        <svg class="w-10 h-10 text-red-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
        </svg>
      </div>
      <h1 class="text-3xl text-white mb-3 font-bold">访问被拒绝</h1>
      <p class="text-gray-400 mb-8 leading-relaxed">{{ error || '您的角色不属于反抗者阵营，无法查看此内容。' }}</p>
      <button
        type="button"
        class="bg-gradient-to-r from-gray-700 to-gray-600 hover:from-gray-600 hover:to-gray-500 text-white px-8 py-3 rounded-xl transition-all duration-150 shadow-lg"
        @click="router.push('/')"
      >
        返回首页
      </button>
    </div>
  </div>

  <!-- Error -->
  <div
    v-else-if="error"
    :class="embedded ? 'py-16' : 'min-h-screen'"
    class="relative overflow-hidden flex items-center justify-center p-4"
    :style="embedded ? undefined : { background: '#0a0a0f' }"
  >
    <div class="absolute inset-0 bg-gradient-to-br from-red-500/5 via-transparent to-orange-500/5" />
    <div class="relative max-w-md w-full bg-gradient-to-br from-gray-900/90 to-gray-800/90 backdrop-blur-xl border border-red-500/20 rounded-3xl p-10 text-center shadow-2xl">
      <div class="w-20 h-20 mx-auto mb-6 rounded-full bg-gradient-to-br from-red-500/20 to-red-600/20 flex items-center justify-center border border-red-500/30 shadow-lg shadow-red-500/20">
        <svg class="w-10 h-10 text-red-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
        </svg>
      </div>
      <h1 class="text-2xl text-white mb-3 font-bold">获取里程碑数据失败</h1>
      <p class="text-gray-400 leading-relaxed">{{ error }}</p>
    </div>
  </div>

  <!-- Main content -->
  <div
    v-else
    :class="embedded ? 'relative' : 'min-h-screen relative overflow-hidden'"
    :style="embedded ? undefined : { background: '#0a0a0f' }"
  >
    <div v-if="!embedded" class="fixed inset-0 pointer-events-none">
      <div class="absolute top-0 left-1/4 w-96 h-96 bg-red-500/10 rounded-full blur-3xl opacity-50 animate-pulse" />
      <div class="absolute bottom-0 right-1/4 w-96 h-96 bg-orange-500/10 rounded-full blur-3xl opacity-50 animate-pulse milestone-bg-delay-1" />
      <div class="absolute top-1/2 left-1/2 w-64 h-64 bg-yellow-500/10 rounded-full blur-3xl opacity-30 animate-pulse milestone-bg-delay-2" />
    </div>

    <div
      class="relative mx-auto px-4"
      :class="embedded ? 'max-w-5xl py-2' : 'max-w-5xl py-16'"
    >
      <header v-if="showHeader" :class="embedded ? 'mb-10' : 'mb-16'" class="text-center milestone-fade-in">
        <div class="inline-block mb-4">
          <div class="w-16 h-1 bg-gradient-to-r from-transparent via-red-500 to-transparent rounded-full" />
        </div>
        <h1 class="text-3xl md:text-5xl text-white mb-3 font-bold tracking-tight bg-gradient-to-r from-white via-gray-100 to-white bg-clip-text text-transparent">
          反叛者里程碑
        </h1>
        <p class="text-gray-400 text-base tracking-wide">追踪反抗者阵营的革命进展</p>
        <div class="inline-block mt-4">
          <div class="w-16 h-1 bg-gradient-to-r from-transparent via-red-500 to-transparent rounded-full" />
        </div>
      </header>

      <!-- Revolution progress -->
      <section :class="embedded ? 'mb-10' : 'mb-16'" class="milestone-slide-up" aria-labelledby="progress-title" style="animation-delay: 0.1s">
        <h2 id="progress-title" class="text-xl text-white mb-6 font-bold tracking-tight flex items-center gap-3">
          <span class="w-1 h-8 bg-gradient-to-b from-red-500 to-orange-500 rounded-full" />
          革命进度
        </h2>

        <div
          class="relative group bg-gradient-to-br backdrop-blur-xl border rounded-3xl p-5 md:p-8 shadow-2xl overflow-hidden transition-shadow duration-150"
          :class="[progressCardClass, { 'revolution-ready-card': isRevolutionReady }]"
        >
          <div
            class="absolute inset-0 opacity-0 group-hover:opacity-100 transition-opacity duration-150"
            :class="progressGlowClass"
          />

          <div class="relative flex flex-col sm:flex-row sm:items-end sm:justify-between gap-4 mb-6">
            <div>
              <div
                class="text-4xl md:text-7xl font-black mb-2 bg-gradient-to-br bg-clip-text text-transparent drop-shadow-2xl"
                :class="progressPercentClass"
                :aria-label="`革命进度 ${progress.percentage.toFixed(1)} 百分比`"
              >
                {{ progress.percentage.toFixed(1) }}%
              </div>
              <div class="text-gray-300 text-base font-medium">
                {{ progress.completed }} / {{ progress.total }} 里程碑已完成
              </div>
            </div>

            <div v-if="isRevolutionReady" class="sm:text-right animate-pulse">
              <div class="inline-flex items-center gap-2 bg-gradient-to-r from-red-500/30 to-red-600/30 text-red-300 text-sm px-5 py-2.5 rounded-2xl border border-red-400/40 shadow-lg shadow-red-500/30">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M5 13l4 4L19 7" />
                </svg>
                <span class="font-bold">革命就绪</span>
              </div>
            </div>
          </div>

          <div class="flame-wrapper relative w-full">
            <div
              class="relative w-full h-6 rounded-full bg-black/40 border border-white/10 shadow-inner overflow-visible"
              :class="{ 'flame-border': isRevolutionReady }"
              role="progressbar"
              :aria-valuenow="progress.percentage"
              aria-valuemin="0"
              aria-valuemax="100"
              aria-label="革命进度条"
            >
              <div
                class="absolute inset-0 blur-md transition-all duration-1000 ease-out rounded-full"
                :class="progressBarGlowClass"
                :style="{ width: `${Math.min(100, progress.percentage)}%` }"
              />
              <div
                ref="progressBarRef"
                class="relative h-full rounded-full transition-all duration-1000 ease-out overflow-hidden"
                :class="progressBarFillClass"
                :style="{ width: `${Math.min(100, progress.percentage)}%` }"
              >
                <div class="absolute inset-0 bg-gradient-to-r from-transparent via-white/30 to-transparent milestone-shine" />
                <div v-if="isRevolutionReady" class="absolute inset-0 rounded-full flame-inner-glow" />
                <div class="absolute inset-0 bg-white/10 animate-pulse" />
                <div
                  class="absolute right-0 top-0 bottom-0 w-2 bg-white blur-sm"
                  :class="progressEdgeGlowClass"
                />
              </div>
            </div>

            <canvas
              v-if="isRevolutionReady"
              ref="flameCanvas"
              class="flame-canvas"
            />
          </div>

          <div
            v-if="isRevolutionReady"
            class="relative mt-6 text-red-300 text-sm leading-relaxed text-center border-t border-red-500/20 pt-6 revolution-text"
          >
            起义已经准备好了，只需要一个时机，或许也可以准备的更加充分
          </div>
        </div>
      </section>

      <!-- Milestone list -->
      <section aria-labelledby="milestones-title">
        <h2 id="milestones-title" class="text-xl text-white mb-6 font-bold tracking-tight flex items-center gap-3">
          <span class="w-1 h-8 bg-gradient-to-b from-red-500 to-orange-500 rounded-full" />
          里程碑列表
        </h2>

        <div class="space-y-6">
          <div
            v-for="(milestone, index) in milestones"
            :key="milestone.id"
            class="relative group milestone-slide-up"
            :style="{ animationDelay: `${0.2 + index * 0.05}s` }"
          >
            <div
              class="relative bg-gradient-to-br backdrop-blur-xl border rounded-2xl p-5 md:p-6 transition-all duration-150 hover:shadow-2xl overflow-hidden"
              :class="milestone.isCompleted
                ? 'from-[#1a0f0f]/90 to-[#2d1818]/90 border-red-500/25 hover:border-red-500/45 hover:shadow-red-500/10'
                : 'from-gray-900/40 to-gray-800/40 border-gray-700/30 hover:border-gray-600/40 hover:shadow-gray-500/10'"
            >
              <div
                class="absolute inset-0 opacity-0 group-hover:opacity-100 transition-opacity duration-150"
                :class="milestone.isCompleted ? 'bg-red-500/5' : 'bg-gray-500/5'"
              />

              <div class="relative flex flex-col sm:flex-row gap-6">
                <div
                  class="flex-shrink-0 w-16 h-16 rounded-2xl flex items-center justify-center font-black text-xl border-2 transition-all duration-150 group-hover:scale-105"
                  :class="milestone.isCompleted
                    ? 'bg-gradient-to-br from-red-500/30 to-red-600/30 border-red-400/50 text-red-300 shadow-lg shadow-red-500/30'
                    : 'bg-gradient-to-br from-gray-700/30 to-gray-600/30 border-gray-500/30 text-gray-400'"
                  :aria-label="`里程碑 ${index + 1}`"
                >
                  <svg
                    v-if="milestone.isCompleted"
                    class="w-8 h-8"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                    stroke-width="3"
                  >
                    <path stroke-linecap="round" stroke-linejoin="round" d="M5 13l4 4L19 7" />
                  </svg>
                  <span v-else>{{ index + 1 }}</span>
                </div>

                <div class="flex-1 min-w-0">
                  <div class="flex flex-col sm:flex-row sm:items-start sm:justify-between gap-3 mb-4">
                    <h3
                      class="text-lg md:text-xl font-bold"
                      :class="milestone.isCompleted ? 'text-red-400' : 'text-gray-300'"
                    >
                      {{ milestone.name }}
                    </h3>
                    <div class="flex flex-wrap items-center gap-2 shrink-0">
                      <span
                        class="text-xs px-4 py-2 rounded-full font-bold"
                        :class="milestone.isCompleted
                          ? 'bg-red-500/20 text-red-300 border border-red-400/40'
                          : 'bg-gray-700/30 text-gray-400 border border-gray-600/30'"
                        :aria-label="milestone.isCompleted ? '状态：已完成' : '状态：未完成'"
                      >
                        {{ milestone.isCompleted ? '已完成' : '未完成' }}
                      </span>
                      <button
                        v-if="isDm"
                        type="button"
                        class="text-xs px-4 py-2 rounded-full font-bold transition-all duration-150"
                        :class="milestone.isCompleted
                          ? 'bg-gray-600/50 text-gray-300 hover:bg-gray-600/70 border border-gray-500/40'
                          : 'bg-red-500/20 text-red-400 hover:bg-red-500/40 border border-red-400/40'"
                        @click="toggleMilestone(milestone.id)"
                      >
                        {{ milestone.isCompleted ? '取消点亮' : '点亮里程碑' }}
                      </button>
                    </div>
                    </div>

                  <p
                    class="leading-relaxed text-xs md:text-sm"
                    :class="milestone.isCompleted ? 'text-gray-400' : 'text-gray-500'"
                  >
                    {{ milestone.description }}
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <footer class="mt-12 text-center text-gray-500 text-sm border-t border-gray-800/50 pt-6 milestone-fade-in">
        <p class="tracking-wide">
          {{ isDm ? '作为DM，您可以点击按钮点亮或取消点亮里程碑。' : '您可以查看反抗者阵营的里程碑进展。' }}
        </p>
      </footer>
    </div>
  </div>
</template>

<style scoped>
.milestone-bg-delay-1 {
  animation-delay: 1s;
}

.milestone-bg-delay-2 {
  animation-delay: 2s;
}

.flame-wrapper {
  position: relative;
  padding-top: 40px;
  margin-top: -8px;
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
  text-shadow: 0 0 8px rgba(248, 113, 113, 0.5), 0 0 16px rgba(255, 80, 0, 0.35);
  animation: text-flicker 2s ease-in-out infinite;
}

@keyframes milestone-fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes milestone-slideUp {
  from { opacity: 0; transform: translateY(30px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes milestone-shine {
  0% { transform: translateX(-100%) skewX(-15deg); }
  100% { transform: translateX(200%) skewX(-15deg); }
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

.milestone-fade-in {
  animation: milestone-fadeIn 1s ease-out;
}

.milestone-slide-up {
  animation: milestone-slideUp 0.8s ease-out forwards;
  opacity: 0;
}

.milestone-shine {
  animation: milestone-shine 3s ease-in-out infinite;
}
</style>
