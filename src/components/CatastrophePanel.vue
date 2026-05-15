<template>
  <div class="space-y-6">
    <div class="bg-gray-800/50 backdrop-blur rounded-2xl p-6 border border-gray-700">
      <div class="flex items-center justify-between mb-4">
        <h2 class="text-xl font-semibold text-white flex items-center gap-2">
          <span class="text-2xl">⚡</span>
          命运之轮：天灾
        </h2>
        <span class="text-3xl font-bold" :class="progressColor">{{ progress }}%</span>
      </div>
      
      <div class="relative h-8 bg-gray-700 rounded-full overflow-hidden mb-4">
        <div 
          class="absolute inset-y-0 left-0 rounded-full transition-all duration-500 ease-out"
          :class="progressBarClass"
          :style="{ width: progress + '%' }"
        ></div>
        <div 
          v-for="marker in [33, 67]" 
          :key="marker"
          class="absolute top-0 bottom-0 w-px bg-gray-500"
          :style="{ left: marker + '%' }"
        ></div>
      </div>

      <div v-if="isDm" class="space-y-4">
        <div class="flex gap-2">
          <button
            v-for="preset in [0, 33, 67, 100]"
            :key="preset"
            @click="updateProgressValue(preset)"
            class="flex-1 px-4 py-2 rounded-lg font-medium transition-all"
            :class="progressInput === preset ? 'bg-purple-600 text-white' : 'bg-gray-700 text-gray-300 hover:bg-gray-600'"
          >
            {{ preset }}%
          </button>
        </div>
        <div class="flex gap-2">
          <input
            v-model.number="progressInput"
            type="range"
            min="0"
            max="100"
            class="flex-1 h-2 bg-gray-700 rounded-lg appearance-none cursor-pointer accent-purple-500"
          />
          <input
            v-model.number="progressInput"
            type="number"
            min="0"
            max="100"
            class="w-20 px-3 py-2 bg-gray-700 border border-gray-600 rounded-lg text-white text-center focus:outline-none focus:border-purple-500"
          />
        </div>
        <button
          @click="submitProgress"
          class="w-full py-3 bg-purple-600 hover:bg-purple-700 text-white font-medium rounded-lg transition-colors"
        >
          更新天灾进度
        </button>
      </div>

      <div v-if="progress >= 100" class="mt-4 p-4 bg-red-500/20 border border-red-500/50 rounded-lg">
        <div class="flex items-center gap-2 text-red-400">
          <span class="text-xl">🔥</span>
          <span class="font-semibold">天灾已触发！游戏进入结算阶段</span>
        </div>
      </div>
    </div>

    <div class="bg-gray-800/50 backdrop-blur rounded-2xl p-6 border border-gray-700">
      <div class="flex items-center justify-between mb-4">
        <h2 class="text-xl font-semibold text-white flex items-center gap-2">
          <span class="text-2xl">🃏</span>
          天灾牌选择
        </h2>
        <span class="text-sm text-gray-400">选择一张天灾牌触发</span>
      </div>

      <div v-if="selectableCards.length === 0" class="text-center py-8 text-gray-500">
        <div class="text-4xl mb-4">📭</div>
        <p>暂无可用的天灾牌</p>
        <p class="text-sm">等待DM发送新的牌组</p>
      </div>

      <div v-else class="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div
          v-for="(card, index) in selectableCards"
          :key="card.selectedId"
          @click="selectCard(card)"
          class="relative p-4 rounded-xl border-2 transition-all"
          :class="[
            card.isSelected
              ? 'border-green-500 bg-green-500/20 shadow-lg shadow-green-500/30'
              : (selectedCardId === card.selectedId 
                ? 'border-purple-500 bg-purple-500/20 shadow-lg shadow-purple-500/30' 
                : 'border-gray-600 bg-gray-700/50'),
            !card.isSelected && !hasConfirmedSelection ? 'cursor-pointer transform hover:scale-105 hover:border-gray-500' : 'cursor-default'
          ]"
        >
          <div class="absolute -top-3 -right-3 w-8 h-8 bg-gray-800 rounded-full flex items-center justify-center text-sm font-bold text-gray-400 border border-gray-600">
            {{ index + 1 }}
          </div>
          <h3 class="text-lg font-bold text-white mb-2">{{ card.name }}</h3>
          <p class="text-gray-400 text-sm leading-relaxed">{{ card.description }}</p>
          <div v-if="card.isSelected" class="mt-3 flex items-center gap-2 text-green-400 text-sm">
            <span>✓</span>
            已选中
          </div>
        </div>
      </div>

      <div v-if="selectableCards.length > 0 && !isDm" class="mt-4 text-center">
        <button
          @click="confirmSelection"
          :disabled="selectedCardId === null || hasConfirmedSelection"
          class="px-8 py-3 text-white font-medium rounded-lg transition-colors"
          :class="hasConfirmedSelection 
            ? 'bg-gray-600 cursor-not-allowed' 
            : (selectedCardId === null 
              ? 'bg-gray-600 cursor-not-allowed' 
              : 'bg-purple-600 hover:bg-purple-700')"
        >
          {{ hasConfirmedSelection ? '今日天灾已确认' : '确认选择' }}
        </button>
        <p v-if="hasConfirmedSelection" class="text-gray-500 text-xs mt-2">等待DM发送新的天灾牌后可再次选择</p>
      </div>
    </div>

    <div v-if="isDm" class="space-y-6">
      <div class="bg-gray-800/50 backdrop-blur rounded-2xl p-6 border border-gray-700">
        <div class="flex items-center justify-between mb-4">
          <h2 class="text-xl font-semibold text-white flex items-center gap-2">
            <span class="text-2xl">🎴</span>
            DM抽取表
          </h2>
        </div>

        <div class="space-y-4">
          <button
            @click="drawCards"
            :disabled="isDrawing"
            class="w-full py-3 bg-red-600 hover:bg-red-700 disabled:bg-gray-600 disabled:cursor-not-allowed text-white font-medium rounded-lg transition-colors flex items-center justify-center gap-2"
          >
            <span v-if="isDrawing">⏳</span>
            <span>抽取3张天灾牌</span>
          </button>

          <div v-if="drawnCards.length > 0" class="space-y-3">
            <div class="flex justify-between items-center">
              <span class="text-gray-400 text-sm">抽取轮次: {{ currentDrawRound }}</span>
              <button
                @click="drawCards"
                class="text-sm text-purple-400 hover:text-purple-300"
              >
                重新抽取
              </button>
            </div>

            <div
              v-for="(card, index) in drawnCards"
              :key="card.deckId"
              class="p-3 bg-gray-700/50 rounded-lg border border-gray-600"
            >
              <div class="flex items-start justify-between mb-1">
                <span class="text-xs text-gray-500">#{{ index + 1 }}</span>
                <span class="text-xs text-gray-500">ID: {{ card.cardNumber }}</span>
              </div>
              <h4 class="text-white font-medium text-sm">{{ card.name }}</h4>
              <p class="text-gray-400 text-xs mt-1 line-clamp-2">{{ card.description }}</p>
            </div>

            <button
              @click="confirmCards"
              class="w-full py-3 bg-green-600 hover:bg-green-700 text-white font-medium rounded-lg transition-colors"
            >
              确认并发送牌组
            </button>
          </div>

          <div v-else class="text-center py-6 text-gray-500">
            <div class="text-3xl mb-2">🃏</div>
            <p>点击抽取卡牌</p>
          </div>
        </div>

        <div class="mt-4 pt-4 border-t border-gray-600">
          <button
            @click="resetCatastrophe"
            :disabled="isResetting"
            class="w-full py-3 bg-yellow-600 hover:bg-yellow-700 disabled:bg-gray-600 disabled:cursor-not-allowed text-white font-medium rounded-lg transition-colors flex items-center justify-center gap-2"
          >
            <span v-if="isResetting">⏳</span>
            <span>🔄 天灾卡复原</span>
          </button>
          <p class="text-gray-500 text-xs mt-2 text-center">复原将清除所有天灾牌操作记录，恢复到初始状态</p>
        </div>
      </div>

      <div class="bg-gray-800/50 backdrop-blur rounded-2xl p-6 border border-gray-700">
        <h3 class="text-lg font-semibold text-white mb-4">游戏状态</h3>
        <div class="space-y-3 text-sm">
          <div class="flex justify-between">
            <span class="text-gray-400">当前天数</span>
            <span class="text-white font-medium">{{ gameState.currentDay }}</span>
          </div>
          <div class="flex justify-between">
            <span class="text-gray-400">当前阶段</span>
            <span class="text-white font-medium">{{ gameState.currentPhase === 'DAY' ? '白天' : '夜晚' }}</span>
          </div>
          <div class="flex justify-between">
            <span class="text-gray-400">天灾触发</span>
            <span :class="gameState.catastropheTriggered ? 'text-red-400' : 'text-gray-400'">
              {{ gameState.catastropheTriggered ? '是' : '否' }}
            </span>
          </div>
          <div class="flex justify-between">
            <span class="text-gray-400">额外卡牌待触发</span>
            <span :class="gameState.extraCardDue ? 'text-yellow-400' : 'text-gray-400'">
              {{ gameState.extraCardDue ? '是' : '否' }}
            </span>
          </div>
        </div>
        <button
          @click="advanceDay"
          class="w-full mt-4 py-2 bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-lg transition-colors text-sm"
        >
          推进一天 (+{{ gameState.currentDay < 3 ? 33 : 34 }}进度)
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { catastropheAPI } from '../utils/api.js';

const props = defineProps({
  isDm: {
    type: Boolean,
    default: false
  }
});

const userRole = props.isDm ? 'dm' : (localStorage.getItem('userRole') || '').toLowerCase();
const playerId = localStorage.getItem('playerId') || null;

const progress = ref(0);
const progressInput = ref(0);
const selectableCards = ref([]);
const selectedCardId = ref(null);
const drawnCards = ref([]);
const currentDrawRound = ref(0);
const isDrawing = ref(false);
const isConfirming = ref(false);
const isResetting = ref(false);
const gameState = ref({
  currentDay: 1,
  currentPhase: 'DAY',
  isGameOver: false,
  catastropheTriggered: false,
  extraCardDue: false
});

const progressColor = computed(() => {
  if (progress.value >= 100) return 'text-red-500';
  if (progress.value >= 67) return 'text-orange-400';
  if (progress.value >= 33) return 'text-yellow-400';
  return 'text-green-400';
});

const progressBarClass = computed(() => {
  if (progress.value >= 100) return 'bg-gradient-to-r from-red-600 to-red-400';
  if (progress.value >= 67) return 'bg-gradient-to-r from-orange-600 to-orange-400';
  if (progress.value >= 33) return 'bg-gradient-to-r from-yellow-600 to-yellow-400';
  return 'bg-gradient-to-r from-green-600 to-green-400';
});

const hasConfirmedSelection = computed(() => {
  return isConfirming.value || selectableCards.value.some(card => card.isSelected);
});

const fetchProgress = async () => {
  try {
    const response = await catastropheAPI.getProgress();
    progress.value = response.progress;
    progressInput.value = response.progress;
  } catch (error) {
    console.error('获取进度失败:', error);
  }
};

const fetchSelectableCards = async () => {
  try {
    const response = await catastropheAPI.getSelectableCards();
    selectableCards.value = response.cards;
    const confirmedCard = response.cards.find(c => c.isSelected);
    selectedCardId.value = confirmedCard?.selectedId || null;
    if (!confirmedCard) {
      isConfirming.value = false;
    }
  } catch (error) {
    console.error('获取可选择卡牌失败:', error);
  }
};

const fetchGameState = async () => {
  try {
    const response = await catastropheAPI.getGameState();
    gameState.value = response;
  } catch (error) {
    console.error('获取游戏状态失败:', error);
  }
};

const updateProgressValue = (value) => {
  progressInput.value = value;
};

const submitProgress = async () => {
  try {
    const response = await catastropheAPI.updateProgress(progressInput.value, userRole);
    if (response.success) {
      progress.value = response.progress;
      alert(response.message);
      if (response.catastropheTriggered) {
        fetchGameState();
      }
    } else {
      alert(response.message);
    }
  } catch (error) {
    console.error('更新进度失败:', error);
  }
};

const drawCards = async () => {
  isDrawing.value = true;
  try {
    const response = await catastropheAPI.drawCards(userRole);
    if (response.success) {
      drawnCards.value = response.cards;
      currentDrawRound.value = response.drawRound;
    } else {
      alert(response.message);
    }
  } catch (error) {
    console.error('抽取卡牌失败:', error);
  } finally {
    isDrawing.value = false;
  }
};

const confirmCards = async () => {
  try {
    const response = await catastropheAPI.confirmCards(userRole);
    if (response.success) {
      alert(response.message);
      await fetchSelectableCards();
    } else {
      alert(response.message);
    }
  } catch (error) {
    console.error('确认牌组失败:', error);
  }
};

const selectCard = (card) => {
  if (!props.isDm && !hasConfirmedSelection.value) {
    selectedCardId.value = card.selectedId;
  }
};

const confirmSelection = async () => {
  if (selectedCardId.value === null || hasConfirmedSelection.value) return;
  
  isConfirming.value = true;
  
  try {
    const response = await catastropheAPI.selectCard(selectedCardId.value, playerId, userRole);
    if (response.success) {
      await fetchSelectableCards();
    } else {
      isConfirming.value = false;
      alert(response.message);
    }
  } catch (error) {
    isConfirming.value = false;
    console.error('选择卡牌失败:', error);
  }
};

const advanceDay = async () => {
  try {
    const response = await catastropheAPI.advanceDay();
    if (response.success) {
      progress.value = response.progress;
      progressInput.value = response.progress;
      await fetchGameState();
      alert(`已推进到第${response.currentDay}天，天灾进度+${response.advanceAmount}`);
    } else {
      alert(response.message);
    }
  } catch (error) {
    console.error('推进天数失败:', error);
  }
};

const resetCatastrophe = async () => {
  if (!confirm('确定要复原天灾牌吗？这将清除所有天灾牌操作记录，恢复到初始状态。')) return;

  isResetting.value = true;
  try {
    const response = await catastropheAPI.resetCatastrophe(userRole);
    if (response.success) {
      drawnCards.value = [];
      currentDrawRound.value = 0;
      selectableCards.value = [];
      selectedCardId.value = null;
      isConfirming.value = false;
      await fetchProgress();
      await fetchGameState();
      alert(response.message);
    } else {
      alert(response.message);
    }
  } catch (error) {
    console.error('天灾卡复原失败:', error);
  } finally {
    isResetting.value = false;
  }
};

onMounted(() => {
  fetchProgress();
  fetchSelectableCards();
  fetchGameState();

  if (props.isDm) {
    catastropheAPI.getDrawnCards().then(response => {
      if (response.success && response.cards.length > 0) {
        drawnCards.value = response.cards;
        currentDrawRound.value = response.drawRound;
      }
    });
  }
});
</script>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>