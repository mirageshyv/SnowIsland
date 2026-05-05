<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { playerAPI } from '../../utils/api.js'

const materials = ref([])
const loading = ref(true)
const playerId = localStorage.getItem('playerId') || '1'

const getMaterialColor = (name) => {
  if (name.includes('金属') || name.includes('发动机') || name.includes('螺旋桨') || name.includes('发电机')) {
    return 'from-gray-400/20 to-gray-500/20'
  }
  if (name.includes('木') || name.includes('绳索') || name.includes('帆布')) {
    return 'from-amber-400/20 to-amber-500/20'
  }
  if (name.includes('石') || name.includes('沥青')) {
    return 'from-stone-400/20 to-stone-500/20'
  }
  if (name.includes('食物')) {
    return 'from-emerald-400/20 to-emerald-500/20'
  }
  if (name.includes('燃料')) {
    return 'from-orange-400/20 to-orange-500/20'
  }
  return 'from-blue-400/20 to-blue-500/20'
}

const getRemarkByMaterial = (materialId) => {
  const remarks = {
    1: '可用于制作工具',
    2: '可用于建造',
    3: '多种用途',
    4: '建筑材料',
    5: '恢复饥饿',
    6: '建筑材料',
    7: '建筑材料',
    8: '提供能源',
    9: '制作帐篷',
    10: '机械动力',
    11: '船只推进',
    12: '发电设备'
  }
  return remarks[materialId] || '基础材料'
}

const getIconByType = (type) => {
  const icons = {
    item: '📦',
    weapon: '⚔️',
    ammo: '🎯',
    material: '🔧'
  }
  return icons[type] || '🔧'
}

const loadMaterials = async () => {
  loading.value = true
  try {
    console.log('=== loadMaterials called, playerId:', playerId)
    const result = await playerAPI.getItems(playerId)
    console.log('=== API result:', result)
    console.log('=== Is array:', Array.isArray(result))
    
    if (Array.isArray(result)) {
      console.log('=== All items from API:', JSON.stringify(result, null, 2))
      const filtered = result.filter(item => item.type === 'material')
      console.log('=== Filtered material items:', JSON.stringify(filtered, null, 2))
      
      materials.value = filtered.map(item => ({
        id: item.id,
        name: item.name,
        unit: item.unit,
        quantity: item.quantity,
        icon: getIconByType('material'),
        remark: getRemarkByMaterial(item.id)
      }))
      console.log('=== Materials after mapping:', JSON.stringify(materials.value, null, 2))
      console.log('Materials refreshed:', materials.value.length, 'items')
    } else {
      console.log('=== Result is not an array:', typeof result)
    }
  } catch (error) {
    console.error('Failed to load materials:', error)
  } finally {
    loading.value = false
  }
}

const handleVisibilityChange = () => {
  if (!document.hidden) {
    console.log('Page visible, refreshing materials...')
    loadMaterials()
  }
}

onMounted(() => {
  loadMaterials()
  document.addEventListener('visibilitychange', handleVisibilityChange)
  console.log('Player basic materials component mounted', { playerId })
})

onUnmounted(() => {
  document.removeEventListener('visibilitychange', handleVisibilityChange)
})

defineExpose({ refresh: loadMaterials })
</script>

<template>
  <div class="max-w-6xl">
    <div class="mb-6">
      <h1 class="text-white mb-1 tracking-tight text-2xl">基础物资管理</h1>
      <p class="text-gray-500 text-sm">Basic Materials & Resources</p>
    </div>

    <div v-if="materials.length > 0" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
      <div
        v-for="material in materials"
        :key="material.id"
        class="relative bg-gradient-to-br from-[#1a2332] to-[#0f1419] border border-white/10 rounded-2xl p-5 overflow-hidden hover:border-white/20 transition-all"
      >
        <div class="absolute top-0 right-0 w-32 h-32 bg-emerald-500/5 rounded-full blur-2xl" />
        
        <div class="relative flex items-start gap-4">
          <div :class="['w-14 h-14 rounded-xl bg-gradient-to-br flex items-center justify-center text-2xl flex-shrink-0', getMaterialColor(material.name)]">
            {{ material.icon }}
          </div>
          
          <div class="flex-1 min-w-0">
            <div class="flex items-center justify-between mb-1">
              <h3 class="text-white font-medium truncate">{{ material.name }}</h3>
              <span class="text-xs text-gray-400 bg-white/5 px-2 py-1 rounded-full">{{ material.unit }}</span>
            </div>
            
            <p class="text-gray-400 text-sm mb-3">{{ material.remark }}</p>
            
            <div class="flex items-center gap-2">
              <span class="text-xs text-gray-500">数量</span>
              <span class="text-lg font-semibold text-emerald-400">{{ material.quantity }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-else class="flex flex-col items-center justify-center py-20">
      <div class="w-24 h-24 rounded-full bg-white/5 flex items-center justify-center mb-4">
        <span class="text-4xl">📦</span>
      </div>
      <h3 class="text-white text-lg mb-2">暂无物资</h3>
      <p class="text-gray-500 text-sm">您当前没有该类别的物资</p>
    </div>
  </div>
</template>
