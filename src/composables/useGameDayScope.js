import { ref, computed } from 'vue'
import { gameStateAPI } from '@/utils/api.js'

/**
 * Shared game-day / phase rules for action submit pages.
 * - Daytime (personal + faction) on day X: editable only when current day === X and phase is DAY.
 * - Night actions on day X: editable only when current day <= X (locked when current > X).
 */
export function useGameDayScope() {
  const currentGameDay = ref(1)
  const currentPhase = ref('DAY')
  const viewGameDay = ref(1)
  const gameStateReady = ref(false)

  const dayOptions = computed(() => {
    const max = Math.max(1, currentGameDay.value || 1)
    return Array.from({ length: max }, (_, i) => i + 1)
  })

  const phaseLabel = computed(() => (currentPhase.value === 'NIGHT' ? '夜晚' : '白天'))

  const daytimeEditable = computed(
    () => viewGameDay.value === currentGameDay.value && currentPhase.value === 'DAY'
  )

  const nightEditable = computed(() => currentGameDay.value <= viewGameDay.value)

  const viewOnlyDaytimeReason = computed(() => {
    if (daytimeEditable.value) return ''
    if (currentGameDay.value > viewGameDay.value) return '该天数已过，仅可查看已提交内容'
    if (currentGameDay.value < viewGameDay.value) return '尚未到达该天数'
    if (currentPhase.value === 'NIGHT') return '当前为夜晚阶段，白天行动仅可查看'
    return '仅可查看'
  })

  const viewOnlyNightReason = computed(() => {
    if (nightEditable.value) return ''
    if (currentGameDay.value > viewGameDay.value) return '该天数已过，仅可查看已提交内容'
    return '仅可查看'
  })

  async function loadGameState() {
    try {
      const s = await gameStateAPI.get()
      if (!s || s.success === false) return
      const day = Number(s.currentDay) || 1
      currentGameDay.value = day
      currentPhase.value = s.currentPhase === 'NIGHT' ? 'NIGHT' : 'DAY'
      if (!gameStateReady.value) {
        viewGameDay.value = day
        gameStateReady.value = true
      } else if (viewGameDay.value > day) {
        viewGameDay.value = day
      }
    } catch (e) {
      console.error('loadGameState failed:', e)
    }
  }

  function syncFromContext(ctx) {
    if (!ctx) return
    if (ctx.currentGameDay != null) currentGameDay.value = Number(ctx.currentGameDay) || currentGameDay.value
    if (ctx.currentPhase != null) currentPhase.value = ctx.currentPhase === 'NIGHT' ? 'NIGHT' : 'DAY'
    if (ctx.daytimeActionsEditable != null && ctx.nightActionsEditable == null) {
      // daytime-only context
    }
  }

  return {
    currentGameDay,
    currentPhase,
    viewGameDay,
    gameStateReady,
    dayOptions,
    phaseLabel,
    daytimeEditable,
    nightEditable,
    viewOnlyDaytimeReason,
    viewOnlyNightReason,
    loadGameState,
    syncFromContext,
  }
}
