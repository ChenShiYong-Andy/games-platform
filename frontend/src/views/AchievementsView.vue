<script setup lang="ts">
import { computed, ref, onMounted } from 'vue'
import { getData } from '@/api'
import type { Achievement } from '@/types'

const achievements = ref<Achievement[]>([])
const loading = ref(true)
const gameNames: Record<string, string> = {
  PLATFORM: '平台成就',
  SUDOKU: '数独成就'
}
const achievementGroups = computed(() => {
  return Object.entries(gameNames)
    .map(([gameCode, name]) => ({
      gameCode,
      name,
      achievements: achievements.value.filter(a => a.gameCode === gameCode)
    }))
    .filter(group => group.achievements.length > 0)
})

onMounted(async () => {
  try {
    achievements.value = await getData<Achievement[]>('/achievements')
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="page-container">
    <h1>成就系统</h1>
    <div v-loading="loading">
      <section v-for="group in achievementGroups" :key="group.gameCode" class="achievement-section">
        <h2>{{ group.name }}</h2>
        <div class="achievement-grid">
          <div
            v-for="a in group.achievements"
            :key="a.id"
            class="card achievement-card"
            :class="{ unlocked: a.unlocked }"
          >
            <div class="icon">{{ a.icon }}</div>
            <h3>{{ a.name }}</h3>
            <p>{{ a.description }}</p>
            <div v-if="a.unlocked" class="unlocked-badge">已解锁 {{ a.unlockedAt }}</div>
            <div v-else class="locked-badge">未解锁</div>
          </div>
        </div>
      </section>
      <el-empty v-if="!loading && achievementGroups.length === 0" description="暂无成就" />
    </div>
  </div>
</template>

<style scoped>
h1 { margin-bottom: 24px; }
.achievement-section { margin-bottom: 28px; }
.achievement-section h2 { margin-bottom: 12px; font-size: 18px; }
.achievement-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
}
.achievement-card {
  text-align: center;
  opacity: 0.5;
  transition: all 0.2s;
}
.achievement-card.unlocked {
  opacity: 1;
  border: 2px solid #667eea;
}
.icon { font-size: 40px; margin-bottom: 8px; }
h3 { font-size: 16px; margin-bottom: 8px; }
p { color: #666; font-size: 13px; margin-bottom: 12px; }
.unlocked-badge { color: #52c41a; font-size: 12px; }
.locked-badge { color: #999; font-size: 12px; }
</style>
