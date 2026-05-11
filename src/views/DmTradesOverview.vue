<script setup>
import { ref, onMounted } from 'vue'
import { tradeAPI } from '../utils/api.js'

const loading = ref(true)
const trades = ref([])
const error = ref('')

const typeLabel = (t) =>
  ({
    item: '道具',
    weapon: '武器',
    ammo: '弹药',
    material: '物资',
    food: '食物',
    energy: '燃料'
  }[String(t || '').toLowerCase()] || String(t || ''))

const formatUnit = (u) => ({ kg: '千克', portion: '份', L: '升' }[u] || u || '')

const statusChip = (status) => {
  const s = String(status || '').toLowerCase()
  if (s === 'pending') return { text: '交易中', cls: 'bg-yellow-500/20 text-yellow-400 border-yellow-500/40' }
  if (s === 'completed') return { text: '已接受', cls: 'bg-emerald-500/20 text-emerald-400 border-emerald-500/40' }
  return { text: status, cls: 'bg-white/10 text-gray-400 border-white/15' }
}

const formatWhen = (s) => {
  if (!s) return ''
  const d = new Date(s)
  if (Number.isNaN(d.getTime())) return String(s)
  return d.toLocaleString('zh-CN', { hour12: false })
}

const load = async () => {
  loading.value = true
  error.value = ''
  try {
    const res = await tradeAPI.getDmOverview()
    if (res && res.success && Array.isArray(res.trades)) {
      trades.value = res.trades
    } else {
      trades.value = []
      error.value = (res && res.message) || '加载失败'
    }
  } catch (e) {
    trades.value = []
    error.value = e?.message || '网络错误'
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="max-w-6xl">
    <div class="mb-4 flex items-start justify-between gap-3">
      <div>
        <h1 class="text-white mb-1 tracking-tight text-xl">全部交易（待处理 / 已完成）</h1>
        <p class="text-gray-500 text-xs">DM 工作台 · {{ trades.length }} 条</p>
      </div>
      <button
        type="button"
        class="text-xs px-3 py-1.5 rounded-lg bg-white/5 border border-white/10 text-gray-300 hover:bg-white/10"
        @click="load"
      >
        刷新
      </button>
    </div>

    <p v-if="error" class="text-red-400 text-sm mb-3">{{ error }}</p>
    <p v-if="!loading && !error && trades.length === 0" class="text-gray-500 text-sm">暂无相关交易。</p>

    <div v-if="loading" class="text-gray-400 text-sm">加载中…</div>

    <div v-else class="space-y-3">
      <div
        v-for="trade in trades"
        :key="trade.id"
        class="rounded-xl border border-white/10 bg-[#0f1419] p-3 text-sm"
      >
        <div class="flex flex-wrap items-center gap-2 mb-2">
          <span :class="['text-[11px] px-2 py-0.5 rounded-full border', statusChip(trade.status).cls]">
            {{ statusChip(trade.status).text }}
          </span>
          <span class="text-gray-300">
            <span class="text-emerald-400/90">{{ trade.fromPlayerName }}</span>
            <span class="text-gray-500 mx-1">→</span>
            <span class="text-sky-400/90">{{ trade.toPlayerName }}</span>
          </span>
          <span class="text-gray-500 text-xs">{{ formatWhen(trade.createdAt) }}</span>
        </div>
        <p v-if="trade.remark" class="text-gray-400 text-xs mb-2 truncate" :title="trade.remark">备注：{{ trade.remark }}</p>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-2 text-xs">
          <div>
            <div class="text-gray-500 mb-1">条目</div>
            <ul class="space-y-0.5 font-mono text-[11px] text-gray-300">
              <li v-for="(it, idx) in trade.items" :key="idx" class="flex flex-wrap gap-x-2 gap-y-0.5">
                <span :class="it.direction === 'give' ? 'text-emerald-400/90' : 'text-amber-400/90'">
                  {{ it.direction === 'give' ? '提供' : '索取' }}
                </span>
                <span>[{{ typeLabel(it.itemType) }}]</span>
                <span>{{ it.name }}</span>
                <span class="text-gray-500">{{ it.quantity }}{{ formatUnit(it.unit) }}</span>
                <span v-if="Number(it.kcalPerUnit) > 0" class="text-gray-500">{{ it.kcalPerUnit }}大卡/单位</span>
              </li>
              <li v-if="!trade.items || trade.items.length === 0" class="text-gray-500">—</li>
            </ul>
          </div>
          <div class="text-[11px] text-gray-500 md:text-right">
            发起者 ID {{ trade.fromPlayerId }} · 接收者 ID {{ trade.toPlayerId }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
