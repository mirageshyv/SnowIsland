<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { factionActionAPI, locationAPI, playerAPI } from '@/utils/api.js'
import {
  FACTION_LABELS,
  FACTION_ACTION_DEFS,
  GM_FACTION_TABS,
  ASSIGNED_FREE_ACTIONS,
  INVESTIGATE_TYPES,
} from '@/data/factionActions.js'

const playerId = parseInt(localStorage.getItem('playerId') || '0')
const userRole = (localStorage.getItem('userRole') || '').toLowerCase()
const isGmView = computed(() => userRole === 'dm')

const gameDay = ref(1)
const viewFaction = ref('')
const context = ref(null)
const locations = ref([])
const loading = ref(true)
const submitting = ref(false)
const submitMessage = ref(null)
const showActionHelpModal = ref(false)

const selectedType = ref('')
const actionResult = ref('')

const forms = reactive({
  govern_location: { actorId: '', targetLocationId: '', note: '' },
  assign_personnel: { targetId: '', targetKind: 'player', assignedAction: '', targetLocationId: '', note: '' },
  assign_guard: { actorId: '', targetLocationId: '', armed: true },
  exploit_labor: { targetIds: [] },
  extra_labor: { note: '' },
  secret_contact: { targetPlayerId: '', message: '', anonymous: false },
  group_discussion: { secretLocationId: '', notifyPlayerIds: [], note: '' },
  sabotage: { targetLocationId: '', facilityId: '' },
  extra_investigate: { investigateType: 'investigate_player', targetId: '' },
  guard_ark: { guardId: '', useWeaponOrSkill: false },
  ark_construction: { actionPoints: 1, useSpecialMaterials: false, note: '' },
  curse: { weaponId: '', target1: '', target2: '' },
})

const playerFaction = computed(() => context.value?.faction || '')
const displayFaction = computed(() => (isGmView.value && viewFaction.value) ? viewFaction.value : playerFaction.value)
const actionDefs = computed(() => FACTION_ACTION_DEFS[displayFaction.value] || [])
const factionMeta = computed(() => FACTION_LABELS[displayFaction.value] || { label: '未知', color: '' })

const locationOptions = computed(() =>
  (locations.value || []).map(l => ({ value: l.id, label: `${l.name}（${l.area || ''}）` }))
)

const allPlayers = computed(() => context.value?.allPlayers || [])
const militiaPlayers = computed(() => context.value?.militiaPlayers || [])
const laborCandidates = computed(() => {
  const list = context.value?.laborCandidates || []
  if (list.length > 0) return list
  return allPlayers.value.filter(p => p.isOverworked)
})

const governedIds = computed(() => new Set(context.value?.governedLocationIds || []))
const usedTypes = computed(() => new Set(context.value?.usedActionTypes || []))
const unlimitedActions = computed(() => !!context.value?.unlimitedActions)
const hasSubmittedToday = computed(() => !!context.value?.hasSubmittedToday)
const usedPlayerNpcKeys = computed(() => new Set(context.value?.usedPlayerNpcKeys || []))
const usedLaborerIds = computed(() => new Set(context.value?.usedLaborerIds || []))
const exploitLaborUsedToday = computed(() => !!context.value?.exploitLaborUsedToday)

const npcOptions = computed(() => {
  const out = []
  for (const loc of locations.value || []) {
    for (const n of loc.npcs || []) {
      out.push({ value: `npc:${n.id}`, id: n.id, kind: 'npc', label: `${n.name}（${n.job}）@ ${loc.name}` })
    }
  }
  return out
})

const personnelTargets = computed(() => [
  ...allPlayers.value.map(p => ({ value: `player:${p.id}`, id: p.id, kind: 'player', label: p.name })),
  ...npcOptions.value,
])

const factionInvestigateTargetOptions = computed(() => {
  const type = forms.extra_investigate.investigateType
  if (type === 'investigate_location') {
    return locationOptions.value
  }
  return allPlayers.value
    .filter(p => p.id !== playerId)
    .map(p => ({ value: p.id, label: p.name }))
})

const arkProgress = computed(() => {
  const ark = context.value?.arkStatus
  if (!ark) return '—'
  return ark.completionPercentage != null ? `${ark.completionPercentage}%` : '—'
})

const facilitiesForSabotage = computed(() => {
  const locId = parseInt(forms.sabotage.targetLocationId)
  if (!locId) return []
  const loc = (locations.value || []).find(l => l.id === locId)
  return (loc?.facilities || []).map(f => ({ value: f.id, label: f.name }))
})

const selectedDef = computed(() =>
  actionDefs.value.find(d => d.type === selectedType.value) || null
)

const actionTypeOptions = computed(() =>
  actionDefs.value.map(def => {
    const st = getActionStatus(def.type)
    return {
      value: def.type,
      label: def.title,
      disabled: st.btn === 'disabled',
      statusLabel: st.label,
    }
  })
)

const currentStatus = computed(() =>
  selectedType.value ? getActionStatus(selectedType.value) : null
)

const canSubmit = computed(() =>
  selectedType.value &&
  currentStatus.value?.btn === 'enabled' &&
  !isGmView.value &&
  !submitting.value
)

function isPersonnelTargetUsed(kind, id) {
  return usedPlayerNpcKeys.value.has(`${kind}:${id}`)
}

function isLaborerUsed(id) {
  return usedLaborerIds.value.has(id)
}

function getActionStatus(type) {
  if (!type) return { key: 'none', label: '', btn: 'disabled' }
  if (!unlimitedActions.value && hasSubmittedToday.value) {
    return { key: 'quota', label: '今日已提交', btn: 'disabled' }
  }
  if (unlimitedActions.value && type === 'exploit_labor' && exploitLaborUsedToday.value) {
    return { key: 'used', label: '今日已使用', btn: 'disabled' }
  }
  if (type === 'extra_labor' && !context.value?.hasProduceToday) {
    return { key: 'blocked', label: '今日未生产', btn: 'disabled' }
  }
  if (type === 'sabotage') {
    const locId = parseInt(forms.sabotage.targetLocationId)
    if (locId && governedIds.value.has(locId)) {
      return { key: 'blocked', label: '地点被监管', btn: 'disabled' }
    }
  }
  if (type === 'curse') {
    const weapons = context.value?.highThreatWeapons || []
    if (!weapons.length) return { key: 'blocked', label: '无威胁≥4武器', btn: 'disabled' }
  }
  return { key: 'ready', label: '可执行', btn: 'enabled' }
}

function resetFormFields(type) {
  const defaults = {
    govern_location: { actorId: '', targetLocationId: '', note: '' },
    assign_personnel: { targetId: '', targetKind: 'player', assignedAction: '', targetLocationId: '', note: '' },
    assign_guard: { actorId: '', targetLocationId: '', armed: true },
    exploit_labor: { targetIds: [] },
    extra_labor: { note: '' },
    secret_contact: { targetPlayerId: '', message: '', anonymous: false },
    group_discussion: { secretLocationId: '', notifyPlayerIds: [], note: '' },
    sabotage: { targetLocationId: '', facilityId: '' },
    extra_investigate: { investigateType: 'investigate_player', targetId: '' },
    guard_ark: { guardId: '', useWeaponOrSkill: false },
    ark_construction: { actionPoints: 1, useSpecialMaterials: false, note: '' },
    curse: { weaponId: '', target1: '', target2: '' },
  }
  if (type && defaults[type]) {
    Object.assign(forms[type], defaults[type])
  }
}

watch(selectedType, (type, prev) => {
  if (prev) resetFormFields(prev)
  actionResult.value = ''
})

watch(() => forms.sabotage.targetLocationId, () => {
  forms.sabotage.facilityId = ''
})

watch(() => forms.extra_investigate.investigateType, () => {
  forms.extra_investigate.targetId = ''
})

async function loadContext() {
  loading.value = true
  try {
    let pid = playerId
    if (isGmView.value && (!pid || isNaN(pid))) {
      const players = await playerAPI.getAll()
      const list = Array.isArray(players) ? players : []
      pid = list[0]?.id || 1
    }
    const ctx = await factionActionAPI.getContext(pid, gameDay.value)
    context.value = ctx
    if (!isGmView.value && ctx?.faction) {
      viewFaction.value = ctx.faction
    } else if (isGmView.value && !viewFaction.value) {
      viewFaction.value = GM_FACTION_TABS[0]
    }
    if (selectedType.value) {
      const st = getActionStatus(selectedType.value)
      if (st.btn === 'disabled') {
        selectedType.value = ''
        actionResult.value = ''
      }
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

async function loadLocations() {
  try {
    const res = await locationAPI.getAll()
    locations.value = Array.isArray(res) ? res : []
  } catch (e) {
    locations.value = []
  }
}

watch(gameDay, () => {
  selectedType.value = ''
  actionResult.value = ''
  loadContext()
})

watch(viewFaction, () => {
  selectedType.value = ''
  actionResult.value = ''
})

function buildPayload(type) {
  const f = forms[type]
  switch (type) {
    case 'govern_location':
      return { actorId: parseInt(f.actorId), targetLocationId: parseInt(f.targetLocationId), note: f.note || '' }
    case 'assign_personnel': {
      const raw = f.targetId
      const [kind, id] = raw.includes(':') ? raw.split(':') : ['player', raw]
      return {
        targetId: parseInt(id),
        targetKind: kind,
        assignedAction: f.assignedAction,
        targetLocationId: f.targetLocationId ? parseInt(f.targetLocationId) : null,
        note: f.note || '',
      }
    }
    case 'assign_guard':
      return { actorId: parseInt(f.actorId), targetLocationId: parseInt(f.targetLocationId), armed: f.armed }
    case 'exploit_labor':
      return { targetIds: [...f.targetIds].map(Number) }
    case 'extra_labor':
      return { note: f.note || '' }
    case 'secret_contact':
      return { targetPlayerId: parseInt(f.targetPlayerId), message: f.message, anonymous: f.anonymous }
    case 'group_discussion':
      return {
        secretLocationId: parseInt(f.secretLocationId),
        notifyPlayerIds: f.notifyPlayerIds.map(Number),
        note: f.note || '',
      }
    case 'sabotage':
      return { targetLocationId: parseInt(f.targetLocationId), facilityId: parseInt(f.facilityId) }
    case 'extra_investigate':
      return {
        investigateType: f.investigateType,
        targetId: parseInt(f.targetId),
      }
    case 'guard_ark':
      return { guardId: parseInt(f.guardId), useWeaponOrSkill: f.useWeaponOrSkill }
    case 'ark_construction':
      return {
        actionPoints: parseInt(f.actionPoints) || 1,
        useSpecialMaterials: f.useSpecialMaterials,
        note: f.note || '',
      }
    case 'curse':
      return {
        weaponId: parseInt(f.weaponId),
        target1: parseInt(f.target1),
        target2: f.target2 ? parseInt(f.target2) : null,
      }
    default:
      return {}
  }
}

function validateClient(type) {
  const f = forms[type]
  switch (type) {
    case 'govern_location':
      if (!f.actorId || !f.targetLocationId) return '请选择监管人员与地点'
      break
    case 'assign_personnel':
      if (!f.targetId || !f.assignedAction) return '请选择目标与指定行动'
      break
    case 'assign_guard':
      if (!f.actorId || !f.targetLocationId) return '请选择看守人员与地点'
      break
    case 'exploit_labor':
      if (!f.targetIds.length) return '请至少选择一名劳工'
      break
    case 'secret_contact':
      if (!f.targetPlayerId || !f.message?.trim()) return '请选择目标并填写秘密信息'
      break
    case 'group_discussion':
      if (!f.secretLocationId || !f.notifyPlayerIds.length) return '请选择秘密地点与通知玩家'
      break
    case 'sabotage':
      if (!f.targetLocationId || !f.facilityId) return '请选择地点与设施'
      break
    case 'extra_investigate':
      if (!f.targetId) return '请选择调查目标'
      break
    case 'guard_ark':
      if (!f.guardId) return '请选择看守人员'
      break
    case 'ark_construction':
      if (!f.actionPoints || f.actionPoints < 1) return '请填写消耗行动点'
      break
    case 'curse':
      if (!f.weaponId || !f.target1) return '请选择武器与目标'
      break
    default:
      break
  }
  return null
}

async function submitAction() {
  if (!selectedType.value) {
    alert('请选择阵营行动')
    return
  }
  const err = validateClient(selectedType.value)
  if (err) {
    alert(err)
    return
  }
  const st = getActionStatus(selectedType.value)
  if (st.btn === 'disabled') return

  submitting.value = true
  submitMessage.value = null
  try {
    const res = await factionActionAPI.submitAction({
      playerId,
      actionType: selectedType.value,
      gameDay: gameDay.value,
      payload: buildPayload(selectedType.value),
    })
    if (res?.success) {
      actionResult.value = res.data?.result || '已提交'
      submitMessage.value = { type: 'success', text: '阵营行动提交成功' }
      selectedType.value = ''
      await loadContext()
    } else {
      actionResult.value = '提交失败：' + (res?.message || '未知错误')
      submitMessage.value = { type: 'error', text: res?.message || '提交失败' }
    }
  } catch (e) {
    submitMessage.value = { type: 'error', text: '提交失败，请重试' }
  } finally {
    submitting.value = false
    if (submitMessage.value) {
      setTimeout(() => { submitMessage.value = null }, 3000)
    }
  }
}

function toggleLabor(id) {
  if (isLaborerUsed(id)) return
  const ids = forms.exploit_labor.targetIds
  const i = ids.indexOf(id)
  if (i >= 0) ids.splice(i, 1)
  else if (ids.length < 3) ids.push(id)
}

function toggleNotify(id) {
  const ids = forms.group_discussion.notifyPlayerIds
  const i = ids.indexOf(id)
  if (i >= 0) ids.splice(i, 1)
  else ids.push(id)
}

const guardDefensePreview = computed(() => {
  const actorId = parseInt(forms.assign_guard.actorId)
  if (!actorId || selectedType.value !== 'assign_guard') return null
  const base = 3
  let bonus = 0
  for (const w of context.value?.highThreatWeapons || []) {
    if (w.threatLevel > bonus) bonus = w.threatLevel
  }
  return { base, bonus, total: base + bonus }
})

const selectClass = 'w-full appearance-none bg-white/5 border border-white/10 rounded-xl px-4 py-3 pr-10 text-gray-200 text-sm focus:outline-none focus:border-blue-500/50'
const textareaClass = 'w-full resize-none bg-white/5 border border-white/10 rounded-xl px-4 py-3 text-gray-200 text-sm placeholder:text-gray-600 focus:outline-none focus:border-blue-500/50'

onMounted(async () => {
  await loadLocations()
  await loadContext()
})
</script>

<template>
  <div class="min-h-screen bg-[#0a0e1a] py-8 px-4 md:px-8">
    <div class="max-w-6xl mx-auto">
      <div class="text-center mb-10">
        <h1 class="text-white text-2xl md:text-3xl font-semibold tracking-tight mb-2">阵营行动</h1>
        <p class="text-gray-500 text-sm">选择阵营行动并提交</p>
        <div class="mt-3 flex items-center justify-center gap-2">
          <label class="text-gray-400 text-sm">当前天数：</label>
          <select
            v-model.number="gameDay"
            class="bg-black/30 border border-white/10 rounded-lg px-3 py-1 text-sm text-gray-200 focus:outline-none"
          >
            <option :value="1">第1天</option>
            <option :value="2">第2天</option>
            <option :value="3">第3天</option>
          </select>
        </div>
      </div>

      <div v-if="isGmView" class="mb-6 flex flex-wrap justify-center gap-2">
        <button
          v-for="f in GM_FACTION_TABS"
          :key="f"
          type="button"
          class="px-4 py-2 rounded-xl text-sm font-medium border transition-colors"
          :class="viewFaction === f ? 'bg-blue-600/30 border-blue-500/50 text-blue-200' : 'bg-white/5 border-white/10 text-gray-400 hover:text-white'"
          @click="viewFaction = f"
        >
          {{ FACTION_LABELS[f]?.label || f }}
        </button>
      </div>

      <div v-if="loading" class="flex justify-center py-20">
        <div class="w-12 h-12 border-4 border-blue-500 border-t-transparent rounded-full animate-spin" />
      </div>

      <div v-else-if="!isGmView && context && !context.success" class="text-center py-16">
        <p class="text-gray-400">{{ context.message || '暂无可用阵营行动。' }}</p>
      </div>

      <template v-else>

        <div class="max-w-3xl mx-auto mb-10">
          <div class="relative bg-gradient-to-br from-[#1a2332] to-[#0f1419] border border-white/10 rounded-3xl p-6 overflow-hidden">
            <div class="absolute top-0 right-0 w-48 h-48 bg-blue-500/5 rounded-full blur-3xl" />
            <div class="relative space-y-4">
              <div class="flex items-center gap-3">
                <span class="inline-flex items-center justify-center w-9 h-9 rounded-lg bg-white/10 border border-white/10 text-gray-300 text-sm font-medium">⚔</span>
                <h2 class="text-white text-lg tracking-tight">阵营行动提交</h2>
                <span
                  v-if="currentStatus"
                  class="ml-auto text-xs px-2 py-0.5 rounded-full"
                  :class="{
                    'bg-green-500/20 text-green-400': currentStatus.key === 'ready',
                    'bg-gray-500/20 text-gray-400': currentStatus.key === 'used',
                    'bg-amber-500/20 text-amber-400': currentStatus.key === 'quota',
                    'bg-red-500/20 text-red-400': currentStatus.key === 'blocked',
                  }"
                >
                  {{ currentStatus.label }}
                </span>
              </div>

              <div>
                <div class="flex items-center gap-2 mb-2 ml-0.5">
                  <label class="text-gray-500 text-xs">选择行动</label>
                  <button
                    type="button"
                    class="inline-flex h-5 w-5 items-center justify-center rounded-full border border-white/20 bg-white/5 text-gray-400 hover:text-white text-xs font-semibold"
                    @click="showActionHelpModal = true"
                  >?</button>
                </div>
                <select v-model="selectedType" :class="selectClass" :disabled="isGmView">
                  <option value="">请选择行动</option>
                  <option
                    v-for="opt in actionTypeOptions"
                    :key="opt.value"
                    :value="opt.value"
                    :disabled="opt.disabled"
                  >
                    {{ opt.label }}{{ opt.disabled ? `（${opt.statusLabel}）` : '' }}
                  </option>
                </select>
              </div>

              <div v-if="selectedDef" class="rounded-xl border border-white/10 bg-black/20 px-4 py-3">
                <p class="text-gray-300 text-sm">{{ selectedDef.description }}</p>
              </div>

              <!-- 安排监管 -->
              <template v-if="selectedType === 'govern_location'">
                <div>
                  <label class="block text-gray-500 text-xs mb-2 ml-0.5">执行人员（民兵/治安官）</label>
                  <select v-model="forms.govern_location.actorId" :class="selectClass">
                    <option value="">请选择</option>
                    <option
                      v-for="p in militiaPlayers"
                      :key="p.id"
                      :value="p.id"
                      :disabled="isPersonnelTargetUsed('player', p.id)"
                    >
                      {{ p.name }}（{{ p.job }}）{{ isPersonnelTargetUsed('player', p.id) ? '（今日已用）' : '' }}
                    </option>
                  </select>
                </div>
                <div>
                  <label class="block text-gray-500 text-xs mb-2 ml-0.5">监管地点</label>
                  <select v-model="forms.govern_location.targetLocationId" :class="selectClass">
                    <option value="">请选择地点</option>
                    <option
                      v-for="o in locationOptions"
                      :key="o.value"
                      :value="o.value"
                      :disabled="governedIds.has(o.value)"
                    >
                      {{ o.label }}{{ governedIds.has(o.value) ? '（已监管）' : '' }}
                    </option>
                  </select>
                </div>
                <div>
                  <label class="block text-gray-500 text-xs mb-2 ml-0.5">备注（可选）</label>
                  <textarea v-model="forms.govern_location.note" rows="2" :class="textareaClass" placeholder="主持人备注..." />
                </div>
              </template>

              <!-- 安排人员 -->
              <template v-else-if="selectedType === 'assign_personnel'">
                <div>
                  <label class="block text-gray-500 text-xs mb-2 ml-0.5">目标玩家/NPC</label>
                  <select v-model="forms.assign_personnel.targetId" :class="selectClass">
                    <option value="">请选择</option>
                    <option
                      v-for="t in personnelTargets"
                      :key="t.value"
                      :value="t.value"
                      :disabled="isPersonnelTargetUsed(t.kind, t.id)"
                    >
                      {{ t.label }}{{ isPersonnelTargetUsed(t.kind, t.id) ? '（今日已用）' : '' }}
                    </option>
                  </select>
                </div>
                <div>
                  <label class="block text-gray-500 text-xs mb-2 ml-0.5">指定自由行动</label>
                  <select v-model="forms.assign_personnel.assignedAction" :class="selectClass">
                    <option value="">请选择</option>
                    <option v-for="a in ASSIGNED_FREE_ACTIONS" :key="a.value" :value="a.value">{{ a.label }}</option>
                  </select>
                </div>
                <div v-if="['go_location', 'investigate_location', 'guard'].includes(forms.assign_personnel.assignedAction)">
                  <label class="block text-gray-500 text-xs mb-2 ml-0.5">地点（如需要）</label>
                  <select v-model="forms.assign_personnel.targetLocationId" :class="selectClass">
                    <option value="">请选择地点</option>
                    <option v-for="o in locationOptions" :key="o.value" :value="o.value">{{ o.label }}</option>
                  </select>
                </div>
                <div>
                  <label class="block text-gray-500 text-xs mb-2 ml-0.5">附加说明</label>
                  <textarea v-model="forms.assign_personnel.note" rows="2" :class="textareaClass" />
                </div>
              </template>

              <!-- 安排看守 -->
              <template v-else-if="selectedType === 'assign_guard'">
                <div>
                  <label class="block text-gray-500 text-xs mb-2 ml-0.5">看守人员</label>
                  <select v-model="forms.assign_guard.actorId" :class="selectClass">
                    <option value="">请选择</option>
                    <option
                      v-for="p in allPlayers"
                      :key="p.id"
                      :value="p.id"
                      :disabled="isPersonnelTargetUsed('player', p.id)"
                    >
                      {{ p.name }}{{ isPersonnelTargetUsed('player', p.id) ? '（今日已用）' : '' }}
                    </option>
                  </select>
                </div>
                <div>
                  <label class="block text-gray-500 text-xs mb-2 ml-0.5">看守地点</label>
                  <select v-model="forms.assign_guard.targetLocationId" :class="selectClass">
                    <option value="">请选择地点</option>
                    <option v-for="o in locationOptions" :key="o.value" :value="o.value">{{ o.label }}</option>
                  </select>
                </div>
                <label class="flex items-center gap-2 text-gray-400 text-sm">
                  <input v-model="forms.assign_guard.armed" type="checkbox" class="rounded" />
                  计入装备武器（自动读取）
                </label>
                <div v-if="guardDefensePreview" class="rounded-xl border border-blue-500/20 bg-blue-500/5 px-4 py-3 text-sm text-gray-300 space-y-0.5">
                  <p>基础防御 +{{ guardDefensePreview.base }}</p>
                  <p>武器防御 +{{ guardDefensePreview.bonus }}</p>
                  <p class="text-white font-medium">总防御值：{{ guardDefensePreview.total }}</p>
                </div>
              </template>

              <!-- 压榨劳工 -->
              <template v-else-if="selectedType === 'exploit_labor'">
                <p class="text-amber-300/90 text-xs">效果：建造×2、受伤、无法生产、格斗失效</p>
                <label class="block text-gray-500 text-xs mb-2 mt-2">目标劳工（最多3人）</label>
                <div class="flex flex-wrap gap-2">
                  <button
                    v-for="p in laborCandidates.length ? laborCandidates : allPlayers"
                    :key="p.id"
                    type="button"
                    class="px-3 py-1.5 rounded-lg text-xs border transition-colors"
                    :class="[
                      forms.exploit_labor.targetIds.includes(p.id)
                        ? 'bg-amber-500/20 border-amber-500/40 text-amber-200'
                        : 'bg-white/5 border-white/10 text-gray-400',
                      isLaborerUsed(p.id) ? 'opacity-40 cursor-not-allowed' : '',
                    ]"
                    :disabled="isLaborerUsed(p.id)"
                    @click="toggleLabor(p.id)"
                  >
                    {{ p.name }}{{ isLaborerUsed(p.id) ? '（今日已压榨）' : '' }}
                  </button>
                </div>
              </template>

              <!-- 额外劳动 -->
              <template v-else-if="selectedType === 'extra_labor'">
                <p v-if="!context?.hasProduceToday" class="text-red-400 text-sm">须今日已提交生产类自由行动。</p>
                <p v-else class="text-cyan-300/90 text-sm">结算后今日生产产出 +50%。</p>
                <div class="mt-2">
                  <label class="block text-gray-500 text-xs mb-2 ml-0.5">备注（可选）</label>
                  <textarea v-model="forms.extra_labor.note" rows="2" :class="textareaClass" />
                </div>
              </template>

              <!-- 暗中联络 -->
              <template v-else-if="selectedType === 'secret_contact'">
                <div>
                  <label class="block text-gray-500 text-xs mb-2 ml-0.5">目标玩家</label>
                  <select v-model="forms.secret_contact.targetPlayerId" :class="selectClass">
                    <option value="">请选择</option>
                    <option v-for="p in allPlayers.filter(x => x.id !== playerId)" :key="p.id" :value="p.id">{{ p.name }}</option>
                  </select>
                </div>
                <div>
                  <label class="block text-gray-500 text-xs mb-2 ml-0.5">秘密信息</label>
                  <textarea v-model="forms.secret_contact.message" rows="3" :class="textareaClass" placeholder="输入秘密信息..." />
                </div>
                <label class="flex items-center gap-2 text-gray-400 text-sm">
                  <input v-model="forms.secret_contact.anonymous" type="checkbox" class="rounded" />
                  匿名发送
                </label>
              </template>

              <!-- 群组讨论 -->
              <template v-else-if="selectedType === 'group_discussion'">
                <div>
                  <label class="block text-gray-500 text-xs mb-2 ml-0.5">秘密地点</label>
                  <select v-model="forms.group_discussion.secretLocationId" :class="selectClass">
                    <option value="">请选择地点</option>
                    <option v-for="o in locationOptions" :key="o.value" :value="o.value">{{ o.label }}</option>
                  </select>
                </div>
                <div>
                  <label class="block text-gray-500 text-xs mb-2 ml-0.5">通知玩家</label>
                  <div class="flex flex-wrap gap-2">
                    <button
                      v-for="p in allPlayers.filter(x => x.id !== playerId)"
                      :key="p.id"
                      type="button"
                      class="px-3 py-1.5 rounded-lg text-xs border transition-colors"
                      :class="forms.group_discussion.notifyPlayerIds.includes(p.id)
                        ? 'bg-purple-500/20 border-purple-500/40 text-purple-200'
                        : 'bg-white/5 border-white/10 text-gray-400'"
                      @click="toggleNotify(p.id)"
                    >
                      {{ p.name }}
                    </button>
                  </div>
                </div>
                <textarea v-model="forms.group_discussion.note" rows="2" :class="textareaClass" placeholder="讨论内容（可选）" />
              </template>

              <!-- 破坏 -->
              <template v-else-if="selectedType === 'sabotage'">
                <div>
                  <label class="block text-gray-500 text-xs mb-2 ml-0.5">目标地点</label>
                  <select v-model="forms.sabotage.targetLocationId" :class="selectClass">
                    <option value="">请选择地点</option>
                    <option
                      v-for="o in locationOptions"
                      :key="o.value"
                      :value="o.value"
                      :disabled="governedIds.has(o.value)"
                    >
                      {{ o.label }}{{ governedIds.has(o.value) ? '（不可选）' : '' }}
                    </option>
                  </select>
                </div>
                <div>
                  <label class="block text-gray-500 text-xs mb-2 ml-0.5">目标设施</label>
                  <select v-model="forms.sabotage.facilityId" :class="selectClass" :disabled="!facilitiesForSabotage.length">
                    <option value="">请选择设施</option>
                    <option v-for="f in facilitiesForSabotage" :key="f.value" :value="f.value">{{ f.label }}</option>
                  </select>
                </div>
              </template>

              <!-- 额外调查 -->
              <template v-else-if="selectedType === 'extra_investigate'">
                <p class="text-amber-300/90 text-xs mb-3">
                  与「行动提交」调查相同：选择类型与目标。结算后该次调查数量翻倍。
                </p>
                <div class="space-y-4">
                  <div>
                    <label class="block text-gray-500 text-xs mb-2 ml-0.5">调查类型</label>
                    <select v-model="forms.extra_investigate.investigateType" :class="selectClass">
                      <option v-for="t in INVESTIGATE_TYPES" :key="t.value" :value="t.value">{{ t.label }}</option>
                    </select>
                  </div>
                  <div>
                    <label class="block text-gray-500 text-xs mb-2 ml-0.5">调查目标</label>
                    <select v-model="forms.extra_investigate.targetId" :class="selectClass">
                      <option value="">请选择</option>
                      <option
                        v-for="o in factionInvestigateTargetOptions"
                        :key="'inv-'+o.value"
                        :value="o.value"
                      >
                        {{ o.label }}
                      </option>
                    </select>
                  </div>
                </div>
              </template>

              <!-- 看守方舟 -->
              <template v-else-if="selectedType === 'guard_ark'">
                <div>
                  <label class="block text-gray-500 text-xs mb-2 ml-0.5">看守人员</label>
                  <select v-model="forms.guard_ark.guardId" :class="selectClass">
                    <option value="">请选择</option>
                    <option v-for="p in allPlayers" :key="p.id" :value="p.id">{{ p.name }}</option>
                  </select>
                </div>
                <label class="flex items-center gap-2 text-gray-400 text-sm">
                  <input v-model="forms.guard_ark.useWeaponOrSkill" type="checkbox" class="rounded" />
                  使用武器/技能计入防御
                </label>
              </template>

              <!-- 方舟建设 -->
              <template v-else-if="selectedType === 'ark_construction'">
                <p class="text-cyan-300 text-sm mb-2">当前方舟进度：{{ arkProgress }}</p>
                <div>
                  <label class="block text-gray-500 text-xs mb-2 ml-0.5">消耗行动点</label>
                  <input v-model.number="forms.ark_construction.actionPoints" type="number" min="1" max="2" :class="selectClass + ' w-32'" />
                </div>
                <label class="flex items-center gap-2 text-gray-400 text-sm">
                  <input v-model="forms.ark_construction.useSpecialMaterials" type="checkbox" class="rounded" />
                  使用特殊材料
                </label>
                <textarea v-model="forms.ark_construction.note" rows="2" :class="textareaClass" placeholder="备注（可选）" />
              </template>

              <!-- 诅咒 -->
              <template v-else-if="selectedType === 'curse'">
                <p class="text-gray-400 text-xs mb-2">效果：获知阵营、施加诅咒。武器威胁值须≥4。</p>
                <div>
                  <label class="block text-gray-500 text-xs mb-2 ml-0.5">消耗武器（威胁≥4）</label>
                  <select v-model="forms.curse.weaponId" :class="selectClass">
                    <option value="">请选择</option>
                    <option v-for="w in (context?.highThreatWeapons || [])" :key="w.weaponId" :value="w.weaponId">
                      {{ w.name }}（威胁{{ w.threatLevel }}）
                    </option>
                  </select>
                </div>
                <div>
                  <label class="block text-gray-500 text-xs mb-2 ml-0.5">诅咒目标1</label>
                  <select v-model="forms.curse.target1" :class="selectClass">
                    <option value="">请选择</option>
                    <option v-for="p in allPlayers.filter(x => x.id !== playerId)" :key="p.id" :value="p.id">{{ p.name }}</option>
                  </select>
                </div>
                <div>
                  <label class="block text-gray-500 text-xs mb-2 ml-0.5">诅咒目标2（可选）</label>
                  <select v-model="forms.curse.target2" :class="selectClass">
                    <option value="">不选</option>
                    <option v-for="p in allPlayers.filter(x => x.id !== playerId)" :key="'c2-'+p.id" :value="p.id">{{ p.name }}</option>
                  </select>
                </div>
              </template>

              <div>
                <label class="block text-gray-500 text-xs mb-2 ml-0.5">行动结果</label>
                <div class="min-h-[5.5rem] rounded-xl border border-white/10 bg-black/25 px-4 py-3 text-sm text-gray-400 whitespace-pre-wrap">
                  {{ actionResult || '结果将在此显示' }}
                </div>
              </div>
            </div>
          </div>
        </div>

        <div v-if="submitMessage" class="flex justify-center mb-4">
          <div
            :class="['px-5 py-2.5 rounded-xl text-sm font-medium', submitMessage.type === 'success' ? 'bg-green-500/20 border border-green-500/30 text-green-400' : 'bg-red-500/20 border border-red-500/30 text-red-400']"
          >
            {{ submitMessage.text }}
          </div>
        </div>

        <div class="flex justify-center pb-4">
          <button
            type="button"
            :disabled="!canSubmit"
            class="min-w-[200px] bg-gradient-to-r from-blue-500 to-blue-600 hover:from-blue-600 hover:to-blue-700 disabled:from-gray-600 disabled:to-gray-600 text-white px-8 py-3 rounded-xl text-sm font-medium shadow-lg shadow-blue-500/30 transition-all"
            @click="submitAction"
          >
            {{ submitting ? '提交中...' : isGmView ? '主持人预览' : '提交行动' }}
          </button>
        </div>

        <div v-if="!isGmView && context?.history?.length" class="mt-8 max-w-3xl mx-auto">
          <h3 class="text-white text-lg font-medium mb-4">已提交的阵营行动</h3>
          <div class="space-y-3">
            <div
              v-for="item in context.history"
              :key="item.id"
              class="bg-gradient-to-br from-[#1a2332] to-[#0f1419] border border-white/10 rounded-xl p-4"
            >
              <div class="flex items-center justify-between mb-2">
                <span class="text-white text-sm font-medium">{{ item.actionTypeLabel }}</span>
                <span
                  class="text-xs px-2 py-0.5 rounded-full"
                  :class="item.status === 'pending' ? 'bg-amber-500/20 text-amber-400' : 'bg-green-500/20 text-green-400'"
                >
                  {{ item.status === 'pending' ? '待反馈' : '已反馈' }}
                </span>
              </div>
              <div v-if="item.result" class="text-gray-400 text-xs whitespace-pre-wrap bg-black/20 rounded-lg p-3 mt-2">{{ item.result }}</div>
            </div>
          </div>
        </div>
      </template>

      <Teleport to="body">
        <div v-if="showActionHelpModal" class="fixed inset-0 bg-black/75 flex items-center justify-center z-50 p-4" @click.self="showActionHelpModal = false">
          <div class="bg-[#161b22] border border-[#2a3444] rounded-[18px] max-w-lg w-full max-h-[85vh] overflow-hidden flex flex-col shadow-[0_24px_48px_-12px_rgba(0,0,0,0.55)]">
            <div class="flex items-start justify-between px-6 pt-6 pb-4 border-b border-[#252d3a] shrink-0">
              <div>
                <h2 class="text-white text-lg font-semibold tracking-tight">阵营行动说明</h2>
                <p class="text-[#a0aab7] text-xs mt-1.5">以下为各阵营行动规则</p>
              </div>
              <button type="button" class="text-[#a0aab7] hover:text-white p-1" @click="showActionHelpModal = false">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/></svg>
              </button>
            </div>
            <div class="overflow-y-auto px-6 py-5 space-y-3">
              <div v-for="(entry, idx) in actionDefs" :key="entry.type" class="rounded-[10px] border border-[#253041] bg-[#1c2533] px-4 py-3.5">
                <p class="text-[#00d1ff] text-sm font-medium mb-2">{{ idx + 1 }}. {{ entry.title }}</p>
                <p class="text-[#a0aab7] text-sm leading-relaxed">{{ entry.description }}</p>
              </div>
            </div>
            <div class="px-6 py-5 border-t border-[#252d3a] shrink-0 flex justify-center">
              <button type="button" class="min-w-[140px] px-10 py-2.5 rounded-full bg-[#303e55] hover:bg-[#3a4d68] text-white text-sm" @click="showActionHelpModal = false">知道了</button>
            </div>
          </div>
        </div>
      </Teleport>
    </div>
  </div>
</template>
