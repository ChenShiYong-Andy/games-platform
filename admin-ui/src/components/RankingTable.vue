<script setup lang="ts">
import type { RankingEntry } from '@/types'

defineProps<{
  data: RankingEntry[]
  loading: boolean
  scoreLabel: string
}>()
</script>

<template>
  <div class="card" v-loading="loading">
    <table class="rank-table" v-if="data.length">
      <thead>
        <tr>
          <th>排名</th>
          <th>玩家</th>
          <th>{{ scoreLabel }}</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="entry in data" :key="entry.userId" :class="{ top3: entry.rank <= 3 }">
          <td>
            <span class="rank-badge" :class="'rank-' + entry.rank">{{ entry.rank }}</span>
          </td>
          <td>{{ entry.nickname }}</td>
          <td class="score">{{ entry.score }}</td>
        </tr>
      </tbody>
    </table>
    <div v-else class="empty">暂无排行数据</div>
  </div>
</template>

<style scoped>
.rank-table { width: 100%; border-collapse: collapse; }
.rank-table th, .rank-table td { padding: 12px 16px; text-align: left; border-bottom: 1px solid #f0f0f0; }
.rank-table th { color: #999; font-weight: 500; font-size: 13px; }
.top3 { background: #fafafa; }
.rank-badge {
  display: inline-block; width: 28px; height: 28px; line-height: 28px;
  text-align: center; border-radius: 50%; font-weight: 700; font-size: 13px;
  background: #f0f0f0;
}
.rank-1 { background: #ffd700; color: #fff; }
.rank-2 { background: #c0c0c0; color: #fff; }
.rank-3 { background: #cd7f32; color: #fff; }
.score { font-weight: 600; color: #667eea; }
.empty { text-align: center; color: #999; padding: 40px; }
</style>
