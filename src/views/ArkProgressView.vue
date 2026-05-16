<script setup>
import { ref, computed, onMounted } from 'vue'
import { arkAPI } from '@/utils/api.js'
import { ElMessage } from 'element-plus'

const props = defineProps({
  embedded: { type: Boolean, default: false },
})

const arkData = ref(null)
const loading = ref(true)
const investForm = ref({ wood: 0, metal: 0, sealant: 0 })
const installing = ref(false)

const currentUserRole = computed(() => (localStorage.getItem('userRole') || 'player').toLowerCase().trim())
const isDm = computed(() => currentUserRole.value === 'dm')

const arkCurrentProgress = computed(() => arkData.value?.completionPercentage || 0)

const showThresholdLine = computed(() => arkCurrentProgress.value < 50)

const arkBuildStages = [
  { progress: 30, title: '底部龙骨', description: '底部龙骨与基础结构已完成，船体轮廓初步形成。' },
  { progress: 50, title: '基本框架', description: '船舷与主舱完成，具备勉强出航能力。' },
  { progress: 70, title: '船壳完成', description: '加装护舷和附属空间，稳定性显著提升。' },
  { progress: 90, title: '甲板铺设', description: '甲板与舱室进一步完善，物资承载能力增强。' },
  { progress: 100, title: '方舟完工', description: '方舟全面完工，可承载人员和长期补给。' },
]

const resourceCards = computed(() => {
  if (!arkData.value) return []
  const d = arkData.value
  return [
    { name: '木材', current: d.currentWood || 0, max: d.targetWood || 250, unit: '吨' },
    { name: '金属制品', current: d.currentMetal || 0, max: d.targetMetal || 100, unit: '吨' },
    { name: '密封材料', current: d.currentSealant || 0, max: d.targetSealant || 100, unit: 'kg' },
  ]
})

function resourcePercent(current, max) {
  if (!max) return 0
  return Math.min(100, (current / max) * 100)
}

function resourceBarClass(percent) {
  if (percent >= 100) return 'bg-gradient-to-r from-green-500 to-emerald-500'
  if (percent >= 50) return 'bg-gradient-to-r from-cyan-500 to-blue-500'
  return 'bg-gradient-to-r from-orange-500 to-yellow-500'
}

function getStageStatus(stageProgress) {
  if (arkCurrentProgress.value >= stageProgress) {
    return arkCurrentProgress.value === stageProgress ? 'current' : 'completed'
  }
  return 'pending'
}

function isStageReached(stageProgress) {
  return arkCurrentProgress.value >= stageProgress
}

async function fetchArkStatus() {
  try {
    loading.value = true
    const data = await arkAPI.getStatus()
    if (data?.success) {
      arkData.value = data
    }
  } catch (error) {
    console.error('获取方舟状态失败:', error)
  } finally {
    loading.value = false
  }
}

async function investResources() {
  if (!isDm.value) {
    ElMessage.warning('您没有权限进行此操作')
    return
  }
  try {
    if (investForm.value.wood < 0 || investForm.value.metal < 0 || investForm.value.sealant < 0) {
      ElMessage.warning('资源不能为负数')
      return
    }
    if (investForm.value.wood === 0 && investForm.value.metal === 0 && investForm.value.sealant === 0) {
      ElMessage.warning('请至少投入一项资源')
      return
    }
    const result = await arkAPI.invest(
      investForm.value.wood || 0,
      investForm.value.metal || 0,
      investForm.value.sealant || 0
    )
    if (result?.success) {
      ElMessage.success(result.message || '资源投入成功')
      arkData.value = result.data
      investForm.value = { wood: 0, metal: 0, sealant: 0 }
    } else {
      ElMessage.error(result?.message || '资源投入失败')
    }
  } catch {
    ElMessage.error('资源投入失败')
  }
}

async function installComponent(type, count) {
  if (!isDm.value) {
    ElMessage.warning('您没有权限进行此操作')
    return
  }
  try {
    installing.value = true
    const result = await arkAPI.installComponent(type, count)
    if (result?.success) {
      ElMessage.success('安装成功')
      arkData.value = result.data
    } else {
      ElMessage.error(result?.message || '安装失败')
    }
  } catch {
    ElMessage.error('安装失败')
  } finally {
    installing.value = false
  }
}

async function buildSail() {
  if (!isDm.value) {
    ElMessage.warning('您没有权限进行此操作')
    return
  }
  try {
    const result = await arkAPI.buildSail()
    if (result?.success) {
      ElMessage.success('帆建造成功')
      arkData.value = result.data
    } else {
      ElMessage.error(result?.message || '建造失败')
    }
  } catch {
    ElMessage.error('建造失败')
  }
}

async function resetArk() {
  if (!isDm.value) {
    ElMessage.warning('您没有权限进行此操作')
    return
  }
  try {
    const result = await arkAPI.reset()
    if (result?.success) {
      ElMessage.success('重置成功')
      arkData.value = result.data
    }
  } catch {
    ElMessage.error('重置失败')
  }
}

onMounted(() => {
  fetchArkStatus()
})
</script>

<template>
  <div
    :class="embedded ? 'relative' : 'min-h-screen relative overflow-hidden'"
    :style="embedded ? undefined : { background: '#0a0a0f' }"
  >
    <div v-if="!embedded" class="fixed inset-0 pointer-events-none">
      <div class="absolute top-0 right-1/4 w-96 h-96 bg-cyan-500/10 rounded-full blur-3xl opacity-50 animate-pulse" />
      <div class="absolute bottom-0 left-1/4 w-96 h-96 bg-blue-500/10 rounded-full blur-3xl opacity-50 animate-pulse ark-bg-delay-1" />
    </div>

    <div
      class="relative mx-auto px-4 md:px-6"
      :class="embedded ? 'max-w-5xl pt-10 pb-2' : 'max-w-4xl pt-16 pb-12 md:pt-20 md:pb-16'"
    >
      <header :class="embedded ? 'mb-10 pt-4' : 'mb-16 pt-6'" class="text-center ark-fade-in">
        <div class="relative inline-block">
          <div class="absolute inset-0 blur-2xl bg-gradient-to-r from-cyan-500/30 via-blue-500/30 to-cyan-500/30 animate-pulse" />
          <h1 class="relative text-4xl md:text-6xl lg:text-7xl text-white mb-3 md:mb-4 font-black tracking-tight bg-gradient-to-r from-cyan-300 via-blue-300 to-cyan-300 bg-clip-text text-transparent drop-shadow-[0_0_30px_rgba(6,182,212,0.6)]">
            方舟建造进度
          </h1>
        </div>
        <p class="text-gray-300 text-base md:text-lg tracking-wide mt-2">追踪方舟从无到有的每一步</p>
      </header>

      <!-- Loading -->
      <div v-if="loading" class="flex items-center justify-center py-20">
        <div class="text-center">
          <div class="relative w-16 h-16 mx-auto mb-6">
            <div class="absolute inset-0 border-4 border-cyan-500/20 rounded-full" />
            <div class="absolute inset-0 border-4 border-transparent border-t-cyan-500 rounded-full animate-spin" />
          </div>
          <p class="text-gray-400 text-lg">加载中...</p>
        </div>
      </div>

      <!-- Error -->
      <div v-else-if="!arkData" class="flex flex-col items-center justify-center py-16">
        <div class="max-w-md w-full bg-gradient-to-br from-gray-900/90 to-gray-800/90 backdrop-blur-xl border border-cyan-500/20 rounded-3xl p-10 text-center shadow-2xl">
          <p class="text-red-400 mb-4">获取数据失败，请检查后端服务</p>
          <button
            type="button"
            class="px-6 py-3 bg-gradient-to-r from-cyan-600/80 to-blue-600/80 text-white rounded-xl hover:from-cyan-500 hover:to-blue-500 transition-all duration-150"
            @click="fetchArkStatus"
          >
            重试
          </button>
        </div>
      </div>

      <template v-else>
        <!-- Overall progress -->
        <section class="mb-10 md:mb-12 ark-slide-up" style="animation-delay: 0.1s">
          <div class="group bg-gradient-to-br from-gray-900/70 to-gray-800/70 backdrop-blur-xl border border-cyan-500/30 rounded-3xl p-6 md:p-10 shadow-2xl hover:border-cyan-500/50 hover:shadow-cyan-500/20 transition-all duration-150 relative overflow-hidden">
            <div class="absolute inset-0 bg-gradient-to-br from-cyan-500/5 via-transparent to-blue-500/5 opacity-50" />

            <div class="relative flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-6 md:mb-8">
              <h2 class="text-white text-xl md:text-2xl font-bold tracking-wide">整体进度</h2>
              <div class="text-4xl md:text-6xl font-black bg-gradient-to-br from-cyan-300 via-blue-400 to-cyan-500 bg-clip-text text-transparent drop-shadow-[0_0_20px_rgba(6,182,212,0.5)] tabular-nums">
                {{ Number(arkCurrentProgress).toFixed(1) }}%
              </div>
            </div>

            <div class="relative w-full h-6 md:h-8 rounded-full overflow-visible">
              <div class="relative w-full h-full bg-gradient-to-r from-black/60 via-black/50 to-black/60 rounded-full border-2 border-cyan-500/20 shadow-[inset_0_2px_20px_rgba(0,0,0,0.8)] overflow-hidden">
                <div
                  class="absolute inset-0 bg-gradient-to-r from-cyan-500/30 via-blue-500/30 to-cyan-500/30 blur-xl transition-all duration-1000"
                  :style="{ width: `${Math.min(100, Number(arkCurrentProgress))}%` }"
                />
                <div
                  class="relative h-full bg-gradient-to-r from-cyan-400 via-blue-500 to-cyan-400 transition-all duration-1000 shadow-[0_0_30px_rgba(6,182,212,0.6)] overflow-hidden"
                  :style="{ width: `${Math.min(100, Number(arkCurrentProgress))}%` }"
                >
                  <div class="absolute inset-0 bg-gradient-to-r from-transparent via-white/40 to-transparent ark-shine" />
                  <div class="absolute inset-0 bg-gradient-to-r from-white/0 via-white/20 to-white/0 animate-pulse" />
                  <div class="absolute right-0 top-0 bottom-0 w-1 bg-white shadow-[0_0_25px_rgba(255,255,255,0.9),0_0_15px_rgba(6,182,212,1)]" />
                </div>
              </div>
              <div
                v-if="showThresholdLine"
                class="absolute top-0 h-full w-0.5 bg-red-500 z-10 pointer-events-none"
                style="left: 50%"
              >
                <div class="absolute top-full mt-2 left-1/2 -translate-x-1/2 whitespace-nowrap text-red-400 text-xs font-medium">
                  方舟基础出航下限 50%
                </div>
              </div>
            </div>
          </div>
        </section>

        <!-- Resources -->
        <section class="mb-10 md:mb-12">
          <h2 class="text-white text-xl md:text-2xl font-bold mb-6 flex items-center gap-3">
            <span class="w-1 h-8 bg-gradient-to-b from-cyan-500 to-blue-500 rounded-full" />
            资源进度
          </h2>

          <div class="grid grid-cols-1 sm:grid-cols-3 gap-4 mb-6">
            <div
              v-for="(resource, index) in resourceCards"
              :key="resource.name"
              class="bg-gradient-to-br from-gray-900/50 to-gray-800/50 backdrop-blur-xl border border-gray-700/30 rounded-2xl p-5 hover:border-gray-600/50 transition-all duration-150 ark-slide-up"
              :style="{ animationDelay: `${0.2 + index * 0.1}s` }"
            >
              <div class="flex items-center justify-between mb-3">
                <span class="text-gray-400 text-sm">{{ resource.name }}</span>
                <span
                  class="text-xs font-bold"
                  :class="resourcePercent(resource.current, resource.max) >= 100 ? 'text-green-400' : 'text-cyan-400'"
                >
                  {{ resourcePercent(resource.current, resource.max).toFixed(0) }}%
                </span>
              </div>
              <div class="text-white text-lg font-bold mb-3">
                {{ resource.current }} / {{ resource.max }} {{ resource.unit }}
              </div>
              <div class="relative w-full h-2 bg-black/30 rounded-full overflow-hidden">
                <div
                  class="h-full rounded-full transition-all duration-700"
                  :class="resourceBarClass(resourcePercent(resource.current, resource.max))"
                  :style="{ width: `${resourcePercent(resource.current, resource.max)}%` }"
                />
              </div>
            </div>
          </div>

          <!-- Components -->
          <div class="grid grid-cols-1 sm:grid-cols-3 gap-4 mb-6">
            <div class="bg-gradient-to-br from-gray-900/40 to-gray-800/40 backdrop-blur-xl border border-gray-700/30 rounded-2xl p-5 text-center hover:border-gray-600/50 transition-all duration-150">
              <div
                class="text-3xl md:text-4xl font-black mb-2"
                :class="(arkData.engineCount || 0) >= 3 ? 'text-green-400' : (arkData.engineCount || 0) === 0 ? 'text-red-400' : 'text-cyan-400'"
              >
                {{ arkData.engineCount || 0 }} / 3
              </div>
              <div class="text-gray-400 text-sm mb-3">发动机</div>
              <div class="flex gap-1 justify-center flex-wrap">
                <button
                  v-if="isDm"
                  v-for="i in 3"
                  :key="'engine-btn-' + i"
                  type="button"
                  :disabled="installing"
                  class="px-2 py-1 text-xs rounded transition-all duration-150"
                  :class="arkData.engineCount >= i ? 'bg-cyan-500/20 text-cyan-400 border border-cyan-500/30' : 'bg-gray-500/10 text-gray-500 hover:bg-gray-500/20 border border-gray-600/30'"
                  @click="installComponent('engine', i)"
                >
                  {{ i }}个
                </button>
                <span
                  v-else
                  v-for="i in 3"
                  :key="'engine-span-' + i"
                  class="px-2 py-1 text-xs rounded"
                  :class="arkData.engineCount >= i ? 'bg-cyan-500/20 text-cyan-400' : 'bg-gray-500/10 text-gray-500'"
                >
                  {{ i }}
                </span>
              </div>
              <div v-if="(arkData.engineCount || 0) === 0" class="mt-2 text-xs text-red-400">缺失</div>
              <div v-else-if="(arkData.engineCount || 0) >= 3" class="mt-2 text-xs text-green-400">完成</div>
            </div>

            <div class="bg-gradient-to-br from-gray-900/40 to-gray-800/40 backdrop-blur-xl border border-gray-700/30 rounded-2xl p-5 text-center hover:border-gray-600/50 transition-all duration-150">
              <div
                class="text-3xl md:text-4xl font-black mb-2"
                :class="(arkData.propellerCount || 0) >= 2 ? 'text-green-400' : (arkData.propellerCount || 0) === 0 ? 'text-red-400' : 'text-cyan-400'"
              >
                {{ arkData.propellerCount || 0 }} / 2
              </div>
              <div class="text-gray-400 text-sm mb-3">螺旋桨</div>
              <div class="flex gap-1 justify-center flex-wrap">
                <button
                  v-if="isDm"
                  v-for="i in 2"
                  :key="'propeller-btn-' + i"
                  type="button"
                  :disabled="installing"
                  class="px-2 py-1 text-xs rounded transition-all duration-150"
                  :class="arkData.propellerCount >= i ? 'bg-emerald-500/20 text-emerald-400 border border-emerald-500/30' : 'bg-gray-500/10 text-gray-500 hover:bg-gray-500/20 border border-gray-600/30'"
                  @click="installComponent('propeller', i)"
                >
                  {{ i }}个
                </button>
                <span
                  v-else
                  v-for="i in 2"
                  :key="'propeller-span-' + i"
                  class="px-2 py-1 text-xs rounded"
                  :class="arkData.propellerCount >= i ? 'bg-emerald-500/20 text-emerald-400' : 'bg-gray-500/10 text-gray-500'"
                >
                  {{ i }}
                </span>
              </div>
              <div v-if="(arkData.propellerCount || 0) === 0" class="mt-2 text-xs text-red-400">缺失</div>
              <div v-else-if="(arkData.propellerCount || 0) >= 2" class="mt-2 text-xs text-green-400">完成</div>
            </div>

            <div class="bg-gradient-to-br from-gray-900/40 to-gray-800/40 backdrop-blur-xl border border-gray-700/30 rounded-2xl p-5 text-center hover:border-gray-600/50 transition-all duration-150">
              <div
                class="text-3xl md:text-4xl font-black mb-2"
                :class="(arkData.generatorCount || 0) >= 1 ? 'text-green-400' : 'text-red-400'"
              >
                {{ arkData.generatorCount || 0 }} / 1
              </div>
              <div class="text-gray-400 text-sm mb-3">发电机</div>
              <div>
                <button
                  v-if="isDm"
                  type="button"
                  :disabled="installing"
                  class="px-3 py-1 text-xs rounded transition-all duration-150 border"
                  :class="arkData.generatorCount >= 1 ? 'bg-emerald-500/20 text-emerald-400 border-emerald-500/30' : 'bg-orange-500/20 text-orange-400 hover:bg-orange-500/30 border-orange-500/30'"
                  @click="installComponent('generator', arkData.generatorCount >= 1 ? 0 : 1)"
                >
                  {{ arkData.generatorCount >= 1 ? '已安装' : '安装' }}
                </button>
                <span
                  v-else
                  class="px-3 py-1 text-xs rounded border inline-block"
                  :class="arkData.generatorCount >= 1 ? 'bg-emerald-500/20 text-emerald-400 border-emerald-500/30' : 'bg-orange-500/20 text-orange-400 border-orange-500/30'"
                >
                  {{ arkData.generatorCount >= 1 ? '已安装' : '未安装' }}
                </span>
              </div>
              <div v-if="(arkData.generatorCount || 0) === 0" class="mt-2 text-xs text-red-400">缺失</div>
              <div v-else class="mt-2 text-xs text-green-400">完成</div>
            </div>
          </div>

          <!-- Cargo & sail -->
          <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div class="bg-gradient-to-br from-gray-900/40 to-gray-800/40 backdrop-blur-xl border border-gray-700/30 rounded-2xl p-5 text-center hover:border-gray-600/50 transition-all duration-150">
              <div class="text-cyan-400 text-3xl md:text-4xl font-black mb-2">{{ arkData.currentCargoCapacity || 0 }} 点</div>
              <div class="text-gray-400 text-sm">载重能力</div>
            </div>
            <div
              class="bg-gradient-to-br from-gray-900/40 to-gray-800/40 backdrop-blur-xl border rounded-2xl p-5 text-center hover:border-gray-600/50 transition-all duration-150"
              :class="arkData.hasSail ? 'border-cyan-500/30' : 'border-gray-700/30'"
            >
              <div v-if="isDm && !arkData.hasSail" class="flex flex-col items-center gap-2">
                <div class="text-gray-400 text-lg font-bold">未建造</div>
                <button
                  type="button"
                  class="px-3 py-1 text-xs bg-cyan-500/20 text-cyan-400 rounded-lg hover:bg-cyan-500/30 border border-cyan-500/30 transition-all duration-150"
                  @click="buildSail"
                >
                  建造
                </button>
              </div>
              <template v-else>
                <div :class="['text-3xl md:text-4xl font-black mb-2', arkData.hasSail ? 'text-green-400' : 'text-gray-400']">
                  {{ arkData.hasSail ? '已建造' : '未建造' }}
                </div>
                <div class="text-gray-400 text-sm">帆</div>
              </template>
            </div>
          </div>

          <!-- DM invest -->
          <div v-if="isDm" class="mt-6 bg-gradient-to-br from-gray-900/50 to-gray-800/50 backdrop-blur-xl border border-cyan-500/20 rounded-2xl p-5 md:p-6">
            <div class="text-gray-300 text-sm mb-4 font-medium">投入资源</div>
            <div class="grid grid-cols-1 sm:grid-cols-3 gap-3">
              <div>
                <label class="text-xs text-gray-500 block mb-1">木材（吨）</label>
                <input
                  v-model.number="investForm.wood"
                  type="number"
                  min="0"
                  class="w-full bg-black/30 border border-white/10 rounded-lg px-3 py-2 text-white text-sm focus:outline-none focus:border-cyan-500 transition-colors duration-150"
                />
              </div>
              <div>
                <label class="text-xs text-gray-500 block mb-1">金属（吨）</label>
                <input
                  v-model.number="investForm.metal"
                  type="number"
                  min="0"
                  class="w-full bg-black/30 border border-white/10 rounded-lg px-3 py-2 text-white text-sm focus:outline-none focus:border-cyan-500 transition-colors duration-150"
                />
              </div>
              <div>
                <label class="text-xs text-gray-500 block mb-1">密封材料（kg）</label>
                <input
                  v-model.number="investForm.sealant"
                  type="number"
                  min="0"
                  class="w-full bg-black/30 border border-white/10 rounded-lg px-3 py-2 text-white text-sm focus:outline-none focus:border-cyan-500 transition-colors duration-150"
                />
              </div>
            </div>
            <div class="mt-4 flex flex-col sm:flex-row gap-3">
              <button
                type="button"
                class="flex-1 bg-gradient-to-r from-cyan-500 to-blue-600 text-white py-2.5 rounded-lg font-semibold hover:from-cyan-400 hover:to-blue-500 transition-all duration-150 shadow-lg shadow-cyan-500/20"
                @click="investResources"
              >
                确认投入
              </button>
              <button
                type="button"
                class="px-6 py-2.5 bg-red-500/20 text-red-400 rounded-lg text-sm hover:bg-red-500/30 border border-red-500/30 transition-all duration-150"
                @click="resetArk"
              >
                重置
              </button>
            </div>
          </div>
        </section>

        <!-- Build stages -->
        <section>
          <h2 class="text-white text-xl md:text-2xl font-bold mb-6 md:mb-8 flex items-center gap-3">
            <span class="w-1 h-8 bg-gradient-to-b from-cyan-500 to-blue-500 rounded-full" />
            建设阶段
          </h2>

          <div class="space-y-4">
            <div
              v-for="(stage, index) in arkBuildStages"
              :key="stage.title"
              class="relative group backdrop-blur-xl border rounded-2xl p-5 md:p-6 transition-all duration-150 ark-slide-up"
              :class="isStageReached(stage.progress)
                ? 'bg-gradient-to-br from-gray-900/50 to-gray-800/50 border-cyan-500/20 hover:border-cyan-500/40 hover:shadow-2xl hover:shadow-cyan-500/10'
                : 'bg-gradient-to-br from-gray-900/30 to-gray-800/30 border-gray-700/20 opacity-40'"
              :style="{ animationDelay: `${0.3 + index * 0.1}s` }"
            >
              <div
                v-if="isStageReached(stage.progress)"
                class="absolute inset-0 bg-cyan-500/5 opacity-0 group-hover:opacity-100 transition-opacity duration-150 rounded-2xl"
              />

              <div class="relative flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
                <div class="flex-1">
                  <h3
                    class="text-lg md:text-xl font-bold mb-2"
                    :class="isStageReached(stage.progress) ? 'text-white' : 'text-gray-500'"
                  >
                    {{ stage.title }}
                  </h3>
                  <p
                    class="leading-relaxed text-sm md:text-base"
                    :class="isStageReached(stage.progress) ? 'text-gray-400' : 'text-gray-600'"
                  >
                    {{ stage.description }}
                  </p>
                  <span
                    v-if="getStageStatus(stage.progress) === 'current'"
                    class="inline-block mt-2 text-xs px-3 py-1 rounded-full bg-cyan-500/20 text-cyan-400 border border-cyan-400/40 font-bold"
                  >
                    当前阶段
                  </span>
                </div>

                <div class="flex-shrink-0 text-center" :class="isStageReached(stage.progress) ? '' : 'opacity-50'">
                  <div
                    class="text-3xl md:text-4xl font-black mb-1"
                    :class="isStageReached(stage.progress)
                      ? 'bg-gradient-to-br from-cyan-400 to-blue-500 bg-clip-text text-transparent'
                      : 'text-gray-600'"
                  >
                    {{ stage.progress }}%
                  </div>
                  <div :class="['text-xs', isStageReached(stage.progress) ? 'text-cyan-400' : 'text-gray-600']">
                    {{ getStageStatus(stage.progress) === 'completed' ? '已完成' : getStageStatus(stage.progress) === 'current' ? '进行中' : '未达成' }}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>
      </template>
    </div>
  </div>
</template>

<style scoped>
.ark-bg-delay-1 {
  animation-delay: 1s;
}

@keyframes ark-fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes ark-slideUp {
  from { opacity: 0; transform: translateY(30px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes ark-shine {
  0% { transform: translateX(-100%) skewX(-15deg); }
  100% { transform: translateX(200%) skewX(-15deg); }
}

.ark-fade-in {
  animation: ark-fadeIn 1s ease-out;
}

.ark-slide-up {
  animation: ark-slideUp 0.8s ease-out forwards;
  opacity: 0;
}

.ark-shine {
  animation: ark-shine 3s ease-in-out infinite;
}
</style>
