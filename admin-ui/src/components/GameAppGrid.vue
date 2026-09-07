<script setup lang="ts">
import { useRouter } from 'vue-router'
import { gameApps, type GameApp } from '@/config/games'
import { ElMessage } from 'element-plus'
import GameEntryCard from '@/components/GameEntryCard.vue'

const router = useRouter()
const props = withDefaults(defineProps<{
  games?: GameApp[]
  todayCounts?: Partial<Record<string, number>>
}>(), {
  games: () => gameApps,
  todayCounts: () => ({})
})

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
    <GameEntryCard
      v-for="game in props.games"
      :key="game.id"
      :title="game.name"
      :icon="game.icon"
      :disabled="!game.enabled"
      :icon-variant="game.id === 'chess' ? 'chess' : 'default'"
      :statistic="props.todayCounts[game.id]"
      :card-style="{
        background: game.bg,
        boxShadow: game.enabled ? game.shadow : undefined
      }"
      @click="openGame(game)"
    />
  </div>
</template>

<style scoped>
.game-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 20px;
}

@media (max-width: 760px) {
  .game-grid { grid-template-columns: repeat(2, minmax(0, 1fr));gap:14px; }
}

</style>
