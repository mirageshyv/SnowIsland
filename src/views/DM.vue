<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import ArkProgressView from './ArkProgressView.vue'
import ShelterProgressView from './ShelterProgressView.vue'
import DmTradesOverview from './DmTradesOverview.vue'
import RebelMilestoneView from './RebelMilestoneView.vue'
import CatastrophePanel from '../components/CatastrophePanel.vue'
import WarehouseView from './WarehouseView.vue'
import ActionFeedbackView from './ActionFeedbackView.vue'

const router = useRouter()
const username = localStorage.getItem('username') || ''
const activeTab = ref('players')

const playerList = ref([
  { id: 1, name: '阿尔伯特', job: '战士', faction: '统治者', isWeak: false, isOverworked: false, isInjured: false },
  { id: 2, name: '莉莉丝', job: '法师', faction: '反叛者', isWeak: false, isOverworked: true, isInjured: false },
  { id: 3, name: '罗宾', job: '盗贼', faction: '冒险者', isWeak: true, isOverworked: false, isInjured: false },
  { id: 4, name: '亚瑟', job: '牧师', faction: '天灾使者', isWeak: false, isOverworked: false, isInjured: true },
  { id: 5, name: '艾米丽', job: '猎人', faction: '平民', isWeak: false, isOverworked: false, isInjured: false }
])

const filterForm = reactive({
  name: '',
  job: '',
  faction: ''
})

const getFactionColor = (faction) => {
  const colors = {
    '统治者': 'text-amber-400 bg-amber-500/20',
    '反叛者': 'text-red-400 bg-red-500/20',
    '冒险者': 'text-emerald-400 bg-emerald-500/20',
    '天灾使者': 'text-purple-400 bg-purple-500/20',
    '平民': 'text-gray-400 bg-gray-500/20'
  }
  return colors[faction] || colors['平民']
}

const getStatusBadges = (player) => {
  const badges = []
  if (player.isWeak) badges.push({ text: '虚弱', color: 'text-amber-400 bg-amber-500/20' })
  if (player.isOverworked) badges.push({ text: '过劳', color: 'text-blue-400 bg-blue-500/20' })
  if (player.isInjured) badges.push({ text: '受伤', color: 'text-red-400 bg-red-500/20' })
  if (badges.length === 0) badges.push({ text: '健康', color: 'text-emerald-400 bg-emerald-500/20' })
  return badges
}

const handleLogout = () => {
  localStorage.removeItem('userRole')
  localStorage.removeItem('username')
  localStorage.removeItem('playerId')
  router.push('/')
}

const handleDelete = (id) => {
  playerList.value = playerList.value.filter(item => item.id !== id)
}

const handleFilter = () => {
  console.log('Filter applied', filterForm)
}

const resetFilter = () => {
  filterForm.name = ''
  filterForm.job = ''
  filterForm.faction = ''
}

onMounted(() => {
  console.log('DM page mounted')
})
</script>

<template>
  <!-- h-screen + overflow-hidden：侧栏固定；仅右侧 main 纵向滚动 -->
  <div class="flex h-screen max-h-[100dvh] overflow-hidden bg-[#0a0e1a]">
    <!-- Sidebar -->
    <aside class="flex h-full w-64 shrink-0 flex-col border-r border-[#1f2937] bg-[#0f1419]">
      <div class="shrink-0 border-b border-[#1f2937] p-6">
        <h2 class="text-white tracking-tight text-lg">DM管理中心</h2>
      </div>

      <nav class="min-h-0 flex-1 overflow-y-auto p-4">
        <button
          type="button"
          class="w-full text-left px-4 py-3 rounded-xl mb-2 transition-colors font-medium"
          :class="activeTab === 'players' ? 'bg-[#2d4263] text-white' : 'text-gray-400 hover:bg-[#151b2e] hover:text-gray-300'"
          @click="activeTab = 'players'"
        >
          玩家管理
        </button>
        <button
          type="button"
          class="w-full text-left px-4 py-3 rounded-xl mb-2 transition-colors font-medium"
          :class="activeTab === 'ark' ? 'bg-[#2d4263] text-white' : 'text-gray-400 hover:bg-[#151b2e] hover:text-gray-300'"
          @click="activeTab = 'ark'"
        >
          方舟建造进度
        </button>
        <button
          type="button"
          class="w-full text-left px-4 py-3 rounded-xl mb-2 transition-colors font-medium"
          :class="activeTab === 'shelter' ? 'bg-[#2d4263] text-white' : 'text-gray-400 hover:bg-[#151b2e] hover:text-gray-300'"
          @click="activeTab = 'shelter'"
        >
          统治者避难所
        </button>
        <button
          type="button"
          class="w-full text-left px-4 py-3 rounded-xl mb-2 transition-colors font-medium"
          :class="activeTab === 'settings' ? 'bg-[#2d4263] text-white' : 'text-gray-400 hover:bg-[#151b2e] hover:text-gray-300'"
          @click="activeTab = 'settings'"
        >
          游戏设置
        </button>
        <button
          type="button"
          class="w-full text-left px-4 py-3 rounded-xl mb-2 transition-colors font-medium"
          :class="activeTab === 'trades' ? 'bg-[#2d4263] text-white' : 'text-gray-400 hover:bg-[#151b2e] hover:text-gray-300'"
          @click="activeTab = 'trades'"
        >
          交易一览
        </button>
        <button
          type="button"
          class="w-full text-left px-4 py-3 rounded-xl mb-2 transition-colors font-medium"
          :class="activeTab === 'tasks' ? 'bg-[#2d4263] text-white' : 'text-gray-400 hover:bg-[#151b2e] hover:text-gray-300'"
          @click="activeTab = 'tasks'"
        >
          任务管理
        </button>
        <button
          type="button"
          class="w-full text-left px-4 py-3 rounded-xl mb-2 transition-colors font-medium"
          :class="activeTab === 'logs' ? 'bg-[#2d4263] text-white' : 'text-gray-400 hover:bg-[#151b2e] hover:text-gray-300'"
          @click="activeTab = 'logs'"
        >
          系统日志
        </button>
        <button
          type="button"
          class="w-full text-left px-4 py-3 rounded-xl mb-2 transition-colors font-medium"
          :class="activeTab === 'milestones' ? 'bg-[#2d4263] text-white' : 'text-gray-400 hover:bg-[#151b2e] hover:text-gray-300'"
          @click="activeTab = 'milestones'"
        >
          里程碑管理
        </button>
        <button
          type="button"
          class="w-full text-left px-4 py-3 rounded-xl mb-2 transition-colors font-medium"
          :class="activeTab === 'catastrophe' ? 'bg-[#2d4263] text-white' : 'text-gray-400 hover:bg-[#151b2e] hover:text-gray-300'"
          @click="activeTab = 'catastrophe'"
        >
          天灾降临
        </button>
        <button
          type="button"
          class="w-full text-left px-4 py-3 rounded-xl mb-2 transition-colors font-medium"
          :class="activeTab === 'warehouse' ? 'bg-[#2d4263] text-white' : 'text-gray-400 hover:bg-[#151b2e] hover:text-gray-300'"
          @click="activeTab = 'warehouse'"
        >
          📦 仓库管理
        </button>
        <button
          type="button"
          class="w-full text-left px-4 py-3 rounded-xl mb-2 transition-colors font-medium"
          :class="activeTab === 'actionFeedback' ? 'bg-[#2d4263] text-white' : 'text-gray-400 hover:bg-[#151b2e] hover:text-gray-300'"
          @click="activeTab = 'actionFeedback'"
        >
          📋 行动反馈
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

    <!-- Main Content -->
    <main class="min-h-0 min-w-0 flex-1 overflow-y-auto p-8">
      <!-- Players Tab -->
      <div v-if="activeTab === 'players'" class="max-w-7xl">
        <div class="mb-6 flex items-center justify-between">
          <div>
            <h1 class="text-white mb-1 tracking-tight text-2xl">玩家管理</h1>
            <p class="text-gray-500 text-sm">Player Management</p>
          </div>
          <button
            type="button"
            class="bg-gradient-to-r from-blue-500 to-blue-600 hover:from-blue-600 hover:to-blue-700 text-white px-4 py-2 rounded-xl transition-all text-sm shadow-lg shadow-blue-500/30"
          >
            添加玩家
          </button>
        </div>

        <!-- Filter Section -->
        <div class="bg-gradient-to-br from-[#1a2332] to-[#0f1419] border border-white/10 rounded-2xl p-5 mb-6">
          <div class="flex flex-wrap gap-4 items-end">
            <div class="flex-1 min-w-[200px]">
              <label class="block text-gray-400 text-xs mb-2">姓名</label>
              <input
                v-model="filterForm.name"
                type="text"
                placeholder="请输入玩家姓名"
                class="w-full bg-black/30 border border-white/10 rounded-xl px-4 py-2.5 text-gray-200 text-sm placeholder-gray-500 focus:outline-none focus:border-blue-500/50"
              />
            </div>
            <div class="flex-1 min-w-[200px]">
              <label class="block text-gray-400 text-xs mb-2">职业</label>
              <select
                v-model="filterForm.job"
                class="w-full bg-black/30 border border-white/10 rounded-xl px-4 py-2.5 text-gray-200 text-sm focus:outline-none focus:border-blue-500/50"
              >
                <option value="">全部职业</option>
                <option value="战士">战士</option>
                <option value="法师">法师</option>
                <option value="盗贼">盗贼</option>
                <option value="牧师">牧师</option>
                <option value="猎人">猎人</option>
              </select>
            </div>
            <div class="flex-1 min-w-[200px]">
              <label class="block text-gray-400 text-xs mb-2">阵营</label>
              <select
                v-model="filterForm.faction"
                class="w-full bg-black/30 border border-white/10 rounded-xl px-4 py-2.5 text-gray-200 text-sm focus:outline-none focus:border-blue-500/50"
              >
                <option value="">全部阵营</option>
                <option value="统治者">统治者</option>
                <option value="反叛者">反叛者</option>
                <option value="冒险者">冒险者</option>
                <option value="天灾使者">天灾使者</option>
                <option value="平民">平民</option>
              </select>
            </div>
            <div class="flex gap-2">
              <button
                type="button"
                class="bg-blue-500/20 hover:bg-blue-500/30 text-blue-400 px-4 py-2.5 rounded-xl text-sm transition-all"
                @click="handleFilter"
              >
                筛选
              </button>
              <button
                type="button"
                class="bg-white/5 hover:bg-white/10 text-gray-400 px-4 py-2.5 rounded-xl text-sm transition-all"
                @click="resetFilter"
              >
                重置
              </button>
            </div>
          </div>
        </div>

        <!-- Player Table -->
        <div class="bg-gradient-to-br from-[#1a2332] to-[#0f1419] border border-white/10 rounded-2xl overflow-hidden">
          <div class="overflow-x-auto">
            <table class="w-full">
              <thead class="bg-white/5">
                <tr>
                  <th class="text-left text-gray-400 text-xs font-medium px-6 py-4">ID</th>
                  <th class="text-left text-gray-400 text-xs font-medium px-6 py-4">姓名</th>
                  <th class="text-left text-gray-400 text-xs font-medium px-6 py-4">职业</th>
                  <th class="text-left text-gray-400 text-xs font-medium px-6 py-4">阵营</th>
                  <th class="text-left text-gray-400 text-xs font-medium px-6 py-4">状态</th>
                  <th class="text-left text-gray-400 text-xs font-medium px-6 py-4">操作</th>
                </tr>
              </thead>
              <tbody class="divide-y divide-white/5">
                <tr v-for="player in playerList" :key="player.id" class="hover:bg-white/5 transition-colors">
                  <td class="px-6 py-4 text-gray-300 text-sm">{{ player.id }}</td>
                  <td class="px-6 py-4 text-white text-sm font-medium">{{ player.name }}</td>
                  <td class="px-6 py-4 text-gray-300 text-sm">{{ player.job }}</td>
                  <td class="px-6 py-4">
                    <span :class="['text-xs px-2 py-1 rounded-full', getFactionColor(player.faction)]">
                      {{ player.faction }}
                    </span>
                  </td>
                  <td class="px-6 py-4">
                    <div class="flex gap-1">
                      <span
                        v-for="badge in getStatusBadges(player)"
                        :key="badge.text"
                        :class="['text-xs px-2 py-1 rounded-full', badge.color]"
                      >
                        {{ badge.text }}
                      </span>
                    </div>
                  </td>
                  <td class="px-6 py-4">
                    <div class="flex gap-2">
                      <button
                        type="button"
                        class="text-blue-400 hover:text-blue-300 text-sm transition-colors"
                      >
                        编辑
                      </button>
                      <button
                        type="button"
                        class="text-red-400 hover:text-red-300 text-sm transition-colors"
                        @click="handleDelete(player.id)"
                      >
                        删除
                      </button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <div v-else-if="activeTab === 'ark'">
        <ArkProgressView />
      </div>

      <div v-else-if="activeTab === 'shelter'">
        <ShelterProgressView />
      </div>

      <div v-else-if="activeTab === 'trades'">
        <DmTradesOverview />
      </div>

      <div v-else-if="activeTab === 'milestones'">
        <div class="mb-6">
          <h1 class="text-white mb-1 tracking-tight text-2xl">里程碑管理</h1>
          <p class="text-gray-500 text-sm">管理反抗者阵营的里程碑进度</p>
        </div>
        <RebelMilestoneView embedded :show-header="false" />
      </div>

      <div v-else-if="activeTab === 'catastrophe'">
        <div class="mb-6">
          <h1 class="text-white mb-1 tracking-tight text-2xl">天灾降临</h1>
          <p class="text-gray-500 text-sm">管理天灾进度和天灾牌系统</p>
        </div>
        <CatastrophePanel :is-dm="true" />
      </div>

      <div v-else-if="activeTab === 'warehouse'">
        <div class="mb-6">
          <h1 class="text-white mb-1 tracking-tight text-2xl">仓库管理</h1>
          <p class="text-gray-500 text-sm">管理所有仓库的物资库存</p>
        </div>
        <WarehouseView />
      </div>

      <div v-else-if="activeTab === 'actionFeedback'">
        <div class="mb-6">
          <h1 class="text-white mb-1 tracking-tight text-2xl">行动反馈</h1>
          <p class="text-gray-500 text-sm">查看玩家提交的行动并给予反馈</p>
        </div>
        <ActionFeedbackView />
      </div>

      <!-- Other Tabs -->
      <div v-else>
        <h1 class="text-white mb-6 tracking-tight text-2xl">
          {{ activeTab === 'settings' ? '游戏设置' : activeTab === 'tasks' ? '任务管理' : '系统日志' }}
        </h1>
        <div class="bg-[#0f1419] border border-[#1f2937] rounded-xl p-6">
          <p class="text-gray-500 font-normal">功能开发中...</p>
        </div>
      </div>
    </main>
  </div>
</template>
