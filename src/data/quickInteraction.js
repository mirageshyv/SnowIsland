/** 快速交互数据定义 */

export const INTERACTION_TYPES = [
  {
    value: 'quick_action',
    label: '快速行动',
    icon: '⚡',
    hint: '与免费搬运共用 2 条',
    description: '与免费搬运共用今日 2 条额度。隐秘居所不是地图地点，须用快速行动前往（可带人）。持有祭坛石可在此提交资源转换或地点防御+2；共鸣石感知也走快速行动。每条一个动作。',
    placeholder: '例如：前往已发现的隐秘居所（可写携带谁）、祭坛石资源转换（10单位从何转何）、为所在地点防御+2、共鸣石感知地脉，或其他立刻要做的事。写清对象、地点和预期效果。',
    accent: 'violet',
  },
  {
    value: 'free_transport',
    label: '免费搬运',
    icon: '📦',
    hint: '与快速行动共用 2 条',
    description: '与快速行动共用今日 2 条额度：交其中任意一种都会各占 1 条。每条一个动作。玩家↔仓库上限 50kg（装卸工服务端×2）。',
    placeholder: '',
    accent: 'cyan',
  },
  {
    value: 'supplementary_action',
    label: '补充行动',
    icon: '✎',
    hint: '补写说明',
    description: '对已提交的行动进行补充说明或修正',
    placeholder: '补充哪一条行动？漏了什么，或要改成什么？',
    accent: 'sky',
  },
  {
    value: 'rule_consult',
    label: '规则咨询',
    icon: '📘',
    hint: '问规则',
    description: '向DM询问游戏规则相关问题',
    placeholder: '你想确认哪条规则？写清场景和你的理解。',
    accent: 'teal',
  },
  {
    value: 'ask_dm',
    label: '询问DM',
    icon: '💬',
    hint: '其他问题',
    description: '向DM提出任何其他问题或请求',
    placeholder: '直接写下你想问主持人的内容。',
    accent: 'amber',
  },
]

export const INTERACTION_STATUS_OPTIONS = [
  { value: '', label: '全部状态' },
  { value: 'pending', label: '未处理' },
  { value: 'processed', label: '已处理' },
  { value: 'replied', label: '已回复' },
]

export const INTERACTION_TYPE_FILTER_OPTIONS = [
  { value: '', label: '全部类型' },
  { value: 'quick_action', label: '快速行动' },
  { value: 'free_transport', label: '免费搬运' },
  { value: 'supplementary_action', label: '补充行动' },
  { value: 'rule_consult', label: '规则咨询' },
  { value: 'ask_dm', label: '询问DM' },
]

export const STATUS_BADGE_MAP = {
  pending: { text: '未处理', color: 'bg-amber-500/20 text-amber-400' },
  processed: { text: '已处理', color: 'bg-blue-500/20 text-blue-400' },
  replied: { text: '已回复', color: 'bg-green-500/20 text-green-400' },
}

export const TYPE_BADGE_MAP = {
  quick_action: { text: '快速行动', color: 'bg-violet-500/20 text-violet-300' },
  free_transport: { text: '免费搬运', color: 'bg-cyan-500/20 text-cyan-300' },
  supplementary_action: { text: '补充行动', color: 'bg-sky-500/20 text-sky-300' },
  rule_consult: { text: '规则咨询', color: 'bg-teal-500/20 text-teal-300' },
  ask_dm: { text: '询问DM', color: 'bg-orange-500/20 text-orange-300' },
}
