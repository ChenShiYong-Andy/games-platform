<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getData } from '@/api'
import type { Achievement } from '@/types'

const achievements = ref<Achievement[]>([])
const loading = ref(true)

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
    <div class="achievement-grid" v-loading="loading">
      <div
        v-for="a in achievements"
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
  </div>
</template>

<style scoped>
h1 { margin-bottom: 24px; }
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
