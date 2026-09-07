<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useAuthStore } from '@/stores/auth'
import GameAppGrid from '@/components/GameAppGrid.vue'
import { gameApps } from '@/config/games'
import { getData } from '@/api'
import type { PointTransaction, TodayGameStats } from '@/types'

const authStore = useAuthStore()
const games = gameApps.filter((game) => game.id !== 'pet')
const petApps = gameApps.filter((game) => game.id === 'pet')
const transactions = ref<PointTransaction[]>([])
const transactionsLoading = ref(false)
const todayGameStats = ref<TodayGameStats>({ sudoku: 0, gomoku: 0, chess: 0 })
const recentIncome = computed(() =>
  transactions.value.filter((item) => item.amount > 0).reduce((sum, item) => sum + item.amount, 0)
)
const recentExpense = computed(() =>
  Math.abs(transactions.value.filter((item) => item.amount < 0).reduce((sum, item) => sum + item.amount, 0))
)

onMounted(async () => {
  await Promise.all([
    authStore.refreshProfile().catch(() => undefined),
    loadTransactions(),
    loadTodayGameStats()
  ])
})

async function loadTodayGameStats() {
  try {
    todayGameStats.value = await getData<TodayGameStats>('/dashboard/today-game-stats')
  } catch {
    todayGameStats.value = { sudoku: 0, gomoku: 0, chess: 0 }
  }
}

async function loadTransactions() {
  transactionsLoading.value = true
  try {
    transactions.value = await getData<PointTransaction[]>('/points/transactions', { limit: 20 })
  } catch {
    transactions.value = []
  } finally {
    transactionsLoading.value = false
  }
}
</script>

<template>
  <div class="page-container home-page">
    <div class="home-dashboard">
      <main class="home-main">
        <div class="hall-layout">
          <section class="hall-section pet-section">
            <div class="section-heading">
              <div>
                <h2 class="section-title">宠物养成</h2>
              </div>
            </div>
            <GameAppGrid :games="petApps" />
          </section>

          <section class="hall-section games-section">
            <div class="section-heading">
              <div>
                <h2 class="section-title">全部游戏</h2>
              </div>
              <span class="section-count">{{ games.length }} 款游戏</span>
            </div>
            <GameAppGrid :games="games" :today-counts="todayGameStats" />
          </section>
        </div>
      </main>

      <aside class="game-data-card">
        <div class="data-heading">
          <div>
            <span>我的数据</span>
            <h2>游戏数据</h2>
          </div>
          <button type="button" :disabled="transactionsLoading" @click="loadTransactions">↻</button>
        </div>

        <div class="data-stat-grid">
          <div><strong>{{ authStore.user?.totalPoints ?? 0 }}</strong><span>总积分</span></div>
          <div><strong>{{ authStore.user?.totalClears ?? 0 }}</strong><span>通关次数</span></div>
          <div><strong>{{ authStore.user?.loginStreak ?? 0 }}</strong><span>连续登录</span></div>
        </div>

        <div class="points-overview">
          <div class="income"><span>近期获取</span><strong>+{{ recentIncome }}</strong></div>
          <div class="expense"><span>近期使用</span><strong>-{{ recentExpense }}</strong></div>
        </div>

        <div class="transaction-heading">
          <h3>最近积分记录</h3>
          <span>最近 {{ transactions.length }} 条</span>
        </div>
        <div v-loading="transactionsLoading" class="transaction-list">
          <div v-if="transactions.length === 0 && !transactionsLoading" class="transaction-empty">
            暂无积分记录
          </div>
          <article v-for="tx in transactions" :key="tx.id" class="transaction-item">
            <span class="transaction-icon" :class="tx.amount > 0 ? 'gain' : 'cost'">
              {{ tx.amount > 0 ? '+' : '−' }}
            </span>
            <div class="transaction-copy">
              <strong>{{ tx.description || '积分变动' }}</strong>
              <time>{{ tx.createdAt }}</time>
            </div>
            <strong class="transaction-amount" :class="tx.amount > 0 ? 'positive' : 'negative'">
              {{ tx.amount > 0 ? '+' : '' }}{{ tx.amount }}
            </strong>
          </article>
        </div>
      </aside>
    </div>
  </div>
</template>

<style scoped>
.home-page { max-width: 1560px; }
.home-dashboard { display:grid;grid-template-columns:minmax(0,1fr) clamp(380px,28vw,460px);gap:24px;align-items:start; }
.home-main { min-width:0; }
.game-data-card { position:sticky;top:88px;min-height:640px;max-height:calc(100dvh - 112px);padding:24px;background:rgba(255,255,255,.88);border:1px solid rgba(255,255,255,.94);border-radius:20px;box-shadow:0 6px 26px rgba(70,76,120,.09);overflow:hidden;display:flex;flex-direction:column; }
.data-heading { display:flex;align-items:center;justify-content:space-between;gap:12px; }
.data-heading span { color:#9299a8;font-size:12px;font-weight:700; }
.data-heading h2 { margin-top:4px;font-size:21px; }
.data-heading button { width:34px;height:34px;border:1px solid #e3e6f0;border-radius:10px;background:#fff;color:#667eea;font-size:19px;cursor:pointer; }
.data-heading button:disabled { cursor:wait;opacity:.55; }
.data-stat-grid { display:grid;grid-template-columns:repeat(3,1fr);gap:8px;margin-top:20px; }
.data-stat-grid div { padding:13px 6px;border-radius:12px;background:#f5f6ff;text-align:center; }
.data-stat-grid strong,.data-stat-grid span { display:block; }
.data-stat-grid strong { color:#6869d9;font-size:20px; }
.data-stat-grid span { margin-top:4px;color:#949aaa;font-size:10px; }
.points-overview { display:grid;grid-template-columns:1fr 1fr;gap:10px;margin-top:12px; }
.points-overview div { display:flex;flex-direction:column;gap:5px;border-radius:12px;padding:12px; }
.points-overview span { color:#888f9d;font-size:11px; }
.points-overview strong { font-size:18px; }
.points-overview .income { background:#edf9f1;color:#2f9d57; }
.points-overview .expense { background:#fff3f1;color:#d75c4b; }
.transaction-heading { display:flex;align-items:center;justify-content:space-between;margin:22px 0 10px; }
.transaction-heading h3 { font-size:15px; }
.transaction-heading span { color:#a0a6b2;font-size:10px; }
.transaction-list { min-height:160px;overflow-y:auto;padding-right:3px; }
.transaction-item { display:grid;grid-template-columns:34px minmax(0,1fr) auto;align-items:center;gap:10px;padding:11px 0;border-bottom:1px solid #eef0f5; }
.transaction-icon { width:32px;height:32px;border-radius:10px;display:grid;place-items:center;font-weight:900; }
.transaction-icon.gain { background:#e9f8ee;color:#35a75c; }
.transaction-icon.cost { background:#fff0ee;color:#df6252; }
.transaction-copy { min-width:0;display:flex;flex-direction:column;gap:4px; }
.transaction-copy strong { overflow:hidden;color:#484e5b;font-size:12px;text-overflow:ellipsis;white-space:nowrap; }
.transaction-copy time { color:#a2a7b1;font-size:9px; }
.transaction-amount { font-size:14px; }
.positive { color:#35a75c; }
.negative { color:#df6252; }
.transaction-empty { padding:44px 0;color:#a0a6b2;text-align:center;font-size:13px; }

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #555;
}

.hall-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr);
  gap: 24px;
  align-items: start;
}

.hall-section {
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(255, 255, 255, 0.9);
  border-radius: 20px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(70, 76, 120, 0.07);
}

.pet-section {
  background: linear-gradient(160deg, rgba(255, 250, 241, 0.94), rgba(255, 255, 255, 0.78));
}

.section-heading {
  min-height: 28px;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 20px;
}

.section-count {
  flex: 0 0 auto;
  color: #7a79a8;
  background: #f0f0ff;
  border-radius: 999px;
  padding: 5px 10px;
  font-size: 12px;
  font-weight: 600;
}

@media (max-width: 900px) {
  .home-dashboard { grid-template-columns:1fr; }
  .game-data-card { position:static;min-height:0;max-height:none; }
  .hall-layout {
    grid-template-columns: 1fr;
  }

}

@media (max-width: 520px) {
  .hall-section {
    padding: 18px;
  }
}
</style>
