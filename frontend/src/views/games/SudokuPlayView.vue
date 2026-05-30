<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { postData } from '@/api'
import { useAuthStore } from '@/stores/auth'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { GameResponse, SubmitGameResponse } from '@/types'
import SudokuBoard from '@/components/SudokuBoard.vue'

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
const history = ref<number[][][]>([])
const historyIndex = ref(-1)
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

const canUndo = computed(() => historyIndex.value > 0)
const canRedo = computed(() => historyIndex.value < history.value.length - 1)
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

function pushHistory(state: number[][]) {
  history.value = history.value.slice(0, historyIndex.value + 1)
  history.value.push(state.map(row => [...row]))
  historyIndex.value = history.value.length - 1
}

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
  pushHistory(newBoard)
  board.value = newBoard
}

function clearCell() {
  if (!selectedCell.value) return
  const [row, col] = selectedCell.value
  if (initialBoard.value[row][col] !== 0) return
  const newBoard = board.value.map(r => [...r])
  newBoard[row][col] = 0
  pushHistory(newBoard)
  board.value = newBoard
}

function undo() {
  if (!canUndo.value) return
  historyIndex.value--
  board.value = history.value[historyIndex.value].map(r => [...r])
}

function redo() {
  if (!canRedo.value) return
  historyIndex.value++
  board.value = history.value[historyIndex.value].map(r => [...r])
}

async function requestHint() {
  if (!game.value) return
  try {
    const hint = await postData<{ row: number; col: number; value: number }>(
      `/sudoku/games/${game.value.id}/hint`
    )
    hintsUsed.value++
    const newBoard = board.value.map(r => [...r])
    newBoard[hint.row][hint.col] = hint.value
    pushHistory(newBoard)
    board.value = newBoard
    selectedCell.value = [hint.row, hint.col]
    ElMessage.success(`提示：(${hint.row + 1}, ${hint.col + 1}) = ${hint.value}`)
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : '获取提示失败')
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
      await ElMessageBox.alert(
        `得分：${result.score} | 获得积分：${result.pointsEarned}\n当前等级：Lv.${result.newLevel} | 总积分：${result.totalPoints}`,
        '恭喜通关！',
        { confirmButtonText: '返回游戏大厅', type: 'success' }
      )
      router.push('/')
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

onMounted(async () => {
  const difficulty = (route.query.difficulty as string) || 'EASY'
  try {
    const data = await postData<GameResponse>('/sudoku/games', { difficulty })
    game.value = data
    board.value = data.puzzle.map(r => [...r])
    initialBoard.value = data.puzzle.map(r => [...r])
    pushHistory(board.value)
    timer = setInterval(() => elapsedSeconds.value++, 1000)
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : '创建游戏失败')
    router.push('/games/sudoku')
  } finally {
    loading.value = false
  }
})

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
          <span class="stat">提示: {{ hintsUsed }}</span>
          <span class="stat">错误: {{ mistakes }}</span>
        </div>
        <div class="game-actions">
          <el-button @click="undo" :disabled="!canUndo">撤销</el-button>
          <el-button @click="redo" :disabled="!canRedo">重做</el-button>
          <el-button type="warning" @click="requestHint">提示</el-button>
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
</style>
