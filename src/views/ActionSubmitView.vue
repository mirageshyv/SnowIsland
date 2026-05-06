<script setup>
import { ref } from 'vue'

/** 为 true 时嵌入玩家中心主区域（不套全屏背景与额外留白） */
defineProps({
  embedded: { type: Boolean, default: false },
})

const showActionHelpModal = ref(false)

/** 行动一 */
const action1Type = ref('')
const action1Target = ref('')
const action1Notes = ref('')
const action1Result = ref('')

/** 行动二 */
const action2Type = ref('')
const action2Target = ref('')
const action2Notes = ref('')
const action2Result = ref('')

const actionTypeOptions = [
  { value: '', label: '请选择行动' },
  { value: 'go_location', label: '前往地点' },
  { value: 'investigate_player', label: '调查玩家' },
  { value: 'produce', label: '生产' },
  { value: 'use_trait', label: '使用特性' },
  { value: 'use_profession', label: '使用职业技能' },
  { value: 'hide', label: '隐藏' },
]

const actionHelpEntries = [
  {
    title: '前往地点',
    body:
      '选择一个地点进行探索，可以获得对应地点的信息（可能的资源信息（取决于设施），防御值，NPC 名单）。同时可以选择使用非上锁的设施和地点 NPC 简单交互。',
  },
  {
    title: '调查玩家',
    body:
      '选择一名玩家，知晓对方的自由行动；1/2 概率获知阵营行动。无法调查拥有潜行技能的玩家；无法调查昨天使用隐藏行动的玩家。',
  },
  {
    title: '生产',
    body: '根据职业技能生产对应资源。',
  },
  {
    title: '使用特性',
    body: '使用需要行动点的特性。',
  },
  {
    title: '使用职业技能',
    body:
      '使用可能的职业技能。如果没有标有「行动」的技能，则不需要玩家行动就可以执行。',
  },
  {
    title: '隐藏',
    body:
      '隐藏自己：第二天不会被调查，也无法被私聊，无法成为统治者与密谋的行动目标。',
  },
]

const placeholderTargets = ['仓库区', '指挥中心', '李华', '王芳', '张伟']

function submitActions() {
  action1Result.value = action1Type.value
    ? '已提交，结果将在此显示。'
    : '结果将在此显示'
  action2Result.value = action2Type.value
    ? '已提交，结果将在此显示。'
    : '结果将在此显示'
}
</script>

<template>
  <div :class="{ 'min-h-screen bg-[#0a0e1a] py-8 px-4 md:px-8': !embedded }">
  <div class="max-w-6xl mx-auto">
    <div
      class="mb-6 rounded-xl border border-amber-500/30 bg-amber-500/10 px-4 py-3 text-sm text-amber-100/95 leading-relaxed"
    >
      <p class="font-medium text-amber-200/95 mb-1">说明</p>
      <p>
        本页仅为<strong class="font-semibold text-amber-100">前端展示演示</strong>，不接后端、不保存真实提交。完整玩法中还包括<strong
          class="font-semibold text-amber-100"
        >夜间行动</strong
        >与<strong class="font-semibold text-amber-100">阵营行动</strong>等页面，当前项目尚未包含。
      </p>
    </div>

    <div class="text-center mb-10">
      <h1 class="text-white text-2xl md:text-3xl font-semibold tracking-tight mb-2">角色行动</h1>
      <p class="text-gray-500 text-sm">选择你的两个行动并提交</p>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-2 gap-5 mb-10">
      <!-- 行动一 -->
      <div
        class="relative bg-gradient-to-br from-[#1a2332] to-[#0f1419] border border-white/10 rounded-3xl p-6 overflow-hidden"
      >
        <div class="absolute top-0 right-0 w-48 h-48 bg-blue-500/5 rounded-full blur-3xl" />
        <div class="relative space-y-5">
          <div class="flex items-center gap-3">
            <span
              class="inline-flex items-center justify-center w-9 h-9 rounded-lg bg-white/10 border border-white/10 text-gray-300 text-sm font-medium"
            >
              1
            </span>
            <h2 class="text-white text-lg tracking-tight">行动一</h2>
          </div>

          <div>
            <div class="flex items-center gap-2 mb-2 ml-0.5">
              <label class="text-gray-500 text-xs">选择行动</label>
              <button
                type="button"
                class="inline-flex h-5 w-5 items-center justify-center rounded-full border border-white/20 bg-white/5 text-gray-400 hover:text-white hover:border-blue-500/40 hover:bg-blue-500/10 transition-colors text-xs font-semibold"
                title="行动说明"
                aria-label="查看行动类型说明"
                @click="showActionHelpModal = true"
              >
                ?
              </button>
            </div>
            <div class="relative">
              <select
                v-model="action1Type"
                class="w-full appearance-none bg-white/5 border border-white/10 rounded-xl px-4 py-3 pr-10 text-gray-200 text-sm focus:outline-none focus:border-blue-500/50 focus:ring-1 focus:ring-blue-500/30"
              >
                <option v-for="opt in actionTypeOptions" :key="`a1-${opt.value}`" :value="opt.value">
                  {{ opt.label }}
                </option>
              </select>
              <svg
                class="pointer-events-none absolute right-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-500"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
              </svg>
            </div>
          </div>

          <div>
            <label class="block text-gray-500 text-xs mb-2 ml-0.5">选择目标</label>
            <div class="relative">
              <select
                v-model="action1Target"
                class="w-full appearance-none bg-white/5 border border-white/10 rounded-xl px-4 py-3 pr-10 text-gray-200 text-sm focus:outline-none focus:border-blue-500/50 focus:ring-1 focus:ring-blue-500/30"
              >
                <option value="">请选择目标</option>
                <option v-for="t in placeholderTargets" :key="`a1t-${t}`" :value="t">{{ t }}</option>
              </select>
              <svg
                class="pointer-events-none absolute right-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-500"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
              </svg>
            </div>
          </div>

          <div>
            <label class="block text-gray-500 text-xs mb-2 ml-0.5">备注说明</label>
            <textarea
              v-model="action1Notes"
              rows="4"
              placeholder="在此输入备注说明..."
              class="w-full resize-none bg-white/5 border border-white/10 rounded-xl px-4 py-3 text-gray-200 text-sm placeholder:text-gray-600 focus:outline-none focus:border-blue-500/50 focus:ring-1 focus:ring-blue-500/30"
            />
          </div>

          <div>
            <label class="block text-gray-500 text-xs mb-2 ml-0.5">行动结果</label>
            <div
              class="min-h-[5.5rem] rounded-xl border border-white/10 bg-black/25 px-4 py-3 text-sm text-gray-500"
            >
              {{ action1Result || '结果将在此显示' }}
            </div>
          </div>
        </div>
      </div>

      <!-- 行动二 -->
      <div
        class="relative bg-gradient-to-br from-[#1a2332] to-[#0f1419] border border-white/10 rounded-3xl p-6 overflow-hidden"
      >
        <div class="absolute top-0 left-0 w-48 h-48 bg-indigo-500/5 rounded-full blur-3xl" />
        <div class="relative space-y-5">
          <div class="flex items-center gap-3">
            <span
              class="inline-flex items-center justify-center w-9 h-9 rounded-lg bg-white/10 border border-white/10 text-gray-300 text-sm font-medium"
            >
              2
            </span>
            <h2 class="text-white text-lg tracking-tight">行动二</h2>
          </div>

          <div>
            <div class="flex items-center gap-2 mb-2 ml-0.5">
              <label class="text-gray-500 text-xs">选择行动</label>
              <button
                type="button"
                class="inline-flex h-5 w-5 items-center justify-center rounded-full border border-white/20 bg-white/5 text-gray-400 hover:text-white hover:border-blue-500/40 hover:bg-blue-500/10 transition-colors text-xs font-semibold"
                title="行动说明"
                aria-label="查看行动类型说明"
                @click="showActionHelpModal = true"
              >
                ?
              </button>
            </div>
            <div class="relative">
              <select
                v-model="action2Type"
                class="w-full appearance-none bg-white/5 border border-white/10 rounded-xl px-4 py-3 pr-10 text-gray-200 text-sm focus:outline-none focus:border-blue-500/50 focus:ring-1 focus:ring-blue-500/30"
              >
                <option v-for="opt in actionTypeOptions" :key="`a2-${opt.value}`" :value="opt.value">
                  {{ opt.label }}
                </option>
              </select>
              <svg
                class="pointer-events-none absolute right-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-500"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
              </svg>
            </div>
          </div>

          <div>
            <label class="block text-gray-500 text-xs mb-2 ml-0.5">选择目标</label>
            <div class="relative">
              <select
                v-model="action2Target"
                class="w-full appearance-none bg-white/5 border border-white/10 rounded-xl px-4 py-3 pr-10 text-gray-200 text-sm focus:outline-none focus:border-blue-500/50 focus:ring-1 focus:ring-blue-500/30"
              >
                <option value="">请选择目标</option>
                <option v-for="t in placeholderTargets" :key="`a2t-${t}`" :value="t">{{ t }}</option>
              </select>
              <svg
                class="pointer-events-none absolute right-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-500"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
              </svg>
            </div>
          </div>

          <div>
            <label class="block text-gray-500 text-xs mb-2 ml-0.5">备注说明</label>
            <textarea
              v-model="action2Notes"
              rows="4"
              placeholder="在此输入备注说明..."
              class="w-full resize-none bg-white/5 border border-white/10 rounded-xl px-4 py-3 text-gray-200 text-sm placeholder:text-gray-600 focus:outline-none focus:border-blue-500/50 focus:ring-1 focus:ring-blue-500/30"
            />
          </div>

          <div>
            <label class="block text-gray-500 text-xs mb-2 ml-0.5">行动结果</label>
            <div
              class="min-h-[5.5rem] rounded-xl border border-white/10 bg-black/25 px-4 py-3 text-sm text-gray-500"
            >
              {{ action2Result || '结果将在此显示' }}
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="flex justify-center pb-4">
      <button
        type="button"
        class="min-w-[200px] bg-gradient-to-r from-blue-500 to-blue-600 hover:from-blue-600 hover:to-blue-700 text-white px-8 py-3 rounded-xl text-sm font-medium shadow-lg shadow-blue-500/30 transition-all"
        @click="submitActions"
      >
        提交行动
      </button>
    </div>

    <Teleport to="body">
      <div
        v-if="showActionHelpModal"
        class="fixed inset-0 bg-black/75 flex items-center justify-center z-50 p-4"
        @click.self="showActionHelpModal = false"
      >
        <div
          class="bg-[#161b22] border border-[#2a3444] rounded-[18px] max-w-lg w-full max-h-[85vh] overflow-hidden flex flex-col shadow-[0_24px_48px_-12px_rgba(0,0,0,0.55)]"
        >
          <div
            class="flex items-start justify-between px-6 pt-6 pb-4 border-b border-[#252d3a] shrink-0"
          >
            <div>
              <h2 class="text-white text-lg font-semibold tracking-tight">行动类型说明</h2>
              <p class="text-[#a0aab7] text-xs mt-1.5">以下为各行动类型的详细规则</p>
            </div>
            <button
              type="button"
              class="text-[#a0aab7] hover:text-white transition-colors shrink-0 -mr-1 -mt-0.5 p-1"
              aria-label="关闭"
              @click="showActionHelpModal = false"
            >
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M6 18L18 6M6 6l12 12"
                />
              </svg>
            </button>
          </div>
          <div class="overflow-y-auto px-6 py-5 space-y-3">
            <div
              v-for="(entry, idx) in actionHelpEntries"
              :key="entry.title"
              class="rounded-[10px] border border-[#253041] bg-[#1c2533] px-4 py-3.5"
            >
              <p class="text-[#00d1ff] text-sm font-medium mb-2">
                {{ idx + 1 }}. {{ entry.title }}
              </p>
              <p class="text-[#a0aab7] text-sm leading-relaxed">{{ entry.body }}</p>
            </div>
          </div>
          <div class="px-6 py-5 border-t border-[#252d3a] shrink-0 flex justify-center">
            <button
              type="button"
              class="min-w-[140px] px-10 py-2.5 rounded-full bg-[#303e55] hover:bg-[#3a4d68] text-white text-sm font-medium transition-colors shadow-inner"
              @click="showActionHelpModal = false"
            >
              知道了
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
  </div>
</template>
