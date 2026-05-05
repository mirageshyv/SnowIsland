<script setup>
import { computed, ref } from 'vue'

// 方舟当前建造进度（可在接入后端后替换为接口值）
const arkCurrentProgress = ref(70)

const arkBuildStages = [
  { progress: 30, title: '方舟刚开始', description: '底部龙骨与基础结构已完成，船体轮廓初步形成。' },
  { progress: 50, title: '基本出航状态', description: '船舷与主舱完成，具备勉强出航能力。' },
  { progress: 70, title: '扩建完成', description: '加装护舷和附属空间，稳定性显著提升。' },
  { progress: 90, title: '几乎完工', description: '甲板与舱室进一步完善，物资承载能力增强。' },
  { progress: 100, title: '方舟成了', description: '方舟全面完工，可承载人员和长期补给。' },
]

const arkActivityLogs = [
  { player: '张三', action: '铺设了方舟底部龙骨', day: 1, increase: 5 },
  { player: '李四', action: '安装了船舷木板', day: 2, increase: 8 },
  { player: '王五', action: '搭建了主舱框架', day: 3, increase: 7 },
  { player: '赵六', action: '制作并安装了桅杆', day: 5, increase: 6 },
  { player: '孙七', action: '铺设了甲板', day: 6, increase: 9 },
]

const visibleArkStages = computed(() =>
  arkBuildStages.filter((stage) => stage.progress <= arkCurrentProgress.value),
)
</script>

<template>
  <div class="max-w-4xl mx-auto">
    <div class="text-center mb-10 pt-2">
      <h1 class="text-white text-3xl md:text-4xl font-semibold tracking-wide mb-3">方舟建造进度</h1>
      <p class="text-gray-500 text-base md:text-lg">追踪方舟从无到有的每一步</p>
    </div>

    <div
      class="relative bg-gradient-to-br from-[#1a2332] to-[#0f1419] border border-white/10 rounded-3xl p-8 mb-10 overflow-hidden shadow-2xl shadow-black/20"
    >
      <div class="absolute top-0 right-0 w-64 h-64 bg-cyan-500/5 rounded-full blur-3xl" />
      <div class="relative">
        <div class="flex items-center justify-between mb-4">
          <span class="text-gray-300 text-lg">整体进度</span>
          <span class="text-3xl text-cyan-400 font-semibold tabular-nums">{{ arkCurrentProgress }}%</span>
        </div>
        <div class="relative w-full h-4 rounded-full bg-black/30 border border-white/10 overflow-hidden">
          <div
            class="h-full rounded-full bg-gradient-to-r from-cyan-600 to-cyan-400 transition-all duration-1000 ease-out"
            :style="{ width: `${arkCurrentProgress}%` }"
          />
        </div>
      </div>
    </div>

    <div class="space-y-2 mb-12 pl-1">
      <div
        v-for="stage in visibleArkStages"
        :key="stage.title"
        :class="[
          'relative border-l-4 pl-6 pb-6 transition-all duration-300',
          stage.progress === arkCurrentProgress ? 'border-cyan-500' : 'border-white/20',
        ]"
      >
        <div class="bg-white/5 border border-white/10 rounded-2xl p-5">
          <div class="flex items-center justify-between gap-3 mb-3">
            <h3 class="text-lg text-white font-medium tracking-tight">{{ stage.title }}</h3>
            <span class="shrink-0 px-3 py-1 rounded-full text-sm font-semibold tabular-nums bg-cyan-500/20 text-cyan-400">
              {{ stage.progress }}%
            </span>
          </div>
          <p class="text-gray-300 text-sm leading-7">{{ stage.description }}</p>
        </div>
      </div>
    </div>

    <div>
      <h2 class="text-2xl text-white mb-4 tracking-tight">建造日志</h2>
      <div class="relative bg-gradient-to-br from-[#1a2332] to-[#0f1419] border border-white/10 rounded-3xl p-4">
        <div class="relative space-y-2">
          <div
            v-for="(log, idx) in arkActivityLogs"
            :key="`${log.day}-${log.player}-${idx}`"
            class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-2 px-4 py-2.5 rounded-xl bg-black/20 border border-white/10"
          >
            <div class="flex flex-wrap items-center gap-x-3 gap-y-1 flex-1 min-w-0">
              <span class="text-cyan-400 font-medium text-sm shrink-0">{{ log.player }}</span>
              <span class="text-gray-500 text-xs shrink-0">第 {{ log.day }} 天</span>
              <p class="text-gray-300 text-sm">{{ log.action }}</p>
            </div>
            <div class="text-left sm:text-right shrink-0">
              <span class="text-emerald-400 font-semibold text-sm tabular-nums">+{{ log.increase }}%</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
