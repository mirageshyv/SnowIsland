/** 快速交互数据定义 */

export const INTERACTION_TYPES = [
  { value: 'quick_action', label: '快速行动', description: '执行一个不需要行动点的行动，由DM判定结果' },
  { value: 'supplementary_action', label: '补充行动', description: '对已提交的行动进行补充说明或修正' },
  { value: 'rule_consult', label: '规则咨询', description: '向DM询问游戏规则相关问题' },
  { value: 'ask_dm', label: '询问DM', description: '向DM提出任何其他问题或请求' },
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
  supplementary_action: { text: '补充行动', color: 'bg-sky-500/20 text-sky-300' },
  rule_consult: { text: '规则咨询', color: 'bg-teal-500/20 text-teal-300' },
  ask_dm: { text: '询问DM', color: 'bg-orange-500/20 text-orange-300' },
}
