<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { playerAPI } from '../../utils/api.js'

const weapons = ref([])
const ammo = ref([])
const loading = ref(true)
const playerId = localStorage.getItem('playerId') || '1'

const weaponsMap = {
  1: { name: '制式手枪', threat_level: 5, remark: '标准配备' },
  2: { name: '猎枪', threat_level: 8, remark: '威力较大' },
  3: { name: '警棍', threat_level: 3, remark: '非致命武器' },
  4: { name: '刺刀', threat_level: 4, remark: '近战武器' },
  5: { name: '水手刀', threat_level: 3, remark: '多功能刀具' },
  6: { name: '鱼叉/矛', threat_level: 6, remark: '狩猎工具' },
  7: { name: '猎弓', threat_level: 5, remark: '远程武器' },
  8: { name: '十字镐', threat_level: 4, remark: '挖掘工具' },
  9: { name: '斧头', threat_level: 6, remark: '砍伐工具' },
  10: { name: '电锯', threat_level: 7, remark: '切割工具' }
}

const ammoMap = {
  1: { name: '手枪弹', weapon_name: '制式手枪', remark: '制式手枪子弹' },
  2: { name: '猎枪弹', weapon_name: '猎枪', remark: '猎枪子弹' },
  3: { name: '信号弹', weapon_name: '信号枪', remark: '信号枪子弹' },
  4: { name: '箭矢', weapon_name: '猎弓', remark: '猎弓箭矢' }
}

const getThreatColor = (level) => {
  if (level >= 8) return 'text-red-400 bg-red-500/20'
  if (level >= 6) return 'text-amber-400 bg-amber-500/20'
  if (level >= 4) return 'text-yellow-400 bg-yellow-500/20'
  return 'text-emerald-400 bg-emerald-500/20'
}

const getWeaponInfo = (weaponId) => {
  return weaponsMap[weaponId] || { name: '未知武器', threat_level: 0, remark: '' }
}

const getAmmoInfo = (ammoId) => {
  return ammoMap[ammoId] || { name: '未知弹药', weapon_name: '', remark: '' }
}

const loadWeaponsAndAmmo = async () => {
  loading.value = true
  try {
    const result = await playerAPI.getItems(playerId)
    if (Array.isArray(result)) {
      weapons.value = result.filter(item => item.type === 'weapon').map(item => {
        const info = getWeaponInfo(item.id)
        return {
          id: item.id,
          name: info.name,
          unit: item.unit || '把',
          quantity: item.quantity,
          threat_level: info.threat_level,
          remark: info.remark,
          icon: '⚔️'
        }
      })

      ammo.value = result.filter(item => item.type === 'ammo').map(item => {
        const info = getAmmoInfo(item.id)
        return {
          id: item.id,
          name: info.name,
          unit: item.unit || '枚',
          quantity: item.quantity,
          weapon_name: info.weapon_name,
          remark: info.remark,
          icon: '🎯'
        }
      })
      console.log('Weapons/Ammo refreshed:', weapons.value.length, 'weapons', ammo.value.length, 'ammo')
    }
  } catch (error) {
    console.error('Failed to load weapons and ammo:', error)
  } finally {
    loading.value = false
  }
}

const handleVisibilityChange = () => {
  if (!document.hidden) {
    console.log('Page visible, refreshing weapons and ammo...')
    loadWeaponsAndAmmo()
  }
}

onMounted(() => {
  loadWeaponsAndAmmo()
  document.addEventListener('visibilitychange', handleVisibilityChange)
  console.log('Player weapons component mounted', { playerId })
})

onUnmounted(() => {
  document.removeEventListener('visibilitychange', handleVisibilityChange)
})

defineExpose({ refresh: loadWeaponsAndAmmo })
</script>

<template>
  <div class="max-w-6xl">
    <div class="mb-6">
      <h1 class="text-white mb-1 tracking-tight text-2xl">武器与子弹管理</h1>
      <p class="text-gray-500 text-sm">Weapons & Ammunition</p>
    </div>

    <!-- Weapons Section -->
    <div v-if="weapons.length > 0" class="mb-8">
      <h2 class="text-white text-lg mb-4 flex items-center gap-2">
        <span class="w-2 h-2 rounded-full bg-red-400"></span>
        武器
      </h2>
      
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        <div
          v-for="weapon in weapons"
          :key="weapon.id"
          class="relative bg-gradient-to-br from-[#1a2332] to-[#0f1419] border border-white/10 rounded-2xl p-5 overflow-hidden hover:border-white/20 transition-all"
        >
          <div class="absolute top-0 right-0 w-32 h-32 bg-red-500/5 rounded-full blur-2xl" />
          
          <div class="relative flex items-start gap-4">
            <div class="w-14 h-14 rounded-xl bg-gradient-to-br from-red-400/20 to-red-500/20 flex items-center justify-center text-2xl flex-shrink-0">
              {{ weapon.icon }}
            </div>
            
            <div class="flex-1 min-w-0">
              <div class="flex items-center justify-between mb-1">
                <h3 class="text-white font-medium truncate">{{ weapon.name }}</h3>
                <span class="text-xs text-gray-400 bg-white/5 px-2 py-1 rounded-full">{{ weapon.unit }}</span>
              </div>
              
              <p class="text-gray-400 text-sm mb-3">{{ weapon.remark }}</p>
              
              <div class="flex items-center justify-between">
                <div class="flex items-center gap-2">
                  <span class="text-xs text-gray-500">数量</span>
                  <span class="text-lg font-semibold text-red-400">{{ weapon.quantity }}</span>
                </div>
                <span :class="['text-xs px-2 py-1 rounded-full', getThreatColor(weapon.threat_level)]">
                  威胁值 {{ weapon.threat_level }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Ammo Section -->
    <div v-if="ammo.length > 0">
      <h2 class="text-white text-lg mb-4 flex items-center gap-2">
        <span class="w-2 h-2 rounded-full bg-amber-400"></span>
        子弹
      </h2>
      
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        <div
          v-for="item in ammo"
          :key="item.id"
          class="relative bg-gradient-to-br from-[#1a2332] to-[#0f1419] border border-white/10 rounded-2xl p-5 overflow-hidden hover:border-white/20 transition-all"
        >
          <div class="absolute top-0 right-0 w-32 h-32 bg-amber-500/5 rounded-full blur-2xl" />
          
          <div class="relative flex items-start gap-4">
            <div class="w-14 h-14 rounded-xl bg-gradient-to-br from-amber-400/20 to-amber-500/20 flex items-center justify-center text-2xl flex-shrink-0">
              {{ item.icon }}
            </div>
            
            <div class="flex-1 min-w-0">
              <div class="flex items-center justify-between mb-1">
                <h3 class="text-white font-medium truncate">{{ item.name }}</h3>
                <span class="text-xs text-gray-400 bg-white/5 px-2 py-1 rounded-full">{{ item.unit }}</span>
              </div>
              
              <p class="text-gray-400 text-sm mb-2">{{ item.remark }}</p>
              
              <div class="flex items-center gap-2 mb-2">
                <span class="text-xs text-gray-500">适用</span>
                <span class="text-xs text-amber-400 bg-amber-500/10 px-2 py-1 rounded">{{ item.weapon_name }}</span>
              </div>
              
              <div class="flex items-center gap-2">
                <span class="text-xs text-gray-500">数量</span>
                <span class="text-lg font-semibold text-amber-400">{{ item.quantity }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Empty State -->
    <div v-if="weapons.length === 0 && ammo.length === 0" class="flex flex-col items-center justify-center py-20">
      <div class="w-24 h-24 rounded-full bg-white/5 flex items-center justify-center mb-4">
        <span class="text-4xl">⚔️</span>
      </div>
      <h3 class="text-white text-lg mb-2">暂无武器</h3>
      <p class="text-gray-500 text-sm">您当前没有该类别的物资</p>
    </div>
  </div>
</template>
