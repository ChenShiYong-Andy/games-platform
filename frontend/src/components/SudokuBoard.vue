<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  board: number[][]
  initialBoard: number[][]
  selectedCell: [number, number] | null
  gridSize?: number
}>()

const emit = defineEmits<{
  select: [row: number, col: number]
}>()

const size = computed(() => props.gridSize ?? props.board.length)

const boxRowSize = computed(() => {
  if (size.value === 4) return 2
  if (size.value === 6) return 2
  return 3
})

const boxColSize = computed(() => {
  if (size.value === 4) return 2
  if (size.value === 6) return 3
  return 3
})

const cellSize = computed(() => {
  if (size.value === 4) return 64
  if (size.value === 6) return 56
  return 48
})

const fontSize = computed(() => {
  if (size.value === 4) return '28px'
  if (size.value === 6) return '24px'
  return '22px'
})

function isSelected(row: number, col: number, selected: [number, number] | null) {
  return selected && selected[0] === row && selected[1] === col
}

function isBoxBorder(row: number, col: number, side: string) {
  if (side === 'right') {
    return (col + 1) % boxColSize.value === 0 && col !== size.value - 1
  }
  if (side === 'bottom') {
    return (row + 1) % boxRowSize.value === 0 && row !== size.value - 1
  }
  return false
}
</script>

<template>
  <div class="sudoku-board">
    <div v-for="(row, r) in board" :key="r" class="board-row">
      <div
        v-for="(cell, c) in row"
        :key="c"
        class="board-cell"
        :class="{
          fixed: initialBoard[r][c] !== 0,
          selected: isSelected(r, c, selectedCell),
          'border-right': isBoxBorder(r, c, 'right'),
          'border-bottom': isBoxBorder(r, c, 'bottom')
        }"
        :style="{ width: cellSize + 'px', height: cellSize + 'px', fontSize }"
        @click="emit('select', r, c)"
      >
        {{ cell !== 0 ? cell : '' }}
      </div>
    </div>
  </div>
</template>

<style scoped>
.sudoku-board {
  border: 3px solid #333;
  border-radius: 4px;
  display: inline-block;
  background: #fff;
}

.board-row {
  display: flex;
}

.board-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 500;
  border: 1px solid #ccc;
  cursor: pointer;
  user-select: none;
  transition: background 0.15s;
}

.board-cell.fixed {
  background: #f5f5f5;
  font-weight: 700;
  color: #333;
  cursor: default;
}

.board-cell.selected {
  background: #e6f0ff;
}

.board-cell:not(.fixed):hover {
  background: #f0f5ff;
}

.border-right {
  border-right: 3px solid #333;
}

.border-bottom {
  border-bottom: 3px solid #333;
}
</style>
