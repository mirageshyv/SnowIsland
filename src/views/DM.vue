<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import ArkProgressView from './ArkProgressView.vue'
import ShelterProgressView from './ShelterProgressView.vue'
import DmTradesOverview from './DmTradesOverview.vue'
import RebelMilestoneView from './RebelMilestoneView.vue'
import CatastrophePanel from '../components/CatastrophePanel.vue'
import WarehouseView from './WarehouseView.vue'
import ActionFeedbackView from './ActionFeedbackView.vue'
import FactionActionFeedbackView from './FactionActionFeedbackView.vue'
import NightActionSettlementView from './NightActionSettlementView.vue'
import DmPlayerInventoryView from './DmPlayerInventoryView.vue'
import DmCombatAssistView from './DmCombatAssistView.vue'
import DmPlayerModalInventory from '../components/DmPlayerModalInventory.vue'
import { dmPlayerAPI, jobAPI, skillAPI } from '../utils/api.js'

const FACTIONS = ['统治者', '反叛者', '冒险者', '天灾使者', '平民']

const router = useRouter()
const username = localStorage.getItem('username') || ''
const activeTab = ref('players')
const inventoryInitialPlayerId = ref(null)

const playerList = ref([])
const jobs = ref([])
const skills = ref([])
const playersLoading = ref(false)
const playersError = ref('')
const savingPlayer = ref(false)
const showPasswordIds = ref(new Set())
const createStartingItems = ref([])
const createInventoryRef = ref(null)

const filterForm = reactive({
  name: '',
  job: '',
  faction: ''
})

const showEditModal = ref(false)
const showCreateModal = ref(false)
const editingPlayer = ref(null)
const createForm = reactive({
  name: '',
  faction: '平民',
  jobId: '',
  skillId: '',
  loginUsername: '',
  loginPassword: 'test123',
  grantStartingInventory: true,
  isWeak: false,
  isOverworked: false,
  isInjured: false
})

const jobNameById = computed(() => {
  const map = new Map()
  for (const job of jobs.value) {
    map.set(job.id, job.name)
  }
  return map
})

const jobFilterOptions = computed(() => {
  const names = new Set()
  for (const p of playerList.value) {
    if (p.jobName) names.add(p.jobName)
  }
  return [...names].sort((a, b) => a.localeCompare(b, 'zh-CN'))
})

function normalizePlayer(raw) {
  const faction =
    typeof raw.faction === 'string'
      ? raw.faction
      : raw.faction?.name ?? raw.faction ?? '平民'
  const jobId = raw.jobId ?? raw.job_id ?? null
  const skillId = raw.skillId ?? raw.skill_id ?? null
  return {
    id: raw.id,
    name: raw.name ?? '',
    jobId,
    jobName: raw.jobName ?? (jobId ? jobNameById.value.get(jobId) ?? '—' : '—'),
    skillId,
    skillName: raw.skillName ?? '—',
    faction,
    loginUsername: raw.loginUsername ?? '',
    loginPassword: raw.loginPassword ?? '',
    isWeak: Boolean(raw.isWeak ?? raw.is_weak),
    isOverworked: Boolean(raw.isOverworked ?? raw.is_overworked),
    isInjured: Boolean(raw.isInjured ?? raw.is_injured)
  }
}

/** 阵营专属特性 + 平民特性（全员可选） */
const skillsForFaction = (faction) => {
  return skills.value.filter((s) => {
    const f = s.faction
    if (!f || f === '平民') return true
    return faction ? f === faction : true
  })
}

function togglePasswordVisible(id) {
  const next = new Set(showPasswordIds.value)
  if (next.has(id)) next.delete(id)
  else next.add(id)
  showPasswordIds.value = next
}

const filteredPlayers = computed(() => {
  let rows = playerList.value
  const nameQ = filterForm.name.trim().toLowerCase()
  if (nameQ) {
    rows = rows.filter((p) => p.name.toLowerCase().includes(nameQ))
  }
  if (filterForm.job) {
    rows = rows.filter((p) => p.jobName === filterForm.job)
  }
  if (filterForm.faction) {
    rows = rows.filter((p) => p.faction === filterForm.faction)
  }
  return rows
})

const getFactionColor = (faction) => {
  const colors = {
    统治者: 'text-amber-400 bg-amber-500/20',
    反叛者: 'text-red-400 bg-red-500/20',
    冒险者: 'text-emerald-400 bg-emerald-500/20',
    天灾使者: 'text-purple-400 bg-purple-500/20',
    平民: 'text-gray-400 bg-gray-500/20'
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

async function loadJobs() {
  const list = await jobAPI.getAll()
  jobs.value = Array.isArray(list) ? list : []
}

async function loadSkills() {
  const list = await skillAPI.getAll()
  skills.value = Array.isArray(list) ? list : []
}

async function loadPlayers() {
  playersLoading.value = true
  playersError.value = ''
  try {
    const result = await dmPlayerAPI.list()
    if (!result?.success) {
      playerList.value = []
      playersError.value = result?.message || '无法加载玩家列表（请确认后端已启动）'
      return
    }
    const list = result.players || []
    playerList.value = list.map((p) => normalizePlayer(p))
  } catch (e) {
    playerList.value = []
    playersError.value = '加载玩家失败: ' + (e.message || '未知错误')
  } finally {
    playersLoading.value = false
  }
}

async function refreshPlayers() {
  await Promise.all([loadJobs(), loadSkills()])
  await loadPlayers()
}

const handleLogout = () => {
  localStorage.removeItem('userRole')
  localStorage.removeItem('username')
  localStorage.removeItem('playerId')
  router.push('/')
}

const openPlayerInventory = (playerId) => {
  inventoryInitialPlayerId.value = playerId
  activeTab.value = 'inventories'
}

const resetFilter = () => {
  filterForm.name = ''
  filterForm.job = ''
  filterForm.faction = ''
}

function openEditModal(player) {
  editingPlayer.value = {
    id: player.id,
    name: player.name,
    faction: player.faction,
    jobId: player.jobId ?? '',
    skillId: player.skillId ?? '',
    loginUsername: player.loginUsername ?? '',
    loginPassword: player.loginPassword ?? '',
    isWeak: player.isWeak,
    isOverworked: player.isOverworked,
    isInjured: player.isInjured
  }
  showEditModal.value = true
}

function closeEditModal() {
  showEditModal.value = false
  editingPlayer.value = null
}

function openCreateModal() {
  createForm.name = ''
  createForm.faction = '平民'
  createForm.jobId = ''
  createForm.skillId = ''
  createForm.loginUsername = ''
  createForm.loginPassword = 'test123'
  createForm.grantStartingInventory = true
  createForm.isWeak = false
  createForm.isOverworked = false
  createForm.isInjured = false
  createStartingItems.value = []
  showCreateModal.value = true
}

function closeCreateModal() {
  showCreateModal.value = false
}

async function saveEditPlayer() {
  if (!editingPlayer.value) return
  const name = editingPlayer.value.name?.trim()
  if (!name) {
    alert('请输入玩家姓名')
    return
  }
  savingPlayer.value = true
  try {
    const payload = {
      name,
      faction: editingPlayer.value.faction,
      isWeak: editingPlayer.value.isWeak,
      isOverworked: editingPlayer.value.isOverworked,
      isInjured: editingPlayer.value.isInjured,
      loginUsername: editingPlayer.value.loginUsername?.trim() || undefined,
      loginPassword: editingPlayer.value.loginPassword || undefined
    }
    if (editingPlayer.value.jobId !== '' && editingPlayer.value.jobId != null) {
      payload.jobId = Number(editingPlayer.value.jobId)
    } else {
      payload.jobId = null
    }
    payload.skillId =
      editingPlayer.value.skillId !== '' && editingPlayer.value.skillId != null
        ? Number(editingPlayer.value.skillId)
        : null
    const result = await dmPlayerAPI.update(editingPlayer.value.id, payload)
    if (result?.success) {
      closeEditModal()
      await loadPlayers()
    } else {
      alert(result?.message || '保存失败')
    }
  } catch (e) {
    alert('保存失败: ' + (e.message || '未知错误'))
  } finally {
    savingPlayer.value = false
  }
}

async function createPlayer() {
  const name = createForm.name.trim()
  if (!name) {
    alert('请输入玩家姓名')
    return
  }
  savingPlayer.value = true
  try {
    const payload = {
      name,
      faction: createForm.faction,
      isWeak: createForm.isWeak,
      isOverworked: createForm.isOverworked,
      isInjured: createForm.isInjured,
      grantStartingInventory: createForm.grantStartingInventory,
      loginUsername: createForm.loginUsername.trim() || undefined,
      loginPassword: createForm.loginPassword || 'test123'
    }
    if (createForm.jobId !== '' && createForm.jobId != null) {
      payload.jobId = Number(createForm.jobId)
    }
    if (createForm.skillId !== '' && createForm.skillId != null) {
      payload.skillId = Number(createForm.skillId)
    }
    if (createStartingItems.value.length > 0) {
      payload.startingItems = createStartingItems.value.map((i) => ({
        itemType: i.itemType,
        itemId: i.itemId,
        quantity: i.quantity
      }))
      payload.grantStartingInventory = false
    }
    const result = await dmPlayerAPI.create(payload)
    if (result?.success) {
      closeCreateModal()
      await loadPlayers()
      const u = result.username || ''
      const p = result.password || 'test123'
      alert(`玩家已创建。\n登录账号：${u}\n密码：${p}`)
    } else {
      alert(result?.message || '创建失败')
    }
  } catch (e) {
    alert('创建失败: ' + (e.message || '未知错误'))
  } finally {
    savingPlayer.value = false
  }
}

async function handleDelete(player) {
  if (!confirm(`确定删除玩家「${player.name}」？此操作不可撤销。`)) return
  savingPlayer.value = true
  try {
    const result = await dmPlayerAPI.delete(player.id)
    if (result?.success) {
      await loadPlayers()
    } else {
      alert(result?.message || '删除失败')
    }
  } catch (e) {
    alert('删除失败: ' + (e.message || '未知错误'))
  } finally {
    savingPlayer.value = false
  }
}

watch(activeTab, (tab) => {
  if (tab === 'players' && playerList.value.length === 0 && !playersLoading.value) {
    refreshPlayers()
  }
})

onMounted(() => {
  refreshPlayers()
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
          :class="activeTab === 'settings' ? 'bg-[#2d4263] text-white' : 'text-gray-400 hover:bg-[#151b2e] hover:text-gray-300'"
          @click="activeTab = 'settings'"
        >
          游戏设置
        </button>
        <button
          type="button"
          class="w-full text-left px-4 py-3 rounded-xl mb-2 transition-colors font-medium"
          :class="activeTab === 'actionFeedback' ? 'bg-[#2d4263] text-white' : 'text-gray-400 hover:bg-[#151b2e] hover:text-gray-300'"
          @click="activeTab = 'actionFeedback'"
        >
          📋 行动反馈
        </button>
        <button
          type="button"
          class="w-full text-left px-4 py-3 rounded-xl mb-2 transition-colors font-medium"
          :class="activeTab === 'factionActionFeedback' ? 'bg-[#2d4263] text-white' : 'text-gray-400 hover:bg-[#151b2e] hover:text-gray-300'"
          @click="activeTab = 'factionActionFeedback'"
        >
          阵营行动反馈
        </button>
        <button
          type="button"
          class="w-full text-left px-4 py-3 rounded-xl mb-2 transition-colors font-medium"
          :class="activeTab === 'nightActionSettlement' ? 'bg-[#2d4263] text-white' : 'text-gray-400 hover:bg-[#151b2e] hover:text-gray-300'"
          @click="activeTab = 'nightActionSettlement'"
        >
          夜晚行动结算
        </button>
        <button
          type="button"
          class="w-full text-left px-4 py-3 rounded-xl mb-2 transition-colors font-medium"
          :class="activeTab === 'combatAssist' ? 'bg-[#2d4263] text-white' : 'text-gray-400 hover:bg-[#151b2e] hover:text-gray-300'"
          @click="activeTab = 'combatAssist'"
        >
          战斗辅助
        </button>
        <button
          type="button"
          class="w-full text-left px-4 py-3 rounded-xl mb-2 transition-colors font-medium"
          :class="activeTab === 'inventories' ? 'bg-[#2d4263] text-white' : 'text-gray-400 hover:bg-[#151b2e] hover:text-gray-300'"
          @click="activeTab = 'inventories'"
        >
          玩家背包
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
          :class="activeTab === 'trades' ? 'bg-[#2d4263] text-white' : 'text-gray-400 hover:bg-[#151b2e] hover:text-gray-300'"
          @click="activeTab = 'trades'"
        >
          交易一览
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
          :class="activeTab === 'ark' ? 'bg-[#2d4263] text-white' : 'text-gray-400 hover:bg-[#151b2e] hover:text-gray-300'"
          @click="activeTab = 'ark'"
        >
          方舟建造进度
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
          :class="activeTab === 'logs' ? 'bg-[#2d4263] text-white' : 'text-gray-400 hover:bg-[#151b2e] hover:text-gray-300'"
          @click="activeTab = 'logs'"
        >
          系统日志
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
          <div class="flex gap-2">
            <button
              type="button"
              class="bg-white/5 hover:bg-white/10 text-gray-300 px-4 py-2 rounded-xl text-sm transition-all disabled:opacity-50"
              :disabled="playersLoading"
              @click="refreshPlayers"
            >
              刷新
            </button>
            <button
              type="button"
              class="bg-gradient-to-r from-blue-500 to-blue-600 hover:from-blue-600 hover:to-blue-700 text-white px-4 py-2 rounded-xl transition-all text-sm shadow-lg shadow-blue-500/30 disabled:opacity-50"
              :disabled="savingPlayer"
              @click="openCreateModal"
            >
              添加玩家
            </button>
          </div>
        </div>

        <p v-if="playersError" class="mb-4 text-red-400 text-sm">{{ playersError }}</p>

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
                <option v-for="jobName in jobFilterOptions" :key="jobName" :value="jobName">
                  {{ jobName }}
                </option>
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
            <div class="flex gap-2 items-center">
              <span class="text-gray-500 text-xs">共 {{ filteredPlayers.length }} / {{ playerList.length }} 人</span>
              <button
                type="button"
                class="bg-white/5 hover:bg-white/10 text-gray-400 px-4 py-2.5 rounded-xl text-sm transition-all"
                @click="resetFilter"
              >
                重置筛选
              </button>
            </div>
          </div>
        </div>

        <!-- Player Table -->
        <div class="bg-gradient-to-br from-[#1a2332] to-[#0f1419] border border-white/10 rounded-2xl overflow-hidden">
          <div v-if="playersLoading" class="py-16 flex justify-center">
            <div class="w-10 h-10 border-4 border-cyan-500 border-t-transparent rounded-full animate-spin" />
          </div>
          <div v-else-if="filteredPlayers.length === 0" class="py-16 text-center text-gray-500 text-sm">
            {{ playerList.length === 0 ? '暂无玩家数据' : '没有符合筛选条件的玩家' }}
          </div>
          <div v-else class="overflow-x-auto">
            <table class="w-full">
              <thead class="bg-white/5">
                <tr>
                  <th class="text-left text-gray-400 text-xs font-medium px-4 py-4">ID</th>
                  <th class="text-left text-gray-400 text-xs font-medium px-4 py-4">姓名</th>
                  <th class="text-left text-gray-400 text-xs font-medium px-4 py-4">登录账号</th>
                  <th class="text-left text-gray-400 text-xs font-medium px-4 py-4">密码</th>
                  <th class="text-left text-gray-400 text-xs font-medium px-4 py-4">职业</th>
                  <th class="text-left text-gray-400 text-xs font-medium px-4 py-4">特性</th>
                  <th class="text-left text-gray-400 text-xs font-medium px-4 py-4">阵营</th>
                  <th class="text-left text-gray-400 text-xs font-medium px-4 py-4">状态</th>
                  <th class="text-left text-gray-400 text-xs font-medium px-4 py-4">操作</th>
                </tr>
              </thead>
              <tbody class="divide-y divide-white/5">
                <tr v-for="player in filteredPlayers" :key="player.id" class="hover:bg-white/5 transition-colors">
                  <td class="px-4 py-4 text-gray-300 text-sm">{{ player.id }}</td>
                  <td class="px-4 py-4 text-white text-sm font-medium">{{ player.name }}</td>
                  <td class="px-4 py-4 text-gray-300 text-sm font-mono">{{ player.loginUsername || '—' }}</td>
                  <td class="px-4 py-4 text-gray-300 text-sm">
                    <span v-if="showPasswordIds.has(player.id)" class="font-mono">{{ player.loginPassword || '—' }}</span>
                    <span v-else>••••••</span>
                    <button
                      type="button"
                      class="ml-2 text-cyan-500/80 text-xs hover:text-cyan-400"
                      @click="togglePasswordVisible(player.id)"
                    >
                      {{ showPasswordIds.has(player.id) ? '隐藏' : '显示' }}
                    </button>
                  </td>
                  <td class="px-4 py-4 text-gray-300 text-sm">{{ player.jobName }}</td>
                  <td class="px-4 py-4 text-gray-300 text-sm max-w-[120px] truncate" :title="player.skillName">{{ player.skillName }}</td>
                  <td class="px-4 py-4">
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
                  <td class="px-4 py-4">
                    <div class="flex gap-2">
                      <button
                        type="button"
                        class="text-cyan-400 hover:text-cyan-300 text-sm transition-colors"
                        @click="openPlayerInventory(player.id)"
                      >
                        背包
                      </button>
                      <button
                        type="button"
                        class="text-blue-400 hover:text-blue-300 text-sm transition-colors"
                        @click="openEditModal(player)"
                      >
                        编辑
                      </button>
                      <button
                        type="button"
                        class="text-red-400 hover:text-red-300 text-sm transition-colors disabled:opacity-50"
                        :disabled="savingPlayer"
                        @click="handleDelete(player)"
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

        <div
          v-if="showEditModal && editingPlayer"
          class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/70 overflow-y-auto"
          @click.self="closeEditModal"
        >
          <div class="bg-[#1a2332] border border-white/10 rounded-2xl w-full max-w-lg p-6 shadow-xl my-8" @click.stop>
            <h3 class="text-white text-lg font-medium mb-4">编辑玩家</h3>
            <div class="space-y-4">
              <div>
                <label class="block text-gray-400 text-xs mb-1">姓名</label>
                <input v-model="editingPlayer.name" type="text" class="w-full bg-black/30 border border-white/10 rounded-lg px-3 py-2 text-white text-sm" />
              </div>
              <div>
                <label class="block text-gray-400 text-xs mb-1">阵营</label>
                <select v-model="editingPlayer.faction" class="w-full bg-black/30 border border-white/10 rounded-lg px-3 py-2 text-white text-sm">
                  <option v-for="f in FACTIONS" :key="f" :value="f">{{ f }}</option>
                </select>
              </div>
              <div>
                <label class="block text-gray-400 text-xs mb-1">职业</label>
                <select v-model="editingPlayer.jobId" class="w-full bg-black/30 border border-white/10 rounded-lg px-3 py-2 text-white text-sm">
                  <option value="">未分配</option>
                  <option v-for="job in jobs" :key="job.id" :value="job.id">{{ job.name }}</option>
                </select>
              </div>
              <div>
                <label class="block text-gray-400 text-xs mb-1">特性</label>
                <select v-model="editingPlayer.skillId" class="w-full bg-black/30 border border-white/10 rounded-lg px-3 py-2 text-white text-sm">
                  <option value="">未分配</option>
                  <option v-for="sk in skillsForFaction(editingPlayer.faction)" :key="sk.id" :value="sk.id">{{ sk.name }}</option>
                </select>
              </div>
              <div>
                <label class="block text-gray-400 text-xs mb-1">登录账号</label>
                <input v-model="editingPlayer.loginUsername" type="text" class="w-full bg-black/30 border border-white/10 rounded-lg px-3 py-2 text-white text-sm font-mono" />
              </div>
              <div>
                <label class="block text-gray-400 text-xs mb-1">登录密码</label>
                <input v-model="editingPlayer.loginPassword" type="text" class="w-full bg-black/30 border border-white/10 rounded-lg px-3 py-2 text-white text-sm font-mono" />
              </div>
              <div class="flex flex-wrap gap-4 text-sm text-gray-300">
                <label class="flex items-center gap-2"><input v-model="editingPlayer.isWeak" type="checkbox" class="rounded" />虚弱</label>
                <label class="flex items-center gap-2"><input v-model="editingPlayer.isOverworked" type="checkbox" class="rounded" />过劳</label>
                <label class="flex items-center gap-2"><input v-model="editingPlayer.isInjured" type="checkbox" class="rounded" />受伤</label>
              </div>
              <p class="text-gray-500 text-xs">修改背包请使用侧栏「玩家背包」。</p>
            </div>
            <div class="flex justify-end gap-2 mt-6">
              <button type="button" class="px-4 py-2 text-gray-400 rounded-lg hover:bg-white/5" @click="closeEditModal">取消</button>
              <button type="button" class="px-4 py-2 bg-cyan-600 text-white rounded-lg disabled:opacity-50" :disabled="savingPlayer" @click="saveEditPlayer">保存</button>
            </div>
          </div>
        </div>

        <div
          v-if="showCreateModal"
          class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/70 overflow-y-auto"
          @click.self="closeCreateModal"
        >
          <div class="bg-[#1a2332] border border-white/10 rounded-2xl w-full max-w-2xl p-6 shadow-xl my-8" @click.stop>
            <h3 class="text-white text-lg font-medium mb-4">添加玩家</h3>
            <div class="space-y-4">
              <div>
                <label class="block text-gray-400 text-xs mb-1">姓名</label>
                <input v-model="createForm.name" type="text" class="w-full bg-black/30 border border-white/10 rounded-lg px-3 py-2 text-white text-sm" />
              </div>
              <div>
                <label class="block text-gray-400 text-xs mb-1">阵营</label>
                <select v-model="createForm.faction" class="w-full bg-black/30 border border-white/10 rounded-lg px-3 py-2 text-white text-sm">
                  <option v-for="f in FACTIONS" :key="f" :value="f">{{ f }}</option>
                </select>
              </div>
              <div>
                <label class="block text-gray-400 text-xs mb-1">职业（可选）</label>
                <select v-model="createForm.jobId" class="w-full bg-black/30 border border-white/10 rounded-lg px-3 py-2 text-white text-sm">
                  <option value="">未分配</option>
                  <option v-for="job in jobs" :key="job.id" :value="job.id">{{ job.name }}</option>
                </select>
              </div>
              <div>
                <label class="block text-gray-400 text-xs mb-1">特性</label>
                <select v-model="createForm.skillId" class="w-full bg-black/30 border border-white/10 rounded-lg px-3 py-2 text-white text-sm">
                  <option value="">未分配</option>
                  <option v-for="sk in skillsForFaction(createForm.faction)" :key="sk.id" :value="sk.id">{{ sk.name }}</option>
                </select>
              </div>
              <div>
                <label class="block text-gray-400 text-xs mb-1">登录账号（留空则 player{id}）</label>
                <input v-model="createForm.loginUsername" type="text" placeholder="可选" class="w-full bg-black/30 border border-white/10 rounded-lg px-3 py-2 text-white text-sm font-mono" />
              </div>
              <div>
                <label class="block text-gray-400 text-xs mb-1">登录密码</label>
                <input v-model="createForm.loginPassword" type="text" class="w-full bg-black/30 border border-white/10 rounded-lg px-3 py-2 text-white text-sm font-mono" />
              </div>
              <label class="flex items-center gap-2 text-sm text-gray-300 sm:col-span-2">
                <input v-model="createForm.grantStartingInventory" type="checkbox" class="rounded" />
                创建时按职业发放初始背包（下方可调整）
              </label>
              <div class="flex flex-wrap gap-4 text-sm text-gray-300">
                <label class="flex items-center gap-2"><input v-model="createForm.isWeak" type="checkbox" class="rounded" />虚弱</label>
                <label class="flex items-center gap-2"><input v-model="createForm.isOverworked" type="checkbox" class="rounded" />过劳</label>
                <label class="flex items-center gap-2"><input v-model="createForm.isInjured" type="checkbox" class="rounded" />受伤</label>
              </div>
              <DmPlayerModalInventory
                ref="createInventoryRef"
                v-model="createStartingItems"
                :job-id="createForm.jobId || null"
                compact
              />
            </div>
            <div class="flex justify-end gap-2 mt-6">
              <button type="button" class="px-4 py-2 text-gray-400 rounded-lg hover:bg-white/5" @click="closeCreateModal">取消</button>
              <button type="button" class="px-4 py-2 bg-blue-600 text-white rounded-lg disabled:opacity-50" :disabled="savingPlayer" @click="createPlayer">创建</button>
            </div>
          </div>
        </div>
      </div>

      <div v-else-if="activeTab === 'inventories'">
        <DmPlayerInventoryView
          ref="inventoryViewRef"
          :initial-player-id="inventoryInitialPlayerId"
        />
      </div>

      <div v-else-if="activeTab === 'ark'">
        <ArkProgressView embedded />
      </div>

      <div v-else-if="activeTab === 'shelter'">
        <ShelterProgressView mode="dm" />
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
        <CatastrophePanel :is-dm="true" embedded />
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

      <div v-else-if="activeTab === 'factionActionFeedback'">
        <FactionActionFeedbackView />
      </div>

      <div v-else-if="activeTab === 'nightActionSettlement'">
        <NightActionSettlementView />
      </div>

      <div v-else-if="activeTab === 'combatAssist'">
        <DmCombatAssistView />
      </div>

      <!-- Other Tabs -->
      <div v-else>
        <h1 class="text-white mb-6 tracking-tight text-2xl">
          {{ activeTab === 'settings' ? '游戏设置' : '系统日志' }}
        </h1>
        <div class="bg-[#0f1419] border border-[#1f2937] rounded-xl p-6">
          <p class="text-gray-500 font-normal">功能开发中...</p>
        </div>
      </div>
    </main>
  </div>
</template>
