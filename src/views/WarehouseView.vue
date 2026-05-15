<script setup>
import { ref, computed, onMounted } from 'vue';
import { warehouseAPI } from '@/utils/api.js';
import { getMaterialImageUrlOrDefault, getTypeTabImage, preloadMaterialImages } from '@/data/gameData.js';

const userRole = (localStorage.getItem('userRole') || '').toLowerCase();
const playerId = localStorage.getItem('playerId') || '';
const isDm = userRole === 'dm';

const warehouses = ref([]);
const currentWarehouse = ref(null);
const currentWarehouseName = ref('');
const stockItems = ref([]);
const loading = ref(true);
const stockLoading = ref(false);
const searchQuery = ref('');
const filterType = ref('all');
const selectedItem = ref(null);
const editingItem = ref(null);
const editQuantity = ref(0);

const typeLabels = { item: '道具', weapon: '武器', ammo: '弹药', material: '材料' };

const accessibleWarehouses = computed(() => {
  if (isDm) return warehouses.value;
  return warehouses.value.filter(w => w.accessible);
});

const hasAnyAccess = computed(() => accessibleWarehouses.value.length > 0);

const displayRows = computed(() => {
  let items = stockItems.value.map(item => ({
    ...item,
    imageUrl: getMaterialImageUrlOrDefault(item.itemType, item.itemId)
  }));
  if (filterType.value !== 'all') {
    items = items.filter(i => i.itemType === filterType.value);
  }
  if (searchQuery.value.trim()) {
    const q = searchQuery.value.trim().toLowerCase();
    items = items.filter(i => (i.name || '').toLowerCase().includes(q));
  }
  return items;
});

const groupedRows = computed(() => {
  const groups = {};
  const order = ['material', 'weapon', 'ammo', 'item'];
  for (const item of displayRows.value) {
    const type = item.itemType;
    if (!groups[type]) groups[type] = [];
    groups[type].push(item);
  }
  const sorted = {};
  for (const t of order) {
    if (groups[t]) sorted[t] = groups[t];
  }
  return sorted;
});

const selectedRow = computed(() => {
  if (!selectedItem.value) return null;
  return displayRows.value.find(i => i.itemType === selectedItem.value.itemType && i.itemId === selectedItem.value.itemId);
});

function selectItem(item) {
  selectedItem.value = { itemType: item.itemType, itemId: item.itemId };
  editingItem.value = null;
}

async function fetchWarehouses() {
  loading.value = true;
  try {
    warehouses.value = await warehouseAPI.getAccessibleWarehouses(playerId, userRole);
    if (accessibleWarehouses.value.length > 0 && !currentWarehouse.value) {
      await selectWarehouse(accessibleWarehouses.value[0].warehouseKey);
    }
  } catch (e) {
    console.error('获取仓库列表失败:', e);
  } finally {
    loading.value = false;
  }
}

async function selectWarehouse(warehouseKey) {
  currentWarehouse.value = warehouseKey;
  selectedItem.value = null;
  editingItem.value = null;
  stockLoading.value = true;
  try {
    const result = await warehouseAPI.getWarehouseStock(warehouseKey, playerId, userRole);
    if (result.success) {
      stockItems.value = result.items || [];
      currentWarehouseName.value = result.warehouseName || '';
    } else {
      stockItems.value = [];
      currentWarehouseName.value = '';
    }
  } catch (e) {
    console.error('获取仓库库存失败:', e);
    stockItems.value = [];
  } finally {
    stockLoading.value = false;
  }
}

function startEdit(item) {
  editingItem.value = { itemType: item.itemType, itemId: item.itemId };
  editQuantity.value = item.quantity;
}

function cancelEdit() {
  editingItem.value = null;
  editQuantity.value = 0;
}

async function saveEdit() {
  if (!editingItem.value || !isDm) return;
  try {
    const result = await warehouseAPI.updateWarehouseStock(
      currentWarehouse.value,
      editingItem.value.itemType,
      editingItem.value.itemId,
      editQuantity.value,
      userRole
    );
    if (result.success) {
      await selectWarehouse(currentWarehouse.value);
      editingItem.value = null;
    } else {
      alert(result.message);
    }
  } catch (e) {
    console.error('更新库存失败:', e);
  }
}

const warehouseIcons = {
  general: '📦', fuel: '⛽', armory: '⚔️', dock: '⚓', rebel: '🏴', ark: '🚢'
};

onMounted(() => {
  preloadMaterialImages();
  fetchWarehouses();
});
</script>

<template>
  <div>
    <div v-if="loading" class="flex items-center justify-center py-20">
      <div class="w-12 h-12 border-4 border-cyan-500 border-t-transparent rounded-full animate-spin"></div>
    </div>

    <div v-else-if="!hasAnyAccess" class="flex flex-col items-center justify-center py-20">
      <div class="w-28 h-28 bg-gradient-to-br from-gray-700/30 to-gray-800/20 rounded-full flex items-center justify-center mb-6 border border-white/10">
        <svg class="w-14 h-14 text-gray-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"/>
        </svg>
      </div>
      <h2 class="text-xl text-gray-300 font-semibold mb-2">您未持有仓库钥匙</h2>
      <p class="text-gray-500 text-sm">获得仓库钥匙后即可访问对应仓库</p>
    </div>

    <div v-else>
      <div class="flex flex-wrap gap-2 mb-6">
        <button
          v-for="wh in accessibleWarehouses"
          :key="wh.warehouseKey"
          @click="selectWarehouse(wh.warehouseKey)"
          :class="[
            'px-4 py-2.5 rounded-xl text-sm font-medium transition-all duration-200 border',
            currentWarehouse === wh.warehouseKey
              ? 'bg-cyan-600/20 border-cyan-500/50 text-cyan-300 shadow-lg shadow-cyan-500/10'
              : 'bg-white/5 border-white/10 text-gray-400 hover:bg-white/10 hover:text-gray-300'
          ]"
        >
          <span class="mr-1.5">{{ warehouseIcons[wh.warehouseKey] || '📦' }}</span>
          {{ wh.warehouseName }}
        </button>
      </div>

      <div class="bg-gradient-to-br from-[#1a2332] to-[#0f1419] border border-white/10 rounded-3xl p-5 md:p-6">
        <div class="flex flex-col sm:flex-row gap-3 items-start sm:items-center justify-between mb-5">
          <div class="flex items-center gap-3">
            <h2 class="text-2xl text-white font-medium tracking-tight">{{ currentWarehouseName }}</h2>
            <span class="text-gray-500 text-sm">({{ stockItems.length }} 种物品)</span>
          </div>
          <div class="flex gap-2 w-full sm:w-auto">
            <input
              v-model="searchQuery"
              type="text"
              placeholder="搜索物品..."
              class="flex-1 sm:w-44 bg-black/30 border border-white/10 rounded-lg px-3 py-1.5 text-sm text-gray-200 placeholder-gray-500 focus:outline-none focus:border-cyan-500/50"
            />
            <select
              v-model="filterType"
              class="bg-black/30 border border-white/10 rounded-lg px-3 py-1.5 text-sm text-gray-200 focus:outline-none focus:border-cyan-500/50"
            >
              <option value="all">全部</option>
              <option value="item">道具</option>
              <option value="weapon">武器</option>
              <option value="ammo">弹药</option>
              <option value="material">材料</option>
            </select>
          </div>
        </div>

        <div v-if="stockLoading" class="flex items-center justify-center py-16">
          <div class="w-8 h-8 border-3 border-cyan-500 border-t-transparent rounded-full animate-spin"></div>
        </div>

        <div v-else-if="displayRows.length === 0" class="flex flex-col items-center justify-center py-16">
          <svg class="w-12 h-12 text-gray-600 mb-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4"/>
          </svg>
          <p class="text-gray-500">{{ searchQuery || filterType !== 'all' ? '没有匹配的物品' : '仓库为空' }}</p>
        </div>

        <div v-else class="flex flex-col lg:flex-row gap-6 lg:gap-8">
          <div class="flex-1 min-w-0">
            <div v-for="(items, type) in groupedRows" :key="type" class="mb-4 last:mb-0">
              <div class="flex items-center gap-2 mb-2">
                <img :src="getTypeTabImage(type)" class="w-4 h-4 object-contain" />
                <span class="text-xs font-semibold text-gray-400 uppercase tracking-wider">{{ typeLabels[type] }}</span>
                <span class="text-gray-600 text-xs">({{ items.length }})</span>
              </div>
              <div class="grid grid-cols-4 sm:grid-cols-5 md:grid-cols-6 gap-2">
                <button
                  v-for="item in items"
                  :key="`${item.itemType}-${item.itemId}`"
                  type="button"
                  class="relative aspect-square rounded-xl border-2 flex flex-col items-center justify-center p-1 transition-all duration-200 group"
                  :class="selectedItem && selectedItem.itemType === item.itemType && selectedItem.itemId === item.itemId
                    ? 'border-cyan-500 bg-white/10'
                    : 'border-white/10 bg-black/20 hover:border-white/20 hover:bg-white/5'"
                  @click="selectItem(item)"
                >
                  <img :src="item.imageUrl" :alt="item.name" class="w-[68%] h-[68%] object-contain pointer-events-none select-none" />
                  <span class="absolute bottom-0.5 right-1.5 text-white text-[10px] sm:text-xs font-semibold tabular-nums drop-shadow-lg">
                    {{ item.quantity }}
                  </span>
                </button>
              </div>
            </div>
          </div>

          <div class="w-full lg:w-72 shrink-0 rounded-2xl border border-white/10 bg-black/25 p-5 flex flex-col min-h-[260px]">
            <template v-if="selectedRow">
              <div class="flex justify-center mb-3">
                <img :src="selectedRow.imageUrl" :alt="selectedRow.name" class="w-24 h-24 object-contain" />
              </div>
              <div class="flex items-center justify-center gap-2 mb-2 flex-wrap">
                <h3 class="text-xl text-white font-medium text-center">{{ selectedRow.name }}</h3>
                <span class="px-2 py-0.5 rounded-full text-[10px] font-medium bg-white/10 text-gray-400 border border-white/10">
                  {{ typeLabels[selectedRow.itemType] }}
                </span>
              </div>

              <template v-if="editingItem && editingItem.itemType === selectedRow.itemType && editingItem.itemId === selectedRow.itemId">
                <div class="flex items-center justify-center gap-2 my-3">
                  <span class="text-gray-400 text-sm">数量：</span>
                  <input
                    v-model.number="editQuantity"
                    type="number"
                    min="0"
                    class="w-20 bg-black/30 border border-cyan-500/50 rounded px-2 py-1 text-sm text-center text-white focus:outline-none"
                    @keyup.enter="saveEdit"
                    @keyup.escape="cancelEdit"
                  />
                  <span class="text-gray-500 text-sm">{{ selectedRow.unit }}</span>
                </div>
                <div class="flex justify-center gap-3 mt-2">
                  <button @click="saveEdit" class="px-4 py-1.5 bg-cyan-600/30 text-cyan-300 rounded-lg text-sm hover:bg-cyan-600/40 transition-colors">保存</button>
                  <button @click="cancelEdit" class="px-4 py-1.5 bg-white/5 text-gray-400 rounded-lg text-sm hover:bg-white/10 transition-colors">取消</button>
                </div>
              </template>
              <template v-else>
                <p class="text-cyan-400 text-center text-sm mb-3 tabular-nums">
                  数量：{{ selectedRow.quantity }} {{ selectedRow.unit }}
                </p>
                <button
                  v-if="isDm"
                  @click="startEdit(selectedRow)"
                  class="w-full py-2 bg-white/5 text-gray-400 rounded-lg text-sm hover:bg-white/10 hover:text-gray-300 transition-colors mb-3"
                >
                  编辑数量
                </button>
              </template>

              <div class="border-t border-white/5 pt-3 mt-auto">
                <p class="text-gray-500 text-xs leading-relaxed">
                  {{ selectedRow.name }} — {{ typeLabels[selectedRow.itemType] }}类物资，存储于{{ currentWarehouseName }}。
                </p>
              </div>
            </template>
            <div v-else class="flex items-center justify-center flex-1 text-gray-600 text-sm text-center">
              点击左侧物品查看详情
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
