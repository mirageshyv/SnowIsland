<script setup>
import { ref, computed, onMounted } from 'vue'
import { gameStateAPI, catastropheAPI, dmPlayerAPI } from '../utils/api.js'
import { RANGED_WEAPON_IDS, EXPLOSIVE_WEAPON_ID } from '../data/combatAssist.js'

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
const advanceStep = ref('')

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
  let step = null
  const raw = String(advanceStep.value ?? '').trim()
  if (raw !== '') {
    const parsed = Number(raw)
    if (!Number.isInteger(parsed) || parsed < 0 || parsed > 100) {
      error.value = '进度步进须为 0–100 的整数'
      return
    }
    step = parsed
  }
  advancing.value = true
  message.value = null
  error.value = ''
  try {
    const result = await catastropheAPI.advanceDay(step)
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

// ---------------------------------
// 图鉴管理（描述对所有类型可编辑；武器额外可编辑威胁值，为战斗结算真相）
// ---------------------------------
const CATALOG_TABS = [
  { key: 'weapon', label: '武器' },
  { key: 'item', label: '道具' },
  { key: 'ammo', label: '弹药' },
  { key: 'material', label: '材料' },
]
const catalogTab = ref('weapon')
const catalogRows = ref([])
const catalogLoading = ref(true)
const catalogError = ref('')
const catalogMessage = ref('')
const catalogSavingKey = ref(null)

const tabRows = computed(() => catalogRows.value.filter((r) => r.itemType === catalogTab.value))
const isWeaponTab = computed(() => catalogTab.value === 'weapon')

async function loadCatalog() {
  catalogLoading.value = true
  catalogError.value = ''
  try {
    const res = await dmPlayerAPI.getCatalog()
    if (res?.success) {
      catalogRows.value = (res.items || []).map((r) => ({
        itemType: r.itemType,
        id: r.itemId,
        name: r.name,
        unit: r.unit,
        threatLevel: r.threatLevel ?? null,
        remark: r.remark || '',
        editThreat: r.threatLevel ?? null,
        editRemark: r.remark || '',
      }))
    } else {
      catalogError.value = res?.message || '无法加载图鉴'
    }
  } catch (e) {
    catalogError.value = '加载图鉴失败：' + (e.message || '未知错误')
  } finally {
    catalogLoading.value = false
  }
}

function rowKey(r) {
  return `${r.itemType}-${r.id}`
}

function threatDirty(r) {
  if (r.itemType !== 'weapon') return false
  const v = Number(r.editThreat)
  return Number.isFinite(v) && v !== r.threatLevel
}

function remarkDirty(r) {
  return (r.editRemark ?? '').trim() !== (r.remark ?? '')
}

function rowDirty(r) {
  return threatDirty(r) || remarkDirty(r)
}

function weaponTypeLabel(r) {
  if (r.id === EXPLOSIVE_WEAPON_ID) return '炸药'
  return RANGED_WEAPON_IDS.has(r.id) ? '远程' : '近战'
}

async function saveRow(r) {
  catalogError.value = ''
  catalogMessage.value = ''
  const remark = (r.editRemark ?? '').trim()
  let threat = null
  if (threatDirty(r)) {
    threat = Math.floor(Number(r.editThreat))
    if (!Number.isFinite(threat) || threat < 0 || threat > 99) {
      catalogError.value = `${r.name}：威胁值需在 0-99 之间`
      return
    }
  }
  catalogSavingKey.value = rowKey(r)
  try {
    let res
    if (r.itemType === 'weapon') {
      const body = {}
      if (threat != null) body.threatLevel = threat
      if (remarkDirty(r)) body.remark = remark
      res = await dmPlayerAPI.updateWeapon(r.id, body)
    } else {
      res = await dmPlayerAPI.updateCatalogItem(r.itemType, r.id, { remark })
    }
    if (res?.success) {
      if (threat != null) {
        r.threatLevel = threat
        r.editThreat = threat
      }
      if (remarkDirty(r)) {
        r.remark = remark
        r.editRemark = remark
      }
      catalogMessage.value = `已保存：${r.name}`
    } else {
      catalogError.value = res?.message || '保存失败'
    }
  } catch (e) {
    catalogError.value = '保存失败：' + (e.message || '未知错误')
  } finally {
    catalogSavingKey.value = null
  }
}

onMounted(() => {
  load()
  loadCatalog()
})
</script>

<template>
  <div class="max-w-7xl">
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

      <div class="grid grid-cols-1 xl:grid-cols-[minmax(0,26rem)_minmax(0,1fr)] gap-6 items-start">

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
            <label class="flex items-center gap-2 text-gray-400 text-xs shrink-0">
              <span class="whitespace-nowrap">进度步进（留空默认33/34）</span>
              <input
                v-model="advanceStep"
                type="number"
                min="0"
                max="100"
                step="1"
                placeholder="—"
                class="w-16 bg-black/30 border border-white/10 rounded-lg px-2 py-1.5 text-white text-sm tabular-nums focus:outline-none focus:border-cyan-500/50"
              />
            </label>
            <button
              type="button"
              class="ml-auto px-4 py-2 rounded-lg bg-purple-600/30 border border-purple-500/40 text-purple-200 text-sm hover:bg-purple-600/40 disabled:opacity-50"
              :disabled="advancing || saving"
              @click="advanceDay"
            >
              {{ advancing ? '推进中…' : '推进一天（天灾进度）' }}
            </button>
          </div>
          <p class="text-gray-600 text-xs mt-2">天灾进度：{{ catastropheProgress }}%（推进一天 +33/34，或自定义步进）</p>
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

      <!-- 图鉴管理 -->
      <div class="bg-gradient-to-br from-[#1a2332] to-[#0f1419] border border-white/10 rounded-2xl p-4 sm:p-6">
        <div class="flex flex-wrap items-center justify-between gap-2 mb-1">
          <h2 class="text-white text-lg font-semibold">图鉴管理</h2>
          <button
            type="button"
            class="px-3 py-1.5 rounded-lg bg-white/5 text-gray-400 text-xs hover:bg-white/10"
            :disabled="catalogLoading"
            @click="loadCatalog"
          >
            重新加载
          </button>
        </div>
        <p class="text-gray-500 text-xs mb-3">
          数据库图鉴是全局真相来源：描述会显示在玩家背包等页面；武器威胁值直接用于战斗辅助的战力计算与额外命中判定。
        </p>

        <div class="flex flex-wrap gap-2 mb-3">
          <button
            v-for="tab in CATALOG_TABS"
            :key="tab.key"
            type="button"
            class="px-4 py-1.5 rounded-lg text-sm border transition-colors"
            :class="catalogTab === tab.key
              ? 'bg-cyan-600/30 border-cyan-500/50 text-cyan-200 font-medium'
              : 'bg-white/5 border-white/10 text-gray-400 hover:bg-white/10'"
            @click="catalogTab = tab.key"
          >
            {{ tab.label }}
          </button>
        </div>

        <p v-if="isWeaponTab" class="text-gray-600 text-xs mb-3">
          命中表定义威胁档 1-6 与 10，其他数值按最接近的较低档结算；炸药外围固定按 5 结算，此处威胁值为内围。
          描述中若写有威胁数字，修改威胁值时请一并更新描述。
        </p>

        <p v-if="catalogError" class="mb-3 text-red-400 text-sm">{{ catalogError }}</p>
        <p v-if="catalogMessage" class="mb-3 text-emerald-400 text-sm">{{ catalogMessage }}</p>

        <div v-if="catalogLoading" class="flex justify-center py-8">
          <div class="w-8 h-8 border-4 border-cyan-500 border-t-transparent rounded-full animate-spin" />
        </div>

        <template v-else>
          <!-- 桌面端：表格 -->
          <div class="hidden md:block overflow-x-auto">
            <table class="w-full text-sm">
              <thead>
                <tr class="text-left text-gray-500 text-xs border-b border-white/10">
                  <th class="py-2 pr-3 font-medium">ID</th>
                  <th class="py-2 pr-3 font-medium">名称</th>
                  <th v-if="isWeaponTab" class="py-2 pr-3 font-medium">类型</th>
                  <th v-if="isWeaponTab" class="py-2 pr-3 font-medium">威胁值</th>
                  <th class="py-2 pr-3 font-medium">描述</th>
                  <th class="py-2 font-medium text-right">操作</th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="r in tabRows"
                  :key="rowKey(r)"
                  class="border-b border-white/5 text-gray-300 align-top"
                  :class="rowDirty(r) ? 'bg-amber-500/5' : ''"
                >
                  <td class="py-2.5 pr-3 tabular-nums text-gray-500">{{ r.id }}</td>
                  <td class="py-2.5 pr-3 font-medium text-gray-200 whitespace-nowrap">{{ r.name }}</td>
                  <td v-if="isWeaponTab" class="py-2.5 pr-3">
                    <span
                      class="px-1.5 py-0.5 rounded text-xs whitespace-nowrap"
                      :class="weaponTypeLabel(r) === '远程'
                        ? 'bg-sky-500/15 text-sky-300'
                        : weaponTypeLabel(r) === '炸药'
                          ? 'bg-red-500/15 text-red-300'
                          : 'bg-white/5 text-gray-400'"
                    >{{ weaponTypeLabel(r) }}</span>
                  </td>
                  <td v-if="isWeaponTab" class="py-2.5 pr-3 whitespace-nowrap">
                    <input
                      v-model.number="r.editThreat"
                      type="number"
                      min="0"
                      max="99"
                      class="w-16 bg-black/30 border rounded-lg px-2 py-1 text-white tabular-nums text-center focus:outline-none focus:border-cyan-500/50"
                      :class="threatDirty(r) ? 'border-amber-500/60' : 'border-white/10'"
                    />
                    <span v-if="threatDirty(r)" class="ml-2 text-xs text-amber-300">{{ r.threatLevel }} → {{ r.editThreat }}</span>
                  </td>
                  <td class="py-2 pr-3 w-full max-w-0">
                    <textarea
                      v-model="r.editRemark"
                      rows="2"
                      class="w-full bg-black/30 border rounded-lg px-2 py-1.5 text-xs text-gray-300 leading-relaxed resize-y focus:outline-none focus:border-cyan-500/50"
                      :class="remarkDirty(r) ? 'border-amber-500/60' : 'border-white/10'"
                      placeholder="无描述"
                    />
                  </td>
                  <td class="py-2.5 text-right">
                    <button
                      type="button"
                      class="px-3 py-1 rounded-lg text-xs font-medium transition-colors disabled:opacity-40 disabled:cursor-not-allowed"
                      :class="rowDirty(r)
                        ? 'bg-cyan-600 text-white hover:bg-cyan-500'
                        : 'bg-white/5 text-gray-500'"
                      :disabled="!rowDirty(r) || catalogSavingKey === rowKey(r)"
                      @click="saveRow(r)"
                    >
                      {{ catalogSavingKey === rowKey(r) ? '保存中…' : '保存' }}
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <!-- 移动端：卡片列表 -->
          <div class="md:hidden space-y-2">
            <div
              v-for="r in tabRows"
              :key="rowKey(r)"
              class="rounded-xl border p-3"
              :class="rowDirty(r) ? 'border-amber-500/40 bg-amber-500/5' : 'border-white/10 bg-black/20'"
            >
              <div class="flex items-center gap-2 mb-2">
                <span class="text-gray-200 font-medium">{{ r.name }}</span>
                <span
                  v-if="isWeaponTab"
                  class="px-1.5 py-0.5 rounded text-xs"
                  :class="weaponTypeLabel(r) === '远程'
                    ? 'bg-sky-500/15 text-sky-300'
                    : weaponTypeLabel(r) === '炸药'
                      ? 'bg-red-500/15 text-red-300'
                      : 'bg-white/5 text-gray-400'"
                >{{ weaponTypeLabel(r) }}</span>
                <span class="ml-auto text-xs text-gray-600 tabular-nums">#{{ r.id }}</span>
              </div>
              <div v-if="isWeaponTab" class="flex items-center gap-2 mb-2">
                <label class="text-xs text-gray-400">威胁值</label>
                <input
                  v-model.number="r.editThreat"
                  type="number"
                  min="0"
                  max="99"
                  class="w-16 bg-black/30 border rounded-lg px-2 py-1.5 text-white tabular-nums text-center focus:outline-none focus:border-cyan-500/50"
                  :class="threatDirty(r) ? 'border-amber-500/60' : 'border-white/10'"
                />
                <span v-if="threatDirty(r)" class="text-xs text-amber-300">{{ r.threatLevel }} → {{ r.editThreat }}</span>
              </div>
              <textarea
                v-model="r.editRemark"
                rows="2"
                class="w-full bg-black/30 border rounded-lg px-2 py-1.5 text-xs text-gray-300 leading-relaxed resize-y focus:outline-none focus:border-cyan-500/50 mb-2"
                :class="remarkDirty(r) ? 'border-amber-500/60' : 'border-white/10'"
                placeholder="无描述"
              />
              <button
                type="button"
                class="w-full px-4 py-2 rounded-lg text-xs font-medium transition-colors disabled:opacity-40 disabled:cursor-not-allowed min-h-[36px]"
                :class="rowDirty(r)
                  ? 'bg-cyan-600 text-white hover:bg-cyan-500'
                  : 'bg-white/5 text-gray-500'"
                :disabled="!rowDirty(r) || catalogSavingKey === rowKey(r)"
                @click="saveRow(r)"
              >
                {{ catalogSavingKey === rowKey(r) ? '保存中…' : '保存' }}
              </button>
            </div>
          </div>
        </template>
      </div>

      </div>
    </template>
  </div>
</template>
