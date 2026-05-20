<script setup>
import { ref, computed, onMounted } from 'vue'
import { quickInteractionAPI, gameStateAPI } from '@/utils/api.js'
import { INTERACTION_TYPES, STATUS_BADGE_MAP, TYPE_BADGE_MAP } from '@/data/quickInteraction.js'

const playerId = parseInt(localStorage.getItem('playerId') || '0')
const context = ref(null)
const loading = ref(true)
const submitting = ref(false)
const submitMessage = ref(null)
const selectedType = ref('')
const contentText = ref('')
const gameDay = ref(1)

const selectedDef = computed(() =>
  INTERACTION_TYPES.find(t => t.value === selectedType.value) || null
)

const canSubmit = computed(() =>
  selectedType.value && contentText.value.trim().length >= 5 && !submitting.value
)

async function loadContext() {
  loading.value = true
  try {
    const [ctx, gs] = await Promise.all([
      quickInteractionAPI.getContext(playerId, gameDay.value),
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
    const res = await quickInteractionAPI.submit({
      playerId,
      interactionType: selectedType.value,
      content: contentText.value.trim(),
      gameDay: gameDay.value,
    })
    if (res?.success) {
      submitMessage.value = { type: 'success', text: '提交成功' }
      contentText.value = ''
      selectedType.value = ''
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

const selectClass =
  'w-full appearance-none bg-white/5 border border-white/10 rounded-xl px-4 py-3 pr-10 text-gray-200 text-sm focus:outline-none focus:border-violet-500/50'
const textareaClass =
  'w-full resize-none bg-white/5 border border-white/10 rounded-xl px-4 py-3 text-gray-200 text-sm placeholder:text-gray-600 focus:outline-none focus:border-violet-500/50'

onMounted(() => loadContext())
</script>

<template>
  <div>
    <div class="text-center mb-10">
      <h1 class="text-white text-2xl md:text-3xl font-semibold tracking-tight mb-2">快速交互</h1>
      <p class="text-gray-500 text-sm">向DM提交快速行动、补充说明或规则咨询</p>
      <p class="text-gray-600 text-xs mt-1">游戏第 {{ gameDay }} 天</p>
    </div>

      <div v-if="loading" class="flex justify-center py-20">
        <div class="w-12 h-12 border-4 border-violet-500 border-t-transparent rounded-full animate-spin" />
      </div>

      <template v-else>
        <div class="relative bg-gradient-to-br from-[#1a2332] to-[#0f1419] border border-white/10 rounded-3xl p-6 mb-8">
          <div class="flex items-center gap-3 mb-5">
            <span
              class="inline-flex w-9 h-9 items-center justify-center rounded-lg bg-violet-500/20 border border-violet-500/30 text-violet-300 text-sm"
            >⚡</span>
            <h2 class="text-white text-lg">提交快速交互</h2>
          </div>

          <div class="space-y-4">
            <div>
              <label class="block text-gray-500 text-xs mb-2">交互类型</label>
              <select v-model="selectedType" :class="selectClass">
                <option value="">请选择类型</option>
                <option v-for="t in INTERACTION_TYPES" :key="t.value" :value="t.value">{{ t.label }}</option>
              </select>
            </div>

            <div v-if="selectedDef" class="rounded-xl border border-white/10 bg-black/20 px-4 py-3">
              <p class="text-gray-300 text-sm">{{ selectedDef.description }}</p>
            </div>

            <div v-if="selectedType">
              <label class="block text-gray-500 text-xs mb-2">
                交互内容
                <span class="text-red-400">（必填，至少5个字符）</span>
              </label>
              <textarea
                v-model="contentText"
                rows="5"
                :class="textareaClass"
                placeholder="请详细描述你的交互内容..."
                :maxlength="2000"
              />
              <p class="text-gray-600 text-xs mt-1 text-right">{{ contentText.length }} / 2000</p>
            </div>
          </div>
        </div>

        <div v-if="submitMessage" class="flex justify-center mb-4">
          <div
            :class="[
              'px-5 py-2.5 rounded-xl text-sm',
              submitMessage.type === 'success' ? 'bg-green-500/20 text-green-400 border border-green-500/30' : 'bg-red-500/20 text-red-400 border border-red-500/30',
            ]"
          >
            {{ submitMessage.text }}
          </div>
        </div>

        <div class="flex justify-center pb-4">
          <button
            type="button"
            :disabled="!canSubmit"
            class="min-w-[200px] bg-gradient-to-r from-violet-500 to-violet-600 hover:from-violet-600 hover:to-violet-700 disabled:from-gray-600 disabled:to-gray-600 text-white px-8 py-3 rounded-xl text-sm font-medium"
            @click="submitInteraction"
          >
            {{ submitting ? '提交中...' : '提交交互' }}
          </button>
        </div>

        <div v-if="context?.history?.length" class="mt-8 space-y-3">
          <h3 class="text-white text-lg font-medium">已提交的交互记录</h3>
          <div
            v-for="item in context.history"
            :key="item.id"
            class="bg-gradient-to-br from-[#1a2332] to-[#0f1419] border border-white/10 rounded-xl p-4"
          >
            <div class="flex items-center justify-between mb-2">
              <div class="flex items-center gap-2">
                <span
                  class="text-xs px-2 py-0.5 rounded-full"
                  :class="TYPE_BADGE_MAP[item.interactionType]?.color || 'bg-white/10 text-gray-400'"
                >
                  {{ item.interactionTypeLabel || TYPE_BADGE_MAP[item.interactionType]?.text || item.interactionType }}
                </span>
                <span class="text-gray-600 text-xs">第{{ item.gameDay }}天</span>
              </div>
              <span
                class="text-xs px-2 py-0.5 rounded-full"
                :class="STATUS_BADGE_MAP[item.status]?.color || 'bg-gray-500/20 text-gray-400'"
              >
                {{ STATUS_BADGE_MAP[item.status]?.text || item.status }}
              </span>
            </div>
            <div class="text-gray-300 text-sm whitespace-pre-wrap mb-2">{{ item.content }}</div>
            <div
              v-if="item.dmReply"
              class="text-green-300 text-sm whitespace-pre-wrap bg-green-500/5 border border-green-500/20 rounded-lg p-3"
            >
              <span class="text-green-400 font-medium">DM回复：</span>{{ item.dmReply }}
            </div>
          </div>
        </div>

        <div v-else-if="!loading" class="text-center py-10 text-gray-500 text-sm">
          暂无交互记录
        </div>
      </template>
  </div>
</template>
