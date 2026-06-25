/** 夜晚行动定义 */

import { FACTION_LABELS, GM_FACTION_TABS } from './factionActions.js'

export { FACTION_LABELS, GM_FACTION_TABS }

export const NIGHT_PERSONAL_ACTION_TYPES = [
  { value: 'go_location', label: '前往地点' },
  { value: 'investigate_player', label: '调查玩家' },
  { value: 'produce', label: '生产' },
  { value: 'use_trait', label: '使用特性' },
  { value: 'use_skill', label: '使用职业技能' },
  { value: 'hide', label: '隐藏' },
  { value: 'other', label: '其他' },
]

export const PRESSURE_DEMAND_OPTIONS = [
  { value: 'labor_rotation', label: '合理的劳工换班机制' },
  { value: 'expand_militia', label: '扩大民兵' },
  { value: 'communal_resources', label: '资源由全体镇民管理' },
  { value: 'ark_resources', label: '增加方舟建设资源' },
]

export const CONSPIRACY_SUBTYPES = {
  '反叛者': [
    { value: 'raid_location', label: '袭击地点' },
    { value: 'assassinate_ruler', label: '暗杀统治者' },
    { value: 'rescue_prisoner', label: '解救人员' },
  ],
  '冒险者': [
    { value: 'raid_location', label: '袭击地点' },
  ],
  '天灾使者': [
    { value: 'raid_location', label: '袭击地点' },
    { value: 'spread_terror', label: '制造恐怖' },
    { value: 'assassinate_target', label: '暗杀目标' },
  ],
  '平民': [
    { value: 'raid_location', label: '袭击地点' },
    { value: 'assassinate_target', label: '暗杀目标' },
  ],
}

export const RAID_OUTCOME_OPTIONS = [
  { value: 'destroy', label: '破坏地点' },
  { value: 'loot', label: '搜刮资源' },
]

export const PAYLOAD_FIELD_LABELS = {
  actionType: '个人行动类型',
  targetId: '目标',
  npcId: '交互NPC',
  notes: '备注',
  targetPlayerId: '目标玩家',
  demand: '施压诉求',
  message: '宣传内容',
  conspiracySubtype: '密谋类型',
  targetLocationId: '目标地点',
  participantIds: '参与玩家',
  raidOutcome: '成功后选择',
  note: '说明',
}

/** @type {Record<string, Array<{ type: string, title: string, description: string }>>} */
export const NIGHT_ACTION_DEFS = {
  '统治者': [
    {
      type: 'night_personal_action',
      title: '夜晚个人行动',
      description: '在夜晚执行一项与白天相同的个人行动（前往、调查、生产、特性、技能、隐藏等）。每日一次。',
    },
    {
      type: 'public_trial',
      title: '公开审判',
      description: '在夜间于集市对一名玩家召开公开审判。每日一次。',
    },
    {
      type: 'explore_island',
      title: '探索岛屿',
      description: '在夜晚探索岛屿周边区域，可能发现物资、遭遇事件或找到有用的线索。每日一次。',
    },
    {
      type: 'other',
      title: '其他',
      description: '执行未列出的特殊行动，由主持人判定是否成功及效果。每日一次。',
    },
  ],
  '反叛者': [
    {
      type: 'pressure_ruler',
      title: '向统治者施压',
      description: '联合其他玩家在公屏发声；若同意者超过半数，统治者须妥协。须有合理理由（通常来自白天调查）。',
    },
    {
      type: 'conspiracy',
      title: '进行密谋',
      description: '与其他玩家秘密组织行动：袭击地点、暗杀统治者或解救被关押人员。夜间由主持人结算成功率与防御值。',
    },
    {
      type: 'explore_island',
      title: '探索岛屿',
      description: '在夜晚探索岛屿周边区域，可能发现物资、遭遇事件或找到有用的线索。每日一次。',
    },
    {
      type: 'other',
      title: '其他',
      description: '执行未列出的特殊行动，由主持人判定是否成功及效果。每日一次。',
    },
  ],
  '冒险者': [
    {
      type: 'pressure_ruler',
      title: '向统治者施压',
      description: '联合玩家公屏施压；超过半数同意则统治者须妥协。',
    },
    {
      type: 'publicity',
      title: '公开宣传',
      description: '在公屏发布方舟计划相关宣传，争取人员与资源支持。',
    },

    {
      type: 'conspiracy',
      title: '进行密谋',
      description: '组织秘密袭击地点；结算规则与反叛者袭击类似。',
    },
    {
      type: 'explore_island',
      title: '探索岛屿',
      description: '在夜晚探索岛屿周边区域，可能发现物资、遭遇事件或找到有用的线索。每日一次。',
    },
    {
      type: 'other',
      title: '其他',
      description: '执行未列出的特殊行动，由主持人判定是否成功及效果。每日一次。',
    },
  ],
  '天灾使者': [
    {
      type: 'conspiracy',
      title: '进行密谋',
      description: '袭击地点、制造恐怖或暗杀目标；夜间由主持人结算。',
    },
    {
      type: 'explore_island',
      title: '探索岛屿',
      description: '在夜晚探索岛屿周边区域，可能发现物资、遭遇事件或找到有用的线索。每日一次。',
    },
    {
      type: 'other',
      title: '其他',
      description: '执行未列出的特殊行动，由主持人判定是否成功及效果。每日一次。',
    },
  ],
  '平民': [
    {
      type: 'conspiracy',
      title: '进行密谋',
      description: '袭击地点或制造恐怖；夜间由主持人结算。',
    },
    {
      type: 'explore_island',
      title: '探索岛屿',
      description: '在夜晚探索岛屿周边区域，可能发现物资、遭遇事件或找到有用的线索。每日一次。',
    },
    {
      type: 'other',
      title: '其他',
      description: '执行未列出的特殊行动，由主持人判定是否成功及效果。每日一次。',
    },
  ],
}

export function getNightActionDef(faction, actionType) {
  const list = NIGHT_ACTION_DEFS[faction] || []
  return list.find((a) => a.type === actionType)
}

export function getConspiracySubtypes(faction) {
  return CONSPIRACY_SUBTYPES[faction] || []
}
