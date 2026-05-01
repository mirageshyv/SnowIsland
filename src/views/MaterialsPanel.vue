<script setup>
import { ref, computed, onMounted } from 'vue'

const playerId = localStorage.getItem('playerId') || '1'

// 筛选状态
const selectedTypes = ref(['item', 'weapon', 'ammo', 'material'])

// 物资类型配置
const typeConfig = {
  item: { label: '道具', color: 'blue', icon: '📦' },
  weapon: { label: '武器', color: 'red', icon: '⚔️' },
  ammo: { label: '子弹', color: 'amber', icon: '🎯' },
  material: { label: '基础物资', color: 'emerald', icon: '🔧' }
}

// 道具数据
const itemsMap = {
  '1': [
    { id: 1, name: '医疗包', unit: '个', quantity: 3, remark: '恢复生命值', icon: '🏥' },
    { id: 2, name: '手电筒', unit: '个', quantity: 1, remark: '提供光源', icon: '🔦' },
    { id: 5, name: '防弹衣', unit: '件', quantity: 1, remark: '减少伤害', icon: '🛡️' }
  ],
  '2': [
    { id: 4, name: '哨子', unit: '个', quantity: 2, remark: '发出信号', icon: '📢' },
    { id: 7, name: '信号枪', unit: '把', quantity: 1, remark: '发射信号', icon: '🔫' }
  ],
  '3': [
    { id: 8, name: '维修工具包', unit: '个', quantity: 1, remark: '修复物品', icon: '🔧' },
    { id: 10, name: '朗姆酒', unit: '瓶', quantity: 2, remark: '恢复精力', icon: '🍾' }
  ],
  '4': [
    { id: 3, name: '手铐', unit: '个', quantity: 1, remark: '限制行动', icon: '⛓️' },
    { id: 12, name: '渔网', unit: '张', quantity: 1, remark: '捕鱼工具', icon: '🕸️' }
  ],
  '5': [
    { id: 6, name: '复合盾', unit: '个', quantity: 1, remark: '提供防护', icon: '🛡️' },
    { id: 14, name: '医用酒精', unit: '升', quantity: 1, remark: '消毒用品', icon: '🧴' }
  ]
}

// 武器数据
const weaponsMap = {
  '1': [
    { id: 1, name: '制式手枪', unit: '把', quantity: 1, threat_level: 5, remark: '标准配备', icon: '🔫' },
    { id: 3, name: '警棍', unit: '个', quantity: 1, threat_level: 3, remark: '非致命武器', icon: '🏒' }
  ],
  '2': [
    { id: 2, name: '猎枪', unit: '把', quantity: 1, threat_level: 8, remark: '威力较大', icon: '🔫' },
    { id: 6, name: '鱼叉/矛', unit: '个', quantity: 1, threat_level: 6, remark: '狩猎工具', icon: '🔱' }
  ],
  '3': [
    { id: 4, name: '刺刀', unit: '把', quantity: 1, threat_level: 4, remark: '近战武器', icon: '🗡️' },
    { id: 8, name: '十字镐', unit: '把', quantity: 1, threat_level: 4, remark: '挖掘工具', icon: '⛏️' }
  ],
  '4': [
    { id: 5, name: '水手刀', unit: '把', quantity: 1, threat_level: 3, remark: '多功能刀具', icon: '🗡️' },
    { id: 7, name: '猎弓', unit: '张', quantity: 1, threat_level: 5, remark: '远程武器', icon: '🏹' }
  ],
  '5': [
    { id: 9, name: '斧头', unit: '把', quantity: 1, threat_level: 6, remark: '砍伐工具', icon: '🪓' },
    { id: 10, name: '电锯', unit: '把', quantity: 1, threat_level: 7, remark: '切割工具', icon: '⚙️' }
  ]
}

// 子弹数据
const ammoMap = {
  '1': [
    { id: 1, name: '手枪弹', unit: '枚', quantity: 30, weapon_name: '制式手枪', remark: '制式手枪子弹', icon: '🎯' }
  ],
  '2': [
    { id: 2, name: '猎枪弹', unit: '枚', quantity: 10, weapon_name: '猎枪', remark: '猎枪子弹', icon: '🎯' },
    { id: 3, name: '信号弹', unit: '枚', quantity: 5, weapon_name: '信号枪', remark: '信号枪子弹', icon: '🎆' }
  ],
  '4': [
    { id: 4, name: '箭矢', unit: '枝', quantity: 20, weapon_name: '猎弓', remark: '猎弓箭矢', icon: '📍' }
  ],
  '3': [],
  '5': []
}

// 基础物资数据
const materialsMap = {
  '1': [
    { id: 1, name: '金属制品', unit: 'kg', quantity: 5, remark: '可用于制作工具', icon: '🔩' },
    { id: 2, name: '木材', unit: 'kg', quantity: 10, remark: '可用于建造', icon: '🪵' }
  ],
  '2': [
    { id: 3, name: '绳索', unit: '米', quantity: 20, remark: '多种用途', icon: '🪢' },
    { id: 5, name: '食物', unit: 'kg', quantity: 8, remark: '恢复饥饿', icon: '🍞' }
  ],
  '3': [
    { id: 4, name: '木板', unit: 'kg', quantity: 15, remark: '建筑材料', icon: '🪵' },
    { id: 6, name: '沥青', unit: 'kg', quantity: 5, remark: '建筑材料', icon: '🛢️' },
    { id: 9, name: '帆布', unit: '米', quantity: 10, remark: '制作帐篷', icon: '⛺' }
  ],
  '4': [
    { id: 7, name: '石料', unit: 'kg', quantity: 20, remark: '建筑材料', icon: '🪨' },
    { id: 8, name: '燃料', unit: 'kg', quantity: 10, remark: '提供能源', icon: '⛽' }
  ],
  '5': [
    { id: 10, name: '发动机', unit: '个', quantity: 1, remark: '机械动力', icon: '⚙️' },
    { id: 11, name: '螺旋桨', unit: '个', quantity: 1, remark: '船只推进', icon: '⚓' },
    { id: 12, name: '发电机', unit: '个', quantity: 1, remark: '发电设备', icon: '🔌' }
  ]
}

// 计算属性：获取当前玩家的各类物资
const currentItems = computed(() => itemsMap[playerId] || [])
const currentWeapons = computed(() => weaponsMap[playerId] || [])
const currentAmmo = computed(() => ammoMap[playerId] || [])
const currentMaterials = computed(() => materialsMap[playerId] || [])

// 计算属性：根据筛选条件显示的物资
const filteredData = computed(() => {
  const result = {}
  if (selectedTypes.value.includes('item')) {
    result.item = currentItems.value
  }
  if (selectedTypes.value.includes('weapon')) {
    result.weapon = currentWeapons.value
  }
  if (selectedTypes.value.includes('ammo')) {
    result.ammo = currentAmmo.value
  }
  if (selectedTypes.value.includes('material')) {
    result.material = currentMaterials.value
  }
  return result
})

// 计算属性：是否有任何物资
const hasAnyMaterials = computed(() => {
  return currentItems.value.length > 0 || 
         currentWeapons.value.length > 0 || 
         currentAmmo.value.length > 0 || 
         currentMaterials.value.length > 0
})

// 计算属性：当前筛选条件下是否有物资
const hasFilteredMaterials = computed(() => {
  return Object.values(filteredData.value).some(arr => arr.length > 0)
})

// 切换物资类型筛选
const toggleType = (type) => {
  const index = selectedTypes.value.indexOf(type)
  if (index > -1) {
    // 至少保留一个选中
    if (selectedTypes.value.length > 1) {
      selectedTypes.value.splice(index, 1)
    }
  } else {
    selectedTypes.value.push(type)
  }
}

// 全选/取消全选
const toggleAll = () => {
  if (selectedTypes.value.length === 4) {
    // 如果全选，则只保留第一个
    selectedTypes.value = ['item']
  } else {
    // 否则全选
    selectedTypes.value = ['item', 'weapon', 'ammo', 'material']
  }
}

// 获取威胁值颜色
const getThreatColor = (level) => {
  if (level >= 8) return 'text-red-400 bg-red-500/20'
  if (level >= 6) return 'text-amber-400 bg-amber-500/20'
  if (level >= 4) return 'text-yellow-400 bg-yellow-500/20'
  return 'text-emerald-400 bg-emerald-500/20'
}

// 获取物资背景色
const getItemBgColor = (type) => {
  const colors = {
    item: 'from-blue-400/20 to-blue-500/20',
    weapon: 'from-red-400/20 to-red-500/20',
    ammo: 'from-amber-400/20 to-amber-500/20',
    material: 'from-emerald-400/20 to-emerald-500/20'
  }
  return colors[type] || colors.item
}

onMounted(() => {
  console.log('Materials panel mounted', { playerId, selectedTypes: selectedTypes.value })
})
</script>

<template>
  <div class="max-w-7xl">
    <!-- 页面标题 -->
    <div class="mb-6">
      <h1 class="text-white mb-1 tracking-tight text-2xl">物资管理</h1>
      <p class="text-gray-500 text-sm">Materials & Equipment Management</p>
    </div>

    <!-- 筛选控制区域 -->
    <div class="bg-gradient-to-br from-[#1a2332] to-[#0f1419] border border-white/10 rounded-2xl p-5 mb-6">
      <div class="flex flex-wrap items-center gap-4">
        <span class="text-gray-400 text-sm font-medium">显示类型：</span>
        
        <!-- 全选按钮 -->
        <button
          type="button"
          class="px-4 py-2 rounded-xl text-sm font-medium transition-all"
          :class="selectedTypes.length === 4 ? 'bg-blue-500/30 text-blue-400 border border-blue-500/50' : 'bg-white/5 text-gray-400 border border-white/10 hover:bg-white/10'"
          @click="toggleAll"
        >
          全部
        </button>

        <!-- 分隔线 -->
        <div class="w-px h-6 bg-white/10"></div>

        <!-- 各类型筛选按钮 -->
        <button
          v-for="(config, type) in typeConfig"
          :key="type"
          type="button"
          class="flex items-center gap-2 px-4 py-2 rounded-xl text-sm font-medium transition-all"
          :class="selectedTypes.includes(type) ? `bg-${config.color}-500/30 text-${config.color}-400 border border-${config.color}-500/50` : 'bg-white/5 text-gray-400 border border-white/10 hover:bg-white/10'"
          @click="toggleType(type)"
        >
          <span>{{ config.icon }}</span>
          <span>{{ config.label }}</span>
          <span 
            v-if="type === 'item' && currentItems.length > 0"
            class="text-xs px-1.5 py-0.5 rounded-full bg-white/10"
          >
            {{ currentItems.length }}
          </span>
          <span 
            v-if="type === 'weapon' && currentWeapons.length > 0"
            class="text-xs px-1.5 py-0.5 rounded-full bg-white/10"
          >
            {{ currentWeapons.length }}
          </span>
          <span 
            v-if="type === 'ammo' && currentAmmo.length > 0"
            class="text-xs px-1.5 py-0.5 rounded-full bg-white/10"
          >
            {{ currentAmmo.length }}
          </span>
          <span 
            v-if="type === 'material' && currentMaterials.length > 0"
            class="text-xs px-1.5 py-0.5 rounded-full bg-white/10"
          >
            {{ currentMaterials.length }}
          </span>
        </button>
      </div>
    </div>

    <!-- 物资展示区域 -->
    <div v-if="hasFilteredMaterials" class="space-y-8">
      <!-- 道具区域 -->
      <div v-if="selectedTypes.includes('item') && filteredData.item?.length > 0">
        <div class="flex items-center gap-3 mb-4">
          <div class="w-10 h-10 rounded-xl bg-gradient-to-br from-blue-400/20 to-blue-500/20 flex items-center justify-center text-xl">
            📦
          </div>
          <div>
            <h2 class="text-white text-lg font-medium">道具</h2>
            <p class="text-gray-500 text-xs">Items & Equipment</p>
          </div>
          <div class="ml-auto text-sm text-gray-400">
            共 {{ filteredData.item.length }} 件
          </div>
        </div>
        
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
          <div
            v-for="item in filteredData.item"
            :key="item.id"
            class="relative bg-gradient-to-br from-[#1a2332] to-[#0f1419] border border-white/10 rounded-2xl p-5 overflow-hidden hover:border-white/20 transition-all group"
          >
            <div class="absolute top-0 right-0 w-32 h-32 bg-blue-500/5 rounded-full blur-2xl group-hover:bg-blue-500/10 transition-all" />
            
            <div class="relative flex items-start gap-4">
              <div class="w-14 h-14 rounded-xl bg-gradient-to-br from-blue-400/20 to-blue-500/20 flex items-center justify-center text-2xl flex-shrink-0">
                {{ item.icon }}
              </div>
              
              <div class="flex-1 min-w-0">
                <div class="flex items-center justify-between mb-1">
                  <h3 class="text-white font-medium truncate">{{ item.name }}</h3>
                  <span class="text-xs text-gray-400 bg-white/5 px-2 py-1 rounded-full">{{ item.unit }}</span>
                </div>
                
                <p class="text-gray-400 text-sm mb-3 line-clamp-2">{{ item.remark }}</p>
                
                <div class="flex items-center gap-2">
                  <span class="text-xs text-gray-500">数量</span>
                  <span class="text-lg font-semibold text-blue-400">{{ item.quantity }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 武器区域 -->
      <div v-if="selectedTypes.includes('weapon') && filteredData.weapon?.length > 0">
        <div class="flex items-center gap-3 mb-4">
          <div class="w-10 h-10 rounded-xl bg-gradient-to-br from-red-400/20 to-red-500/20 flex items-center justify-center text-xl">
            ⚔️
          </div>
          <div>
            <h2 class="text-white text-lg font-medium">武器</h2>
            <p class="text-gray-500 text-xs">Weapons</p>
          </div>
          <div class="ml-auto text-sm text-gray-400">
            共 {{ filteredData.weapon.length }} 件
          </div>
        </div>
        
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
          <div
            v-for="weapon in filteredData.weapon"
            :key="weapon.id"
            class="relative bg-gradient-to-br from-[#1a2332] to-[#0f1419] border border-white/10 rounded-2xl p-5 overflow-hidden hover:border-white/20 transition-all group"
          >
            <div class="absolute top-0 right-0 w-32 h-32 bg-red-500/5 rounded-full blur-2xl group-hover:bg-red-500/10 transition-all" />
            
            <div class="relative flex items-start gap-4">
              <div class="w-14 h-14 rounded-xl bg-gradient-to-br from-red-400/20 to-red-500/20 flex items-center justify-center text-2xl flex-shrink-0">
                {{ weapon.icon }}
              </div>
              
              <div class="flex-1 min-w-0">
                <div class="flex items-center justify-between mb-1">
                  <h3 class="text-white font-medium truncate">{{ weapon.name }}</h3>
                  <span class="text-xs text-gray-400 bg-white/5 px-2 py-1 rounded-full">{{ weapon.unit }}</span>
                </div>
                
                <p class="text-gray-400 text-sm mb-3 line-clamp-2">{{ weapon.remark }}</p>
                
                <div class="flex items-center justify-between">
                  <div class="flex items-center gap-2">
                    <span class="text-xs text-gray-500">数量</span>
                    <span class="text-lg font-semibold text-red-400">{{ weapon.quantity }}</span>
                  </div>
                  <span :class="['text-xs px-2 py-1 rounded-full', getThreatColor(weapon.threat_level)]">
                    威胁 {{ weapon.threat_level }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 子弹区域 -->
      <div v-if="selectedTypes.includes('ammo') && filteredData.ammo?.length > 0">
        <div class="flex items-center gap-3 mb-4">
          <div class="w-10 h-10 rounded-xl bg-gradient-to-br from-amber-400/20 to-amber-500/20 flex items-center justify-center text-xl">
            🎯
          </div>
          <div>
            <h2 class="text-white text-lg font-medium">子弹</h2>
            <p class="text-gray-500 text-xs">Ammunition</p>
          </div>
          <div class="ml-auto text-sm text-gray-400">
            共 {{ filteredData.ammo.length }} 种
          </div>
        </div>
        
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
          <div
            v-for="ammo in filteredData.ammo"
            :key="ammo.id"
            class="relative bg-gradient-to-br from-[#1a2332] to-[#0f1419] border border-white/10 rounded-2xl p-5 overflow-hidden hover:border-white/20 transition-all group"
          >
            <div class="absolute top-0 right-0 w-32 h-32 bg-amber-500/5 rounded-full blur-2xl group-hover:bg-amber-500/10 transition-all" />
            
            <div class="relative flex items-start gap-4">
              <div class="w-14 h-14 rounded-xl bg-gradient-to-br from-amber-400/20 to-amber-500/20 flex items-center justify-center text-2xl flex-shrink-0">
                {{ ammo.icon }}
              </div>
              
              <div class="flex-1 min-w-0">
                <div class="flex items-center justify-between mb-1">
                  <h3 class="text-white font-medium truncate">{{ ammo.name }}</h3>
                  <span class="text-xs text-gray-400 bg-white/5 px-2 py-1 rounded-full">{{ ammo.unit }}</span>
                </div>
                
                <p class="text-gray-400 text-sm mb-2 line-clamp-2">{{ ammo.remark }}</p>
                
                <div class="flex items-center gap-2 mb-2">
                  <span class="text-xs text-gray-500">适用</span>
                  <span class="text-xs text-amber-400 bg-amber-500/10 px-2 py-1 rounded">{{ ammo.weapon_name }}</span>
                </div>
                
                <div class="flex items-center gap-2">
                  <span class="text-xs text-gray-500">数量</span>
                  <span class="text-lg font-semibold text-amber-400">{{ ammo.quantity }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 基础物资区域 -->
      <div v-if="selectedTypes.includes('material') && filteredData.material?.length > 0">
        <div class="flex items-center gap-3 mb-4">
          <div class="w-10 h-10 rounded-xl bg-gradient-to-br from-emerald-400/20 to-emerald-500/20 flex items-center justify-center text-xl">
            🔧
          </div>
          <div>
            <h2 class="text-white text-lg font-medium">基础物资</h2>
            <p class="text-gray-500 text-xs">Basic Materials</p>
          </div>
          <div class="ml-auto text-sm text-gray-400">
            共 {{ filteredData.material.length }} 种
          </div>
        </div>
        
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
          <div
            v-for="material in filteredData.material"
            :key="material.id"
            class="relative bg-gradient-to-br from-[#1a2332] to-[#0f1419] border border-white/10 rounded-2xl p-5 overflow-hidden hover:border-white/20 transition-all group"
          >
            <div class="absolute top-0 right-0 w-32 h-32 bg-emerald-500/5 rounded-full blur-2xl group-hover:bg-emerald-500/10 transition-all" />
            
            <div class="relative flex items-start gap-4">
              <div class="w-14 h-14 rounded-xl bg-gradient-to-br from-emerald-400/20 to-emerald-500/20 flex items-center justify-center text-2xl flex-shrink-0">
                {{ material.icon }}
              </div>
              
              <div class="flex-1 min-w-0">
                <div class="flex items-center justify-between mb-1">
                  <h3 class="text-white font-medium truncate">{{ material.name }}</h3>
                  <span class="text-xs text-gray-400 bg-white/5 px-2 py-1 rounded-full">{{ material.unit }}</span>
                </div>
                
                <p class="text-gray-400 text-sm mb-3 line-clamp-2">{{ material.remark }}</p>
                
                <div class="flex items-center gap-2">
                  <span class="text-xs text-gray-500">数量</span>
                  <span class="text-lg font-semibold text-emerald-400">{{ material.quantity }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-else-if="!hasAnyMaterials" class="flex flex-col items-center justify-center py-20">
      <div class="w-24 h-24 rounded-full bg-white/5 flex items-center justify-center mb-4">
        <span class="text-4xl">📦</span>
      </div>
      <h3 class="text-white text-lg mb-2">暂无物资</h3>
      <p class="text-gray-500 text-sm">您当前没有任何物资</p>
    </div>

    <!-- 筛选后无数据 -->
    <div v-else class="flex flex-col items-center justify-center py-20">
      <div class="w-24 h-24 rounded-full bg-white/5 flex items-center justify-center mb-4">
        <span class="text-4xl">🔍</span>
      </div>
      <h3 class="text-white text-lg mb-2">未找到物资</h3>
      <p class="text-gray-500 text-sm">当前筛选条件下没有物资</p>
    </div>
  </div>
</template>
