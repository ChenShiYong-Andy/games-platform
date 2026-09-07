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
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 38px;
  border: 1px solid rgba(102, 126, 234, 0.3);
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.85);
  color: #667eea;
  font-size: 14px;
  cursor: pointer;
  padding: 8px 14px;
  margin-bottom: 16px;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.1);
  transition: color 0.2s ease, background 0.2s ease, border-color 0.2s ease,
    box-shadow 0.2s ease, transform 0.2s ease;
}

.back-btn:hover {
  color: #fff;
  border-color: #667eea;
  background: #667eea;
  box-shadow: 0 7px 16px rgba(102, 126, 234, 0.25);
  transform: translateY(-1px);
}

.back-btn:active {
  transform: translateY(0);
}

.back-btn:focus-visible {
  outline: 3px solid rgba(102, 126, 234, 0.22);
  outline-offset: 2px;
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
