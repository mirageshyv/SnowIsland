<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import MaterialsPanel from './MaterialsPanel.vue'
import TradePanel from './TradePanel.vue'
import ArkProgressView from './ArkProgressView.vue'
import ShelterProgressView from './ShelterProgressView.vue'
import ActionSubmitView from './ActionSubmitView.vue'
import { tradeAPI, playerAPI } from '../utils/api.js'

const router = useRouter()
const username = localStorage.getItem('username') || ''
const playerId = parseInt(localStorage.getItem('playerId') || '1')
const activeTab = ref('info')

const tradePanelRef = ref(null)

const pendingTradesCount = ref(0)
const loading = ref(false)
const error = ref(null)
const playerInfo = ref(null)
const isEditing = ref(false)
const editForm = ref(null)
const saving = ref(false)
const saveMessage = ref(null)

/** 仅冒险者可见「方舟建造进度」 */
const showArkTab = computed(() => playerInfo.value?.faction === '冒险者')
/** 仅统治者可见「统治者避难所」 */
const showShelterTab = computed(() => playerInfo.value?.faction === '统治者')

watch([showArkTab, showShelterTab, activeTab], () => {
  if (activeTab.value === 'ark' && !showArkTab.value) activeTab.value = 'info'
  if (activeTab.value === 'shelter' && !showShelterTab.value) activeTab.value = 'info'
})

let pollTimer = null

const fetchPendingTradesCount = async () => {
  try {
    const result = await tradeAPI.getIncoming(playerId)
    if (Array.isArray(result)) {
      const pendingCount = result.filter(t => t.status === 'pending').length
      if (pendingCount !== pendingTradesCount.value) {
        console.log('Pending trades count updated:', pendingCount)
        pendingTradesCount.value = pendingCount
      }
    }
  } catch (error) {
    console.log('Failed to fetch pending trades:', error.message)
  }
}

const fetchPlayerInfo = async () => {
  loading.value = true
  error.value = null
  try {
    const result = await playerAPI.getDetails(playerId)
    if (result && result.success) {
      playerInfo.value = result
    } else {
      error.value = result?.message || '获取玩家信息失败'
    }
  } catch (err) {
    error.value = '网络请求失败，请稍后重试'
    console.error('Failed to fetch player info:', err)
  } finally {
    loading.value = false
  }
}

const startPolling = () => {
  fetchPendingTradesCount()
  pollTimer = setInterval(fetchPendingTradesCount, 3000)
}

const stopPolling = () => {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
}

const handleTradeTabClick = () => {
  activeTab.value = 'trade'
  fetchPendingTradesCount()
}

const getStatusColor = () => {
  const faction = playerInfo.value?.faction
  const colors = {
    '统治者': 'text-amber-400 bg-amber-500/20',
    '反叛者': 'text-red-400 bg-red-500/20',
    '冒险者': 'text-emerald-400 bg-emerald-500/20',
    '杀戮者': 'text-purple-400 bg-purple-500/20',
    '平民': 'text-gray-400 bg-gray-500/20'
  }
  return colors[faction] || colors['平民']
}

const startEdit = () => {
  editForm.value = {
    name: playerInfo.value?.name || '',
    isWeak: playerInfo.value?.isWeak || false,
    isOverworked: playerInfo.value?.isOverworked || false,
    isInjured: playerInfo.value?.isInjured || false
  }
  isEditing.value = true
  saveMessage.value = null
}

const cancelEdit = () => {
  isEditing.value = false
  editForm.value = null
  saveMessage.value = null
}

const saveEdit = async () => {
  if (!editForm.value) return
  saving.value = true
  saveMessage.value = null
  try {
    const result = await playerAPI.update(playerId, editForm.value)
    if (result && result.success) {
      saveMessage.value = { type: 'success', text: '保存成功！' }
      await fetchPlayerInfo()
      isEditing.value = false
      editForm.value = null
      setTimeout(() => { saveMessage.value = null }, 3000)
    } else {
      saveMessage.value = { type: 'error', text: result?.message || '保存失败' }
    }
  } catch (err) {
    saveMessage.value = { type: 'error', text: '网络请求失败，请稍后重试' }
    console.error('Failed to save player info:', err)
  } finally {
    saving.value = false
  }
}

const handleLogout = () => {
  localStorage.removeItem('userRole')
  localStorage.removeItem('username')
  localStorage.removeItem('playerId')
  router.push('/')
}

onMounted(() => {
  console.log('Player page mounted, playerId:', playerId)
  fetchPlayerInfo()
  startPolling()
})

onUnmounted(() => {
  stopPolling()
})
</script>

<template>
  <!-- h-screen + overflow-hidden：侧栏固定；仅右侧 main 纵向滚动 -->
  <div class="flex h-screen max-h-[100dvh] overflow-hidden bg-[#0a0e1a]">
    <aside class="flex h-full w-64 shrink-0 flex-col border-r border-[#1f2937] bg-[#0f1419]">
      <div class="shrink-0 border-b border-[#1f2937] p-6">
        <h2 class="text-white tracking-tight text-lg">玩家中心</h2>
      </div>

      <nav class="min-h-0 flex-1 overflow-y-auto p-4">
        <button
          type="button"
          class="w-full text-left px-4 py-3 rounded-xl mb-2 transition-colors font-medium"
          :class="activeTab === 'info' ? 'bg-[#2d4263] text-white' : 'text-gray-400 hover:bg-[#151b2e] hover:text-gray-300'"
          @click="activeTab = 'info'"
        >
          个人信息
        </button>
        <button
          type="button"
          class="w-full text-left px-4 py-3 rounded-xl mb-2 transition-colors font-medium"
          :class="activeTab === 'status' ? 'bg-[#2d4263] text-white' : 'text-gray-400 hover:bg-[#151b2e] hover:text-gray-300'"
          @click="activeTab = 'status'"
        >
          游戏状态
        </button>
        <button
          type="button"
          class="w-full text-left px-4 py-3 rounded-xl mb-2 transition-colors font-medium"
          :class="activeTab === 'tasks' ? 'bg-[#2d4263] text-white' : 'text-gray-400 hover:bg-[#151b2e] hover:text-gray-300'"
          @click="activeTab = 'tasks'"
        >
          任务列表
        </button>
        <button
          type="button"
          class="w-full text-left px-4 py-3 rounded-xl mb-2 transition-colors font-medium"
          :class="activeTab === 'actions' ? 'bg-[#2d4263] text-white' : 'text-gray-400 hover:bg-[#151b2e] hover:text-gray-300'"
          @click="activeTab = 'actions'"
        >
          行动提交
        </button>
        <button
          type="button"
          class="w-full text-left px-4 py-3 rounded-xl mb-2 transition-colors font-medium"
          :class="activeTab === 'materials' ? 'bg-[#2d4263] text-white' : 'text-gray-400 hover:bg-[#151b2e] hover:text-gray-300'"
          @click="activeTab = 'materials'"
        >
          物资管理
        </button>
        <button
          v-if="showArkTab"
          type="button"
          class="w-full text-left px-4 py-3 rounded-xl mb-2 transition-colors font-medium"
          :class="activeTab === 'ark' ? 'bg-[#2d4263] text-white' : 'text-gray-400 hover:bg-[#151b2e] hover:text-gray-300'"
          @click="activeTab = 'ark'"
        >
          方舟建造进度
        </button>
        <button
          v-if="showShelterTab"
          type="button"
          class="w-full text-left px-4 py-3 rounded-xl mb-2 transition-colors font-medium"
          :class="activeTab === 'shelter' ? 'bg-[#2d4263] text-white' : 'text-gray-400 hover:bg-[#151b2e] hover:text-gray-300'"
          @click="activeTab = 'shelter'"
        >
          统治者避难所
        </button>
        <button
          type="button"
          class="w-full text-left px-4 py-3 rounded-xl mb-2 transition-colors font-medium relative group"
          :class="activeTab === 'trade' ? 'bg-[#2d4263] text-white' : 'text-gray-400 hover:bg-[#151b2e] hover:text-gray-300'"
          @click="handleTradeTabClick"
        >
          <span class="flex items-center justify-between">
            <span class="flex items-center gap-2">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7h12m0 0l-4-4m4 4l-4 4m0 6H4m0 0l4 4m-4-4l4-4" />
              </svg>
              <span>交易管理</span>
            </span>
            <span
              v-if="pendingTradesCount > 0"
              class="bg-gradient-to-r from-red-600 to-red-500 text-white text-xs font-bold px-2.5 py-1 rounded-full shadow-lg shadow-red-500/50 animate-pulse min-w-[20px] text-center"
            >
              {{ pendingTradesCount > 9 ? '9+' : pendingTradesCount }}
            </span>
          </span>
        </button>
      </nav>

      <div class="shrink-0 border-t border-[#1f2937] p-4">
        <div class="flex items-center justify-between">
          <span class="text-gray-400 text-sm">{{ username }}</span>
          <button
            type="button"
            class="text-gray-500 hover:text-white text-sm transition-colors"
            @click="handleLogout"
          >
            退出
          </button>
        </div>
      </div>
    </aside>

    <main class="min-h-0 min-w-0 flex-1 overflow-y-auto p-8">
      <div v-if="activeTab === 'info'" class="max-w-4xl">
        <div class="mb-6 flex items-center justify-between">
          <div>
            <h1 class="text-white mb-1 tracking-tight text-2xl">个人信息</h1>
            <p class="text-gray-500 text-sm">Player Information</p>
          </div>
          <div class="flex items-center gap-3">
            <button
              v-if="!loading && playerInfo && !isEditing"
              @click="fetchPlayerInfo"
              class="px-4 py-2 text-sm text-gray-300 hover:text-white bg-white/5 hover:bg-white/10 rounded-lg transition-colors flex items-center gap-2"
            >
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
              </svg>
              刷新
            </button>
            <button
              v-if="!loading && playerInfo && !isEditing"
              @click="startEdit"
              class="px-4 py-2 text-sm text-white bg-blue-600 hover:bg-blue-700 rounded-lg transition-colors flex items-center gap-2"
            >
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
              </svg>
              编辑
            </button>
          </div>
        </div>

        <div v-if="loading" class="flex items-center justify-center py-20">
          <div class="text-center">
            <div class="inline-block animate-spin rounded-full h-12 w-12 border-4 border-blue-500 border-t-transparent mb-4"></div>
            <p class="text-gray-400">加载中...</p>
          </div>
        </div>

        <div v-else-if="error" class="bg-red-500/10 border border-red-500/30 rounded-xl p-6">
          <div class="flex items-center gap-3 mb-4">
            <svg class="w-6 h-6 text-red-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            <p class="text-red-400 font-medium">{{ error }}</p>
          </div>
          <button
            @click="fetchPlayerInfo"
            class="px-4 py-2 text-sm text-white bg-red-600 hover:bg-red-700 rounded-lg transition-colors"
          >
            重试
          </button>
        </div>

        <div v-else-if="playerInfo" class="relative bg-gradient-to-br from-[#1a2332] to-[#0f1419] border border-white/10 rounded-3xl p-6 overflow-hidden">
          <div class="absolute top-0 right-0 w-96 h-96 bg-blue-500/5 rounded-full blur-3xl"></div>

          <div v-if="saveMessage" class="relative z-10 mb-4">
            <div :class="['px-4 py-3 rounded-lg flex items-center gap-3', saveMessage.type === 'success' ? 'bg-green-500/20 border border-green-500/30' : 'bg-red-500/20 border border-red-500/30']">
              <svg v-if="saveMessage.type === 'success'" class="w-5 h-5 text-green-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
              </svg>
              <svg v-else class="w-5 h-5 text-red-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              <span :class="saveMessage.type === 'success' ? 'text-green-400' : 'text-red-400'">{{ saveMessage.text }}</span>
            </div>
          </div>

          <div class="relative z-10">
            <template v-if="isEditing">
              <div class="space-y-6">
                <div>
                  <label class="block text-gray-400 text-sm mb-2">姓名</label>
                  <input
                    v-model="editForm.name"
                    type="text"
                    class="w-full px-4 py-3 bg-white/5 border border-white/10 rounded-xl text-white placeholder-gray-500 focus:outline-none focus:border-blue-500 transition-colors"
                    placeholder="请输入姓名"
                  />
                </div>

                <div>
                  <label class="block text-gray-400 text-sm mb-3">状态</label>
                  <div class="space-y-3">
                    <label class="flex items-center gap-3 cursor-pointer">
                      <input
                        v-model="editForm.isWeak"
                        type="checkbox"
                        class="w-5 h-5 rounded bg-white/5 border-white/10 text-amber-500 focus:ring-amber-500/50"
                      />
                      <span class="text-gray-300">虚弱</span>
                    </label>
                    <label class="flex items-center gap-3 cursor-pointer">
                      <input
                        v-model="editForm.isOverworked"
                        type="checkbox"
                        class="w-5 h-5 rounded bg-white/5 border-white/10 text-blue-500 focus:ring-blue-500/50"
                      />
                      <span class="text-gray-300">过劳</span>
                    </label>
                    <label class="flex items-center gap-3 cursor-pointer">
                      <input
                        v-model="editForm.isInjured"
                        type="checkbox"
                        class="w-5 h-5 rounded bg-white/5 border-white/10 text-red-500 focus:ring-red-500/50"
                      />
                      <span class="text-gray-300">受伤</span>
                    </label>
                  </div>
                </div>

                <div class="flex gap-3 pt-4">
                  <button
                    @click="cancelEdit"
                    :disabled="saving"
                    class="px-6 py-3 text-gray-300 bg-white/5 hover:bg-white/10 rounded-xl transition-colors disabled:opacity-50"
                  >
                    取消
                  </button>
                  <button
                    @click="saveEdit"
                    :disabled="saving"
                    class="px-6 py-3 text-white bg-blue-600 hover:bg-blue-700 rounded-xl transition-colors disabled:opacity-50 flex items-center gap-2"
                  >
                    <div v-if="saving" class="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                    {{ saving ? '保存中...' : '保存' }}
                  </button>
                </div>
              </div>
            </template>

            <template v-else>
              <div class="flex items-start justify-between mb-6">
                <div class="flex items-center gap-5">
                  <div class="relative">
                    <div class="absolute inset-0 bg-blue-500/20 rounded-2xl blur-xl"></div>
                    <div class="relative w-20 h-20 rounded-2xl bg-gradient-to-br from-blue-400 via-blue-500 to-blue-600 flex items-center justify-center shadow-2xl text-4xl">
                      {{ playerInfo.avatar || '🧑' }}
                    </div>
                  </div>
                  <div>
                    <h2 class="text-white mb-1 tracking-tight text-xl">{{ playerInfo.name }}</h2>
                    <p class="text-gray-400 text-sm mb-3">{{ playerInfo.job || '未设定职业' }}</p>
                    <div class="flex items-center gap-2">
                      <span class="text-xs text-gray-300 bg-white/10 px-3 py-1 rounded-full backdrop-blur">
                        {{ playerInfo.personalSkill || '未设定技能' }}
                      </span>
                      <span :class="['text-xs px-3 py-1 rounded-full flex items-center gap-1.5 backdrop-blur', getStatusColor()]">
                        <span class="w-1.5 h-1.5 rounded-full bg-current shadow-lg"></span>
                        {{ playerInfo.faction || '未设定阵营' }}
                      </span>
                    </div>
                  </div>
                </div>

                <div class="bg-white/5 backdrop-blur border border-white/10 rounded-xl px-4 py-3">
                  <p class="text-gray-400 text-xs mb-1">状态</p>
                  <div class="flex gap-2">
                    <span v-if="playerInfo.isWeak" class="text-xs text-amber-400 bg-amber-500/20 px-2 py-1 rounded">虚弱</span>
                    <span v-if="playerInfo.isOverworked" class="text-xs text-blue-400 bg-blue-500/20 px-2 py-1 rounded">过劳</span>
                    <span v-if="playerInfo.isInjured" class="text-xs text-red-400 bg-red-500/20 px-2 py-1 rounded">受伤</span>
                    <span v-if="!playerInfo.isWeak && !playerInfo.isOverworked && !playerInfo.isInjured" class="text-xs text-emerald-400 bg-emerald-500/20 px-2 py-1 rounded">健康</span>
                  </div>
                </div>
              </div>

              <div class="grid grid-cols-2 gap-4">
                <div class="bg-white/5 backdrop-blur border border-white/10 rounded-xl p-4">
                  <p class="text-gray-400 text-xs mb-2">职业技能</p>
                  <p class="text-gray-200 text-sm">{{ playerInfo.jobSkills || '暂无' }}</p>
                </div>
                <div class="bg-white/5 backdrop-blur border border-white/10 rounded-xl p-4">
                  <p class="text-gray-400 text-xs mb-2">个人技能</p>
                  <p class="text-gray-200 text-sm">{{ playerInfo.personalSkill || '暂无' }}</p>
                </div>
              </div>

              <div class="mt-6 pt-6 border-t border-white/10">
                <p class="text-gray-500 text-xs">
                  最后更新: {{ playerInfo.updatedAt ? new Date(playerInfo.updatedAt).toLocaleString('zh-CN') : '未知' }}
                </p>
              </div>
            </template>
          </div>
        </div>
      </div>

      <div v-else-if="activeTab === 'status'">
        <h1 class="text-white mb-6 tracking-tight text-2xl">游戏状态</h1>
        <div class="bg-[#0f1419] border border-[#1f2937] rounded-xl p-6">
          <p class="text-gray-500 font-normal">游戏状态功能开发中...</p>
        </div>
      </div>

      <div v-else-if="activeTab === 'tasks'">
        <h1 class="text-white mb-6 tracking-tight text-2xl">任务列表</h1>
        <div class="bg-[#0f1419] border border-[#1f2937] rounded-xl p-6">
          <p class="text-gray-500 font-normal">任务列表功能开发中...</p>
        </div>
      </div>

      <div v-else-if="activeTab === 'materials'">
        <MaterialsPanel />
      </div>

      <div v-else-if="activeTab === 'actions'">
        <ActionSubmitView embedded />
      </div>

      <div v-else-if="activeTab === 'ark' && showArkTab">
        <ArkProgressView />
      </div>

      <div v-else-if="activeTab === 'shelter' && showShelterTab">
        <ShelterProgressView />
      </div>

      <div v-else-if="activeTab === 'trade'">
        <TradePanel ref="tradePanelRef" />
      </div>
    </main>
  </div>
</template>
