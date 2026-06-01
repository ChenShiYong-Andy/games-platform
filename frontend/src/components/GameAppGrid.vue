<script setup lang="ts">
import { useRouter } from 'vue-router'
import { gameApps, type GameApp } from '@/config/games'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

function getGameStat(game: GameApp) {
  if (game.id === 'sudoku') {
    return { label: '通关次数', value: authStore.user?.totalClears ?? 0 }
  }
  if (game.id === 'zoo-keeper') {
    return { label: '照顾次数', value: authStore.user?.totalZooCares ?? 0 }
  }
  return null
}

function openGame(game: GameApp) {
  if (!game.enabled || !game.route) {
    ElMessage.info('该游戏即将上线，敬请期待')
    return
  }
  router.push(game.route)
}
</script>

<template>
  <div class="game-grid">
    <button
      v-for="game in gameApps"
      :key="game.id"
      type="button"
      class="game-app"
      :class="{ disabled: !game.enabled }"
      :style="game.enabled ? { background: game.bg, boxShadow: game.shadow } : { background: game.bg }"
      @click="openGame(game)"
    >
      <span class="app-icon">{{ game.icon }}</span>
      <span class="app-name">{{ game.name }}</span>
      <span class="app-desc">{{ game.description }}</span>
      <span v-if="getGameStat(game)" class="app-stat">
        {{ getGameStat(game)?.label }} {{ getGameStat(game)?.value }}
      </span>
      <span v-if="game.enabled" class="app-hint">点击进入 →</span>
      <span v-else class="app-badge">即将上线</span>
    </button>
  </div>
</template>

<style scoped>
.game-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 20px;
}

.game-app {
  border: none;
  border-radius: 20px;
  padding: 28px 20px;
  min-height: 180px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #fff;
  cursor: pointer;
  transition: transform 0.25s ease, box-shadow 0.25s ease, filter 0.25s ease;
  text-align: center;
  position: relative;
  overflow: hidden;
}

.game-app::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.15) 0%, transparent 55%);
  pointer-events: none;
}

.game-app:not(.disabled):hover {
  transform: translateY(-6px) scale(1.03);
  filter: brightness(1.05);
}

.game-app:not(.disabled):active {
  transform: translateY(-2px) scale(0.99);
}

.game-app.disabled {
  cursor: not-allowed;
  opacity: 0.65;
  color: rgba(255, 255, 255, 0.9);
}

.app-icon {
  font-size: 44px;
  line-height: 1;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.12));
}

.app-name {
  font-size: 22px;
  font-weight: 800;
  letter-spacing: 1px;
  text-shadow: 0 2px 6px rgba(0, 0, 0, 0.12);
}

.app-desc {
  font-size: 13px;
  opacity: 0.9;
}

.app-hint {
  margin-top: 8px;
  font-size: 12px;
  opacity: 0.8;
}

.app-stat {
  background: rgba(255, 255, 255, 0.2);
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
  margin-top: 4px;
  padding: 4px 10px;
}

.app-badge {
  margin-top: 8px;
  font-size: 11px;
  background: rgba(255, 255, 255, 0.25);
  padding: 3px 10px;
  border-radius: 10px;
}
</style>
