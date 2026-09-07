<script setup lang="ts">
import { ref, computed, h, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { postData } from '@/api'
import { useAuthStore } from '@/stores/auth'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { GameResponse, SubmitGameResponse } from '@/types'
import SudokuBoard from '@/components/SudokuBoard.vue'
import gameWinGif from '@/assets/game_win.gif'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const game = ref<GameResponse | null>(null)
const board = ref<number[][]>([])
const initialBoard = ref<number[][]>([])
const loading = ref(true)
const selectedCell = ref<[number, number] | null>(null)
const hintsUsed = ref(0)
const mistakes = ref(0)
const elapsedSeconds = ref(0)
let timer: ReturnType<typeof setInterval> | null = null

const difficultyLabel = computed(() => {
  const map: Record<string, string> = { EASY: '简单', MEDIUM: '中等', HARD: '困难' }
  return map[game.value?.difficulty || ''] || game.value?.difficulty
})

const formattedTime = computed(() => {
  const m = Math.floor(elapsedSeconds.value / 60)
  const s = elapsedSeconds.value % 60
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
})

const hasFilledCells = computed(() => board.value.some((row, r) =>
  row.some((cell, c) => initialBoard.value[r]?.[c] === 0 && cell !== 0)
))
const maxNumber = computed(() => game.value?.gridSize ?? 9)
const numberPadCols = computed(() => {
  if (maxNumber.value <= 4) return 2
  if (maxNumber.value <= 6) return 3
  return 5
})
const numberPadWidth = computed(() => {
  if (maxNumber.value <= 4) return '160px'
  if (maxNumber.value <= 6) return '240px'
  return '360px'
})
const clearBtnSpan = computed(() => {
  if (maxNumber.value <= 4) return 2
  if (maxNumber.value <= 6) return 3
  return 2
})

function selectCell(row: number, col: number) {
  if (initialBoard.value[row]?.[col] !== 0) return
  selectedCell.value = [row, col]
}

function inputNumber(num: number) {
  if (!selectedCell.value || num > maxNumber.value) return
  const [row, col] = selectedCell.value
  if (initialBoard.value[row][col] !== 0) return

  const newBoard = board.value.map(r => [...r])
  newBoard[row][col] = num
  board.value = newBoard
}

function clearCell() {
  if (!selectedCell.value) return
  const [row, col] = selectedCell.value
  if (initialBoard.value[row][col] !== 0) return
  const newBoard = board.value.map(r => [...r])
  newBoard[row][col] = 0
  board.value = newBoard
}

async function redo() {
  if (!hasFilledCells.value) return
  try {
    await ElMessageBox.confirm(
      '重做会清空所有已填写的数字，此操作无法撤销，确定继续吗？',
      '确认重做',
      {
        confirmButtonText: '确定清空',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    board.value = initialBoard.value.map(row => [...row])
    selectedCell.value = null
  } catch {
    // 用户取消重做时保持当前棋盘不变。
  }
}

async function submitGame() {
  if (!game.value) return
  try {
    const result = await postData<SubmitGameResponse>(`/sudoku/games/${game.value.id}/submit`, {
      board: board.value,
      elapsedSeconds: elapsedSeconds.value,
      hintsUsed: hintsUsed.value,
      mistakes: mistakes.value
    })
    if (result.success) {
      stopTimer()
      await authStore.refreshProfile()
      let playAgain = false
      try {
        await ElMessageBox.confirm(
          h('div', { style: { width: '100%' } }, [
            h('img', {
              src: gameWinGif,
              alt: '恭喜通关动画',
              style: {
                display: 'block',
                width: '160px',
                maxWidth: '100%',
                height: 'auto',
                margin: '0 auto 14px',
                objectFit: 'contain'
              }
            }),
            h('div', `得分：${result.score} | 获得积分：${result.pointsEarned} | 当前等级：Lv.${result.newLevel} | 总积分：${result.totalPoints}`)
          ]),
          '恭喜通关！',
          {
            confirmButtonText: '再来一次',
            cancelButtonText: '返回游戏大厅',
            customClass: 'sudoku-success-dialog',
            showClose: false,
            closeOnClickModal: false,
            closeOnPressEscape: false
          }
        )
        playAgain = true
      } catch {
        // 点击“返回游戏大厅”时 MessageBox 以 cancel 结束。
      }
      if (playAgain) await startGame()
      else router.push('/')
    } else {
      mistakes.value++
      ElMessage.error(result.message)
    }
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : '提交失败')
  }
}

function stopTimer() {
  if (timer) {
    clearInterval(timer)
    timer = null
  }
}

async function startGame() {
  stopTimer()
  loading.value = true
  game.value = null
  board.value = []
  initialBoard.value = []
  selectedCell.value = null
  hintsUsed.value = 0
  mistakes.value = 0
  elapsedSeconds.value = 0

  const difficulty = (route.query.difficulty as string) || 'EASY'
  try {
    const data = await postData<GameResponse>('/sudoku/games', { difficulty })
    game.value = data
    board.value = data.puzzle.map(r => [...r])
    initialBoard.value = data.puzzle.map(r => [...r])
    timer = setInterval(() => elapsedSeconds.value++, 1000)
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : '创建游戏失败')
    router.push('/games/sudoku')
  } finally {
    loading.value = false
  }
}

onMounted(startGame)

onUnmounted(stopTimer)
</script>

<template>
  <div class="page-container game-page" v-loading="loading">
    <button class="back-btn" @click="router.push('/games/sudoku')">← 返回难度选择</button>
    <template v-if="game">
      <div class="game-header">
        <div class="game-info">
          <el-tag>{{ difficultyLabel }}</el-tag>
          <span class="timer">⏱ {{ formattedTime }}</span>
          <span class="stat">错误: {{ mistakes }}</span>
        </div>
        <div class="game-actions">
          <el-button @click="redo" :disabled="!hasFilledCells">重做</el-button>
          <el-button type="primary" @click="submitGame">提交</el-button>
        </div>
      </div>

      <div class="game-body">
        <SudokuBoard
          :board="board"
          :initial-board="initialBoard"
          :selected-cell="selectedCell"
          :grid-size="game.gridSize"
          @select="selectCell"
        />
        <div
          class="number-pad"
          :style="{
            gridTemplateColumns: `repeat(${numberPadCols}, 1fr)`,
            maxWidth: numberPadWidth
          }"
        >
          <button v-for="n in maxNumber" :key="n" class="num-btn" @click="inputNumber(n)">{{ n }}</button>
          <button class="num-btn clear-btn" :style="{ gridColumn: `span ${clearBtnSpan}` }" @click="clearCell">清除</button>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.game-page { max-width: 700px; }

.back-btn {
  border: none;
  background: none;
  color: #667eea;
  font-size: 14px;
  cursor: pointer;
  padding: 0;
  margin-bottom: 16px;
  font-weight: 500;
}

.back-btn:hover {
  text-decoration: underline;
}

.game-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 12px;
}

.game-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.timer {
  font-size: 20px;
  font-weight: 600;
  font-family: monospace;
}

.stat { color: #666; font-size: 14px; }

.game-actions { display: flex; gap: 8px; }

.game-body {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24px;
}

.number-pad {
  display: grid;
  gap: 8px;
  width: 100%;
}

.num-btn {
  height: 48px;
  border: 1px solid #ddd;
  border-radius: 8px;
  background: #fff;
  font-size: 18px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.15s;
}

.num-btn:hover {
  background: #667eea;
  color: #fff;
  border-color: #667eea;
}

.clear-btn {
  color: #f5222d;
}

:global(.sudoku-success-dialog .el-message-box__btns) {
  justify-content: center;
}

:global(.sudoku-success-dialog .el-message-box__container) {
  justify-content: center;
}

:global(.sudoku-success-dialog .el-message-box__message) {
  flex: 1;
  width: 100%;
  text-align: center;
}
</style>
