<script setup lang="ts">
import { onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import GameAppGrid from '@/components/GameAppGrid.vue'

const authStore = useAuthStore()

onMounted(() => {
  void authStore.refreshProfile().catch(() => undefined)
})
</script>

<template>
  <div class="page-container">
    <div class="welcome-card">
      <div class="welcome-text">
        <p class="greeting">你好，{{ authStore.user?.nickname }} 👋</p>
        <h1>游戏大厅</h1>
        <p class="subtitle">选择一个游戏开始玩吧</p>
      </div>
      <div class="stats">
        <div class="stat-item">
          <span class="stat-value">Lv.{{ authStore.user?.level }}</span>
          <span class="stat-label">等级</span>
        </div>
        <div class="stat-item">
          <span class="stat-value">{{ authStore.user?.totalPoints }}</span>
          <span class="stat-label">总积分</span>
        </div>
        <div class="stat-item">
          <span class="stat-value">{{ authStore.user?.loginStreak }}</span>
          <span class="stat-label">连续登录(天)</span>
        </div>
      </div>
    </div>

    <h2 class="section-title">全部游戏</h2>
    <GameAppGrid />
  </div>
</template>

<style scoped>
.welcome-card {
  background: #fff;
  border-radius: 20px;
  padding: 28px 32px;
  margin-bottom: 36px;
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 24px;
}

.greeting {
  font-size: 14px;
  color: #888;
  margin-bottom: 6px;
}

.welcome-text h1 {
  font-size: 26px;
  font-weight: 700;
  color: #333;
}

.subtitle {
  font-size: 14px;
  color: #999;
  margin-top: 6px;
}

.stats {
  display: flex;
  gap: 28px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 64px;
}

.stat-value {
  font-size: 24px;
  font-weight: 800;
  background: linear-gradient(135deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.stat-label {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #555;
  margin-bottom: 20px;
}

@media (max-width: 768px) {
  .welcome-card {
    flex-direction: column;
    align-items: flex-start;
  }

  .stats {
    width: 100%;
    justify-content: space-between;
    gap: 12px;
  }
}
</style>
