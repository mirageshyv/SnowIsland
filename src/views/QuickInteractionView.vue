<script setup>
import { ref, computed, nextTick, onMounted } from 'vue'
import { quickInteractionAPI, gameStateAPI } from '@/utils/api.js'
import { INTERACTION_TYPES, STATUS_BADGE_MAP, TYPE_BADGE_MAP } from '@/data/quickInteraction.js'
import FreeTransportForm from '@/components/FreeTransportForm.vue'
import { useGameDayScope } from '@/composables/useGameDayScope.js'

const CHANNEL = {
  violet: { mark: '#c4b5fd', wash: 'rgba(139, 92, 246, 0.16)', glow: 'rgba(167, 139, 250, 0.35)' },
  sky: { mark: '#7dd3fc', wash: 'rgba(14, 165, 233, 0.14)', glow: 'rgba(56, 189, 248, 0.32)' },
  teal: { mark: '#5eead4', wash: 'rgba(20, 184, 166, 0.14)', glow: 'rgba(45, 212, 191, 0.32)' },
  amber: { mark: '#fbbf24', wash: 'rgba(245, 158, 11, 0.16)', glow: 'rgba(251, 191, 36, 0.35)' },
  cyan: { mark: '#67e8f9', wash: 'rgba(6, 182, 212, 0.14)', glow: 'rgba(34, 211, 238, 0.32)' },
}

const playerId = parseInt(localStorage.getItem('playerId') || '0')
const {
  daytimeEditable,
  viewOnlyDaytimeReason,
  loadGameState,
} = useGameDayScope()
const context = ref(null)
const loading = ref(true)
const submitting = ref(false)
const submitMessage = ref(null)
const selectedType = ref('')
const contentText = ref('')
const gameDay = ref(1)
const freeTransportForm = ref(null)
const historyFilter = ref('all')
const composerEl = ref(null)

const isFreeTransport = computed(() => selectedType.value === 'free_transport')
const usesQuickQuota = computed(
  () => selectedType.value === 'quick_action' || selectedType.value === 'free_transport',
)

const selectedDef = computed(() =>
  INTERACTION_TYPES.find((t) => t.value === selectedType.value) || null
)

const channel = computed(() => CHANNEL[selectedDef.value?.accent] || CHANNEL.violet)

const canSubmit = computed(() => {
  if (!selectedType.value || submitting.value) return false
  if (usesQuickQuota.value && quickActionRemaining.value <= 0) return false
  if (isFreeTransport.value) {
    return daytimeEditable.value
  }
  return contentText.value.trim().length >= 5
})

const remainingHint = computed(() => {
  const n = contentText.value.trim().length
  if (!selectedType.value) return '先选左侧一种交互'
  if (usesQuickQuota.value && quickActionRemaining.value <= 0) {
    return '今日共用额度已用完（快速行动 + 免费搬运 一共 2 条）'
  }
  if (isFreeTransport.value) {
    if (!daytimeEditable.value) return viewOnlyDaytimeReason.value || '当前不可提交免费搬运'
    return `可以发出了（占用 1 条共用额度，剩余 ${quickActionRemaining.value}）`
  }
  if (usesQuickQuota.value) {
    if (n < 5) return `再写 ${5 - n} 个字即可发出（将占用 1 条共用额度）`
    return `可以发出了（占用 1 条共用额度，剩余 ${quickActionRemaining.value}）`
  }
  if (n < 5) return `再写 ${5 - n} 个字即可发出`
  return '可以发给主持人了'
})

const quotaTypes = INTERACTION_TYPES.filter((t) => t.value === 'quick_action' || t.value === 'free_transport')
const otherTypes = INTERACTION_TYPES.filter((t) => t.value !== 'quick_action' && t.value !== 'free_transport')
const quotaExhausted = computed(() => quickActionRemaining.value <= 0)

const history = computed(() => context.value?.history || [])

const quickActionUsed = computed(() => context.value?.quickActionUsed ?? 0)
const quickActionLimit = computed(() => context.value?.quickActionDailyLimit ?? 2)
const quickActionRemaining = computed(() => context.value?.quickActionRemaining ?? 2)

const unreadReplyCount = computed(() =>
  history.value.filter((item) => item.status === 'replied').length
)

const waitingCount = computed(() =>
  history.value.filter((item) => item.status !== 'replied').length
)

const filteredHistory = computed(() => {
  if (historyFilter.value === 'waiting') {
    return history.value.filter((item) => item.status !== 'replied')
  }
  if (historyFilter.value === 'replied') {
    return history.value.filter((item) => item.status === 'replied')
  }
  return history.value
})

const groupedHistory = computed(() => {
  const groups = []
  const map = new Map()
  for (const item of filteredHistory.value) {
    const key = item.gameDay ?? 0
    if (!map.has(key)) {
      const group = { day: key, items: [] }
      map.set(key, group)
      groups.push(group)
    }
    map.get(key).items.push(item)
  }
  return groups
})

function channelOf(t) {
  return CHANNEL[t.accent] || CHANNEL.violet
}

function typeHint(t) {
  if (t.value !== 'quick_action' && t.value !== 'free_transport') return t.hint
  if (quickActionRemaining.value <= 0) return `额度已满 ${quickActionUsed.value}/${quickActionLimit.value}`
  return `共用剩 ${quickActionRemaining.value} 条`
}

async function selectType(value) {
  selectedType.value = selectedType.value === value ? '' : value
  await nextTick()
  if (selectedType.value && !isFreeTransport.value) {
    composerEl.value?.querySelector?.('textarea')?.focus()
  }
}

async function loadContext() {
  loading.value = true
  try {
    await loadGameState()
    const [ctx, gs] = await Promise.all([
      quickInteractionAPI.getContext(playerId),
      gameStateAPI.get(),
    ])
    context.value = ctx
    if (gs?.currentDay) gameDay.value = gs.currentDay
  } catch {
    context.value = null
  } finally {
    loading.value = false
  }
}

async function submitInteraction() {
  if (!canSubmit.value) return
  submitting.value = true
  submitMessage.value = null
  try {
    let res
    if (isFreeTransport.value) {
      const err = freeTransportForm.value?.validate?.()
      if (err) {
        submitMessage.value = { type: 'error', text: err }
        return
      }
      res = await quickInteractionAPI.submitFreeTransport({
        playerId,
        notes: freeTransportForm.value.buildNotes(),
        gameDay: gameDay.value,
      })
    } else {
      res = await quickInteractionAPI.submit({
        playerId,
        interactionType: selectedType.value,
        content: contentText.value.trim(),
        gameDay: gameDay.value,
      })
    }
    if (res?.success) {
      submitMessage.value = { type: 'success', text: '已交给主持人' }
      contentText.value = ''
      freeTransportForm.value?.reset?.()
      historyFilter.value = 'all'
      await loadContext()
    } else {
      submitMessage.value = { type: 'error', text: res?.message || '提交失败' }
    }
  } catch {
    submitMessage.value = { type: 'error', text: '提交失败，请重试' }
  } finally {
    submitting.value = false
    if (submitMessage.value) setTimeout(() => { submitMessage.value = null }, 3000)
  }
}

onMounted(() => loadContext())
</script>

<template>
  <div class="desk">
    <header class="desk-mast">
      <div class="desk-mast-copy">
        <p class="desk-kicker">Dispatch</p>
        <h1>快速交互</h1>
        <p class="desk-lead">不占白天行动槽。把短讯交给主持人——快速行动与免费搬运共用今日 2 条额度。</p>
      </div>
      <div class="desk-stamps">
        <span class="stamp">第 {{ gameDay }} 天</span>
        <span class="stamp stamp-wait">等候 {{ waitingCount }}</span>
        <span class="stamp stamp-ok">已回复 {{ unreadReplyCount }}</span>
      </div>
    </header>

    <div v-if="loading" class="desk-loading">
      <span class="desk-pulse" />
      <span>接通主持人频道…</span>
    </div>

    <div v-else class="desk-split">
      <aside class="desk-send">
        <section class="quota" :class="{ 'quota-full': quotaExhausted }">
          <div class="quota-copy">
            <p class="quota-title">今日共用额度</p>
            <p class="quota-sub">快速行动 · 免费搬运 各占 1 条，合计 {{ quickActionLimit }} 条</p>
          </div>
          <div class="quota-slots" :title="`已用 ${quickActionUsed} / ${quickActionLimit}`">
            <span
              v-for="n in quickActionLimit"
              :key="n"
              class="slot"
              :class="n <= quickActionUsed ? 'slot-used' : 'slot-open'"
            >
              {{ n <= quickActionUsed ? '已用' : '空' }}
            </span>
          </div>
        </section>

        <div class="channel-block">
          <p class="rail-label">占用额度</p>
          <div class="channel-stack">
            <button
              v-for="t in quotaTypes"
              :key="t.value"
              type="button"
              class="channel"
              :class="{
                'channel-on': selectedType === t.value,
                'channel-dim': quotaExhausted && selectedType !== t.value,
              }"
              :style="{
                '--ch': channelOf(t).mark,
                '--ch-wash': channelOf(t).wash,
                '--ch-glow': channelOf(t).glow,
              }"
              @click="selectType(t.value)"
            >
              <span class="channel-icon">{{ t.icon }}</span>
              <span class="channel-body">
                <span class="channel-name">{{ t.label }}</span>
                <span class="channel-hint">{{ typeHint(t) }}</span>
              </span>
              <span class="channel-tick" aria-hidden="true" />
            </button>
          </div>
        </div>

        <div class="channel-block">
          <p class="rail-label">不占额度</p>
          <div class="channel-stack">
            <button
              v-for="t in otherTypes"
              :key="t.value"
              type="button"
              class="channel"
              :class="{ 'channel-on': selectedType === t.value }"
              :style="{
                '--ch': channelOf(t).mark,
                '--ch-wash': channelOf(t).wash,
                '--ch-glow': channelOf(t).glow,
              }"
              @click="selectType(t.value)"
            >
              <span class="channel-icon">{{ t.icon }}</span>
              <span class="channel-body">
                <span class="channel-name">{{ t.label }}</span>
                <span class="channel-hint">{{ typeHint(t) }}</span>
              </span>
              <span class="channel-tick" aria-hidden="true" />
            </button>
          </div>
        </div>

        <section
          ref="composerEl"
          class="composer"
          :class="{ 'composer-live': selectedDef }"
          :style="selectedDef ? { '--ch': channel.mark, '--ch-wash': channel.wash, '--ch-glow': channel.glow } : {}"
        >
          <div class="composer-head">
            <div>
              <p class="composer-title">
                {{ selectedDef ? `写给主持人 · ${selectedDef.label}` : '选择一种交互' }}
              </p>
              <p class="composer-desc">
                {{ selectedDef ? selectedDef.description : '点上方频道开始。内容只有主持人看得到。' }}
              </p>
            </div>
            <span v-if="!isFreeTransport" class="composer-count">{{ contentText.length }}/2000</span>
          </div>

          <p v-if="isFreeTransport && viewOnlyDaytimeReason" class="composer-lock">
            {{ viewOnlyDaytimeReason }}
          </p>

          <FreeTransportForm
            v-if="isFreeTransport"
            ref="freeTransportForm"
            :player-id="playerId"
            :disabled="!daytimeEditable || submitting || quickActionRemaining <= 0"
          />

          <textarea
            v-else
            v-model="contentText"
            rows="7"
            maxlength="2000"
            class="composer-paper"
            :disabled="!selectedType"
            :placeholder="selectedDef?.placeholder || '先选一种交互，再写内容…'"
          />

          <div v-if="!isFreeTransport" class="composer-meter">
            <div
              class="composer-meter-fill"
              :style="{ width: `${Math.min(100, (contentText.trim().length / 5) * 100)}%` }"
            />
          </div>

          <div class="composer-foot">
            <p class="composer-hint" :class="{ 'is-ready': canSubmit }">{{ remainingHint }}</p>
            <button
              type="button"
              class="send"
              :class="{ 'send-ready': canSubmit }"
              :disabled="!canSubmit"
              @click="submitInteraction"
            >
              {{ submitting ? '送出中…' : '交给主持人' }}
            </button>
          </div>
        </section>

        <div v-if="submitMessage" class="toast" :class="submitMessage.type === 'success' ? 'toast-ok' : 'toast-err'">
          {{ submitMessage.text }}
        </div>
      </aside>

      <section class="desk-thread">
        <div class="thread-bar">
          <h2>往来记录</h2>
          <div class="thread-filters">
            <button
              type="button"
              class="tf"
              :class="{ 'tf-on': historyFilter === 'all' }"
              @click="historyFilter = 'all'"
            >全部 {{ history.length }}</button>
            <button
              type="button"
              class="tf"
              :class="{ 'tf-on': historyFilter === 'waiting' }"
              @click="historyFilter = 'waiting'"
            >等候 {{ waitingCount }}</button>
            <button
              type="button"
              class="tf"
              :class="{ 'tf-on': historyFilter === 'replied' }"
              @click="historyFilter = 'replied'"
            >已回复 {{ unreadReplyCount }}</button>
          </div>
        </div>

        <div v-if="!filteredHistory.length" class="thread-empty">
          <span class="thread-empty-mark">⌁</span>
          <p>{{ history.length ? '这个筛选下没有记录' : '还没有交互。选一个频道，写第一句给主持人。' }}</p>
        </div>

        <div v-else class="thread-list">
          <section v-for="group in groupedHistory" :key="group.day" class="day-group">
            <p class="day-rule"><span>第 {{ group.day }} 天</span></p>
            <article v-for="item in group.items" :key="item.id" class="slip" :class="{ 'slip-back': !!item.dmReply }">
              <header class="slip-meta">
                <span
                  class="slip-type"
                  :class="TYPE_BADGE_MAP[item.interactionType]?.color || 'bg-white/10 text-gray-400'"
                >
                  {{ item.interactionTypeLabel || TYPE_BADGE_MAP[item.interactionType]?.text || item.interactionType }}
                </span>
                <span
                  class="slip-status"
                  :class="STATUS_BADGE_MAP[item.status]?.color || 'bg-gray-500/20 text-gray-400'"
                >
                  {{ STATUS_BADGE_MAP[item.status]?.text || item.status }}
                </span>
              </header>
              <p class="slip-body">{{ item.content }}</p>
              <div v-if="item.dmReply" class="return">
                <p class="return-from">主持人回复</p>
                <p class="return-body">{{ item.dmReply }}</p>
              </div>
            </article>
          </section>
        </div>
      </section>
    </div>
  </div>
</template>

<style scoped>
.desk {
  --paper: #f3ead7;
  --ink: #1a1410;
  --oak: #2a221c;
  --oak-2: #3a2f26;
  --copper: #c9a36a;
  --copper-2: #e0c08a;
  --ember: #c45c2a;
  --moss: #6f9b74;
  --wait: #d4a017;
  --mist: rgba(243, 234, 215, 0.58);
  --line: rgba(201, 163, 106, 0.28);
  margin: -0.35rem -0.15rem 0;
  padding: 0.35rem 0.15rem 0.2rem;
  color: var(--paper);
  font-family: ui-sans-serif, "PingFang SC", "Hiragino Sans GB", "Noto Sans SC", "Microsoft YaHei", system-ui, sans-serif;
  background:
    radial-gradient(900px 280px at 0% 0%, rgba(201, 163, 106, 0.09), transparent 55%),
    radial-gradient(700px 240px at 100% 8%, rgba(111, 155, 116, 0.06), transparent 50%);
}

.desk-mast {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-end;
  justify-content: space-between;
  gap: 1rem;
  margin-bottom: 1.5rem;
  padding-bottom: 1.1rem;
  border-bottom: 1px solid var(--line);
}

.desk-kicker {
  margin: 0 0 0.2rem;
  font-size: 0.68rem;
  letter-spacing: 0.28em;
  text-transform: uppercase;
  color: var(--copper);
}

.desk-mast h1 {
  margin: 0;
  font-size: clamp(1.6rem, 3vw, 2.05rem);
  font-weight: 600;
  letter-spacing: 0.04em;
  color: var(--paper);
}

.desk-lead {
  margin: 0.45rem 0 0;
  max-width: 36rem;
  font-size: 0.88rem;
  line-height: 1.55;
  color: var(--mist);
}

.desk-stamps {
  display: flex;
  flex-wrap: wrap;
  gap: 0.45rem;
}

.stamp {
  display: inline-flex;
  align-items: center;
  padding: 0.28rem 0.7rem;
  border: 1px dashed var(--line);
  border-radius: 999px;
  font-size: 0.72rem;
  letter-spacing: 0.06em;
  color: var(--mist);
}

.stamp-wait {
  border-color: rgba(212, 160, 23, 0.45);
  color: #f0d48a;
}

.stamp-ok {
  border-color: rgba(111, 155, 116, 0.5);
  color: #b7d4b8;
}

.desk-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.85rem;
  padding: 4.5rem 0;
  color: var(--mist);
  font-size: 0.9rem;
}

.desk-pulse {
  width: 2.4rem;
  height: 2.4rem;
  border: 2px solid var(--copper);
  border-radius: 50%;
  animation: desk-ping 1.4s ease-out infinite;
}

@keyframes desk-ping {
  0% { transform: scale(0.7); opacity: 1; box-shadow: 0 0 0 0 rgba(201, 163, 106, 0.5); }
  100% { transform: scale(1.15); opacity: 0; box-shadow: 0 0 0 12px rgba(201, 163, 106, 0); }
}

.desk-split {
  display: grid;
  gap: 1.25rem;
}

@media (min-width: 1024px) {
  .desk-split {
    grid-template-columns: minmax(0, 1.05fr) minmax(0, 0.95fr);
    align-items: start;
  }

  .desk-thread {
    position: sticky;
    top: 0.75rem;
    max-height: calc(100vh - 5.5rem);
    overflow: auto;
  }
}

.desk-send,
.desk-thread {
  min-width: 0;
}

.quota {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 0.85rem;
  margin-bottom: 1rem;
  padding: 0.85rem 1rem;
  border: 1px solid var(--line);
  border-radius: 0.35rem;
  background:
    linear-gradient(180deg, rgba(201, 163, 106, 0.12), rgba(26, 20, 16, 0.35)),
    repeating-linear-gradient(-45deg, transparent, transparent 6px, rgba(201, 163, 106, 0.04) 6px, rgba(201, 163, 106, 0.04) 7px);
}

.quota-full {
  border-color: rgba(196, 92, 42, 0.45);
  background:
    linear-gradient(180deg, rgba(196, 92, 42, 0.14), rgba(26, 20, 16, 0.4)),
    repeating-linear-gradient(-45deg, transparent, transparent 6px, rgba(196, 92, 42, 0.05) 6px, rgba(196, 92, 42, 0.05) 7px);
}

.quota-title {
  margin: 0;
  font-size: 0.92rem;
  font-weight: 600;
}

.quota-sub {
  margin: 0.2rem 0 0;
  font-size: 0.72rem;
  color: var(--mist);
}

.quota-slots {
  display: flex;
  gap: 0.4rem;
}

.slot {
  min-width: 2.6rem;
  padding: 0.32rem 0.55rem;
  border-radius: 0.2rem;
  font-size: 0.68rem;
  letter-spacing: 0.08em;
  text-align: center;
}

.slot-open {
  border: 1px dashed var(--copper);
  color: var(--copper-2);
  background: rgba(201, 163, 106, 0.08);
}

.slot-used {
  border: 1px solid rgba(201, 163, 106, 0.55);
  color: #1a1410;
  background: var(--copper);
}

.quota-full .slot-used {
  border-color: rgba(196, 92, 42, 0.7);
  background: #d4784a;
}

.channel-block {
  margin-bottom: 0.75rem;
}

.rail-label {
  margin: 0 0 0.4rem 0.1rem;
  font-size: 0.68rem;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: var(--copper);
}

.channel-stack {
  display: grid;
  gap: 0.4rem;
}

.channel {
  display: grid;
  grid-template-columns: auto 1fr auto;
  align-items: center;
  gap: 0.7rem;
  width: 100%;
  padding: 0.7rem 0.8rem;
  text-align: left;
  border: 1px solid rgba(243, 234, 215, 0.1);
  border-radius: 0.35rem;
  background: rgba(26, 20, 16, 0.45);
  color: inherit;
  cursor: pointer;
  transition: border-color 0.2s ease, background 0.2s ease, transform 0.2s ease;
}

.channel:hover {
  border-color: color-mix(in srgb, var(--ch) 55%, transparent);
  background: var(--ch-wash);
}

.channel-on {
  border-color: var(--ch);
  background: var(--ch-wash);
  box-shadow: inset 3px 0 0 var(--ch), 0 0 0 1px var(--ch-glow);
}

.channel-dim {
  opacity: 0.62;
}

.channel-icon {
  display: inline-flex;
  width: 2.1rem;
  height: 2.1rem;
  align-items: center;
  justify-content: center;
  border-radius: 0.28rem;
  background: rgba(243, 234, 215, 0.08);
  font-size: 1rem;
}

.channel-on .channel-icon {
  background: var(--ch-wash);
}

.channel-name {
  display: block;
  font-size: 0.95rem;
  font-weight: 600;
}

.channel-hint {
  display: block;
  margin-top: 0.12rem;
  font-size: 0.72rem;
  color: var(--mist);
}

.channel-on .channel-hint {
  color: var(--ch);
}

.channel-tick {
  width: 0.55rem;
  height: 0.55rem;
  border: 1px solid rgba(243, 234, 215, 0.25);
  border-radius: 50%;
}

.channel-on .channel-tick {
  border-color: var(--ch);
  background: var(--ch);
  box-shadow: 0 0 8px var(--ch-glow);
}

.composer {
  margin-top: 0.85rem;
  padding: 1rem 1.05rem 1.05rem;
  border: 1px solid rgba(243, 234, 215, 0.1);
  border-radius: 0.4rem;
  background: rgba(18, 14, 12, 0.55);
}

.composer-live {
  border-color: color-mix(in srgb, var(--ch) 45%, transparent);
  box-shadow: 0 0 0 1px color-mix(in srgb, var(--ch-glow) 40%, transparent);
}

.composer-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 0.75rem;
  margin-bottom: 0.85rem;
}

.composer-title {
  margin: 0;
  font-size: 0.95rem;
  font-weight: 600;
}

.composer-desc {
  margin: 0.3rem 0 0;
  font-size: 0.75rem;
  line-height: 1.5;
  color: var(--mist);
}

.composer-count {
  flex-shrink: 0;
  font-size: 0.68rem;
  font-variant-numeric: tabular-nums;
  color: var(--mist);
}

.composer-lock {
  margin: 0 0 0.75rem;
  padding: 0.55rem 0.75rem;
  border: 1px dashed rgba(212, 160, 23, 0.4);
  border-radius: 0.3rem;
  background: rgba(212, 160, 23, 0.08);
  color: #f0d48a;
  font-size: 0.75rem;
}

.composer-paper {
  display: block;
  width: 100%;
  min-height: 10.5rem;
  resize: vertical;
  padding: 0.85rem 0.95rem;
  border: 1px solid rgba(201, 163, 106, 0.22);
  border-radius: 0.2rem;
  background:
    linear-gradient(180deg, rgba(243, 234, 215, 0.06), rgba(243, 234, 215, 0.03)),
    repeating-linear-gradient(
      to bottom,
      transparent,
      transparent 1.55rem,
      rgba(201, 163, 106, 0.08) 1.55rem,
      rgba(201, 163, 106, 0.08) 1.6rem
    );
  color: var(--paper);
  font: inherit;
  font-size: 0.92rem;
  line-height: 1.6rem;
  outline: none;
}

.composer-paper::placeholder {
  color: rgba(243, 234, 215, 0.32);
}

.composer-paper:focus {
  border-color: var(--copper);
  box-shadow: 0 0 0 1px rgba(201, 163, 106, 0.28);
}

.composer-paper:disabled {
  opacity: 0.5;
}

.composer-meter {
  height: 2px;
  margin-top: 0.65rem;
  overflow: hidden;
  background: rgba(243, 234, 215, 0.08);
}

.composer-meter-fill {
  height: 100%;
  background: var(--ch, var(--copper));
  transition: width 0.25s ease;
}

.composer-foot {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 0.75rem;
  margin-top: 0.9rem;
}

.composer-hint {
  margin: 0;
  flex: 1;
  font-size: 0.75rem;
  color: var(--mist);
}

.composer-hint.is-ready {
  color: #b7d4b8;
}

.send {
  min-width: 8.5rem;
  padding: 0.55rem 1.15rem;
  border: 1px solid rgba(243, 234, 215, 0.15);
  border-radius: 999px;
  background: rgba(243, 234, 215, 0.08);
  color: rgba(243, 234, 215, 0.45);
  font: inherit;
  font-size: 0.85rem;
  letter-spacing: 0.06em;
  cursor: not-allowed;
}

.send-ready {
  border-color: var(--copper);
  background: linear-gradient(180deg, var(--copper-2), var(--copper));
  color: var(--ink);
  cursor: pointer;
  box-shadow: 0 6px 16px rgba(201, 163, 106, 0.22);
}

.send-ready:hover {
  filter: brightness(1.06);
}

.toast {
  margin-top: 0.85rem;
  padding: 0.55rem 0.9rem;
  border-radius: 999px;
  text-align: center;
  font-size: 0.82rem;
}

.toast-ok {
  border: 1px solid rgba(111, 155, 116, 0.4);
  background: rgba(111, 155, 116, 0.12);
  color: #c5e0c6;
}

.toast-err {
  border: 1px solid rgba(196, 92, 42, 0.45);
  background: rgba(196, 92, 42, 0.12);
  color: #f0b49a;
}

.thread-bar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 0.65rem;
  margin-bottom: 0.9rem;
}

.thread-bar h2 {
  margin: 0;
  font-size: 1.05rem;
  font-weight: 600;
  letter-spacing: 0.04em;
}

.thread-filters {
  display: flex;
  flex-wrap: wrap;
  gap: 0.3rem;
}

.tf {
  padding: 0.22rem 0.55rem;
  border: 1px solid transparent;
  border-radius: 999px;
  background: transparent;
  color: var(--mist);
  font: inherit;
  font-size: 0.7rem;
  letter-spacing: 0.04em;
  cursor: pointer;
}

.tf-on {
  border-color: var(--line);
  color: var(--paper);
  background: rgba(201, 163, 106, 0.12);
}

.thread-empty {
  padding: 3rem 1rem;
  border: 1px dashed var(--line);
  border-radius: 0.4rem;
  text-align: center;
  color: var(--mist);
  font-size: 0.88rem;
}

.thread-empty-mark {
  display: block;
  margin-bottom: 0.45rem;
  color: var(--copper);
  font-size: 1.8rem;
}

.thread-list {
  display: flex;
  flex-direction: column;
  gap: 1.1rem;
}

.day-rule {
  display: flex;
  align-items: center;
  gap: 0.65rem;
  margin: 0 0 0.55rem;
  font-size: 0.68rem;
  letter-spacing: 0.16em;
  text-transform: uppercase;
  color: var(--copper);
}

.day-rule::before,
.day-rule::after {
  content: "";
  flex: 1;
  height: 1px;
  background: var(--line);
}

.slip {
  position: relative;
  padding: 0.85rem 0.95rem 0.95rem;
  border: 1px solid rgba(243, 234, 215, 0.1);
  border-radius: 0.2rem 0.55rem 0.2rem 0.2rem;
  background:
    linear-gradient(90deg, rgba(201, 163, 106, 0.12) 0, rgba(201, 163, 106, 0.12) 4px, transparent 4px),
    rgba(26, 20, 16, 0.55);
}

.slip + .slip {
  margin-top: 0.55rem;
}

.slip-back {
  border-color: rgba(111, 155, 116, 0.28);
}

.slip-meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 0.4rem;
  margin-bottom: 0.55rem;
}

.slip-type,
.slip-status {
  padding: 0.12rem 0.5rem;
  border-radius: 999px;
  font-size: 0.68rem;
}

.slip-status {
  margin-left: auto;
}

.slip-body,
.return-body {
  margin: 0;
  white-space: pre-wrap;
  font-size: 0.88rem;
  line-height: 1.65;
  color: rgba(243, 234, 215, 0.92);
}

.return {
  margin-top: 0.7rem;
  padding: 0.65rem 0.75rem;
  border-left: 2px solid var(--moss);
  background: rgba(111, 155, 116, 0.1);
}

.return-from {
  margin: 0 0 0.28rem;
  font-size: 0.68rem;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: #b7d4b8;
}
</style>
