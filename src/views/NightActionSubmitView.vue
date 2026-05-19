<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { nightActionAPI, locationAPI, actionAPI } from '@/utils/api.js'
import {
  FACTION_LABELS,
  NIGHT_ACTION_DEFS,
  NIGHT_PERSONAL_ACTION_TYPES,
  PRESSURE_DEMAND_OPTIONS,
  RAID_OUTCOME_OPTIONS,
  getConspiracySubtypes,
} from '@/data/nightActions.js'

const playerId = parseInt(localStorage.getItem('playerId') || '0')
const gameDay = ref(1)
const context = ref(null)
const locations = ref([])
const productionInfo = ref(null)
const loading = ref(true)
const submitting = ref(false)
const submitMessage = ref(null)
const selectedType = ref('')
const actionResult = ref('')

const forms = reactive({
  night_personal_action: { actionType: '', targetId: '', npcId: '', notes: '' },
  public_trial: { targetPlayerId: '', note: '' },
  pressure_ruler: { demand: '', note: '' },
  publicity: { message: '', note: '' },
  conspiracy: {
    conspiracySubtype: '',
    targetLocationId: '',
    targetPlayerId: '',
    participantIds: [],
    raidOutcome: '',
    note: '',
  },
  other: { note: '' },
})

const playerFaction = computed(() => context.value?.faction || '')
const actionDefs = computed(() => NIGHT_ACTION_DEFS[playerFaction.value] || [])
const unlimitedActions = computed(() => !!context.value?.unlimitedActions)
const hasSubmittedToday = computed(() => !!context.value?.hasSubmittedToday)
const usedTypes = computed(() => new Set(context.value?.usedActionTypes || []))
const allPlayers = computed(() => context.value?.allPlayers || [])
const conspiracyOptions = computed(() => getConspiracySubtypes(playerFaction.value))

const locationOptions = computed(() =>
  (locations.value || []).map((l) => ({ value: l.id, label: `${l.name}（${l.area || ''}）` }))
)

const personalActionOptions = computed(() =>
  NIGHT_PERSONAL_ACTION_TYPES.filter(
    (o) => o.value !== 'produce' || productionInfo.value?.canProduce
  )
)

const personalTargetOptions = computed(() => {
  const at = forms.night_personal_action.actionType
  if (at === 'go_location') return locationOptions.value
  if (at === 'investigate_player') {
    return allPlayers.value
      .filter((p) => p.id !== playerId)
      .map((p) => ({ value: p.id, label: p.name }))
  }
  return []
})

const personalNpcOptions = computed(() => {
  const locId = parseInt(forms.night_personal_action.targetId)
  if (!locId) return []
  const loc = (locations.value || []).find((l) => l.id === locId)
  return (loc?.npcs || []).map((n) => ({ value: n.id, label: `${n.name}（${n.job}）` }))
})

const selectedDef = computed(() => actionDefs.value.find((d) => d.type === selectedType.value) || null)

const actionTypeOptions = computed(() =>
  actionDefs.value.map((def) => {
    const used = unlimitedActions.value && usedTypes.value.has(def.type)
    const quota = !unlimitedActions.value && hasSubmittedToday.value
    return {
      value: def.type,
      label: def.title,
      disabled: used || quota,
      statusLabel: used ? '今日已使用' : quota ? '今日已提交' : '可执行',
    }
  })
)

const currentStatus = computed(() => {
  if (!selectedType.value) return { btn: 'disabled', label: '' }
  const opt = actionTypeOptions.value.find((o) => o.value === selectedType.value)
  if (!opt) return { btn: 'disabled', label: '' }
  return {
    btn: opt.disabled ? 'disabled' : 'enabled',
    label: opt.statusLabel,
    key: opt.disabled ? 'blocked' : 'ready',
  }
})

const canSubmit = computed(
  () => selectedType.value && currentStatus.value?.btn === 'enabled' && !submitting.value
)

const showConspiracyLocation = computed(() => {
  const sub = forms.conspiracy.conspiracySubtype
  return sub && sub !== 'spread_terror'
})

const showConspiracyTerrorTarget = computed(() => forms.conspiracy.conspiracySubtype === 'spread_terror')
const showRaidOutcome = computed(() => forms.conspiracy.conspiracySubtype === 'raid_location')

function isTypeUsed(type) {
  return unlimitedActions.value && usedTypes.value.has(type)
}

function toggleParticipant(id) {
  const ids = forms.conspiracy.participantIds
  const i = ids.indexOf(id)
  if (i >= 0) ids.splice(i, 1)
  else ids.push(id)
}

function resetForm(type) {
  const defaults = {
    night_personal_action: { actionType: '', targetId: '', npcId: '', notes: '' },
    public_trial: { targetPlayerId: '', note: '' },
    pressure_ruler: { demand: '', note: '' },
    publicity: { message: '', note: '' },
    conspiracy: {
      conspiracySubtype: '',
      targetLocationId: '',
      targetPlayerId: '',
      participantIds: [],
      raidOutcome: '',
      note: '',
    },
    other: { note: '' },
  }
  if (defaults[type]) Object.assign(forms[type], defaults[type])
}

watch(selectedType, (type, prev) => {
  if (prev) resetForm(prev)
  actionResult.value = ''
})

watch(() => forms.night_personal_action.actionType, () => {
  forms.night_personal_action.targetId = ''
  forms.night_personal_action.npcId = ''
})

watch(() => forms.conspiracy.conspiracySubtype, () => {
  forms.conspiracy.targetLocationId = ''
  forms.conspiracy.targetPlayerId = ''
  forms.conspiracy.raidOutcome = ''
})

async function loadContext() {
  loading.value = true
  try {
    const [ctx, locs, prod] = await Promise.all([
      nightActionAPI.getContext(playerId, gameDay.value),
      locationAPI.getAll(),
      actionAPI.getProductionInfo(playerId),
    ])
    context.value = ctx
    locations.value = Array.isArray(locs) ? locs : []
    productionInfo.value = prod
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

watch(gameDay, () => {
  selectedType.value = ''
  actionResult.value = ''
  loadContext()
})

function buildPayload(type) {
  const f = forms[type]
  switch (type) {
    case 'night_personal_action':
      return {
        actionType: f.actionType,
        targetId: f.targetId ? parseInt(f.targetId) : null,
        npcId: f.npcId ? parseInt(f.npcId) : null,
        notes: f.notes || '',
      }
    case 'public_trial':
      return { targetPlayerId: parseInt(f.targetPlayerId), note: f.note || '' }
    case 'pressure_ruler':
      return { demand: f.demand, note: f.note || '' }
    case 'publicity':
      return { message: f.message, note: f.note || '' }
    case 'conspiracy':
      return {
        conspiracySubtype: f.conspiracySubtype,
        targetLocationId: f.targetLocationId ? parseInt(f.targetLocationId) : null,
        targetPlayerId: f.targetPlayerId ? parseInt(f.targetPlayerId) : null,
        participantIds: f.participantIds.map(Number),
        raidOutcome: f.raidOutcome || null,
        note: f.note || '',
      }
    case 'other':
      return { note: f.note || '' }
    default:
      return {}
  }
}

function validateClient(type) {
  const f = forms[type]
  switch (type) {
    case 'night_personal_action':
      if (!f.actionType) return '请选择个人行动'
      if (['go_location', 'investigate_player'].includes(f.actionType) && !f.targetId) return '请选择目标'
      if (['use_trait', 'use_skill'].includes(f.actionType) && (!f.notes || f.notes.trim().length < 5)) {
        return '使用特性/技能时请在备注中详细描述'
      }
      if (f.actionType === 'other' && (!f.notes || f.notes.trim().length < 5)) {
        return '请详细描述你想执行的具体行动内容'
      }
      break
    case 'public_trial':
      if (!f.targetPlayerId) return '请选择审判目标'
      break
    case 'pressure_ruler':
      if (!f.demand) return '请选择施压诉求'
      break
    case 'publicity':
      if (!f.message?.trim()) return '请填写宣传内容'
      break
    case 'conspiracy':
      if (!f.conspiracySubtype) return '请选择密谋类型'
      if (showConspiracyLocation.value && !f.targetLocationId) return '请选择目标地点'
      if (showConspiracyTerrorTarget.value && !f.targetLocationId && !f.targetPlayerId) {
        return '请选择目标地点或玩家'
      }
      if (!f.participantIds.length) return '请至少选择一名参与玩家'
      if (showRaidOutcome.value && !f.raidOutcome) return '请选择袭击成功后的意向'
      break
    case 'other':
      if (!f.note || f.note.trim().length < 5) return '请详细描述你想执行的具体行动内容'
      break
    default:
      break
  }
  return null
}

async function submitAction() {
  if (!selectedType.value) {
    alert('请选择夜晚行动')
    return
  }
  const err = validateClient(selectedType.value)
  if (err) {
    alert(err)
    return
  }
  if (currentStatus.value?.btn === 'disabled') return

  submitting.value = true
  submitMessage.value = null
  try {
    const res = await nightActionAPI.submitAction({
      playerId,
      actionType: selectedType.value,
      gameDay: gameDay.value,
      payload: buildPayload(selectedType.value),
    })
    if (res?.success) {
      actionResult.value = res.data?.result || '已提交'
      submitMessage.value = { type: 'success', text: '夜晚行动提交成功' }
      selectedType.value = ''
      await loadContext()
    } else {
      submitMessage.value = { type: 'error', text: res?.message || '提交失败' }
    }
  } catch {
    submitMessage.value = { type: 'error', text: '提交失败，请重试' }
  } finally {
    submitting.value = false
    if (submitMessage.value) setTimeout(() => { submitMessage.value = null }, 3000)
  }
}

const selectClass =
  'w-full appearance-none bg-white/5 border border-white/10 rounded-xl px-4 py-3 pr-10 text-gray-200 text-sm focus:outline-none focus:border-indigo-500/50'
const textareaClass =
  'w-full resize-none bg-white/5 border border-white/10 rounded-xl px-4 py-3 text-gray-200 text-sm placeholder:text-gray-600 focus:outline-none focus:border-indigo-500/50'

onMounted(loadContext)
</script>

<template>
  <div class="min-h-screen bg-[#0a0e1a] py-8 px-4 md:px-8">
    <div class="max-w-3xl mx-auto">
      <div class="text-center mb-10">
        <h1 class="text-white text-2xl md:text-3xl font-semibold tracking-tight mb-2">夜晚行动</h1>
        <p class="text-gray-500 text-sm">根据阵营选择夜间行动并提交，由主持人在夜晚阶段结算</p>
        <div class="mt-3 flex items-center justify-center gap-2">
          <label class="text-gray-400 text-sm">当前天数：</label>
          <select
            v-model.number="gameDay"
            class="bg-black/30 border border-white/10 rounded-lg px-3 py-1 text-sm text-gray-200"
          >
            <option :value="1">第1天</option>
            <option :value="2">第2天</option>
            <option :value="3">第3天</option>
          </select>
        </div>
      </div>

      <div v-if="loading" class="flex justify-center py-20">
        <div class="w-12 h-12 border-4 border-indigo-500 border-t-transparent rounded-full animate-spin" />
      </div>

      <div v-else-if="context && !context.success" class="text-center py-16 text-gray-400">
        {{ context.message || '暂无可用夜晚行动' }}
      </div>

      <template v-else>
        <div
          class="relative bg-gradient-to-br from-[#1a2332] to-[#0f1419] border border-white/10 rounded-3xl p-6 mb-8"
        >
          <div class="space-y-4">
            <div class="flex items-center gap-3">
              <span
                class="inline-flex w-9 h-9 items-center justify-center rounded-lg bg-indigo-500/20 border border-indigo-500/30 text-indigo-300 text-sm"
              >🌙</span>
              <h2 class="text-white text-lg">夜晚行动提交</h2>
              <span
                v-if="currentStatus.label"
                class="ml-auto text-xs px-2 py-0.5 rounded-full"
                :class="currentStatus.key === 'ready' ? 'bg-green-500/20 text-green-400' : 'bg-gray-500/20 text-gray-400'"
              >
                {{ currentStatus.label }}
              </span>
            </div>

            <div>
              <label class="block text-gray-500 text-xs mb-2">选择行动</label>
              <select v-model="selectedType" :class="selectClass">
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

            <!-- 夜晚个人行动 -->
            <template v-if="selectedType === 'night_personal_action'">
              <p class="text-indigo-300/90 text-xs">与白天个人行动相同，选择一项行动提交。每日一次。</p>
              <div>
                <label class="block text-gray-500 text-xs mb-2">行动类型</label>
                <select v-model="forms.night_personal_action.actionType" :class="selectClass">
                  <option value="">请选择</option>
                  <option v-for="a in personalActionOptions" :key="a.value" :value="a.value">{{ a.label }}</option>
                </select>
              </div>
              <div v-if="['go_location', 'investigate_player'].includes(forms.night_personal_action.actionType)">
                <label class="block text-gray-500 text-xs mb-2">目标</label>
                <select v-model="forms.night_personal_action.targetId" :class="selectClass">
                  <option value="">请选择</option>
                  <option v-for="o in personalTargetOptions" :key="o.value" :value="o.value">{{ o.label }}</option>
                </select>
              </div>
              <div
                v-if="forms.night_personal_action.actionType === 'go_location' && personalNpcOptions.length"
              >
                <label class="block text-gray-500 text-xs mb-2">交互 NPC（可选）</label>
                <select v-model="forms.night_personal_action.npcId" :class="selectClass">
                  <option value="">不交互</option>
                  <option v-for="n in personalNpcOptions" :key="n.value" :value="n.value">{{ n.label }}</option>
                </select>
              </div>
              <div v-if="forms.night_personal_action.actionType === 'other'">
                <div class="rounded-xl border border-gray-500/20 bg-gray-500/5 px-4 py-3">
                  <p class="text-gray-300 text-sm">请在下方描述中详细说明你想执行的具体行动内容，由DM判定是否成功及效果</p>
                </div>
              </div>
              <div>
                <label class="block text-gray-500 text-xs mb-2">
                  备注
                  <span
                    v-if="['use_trait', 'use_skill', 'other'].includes(forms.night_personal_action.actionType)"
                    class="text-red-400"
                  >（必填）</span>
                </label>
                <textarea v-model="forms.night_personal_action.notes" rows="3" :class="textareaClass"
                  :placeholder="forms.night_personal_action.actionType === 'other' ? '请详细描述你想执行的具体行动内容...' : '在此输入备注说明...'" />
              </div>
            </template>

            <!-- 公开审判 -->
            <template v-else-if="selectedType === 'public_trial'">
              <div>
                <label class="block text-gray-500 text-xs mb-2">审判对象</label>
                <select v-model="forms.public_trial.targetPlayerId" :class="selectClass">
                  <option value="">请选择玩家</option>
                  <option v-for="p in allPlayers.filter(x => x.id !== playerId)" :key="p.id" :value="p.id">
                    {{ p.name }}
                  </option>
                </select>
              </div>
              <textarea v-model="forms.public_trial.note" rows="2" :class="textareaClass" placeholder="审判事由（可选）" />
            </template>

            <!-- 施压 -->
            <template v-else-if="selectedType === 'pressure_ruler'">
              <div>
                <label class="block text-gray-500 text-xs mb-2">施压诉求</label>
                <select v-model="forms.pressure_ruler.demand" :class="selectClass">
                  <option value="">请选择</option>
                  <option v-for="d in PRESSURE_DEMAND_OPTIONS" :key="d.value" :value="d.value">{{ d.label }}</option>
                </select>
              </div>
              <textarea v-model="forms.pressure_ruler.note" rows="2" :class="textareaClass" placeholder="理由与说明（建议来自白天调查）" />
            </template>

            <!-- 公开宣传 -->
            <template v-else-if="selectedType === 'publicity'">
              <textarea
                v-model="forms.publicity.message"
                rows="3"
                :class="textareaClass"
                placeholder="公屏宣传内容..."
              />
            </template>

            <!-- 其他 -->
            <template v-else-if="selectedType === 'other'">
              <div class="rounded-xl border border-gray-500/20 bg-gray-500/5 px-4 py-3">
                <p class="text-gray-300 text-sm">请在下方详细描述你想执行的具体行动内容，由DM判定是否成功及效果</p>
              </div>
              <div>
                <label class="block text-gray-500 text-xs mb-2">
                  行动描述
                  <span class="text-red-400">（必填）</span>
                </label>
                <textarea v-model="forms.other.note" rows="4" :class="textareaClass" placeholder="请详细描述你想执行的具体行动内容..." />
              </div>
            </template>

            <!-- 密谋 -->
            <template v-else-if="selectedType === 'conspiracy'">
              <div>
                <label class="block text-gray-500 text-xs mb-2">密谋类型</label>
                <select v-model="forms.conspiracy.conspiracySubtype" :class="selectClass">
                  <option value="">请选择</option>
                  <option v-for="c in conspiracyOptions" :key="c.value" :value="c.value">{{ c.label }}</option>
                </select>
              </div>
              <div v-if="showConspiracyLocation">
                <label class="block text-gray-500 text-xs mb-2">目标地点</label>
                <select v-model="forms.conspiracy.targetLocationId" :class="selectClass">
                  <option value="">请选择地点</option>
                  <option v-for="o in locationOptions" :key="o.value" :value="o.value">{{ o.label }}</option>
                </select>
              </div>
              <template v-if="showConspiracyTerrorTarget">
                <div>
                  <label class="block text-gray-500 text-xs mb-2">目标地点（可选）</label>
                  <select v-model="forms.conspiracy.targetLocationId" :class="selectClass">
                    <option value="">不选地点</option>
                    <option v-for="o in locationOptions" :key="'t-'+o.value" :value="o.value">{{ o.label }}</option>
                  </select>
                </div>
                <div>
                  <label class="block text-gray-500 text-xs mb-2">目标玩家（可选）</label>
                  <select v-model="forms.conspiracy.targetPlayerId" :class="selectClass">
                    <option value="">不选玩家</option>
                    <option v-for="p in allPlayers" :key="'tp-'+p.id" :value="p.id">{{ p.name }}</option>
                  </select>
                </div>
              </template>
              <div v-if="showRaidOutcome">
                <label class="block text-gray-500 text-xs mb-2">成功后意向</label>
                <select v-model="forms.conspiracy.raidOutcome" :class="selectClass">
                  <option value="">请选择</option>
                  <option v-for="r in RAID_OUTCOME_OPTIONS" :key="r.value" :value="r.value">{{ r.label }}</option>
                </select>
              </div>
              <div>
                <label class="block text-gray-500 text-xs mb-2">参与玩家</label>
                <div class="flex flex-wrap gap-2">
                  <button
                    v-for="p in allPlayers.filter(x => x.id !== playerId)"
                    :key="p.id"
                    type="button"
                    class="px-3 py-1.5 rounded-lg text-xs border transition-colors"
                    :class="forms.conspiracy.participantIds.includes(p.id)
                      ? 'bg-purple-500/20 border-purple-500/40 text-purple-200'
                      : 'bg-white/5 border-white/10 text-gray-400'"
                    @click="toggleParticipant(p.id)"
                  >
                    {{ p.name }}
                  </button>
                </div>
              </div>
              <textarea v-model="forms.conspiracy.note" rows="2" :class="textareaClass" placeholder="密谋说明（可选）" />
            </template>

            <div>
              <label class="block text-gray-500 text-xs mb-2">行动结果</label>
              <div
                class="min-h-[5rem] rounded-xl border border-white/10 bg-black/25 px-4 py-3 text-sm text-gray-400 whitespace-pre-wrap"
              >
                {{ actionResult || '结果将在此显示' }}
              </div>
            </div>
          </div>
        </div>

        <div v-if="submitMessage" class="flex justify-center mb-4">
          <div
            :class="[
              'px-5 py-2.5 rounded-xl text-sm',
              submitMessage.type === 'success' ? 'bg-green-500/20 text-green-400 border border-green-500/30' : 'bg-red-500/20 text-red-400 border border-red-500/30',
            ]"
          >
            {{ submitMessage.text }}
          </div>
        </div>

        <div class="flex justify-center pb-4">
          <button
            type="button"
            :disabled="!canSubmit"
            class="min-w-[200px] bg-gradient-to-r from-indigo-500 to-indigo-600 hover:from-indigo-600 hover:to-indigo-700 disabled:from-gray-600 disabled:to-gray-600 text-white px-8 py-3 rounded-xl text-sm font-medium"
            @click="submitAction"
          >
            {{ submitting ? '提交中...' : '提交夜晚行动' }}
          </button>
        </div>

        <div v-if="context?.history?.length" class="mt-8 space-y-3">
          <h3 class="text-white text-lg font-medium">已提交的夜晚行动</h3>
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
                {{ item.status === 'pending' ? '待结算' : '已结算' }}
              </span>
            </div>
            <div
              v-if="item.result"
              class="text-gray-400 text-xs whitespace-pre-wrap bg-black/20 rounded-lg p-3"
            >{{ item.result }}</div>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>
