/** 阵营行动定义 */

export const FACTION_LABELS = {
  '统治者': { key: 'ruler', label: '统治者', color: 'text-red-400 border-red-500/30 bg-red-500/10' },
  '反叛者': { key: 'rebel', label: '反叛者', color: 'text-purple-400 border-purple-500/30 bg-purple-500/10' },
  '冒险者': { key: 'adventurer', label: '冒险者', color: 'text-cyan-400 border-cyan-500/30 bg-cyan-500/10' },
  '天灾使者': { key: 'scourge', label: '天灾使者', color: 'text-amber-400 border-amber-500/30 bg-amber-500/10' },
}

export const GM_FACTION_TABS = ['统治者', '反叛者', '冒险者', '天灾使者']

export const ASSIGNED_FREE_ACTIONS = [
  { value: 'investigate_location', label: '调查地点' },
  { value: 'investigate_player', label: '调查玩家' },
  { value: 'produce', label: '生产' },
  { value: 'go_location', label: '前往地点' },
  { value: 'guard', label: '看守' },
  { value: 'trade', label: '交易' },
  { value: 'other', label: '其他自由行动' },
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
      type: 'govern_location',
      title: '安排监管',
      description: '安排民兵或治安官监管某地点，限制其他玩家进入与交互设施/NPC。',
      tooltip: '监管期间禁止其他玩家前往该地点、交互设施与 NPC。',
    },
    {
      type: 'assign_personnel',
      title: '安排人员',
      description: '强制某玩家/NPC 执行自由行动并共享结果；对方可拒绝（可作为审判理由）。',
      tooltip: '目标可拒绝，拒绝可能成为审判理由。',
    },
    {
      type: 'assign_guard',
      title: '安排看守',
      description: '夜晚前安排人员驻守地点，基础防御 +3，另加武器威胁值加成。',
      tooltip: '基础防御 +3，装备武器威胁值计入额外防御。',
    },
    {
      type: 'exploit_labor',
      title: '压榨劳工',
      description: '劳工建造值翻倍，但获得「受伤」：无法生产、格斗技能失效。',
      tooltip: '最多选 3 名劳工；建造 ×2，获得受伤，无法生产。',
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
      type: 'group_discussion',
      title: '群组讨论',
      description: '向选定玩家公开秘密地点路径，对方获得临时访问权限。',
      tooltip: '被通知玩家获得该秘密地点临时访问权。',
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
      tooltip: '选择调查类型与目标（仅一项），与行动提交调查相同。',
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
      description: '消耗行动点推进方舟建造进度，可选用特殊材料。',
      tooltip: '消耗自由行动点，可选特殊材料。',
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
      tooltip: '选择调查类型与目标（仅一项），与行动提交调查相同。',
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
