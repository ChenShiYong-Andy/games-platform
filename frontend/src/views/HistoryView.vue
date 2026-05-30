<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getData } from '@/api'
import type { GameResponse } from '@/types'

const games = ref<GameResponse[]>([])
const loading = ref(true)

const diffMap: Record<string, string> = {
  EASY: '简单', MEDIUM: '中等', HARD: '困难'
}

const statusMap: Record<string, string> = {
  IN_PROGRESS: '进行中', COMPLETED: '已完成'
}

onMounted(async () => {
  try {
    games.value = await getData<GameResponse[]>('/sudoku/games')
  } finally {
    loading.value = false
  }
})

function formatTime(seconds: number) {
  const m = Math.floor(seconds / 60)
  const s = seconds % 60
  return `${m}分${s}秒`
}
</script>

<template>
  <div class="page-container">
    <h1>游戏记录</h1>
    <div class="card" v-loading="loading">
      <el-table :data="games" stripe v-if="games.length">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="难度" width="100">
          <template #default="{ row }">{{ diffMap[row.difficulty] || row.difficulty }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'COMPLETED' ? 'success' : 'info'" size="small">
              {{ statusMap[row.status] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="用时" width="120">
          <template #default="{ row }">{{ formatTime(row.elapsedSeconds) }}</template>
        </el-table-column>
        <el-table-column prop="hintsUsed" label="提示" width="80" />
        <el-table-column prop="mistakes" label="错误" width="80" />
        <el-table-column prop="score" label="得分" width="80" />
        <el-table-column prop="startedAt" label="开始时间" />
      </el-table>
      <div v-else class="empty">暂无游戏记录</div>
    </div>
  </div>
</template>

<style scoped>
h1 { margin-bottom: 24px; }
.empty { text-align: center; color: #999; padding: 40px; }
</style>
