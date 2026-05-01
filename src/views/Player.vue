<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import MaterialsPanel from './MaterialsPanel.vue'
import TradePanel from './TradePanel.vue'

const router = useRouter()
const username = localStorage.getItem('username') || ''
const playerId = localStorage.getItem('playerId') || '1'
const activeTab = ref('info')

// 交易面板引用
const tradePanelRef = ref(null)

// 待处理交易数量（初始显示2个）
const pendingTradesCount = ref(2)

const playerInfoMap = {
  '1': {
    name: '阿尔伯特',
    job: '战士',
    faction: '统治者',
    isWeak: false,
    isOverworked: false,
    isInjured: false,
    jobSkills: '近战攻击、防御、武器精通',
    personalSkill: '力量增强',
    avatar: '⚔️'
  },
  '2': {
    name: '莉莉丝',
    job: '法师',
    faction: '反叛者',
    isWeak: false,
    isOverworked: true,
    isInjured: false,
    jobSkills: '元素魔法、法术护盾、远程攻击',
    personalSkill: '魔法抗性',
    avatar: '🔮'
  },
  '3': {
    name: '罗宾',
    job: '盗贼',
    faction: '冒险者',
    isWeak: true,
    isOverworked: false,
    isInjured: false,
    jobSkills: '潜行、开锁、背刺',
    personalSkill: '敏捷提升',
    avatar: '🗡️'
  },
  '4': {
    name: '亚瑟',
    job: '牧师',
    faction: '杀戮者',
    isWeak: false,
    isOverworked: false,
    isInjured: true,
    jobSkills: '治疗、祝福、神圣魔法',
    personalSkill: '生命恢复',
    avatar: '✨'
  },
  '5': {
    name: '艾米丽',
    job: '猎人',
    faction: '平民',
    isWeak: false,
    isOverworked: false,
    isInjured: false,
    jobSkills: '远程攻击、追踪、宠物驯养',
    personalSkill: '幸运加成',
    avatar: '🏹'
  }
}

const playerInfo = computed(() => playerInfoMap[playerId] || playerInfoMap['1'])

const getStatusColor = (status) => {
  const colors = {
    '统治者': 'text-amber-400 bg-amber-500/20',
    '反叛者': 'text-red-400 bg-red-500/20',
    '冒险者': 'text-emerald-400 bg-emerald-500/20',
    '杀戮者': 'text-purple-400 bg-purple-500/20',
    '平民': 'text-gray-400 bg-gray-500/20'
  }
  return colors[playerInfo.value.faction] || colors['平民']
}

const handleLogout = () => {
  localStorage.removeItem('userRole')
  localStorage.removeItem('username')
  localStorage.removeItem('playerId')
  router.push('/')
}

onMounted(() => {
  console.log('Player page mounted', { playerId, playerInfo: playerInfo.value })
})
</script>

<template>
  <div class="min-h-screen bg-[#0a0e1a] flex">
    <!-- Sidebar -->
    <aside class="w-64 bg-[#0f1419] border-r border-[#1f2937] flex flex-col">
      <div class="p-6 border-b border-[#1f2937]">
        <h2 class="text-white tracking-tight text-lg">玩家中心</h2>
      </div>

      <nav class="flex-1 p-4">
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
          class="w-full text-left px-4 py-3 rounded-xl transition-colors font-medium"
          :class="activeTab === 'materials' ? 'bg-[#2d4263] text-white' : 'text-gray-400 hover:bg-[#151b2e] hover:text-gray-300'"
          @click="activeTab = 'materials'"
        >
          物资管理
        </button>
        <button
          type="button"
          class="w-full text-left px-4 py-3 rounded-xl transition-colors font-medium relative"
          :class="activeTab === 'trade' ? 'bg-[#2d4263] text-white' : 'text-gray-400 hover:bg-[#151b2e] hover:text-gray-300'"
          @click="activeTab = 'trade'"
        >
          <span class="flex items-center justify-between">
            <span>交易管理</span>
            <span
              v-if="pendingTradesCount > 0"
              class="bg-red-500 text-white text-xs px-2 py-0.5 rounded-full"
            >
              {{ pendingTradesCount }}
            </span>
          </span>
        </button>
      </nav>

      <div class="p-4 border-t border-[#1f2937]">
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

    <!-- Main Content -->
    <main class="flex-1 p-8 overflow-auto">
      <!-- Personal Info Tab -->
      <div v-if="activeTab === 'info'" class="max-w-4xl">
        <div class="mb-6">
          <h1 class="text-white mb-1 tracking-tight text-2xl">个人信息</h1>
          <p class="text-gray-500 text-sm">Player Information</p>
        </div>

        <!-- Character Card -->
        <div class="relative bg-gradient-to-br from-[#1a2332] to-[#0f1419] border border-white/10 rounded-3xl p-6 mb-6 overflow-hidden">
          <div class="absolute top-0 right-0 w-96 h-96 bg-blue-500/5 rounded-full blur-3xl" />
          <div class="relative">
            <div class="flex items-start justify-between mb-6">
              <div class="flex items-center gap-5">
                <div class="relative">
                  <div class="absolute inset-0 bg-blue-500/20 rounded-2xl blur-xl" />
                  <div class="relative w-20 h-20 rounded-2xl bg-gradient-to-br from-blue-400 via-blue-500 to-blue-600 flex items-center justify-center shadow-2xl text-4xl">
                    {{ playerInfo.avatar }}
                  </div>
                </div>
                <div>
                  <h2 class="text-white mb-1 tracking-tight text-xl">{{ playerInfo.name }}</h2>
                  <p class="text-gray-400 text-sm mb-3">{{ playerInfo.job }}</p>
                  <div class="flex items-center gap-2">
                    <span class="text-xs text-gray-300 bg-white/10 px-3 py-1 rounded-full backdrop-blur">
                      {{ playerInfo.personalSkill }}
                    </span>
                    <span :class="['text-xs px-3 py-1 rounded-full flex items-center gap-1.5 backdrop-blur', getStatusColor()]">
                      <span class="w-1.5 h-1.5 rounded-full bg-current shadow-lg" />
                      {{ playerInfo.faction }}
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
                <p class="text-gray-200 text-sm">{{ playerInfo.jobSkills }}</p>
              </div>
              <div class="bg-white/5 backdrop-blur border border-white/10 rounded-xl p-4">
                <p class="text-gray-400 text-xs mb-2">个人技能</p>
                <p class="text-gray-200 text-sm">{{ playerInfo.personalSkill }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Status Tab -->
      <div v-else-if="activeTab === 'status'">
        <h1 class="text-white mb-6 tracking-tight text-2xl">游戏状态</h1>
        <div class="bg-[#0f1419] border border-[#1f2937] rounded-xl p-6">
          <p class="text-gray-500 font-normal">游戏状态功能开发中...</p>
        </div>
      </div>

      <!-- Tasks Tab -->
      <div v-else-if="activeTab === 'tasks'">
        <h1 class="text-white mb-6 tracking-tight text-2xl">任务列表</h1>
        <div class="bg-[#0f1419] border border-[#1f2937] rounded-xl p-6">
          <p class="text-gray-500 font-normal">任务列表功能开发中...</p>
        </div>
      </div>

      <!-- Materials Tab -->
      <div v-else-if="activeTab === 'materials'">
        <MaterialsPanel />
      </div>

      <!-- Trade Tab -->
      <div v-else-if="activeTab === 'trade'">
        <TradePanel ref="tradePanelRef" />
      </div>
    </main>
  </div>
</template>
