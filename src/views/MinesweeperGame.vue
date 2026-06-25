<script setup>
/**
 * 扫雷游戏组件
 * 功能：标准扫雷核心玩法，包含难度选择、计时器、地雷计数、标记功能
 * 交互：左键翻开格子，右键标记地雷
 */

import { ref, computed, onMounted, onUnmounted } from 'vue'

// ==================== 游戏配置 ====================
/** 难度配置：初级(9x9,10雷)、中级(16x16,40雷)、高级(30x16,99雷) */
const DIFFICULTY_CONFIG = {
  easy: { rows: 9, cols: 9, mines: 10, label: '初级' },
  medium: { rows: 16, cols: 16, mines: 40, label: '中级' },
  hard: { rows: 16, cols: 30, mines: 99, label: '高级' }
}

// ==================== 游戏状态 ====================
const difficulty = ref('easy')
const gameBoard = ref([])
const gameState = ref('idle') // idle | playing | won | lost
const timer = ref(0)
const minesRemaining = ref(0)
const revealedCount = ref(0)
const flaggedCount = ref(0)
const firstClick = ref(true)

let timerInterval = null

// ==================== 计算属性 ====================
const config = computed(() => DIFFICULTY_CONFIG[difficulty.value])
const totalCells = computed(() => config.value.rows * config.value.cols)
const isPlaying = computed(() => gameState.value === 'playing')
const isGameOver = computed(() => gameState.value === 'won' || gameState.value === 'lost')

// ==================== 核心游戏逻辑 ====================

/**
 * 初始化游戏棋盘
 * 创建空白棋盘，等待首次点击后生成雷区
 */
function initBoard() {
  stopTimer()
  timer.value = 0
  gameState.value = 'idle'
  firstClick.value = true
  minesRemaining.value = config.value.mines
  revealedCount.value = 0
  flaggedCount.value = 0

  const { rows, cols } = config.value
  gameBoard.value = Array.from({ length: rows }, (_, row) =>
    Array.from({ length: cols }, (_, col) => ({
      row,
      col,
      isMine: false,
      isRevealed: false,
      isFlagged: false,
      adjacentMines: 0,
      isExploded: false
    }))
  )
}

/**
 * 首次点击后生成雷区
 * 确保首次点击位置及周围无雷
 */
function generateMines(excludeRow, excludeCol) {
  const { rows, cols, mines } = config.value
  const excludeZone = getAdjacentCells(excludeRow, excludeCol)
  excludeZone.push(gameBoard.value[excludeRow][excludeCol])

  let placedMines = 0
  while (placedMines < mines) {
    const row = Math.floor(Math.random() * rows)
    const col = Math.floor(Math.random() * cols)
    const cell = gameBoard.value[row][col]

    // 排除首次点击区域
    if (!cell.isMine && !excludeZone.some(c => c.row === row && c.col === col)) {
      cell.isMine = true
      placedMines++
    }
  }

  // 计算每个格子周围的雷数
  calculateAdjacentMines()
}

/**
 * 计算每个非雷格子周围的雷数
 */
function calculateAdjacentMines() {
  const { rows, cols } = config.value
  for (let row = 0; row < rows; row++) {
    for (let col = 0; col < cols; col++) {
      const cell = gameBoard.value[row][col]
      if (!cell.isMine) {
        cell.adjacentMines = countAdjacentMines(row, col)
      }
    }
  }
}

/**
 * 计算指定格子周围的雷数
 */
function countAdjacentMines(row, col) {
  return getAdjacentCells(row, col).filter(c => c.isMine).length
}

/**
 * 获取指定格子周围的所有格子（8方向）
 */
function getAdjacentCells(row, col) {
  const cells = []
  const { rows, cols } = config.value
  for (let dr = -1; dr <= 1; dr++) {
    for (let dc = -1; dc <= 1; dc++) {
      if (dr === 0 && dc === 0) continue
      const r = row + dr
      const c = col + dc
      if (r >= 0 && r < rows && c >= 0 && c < cols) {
        cells.push(gameBoard.value[r][c])
      }
    }
  }
  return cells
}

/**
 * 翻开格子（左键点击）
 */
function revealCell(row, col) {
  if (isGameOver.value) return
  const cell = gameBoard.value[row][col]
  if (cell.isRevealed || cell.isFlagged) return

  // 首次点击时生成雷区并开始计时
  if (firstClick.value) {
    firstClick.value = false
    generateMines(row, col)
    gameState.value = 'playing'
    startTimer()
  }

  if (cell.isMine) {
    // 点击到雷，游戏失败
    cell.isExploded = true
    revealAllMines()
    gameState.value = 'lost'
    stopTimer()
    return
  }

  // 翻开格子，如果是空白格子则递归翻开周围
  revealRecursive(row, col)

  // 检查胜利条件
  checkWinCondition()
}

/**
 * 递归翻开格子
 * 如果格子周围无雷，自动翻开周围所有格子
 */
function revealRecursive(row, col) {
  const cell = gameBoard.value[row][col]
  if (cell.isRevealed || cell.isFlagged || cell.isMine) return

  cell.isRevealed = true
  revealedCount.value++

  if (cell.adjacentMines === 0) {
    getAdjacentCells(row, col).forEach(adj => {
      revealRecursive(adj.row, adj.col)
    })
  }
}

/**
 * 标记/取消标记地雷（右键点击）
 */
function toggleFlag(row, col) {
  if (isGameOver.value) return
  const cell = gameBoard.value[row][col]
  if (cell.isRevealed) return

  cell.isFlagged = !cell.isFlagged
  flaggedCount.value += cell.isFlagged ? 1 : -1
  minesRemaining.value = config.value.mines - flaggedCount.value
}

/**
 * 翻开所有雷（游戏失败时）
 */
function revealAllMines() {
  gameBoard.value.forEach(row => {
    row.forEach(cell => {
      if (cell.isMine) {
        cell.isRevealed = true
      }
    })
  })
}

/**
 * 检查胜利条件
 * 所有非雷格子都被翻开即胜利
 */
function checkWinCondition() {
  const nonMineCount = totalCells.value - config.value.mines
  if (revealedCount.value === nonMineCount) {
    gameState.value = 'won'
    stopTimer()
    // 自动标记所有未标记的雷
    gameBoard.value.forEach(row => {
      row.forEach(cell => {
        if (cell.isMine && !cell.isFlagged) {
          cell.isFlagged = true
        }
      })
    })
    flaggedCount.value = config.value.mines
    minesRemaining.value = 0
  }
}

// ==================== 计时器 ====================

function startTimer() {
  if (timerInterval) return
  timerInterval = setInterval(() => {
    timer.value++
  }, 1000)
}

function stopTimer() {
  if (timerInterval) {
    clearInterval(timerInterval)
    timerInterval = null
  }
}

// ==================== 辅助函数 ====================

/** 获取数字显示的颜色 */
function getNumberColor(num) {
  const colors = {
    1: 'text-blue-400',
    2: 'text-green-400',
    3: 'text-red-400',
    4: 'text-purple-400',
    5: 'text-yellow-400',
    6: 'text-cyan-400',
    7: 'text-gray-300',
    8: 'text-gray-400'
  }
  return colors[num] || 'text-white'
}

/** 格式化时间显示 */
function formatTime(seconds) {
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
}

// ==================== 生命周期 ====================

onMounted(() => {
  initBoard()
})

onUnmounted(() => {
  stopTimer()
})

// ==================== 难度切换 ====================

function changeDifficulty(newDifficulty) {
  difficulty.value = newDifficulty
  initBoard()
}

function restartGame() {
  initBoard()
}
</script>

<template>
  <div class="minesweeper-game">
    <!-- 游戏标题 -->
    <div class="text-center mb-6">
      <h1 class="text-white text-2xl font-semibold mb-2">扫雷游戏</h1>
      <p class="text-gray-500 text-sm">左键翻开格子，右键标记地雷</p>
    </div>

    <!-- 控制面板 -->
    <div class="flex flex-wrap items-center justify-center gap-4 mb-6">
      <!-- 难度选择 -->
      <div class="flex items-center gap-2">
        <span class="text-gray-400 text-sm">难度：</span>
        <select
          v-model="difficulty"
          class="bg-slate-800 border border-slate-600 rounded-lg px-3 py-2 text-white text-sm focus:outline-none focus:border-cyan-500"
          @change="initBoard"
        >
          <option value="easy">初级 (9×9, 10雷)</option>
          <option value="medium">中级 (16×16, 40雷)</option>
          <option value="hard">高级 (16×30, 99雷)</option>
        </select>
      </div>

      <!-- 计时器 -->
      <div class="flex items-center gap-2 bg-slate-800/50 rounded-lg px-4 py-2">
        <span class="text-gray-400">⏱️</span>
        <span class="text-cyan-400 font-mono text-lg">{{ formatTime(timer) }}</span>
      </div>

      <!-- 剩余地雷 -->
      <div class="flex items-center gap-2 bg-slate-800/50 rounded-lg px-4 py-2">
        <span class="text-gray-400">💣</span>
        <span class="text-red-400 font-mono text-lg">{{ minesRemaining }}</span>
      </div>

      <!-- 重新开始 -->
      <button
        type="button"
        class="bg-gradient-to-r from-cyan-600 to-cyan-500 hover:from-cyan-500 hover:to-cyan-400 text-white px-4 py-2 rounded-lg text-sm font-medium transition-all"
        @click="restartGame"
      >
        🔄 重新开始
      </button>
    </div>

    <!-- 游戏状态提示 -->
    <div class="text-center mb-4">
      <div v-if="gameState === 'won'" class="text-green-400 text-lg font-medium animate-pulse">
        🎉 恭喜你赢了！用时 {{ formatTime(timer) }}
      </div>
      <div v-if="gameState === 'lost'" class="text-red-400 text-lg font-medium animate-pulse">
        💥 游戏结束！点击重新开始
      </div>
      <div v-if="gameState === 'idle'" class="text-gray-400 text-sm">
        点击任意格子开始游戏
      </div>
    </div>

    <!-- 游戏棋盘 -->
    <div class="flex justify-center overflow-auto">
      <div
        class="inline-grid gap-1 p-4 bg-slate-900/80 rounded-xl border border-slate-700/50"
        :style="{ gridTemplateColumns: `repeat(${config.cols}, minmax(0, 1fr))` }"
      >
        <template v-for="row in gameBoard" :key="row[0]?.row">
          <div
            v-for="cell in row"
            :key="`${cell.row}-${cell.col}`"
            class="cell relative select-none"
            :class="[
              'w-7 h-7 md:w-8 md:h-8 flex items-center justify-center rounded cursor-pointer transition-all duration-100',
              cell.isRevealed
                ? (cell.isMine
                  ? (cell.isExploded ? 'bg-red-600' : 'bg-slate-700')
                  : 'bg-slate-800/80')
                : 'bg-gradient-to-br from-slate-600 to-slate-700 hover:from-slate-500 hover:to-slate-600',
              cell.isFlagged && !cell.isRevealed ? 'ring-2 ring-yellow-500/50' : ''
            ]"
            @click="revealCell(cell.row, cell.col)"
            @contextmenu.prevent="toggleFlag(cell.row, cell.col)"
          >
            <!-- 未翻开：显示旗帜或空白 -->
            <template v-if="!cell.isRevealed">
              <span v-if="cell.isFlagged" class="text-yellow-400 text-lg">🚩</span>
            </template>

            <!-- 已翻开：显示雷或数字 -->
            <template v-else>
              <span v-if="cell.isMine" class="text-xl">💣</span>
              <span
                v-else-if="cell.adjacentMines > 0"
                class="font-bold text-sm md:text-base"
                :class="getNumberColor(cell.adjacentMines)"
              >
                {{ cell.adjacentMines }}
              </span>
            </template>
          </div>
        </template>
      </div>
    </div>

    <!-- 游戏说明 -->
    <div class="mt-6 text-center text-gray-500 text-xs">
      <p>提示：数字表示周围8格内的地雷数量</p>
      <p class="mt-1">首次点击必定安全，请放心探索</p>
    </div>
  </div>
</template>

<style scoped>
.minesweeper-game {
  background: rgba(15, 20, 35, 0.9);
  border-radius: 1rem;
  padding: 1.5rem;
}

.cell {
  user-select: none;
  -webkit-user-select: none;
  touch-action: manipulation;
}

.cell:active {
  transform: scale(0.95);
}

/* 数字颜色优化 */
.cell span {
  line-height: 1;
}
</style>