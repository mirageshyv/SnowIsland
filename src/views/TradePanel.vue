<script setup>
import { ref, computed, onMounted } from 'vue'
import { playerAPI, tradeAPI } from '../utils/api.js'
import { getMaterialImageUrlOrDefault } from '../data/gameData.js'

const tradeTypeLabel = (t) =>
  ({
    item: '道具',
    weapon: '武器',
    ammo: '弹药',
    material: '其他物资',
    food: '食物',
    energy: '燃料'
  }[String(t || '').toLowerCase()] || String(t || ''))

const playerId = parseInt(localStorage.getItem('playerId') || '1')
const currentPlayerName = ref('')
const trades = ref([])
const players = ref([])
const loading = ref(false)

const playerItemsMap = {
  1: [
    { id: 1, type: 'item', name: '医疗包', unit: '个', quantity: 3, icon: '🏥' },
    { id: 2, type: 'item', name: '手电筒', unit: '个', quantity: 1, icon: '🔦' },
    { id: 5, type: 'item', name: '防弹衣', unit: '件', quantity: 1, icon: '🛡️' },
    { id: 1, type: 'weapon', name: '制式手枪', unit: '把', quantity: 1, icon: '🔫' },
    { id: 3, type: 'weapon', name: '警棍', unit: '把', quantity: 1, icon: '⚔️' },
    { id: 11, type: 'weapon', name: '手术刀', unit: '把', quantity: 1, icon: '🔪' },
    { id: 1, type: 'ammo', name: '手枪弹', unit: '枚', quantity: 30, icon: '🎯' },
    { id: 1, type: 'material', name: '金属制品', unit: 'kg', quantity: 5, icon: '🔩' },
    { id: 2, type: 'material', name: '木材', unit: 'kg', quantity: 10, icon: '🌲' }
  ],
  2: [
    { id: 4, type: 'item', name: '哨子', unit: '个', quantity: 2, icon: '📢' },
    { id: 7, type: 'item', name: '信号枪', unit: '把', quantity: 1, icon: '🔴' },
    { id: 2, type: 'weapon', name: '猎枪', unit: '把', quantity: 1, icon: '🔫' },
    { id: 6, type: 'weapon', name: '鱼叉/矛', unit: '个', quantity: 1, icon: '⚔️' },
    { id: 2, type: 'ammo', name: '猎枪弹', unit: '枚', quantity: 10, icon: '🎯' },
    { id: 3, type: 'ammo', name: '信号弹', unit: '枚', quantity: 5, icon: '🔴' },
    { id: 3, type: 'material', name: '绳索', unit: '米', quantity: 20, icon: '🪢' },
    { id: 5, type: 'material', name: '食物', unit: 'kg', quantity: 8, icon: '🍞' }
  ],
  3: [
    { id: 8, type: 'item', name: '维修工具包', unit: '个', quantity: 1, icon: '🔧' },
    { id: 10, type: 'item', name: '朗姆酒', unit: '瓶', quantity: 2, icon: '🍾' },
    { id: 4, type: 'weapon', name: '刺刀', unit: '把', quantity: 1, icon: '⚔️' },
    { id: 8, type: 'weapon', name: '十字镐', unit: '把', quantity: 1, icon: '⛏️' },
    { id: 4, type: 'material', name: '木板', unit: 'kg', quantity: 15, icon: '🪵' },
    { id: 6, type: 'material', name: '沥青', unit: 'kg', quantity: 5, icon: '🔧' },
    { id: 9, type: 'material', name: '帆布', unit: '米', quantity: 10, icon: '🧵' }
  ],
  4: [
    { id: 3, type: 'item', name: '手铐', unit: '个', quantity: 1, icon: '⛓️' },
    { id: 12, type: 'item', name: '渔网', unit: '张', quantity: 1, icon: '🎣' },
    { id: 5, type: 'weapon', name: '水手刀', unit: '把', quantity: 1, icon: '🔪' },
    { id: 7, type: 'weapon', name: '猎弓', unit: '张', quantity: 1, icon: '🏹' },
    { id: 4, type: 'ammo', name: '箭矢', unit: '枝', quantity: 20, icon: '🎯' },
    { id: 7, type: 'material', name: '石料', unit: 'kg', quantity: 20, icon: '🪨' },
    { id: 8, type: 'material', name: '燃料', unit: 'kg', quantity: 10, icon: '⛽' }
  ],
  5: [
    { id: 6, type: 'item', name: '复合盾', unit: '个', quantity: 1, icon: '🛡️' },
    { id: 14, type: 'item', name: '医用酒精', unit: '升', quantity: 1, icon: '🧪' },
    { id: 9, type: 'weapon', name: '斧头', unit: '把', quantity: 1, icon: '🪓' },
    { id: 10, type: 'weapon', name: '电锯', unit: '把', quantity: 1, icon: '⚙️' },
    { id: 10, type: 'material', name: '发动机', unit: '个', quantity: 1, icon: '🔧' },
    { id: 11, type: 'material', name: '螺旋桨', unit: '个', quantity: 1, icon: '🌀' },
    { id: 12, type: 'material', name: '发电机', unit: '个', quantity: 1, icon: '⚡' }
  ]
}

const allMaterialsMap = {
  item: [
    { id: 1, name: '医疗包', unit: '个', icon: '🏥' },
    { id: 2, name: '手电筒', unit: '个', icon: '🔦' },
    { id: 3, name: '手铐', unit: '个', icon: '⛓️' },
    { id: 4, name: '哨子', unit: '个', icon: '📢' },
    { id: 5, name: '防弹衣', unit: '件', icon: '🛡️' },
    { id: 6, name: '复合盾', unit: '个', icon: '🛡️' },
    { id: 7, name: '信号枪', unit: '把', icon: '🔴' },
    { id: 8, name: '维修工具包', unit: '个', icon: '🔧' },
    { id: 9, name: '协议书', unit: '个', icon: '📜' },
    { id: 10, name: '朗姆酒', unit: '瓶', icon: '🍾' },
    { id: 11, name: '草药', unit: '个', icon: '🌿' },
    { id: 12, name: '渔网', unit: '张', icon: '🎣' },
    { id: 13, name: '蜡烛', unit: '根', icon: '🕯️' },
    { id: 14, name: '医用酒精', unit: '升', icon: '🧪' },
    { id: 15, name: '火柴', unit: '盒', icon: '🔥' },
    { id: 16, name: '铅笔', unit: '盒', icon: '✏️' },
    { id: 17, name: '破损海图', unit: '张', icon: '🗺️' },
    { id: 18, name: '便当', unit: '份', icon: '🍱' },
    { id: 19, name: '仓库钥匙', unit: '把', icon: '🔑' },
    { id: 20, name: '燃料仓库钥匙', unit: '把', icon: '🔑' },
    { id: 21, name: '镇武库钥匙', unit: '把', icon: '🔑' },
    { id: 22, name: '码头集购站钥匙', unit: '把', icon: '🔑' }
  ],
  weapon: [
    { id: 1, name: '制式手枪', unit: '把', icon: '🔫' },
    { id: 2, name: '猎枪', unit: '把', icon: '🔫' },
    { id: 3, name: '警棍', unit: '把', icon: '⚔️' },
    { id: 4, name: '刺刀', unit: '把', icon: '⚔️' },
    { id: 5, name: '水手刀', unit: '把', icon: '🔪' },
    { id: 6, name: '鱼叉/矛', unit: '个', icon: '⚔️' },
    { id: 7, name: '猎弓', unit: '张', icon: '🏹' },
    { id: 8, name: '十字镐', unit: '把', icon: '⛏️' },
    { id: 9, name: '斧头', unit: '把', icon: '🪓' },
    { id: 10, name: '电锯', unit: '把', icon: '⚙️' },
    { id: 11, name: '手术刀', unit: '把', icon: '🔪' },
    { id: 12, name: '炸药', unit: 'kg', icon: '💣' }
  ],
  ammo: [
    { id: 1, name: '手枪弹', unit: '枚', icon: '🎯' },
    { id: 2, name: '猎枪弹', unit: '枚', icon: '🎯' },
    { id: 3, name: '信号弹', unit: '枚', icon: '🔴' },
    { id: 4, name: '箭矢', unit: '枝', icon: '🎯' }
  ],
  material: [
    { id: 1, name: '金属制品', unit: 'kg', icon: '🔩' },
    { id: 2, name: '木材', unit: 'kg', icon: '🌲' },
    { id: 3, name: '绳索', unit: '米', icon: '🪢' },
    { id: 4, name: '木板', unit: 'kg', icon: '🪵' },
    { id: 5, name: '食物', unit: 'kg', icon: '🍞' },
    { id: 6, name: '沥青', unit: 'kg', icon: '🔧' },
    { id: 7, name: '石料', unit: 'kg', icon: '🪨' },
    { id: 8, name: '燃料', unit: 'kg', icon: '⛽' },
    { id: 9, name: '帆布', unit: '米', icon: '🧵' },
    { id: 10, name: '发动机', unit: '个', icon: '🔧' },
    { id: 11, name: '螺旋桨', unit: '个', icon: '🌀' },
    { id: 12, name: '发电机', unit: '个', icon: '⚡' }
  ]
}

const takePaletteTab = ref('item')
const takePaletteTabs = [
  { key: 'item', label: '道具' },
  { key: 'weapon', label: '武器' },
  { key: 'ammo', label: '弹药' },
  { key: 'material', label: '其他物资' },
  { key: 'food', label: '食物' },
  { key: 'energy', label: '燃料' }
]

const takePaletteRows = (() => {
  const keys = ['item', 'weapon', 'ammo', 'material']
  const o = {}
  for (const type of keys) {
    o[type] = allMaterialsMap[type].map((item) => ({
      ...item,
      itemType: type,
      imageUrl: getMaterialImageUrlOrDefault(type, item.id)
    }))
  }
  o.food = []
  o.energy = []
  return o
})()

const visibleTakePalette = computed(() => takePaletteRows[takePaletteTab.value] || [])

const currentPlayerItems = ref([])
const playerSupplies = ref(null)

const compactSupplyRows = computed(() => {
  const food = (playerSupplies.value?.foodSupply?.items || [])
    .filter((row) => Number(row.quantity) > 0)
    .map((row) => ({
      id: row.id,
      itemKey: row.id,
      itemType: 'food',
      name: row.name,
      unit: row.unit,
      quantity: Number(row.quantity),
      kcalPerUnit: Number(row.kcalPerUnit || 0),
      lineKcal: Number(row.lineKcal || 0),
      imageUrl: getMaterialImageUrlOrDefault('material', 5)
    }))
  const energy = (playerSupplies.value?.energyReserve?.items || [])
    .filter((row) => Number(row.quantity) > 0)
    .map((row) => ({
      id: row.id,
      itemKey: row.id,
      itemType: 'energy',
      name: row.name,
      unit: row.unit,
      quantity: Number(row.quantity),
      kcalPerUnit: Number(row.kcalPerUnit || 0),
      lineKcal: Number(row.lineKcal || 0),
      imageUrl: getMaterialImageUrlOrDefault('material', 8)
    }))
  return [...food, ...energy]
})

const giveInventoryItemRows = computed(() =>
  currentPlayerItems.value.map((item) => ({
    ...item,
    imageUrl: getMaterialImageUrlOrDefault(item.type, item.id)
  }))
)

const giveInventorySupplyRows = computed(() => compactSupplyRows.value)

const isSupplyType = (type) => type === 'food' || type === 'energy'

const formatChineseUnit = (unit) => ({ kg: '千克', portion: '份', L: '升' }[unit] || unit || '单位')

const giveSelectedItemRows = computed(() => giveItems.value.filter((i) => !isSupplyType(i.itemType)))
const giveSelectedSupplyRows = computed(() => giveItems.value.filter((i) => isSupplyType(i.itemType)))
const takeSelectedItemRows = computed(() => takeItems.value.filter((i) => !isSupplyType(i.itemType)))
const takeSelectedSupplyRows = computed(() => takeItems.value.filter((i) => isSupplyType(i.itemType)))

const tradeKcalSummary = computed(() => {
  const sumByType = (rows, type) =>
    rows
      .filter((i) => i.itemType === type)
      .reduce((sum, i) => sum + Number(i.kcalPerUnit || 0) * Number(i.quantity || 0), 0)
  const foodDelta = sumByType(takeItems.value, 'food') - sumByType(giveItems.value, 'food')
  const energyDelta = sumByType(takeItems.value, 'energy') - sumByType(giveItems.value, 'energy')
  return { foodDelta, energyDelta }
})

const loadCurrentPlayerItems = async () => {
  try {
    const items = await playerAPI.getItems(playerId)
    if (Array.isArray(items) && items.length > 0) {
      currentPlayerItems.value = items.map(item => ({
        id: item.id,
        type: item.type,
        name: item.name,
        unit: item.unit,
        quantity: item.quantity
      }))
      console.log('Loaded player items from API:', currentPlayerItems.value)
    } else {
      console.log('API returned empty or invalid data, using fallback playerItemsMap')
      currentPlayerItems.value = playerItemsMap[playerId] || []
    }
  } catch (error) {
    console.log('Failed to load player items, using fallback due to:', error.message)
    currentPlayerItems.value = playerItemsMap[playerId] || []
  }
}

const loadPlayerSupplies = async () => {
  try {
    const result = await playerAPI.getSupplies(playerId)
    if (result && result.success) {
      playerSupplies.value = result
      takePaletteRows.food = (result.foodSupply?.items || []).map((row) => ({
        id: row.id,
        itemKey: row.id,
        itemType: 'food',
        name: row.name,
        unit: row.unit,
        quantity: Number(row.quantity || 0),
        kcalPerUnit: Number(row.kcalPerUnit || 0),
        imageUrl: getMaterialImageUrlOrDefault('material', 5)
      }))
      takePaletteRows.energy = (result.energyReserve?.items || []).map((row) => ({
        id: row.id,
        itemKey: row.id,
        itemType: 'energy',
        name: row.name,
        unit: row.unit,
        quantity: Number(row.quantity || 0),
        kcalPerUnit: Number(row.kcalPerUnit || 0),
        imageUrl: getMaterialImageUrlOrDefault('material', 8)
      }))
    } else {
      playerSupplies.value = null
      takePaletteRows.food = []
      takePaletteRows.energy = []
    }
  } catch (error) {
    console.log('Failed to load player supplies:', error.message)
    playerSupplies.value = null
    takePaletteRows.food = []
    takePaletteRows.energy = []
  }
}

const otherPlayers = computed(() => {
  return players.value.filter(p => p.id !== playerId)
})

const pendingTradesCount = computed(() => {
  return trades.value.filter(t => t.toPlayerId === playerId && t.status === 'pending').length
})

const incomingTrades = computed(() => {
  return trades.value
    .filter(t => t.toPlayerId === playerId)
    .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
})

const pendingTrades = computed(() => {
  return incomingTrades.value.filter(t => t.status === 'pending')
})

const showTradeModal = ref(false)
const tradeModalStep = ref(1)
const selectedTargetPlayer = ref('')
const giveItems = ref([])
const takeItems = ref([])
const tradeRemark = ref('')

const showDetailModal = ref(false)
const selectedTrade = ref(null)

const providedItems = computed(() => {
  if (!selectedTrade.value || !selectedTrade.value.items || !Array.isArray(selectedTrade.value.items)) {
    console.log('providedItems: empty - selectedTrade or items missing')
    return []
  }
  const fromId = parseInt(selectedTrade.value.fromPlayerId)
  const toId = parseInt(selectedTrade.value.toPlayerId)
  console.log('providedItems - playerId:', playerId, 'fromId:', fromId, 'toId:', toId)
  const result = selectedTrade.value.items.filter(i => {
    const isGive = i.direction === 'give'
    const isTake = i.direction === 'take'
    const isInitiator = fromId === playerId
    const isRecipient = toId === playerId
    console.log('item:', i.name, 'direction:', i.direction, 'isInitiator:', isInitiator, 'isRecipient:', isRecipient)
    return (isInitiator && isGive) || (isRecipient && isTake)
  })
  console.log('providedItems result:', JSON.stringify(result))
  return result
})

const receivedItems = computed(() => {
  if (!selectedTrade.value || !selectedTrade.value.items || !Array.isArray(selectedTrade.value.items)) {
    console.log('receivedItems: empty - selectedTrade or items missing')
    return []
  }
  const fromId = parseInt(selectedTrade.value.fromPlayerId)
  const toId = parseInt(selectedTrade.value.toPlayerId)
  console.log('receivedItems - playerId:', playerId, 'fromId:', fromId, 'toId:', toId)
  const result = selectedTrade.value.items.filter(i => {
    const isGive = i.direction === 'give'
    const isTake = i.direction === 'take'
    const isInitiator = fromId === playerId
    const isRecipient = toId === playerId
    console.log('item:', i.name, 'direction:', i.direction, 'isInitiator:', isInitiator, 'isRecipient:', isRecipient)
    return (isInitiator && isTake) || (isRecipient && isGive)
  })
  console.log('receivedItems result:', JSON.stringify(result))
  return result
})

function tradeRowWithThumb(item) {
  const fallbackType = item.itemType === 'food' ? 'material' : item.itemType === 'energy' ? 'material' : item.itemType
  const fallbackId = item.itemType === 'food' ? 5 : item.itemType === 'energy' ? 8 : item.itemId
  return {
    ...item,
    imageUrl: item.imageUrl ?? getMaterialImageUrlOrDefault(fallbackType, fallbackId)
  }
}

const detailTradeItems = computed(() => {
  const items = selectedTrade.value?.items
  if (!Array.isArray(items)) return []
  return items.map(tradeRowWithThumb)
})

const detailTradeItemRows = computed(() => detailTradeItems.value.filter((i) => !isSupplyType(i.itemType)))
const detailTradeSupplyRows = computed(() => detailTradeItems.value.filter((i) => isSupplyType(i.itemType)))

const providedItemsDisplay = computed(() => providedItems.value.map(tradeRowWithThumb))

const receivedItemsDisplay = computed(() => receivedItems.value.map(tradeRowWithThumb))

const providedItemRows = computed(() => providedItemsDisplay.value.filter((i) => !isSupplyType(i.itemType)))
const providedSupplyRows = computed(() => providedItemsDisplay.value.filter((i) => isSupplyType(i.itemType)))
const receivedItemRows = computed(() => receivedItemsDisplay.value.filter((i) => !isSupplyType(i.itemType)))
const receivedSupplyRows = computed(() => receivedItemsDisplay.value.filter((i) => isSupplyType(i.itemType)))

const isSameTradeEntry = (a, b) => {
  if (!a || !b) return false
  if (a.itemType !== b.itemType) return false
  const aKey = a.itemKey ?? null
  const bKey = b.itemKey ?? null
  if (aKey !== null && bKey !== null) return aKey === bKey
  return a.itemId === b.itemId
}

const selectedCountIn = (rows, probe) =>
  rows.reduce((sum, row) => (isSameTradeEntry(row, probe) ? sum + Number(row.quantity || 0) : sum), 0)

const selectedGiveCount = (row) =>
  selectedCountIn(giveItems.value, {
    itemType: row.itemType || row.type,
    itemId: typeof row.id === 'number' ? row.id : null,
    itemKey: row.itemKey ?? (typeof row.id === 'string' ? row.id : null)
  })

const selectedTakeCount = (row) =>
  selectedCountIn(takeItems.value, {
    itemType: row.itemType,
    itemId: typeof row.id === 'number' ? row.id : null,
    itemKey: row.itemKey ?? (typeof row.id === 'string' ? row.id : null)
  })

const openTradeModal = () => {
  tradeModalStep.value = 1
  showTradeModal.value = true
}

const closeTradeModal = () => {
  tradeModalStep.value = 1
  showTradeModal.value = false
}

const goPreviewStep = () => {
  if (!selectedTargetPlayer.value) {
    alert('请选择交易对象')
    return
  }
  if (giveItems.value.length === 0 && takeItems.value.length === 0) {
    alert('请至少添加一件给予或索取的物品')
    return
  }
  tradeModalStep.value = 2
}

const detailKcalSummary = computed(() => {
  const sumByType = (rows, type) =>
    rows
      .filter((i) => i.itemType === type)
      .reduce((sum, i) => sum + Number(i.lineKcal || (Number(i.kcalPerUnit || 0) * Number(i.quantity || 0))), 0)
  const foodDelta = sumByType(receivedItemsDisplay.value, 'food') - sumByType(providedItemsDisplay.value, 'food')
  const energyDelta = sumByType(receivedItemsDisplay.value, 'energy') - sumByType(providedItemsDisplay.value, 'energy')
  return { foodDelta, energyDelta }
})

const addGiveItem = (item) => {
  const itemType = item.itemType || item.type
  const itemId = typeof item.id === 'number' ? item.id : null
  const itemKey = item.itemKey ?? (typeof item.id === 'string' ? item.id : null)
  const existItem = giveItems.value.find((i) =>
    isSameTradeEntry(
      { itemType: i.itemType, itemId: i.itemId, itemKey: i.itemKey },
      { itemType, itemId, itemKey }
    )
  )
  if (existItem) {
    if (existItem.quantity < item.quantity) {
      existItem.quantity++
    }
  } else {
    giveItems.value.push({
      itemId,
      itemKey,
      itemType,
      name: item.name,
      unit: item.unit,
      kcalPerUnit: Number(item.kcalPerUnit || 0),
      imageUrl: item.imageUrl ?? getMaterialImageUrlOrDefault(itemType, item.id),
      quantity: 1,
      maxQuantity: item.quantity
    })
  }
}

const removeGiveItem = (index) => {
  giveItems.value.splice(index, 1)
}

const addTakeItem = (row) => {
  const rowItemId = typeof row.id === 'number' ? row.id : null
  const rowItemKey = row.itemKey ?? (typeof row.id === 'string' ? row.id : null)
  const existItem = takeItems.value.find((i) =>
    isSameTradeEntry(
      { itemType: i.itemType, itemId: i.itemId, itemKey: i.itemKey },
      { itemType: row.itemType, itemId: rowItemId, itemKey: rowItemKey }
    )
  )
  if (existItem) {
    if (isSupplyType(row.itemType)) {
      existItem.quantity += 1
    } else {
      existItem.quantity = Math.min(500, Number(existItem.quantity || 0) + 1)
    }
  } else {
    takeItems.value.push({
      itemId: typeof row.id === 'number' ? row.id : null,
      itemKey: row.itemKey ?? (typeof row.id === 'string' ? row.id : null),
      itemType: row.itemType,
      name: row.name,
      unit: row.unit,
      kcalPerUnit: Number(row.kcalPerUnit || 0),
      imageUrl: row.imageUrl ?? getMaterialImageUrlOrDefault(row.itemType, row.id),
      quantity: 1,
      maxQuantity: isSupplyType(row.itemType) ? null : 500
    })
  }
}

const removeTakeItem = (index) => {
  takeItems.value.splice(index, 1)
}

const normalizeTakeQuantity = (item) => {
  const val = Math.max(1, Number(item.quantity || 1))
  if (isSupplyType(item.itemType)) {
    item.quantity = val
    return
  }
  item.quantity = Math.min(500, val)
}

const sendTrade = async () => {
  if (!selectedTargetPlayer.value) {
    alert('请选择交易对象')
    return
  }
  if (giveItems.value.length === 0 && takeItems.value.length === 0) {
    alert('请至少添加一件给予或索取的物品')
    return
  }

  loading.value = true
  
  try {
    const items = [
      ...giveItems.value.map(i => ({
        itemType: i.itemType,
        itemId: i.itemId,
        itemKey: i.itemKey ?? null,
        quantity: i.quantity,
        direction: 'give'
      })),
      ...takeItems.value.map(i => ({
        itemType: i.itemType,
        itemId: i.itemId,
        itemKey: i.itemKey ?? null,
        quantity: i.quantity,
        direction: 'take'
      }))
    ]

    const result = await tradeAPI.create({
      fromPlayerId: playerId,
      toPlayerId: parseInt(selectedTargetPlayer.value),
      remark: tradeRemark.value,
      items
    })

    if (result.success) {
      alert('交易请求已发送')
      await loadTrades()
      await loadCurrentPlayerItems()
      await loadPlayerSupplies()
      selectedTargetPlayer.value = ''
      giveItems.value = []
      takeItems.value = []
      tradeRemark.value = ''
      showTradeModal.value = false
      tradeModalStep.value = 1
    } else {
      alert(result.message || '发送交易失败')
    }
  } catch (error) {
    alert('发送交易失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

const acceptTrade = async (trade) => {
  loading.value = true
  try {
    const result = await tradeAPI.accept(trade.id, playerId)
    if (result.success) {
      alert(result.message || '交易已接受')
      await loadTrades()
    } else {
      alert(result.message || '确认交易失败')
    }
  } catch (error) {
    alert('确认交易失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

const rejectTrade = async (trade) => {
  loading.value = true
  try {
    const result = await tradeAPI.reject(trade.id, playerId)
    if (result.success) {
      alert('交易已拒绝')
      await loadTrades()
    } else {
      alert(result.message || '拒绝交易失败')
    }
  } catch (error) {
    alert('拒绝交易失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

const viewTradeDetail = (trade) => {
  console.log('viewTradeDetail called')
  console.log('trade:', JSON.stringify(trade))
  console.log('trade.items:', trade && trade.items ? JSON.stringify(trade.items) : 'undefined')
  selectedTrade.value = {
    id: trade.id,
    fromPlayerId: trade.fromPlayerId,
    fromPlayerName: trade.fromPlayerName,
    toPlayerId: trade.toPlayerId,
    toPlayerName: trade.toPlayerName || players.value.find(p => p.id === trade.toPlayerId)?.name || '玩家',
    status: trade.status,
    remark: trade.remark,
    createdAt: trade.createdAt,
    items: trade.items || []
  }
  console.log('selectedTrade.value:', JSON.stringify(selectedTrade.value))
  console.log('selectedTrade.value.items:', selectedTrade.value ? JSON.stringify(selectedTrade.value.items) : 'undefined')
  showDetailModal.value = true
}

const closeDetailModal = () => {
  showDetailModal.value = false
  selectedTrade.value = null
}

const loadPlayers = async () => {
  const result = await playerAPI.getAll()
  if (Array.isArray(result)) {
    players.value = result
  } else {
    players.value = [
      { id: 1, name: '阿尔伯特', faction: '统治者' },
      { id: 2, name: '莉莉丝', faction: '反叛者' },
      { id: 3, name: '罗宾', faction: '冒险者' },
      { id: 4, name: '亚瑟', faction: '杀戮者' },
      { id: 5, name: '艾米丽', faction: '平民' }
    ]
  }
}

const loadTrades = async () => {
  try {
    const result = await tradeAPI.getByPlayer(playerId)
    console.log('loadTrades result:', result)
    console.log('isArray:', Array.isArray(result))
    if (Array.isArray(result) && result.length > 0) {
      trades.value = result.map(t => ({
        ...t,
        fromPlayerName: players.value.find(p => p.id === t.fromPlayerId)?.name || '玩家',
        toPlayerName: players.value.find(p => p.id === t.toPlayerId)?.name || '玩家',
        items: t.items || []
      }))
      console.log('loaded from API')
      return
    }
  } catch (error) {
    console.log('API error:', error)
  }
  console.log('loading mock data')
  const mockTrades = [
      {
        id: 1,
        fromPlayerId: 2,
        fromPlayerName: '莉莉丝',
        toPlayerId: playerId,
        toPlayerName: '阿尔伯特',
        status: 'pending',
        remark: '需要一些食物',
        createdAt: new Date(Date.now() - 3600000).toISOString(),
        items: [
          { itemId: 5, itemType: 'material', name: '食物', unit: 'kg', quantity: 5, direction: 'give' },
          { itemId: 1, itemType: 'item', name: '医疗包', unit: '个', quantity: 1, direction: 'take' },
          { itemId: 1, itemType: 'weapon', name: '制式手枪', unit: '把', quantity: 1, direction: 'take' }
        ]
      },
      {
        id: 2,
        fromPlayerId: 3,
        fromPlayerName: '罗宾',
        toPlayerId: playerId,
        toPlayerName: '阿尔伯特',
        status: 'accepted',
        remark: '换一些工具',
        createdAt: new Date(Date.now() - 7200000).toISOString(),
        items: [
          { itemId: 4, itemType: 'weapon', name: '刺刀', unit: '把', quantity: 1, direction: 'give' },
          { itemId: 8, itemType: 'item', name: '维修工具包', unit: '个', quantity: 1, direction: 'take' }
        ]
      },
      {
        id: 5,
        fromPlayerId: 5,
        fromPlayerName: '艾米丽',
        toPlayerId: playerId,
        toPlayerName: '阿尔伯特',
        status: 'completed',
        remark: '完成了交易',
        createdAt: new Date(Date.now() - 86400000).toISOString(),
        items: [
          { itemId: 9, itemType: 'weapon', name: '斧头', unit: '把', quantity: 1, direction: 'give' },
          { itemId: 3, itemType: 'weapon', name: '警棍', unit: '把', quantity: 1, direction: 'take' }
        ]
      }
    ]
    console.log('mockTrades:', mockTrades)
    console.log('mockTrades length:', mockTrades.length)
    trades.value.length = 0
    for (const trade of mockTrades) {
      trades.value.push(trade)
    }
    console.log('trades.value after push:', trades.value)
    console.log('trades.value length:', trades.value.length)
  }

const formatTime = (dateStr) => {
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now - date
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  return `${Math.floor(diff / 86400000)}天前`
}

const getStatusBadge = (status) => {
  const badges = {
    pending: { text: '交易中', color: 'bg-yellow-500/20 text-yellow-400 border-yellow-500/50' },
    accepted: { text: '已接受', color: 'bg-emerald-500/20 text-emerald-400 border-emerald-500/50' },
    rejected: { text: '已拒绝', color: 'bg-red-500/20 text-red-400 border-red-500/50' },
    cancelled: { text: '交易中止', color: 'bg-red-500/20 text-red-400 border-red-500/50' },
    completed: { text: '已接受', color: 'bg-emerald-500/20 text-emerald-400 border-emerald-500/50' }
  }
  return badges[status] || badges.pending
}

onMounted(async () => {
  await loadPlayers()
  await loadTrades()
  await loadCurrentPlayerItems()
  await loadPlayerSupplies()
  const currentPlayer = players.value.find(p => p.id === playerId)
  currentPlayerName.value = currentPlayer?.name || ''
})

defineExpose({
  pendingTradesCount
})
</script>

<template>
  <div class="max-w-7xl">
    <div class="mb-6">
      <h1 class="text-white mb-1 tracking-tight text-2xl">交易管理</h1>
      <p class="text-gray-500 text-sm">Trade Management</p>
    </div>

    <div class="flex gap-3 mb-6">
      <button
        type="button"
        class="bg-gradient-to-r from-blue-500 to-blue-600 hover:from-blue-600 hover:to-blue-700 text-white px-5 py-2.5 rounded-xl transition-all text-sm shadow-lg shadow-blue-500/30 flex items-center gap-2"
        @click="openTradeModal"
      >
        <span>发起交易</span>
      </button>
    </div>

    <div v-if="pendingTrades.length > 0" class="mb-8">
      <h2 class="text-white text-lg mb-4 flex items-center gap-2">
        <span class="w-2 h-2 rounded-full bg-blue-400"></span>
        待处理交易
        <span class="text-xs text-gray-400 bg-white/5 px-2 py-1 rounded-full">{{ pendingTrades.length }} 个待审批</span>
      </h2>
      
      <div class="space-y-4">
        <div
          v-for="trade in pendingTrades"
          :key="trade.id"
          class="relative bg-gradient-to-br from-[#1a2332] to-[#0f1419] border border-white/10 rounded-2xl p-5 overflow-hidden hover:border-white/20 transition-all"
        >
          <div class="absolute top-0 right-0 w-64 h-64 bg-blue-500/5 rounded-full blur-3xl" />
          
          <div class="relative">
            <div class="flex items-center justify-between mb-4">
              <div class="flex items-center gap-3">
                <div class="w-12 h-12 rounded-xl bg-blue-500/20 flex items-center justify-center text-2xl">
                  👤
                </div>
                <div>
                  <p class="text-white font-medium">来自 {{ trade.fromPlayerName }}</p>
                  <p class="text-gray-500 text-xs">{{ formatTime(trade.createdAt) }}</p>
                </div>
              </div>
              
              <div class="flex gap-2">
                <button
                  type="button"
                  class="bg-blue-500/20 hover:bg-blue-500/30 text-blue-400 px-4 py-2 rounded-xl text-sm transition-all"
                  @click="viewTradeDetail(trade)"
                >
                  查看
                </button>
                <button
                  type="button"
                  class="bg-emerald-500/20 hover:bg-emerald-500/30 text-emerald-400 px-4 py-2 rounded-xl text-sm transition-all"
                  @click="acceptTrade(trade)"
                  :disabled="loading"
                >
                  接受
                </button>
                <button
                  type="button"
                  class="bg-red-500/20 hover:bg-red-500/30 text-red-400 px-4 py-2 rounded-xl text-sm transition-all"
                  @click="rejectTrade(trade)"
                  :disabled="loading"
                >
                  拒绝
                </button>
              </div>
            </div>
            
            <p v-if="trade.remark" class="text-gray-400 text-sm mb-4">备注：{{ trade.remark }}</p>
          </div>
        </div>
      </div>
    </div>

    <div v-if="trades.length > 0">
      <h2 class="text-white text-lg mb-4 flex items-center gap-2">
        <span class="w-2 h-2 rounded-full bg-gray-400"></span>
        交易记录
      </h2>
      
      <div class="space-y-3">
        <div
          v-for="trade in trades"
          :key="trade.id"
          class="bg-gradient-to-br from-[#1a2332] to-[#0f1419] border border-white/10 rounded-xl p-4 hover:border-white/20 transition-all"
        >
          <div class="flex items-center justify-between">
            <div class="flex items-center gap-3">
              <div class="w-10 h-10 rounded-lg bg-white/5 flex items-center justify-center text-lg">
                👤
              </div>
              <div>
                <p class="text-gray-200 text-sm">
                  {{ trade.fromPlayerId === playerId ? '向 ' : '来自 ' }}
                  {{ trade.fromPlayerId === playerId ? trade.toPlayerName : trade.fromPlayerName }}
                </p>
                <p class="text-gray-500 text-xs">{{ formatTime(trade.createdAt) }}</p>
              </div>
            </div>
            <div class="flex items-center gap-2">
              <span :class="['text-xs px-3 py-1 rounded-full border', getStatusBadge(trade.status).color]">
                {{ getStatusBadge(trade.status).text }}
              </span>
              <button
                type="button"
                class="bg-blue-500/20 hover:bg-blue-500/30 text-blue-400 px-3 py-1.5 rounded-lg text-xs transition-all"
                @click="viewTradeDetail(trade)"
              >
                查看
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-if="trades.length === 0" class="flex flex-col items-center justify-center py-20">
      <div class="text-red-400 text-sm mb-2">调试: trades.length = {{ trades.length }}</div>
      <div class="w-24 h-24 rounded-full bg-white/5 flex items-center justify-center mb-4">
        <span class="text-4xl">🤝</span>
      </div>
      <h3 class="text-white text-lg mb-2">暂无交易记录</h3>
      <p class="text-gray-500 text-sm">点击"发起交易"开始与他人交易</p>
    </div>

    <Teleport to="body">
      <div
        v-if="showTradeModal"
        class="fixed inset-0 bg-black/65 flex items-center justify-center z-50"
        @click.self="closeTradeModal"
      >
        <div class="bg-gradient-to-br from-[#1a2332] to-[#0f1419] border border-white/10 rounded-3xl p-6 max-w-2xl w-full mx-4 shadow-2xl max-h-[90vh] overflow-y-auto">
          <div class="flex items-center justify-between mb-6">
            <h2 class="text-white text-xl tracking-tight">发起交易</h2>
            <button
              type="button"
              class="text-gray-400 hover:text-white transition-colors"
              @click="closeTradeModal"
            >
              <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </div>

          <div v-if="tradeModalStep === 1" class="space-y-5">
            <div>
              <label class="block text-gray-300 text-sm mb-2">选择交易对象</label>
              <select
                v-model="selectedTargetPlayer"
                class="w-full bg-white/5 border border-white/10 rounded-xl px-4 py-3 text-gray-200 focus:outline-none focus:border-blue-500/50"
              >
                <option value="">请选择玩家...</option>
                <option v-for="player in otherPlayers" :key="player.id" :value="player.id">
                  {{ player.name }} ({{ player.faction }})
                </option>
              </select>
            </div>

            <div class="grid grid-cols-2 gap-4">
              <div>
                <div class="flex items-center justify-between mb-2">
                  <label class="text-gray-300 text-sm">你提供的物品</label>
                  <span class="text-xs text-gray-500">点击添加</span>
                </div>
                <div class="bg-white/5 border border-white/10 rounded-xl p-3 min-h-[200px] flex flex-col">
                  <div class="text-xs text-gray-500 mb-1">背包物品（点击一行加入）</div>
                  <div class="max-h-44 overflow-y-auto space-y-0.5 mb-3 rounded-lg border border-white/5 p-1 shrink-0">
                    <button
                      v-for="item in giveInventoryItemRows"
                      :key="`give-${item.id}-${item.type}`"
                      type="button"
                      class="w-full flex items-center gap-2 px-2 py-1.5 rounded-md text-xs text-gray-200 hover:bg-white/10 transition-colors text-left"
                      @click="addGiveItem(item)"
                    >
                      <div class="w-8 h-8 shrink-0 rounded-md bg-white/10 overflow-hidden flex items-center justify-center p-0.5">
                        <img
                          :src="item.imageUrl"
                          :alt="item.name"
                          class="w-full h-full object-contain"
                          loading="lazy"
                          decoding="async"
                        />
                      </div>
                      <div class="min-w-0 flex-1">
                        <span class="text-gray-500">[{{ tradeTypeLabel(item.itemType || item.type) }}]</span>
                        {{ item.name }}
                        <span class="text-gray-500"> · {{ item.quantity }}{{ formatChineseUnit(item.unit) }}</span>
                        <span v-if="Number(item.kcalPerUnit) > 0" class="text-gray-500"> · {{ item.kcalPerUnit }}大卡/单位</span>
                      </div>
                      <span v-if="selectedGiveCount(item) > 0" class="text-[11px] text-blue-300 bg-blue-500/20 px-2 py-0.5 rounded-full shrink-0">
                        {{ selectedGiveCount(item) }}
                      </span>
                    </button>
                    <div
                      v-if="giveInventoryItemRows.length === 0"
                      class="text-gray-500 text-xs text-center py-4"
                    >
                      暂无背包数据
                    </div>
                  </div>

                  <div class="text-xs text-gray-500 mb-1">食物与燃料（点击一行加入）</div>
                  <div class="max-h-32 overflow-y-auto space-y-0.5 mb-3 rounded-lg border border-white/5 p-1 shrink-0">
                    <button
                      v-for="item in giveInventorySupplyRows"
                      :key="`give-supply-${item.itemType}-${item.itemKey}`"
                      type="button"
                      class="w-full flex items-center justify-between gap-2 px-2 py-1.5 rounded-md text-xs text-gray-200 hover:bg-white/10 transition-colors text-left"
                      @click="addGiveItem(item)"
                    >
                      <div class="min-w-0 flex-1">
                        <span class="text-gray-500">[{{ tradeTypeLabel(item.itemType) }}]</span>
                        {{ item.name }}
                        <span class="text-gray-500"> · {{ item.quantity }}{{ formatChineseUnit(item.unit) }}</span>
                      </div>
                      <div class="flex items-center gap-2 shrink-0">
                        <span class="text-[11px] text-gray-500">{{ item.kcalPerUnit }} 大卡/单位</span>
                        <span v-if="selectedGiveCount(item) > 0" class="text-[11px] text-blue-300 bg-blue-500/20 px-2 py-0.5 rounded-full">
                          {{ selectedGiveCount(item) }}
                        </span>
                      </div>
                    </button>
                    <div
                      v-if="giveInventorySupplyRows.length === 0"
                      class="text-gray-500 text-xs text-center py-4"
                    >
                      暂无食物或燃料库存
                    </div>
                  </div>

                  <div class="text-xs text-gray-500 mb-1">已选物品（提供给对方）</div>
                  <div class="space-y-2 flex-1 min-h-0">
                    <div
                      v-for="(item, index) in giveItems"
                      :key="`selected-item-${item.itemType}-${item.itemId}-${item.itemKey}-${index}`"
                      class="bg-white/5 rounded-lg px-2 py-2 flex items-center justify-between gap-2"
                    >
                      <div class="flex items-center gap-2 min-w-0 flex-1">
                        <div
                          v-if="item.itemType !== 'food' && item.itemType !== 'energy'"
                          class="w-8 h-8 shrink-0 rounded-md bg-white/10 overflow-hidden flex items-center justify-center p-0.5"
                        >
                          <img
                            :src="item.imageUrl"
                            :alt="item.name"
                            class="w-full h-full object-contain"
                            decoding="async"
                          />
                        </div>
                        <div class="min-w-0">
                          <span class="text-gray-500 text-xs">[{{ tradeTypeLabel(item.itemType) }}]</span>
                          <span class="text-gray-200 text-sm">{{ item.name }}</span>
                          <span v-if="Number(item.kcalPerUnit) > 0" class="text-[11px] text-gray-500 block">{{ item.kcalPerUnit }} 大卡/单位</span>
                        </div>
                      </div>
                      <div class="flex items-center gap-2">
                        <input
                          v-model.number="item.quantity"
                          type="number"
                          min="1"
                          :max="item.maxQuantity"
                          class="w-16 bg-white/10 rounded px-2 py-1 text-gray-200 text-sm text-center"
                        />
                        <button
                          type="button"
                          class="text-red-400 hover:text-red-300 text-xs"
                          @click="removeGiveItem(giveItems.indexOf(item))"
                        >
                          移除
                        </button>
                      </div>
                    </div>
                  </div>
                  <div v-if="giveItems.length === 0" class="text-gray-500 text-sm text-center py-2">
                    暂无选择物品
                  </div>
                </div>
              </div>

              <div>
                <div class="flex items-center justify-between mb-2">
                  <label class="text-gray-300 text-sm">你需要的物品</label>
                  <span class="text-xs text-gray-500">点击添加</span>
                </div>
                <div class="bg-white/5 border border-white/10 rounded-xl p-3 min-h-[200px] flex flex-col">
                  <div class="flex flex-wrap gap-1.5 mb-2 shrink-0">
                    <button
                      v-for="tab in takePaletteTabs"
                      :key="tab.key"
                      type="button"
                      class="px-2.5 py-1 rounded-lg text-xs transition-all border"
                      :class="
                        takePaletteTab === tab.key
                          ? 'bg-blue-500/25 text-blue-300 border-blue-500/40'
                          : 'bg-white/5 text-gray-400 border-transparent hover:bg-white/10'
                      "
                      @click="takePaletteTab = tab.key"
                    >
                      {{ tab.label }}
                    </button>
                  </div>
                  <div class="text-xs text-gray-500 mb-1">图鉴（点击一行加入索取）</div>
                  <div class="max-h-44 overflow-y-auto space-y-0.5 mb-3 rounded-lg border border-white/5 p-1 shrink-0">
                    <button
                      v-for="row in visibleTakePalette"
                      :key="`take-${row.id}-${row.itemType}`"
                      type="button"
                      class="w-full flex items-center gap-2 px-2 py-1.5 rounded-md text-xs text-gray-200 hover:bg-white/10 transition-colors text-left"
                      @click="addTakeItem(row)"
                    >
                      <div
                        v-if="row.itemType !== 'food' && row.itemType !== 'energy'"
                        class="w-8 h-8 shrink-0 rounded-md bg-white/10 overflow-hidden flex items-center justify-center p-0.5"
                      >
                        <img
                          :src="row.imageUrl"
                          :alt="row.name"
                          class="w-full h-full object-contain"
                          loading="lazy"
                          decoding="async"
                        />
                      </div>
                      <div class="min-w-0 flex-1">
                        <span class="text-gray-500">[{{ tradeTypeLabel(row.itemType) }}]</span>
                        {{ row.name }}
                        <span class="text-gray-500"> · {{ formatChineseUnit(row.unit) }}</span>
                        <span v-if="Number(row.kcalPerUnit) > 0" class="text-gray-500"> · {{ row.kcalPerUnit }}大卡/单位</span>
                      </div>
                      <span v-if="selectedTakeCount(row) > 0" class="text-[11px] text-blue-300 bg-blue-500/20 px-2 py-0.5 rounded-full shrink-0">
                        {{ selectedTakeCount(row) }}
                      </span>
                    </button>
                  </div>

                  <div class="text-xs text-gray-500 mb-1">已选物品（向对方索取）</div>
                  <div class="space-y-2 flex-1 min-h-0">
                    <div
                      v-for="(item, index) in takeItems"
                      :key="`selected-take-item-${item.itemType}-${item.itemId}-${item.itemKey}-${index}`"
                      class="bg-white/5 rounded-lg px-2 py-2 flex items-center justify-between gap-2"
                    >
                      <div class="flex items-center gap-2 min-w-0 flex-1">
                        <div
                          v-if="item.itemType !== 'food' && item.itemType !== 'energy'"
                          class="w-8 h-8 shrink-0 rounded-md bg-white/10 overflow-hidden flex items-center justify-center p-0.5"
                        >
                          <img
                            :src="item.imageUrl"
                            :alt="item.name"
                            class="w-full h-full object-contain"
                            decoding="async"
                          />
                        </div>
                        <div class="min-w-0">
                          <span class="text-gray-500 text-xs">[{{ tradeTypeLabel(item.itemType) }}]</span>
                          <span class="text-gray-200 text-sm">{{ item.name }}</span>
                          <span v-if="Number(item.kcalPerUnit) > 0" class="text-[11px] text-gray-500 block">{{ item.kcalPerUnit }} 大卡/单位</span>
                        </div>
                      </div>
                      <div class="flex items-center gap-2">
                        <input
                          v-model.number="item.quantity"
                          type="number"
                          min="1"
                          :max="item.itemType === 'food' || item.itemType === 'energy' ? null : 500"
                          class="w-16 bg-white/10 rounded px-2 py-1 text-gray-200 text-sm text-center"
                          @change="normalizeTakeQuantity(item)"
                        />
                        <button
                          type="button"
                          class="text-red-400 hover:text-red-300 text-xs"
                          @click="removeTakeItem(takeItems.indexOf(item))"
                        >
                          移除
                        </button>
                      </div>
                    </div>
                  </div>
                  <div v-if="takeItems.length === 0" class="text-gray-500 text-sm text-center py-2">
                    暂无选择物品
                  </div>
                </div>
              </div>
            </div>

            <div>
              <label class="block text-gray-300 text-sm mb-2">备注信息（可选）</label>
              <textarea
                v-model="tradeRemark"
                class="w-full bg-white/5 border border-white/10 rounded-xl px-4 py-3 text-gray-200 focus:outline-none focus:border-blue-500/50 resize-none"
                rows="2"
                placeholder="添加交易备注..."
              />
            </div>

            <div class="flex gap-3 pt-2">
              <button
                type="button"
                class="flex-1 bg-white/5 hover:bg-white/10 text-gray-300 py-3 rounded-xl transition-all"
                @click="closeTradeModal"
              >
                取消
              </button>
              <button
                type="button"
                class="flex-1 bg-gradient-to-r from-blue-500 to-blue-600 hover:from-blue-600 hover:to-blue-700 text-white py-3 rounded-xl transition-all shadow-lg shadow-blue-500/30"
                @click="goPreviewStep"
              >
                预览
              </button>
            </div>
          </div>

          <div v-else class="space-y-5">
            <div class="bg-white/5 border border-white/10 rounded-xl p-4">
              <h3 class="text-gray-300 text-sm mb-2">交易预览</h3>
              <div class="text-xs text-gray-400 mb-1">交易对象：{{ otherPlayers.find(p => p.id === Number(selectedTargetPlayer))?.name || '未选择' }}</div>
              <div class="text-xs text-gray-400 mb-2">你提供 {{ giveItems.length }} 项，索取 {{ takeItems.length }} 项</div>
              <div class="space-y-3 max-h-60 overflow-y-auto">
                <div>
                  <div class="text-[11px] text-gray-500 mb-1">提供</div>
                  <div class="space-y-1">
                    <div v-for="(item, index) in giveItems" :key="`pv-give-${index}`" class="text-xs text-gray-300 flex items-center justify-between">
                      <span>[{{ tradeTypeLabel(item.itemType) }}] {{ item.name }}</span>
                      <span class="text-gray-400 text-right">
                        {{ item.quantity }} {{ formatChineseUnit(item.unit) }}
                        <span v-if="Number(item.kcalPerUnit) > 0" class="block text-[11px] text-gray-500">{{ item.kcalPerUnit }} 大卡/单位</span>
                      </span>
                    </div>
                    <div v-if="giveItems.length === 0" class="text-xs text-gray-500">无</div>
                  </div>
                </div>
                <div>
                  <div class="text-[11px] text-gray-500 mb-1">索取</div>
                  <div class="space-y-1">
                    <div v-for="(item, index) in takeItems" :key="`pv-take-${index}`" class="text-xs text-gray-300 flex items-center justify-between">
                      <span>[{{ tradeTypeLabel(item.itemType) }}] {{ item.name }}</span>
                      <span class="text-gray-400 text-right">
                        {{ item.quantity }} {{ formatChineseUnit(item.unit) }}
                        <span v-if="Number(item.kcalPerUnit) > 0" class="block text-[11px] text-gray-500">{{ item.kcalPerUnit }} 大卡/单位</span>
                      </span>
                    </div>
                    <div v-if="takeItems.length === 0" class="text-xs text-gray-500">无</div>
                  </div>
                </div>
              </div>
            </div>

            <div class="bg-white/5 border border-white/10 rounded-xl px-3 py-2 text-xs text-gray-300 space-y-1">
              <div class="flex items-center justify-between">
                <span>食物热量变化（大卡）</span>
                <span :class="tradeKcalSummary.foodDelta >= 0 ? 'text-emerald-400' : 'text-red-400'">
                  {{ tradeKcalSummary.foodDelta >= 0 ? '+' : '' }}{{ tradeKcalSummary.foodDelta.toLocaleString() }}
                </span>
              </div>
              <div class="flex items-center justify-between">
                <span>燃料热量变化（大卡）</span>
                <span :class="tradeKcalSummary.energyDelta >= 0 ? 'text-emerald-400' : 'text-red-400'">
                  {{ tradeKcalSummary.energyDelta >= 0 ? '+' : '' }}{{ tradeKcalSummary.energyDelta.toLocaleString() }}
                </span>
              </div>
            </div>

            <div v-if="tradeRemark" class="bg-white/5 border border-white/10 rounded-xl p-3">
              <div class="text-xs text-gray-500 mb-1">备注信息</div>
              <p class="text-sm text-gray-300">{{ tradeRemark }}</p>
            </div>

            <div class="flex gap-3 pt-2">
              <button
                type="button"
                class="flex-1 bg-white/5 hover:bg-white/10 text-gray-300 py-3 rounded-xl transition-all"
                @click="tradeModalStep = 1"
              >
                返回
              </button>
              <button
                type="button"
                class="flex-1 bg-gradient-to-r from-blue-500 to-blue-600 hover:from-blue-600 hover:to-blue-700 text-white py-3 rounded-xl transition-all shadow-lg shadow-blue-500/30"
                @click="sendTrade"
                :disabled="loading"
              >
                发送交易请求
              </button>
            </div>
          </div>
        </div>
      </div>
    </Teleport>

    <Teleport to="body">
      <div
        v-if="showDetailModal && selectedTrade"
        class="fixed inset-0 bg-black/65 flex items-center justify-center z-50"
        @click.self="closeDetailModal"
      >
        <div class="bg-gradient-to-br from-[#1a2332] to-[#0f1419] border border-white/10 rounded-3xl p-6 max-w-4xl w-full mx-4 shadow-2xl max-h-[90vh] overflow-y-auto">
          <div class="flex items-center justify-between mb-6">
            <h2 class="text-white text-xl tracking-tight">交易详情</h2>
            <button
              type="button"
              class="text-gray-400 hover:text-white transition-colors"
              @click="closeDetailModal"
            >
              <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </div>

          <div class="space-y-6">
            <div class="bg-white/5 backdrop-blur border border-white/10 rounded-xl p-4">
              <h3 class="text-gray-400 text-xs mb-3">基本信息</h3>
              <div class="space-y-3">
                <div class="flex items-center justify-between">
                  <span class="text-gray-500 text-sm">交易对象</span>
                  <span class="text-gray-200 text-sm">
                    {{ selectedTrade.fromPlayerId === playerId ? '向 ' : '来自 ' }}
                    {{ selectedTrade.fromPlayerId === playerId ? selectedTrade.toPlayerName : selectedTrade.fromPlayerName }}
                  </span>
                </div>
                <div class="flex items-center justify-between">
                  <span class="text-gray-500 text-sm">交易状态</span>
                  <span :class="['text-xs px-3 py-1 rounded-full border', getStatusBadge(selectedTrade.status).color]">
                    {{ getStatusBadge(selectedTrade.status).text }}
                  </span>
                </div>
                <div class="flex items-center justify-between">
                  <span class="text-gray-500 text-sm">创建时间</span>
                  <span class="text-gray-200 text-sm">{{ formatTime(selectedTrade.createdAt) }}</span>
                </div>
              </div>
            </div>

            <div class="grid grid-cols-2 gap-3">
              <div class="bg-white/5 backdrop-blur border border-white/10 rounded-xl p-3">
                <h3 class="text-gray-400 text-xs mb-2">你提供</h3>
                <div class="text-[11px] text-gray-500 mb-1">物品</div>
                <div class="space-y-1 max-h-36 overflow-y-auto">
                  <div
                    v-for="(item, index) in providedItemRows"
                    :key="index"
                    class="flex items-center justify-between gap-2 bg-white/5 rounded-lg px-3 py-2 text-sm"
                  >
                    <span class="flex items-center gap-2 text-gray-200 truncate min-w-0">
                      <span class="w-7 h-7 shrink-0 rounded bg-white/10 overflow-hidden flex items-center justify-center p-0.5">
                        <img
                          :src="item.imageUrl"
                          :alt="item.name"
                          class="w-full h-full object-contain"
                          decoding="async"
                        />
                      </span>
                      <span class="truncate">{{ item.name }}</span>
                    </span>
                    <span class="text-gray-400 shrink-0 whitespace-nowrap text-right">
                      {{ item.quantity }} {{ formatChineseUnit(item.unit) }}
                      <span v-if="Number(item.kcalPerUnit) > 0" class="block text-[11px] text-gray-500">{{ item.kcalPerUnit }} 大卡/单位</span>
                    </span>
                  </div>
                  <div v-if="providedItemRows.length === 0" class="text-gray-500 text-sm text-center py-4">
                    无
                  </div>
                </div>
                <div class="text-[11px] text-gray-500 mt-2 mb-1">食物与燃料</div>
                <div class="space-y-1 max-h-28 overflow-y-auto">
                  <div v-for="(item, index) in providedSupplyRows" :key="`provided-supply-${index}`" class="flex items-center justify-between gap-2 bg-white/5 rounded-lg px-3 py-2 text-sm">
                    <span class="text-gray-200 truncate min-w-0">
                      <span class="text-gray-500 text-xs">[{{ tradeTypeLabel(item.itemType) }}]</span>
                      {{ item.name }}
                    </span>
                    <span class="text-gray-400 shrink-0 whitespace-nowrap text-right">
                      {{ item.quantity }} {{ formatChineseUnit(item.unit) }}
                      <span class="block text-[11px] text-gray-500">{{ item.kcalPerUnit }} 大卡/单位</span>
                    </span>
                  </div>
                  <div v-if="providedSupplyRows.length === 0" class="text-gray-500 text-sm text-center py-2">无</div>
                </div>
              </div>

              <div class="bg-white/5 backdrop-blur border border-white/10 rounded-xl p-3">
                <h3 class="text-gray-400 text-xs mb-2">你获得</h3>
                <div class="text-[11px] text-gray-500 mb-1">物品</div>
                <div class="space-y-1 max-h-36 overflow-y-auto">
                  <div
                    v-for="(item, index) in receivedItemRows"
                    :key="index"
                    class="flex items-center justify-between gap-2 bg-white/5 rounded-lg px-3 py-2 text-sm"
                  >
                    <span class="flex items-center gap-2 text-gray-200 truncate min-w-0">
                      <span class="w-7 h-7 shrink-0 rounded bg-white/10 overflow-hidden flex items-center justify-center p-0.5">
                        <img
                          :src="item.imageUrl"
                          :alt="item.name"
                          class="w-full h-full object-contain"
                          decoding="async"
                        />
                      </span>
                      <span class="truncate">{{ item.name }}</span>
                    </span>
                    <span class="text-gray-400 shrink-0 whitespace-nowrap text-right">
                      {{ item.quantity }} {{ formatChineseUnit(item.unit) }}
                      <span v-if="Number(item.kcalPerUnit) > 0" class="block text-[11px] text-gray-500">{{ item.kcalPerUnit }} 大卡/单位</span>
                    </span>
                  </div>
                  <div v-if="receivedItemRows.length === 0" class="text-gray-500 text-sm text-center py-4">
                    无
                  </div>
                </div>
                <div class="text-[11px] text-gray-500 mt-2 mb-1">食物与燃料</div>
                <div class="space-y-1 max-h-28 overflow-y-auto">
                  <div v-for="(item, index) in receivedSupplyRows" :key="`received-supply-${index}`" class="flex items-center justify-between gap-2 bg-white/5 rounded-lg px-3 py-2 text-sm">
                    <span class="text-gray-200 truncate min-w-0">
                      <span class="text-gray-500 text-xs">[{{ tradeTypeLabel(item.itemType) }}]</span>
                      {{ item.name }}
                    </span>
                    <span class="text-gray-400 shrink-0 whitespace-nowrap text-right">
                      {{ item.quantity }} {{ formatChineseUnit(item.unit) }}
                      <span class="block text-[11px] text-gray-500">{{ item.kcalPerUnit }} 大卡/单位</span>
                    </span>
                  </div>
                  <div v-if="receivedSupplyRows.length === 0" class="text-gray-500 text-sm text-center py-2">无</div>
                </div>
              </div>
            </div>

            <div class="bg-white/5 border border-white/10 rounded-xl px-3 py-2 text-xs text-gray-300 space-y-1">
              <div class="flex items-center justify-between">
                <span>食物热量变化（大卡）</span>
                <span :class="detailKcalSummary.foodDelta >= 0 ? 'text-emerald-400' : 'text-red-400'">
                  {{ detailKcalSummary.foodDelta >= 0 ? '+' : '' }}{{ detailKcalSummary.foodDelta.toLocaleString() }}
                </span>
              </div>
              <div class="flex items-center justify-between">
                <span>燃料热量变化（大卡）</span>
                <span :class="detailKcalSummary.energyDelta >= 0 ? 'text-emerald-400' : 'text-red-400'">
                  {{ detailKcalSummary.energyDelta >= 0 ? '+' : '' }}{{ detailKcalSummary.energyDelta.toLocaleString() }}
                </span>
              </div>
            </div>

            <div v-if="selectedTrade.remark" class="bg-white/5 backdrop-blur border border-white/10 rounded-xl p-4">
              <h3 class="text-gray-400 text-xs mb-2">备注信息</h3>
              <p class="text-gray-300 text-sm">{{ selectedTrade.remark }}</p>
            </div>

            <div v-if="selectedTrade.status === 'pending' && selectedTrade.toPlayerId === playerId" class="flex gap-3">
              <button
                type="button"
                class="flex-1 bg-white/5 hover:bg-white/10 text-gray-300 py-3 rounded-xl transition-all"
                @click="closeDetailModal"
              >
                关闭
              </button>
              <button
                type="button"
                class="flex-1 bg-gradient-to-r from-blue-500 to-blue-600 hover:from-blue-600 hover:to-blue-700 text-white py-3 rounded-xl transition-all shadow-lg shadow-blue-500/30"
                @click="closeDetailModal"
              >
                继续操作
              </button>
            </div>
            <div v-else class="flex justify-center">
              <button
                type="button"
                class="w-full bg-gradient-to-r from-blue-500 to-blue-600 hover:from-blue-600 hover:to-blue-700 text-white py-3 rounded-xl transition-all shadow-lg shadow-blue-500/30"
                @click="closeDetailModal"
              >
                关闭
              </button>
            </div>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>