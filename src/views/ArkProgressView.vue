<script setup>
import { ref, computed, onMounted } from 'vue'
import { arkAPI } from '@/utils/api.js'
import { ElMessage } from 'element-plus'

const arkData = ref(null)
const loading = ref(true)
const investForm = ref({ wood: 0, metal: 0, sealant: 0 })
const installing = ref(false)

const rawRole = localStorage.getItem('userRole')
console.log('=== ArkProgressView Debug ===')
console.log('Raw role from localStorage:', rawRole)
console.log('Username:', localStorage.getItem('username'))
console.log('UserID:', localStorage.getItem('userId'))

const currentUserRole = computed(() => {
  return (rawRole || 'player').toLowerCase().trim()
})

const isDm = computed(() => {
  const role = currentUserRole.value
  const result = role === 'dm'
  console.log('Computed role:', role)
  console.log('isDm:', result)
  return result
})

const arkCurrentProgress = computed(() => {
  return arkData.value?.completionPercentage || 0
})

const progressColor = computed(() => {
  const p = arkCurrentProgress.value
  if (p < 50) return 'from-red-600 to-red-400'
  if (p < 100) return 'from-blue-600 to-blue-400'
  return 'from-yellow-500 to-yellow-300'
})

const progressTextColor = computed(() => {
  const p = arkCurrentProgress.value
  if (p < 50) return 'text-red-400'
  if (p < 100) return 'text-blue-400'
  return 'text-yellow-400'
})

const showThresholdLine = computed(() => {
  return arkCurrentProgress.value < 50
})

const arkBuildStages = [
  { progress: 30, title: '底部龙骨', description: '底部龙骨与基础结构已完成，船体轮廓初步形成。' },
  { progress: 50, title: '基本框架', description: '船舷与主舱完成，具备勉强出航能力。' },
  { progress: 70, title: '船壳完成', description: '加装护舷和附属空间，稳定性显著提升。' },
  { progress: 90, title: '甲板铺设', description: '甲板与舱室进一步完善，物资承载能力增强。' },
  { progress: 100, title: '方舟完工', description: '方舟全面完工，可承载人员和长期补给。' },
]

const getStageStatus = (stageProgress) => {
  if (arkCurrentProgress.value >= stageProgress) {
    return arkCurrentProgress.value === stageProgress ? 'current' : 'completed'
  }
  return 'pending'
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
  } catch (e) {
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
  } catch (e) {
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
  } catch (e) {
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
  } catch (e) {
    ElMessage.error('重置失败')
  }
}

onMounted(() => {
  fetchArkStatus()
})
</script>

<template>
  <div class="max-w-4xl mx-auto">
    <div class="text-center mb-10 pt-2">
      <h1 class="text-white text-3xl md:text-4xl font-semibold tracking-wide mb-3">方舟建造进度</h1>
      <p class="text-gray-500 text-base md:text-lg">追踪方舟从无到有的每一步</p>
    </div>

    <div class="relative bg-gradient-to-br from-[#1a2332] to-[#0f1419] border border-white/10 rounded-3xl p-8 mb-10 overflow-hidden shadow-2xl shadow-black/20">
      <div class="absolute top-0 right-0 w-64 h-64 bg-cyan-500/5 rounded-full blur-3xl"></div>

      <div v-if="loading" class="flex items-center justify-center py-8">
        <div class="w-8 h-8 border-2 border-cyan-500 border-t-transparent rounded-full animate-spin"></div>
        <span class="ml-3 text-gray-400">加载中...</span>
      </div>

      <div v-else-if="!arkData" class="flex flex-col items-center justify-center py-8">
        <p class="text-red-400 mb-4">获取数据失败，请检查后端服务</p>
        <button @click="fetchArkStatus" class="px-4 py-2 bg-blue-500/20 text-blue-400 rounded-xl hover:bg-blue-500/30">
          重试
        </button>
      </div>

      <div v-else class="relative">
        <div class="flex items-center justify-between mb-4">
          <span class="text-gray-300 text-lg">整体进度</span>
          <span :class="['text-3xl font-semibold tabular-nums', progressTextColor]">
            {{ Number(arkCurrentProgress).toFixed(1) }}%
          </span>
        </div>

        <div class="relative w-full h-6 rounded-full bg-black/30 border border-white/10 overflow-visible">
          <div
            class="h-full rounded-full bg-gradient-to-r transition-all duration-1000 ease-out"
            :class="progressColor"
            :style="{ width: `${Math.min(100, Number(arkCurrentProgress))}%` }"
          />
          <div v-if="showThresholdLine" class="absolute top-0 h-full w-0.5 bg-red-500 z-10" style="left: 50%">
            <div class="absolute top-full mt-1 left-1/2 transform -translate-x-1/2 whitespace-nowrap text-red-400 text-xs font-medium">
              方舟基础出航下限 50%
            </div>
          </div>
        </div>

        <div class="mt-6 grid grid-cols-2 md:grid-cols-3 gap-4">
          <div class="bg-black/20 rounded-xl p-4 border border-white/5">
            <div class="text-gray-400 text-xs mb-2">木材</div>
            <div class="text-white font-bold text-lg">
              {{ arkData.currentWood || 0 }} / {{ arkData.targetWood || 250 }} 吨
            </div>
            <div class="mt-1 h-2 bg-black/30 rounded-full overflow-hidden">
              <div
                class="h-full bg-amber-500"
                :style="{ width: `${Math.min(100, ((arkData.currentWood || 0) / (arkData.targetWood || 250)) * 100)}%` }"
              ></div>
            </div>
          </div>
          <div class="bg-black/20 rounded-xl p-4 border border-white/5">
            <div class="text-gray-400 text-xs mb-2">金属制品</div>
            <div class="text-white font-bold text-lg">
              {{ arkData.currentMetal || 0 }} / {{ arkData.targetMetal || 100 }} 吨
            </div>
            <div class="mt-1 h-2 bg-black/30 rounded-full overflow-hidden">
              <div
                class="h-full bg-slate-400"
                :style="{ width: `${Math.min(100, ((arkData.currentMetal || 0) / (arkData.targetMetal || 100)) * 100)}%` }"
              ></div>
            </div>
          </div>
          <div class="bg-black/20 rounded-xl p-4 border border-white/5">
            <div class="text-gray-400 text-xs mb-2">密封材料</div>
            <div class="text-white font-bold text-lg">
              {{ arkData.currentSealant || 0 }} / {{ arkData.targetSealant || 100 }} kg
            </div>
            <div class="mt-1 h-2 bg-black/30 rounded-full overflow-hidden">
              <div
                class="h-full bg-stone-500"
                :style="{ width: `${Math.min(100, ((arkData.currentSealant || 0) / (arkData.targetSealant || 100)) * 100)}%` }"
              ></div>
            </div>
          </div>
        </div>

        <div class="mt-6 grid grid-cols-3 gap-3">
          <div class="bg-black/20 rounded-xl p-3 border border-white/5 text-center">
            <div class="text-gray-400 text-xs mb-1">发动机</div>
            <div class="text-xl font-bold text-white">{{ arkData.engineCount || 0 }} / 3</div>
            <div class="mt-2 flex gap-1 justify-center">
              <button v-if="isDm" v-for="i in 3" :key="'engine-btn-' + i" @click="installComponent('engine', i)" :disabled="installing"
                :class="['px-2 py-1 text-xs rounded transition-colors', arkData.engineCount >= i ? 'bg-cyan-500/20 text-cyan-400' : 'bg-gray-500/10 text-gray-500 hover:bg-gray-500/20']"
              >
                {{ i }}个
              </button>
              <span v-else v-for="i in 3" :key="'engine-span-' + i"
                :class="['px-2 py-1 text-xs rounded', arkData.engineCount >= i ? 'bg-cyan-500/20 text-cyan-400' : 'bg-gray-500/10 text-gray-500']"
              >
                {{ i }}
              </span>
            </div>
          </div>
          <div class="bg-black/20 rounded-xl p-3 border border-white/5 text-center">
            <div class="text-gray-400 text-xs mb-1">螺旋桨</div>
            <div class="text-xl font-bold text-white">{{ arkData.propellerCount || 0 }} / 2</div>
            <div class="mt-2 flex gap-1 justify-center">
              <button v-if="isDm" v-for="i in 2" :key="'propeller-btn-' + i" @click="installComponent('propeller', i)" :disabled="installing"
                :class="['px-2 py-1 text-xs rounded transition-colors', arkData.propellerCount >= i ? 'bg-emerald-500/20 text-emerald-400' : 'bg-gray-500/10 text-gray-500 hover:bg-gray-500/20']"
              >
                {{ i }}个
              </button>
              <span v-else v-for="i in 2" :key="'propeller-span-' + i"
                :class="['px-2 py-1 text-xs rounded', arkData.propellerCount >= i ? 'bg-emerald-500/20 text-emerald-400' : 'bg-gray-500/10 text-gray-500']"
              >
                {{ i }}
              </span>
            </div>
          </div>
          <div class="bg-black/20 rounded-xl p-3 border border-white/5 text-center">
            <div class="text-gray-400 text-xs mb-1">发电机</div>
            <div class="text-xl font-bold text-white">{{ arkData.generatorCount || 0 }} / 1</div>
            <div class="mt-2">
              <button v-if="isDm" @click="installComponent('generator', arkData.generatorCount >= 1 ? 0 : 1)" :disabled="installing"
                :class="['px-3 py-1 text-xs rounded transition-colors', arkData.generatorCount >= 1 ? 'bg-emerald-500/20 text-emerald-400' : 'bg-orange-500/20 text-orange-400 hover:bg-orange-500/30']"
              >
                {{ arkData.generatorCount >= 1 ? '已安装' : '安装' }}
              </button>
              <span v-else :class="['px-3 py-1 text-xs rounded', arkData.generatorCount >= 1 ? 'bg-emerald-500/20 text-emerald-400' : 'bg-orange-500/20 text-orange-400']">
                {{ arkData.generatorCount >= 1 ? '已安装' : '未安装' }}
              </span>
            </div>
          </div>
        </div>

        <div class="mt-6 flex items-center justify-between">
          <div class="bg-black/20 rounded-xl p-3 border border-white/5 flex-1 mr-3">
            <div class="text-gray-400 text-xs mb-1">载重能力</div>
            <div class="text-white font-bold text-lg">{{ arkData.currentCargoCapacity || 0 }} 点</div>
          </div>
          <div class="bg-black/20 rounded-xl p-3 border border-white/5 flex-1 ml-3"
            :class="arkData.hasSail ? 'border-cyan-500/30' : 'border-white/5'"
          >
            <div class="text-gray-400 text-xs mb-1">帆</div>
            <div v-if="isDm && !arkData.hasSail" class="flex items-center gap-2">
              <span class="text-gray-400">未建造</span>
              <button @click="buildSail" class="px-2 py-1 text-xs bg-cyan-500/20 text-cyan-400 rounded hover:bg-cyan-500/30">
                建造
              </button>
            </div>
            <div v-else :class="['font-bold text-lg', arkData.hasSail ? 'text-cyan-400' : 'text-gray-400']">
              {{ arkData.hasSail ? '已建造' : '未建造' }}
            </div>
          </div>
        </div>

        <div v-if="isDm" class="mt-6 bg-black/20 rounded-xl p-4 border border-white/5">
          <div class="text-gray-400 text-sm mb-3">投入资源</div>
          <div class="grid grid-cols-3 gap-3">
            <div>
              <label class="text-xs text-gray-500 block mb-1">木材（吨）</label>
              <input v-model.number="investForm.wood" type="number" min="0"
                class="w-full bg-black/30 border border-white/10 rounded-lg px-3 py-2 text-white text-sm focus:outline-none focus:border-cyan-500"
              />
            </div>
            <div>
              <label class="text-xs text-gray-500 block mb-1">金属（吨）</label>
              <input v-model.number="investForm.metal" type="number" min="0"
                class="w-full bg-black/30 border border-white/10 rounded-lg px-3 py-2 text-white text-sm focus:outline-none focus:border-cyan-500"
              />
            </div>
            <div>
              <label class="text-xs text-gray-500 block mb-1">密封材料（kg）</label>
              <input v-model.number="investForm.sealant" type="number" min="0"
                class="w-full bg-black/30 border border-white/10 rounded-lg px-3 py-2 text-white text-sm focus:outline-none focus:border-cyan-500"
              />
            </div>
          </div>
          <div class="mt-4 flex gap-3">
            <button @click="investResources"
              class="flex-1 bg-gradient-to-r from-cyan-500 to-blue-600 text-white py-2 rounded-lg font-semibold hover:from-cyan-400 hover:to-blue-500 transition-all"
            >
              确认投入
            </button>
            <button @click="resetArk" class="px-4 py-2 bg-red-500/20 text-red-400 rounded-lg text-sm hover:bg-red-500/30">
              重置
            </button>
          </div>
        </div>
      </div>
    </div>

    <div class="space-y-2 mb-12 pl-1">
      <h2 class="text-2xl text-white mb-4 tracking-tight">建设阶段</h2>
      <div v-for="stage in arkBuildStages" :key="stage.title"
        :class="['relative border-l-4 pl-6 pb-6 transition-all duration-300',
          getStageStatus(stage.progress) === 'current' ? 'border-cyan-500' :
          getStageStatus(stage.progress) === 'completed' ? 'border-emerald-500' : 'border-white/20'
        ]"
      >
        <div :class="['bg-white/5 border rounded-2xl p-5', getStageStatus(stage.progress) === 'pending' ? 'opacity-60' : '']">
          <div class="flex items-center justify-between gap-3 mb-3">
            <h3 class="text-lg text-white font-medium tracking-tight">{{ stage.title }}</h3>
            <span :class="['shrink-0 px-3 py-1 rounded-full text-sm font-semibold tabular-nums',
                getStageStatus(stage.progress) === 'current' ? 'bg-cyan-500/20 text-cyan-400' :
                getStageStatus(stage.progress) === 'completed' ? 'bg-emerald-500/20 text-emerald-400' :
                'bg-white/10 text-gray-400'
              ]"
            >
              {{ stage.progress }}%
            </span>
          </div>
          <p class="text-gray-300 text-sm leading-7">{{ stage.description }}</p>
        </div>
      </div>
    </div>
  </div>
</template>
