<script setup>
import { ref, onMounted, computed } from 'vue'
import { gameResetAPI } from '../utils/api.js'

const loading = ref(true)
const previewData = ref(null)
const resetting = ref(false)
const resetProgress = ref(0)
const resetStep = ref('')
const resetResult = ref(null)
const showConfirmModal = ref(false)
const confirmStep = ref(0)
const confirmPassword = ref('')

const currentUserRole = computed(() => localStorage.getItem('userRole') || '')

const resetSteps = [
  { key: 'level1', name: '清除无外键依赖表' },
  { key: 'level2', name: '清除依赖player表' },
  { key: 'level3', name: '清除player表' },
  { key: 'level4', name: '清除其他可清除表' },
  { key: 'level5', name: '清除游戏状态表' },
  { key: 'init', name: '初始化初始数据' }
]

async function loadPreview() {
  loading.value = true
  try {
    const data = await gameResetAPI.getPreview()
    if (data) {
      previewData.value = data
    }
  } catch (e) {
    console.error('加载预览数据失败:', e)
  } finally {
    loading.value = false
  }
}

function openConfirmModal() {
  confirmStep.value = 0
  confirmPassword.value = ''
  showConfirmModal.value = true
}

function closeConfirmModal() {
  showConfirmModal.value = false
  confirmStep.value = 0
  confirmPassword.value = ''
}

function nextConfirmStep() {
  if (confirmStep.value === 0) {
    confirmStep.value = 1
  } else if (confirmStep.value === 1) {
    confirmStep.value = 2
  }
}

function prevConfirmStep() {
  if (confirmStep.value > 0) {
    confirmStep.value--
  }
}

async function executeReset() {
  resetting.value = true
  resetProgress.value = 0
  resetStep.value = '准备开始...'
  resetResult.value = null

  try {
    const result = await gameResetAPI.reset(currentUserRole.value)
    
    if (result?.success) {
      resetResult.value = {
        success: true,
        message: result.message,
        duration: result.durationMs,
        deletedCounts: result.deletedCounts
      }
    } else {
      resetResult.value = {
        success: false,
        message: result?.message || '重置失败'
      }
    }
  } catch (e) {
    resetResult.value = {
      success: false,
      message: '重置失败: ' + (e.message || '未知错误')
    }
  } finally {
    resetting.value = false
    resetProgress.value = 100
    closeConfirmModal()
  }
}

onMounted(() => {
  loadPreview()
})
</script>

<template>
  <div class="min-h-screen bg-slate-900 text-white p-6">
    <div class="max-w-4xl mx-auto">
      <div class="flex items-center justify-between mb-6">
        <h1 class="text-2xl font-bold text-cyan-400">复原到游戏开始状态</h1>
        <button
          @click="loadPreview"
          class="px-4 py-2 rounded-lg bg-slate-700 hover:bg-slate-600 text-sm transition-colors"
          :disabled="loading || resetting"
        >
          {{ loading ? '加载中...' : '刷新预览' }}
        </button>
      </div>

      <div v-if="loading" class="text-center py-12">
        <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-cyan-400 mx-auto mb-4"></div>
        <p>加载数据中...</p>
      </div>

      <div v-else-if="previewData" class="space-y-6">
        <div class="bg-slate-800/50 rounded-xl p-6 border border-slate-700">
          <h2 class="text-lg font-semibold text-cyan-300 mb-4">操作说明</h2>
          <div class="bg-red-900/30 border border-red-700/50 rounded-lg p-4 mb-4">
            <p class="text-red-300 text-sm mb-2">⚠️ 警告：此操作将清除所有游戏进程数据，包括玩家角色、物品、行动记录、建造进度等。</p>
            <p class="text-red-300 text-sm">⚠️ 以下数据将被保留：用户账户、物品/武器/材料定义、职业配置、规则书、事件配置、天灾牌定义、终局事件配置等。</p>
          </div>
          <p class="text-slate-300 text-sm">
            执行此操作后，系统将恢复至游戏开始前的初始状态，包括重新初始化玩家数据、避难所库存、方舟建造进度、仓库物资，以及 12 名开放 NPC 的人设与阵营态度。
          </p>
        </div>

        <div class="bg-slate-800/50 rounded-xl p-6 border border-slate-700">
          <h2 class="text-lg font-semibold text-cyan-300 mb-4">当前数据概览</h2>
          <div class="grid grid-cols-2 md:grid-cols-5 gap-4 mb-6">
            <div class="bg-slate-700/50 rounded-lg p-4 text-center">
              <div class="text-2xl font-bold text-cyan-400">{{ previewData.totalRecords || 0 }}</div>
              <div class="text-xs text-slate-400">待清除记录</div>
            </div>
            <div class="bg-slate-700/50 rounded-lg p-4 text-center">
              <div class="text-2xl font-bold text-green-400">{{ previewData.clearedTables?.length || 0 }}</div>
              <div class="text-xs text-slate-400">将清除表数</div>
            </div>
            <div class="bg-slate-700/50 rounded-lg p-4 text-center">
              <div class="text-2xl font-bold text-yellow-400">{{ previewData.preservedTables?.length || 0 }}</div>
              <div class="text-xs text-slate-400">保留表数</div>
            </div>
            <div class="bg-slate-700/50 rounded-lg p-4 text-center">
              <div class="text-2xl font-bold text-purple-400">{{ previewData.tableCounts?.player || 0 }}</div>
              <div class="text-xs text-slate-400">玩家数量</div>
            </div>
            <div class="bg-slate-700/50 rounded-lg p-4 text-center">
              <div class="text-2xl font-bold text-orange-400">{{ previewData.tableCounts?.player_items || 0 }}</div>
              <div class="text-xs text-slate-400">物品数量</div>
            </div>
          </div>

          <div class="space-y-3">
            <h3 class="text-sm font-medium text-slate-400">各表记录数：</h3>
            <div class="grid grid-cols-2 md:grid-cols-5 gap-2">
              <div v-for="(count, table) in previewData.tableCounts" :key="table"
                class="bg-slate-700/30 rounded px-3 py-2 text-xs">
                <div class="text-slate-400">{{ table }}</div>
                <div class="text-white font-medium">{{ count }}</div>
              </div>
            </div>
          </div>
        </div>

        <div class="bg-slate-800/50 rounded-xl p-6 border border-slate-700">
          <h2 class="text-lg font-semibold text-cyan-300 mb-4">将清除的数据表</h2>
          <div class="flex flex-wrap gap-2">
            <span v-for="table in previewData.clearedTables" :key="table"
              class="px-3 py-1 rounded-full bg-red-900/30 text-red-300 text-xs border border-red-700/50">
              {{ table }}
            </span>
          </div>
        </div>

        <div class="bg-slate-800/50 rounded-xl p-6 border border-slate-700">
          <h2 class="text-lg font-semibold text-cyan-300 mb-4">将保留的数据表</h2>
          <div class="flex flex-wrap gap-2">
            <span v-for="table in previewData.preservedTables" :key="table"
              class="px-3 py-1 rounded-full bg-green-900/30 text-green-300 text-xs border border-green-700/50">
              {{ table }}
            </span>
          </div>
        </div>

        <div class="flex justify-center">
          <button
            @click="openConfirmModal"
            class="px-8 py-4 rounded-xl bg-red-600 hover:bg-red-500 text-white font-semibold text-lg transition-all transform hover:scale-105"
            :disabled="resetting"
          >
            {{ resetting ? '复原中...' : '复原到游戏开始状态' }}
          </button>
        </div>

        <div v-if="resetting" class="bg-slate-800/50 rounded-xl p-6 border border-slate-700">
          <h2 class="text-lg font-semibold text-cyan-300 mb-4">复原进度</h2>
          <div class="mb-4">
            <div class="flex justify-between text-sm mb-2">
              <span>{{ resetStep }}</span>
              <span>{{ Math.round(resetProgress) }}%</span>
            </div>
            <div class="h-3 bg-slate-700 rounded-full overflow-hidden">
              <div
                class="h-full bg-gradient-to-r from-cyan-500 to-blue-500 transition-all duration-300"
                :style="{ width: resetProgress + '%' }"
              ></div>
            </div>
          </div>
          <div class="text-center">
            <div class="animate-spin rounded-full h-10 w-10 border-b-2 border-cyan-400 mx-auto"></div>
            <p class="text-slate-400 text-sm mt-2">正在执行数据复原...</p>
          </div>
        </div>

        <div v-if="resetResult" 
          class="rounded-xl p-6 border"
          :class="resetResult.success ? 'bg-green-900/30 border-green-700/50' : 'bg-red-900/30 border-red-700/50'">
          <div class="flex items-center gap-3 mb-4">
            <div :class="resetResult.success ? 'text-green-400' : 'text-red-400'">
              {{ resetResult.success ? '✅' : '❌' }}
            </div>
            <h2 class="text-lg font-semibold" :class="resetResult.success ? 'text-green-300' : 'text-red-300'">
              {{ resetResult.success ? '复原成功' : '复原失败' }}
            </h2>
          </div>
          <p class="text-slate-300 mb-4">{{ resetResult.message }}</p>
          <div v-if="resetResult.success && resetResult.duration" class="text-sm text-slate-400 mb-4">
            耗时：{{ resetResult.duration }} 毫秒
          </div>
          <div v-if="resetResult.success && resetResult.deletedCounts" class="space-y-2">
            <h3 class="text-sm font-medium text-slate-400">操作详情：</h3>
            <div v-for="(count, step) in resetResult.deletedCounts" :key="step" class="text-sm">
              <span class="text-slate-400">{{ step }}：</span>
              <span class="text-white">{{ count }}</span>
            </div>
          </div>
          <button
            @click="loadPreview"
            class="mt-4 px-4 py-2 rounded-lg bg-slate-700 hover:bg-slate-600 text-sm transition-colors"
          >
            刷新数据预览
          </button>
        </div>
      </div>

      <div v-if="showConfirmModal" class="fixed inset-0 bg-black/70 flex items-center justify-center z-50 p-4">
        <div class="bg-slate-800 rounded-2xl p-6 max-w-md w-full border border-slate-700">
          <h3 class="text-xl font-bold text-white mb-4">确认复原操作</h3>

          <div v-if="confirmStep === 0" class="space-y-4">
            <p class="text-red-300 text-sm">
              此操作将清除所有游戏进程数据，恢复至游戏初始状态。
            </p>
            <div class="flex items-center gap-2">
              <input
                type="checkbox"
                id="confirm1"
                class="rounded"
                required
              />
              <label for="confirm1" class="text-sm text-slate-300">我已了解此操作的后果</label>
            </div>
          </div>

          <div v-if="confirmStep === 1" class="space-y-4">
            <p class="text-red-300 text-sm">
              请再次确认：所有玩家数据、物品、行动记录、建造进度等都将被清除。
            </p>
            <div class="flex items-center gap-2">
              <input
                type="checkbox"
                id="confirm2"
                class="rounded"
                required
              />
              <label for="confirm2" class="text-sm text-slate-300">我确认要继续执行</label>
            </div>
          </div>

          <div v-if="confirmStep === 2" class="space-y-4">
            <p class="text-red-300 text-sm mb-4">
              最后确认：请输入 "CONFIRM" 以确认操作
            </p>
            <input
              v-model="confirmPassword"
              type="text"
              placeholder="输入 CONFIRM"
              class="w-full px-4 py-3 rounded-lg bg-slate-700 border border-slate-600 text-white placeholder-slate-500 focus:outline-none focus:border-cyan-500"
            />
          </div>

          <div class="flex justify-between mt-6">
            <button
              @click="confirmStep > 0 ? prevConfirmStep() : closeConfirmModal()"
              class="px-4 py-2 rounded-lg bg-slate-700 hover:bg-slate-600 text-sm transition-colors"
            >
              {{ confirmStep > 0 ? '上一步' : '取消' }}
            </button>
            <button
              v-if="confirmStep < 2"
              @click="nextConfirmStep()"
              class="px-4 py-2 rounded-lg bg-cyan-600 hover:bg-cyan-500 text-sm transition-colors"
            >
              下一步
            </button>
            <button
              v-else
              @click="confirmPassword === 'CONFIRM' ? executeReset() : null"
              :disabled="confirmPassword !== 'CONFIRM'"
              class="px-4 py-2 rounded-lg bg-red-600 hover:bg-red-500 text-sm transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
            >
              确认复原
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>