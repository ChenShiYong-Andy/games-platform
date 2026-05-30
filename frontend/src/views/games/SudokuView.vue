<script setup lang="ts">
import { useRouter } from 'vue-router'
import DifficultyPicker from '@/components/DifficultyPicker.vue'

const router = useRouter()

function startGame(difficulty: string) {
  router.push({ path: '/games/sudoku/play', query: { difficulty } })
}
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <button class="back-btn" @click="router.push('/')">← 返回游戏大厅</button>
      <div class="title-row">
        <span class="game-icon">🔢</span>
        <div>
          <h1>数独</h1>
          <p>选一个喜欢的难度，点击卡片即可开始</p>
        </div>
      </div>
    </div>
    <DifficultyPicker @select="startGame" />
  </div>
</template>

<style scoped>
.page-header {
  margin-bottom: 28px;
}

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

.title-row {
  display: flex;
  align-items: center;
  gap: 16px;
}

.game-icon {
  font-size: 48px;
  line-height: 1;
}

.page-header h1 {
  font-size: 28px;
  font-weight: 700;
  color: #333;
  margin-bottom: 4px;
}

.page-header p {
  color: #888;
  font-size: 15px;
}
</style>
