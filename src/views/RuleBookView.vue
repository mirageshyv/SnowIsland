<template>
  <div :class="embedded ? '' : 'min-h-screen bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900 text-white'">
    <div v-if="!embedded" class="sticky top-0 z-40 bg-slate-900/80 backdrop-blur-md border-b border-slate-700">
      <div class="max-w-7xl mx-auto px-4 py-4">
        <div class="flex items-center justify-between flex-wrap gap-3">
          <h1 class="text-2xl font-bold bg-gradient-to-r from-blue-400 to-cyan-400 bg-clip-text text-transparent">
            海岛生存规则书
          </h1>
          <div class="flex gap-2 flex-wrap">
            <button
              v-for="tab in tabs"
              :key="tab.key"
              type="button"
              @click="activeTab = tab.key"
              :class="tabButtonClass(tab.key)"
            >
              {{ tab.label }}
              <span
                v-if="tab.key === 'lore' && loreUnreadCount > 0"
                class="ml-1.5 inline-flex min-w-[16px] h-4 px-1 rounded-full bg-amber-500 text-black text-[10px] font-bold items-center justify-center"
              >
                {{ loreUnreadCount }}
              </span>
            </button>
          </div>
        </div>
      </div>
    </div>

    <div v-else class="mb-4 flex flex-wrap gap-2">
      <button
        v-for="tab in tabs"
        :key="tab.key"
        type="button"
        @click="activeTab = tab.key"
        :class="tabButtonClass(tab.key)"
      >
        {{ tab.label }}
        <span
          v-if="tab.key === 'lore' && loreUnreadCount > 0"
          class="ml-1.5 inline-flex min-w-[16px] h-4 px-1 rounded-full bg-amber-500 text-black text-[10px] font-bold items-center justify-center"
        >
          {{ loreUnreadCount }}
        </span>
      </button>
    </div>

    <div :class="embedded ? '' : 'max-w-7xl mx-auto px-4 py-8'">
      <div v-if="activeTab === 'map'" class="space-y-6">
        <div class="bg-slate-800/50 rounded-xl p-6 border border-slate-700">
          <h2 class="text-xl font-semibold mb-4 text-cyan-400">海岛小镇地图</h2>
          <div class="relative rounded-lg overflow-hidden shadow-2xl">
            <img
              :src="mapImageUrl"
              alt="海岛小镇地图"
              class="w-full h-auto max-h-[70vh] object-contain rounded-lg"
            />
          </div>
          <div class="mt-6 grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-3">
            <div v-for="location in locations" :key="location.name" class="bg-slate-700/50 rounded-lg p-3">
              <div class="flex items-center gap-2">
                <span class="text-cyan-400">{{ location.icon }}</span>
                <span class="text-sm">{{ location.name }}</span>
              </div>
              <p class="text-xs text-slate-400 mt-1">{{ location.desc }}</p>
            </div>
          </div>
        </div>
      </div>

      <div v-else-if="activeTab === 'lore'" class="space-y-6">
        <div class="bg-slate-800/50 rounded-xl p-6 border border-slate-700">
          <h2 class="text-xl font-semibold mb-2 text-cyan-400">线索文献</h2>
          <p class="text-slate-400 text-sm mb-4 leading-relaxed">
            {{ loreWarning }}
          </p>

          <div
            v-if="!isDm && loreUnreadCount > 0"
            class="mb-4 rounded-lg border border-amber-500/40 bg-amber-500/10 px-4 py-3 text-amber-200/90 text-sm"
          >
            你有 {{ loreUnreadCount }} 份新获得的线索文献。请点击「查看文献」阅读后，通知将消失。
          </div>

          <p v-if="loreLoading" class="text-slate-400 text-sm">加载中…</p>
          <p v-else-if="loreMessage" class="text-sm" :class="loreMessageOk ? 'text-emerald-400' : 'text-red-400'">
            {{ loreMessage }}
          </p>

          <div class="space-y-4">
            <div
              v-for="doc in displayedLoreDocuments"
              :key="doc.slug"
              class="bg-slate-700/50 rounded-lg p-5 border border-slate-600/50"
            >
              <div class="flex flex-wrap items-start justify-between gap-3">
                <div class="min-w-0 flex-1">
                  <h3 class="text-lg font-semibold text-blue-400">{{ doc.title }}</h3>
                  <p class="text-slate-500 text-xs mt-1 font-mono">图像文件：{{ doc.fileName }}</p>
                  <p v-if="doc.unread" class="text-amber-300 text-xs mt-2 font-medium">新文献 · 尚未阅读</p>
                  <p v-else-if="!isDm && doc.visible" class="text-emerald-400/90 text-xs mt-2">已获得查阅权限</p>
                  <p v-else-if="!isDm" class="text-slate-500 text-xs mt-2">尚未获得</p>
                  <div v-if="isDm && doc.grantedPlayers?.length" class="flex flex-wrap gap-1 mt-2">
                    <span
                      v-for="gp in doc.grantedPlayers"
                      :key="gp.playerId"
                      class="inline-flex items-center gap-1 text-[11px] bg-emerald-500/15 text-emerald-300/90 px-2 py-0.5 rounded"
                    >
                      {{ gp.playerName }}
                      <button
                        type="button"
                        class="text-red-400/80 hover:text-red-300"
                        :disabled="loreGranting === `${doc.slug}-${gp.playerId}`"
                        @click="revokeLore(doc.slug, gp.playerId)"
                      >
                        ×
                      </button>
                    </span>
                  </div>
                </div>
                <div class="flex flex-wrap gap-2 shrink-0 items-start">
                  <button
                    v-if="doc.visible"
                    type="button"
                    class="px-3 py-1.5 rounded-lg text-white text-sm"
                    :class="doc.unread ? 'bg-amber-600/90 hover:bg-amber-500' : 'bg-cyan-600/80 hover:bg-cyan-500'"
                    @click="openLoreDoc(doc)"
                  >
                    {{ doc.unread ? '查看新文献' : '查看文献' }}
                  </button>
                </div>
              </div>
              <div v-if="isDm" class="mt-4 pt-3 border-t border-slate-600/40 flex flex-wrap items-center gap-2">
                <select
                  v-model="grantPlayerBySlug[doc.slug]"
                  class="bg-slate-900/80 border border-slate-600 rounded-lg px-2 py-1.5 text-white text-sm min-w-[140px]"
                >
                  <option :value="null">选择玩家…</option>
                  <option v-for="p in allPlayers" :key="p.id" :value="p.id">{{ p.name }}</option>
                </select>
                <button
                  type="button"
                  class="px-3 py-1.5 rounded-lg bg-amber-600/80 hover:bg-amber-500 text-white text-sm disabled:opacity-50"
                  :disabled="!grantPlayerBySlug[doc.slug] || loreGranting === doc.slug"
                  @click="grantLoreToPlayer(doc.slug, grantPlayerBySlug[doc.slug])"
                >
                  {{ loreGranting === doc.slug ? '处理中…' : '给予权限' }}
                </button>
              </div>
            </div>
          </div>

          <div v-if="!loreLoading && visibleLoreCount === 0 && !isDm" class="text-center py-8 text-slate-400 text-sm">
            暂无已获得的线索文献。
          </div>
        </div>
      </div>

      <div v-else class="space-y-6">
        <div class="bg-slate-800/50 rounded-xl p-6 border border-slate-700">
          <h2 class="text-xl font-semibold mb-6 text-cyan-400">{{ currentSectionTitle }}</h2>
          <div class="space-y-4">
            <div
              v-for="rule in currentRules"
              :key="rule.id"
              class="bg-slate-700/50 rounded-lg p-5 border border-slate-600/50 hover:border-cyan-500/50 transition-all duration-200"
            >
              <h3 class="text-lg font-semibold text-blue-400 mb-2">{{ rule.title }}</h3>
              <div class="text-slate-300 leading-relaxed rule-content" v-html="renderContent(rule.content)"></div>
            </div>
          </div>
          <div v-if="currentRules.length === 0" class="text-center py-12 text-slate-400">
            <p>暂无规则内容</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, reactive, onMounted } from 'vue'
import { loreAPI, playerAPI } from '@/utils/api.js'
import { LORE_DISCOVERY_WARNING } from '@/data/loreDocuments.js'

const props = defineProps({
  embedded: { type: Boolean, default: false },
  playerFaction: { type: String, default: '' },
})

const emit = defineEmits(['lore-unread-updated'])

const activeTab = ref('map')
const rules = ref({})
const loreDocuments = ref([])
const loreLoading = ref(false)
const loreGranting = ref(null)
const loreMessage = ref('')
const loreMessageOk = ref(true)

const userRole = (localStorage.getItem('userRole') || '').toLowerCase()
const viewerPlayerId = parseInt(localStorage.getItem('playerId') || '0', 10) || null
const isDm = computed(() => userRole === 'dm')
const loreWarning = LORE_DISCOVERY_WARNING
const allPlayers = ref([])
const grantPlayerBySlug = reactive({})

const defaultRules = {
  general: [
    { id: 1, title: '游戏目标', content: '海岛生存游戏的核心目标是在严酷的环境中生存下去，并通过完成各种任务和行动来推进游戏进程。玩家需要管理自己的资源、满足每日需求，并与其他玩家互动。', orderNum: 1 },
    { id: 2, title: '每日需求', content: '每位玩家每天需要消耗一定数量的食物和燃料来维持生存。食物不足将导致虚弱状态，燃料不足将无法取暖，同样会导致虚弱。', orderNum: 2 },
    { id: 3, title: '行动系统', content: '每位玩家每天有一定的行动点数，可以用来执行各种行动。行动类型包括调查、生产、交易、移动等。', orderNum: 3 },
    { id: 4, title: '夜晚行动', content: '夜晚是危险的时刻，玩家可以选择执行夜晚行动，但也可能遭遇意外事件。夜晚行动需要谨慎选择。', orderNum: 4 },
    { id: 5, title: '虚弱状态', content: '当玩家未能满足当日需求时，次日将陷入虚弱状态。虚弱状态会影响行动效果和成功率。', orderNum: 5 }
  ],
  ruler: [
    { id: 101, title: '阵营特性', content: '统治者拥有最高的权威，可以管理避难所、安排劳工，并对反叛者实施制裁。统治者的决策影响整个海岛的命运。', orderNum: 1 },
    { id: 102, title: '劳工管理', content: '统治者可以选择NPC作为劳工参与避难所建设。劳工的效率取决于其职业和状态。', orderNum: 2 },
    { id: 103, title: '制裁权力', content: '统治者可以对反叛者实施制裁，包括限制行动、没收物资等。但过度制裁可能引起民愤。', orderNum: 3 },
    { id: 104, title: '避难所管理', content: '统治者负责避难所的建设和物资分配。合理分配资源是统治者的核心职责。', orderNum: 4 }
  ],
  rebel: [
    { id: 201, title: '阵营特性', content: '反叛者致力于推翻统治者的统治，建立新的秩序。反叛者需要秘密组织活动，避免被统治者察觉。', orderNum: 1 },
    { id: 202, title: '秘密行动', content: '反叛者可以执行秘密行动，包括破坏、暗杀、窃取情报等。成功的秘密行动可以削弱统治者的权威。', orderNum: 2 },
    { id: 203, title: '地下网络', content: '反叛者拥有自己的地下网络，可以共享资源、传递消息。网络的发展壮大是反叛成功的关键。', orderNum: 3 },
    { id: 204, title: '里程碑', content: '反叛者需要完成一系列里程碑来提升影响力。每个里程碑都会解锁新的能力和资源。', orderNum: 4 }
  ],
  adventurer: [
    { id: 301, title: '阵营特性', content: '冒险者是勇敢的探索者，探索未知、寻找宝藏是他们的天性。冒险者可以前往危险区域获取稀有资源。', orderNum: 1 },
    { id: 302, title: '方舟建设', content: '冒险者的终极目标是建造一艘方舟，带领幸存者逃离海岛。方舟需要大量资源和时间来建造。', orderNum: 2 },
    { id: 303, title: '探索行动', content: '冒险者可以探索海岛的各个角落，发现隐藏的资源和秘密。探索有风险，但回报丰厚。', orderNum: 3 },
    { id: 304, title: '特殊能力', content: '冒险者拥有独特的生存技能，可以在恶劣环境中生存，并找到其他玩家无法获取的资源。', orderNum: 4 }
  ],
  scourge: [
    { id: 401, title: '阵营特性', content: '天灾使者是神秘的存在，他们掌握着天灾的力量，可以召唤灾难、散播恐惧。', orderNum: 1 },
    { id: 402, title: '天灾卡牌', content: '天灾使者可以触发天灾卡牌，给海岛带来各种灾难。灾难可以削弱其他玩家的生存能力。', orderNum: 2 },
    { id: 403, title: '恐惧散播', content: '天灾使者的存在本身就是一种威慑。其他玩家对天灾使者既恐惧又敬畏。', orderNum: 3 },
    { id: 404, title: '黑暗仪式', content: '天灾使者可以执行黑暗仪式来增强自己的力量，但仪式需要付出代价。', orderNum: 4 }
  ],
  civilian: [
    { id: 501, title: '阵营特性', content: '平民是海岛的普通居民，他们没有特殊能力，但也不受阵营冲突的直接影响。平民需要在各方势力之间求生存。', orderNum: 1 },
    { id: 502, title: '灵活性', content: '平民可以选择支持不同的阵营，获得相应的保护和资源。明智的选择是平民生存的关键。', orderNum: 2 },
    { id: 503, title: '低调生存', content: '平民的优势在于不引人注目。保持低调可以避免成为冲突的目标。', orderNum: 3 },
    { id: 504, title: '秘密行动', content: '平民可以执行一些秘密行动来影响游戏进程，但需要谨慎行事。', orderNum: 4 }
  ]
}

const tabs = computed(() => {
  const fixedTabs = [
    { key: 'map', label: '海岛地图' },
    { key: 'lore', label: '线索文献' }
  ]
  const sectionKeys = Object.keys(rules.value).filter(k => {
    const arr = rules.value[k]
    if (!Array.isArray(arr) || arr.length === 0) return false
    // Permission filtering
    if (isDm.value) return true // DM sees everything
    // 天灾牌: only 天灾使者 can see
    if (k === '天灾牌' && props.playerFaction !== '天灾使者') return false
    // 方舟建造: only 冒险者 can see
    if (k === '方舟建造' && props.playerFaction !== '冒险者') return false
    return true
  })
  const sectionTabs = sectionKeys.map(key => ({ key, label: key }))
  return [...fixedTabs, ...sectionTabs]
})

const mapImageUrl = '/src/assets/小镇地图.png'
const locations = [
  { name: '镇长厅', icon: '🏛️', desc: '统治者办公地点' },
  { name: '警察局', icon: '🏢', desc: '维持治安' },
  { name: '教堂', icon: '⛪', desc: '宗教活动场所' },
  { name: '集市', icon: '🏪', desc: '交易中心' },
  { name: '码头', icon: '🚢', desc: '船只停靠处' },
  { name: '灯塔', icon: '🗼', desc: '指引方向' },
  { name: '矿场', icon: '⛏️', desc: '矿石开采' },
  { name: '伐木营地', icon: '🪓', desc: '木材收集' },
  { name: '猎人小屋', icon: '🏚️', desc: '山区猎人据点' },
  { name: '监狱', icon: '🏯', desc: '关押囚犯' },
  { name: '墓地', icon: '⛁', desc: '逝者安息' },
  { name: '气象台', icon: '🌤️', desc: '天气观测' }
]

function tabButtonClass(key) {
  return [
    'px-3 py-1.5 rounded-lg text-sm font-medium transition-all duration-200',
    activeTab.value === key
      ? 'bg-blue-600 text-white shadow-lg shadow-blue-600/30'
      : 'bg-slate-700/50 text-slate-400 hover:bg-slate-700 hover:text-white'
  ]
}

const currentSectionTitle = computed(() => {
  const tab = tabs.value.find(t => t.key === activeTab.value)
  return tab ? tab.label : ''
})

const currentRules = computed(() => {
  if (activeTab.value === 'map' || activeTab.value === 'lore') return []
  const sectionRules = rules.value[activeTab.value]
  if (!Array.isArray(sectionRules) || sectionRules.length === 0) {
    return defaultRules[activeTab.value] || []
  }
  // DM sees everything
  if (isDm.value) return sectionRules
  // 阵营机制: players only see their own faction's content
  if (activeTab.value === '阵营机制') {
    return sectionRules.filter(rule => {
      const title = rule.title || ''
      // Match title pattern "阵营机制-XXX" to faction
      // Note: 后端枚举用"反叛者"，规则书title用"反抗者"，需兼容
      if (title === '阵营机制-统治者' && props.playerFaction === '统治者') return true
      if ((title === '阵营机制-反抗者' || title === '阵营机制-反叛者') && props.playerFaction === '反叛者') return true
      if (title === '阵营机制-冒险者' && props.playerFaction === '冒险者') return true
      if (title === '阵营机制-天灾使者' && props.playerFaction === '天灾使者') return true
      if (title === '阵营机制-平民' && props.playerFaction === '平民') return true
      return false
    })
  }
  // 终局结算: 避难所结局结算 and 方舟结局结算 are DM-only
  if (activeTab.value === '终局结算') {
    return sectionRules.filter(rule => {
      const title = rule.title || ''
      if (title === '避难所结局结算' || title === '方舟结局结算') return false
      return true
    })
  }
  return sectionRules
})

const displayedLoreDocuments = computed(() =>
  isDm.value
    ? loreDocuments.value
    : loreDocuments.value.filter((d) => d.visible)
)

const visibleLoreCount = computed(() =>
  loreDocuments.value.filter((d) => d.visible).length
)

const loreUnreadCount = computed(() =>
  loreDocuments.value.filter((d) => d.unread).length
)

async function openLoreDoc(doc) {
  if (!isDm.value && doc.unread && viewerPlayerId) {
    await loreAPI.acknowledge(doc.slug, viewerPlayerId)
  }
  window.open(doc.path, '_blank')
  await fetchLoreCatalog()
  emit('lore-unread-updated', loreUnreadCount.value)
}

async function fetchLoreCatalog() {
  loreLoading.value = true
  try {
    const pid = isDm.value ? null : viewerPlayerId
    const data = await loreAPI.getCatalog(userRole, pid)
    if (data?.success) {
      loreDocuments.value = data.documents || []
      emit('lore-unread-updated', data.unreadCount || 0)
    }
  } catch (e) {
    console.error('Failed to fetch lore catalog:', e)
  } finally {
    loreLoading.value = false
  }
}

async function loadPlayers() {
  if (!isDm.value) return
  try {
    const list = await playerAPI.getAll()
    allPlayers.value = Array.isArray(list) ? list.filter((p) => !p.isDead) : []
  } catch {
    allPlayers.value = []
  }
}

async function grantLoreToPlayer(slug, targetPlayerId) {
  if (!targetPlayerId) return
  loreGranting.value = slug
  loreMessage.value = ''
  try {
    const data = await loreAPI.grantPlayer(slug, targetPlayerId, userRole)
    if (data?.success) {
      loreMessageOk.value = true
      loreMessage.value = data.message || '已授予权限'
      loreDocuments.value = data.documents || []
      grantPlayerBySlug[slug] = null
    } else {
      loreMessageOk.value = false
      loreMessage.value = data?.message || '操作失败'
    }
  } catch {
    loreMessageOk.value = false
    loreMessage.value = '操作失败'
  } finally {
    loreGranting.value = null
  }
}

async function revokeLore(slug, targetPlayerId) {
  loreGranting.value = `${slug}-${targetPlayerId}`
  loreMessage.value = ''
  try {
    const data = await loreAPI.revokePlayer(slug, targetPlayerId, userRole)
    if (data?.success) {
      loreMessageOk.value = true
      loreMessage.value = data.message || '已撤销'
      loreDocuments.value = data.documents || []
    } else {
      loreMessageOk.value = false
      loreMessage.value = data?.message || '操作失败'
    }
  } catch {
    loreMessageOk.value = false
    loreMessage.value = '操作失败'
  } finally {
    loreGranting.value = null
  }
}

function renderContent(content) {
  if (!content) return ''
  let html = content
  // Escape HTML special chars first (but preserve \r\n)
  html = html.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
  // Convert \r\n or \n to line breaks
  html = html.replace(/\r\n/g, '\n')
  // Split into lines and process
  const lines = html.split('\n')
  const result = []
  let inList = false
  for (const line of lines) {
    const trimmed = line.trim()
    if (!trimmed) {
      if (inList) { result.push('</ul>'); inList = false }
      result.push('<br>')
      continue
    }
    // Heading-like lines (short lines ending with colon or starting with numbers like "一、")
    if (/^[一二三四五六七八九十]+[、．.]/.test(trimmed) || /^[第][一二三四五六七八九十]+[章阶段]/.test(trimmed)) {
      if (inList) { result.push('</ul>'); inList = false }
      result.push(`<h4 class="text-cyan-300 font-semibold mt-3 mb-1">${trimmed}</h4>`)
    } else if (/^(\d+)[\.、）)]\s*/.test(trimmed)) {
      // Numbered list items
      if (!inList) { result.push('<ul class="list-decimal list-inside space-y-1 ml-2">'); inList = true }
      const itemText = trimmed.replace(/^(\d+)[\.、）)]\s*/, '')
      result.push(`<li>${itemText}</li>`)
    } else if (/^[·\-—–•]\s*/.test(trimmed)) {
      // Bullet list items
      if (inList) { result.push('</ul>'); inList = false }
      result.push(`<div class="ml-4 flex gap-2"><span class="text-cyan-400 shrink-0">•</span><span>${trimmed.replace(/^[·\-—–•]\s*/, '')}</span></div>`)
    } else if (/^[【《]/.test(trimmed) || /】$/.test(trimmed) || /^【.+】$/.test(trimmed)) {
      // Special terms with brackets - outcome titles like 【诺亚方舟】
      if (inList) { result.push('</ul>'); inList = false }
      result.push(`<div class="mt-3 mb-1 text-amber-300 font-semibold text-base">${trimmed}</div>`)
    } else {
      if (inList) { result.push('</ul>'); inList = false }
      result.push(`<p class="mb-1">${trimmed}</p>`)
    }
  }
  if (inList) result.push('</ul>')
  return result.join('')
}

const fetchRules = async () => {
  try {
    const response = await fetch('/api/rule-book/all')
    const data = await response.json()
    if (data.success) {
      rules.value = data.data
    }
  } catch (error) {
    console.error('Failed to fetch rules:', error)
    rules.value = defaultRules
  }
}

onMounted(() => {
  rules.value = defaultRules
  fetchRules()
  loadPlayers()
  fetchLoreCatalog()
})
</script>

<style scoped>
.rule-content :deep(h4) {
  font-size: 0.95rem;
  letter-spacing: 0.02em;
}
.rule-content :deep(ul) {
  padding-left: 0.5rem;
}
.rule-content :deep(li) {
  margin-bottom: 0.25rem;
  line-height: 1.6;
}
.rule-content :deep(p) {
  line-height: 1.7;
}
</style>
