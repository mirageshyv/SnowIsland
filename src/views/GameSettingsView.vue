<script setup>
import { ref, onMounted } from 'vue'
import { gameStateAPI, catastropheAPI } from '../utils/api.js'

const userRole = (localStorage.getItem('userRole') || '').toLowerCase()

const loading = ref(true)
const saving = ref(false)
const advancing = ref(false)
const message = ref(null)
const error = ref('')

const form = ref({
  currentDay: 1,
  currentPhase: 'DAY',
  isGameOver: false,
  catastropheTriggered: false,
  extraCardDue: false,
  requiredFoodUnits: 2,
  requiredFuelKg: 15
})

const catastropheProgress = ref(0)

const phaseOptions = [
  { value: 'DAY', label: '白天' },
  { value: 'NIGHT', label: '夜晚' }
]

function phaseLabel(phase) {
  return phase === 'NIGHT' ? '夜晚' : '白天'
}

async function load() {
  loading.value = true
  error.value = ''
  try {
    const [state, cat] = await Promise.all([
      gameStateAPI.get(),
      catastropheAPI.getGameState().catch(() => ({}))
    ])
    if (state?.success !== false) {
      form.value = {
        currentDay: Math.max(1, Math.floor(Number(state.currentDay) || 1)),
        currentPhase: state.currentPhase === 'NIGHT' ? 'NIGHT' : 'DAY',
        isGameOver: Boolean(state.isGameOver),
        catastropheTriggered: Boolean(state.catastropheTriggered),
        extraCardDue: Boolean(state.extraCardDue),
        requiredFoodUnits: Math.max(0, Math.floor(Number(state.requiredFoodUnits) || 2)),
        requiredFuelKg: Math.max(0, Math.floor(Number(state.requiredFuelKg) || 15))
      }
    } else {
      error.value = state?.message || '无法加载游戏状态'
    }
    catastropheProgress.value = Number(cat?.catastropheProgress) || 0
  } catch (e) {
    error.value = '加载失败：' + (e.message || '未知错误')
  } finally {
    loading.value = false
  }
}

async function save() {
  saving.value = true
  message.value = null
  error.value = ''
  try {
    const result = await gameStateAPI.update(
      {
        currentDay: Math.max(1, Math.floor(Number(form.value.currentDay) || 1)),
        currentPhase: form.value.currentPhase,
        isGameOver: form.value.isGameOver,
        catastropheTriggered: form.value.catastropheTriggered,
        extraCardDue: form.value.extraCardDue,
        requiredFoodUnits: Math.max(0, Math.floor(Number(form.value.requiredFoodUnits) || 0)),
        requiredFuelKg: Math.max(0, Math.floor(Number(form.value.requiredFuelKg) || 0))
      },
      userRole
    )
    if (result?.success) {
      message.value = result.message || '已保存'
      form.value.currentDay = Math.max(1, Math.floor(Number(result.currentDay) || form.value.currentDay))
      form.value.currentPhase = result.currentPhase === 'NIGHT' ? 'NIGHT' : 'DAY'
      if (result.requiredFoodUnits != null) {
        form.value.requiredFoodUnits = Math.max(0, Math.floor(Number(result.requiredFoodUnits)))
      }
      if (result.requiredFuelKg != null) {
        form.value.requiredFuelKg = Math.max(0, Math.floor(Number(result.requiredFuelKg)))
      }
    } else {
      error.value = result?.message || '保存失败'
    }
  } catch (e) {
    error.value = '保存失败：' + (e.message || '未知错误')
  } finally {
    saving.value = false
  }
}

async function advanceDay() {
  if (!confirm('推进一天将同时增加天灾进度，并可能触发天灾。确定继续？')) return
  advancing.value = true
  message.value = null
  error.value = ''
  try {
    const result = await catastropheAPI.advanceDay()
    if (result?.success) {
      form.value.currentDay = Number(result.currentDay) || form.value.currentDay + 1
      if (result.catastropheTriggered) {
        form.value.catastropheTriggered = true
        form.value.isGameOver = true
      }
      catastropheProgress.value = Number(result.progress) ?? catastropheProgress.value
      message.value = result.message || `已推进至第 ${form.value.currentDay} 天`
    } else {
      error.value = result?.message || '推进失败'
    }
  } catch (e) {
    error.value = '推进失败：' + (e.message || '未知错误')
  } finally {
    advancing.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="max-w-2xl">
    <div class="mb-6">
      <h1 class="text-white text-2xl font-semibold tracking-tight mb-1">游戏设置</h1>
      <p class="text-gray-500 text-sm">管理全局游戏状态。玩家「个人信息」页会同步显示当前天数。</p>
    </div>

    <div v-if="loading" class="flex justify-center py-16">
      <div class="w-10 h-10 border-4 border-cyan-500 border-t-transparent rounded-full animate-spin" />
    </div>

    <template v-else>
      <p v-if="error" class="mb-4 text-red-400 text-sm">{{ error }}</p>
      <p v-if="message" class="mb-4 text-emerald-400 text-sm">{{ message }}</p>

      <div class="bg-gradient-to-br from-[#1a2332] to-[#0f1419] border border-white/10 rounded-2xl p-6 space-y-6">
        <div>
          <label class="block text-gray-400 text-xs mb-2">当前天数</label>
          <div class="flex flex-wrap items-center gap-3">
            <input
              v-model.number="form.currentDay"
              type="number"
              min="1"
              max="99"
              step="1"
              class="w-28 bg-black/30 border border-white/10 rounded-lg px-3 py-2 text-white text-lg font-semibold tabular-nums focus:outline-none focus:border-cyan-500/50"
            />
            <span class="text-gray-500 text-sm">第 {{ form.currentDay }} 天</span>
            <button
              type="button"
              class="ml-auto px-4 py-2 rounded-lg bg-purple-600/30 border border-purple-500/40 text-purple-200 text-sm hover:bg-purple-600/40 disabled:opacity-50"
              :disabled="advancing || saving"
              @click="advanceDay"
            >
              {{ advancing ? '推进中…' : '推进一天（天灾进度）' }}
            </button>
          </div>
          <p class="text-gray-600 text-xs mt-2">天灾进度：{{ catastropheProgress }}%（推进一天 +33/34）</p>
        </div>

        <div>
          <label class="block text-gray-400 text-xs mb-2">当前阶段</label>
          <div class="flex flex-wrap gap-2">
            <button
              v-for="opt in phaseOptions"
              :key="opt.value"
              type="button"
              class="px-4 py-2 rounded-lg text-sm border transition-colors"
              :class="
                form.currentPhase === opt.value
                  ? 'bg-cyan-600/30 border-cyan-500/50 text-cyan-200 font-medium'
                  : 'bg-white/5 border-white/10 text-gray-400 hover:bg-white/10'
              "
              @click="form.currentPhase = opt.value"
            >
              {{ opt.label }}
            </button>
          </div>
          <p class="text-gray-600 text-xs mt-2">当前：{{ phaseLabel(form.currentPhase) }}（{{ form.currentPhase }}）</p>
        </div>

        <div class="grid grid-cols-1 sm:grid-cols-2 gap-4 pt-2 border-t border-white/10">
          <div>
            <label class="block text-gray-400 text-xs mb-2">当日每人进食需求（单位）</label>
            <input
              v-model.number="form.requiredFoodUnits"
              type="number"
              min="0"
              max="99"
              step="1"
              class="w-full bg-black/30 border border-white/10 rounded-lg px-3 py-2 text-white tabular-nums focus:outline-none focus:border-cyan-500/50"
            />
            <p class="text-gray-600 text-xs mt-1">默认 2 单位；玩家可在个人信息页选择食物提交。</p>
          </div>
          <div>
            <label class="block text-gray-400 text-xs mb-2">当日每人取暖需求（单位）</label>
            <input
              v-model.number="form.requiredFuelKg"
              type="number"
              min="0"
              max="999"
              step="1"
              class="w-full bg-black/30 border border-white/10 rounded-lg px-3 py-2 text-white tabular-nums focus:outline-none focus:border-cyan-500/50"
            />
            <p class="text-gray-600 text-xs mt-1">默认15热值；木材1kg：1热值，燃料1kg：15热值</p>
          </div>
        </div>

        <div class="space-y-3 pt-2 border-t border-white/10">
          <label class="flex items-center gap-2 text-sm text-gray-300 cursor-pointer">
            <input v-model="form.catastropheTriggered" type="checkbox" class="rounded" />
            天灾已触发
          </label>
          <label class="flex items-center gap-2 text-sm text-gray-300 cursor-pointer">
            <input v-model="form.extraCardDue" type="checkbox" class="rounded" />
            待触发额外天灾牌
          </label>
          <label class="flex items-center gap-2 text-sm text-gray-300 cursor-pointer">
            <input v-model="form.isGameOver" type="checkbox" class="rounded" />
            游戏已结束
          </label>
        </div>

        <div class="flex flex-wrap gap-3 pt-2">
          <button
            type="button"
            class="px-5 py-2.5 rounded-lg bg-cyan-600 text-white text-sm font-medium hover:bg-cyan-500 disabled:opacity-50"
            :disabled="saving"
            @click="save"
          >
            {{ saving ? '保存中…' : '保存设置' }}
          </button>
          <button
            type="button"
            class="px-5 py-2.5 rounded-lg bg-white/5 text-gray-400 text-sm hover:bg-white/10"
            :disabled="saving"
            @click="load"
          >
            重新加载
          </button>
        </div>
      </div>
    </template>
  </div>
</template>
