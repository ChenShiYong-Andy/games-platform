<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getData } from '@/api'
import type { RankingEntry } from '@/types'
import RankingTable from '@/components/RankingTable.vue'

const activeTab = ref('total')
const totalRanking = ref<RankingEntry[]>([])
const weeklyRanking = ref<RankingEntry[]>([])
const speedRanking = ref<RankingEntry[]>([])
const speedDifficulty = ref('EASY')
const loading = ref(false)

async function loadRanking() {
  loading.value = true
  try {
    if (activeTab.value === 'total') {
      totalRanking.value = await getData<RankingEntry[]>('/ranking/total')
    } else if (activeTab.value === 'weekly') {
      weeklyRanking.value = await getData<RankingEntry[]>('/ranking/weekly')
    } else {
      speedRanking.value = await getData<RankingEntry[]>('/ranking/sudoku-speed', { difficulty: speedDifficulty.value })
    }
  } finally {
    loading.value = false
  }
}

onMounted(loadRanking)
</script>

<template>
  <div class="page-container">
    <h1>排行榜</h1>
    <el-tabs v-model="activeTab" @tab-change="loadRanking">
      <el-tab-pane label="总积分榜" name="total">
        <RankingTable :data="totalRanking" :loading="loading" score-label="积分" />
      </el-tab-pane>
      <el-tab-pane label="本周积分榜" name="weekly">
        <RankingTable :data="weeklyRanking" :loading="loading" score-label="本周积分" />
      </el-tab-pane>
      <el-tab-pane label="数独速度榜" name="speed">
        <div style="margin-bottom: 16px">
          <el-radio-group v-model="speedDifficulty" @change="loadRanking">
            <el-radio-button value="EASY">简单</el-radio-button>
            <el-radio-button value="MEDIUM">中等</el-radio-button>
            <el-radio-button value="HARD">困难</el-radio-button>
          </el-radio-group>
        </div>
        <RankingTable :data="speedRanking" :loading="loading" score-label="用时(秒)" />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<style scoped>
h1 { margin-bottom: 24px; }
</style>
