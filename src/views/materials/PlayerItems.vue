<script setup>
import { ref, onMounted } from 'vue'
import { playerAPI } from '../../utils/api.js'

const items = ref([])
const loading = ref(true)
const playerId = localStorage.getItem('playerId') || '1'

const loadItems = async () => {
  loading.value = true
  try {
    const result = await playerAPI.getItems(playerId)
    if (Array.isArray(result)) {
      items.value = result.filter(item => item.type === 'item').map(item => ({
        id: item.id,
        name: item.name,
        unit: item.unit,
        quantity: item.quantity,
        icon: getIconByType('item'),
        remark: getRemarkByItem(item.id)
      }))
    }
  } catch (error) {
    console.error('Failed to load items:', error)
  } finally {
    loading.value = false
  }
}

const getIconByType = (type) => {
  const icons = {
    item: '📦',
    weapon: '⚔️',
    ammo: '🎯',
    material: '🔧'
  }
  return icons[type] || '📦'
}

const getRemarkByItem = (itemId) => {
  const remarks = {
    1: '恢复生命值',
    2: '提供光源',
    3: '限制行动',
    4: '发出信号',
    5: '减少伤害',
    6: '提供防护',
    7: '发射信号',
    8: '修复物品',
    9: '交易凭证',
    10: '恢复精力',
    11: '医疗材料',
    12: '捕鱼工具',
    13: '照明工具',
    14: '消毒用品',
    15: '点火工具',
    16: '书写工具',
    17: '导航工具',
    18: '食物补给'
  }
  return remarks[itemId] || '道具'
}

onMounted(() => {
  loadItems()
  console.log('Player items loaded from API', { playerId })
})
</script>

<template>
  <div class="max-w-6xl">
    <div class="mb-6">
      <h1 class="text-white mb-1 tracking-tight text-2xl">道具管理</h1>
      <p class="text-gray-500 text-sm">Items & Equipment</p>
    </div>

    <div v-if="items.length > 0" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
      <div
        v-for="item in items"
        :key="item.id"
        class="relative bg-gradient-to-br from-[#1a2332] to-[#0f1419] border border-white/10 rounded-2xl p-5 overflow-hidden hover:border-white/20 transition-all"
      >
        <div class="absolute top-0 right-0 w-32 h-32 bg-blue-500/5 rounded-full blur-2xl" />
        
        <div class="relative flex items-start gap-4">
          <div class="w-14 h-14 rounded-xl bg-gradient-to-br from-blue-400/20 to-blue-500/20 flex items-center justify-center text-2xl flex-shrink-0">
            {{ item.icon }}
          </div>
          
          <div class="flex-1 min-w-0">
            <div class="flex items-center justify-between mb-1">
              <h3 class="text-white font-medium truncate">{{ item.name }}</h3>
              <span class="text-xs text-gray-400 bg-white/5 px-2 py-1 rounded-full">{{ item.unit }}</span>
            </div>
            
            <p class="text-gray-400 text-sm mb-3">{{ item.remark }}</p>
            
            <div class="flex items-center gap-2">
              <span class="text-xs text-gray-500">数量</span>
              <span class="text-lg font-semibold text-blue-400">{{ item.quantity }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-else class="flex flex-col items-center justify-center py-20">
      <div class="w-24 h-24 rounded-full bg-white/5 flex items-center justify-center mb-4">
        <span class="text-4xl">📦</span>
      </div>
      <h3 class="text-white text-lg mb-2">暂无道具</h3>
      <p class="text-gray-500 text-sm">您当前没有该类别的物资</p>
    </div>
  </div>
</template>
