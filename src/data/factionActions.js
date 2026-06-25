/** 阵营行动定义 */

export const FACTION_LABELS = {
  '统治者': { key: 'ruler', label: '统治者', color: 'text-red-400 border-red-500/30 bg-red-500/10' },
  '反叛者': { key: 'rebel', label: '反叛者', color: 'text-purple-400 border-purple-500/30 bg-purple-500/10' },
  '冒险者': { key: 'adventurer', label: '冒险者', color: 'text-cyan-400 border-cyan-500/30 bg-cyan-500/10' },
  '天灾使者': { key: 'scourge', label: '天灾使者', color: 'text-amber-400 border-amber-500/30 bg-amber-500/10' },
  '平民': { key: 'civilian', label: '平民', color: 'text-gray-400 border-gray-500/30 bg-gray-500/10' },
}

export const GM_FACTION_TABS = ['统治者', '反叛者', '冒险者', '天灾使者', '平民']

/** 与「个人行动提交」白天选项一致（生产由 canProduce 动态插入） */
export const PERSONAL_DAY_ACTION_OPTIONS = [
  { value: 'go_location', label: '前往地点' },
  { value: 'investigate_player', label: '调查玩家' },
  { value: 'use_trait', label: '使用特性' },
  { value: 'use_skill', label: '使用职业技能' },
  { value: 'hide', label: '隐藏' },
  { value: 'transport', label: '搬运' },
  { value: 'other', label: '其他' },
]

/** @deprecated 使用 PERSONAL_DAY_ACTION_OPTIONS */
export const ASSIGNED_FREE_ACTIONS = PERSONAL_DAY_ACTION_OPTIONS

/** 额外行动：与白天个人行动类型一致（不含搬运） */
export const EXTRA_DAY_ACTION_TYPES = [
  { value: 'go_location', label: '前往地点' },
  { value: 'investigate_player', label: '调查玩家' },
  { value: 'produce', label: '生产' },
  { value: 'use_trait', label: '使用特性' },
  { value: 'use_skill', label: '使用职业技能' },
  { value: 'hide', label: '隐藏' },
]

export const INVESTIGATE_TYPES = [
  { value: 'investigate_location', label: '调查地点' },
  { value: 'investigate_player', label: '调查玩家' },
]

/** payload 字段中文名（DM 反馈页展示用） */
export const PAYLOAD_FIELD_LABELS = {
  actorId: '执行人员',
  targetLocationId: '目标地点',
  targetId: '目标',
  targetKind: '目标类型',
  assignedAction: '指定行动',
  assignedActions: '指定行动列表',
  mode: '模式',
  actionType: '行动类型',
  npcId: '交互NPC',
  notes: '备注',
  targetIds: '目标列表',
  targetPlayerId: '目标玩家',
  message: '秘密信息',
  anonymous: '匿名',
  secretLocationId: '秘密地点',
  notifyPlayerIds: '通知玩家',
  facilityId: '目标设施',
  investigateType: '调查类型',
  guardId: '看守人员',
  useWeaponOrSkill: '使用武器/技能',
  actionPoints: '行动点',
  useSpecialMaterials: '特殊材料',
  weaponId: '消耗武器',
  armed: '计入武器',
  note: '备注',
}

/** @type {Record<string, Array<{ type: string, title: string, description: string, tooltip: string }>>} */
export const FACTION_ACTION_DEFS = {
  '统治者': [
    {
      type: 'assign_personnel',
      title: '安排人员',
      description: '指定某玩家/NPC 在白天提交一项或两项自由行动（选项与个人行动一致，须与你安排的一致）；对方可拒绝（可作为审判理由）。每日限用一次；同一目标当日不可重复被安排人员。',
      tooltip: '可安排 1～2 项对方白天自由行动。每日一次，目标不可与当日其他「安排人员」重复。',
    },
    {
      type: 'assign_guard',
      title: '安排看守',
      description: '指定人员在夜晚驻守地点（消耗对方夜晚行动点）；基础防御 +3。每日限用一次；同一人员当日不可重复安排看守。',
      tooltip: '占用对方夜晚行动点。每日一次，人员不可与当日其他「安排看守」重复。',
    },
  ],
  '反叛者': [
    {
      type: 'extra_labor',
      title: '额外劳动',
      description: '今日生产行动产出 +50%，须当天已提交生产类自由行动。',
      tooltip: '需当天已提交「生产」自由行动。',
    },
    {
      type: 'secret_contact',
      title: '暗中联络',
      description: '向某玩家发送秘密信息，仅主持人与目标可见。',
      tooltip: '仅 DM 与收件人可见。',
    },
    {
      type: 'extra_action',
      title: '额外行动',
      description: '作为反抗者你已经习惯在黑暗中行动，你不需要手电筒即可进行夜晚行动。在夜晚时间你可以执行白天回合的一个任意行动，或者尝试杀掉某个反抗革命的人。此环节无法寻找NPC进行对话，NPC在没有前置沟通的情况下会默认回家睡觉。',
      tooltip: '选择一项白天自由行动在夜晚执行，选项与个人行动提交一致。',
    },
    {
      type: 'sabotage',
      title: '破坏',
      description: '摧毁某地设施；被监管地点无法破坏。',
      tooltip: '统治者「安排监管」中的地点不可选。',
    },
  ],
  '冒险者': [
    {
      type: 'extra_investigate',
      title: '额外调查',
      description: '提交一次阵营调查，结算时该次调查数量翻倍。',
      tooltip: '选择调查类型与目标（仅一项），与个人行动提交调查相同。',
    },
    {
      type: 'extra_labor',
      title: '额外劳动',
      description: '今日生产行动产出 +50%。',
      tooltip: '需当天已提交「生产」自由行动。',
    },
    {
      type: 'guard_ark',
      title: '看守方舟',
      description: '指派人员提升方舟防御，可计入武器/技能防御。',
      tooltip: '增加方舟武器与技能防御值。',
    },
    {
      type: 'ark_construction',
      title: '方舟建设',
      description: '消耗1个行动点投入资源推进方舟建造。每日上限：木材30吨、金属制品20吨、密封材料20kg（个人+仓库合计）。发动机/发电机/螺旋桨/船帆无上限。材料不足时可选择工作量推进（5吨木材/5吨金属/5kg密封材料当量）。',
      tooltip: '消耗1行动点，可选资源投入或工作量推进。',
    },
  ],
  '天灾使者': [
    {
      type: 'sabotage',
      title: '破坏',
      description: '摧毁设施；被监管地点无法破坏。',
      tooltip: '被监管地点不可选。',
    },
    {
      type: 'extra_investigate',
      title: '额外调查',
      description: '提交一次阵营调查，结算时该次调查数量翻倍。',
      tooltip: '选择调查类型与目标（仅一项），与个人行动提交调查相同。',
    },
    {
      type: 'curse',
      title: '诅咒',
      description: '消耗威胁值 ≥4 的武器，获知目标阵营并施加「诅咒」。',
      tooltip: '武器威胁值须 ≥4。',
    },
  ],
}

export function getActionDef(faction, actionType) {
  const list = FACTION_ACTION_DEFS[faction] || []
  return list.find(a => a.type === actionType)
}
