import { computed, nextTick, onUnmounted, ref } from 'vue'
import { buildTutorialSteps, hasSeenPlayerTutorial, markPlayerTutorialSeen } from '../data/playerTutorial.js'

function delay(ms) {
  return new Promise((resolve) => setTimeout(resolve, ms))
}

/**
 * Click-through player-hub tutorial.
 * First visit offers a prompt; Previous / Next move between steps.
 */
export function usePlayerTutorial({
  playerId,
  getFaction,
  setTab,
  collapsed,
  mobileOpen,
  isMobile,
}) {
  const prompting = ref(false)
  const playing = ref(false)
  const steps = ref([])
  const stepIndex = ref(0)
  const measureGen = ref(0)

  const currentStep = computed(() => steps.value[stepIndex.value] || null)
  const isCardStep = computed(() => {
    const target = currentStep.value?.target
    return target === 'welcome' || target === 'close'
  })
  const stepCount = computed(() => steps.value.length)
  const canPrev = computed(() => playing.value && stepIndex.value > 0)
  const isLast = computed(() => playing.value && stepIndex.value >= steps.value.length - 1)
  const active = computed(() => prompting.value || playing.value)

  let runId = 0
  let savedCollapsed = false
  let savedMobileOpen = false
  let offered = false

  function applySidebar(step) {
    if (isMobile.value) {
      mobileOpen.value = step?.sidebarMode === 'open'
    } else {
      collapsed.value = false
    }
  }

  function restoreShell() {
    collapsed.value = savedCollapsed
    mobileOpen.value = savedMobileOpen
  }

  function teardown() {
    prompting.value = false
    playing.value = false
    restoreShell()
    setTab('info')
  }

  async function goTo(index) {
    const id = ++runId
    const step = steps.value[index]
    if (!step) {
      finish()
      return
    }
    stepIndex.value = index
    applySidebar(step)
    if (step.tab) setTab(step.tab)
    await nextTick()
    const waitMs = step.target === 'welcome' || step.target === 'close' ? 80 : 380
    await delay(waitMs)
    if (id !== runId || !playing.value) return
    measureGen.value += 1
  }

  function finish() {
    runId += 1
    markPlayerTutorialSeen(playerId)
    teardown()
  }

  function skip() {
    if (!playing.value && !prompting.value) return
    runId += 1
    markPlayerTutorialSeen(playerId)
    teardown()
  }

  function next() {
    if (!playing.value) return
    if (isLast.value) {
      finish()
      return
    }
    goTo(stepIndex.value + 1)
  }

  function prev() {
    if (!canPrev.value) return
    goTo(stepIndex.value - 1)
  }

  async function start({ replay = false } = {}) {
    if (playing.value) return
    if (!replay && hasSeenPlayerTutorial(playerId)) return

    steps.value = buildTutorialSteps(getFaction())
    if (!steps.value.length) return

    savedCollapsed = collapsed.value
    savedMobileOpen = mobileOpen.value
    prompting.value = false
    playing.value = true
    stepIndex.value = 0
    await goTo(0)
  }

  function replay() {
    prompting.value = false
    return start({ replay: true })
  }

  function tryOfferPrompt() {
    if (offered || playing.value || prompting.value) return
    if (hasSeenPlayerTutorial(playerId)) {
      offered = true
      return
    }
    offered = true
    prompting.value = true
  }

  function acceptPrompt() {
    if (!prompting.value) return
    start()
  }

  function declinePrompt() {
    if (!prompting.value) return
    markPlayerTutorialSeen(playerId)
    prompting.value = false
  }

  onUnmounted(() => {
    runId += 1
    playing.value = false
    prompting.value = false
  })

  return {
    prompting,
    playing,
    active,
    steps,
    stepIndex,
    stepCount,
    currentStep,
    isCardStep,
    canPrev,
    isLast,
    measureGen,
    start,
    replay,
    skip,
    next,
    prev,
    tryOfferPrompt,
    acceptPrompt,
    declinePrompt,
  }
}
