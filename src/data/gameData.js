/**
 * 项目统一数据文件（避免 src/data 下零散多个 .js）。
 *
 * 包含：
 * - 物资管理页：后端 item / weapon / ammo / material 的 id 与本地素材对应关系
 * - 统治者避难所：图鉴、物资展示映射、建造日志（演示数据）
 *
 * 说明：
 * - 有对应 PNG 的 id 会映射到素材；无映射时 `getMaterialImageUrl` 返回 null。
 * - `getMaterialImageUrlOrDefault` 在无映射时返回统一占位图（医疗包），供界面始终可显示图片。
 */

// -----------------------------
// 素材（assets）
// -----------------------------
import imgMedicalKit from '@/assets/医疗包.png?url'
import imgFlashlight from '@/assets/手电筒.png?url'
import imgHandcuffs from '@/assets/手铐.png?url'
import imgWhistle from '@/assets/哨子.png?url'
import imgBodyArmor from '@/assets/防弹衣.png?url'
import imgCompositeShield from '@/assets/复合盾.png?url'
import imgFlareGun from '@/assets/信号枪.png?url'
import imgRepairKit from '@/assets/维修工具包.png?url'
import imgContract from '@/assets/协议书.png?url'
import imgRum from '@/assets/朗姆酒.png?url'
import imgHerbs from '@/assets/草药.png?url'
import imgFishingNet from '@/assets/渔网.png?url'
import imgCandle from '@/assets/蜡烛.png?url'
import imgAlcohol from '@/assets/医用酒精.png?url'
import imgMatches from '@/assets/火柴.png?url'
import imgPencil from '@/assets/铅笔.png?url'
import imgSeaChart from '@/assets/破损海图.png?url'
import imgBento from '@/assets/便当.png?url'
import imgServicePistol from '@/assets/制式手枪.png?url'
import imgHuntingShotgun from '@/assets/猎枪.png?url'
import imgBaton from '@/assets/警棍.png?url'
import imgBayonet from '@/assets/刺刀.png?url'
import imgHarpoon from '@/assets/鱼叉_矛.png?url'
import imgHuntingBow from '@/assets/猎弓.png?url'
import imgPickaxe from '@/assets/十字镐.png?url'
import imgAxe from '@/assets/斧头.png?url'
import imgRifle from '@/assets/步枪.png?url'
import imgWood from '@/assets/木材.png?url'
import imgStone from '@/assets/石料.png?url'
import imgPlank from '@/assets/木板.png?url'
import imgRope from '@/assets/绳索.png?url'
import imgWarehouseKey from '@/assets/仓库钥匙.png?url'
import imgFuelDepotKey from '@/assets/燃料仓库钥匙.png?url'
import imgArmoryKey from '@/assets/镇武库钥匙.png?url'
import imgDockMarketKey from '@/assets/码头集购站钥匙.png?url'
import imgRebelBaseKey from '@/assets/反叛者基地钥匙.png?url'
import imgArkKey from '@/assets/方舟钥匙.png?url'
import imgSailorKnife from '@/assets/水手刀.png?url'
import imgMetalProducts from '@/assets/金属制品.png?url'
import imgAsphalt from '@/assets/沥青.png?url'
import imgCanvas from '@/assets/帆布.png?url'
import imgEngine from '@/assets/发动机.png?url'
import imgPropeller from '@/assets/螺旋桨.png?url'
import imgGenerator from '@/assets/发电机.png?url'
import imgChainsaw from '@/assets/电锯.png?url'
import imgScalpel from '@/assets/手术刀.png?url'
import imgExplosives from '@/assets/炸药.png?url'

// -----------------------------
// 物资管理页：图片映射
// -----------------------------
const ITEM_IMAGES = {
  1: imgMedicalKit,
  2: imgFlashlight,
  3: imgHandcuffs,
  4: imgWhistle,
  5: imgBodyArmor,
  6: imgCompositeShield,
  7: imgFlareGun,
  8: imgRepairKit,
  9: imgContract,
  10: imgRum,
  11: imgHerbs,
  12: imgFishingNet,
  13: imgCandle,
  14: imgAlcohol,
  15: imgMatches,
  16: imgPencil,
  17: imgSeaChart,
  18: imgBento,
  19: imgWarehouseKey,
  20: imgFuelDepotKey,
  21: imgArmoryKey,
  22: imgDockMarketKey,
  23: imgRebelBaseKey,
  24: imgArkKey,
}

const WEAPON_IMAGES = {
  1: imgServicePistol,
  2: imgHuntingShotgun,
  3: imgBaton,
  4: imgBayonet,
  5: imgSailorKnife,
  6: imgHarpoon,
  7: imgHuntingBow,
  8: imgPickaxe,
  9: imgAxe,
  10: imgChainsaw,
  11: imgScalpel,
  12: imgExplosives,
}

const AMMO_IMAGES = {
  // 弹药暂无独立素材，先用对应武器素材占位
  1: imgServicePistol,
  2: imgHuntingShotgun,
  3: imgFlareGun,
  4: imgHuntingBow,
}

const MATERIAL_IMAGES = {
  1: imgMetalProducts,
  2: imgWood,
  3: imgRope,
  4: imgPlank,
  5: imgBento,
  6: imgAsphalt,
  7: imgStone,
  8: imgCandle,
  9: imgCanvas,
  10: imgEngine,
  11: imgPropeller,
  12: imgGenerator,
}

const MATERIAL_MAPS = {
  item: ITEM_IMAGES,
  weapon: WEAPON_IMAGES,
  ammo: AMMO_IMAGES,
  material: MATERIAL_IMAGES,
}

// 与数据库 item / weapon / ammo / material 表同步（snowisland_5_15.sql）
export const GAME_ITEM_NAMES = {
  item: {
    1: '医疗包',
    2: '手电筒',
    3: '手铐',
    4: '哨子',
    5: '防弹衣',
    6: '复合盾',
    7: '信号枪',
    8: '维修工具包',
    9: '协议书',
    10: '朗姆酒',
    11: '草药',
    12: '渔网',
    13: '蜡烛',
    14: '医用酒精',
    15: '火柴',
    16: '铅笔',
    17: '破损海图',
    18: '便当',
    19: '仓库钥匙',
    20: '燃料仓库钥匙',
    21: '镇武库钥匙',
    22: '码头集换站钥匙',
    23: '反叛者基地钥匙',
    24: '方舟钥匙',
  },
  weapon: {
    1: '制式手枪',
    2: '猎枪',
    3: '警棍',
    4: '刺刀',
    5: '水手刀',
    6: '鱼叉/矛',
    7: '猎弓',
    8: '十字镐',
    9: '斧头',
    10: '电锯',
    11: '手术刀',
    12: '炸药',
  },
  ammo: {
    1: '手枪弹',
    2: '猎枪弹',
    3: '信号弹',
    4: '箭矢',
  },
  material: {
    1: '金属制品',
    2: '木材',
    3: '绳索',
    4: '木板',
    5: '食物',
    6: '沥青',
    7: '石料',
    8: '燃料/煤油',
    9: '帆布',
    10: '发动机',
    11: '螺旋桨',
    12: '发电机',
  },
}

/**
 * @param {'item'|'weapon'|'ammo'|'material'|string} itemType
 * @param {number|string} itemId
 */
export function getItemDisplayName(itemType, itemId) {
  if (itemType == null || itemId === '' || itemId === undefined) return '未知物品'
  const t = String(itemType).toLowerCase()
  const numId = typeof itemId === 'number' && Number.isFinite(itemId) ? itemId : parseInt(String(itemId), 10)
  if (Number.isNaN(numId)) return '未知物品'
  return GAME_ITEM_NAMES[t]?.[numId] ?? '未知物品'
}

export function formatTransportItemLine(item) {
  const name = getItemDisplayName(item.itemType, item.itemId)
  return `${name} × ${item.quantity}（${item.weight}千克/单位）`
}

/** 将行动结果中的 material-3 等形式替换为中文名称 */
export function formatActionResultText(text) {
  if (!text) return text
  return String(text).replace(
    /\b(item|weapon|ammo|material)-(\d+)\b/g,
    (_, type, id) => getItemDisplayName(type, id),
  )
}

/**
 * 获取物资图片 url；若没有对应素材返回 null。
 * @param {'item'|'weapon'|'ammo'|'material'} type
 * @param {number|string} id
 * @returns {string | null}
 */
export function getMaterialImageUrl(type, id) {
  if (type == null || id === '' || id === undefined) return null
  const t = String(type).toLowerCase()
  const table = MATERIAL_MAPS[t]
  if (!table) return null
  const numId = typeof id === 'number' && Number.isFinite(id) ? id : parseInt(String(id), 10)
  if (Number.isNaN(numId)) return null
  return table[numId] ?? null
}

/** 无对应素材时返回统一占位图（与 {@link imgMedicalKit} 相同资源） */
export const DEFAULT_MATERIAL_IMAGE_URL = imgMedicalKit

/** 解析结果缓存，避免大量组件重复拼接字符串与查表 */
const materialUrlOrDefaultCache = new Map()

export function getMaterialImageUrlOrDefault(type, id) {
  if (type == null || id === '' || id === undefined) return DEFAULT_MATERIAL_IMAGE_URL
  const t = String(type).toLowerCase()
  const numId = typeof id === 'number' && Number.isFinite(id) ? id : parseInt(String(id), 10)
  if (Number.isNaN(numId)) return DEFAULT_MATERIAL_IMAGE_URL
  const key = `${t}:${numId}`
  let cached = materialUrlOrDefaultCache.get(key)
  if (cached !== undefined) return cached
  cached = getMaterialImageUrl(t, numId) ?? DEFAULT_MATERIAL_IMAGE_URL
  materialUrlOrDefaultCache.set(key, cached)
  return cached
}

/**
 * 空闲时预加载所有物资图 URL（浏览器 HTTP 缓存 + 解码后可更快展示）。
 * 在交易页等入口 mounted 时调用一次即可。
 */
export function preloadMaterialImages() {
  if (typeof Image === 'undefined') return
  const seen = new Set()
  /** @param {string} src */
  const queue = (src) => {
    if (!src || seen.has(src)) return
    seen.add(src)
    const img = new Image()
    img.decoding = 'async'
    img.src = src
  }
  for (const table of Object.values(MATERIAL_MAPS)) {
    for (const url of Object.values(table)) {
      if (typeof url === 'string') queue(url)
    }
  }
  queue(DEFAULT_MATERIAL_IMAGE_URL)
}

/** 筛选标签 / 标题旁的小图（可选） */
export function getTypeTabImage(type) {
  if (type === 'weapon') return imgRifle
  if (type === 'ammo') return imgServicePistol
  if (type === 'material') return imgWood
  return imgMedicalKit
}

// -----------------------------
// 统治者避难所：图鉴 / 库存 / 日志
// -----------------------------
export const SHELTER_ITEM_CATALOG = {
  medical_kit: { id: 'medical_kit', name: '医疗包', category: 'prop', description: '急救包，提供基础医疗支援。', imageUrl: imgMedicalKit },
  flashlight: { id: 'flashlight', name: '手电筒', category: 'prop', description: '夜间行动照明工具。', imageUrl: imgFlashlight },
  handcuffs: { id: 'handcuffs', name: '手铐', category: 'prop', description: '约束目标行动的控制道具。', imageUrl: imgHandcuffs },
  whistle: { id: 'whistle', name: '哨子', category: 'prop', description: '可用于报警或快速集合。', imageUrl: imgWhistle },
  body_armor: { id: 'body_armor', name: '防弹衣', category: 'prop', description: '冲突中提供额外防护。', imageUrl: imgBodyArmor },
  composite_shield: { id: 'composite_shield', name: '复合盾', category: 'prop', description: '降低正面冲突受伤风险。', imageUrl: imgCompositeShield },
  flare_gun: { id: 'flare_gun', name: '信号枪', category: 'prop', description: '发射信号弹，远距离传递信息。', imageUrl: imgFlareGun },
  repair_kit: { id: 'repair_kit', name: '维修工具包', category: 'prop', description: '用于设施与器械维修。', imageUrl: imgRepairKit },
  contract: { id: 'contract', name: '协议书', category: 'prop', description: '用于记录玩家间契约。', imageUrl: imgContract },
  rum: { id: 'rum', name: '朗姆酒', category: 'prop', description: '可作为补给或交易物资。', imageUrl: imgRum },
  herbs: { id: 'herbs', name: '草药', category: 'prop', description: '简易治疗所需药材。', imageUrl: imgHerbs },
  fishing_net: { id: 'fishing_net', name: '渔网', category: 'prop', description: '渔猎行动常用工具。', imageUrl: imgFishingNet },
  candle: { id: 'candle', name: '蜡烛', category: 'prop', description: '基础照明消耗品。', imageUrl: imgCandle },
  rubbing_alcohol: { id: 'rubbing_alcohol', name: '医用酒精', category: 'prop', description: '常见消毒用品。', imageUrl: imgAlcohol },
  matches: { id: 'matches', name: '火柴', category: 'prop', description: '取火工具。', imageUrl: imgMatches },
  pencil: { id: 'pencil', name: '铅笔', category: 'prop', description: '书写记录工具。', imageUrl: imgPencil },
  tattered_chart: { id: 'tattered_chart', name: '破损海图', category: 'prop', description: '可用于航线参考。', imageUrl: imgSeaChart },
  service_pistol: { id: 'service_pistol', name: '制式手枪', category: 'weapon', description: '标准短枪武器。', imageUrl: imgServicePistol },
  hunting_shotgun: { id: 'hunting_shotgun', name: '猎枪', category: 'weapon', description: '中近距离高威力武器。', imageUrl: imgHuntingShotgun },
  baton: { id: 'baton', name: '警棍', category: 'weapon', description: '非致命近战武器。', imageUrl: imgBaton },
  bayonet: { id: 'bayonet', name: '刺刀', category: 'weapon', description: '常见近战刀具。', imageUrl: imgBayonet },
  harpoon_spear: { id: 'harpoon_spear', name: '鱼叉 / 矛', category: 'weapon', description: '可狩猎亦可近战。', imageUrl: imgHarpoon },
  hunting_bow: { id: 'hunting_bow', name: '猎弓', category: 'weapon', description: '静音远程武器。', imageUrl: imgHuntingBow },
  pickaxe: { id: 'pickaxe', name: '十字镐', category: 'weapon', description: '可挖掘也可应急防卫。', imageUrl: imgPickaxe },
  axe: { id: 'axe', name: '斧头', category: 'weapon', description: '伐木与破拆工具。', imageUrl: imgAxe },
  wood: { id: 'wood', name: '木材', category: 'material', description: '基础建造材料。', imageUrl: imgWood },
  stone: { id: 'stone', name: '石料', category: 'material', description: '用于加固结构。', imageUrl: imgStone },
  plank: { id: 'plank', name: '木板', category: 'material', description: '用于铺板和隔断。', imageUrl: imgPlank },
  rope: { id: 'rope', name: '绳索', category: 'material', description: '常用固定与捆绑耗材。', imageUrl: imgRope },
}

export const SHELTER_ITEM_CATALOG_BY_TYPE = {
  'item_1': SHELTER_ITEM_CATALOG.medical_kit,
  'item_2': SHELTER_ITEM_CATALOG.flashlight,
  'item_3': SHELTER_ITEM_CATALOG.handcuffs,
  'item_4': SHELTER_ITEM_CATALOG.whistle,
  'item_5': SHELTER_ITEM_CATALOG.body_armor,
  'item_6': SHELTER_ITEM_CATALOG.composite_shield,
  'item_7': SHELTER_ITEM_CATALOG.flare_gun,
  'item_8': SHELTER_ITEM_CATALOG.repair_kit,
  'item_9': SHELTER_ITEM_CATALOG.contract,
  'item_10': SHELTER_ITEM_CATALOG.rum,
  'item_11': SHELTER_ITEM_CATALOG.herbs,
  'item_12': SHELTER_ITEM_CATALOG.fishing_net,
  'item_13': SHELTER_ITEM_CATALOG.candle,
  'item_14': SHELTER_ITEM_CATALOG.rubbing_alcohol,
  'item_15': SHELTER_ITEM_CATALOG.matches,
  'item_16': SHELTER_ITEM_CATALOG.pencil,
  'item_17': SHELTER_ITEM_CATALOG.tattered_chart,
  'weapon_1': SHELTER_ITEM_CATALOG.service_pistol,
  'weapon_2': SHELTER_ITEM_CATALOG.hunting_shotgun,
  'weapon_3': SHELTER_ITEM_CATALOG.baton,
  'weapon_4': SHELTER_ITEM_CATALOG.bayonet,
  'weapon_6': SHELTER_ITEM_CATALOG.harpoon_spear,
  'weapon_7': SHELTER_ITEM_CATALOG.hunting_bow,
  'weapon_8': SHELTER_ITEM_CATALOG.pickaxe,
  'weapon_9': SHELTER_ITEM_CATALOG.axe,
  'material_2': SHELTER_ITEM_CATALOG.wood,
  'material_3': SHELTER_ITEM_CATALOG.rope,
  'material_4': SHELTER_ITEM_CATALOG.plank,
  'material_7': SHELTER_ITEM_CATALOG.stone,
}

export const SHELTER_DAILY_LOGS = [
  { day: 1, workers: [
    { name: '张三', isProfessional: true, isOppressed: false, value: 5 },
    { name: '李四', isProfessional: false, isOppressed: true, value: 7 },
    { name: '王五', isProfessional: true, isOppressed: true, value: 8 },
    { name: '赵六', isProfessional: false, isOppressed: false, value: 4 },
  ]},
  { day: 2, workers: [
    { name: '孙七', isProfessional: false, isOppressed: false, value: 4 },
    { name: '周八', isProfessional: true, isOppressed: false, value: 5 },
    { name: '张三', isProfessional: true, isOppressed: true, value: 8 },
    { name: '李四', isProfessional: false, isOppressed: false, value: 4 },
    { name: '王五', isProfessional: false, isOppressed: true, value: 7 },
  ]},
  { day: 3, workers: [
    { name: '赵六', isProfessional: true, isOppressed: false, value: 5 },
    { name: '孙七', isProfessional: false, isOppressed: true, value: 7 },
    { name: '周八', isProfessional: true, isOppressed: true, value: 8 },
    { name: '张三', isProfessional: false, isOppressed: false, value: 4 },
  ]},
]

// 将后端库存与图鉴映射成可展示的数据
// 支持两种数据格式：
// 旧格式: { id: 'wood', quantity: 45 }
// 新格式: { itemType: 'material', itemId: 2, quantity: 45 }
export function resolveShelterInventoryRows(items) {
  return items
    .map((row) => {
      if (row.name) {
        const key = row.itemType != null && row.itemId != null
          ? `${row.itemType}_${row.itemId}`
          : String(row.id ?? '')
        const category = { item: 'prop', weapon: 'weapon', ammo: 'ammo', material: 'material' }[row.itemType] || 'prop'
        return {
          id: key,
          name: row.name,
          category,
          description: row.description || '',
          imageUrl: getMaterialImageUrlOrDefault(row.itemType, row.itemId),
          unit: row.unit || '',
          quantity: row.quantity,
        }
      }

      let def = null
      if (row.id != null) {
        def = SHELTER_ITEM_CATALOG[row.id]
      } else if (row.itemType != null && row.itemId != null) {
        const key = `${row.itemType}_${row.itemId}`
        def = SHELTER_ITEM_CATALOG_BY_TYPE[key]
      }
      if (!def) {
        const key = row.itemType != null && row.itemId != null
          ? `${row.itemType}_${row.itemId}`
          : String(row.id ?? '')
        if (!key) return null
        const category = { item: 'prop', weapon: 'weapon', ammo: 'ammo', material: 'material' }[row.itemType] || 'prop'
        return {
          id: key,
          name: getItemDisplayName(row.itemType, row.itemId),
          category,
          description: '',
          imageUrl: getMaterialImageUrlOrDefault(row.itemType, row.itemId),
          unit: '',
          quantity: row.quantity,
        }
      }
      return { ...def, quantity: row.quantity }
    })
    .filter(Boolean)
}

// 计算避难所总建造值
export function shelterTotalBuildValue(logs) {
  return logs.reduce((sum, day) => sum + day.workers.reduce((s, worker) => s + worker.value, 0), 0)
}

