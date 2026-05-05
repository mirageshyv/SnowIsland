/**
 * 统治者避难所的库存与建造日志数据。
 * 后端可只返回 [{ id, quantity }]，前端通过图鉴补全展示信息。
 */

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
import imgServicePistol from '@/assets/制式手枪.png?url'
import imgHuntingShotgun from '@/assets/猎枪.png?url'
import imgBaton from '@/assets/警棍.png?url'
import imgBayonet from '@/assets/刺刀.png?url'
import imgHarpoon from '@/assets/鱼叉:矛.png?url'
import imgHuntingBow from '@/assets/猎弓.png?url'
import imgPickaxe from '@/assets/十字镐.png?url'
import imgAxe from '@/assets/斧头.png?url'
import imgWood from '@/assets/木材.png?url'
import imgStone from '@/assets/石料.png?url'
import imgPlank from '@/assets/木板.png?url'
import imgRope from '@/assets/绳索.png?url'

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

export const DEFAULT_SHELTER_INVENTORY = [
  { id: 'wood', quantity: 45 },
  { id: 'stone', quantity: 32 },
  { id: 'medical_kit', quantity: 8 },
  { id: 'flashlight', quantity: 4 },
  { id: 'handcuffs', quantity: 2 },
  { id: 'whistle', quantity: 3 },
  { id: 'body_armor', quantity: 1 },
  { id: 'composite_shield', quantity: 1 },
  { id: 'flare_gun', quantity: 1 },
  { id: 'repair_kit', quantity: 5 },
  { id: 'contract', quantity: 2 },
  { id: 'rum', quantity: 10 },
  { id: 'herbs', quantity: 12 },
  { id: 'fishing_net', quantity: 2 },
  { id: 'candle', quantity: 18 },
  { id: 'rubbing_alcohol', quantity: 3 },
  { id: 'matches', quantity: 6 },
  { id: 'pencil', quantity: 4 },
  { id: 'tattered_chart', quantity: 1 },
  { id: 'service_pistol', quantity: 1 },
  { id: 'hunting_shotgun', quantity: 1 },
  { id: 'baton', quantity: 2 },
  { id: 'bayonet', quantity: 1 },
  { id: 'harpoon_spear', quantity: 1 },
  { id: 'hunting_bow', quantity: 1 },
  { id: 'pickaxe', quantity: 2 },
  { id: 'axe', quantity: 1 },
  { id: 'plank', quantity: 24 },
  { id: 'rope', quantity: 35 },
]

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
export function resolveShelterInventoryRows(items) {
  return items
    .map((row) => {
      const def = SHELTER_ITEM_CATALOG[row.id]
      if (!def) return null
      return { ...def, quantity: row.quantity }
    })
    .filter(Boolean)
}

// 计算避难所总建造值
export function shelterTotalBuildValue(logs) {
  return logs.reduce((sum, day) => sum + day.workers.reduce((s, worker) => s + worker.value, 0), 0)
}
