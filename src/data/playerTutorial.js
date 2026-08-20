/** Player-hub cinematic tutorial: step script and faction-page copy. */

export const TUTORIAL_STORAGE_PREFIX = 'si-player-tutorial-v1-'

export const FACTION_PAGE_BY_FACTION = {
  冒险者: {
    tab: 'ark',
    title: '方舟建造进度',
    body: '在这里投入物资建造方舟。',
  },
  统治者: {
    tab: 'shelter',
    title: '统治者避难所',
    body: '在这里查看并推进避难所建设。',
  },
  反叛者: {
    tab: 'milestone',
    title: '反叛者里程碑',
    body: '在这里追踪革命里程碑。',
  },
  天灾使者: {
    tab: 'catastrophe',
    title: '天灾降临',
    body: '在这里打出天灾卡牌。',
  },
}

const CORE_STEPS = [
  {
    id: 'welcome',
    tab: 'info',
    target: 'welcome',
    sidebarMode: 'content',
    title: '欢迎来到雪岛',
    body: '这是你的玩家中心。用「上一步 / 下一步」查看常用功能，可随时跳过。',
  },
  {
    id: 'sidebar',
    tab: 'info',
    target: 'sidebar',
    sidebarMode: 'open',
    title: '功能导航',
    body: '用左侧列表切换功能。点「下一步」继续。',
  },
  {
    id: 'profile',
    tab: 'info',
    target: 'profile',
    sidebarMode: 'content',
    title: '个人信息',
    body: '这里是你的名字、状态，以及当前天数与阶段。',
  },
  {
    id: 'survival',
    tab: 'info',
    target: 'survival',
    sidebarMode: 'content',
    title: '进食与取暖',
    body: '每天提交食物和取暖。若当日未满足，次日会陷入「虚弱」。',
  },
  {
    id: 'materials',
    tab: 'materials',
    target: 'materials',
    sidebarMode: 'content',
    title: '背包',
    body: '背包里是你随身携带的道具、武器和物资。',
  },
  {
    id: 'notebook',
    tab: 'notebook',
    target: 'notebook',
    sidebarMode: 'content',
    title: '笔记本',
    body: '记下线索和计划，最多 30 页，输入后自动保存。',
  },
  {
    id: 'actions',
    tab: 'actions',
    target: 'actions',
    sidebarMode: 'content',
    title: '个人行动提交',
    body: '每天有两个个人行动位，白天在这里提交。',
  },
  {
    id: 'nightActions',
    tab: 'nightActions',
    target: 'nightActions',
    sidebarMode: 'content',
    requiresNight: true,
    title: '夜晚行动提交',
    body: '夜晚可以提交夜间行动，也可以探索岛屿。',
  },
  {
    id: 'faction',
    tab: '',
    target: '',
    sidebarMode: 'content',
    factionStep: true,
    title: '',
    body: '',
  },
  {
    id: 'trade',
    tab: 'trade',
    target: 'trade',
    sidebarMode: 'content',
    title: '交易',
    body: '与其他玩家交换物资。侧栏红点表示有待处理交易。',
  },
  {
    id: 'npc',
    tab: 'npc',
    target: 'npc',
    sidebarMode: 'content',
    title: 'NPC 交互',
    body: '已认识的 NPC 可以对话、交易或请求帮助。',
  },
  {
    id: 'ruleBook',
    tab: 'ruleBook',
    target: 'ruleBook',
    sidebarMode: 'content',
    title: '规则书',
    body: '查看海岛地图和主持人发放的线索文献。',
  },
  {
    id: 'close',
    tab: 'info',
    target: 'close',
    sidebarMode: 'content',
    title: '引导结束',
    body: '你可以随时在「个人信息」页点击「重新观看引导」再看一遍。',
  },
]

export function tutorialStorageKey(playerId) {
  return `${TUTORIAL_STORAGE_PREFIX}${playerId}`
}

export function hasSeenPlayerTutorial(playerId) {
  try {
    return localStorage.getItem(tutorialStorageKey(playerId)) === '1'
  } catch {
    return false
  }
}

export function markPlayerTutorialSeen(playerId) {
  try {
    localStorage.setItem(tutorialStorageKey(playerId), '1')
  } catch {
    /* ignore quota / private mode */
  }
}

/**
 * Build the run list for this player: skip night if they have no faction,
 * and include exactly one faction-page step when they have a dedicated page.
 */
export function buildTutorialSteps(faction) {
  const hasFaction = !!faction
  const factionPage = FACTION_PAGE_BY_FACTION[faction] || null

  return CORE_STEPS.flatMap((step) => {
    if (step.requiresNight && !hasFaction) return []
    if (step.factionStep) {
      if (!factionPage) return []
      return [{
        ...step,
        id: `faction-${factionPage.tab}`,
        tab: factionPage.tab,
        target: factionPage.tab,
        title: factionPage.title,
        body: factionPage.body,
      }]
    }
    return [{ ...step }]
  })
}
