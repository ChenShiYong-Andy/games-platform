<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { getData } from '@/api'
import type { PointTransaction } from '@/types'
import { ElMessage } from 'element-plus'

const authStore = useAuthStore()
const form = ref({ nickname: '', email: '', avatarUrl: '' })
const gameConfig = ref({ sudokuDailyLimit: 5, zooDailyCareLimit: 3 })
const transactions = ref<PointTransaction[]>([])
const saving = ref(false)
const savingGameConfig = ref(false)

onMounted(async () => {
  const user = authStore.user
  if (user) {
    form.value.nickname = user.nickname
    form.value.email = user.email || ''
    form.value.avatarUrl = user.avatarUrl || ''
    gameConfig.value.sudokuDailyLimit = user.sudokuDailyLimit || 5
    gameConfig.value.zooDailyCareLimit = user.zooDailyCareLimit || 3
  }
  try {
    transactions.value = await getData<PointTransaction[]>('/points/transactions', { limit: 10 })
  } catch { /* ignore */ }
})

async function saveProfile() {
  saving.value = true
  try {
    await authStore.updateProfile(form.value)
    ElMessage.success('资料已更新')
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : '更新失败')
  } finally {
    saving.value = false
  }
}

async function saveGameConfig() {
  savingGameConfig.value = true
  try {
    await authStore.updateProfile(gameConfig.value)
    ElMessage.success('游戏配置已更新')
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : '更新失败')
  } finally {
    savingGameConfig.value = false
  }
}
</script>

<template>
  <div class="page-container">
    <div class="profile-grid">
      <div class="profile-column">
        <div class="card profile-card">
          <h2>个人资料</h2>
          <div class="avatar-section">
            <div class="avatar-large">{{ form.nickname?.charAt(0) || '?' }}</div>
            <div class="level-badge">Lv.{{ authStore.user?.level }}</div>
          </div>
          <el-form label-width="80px" style="margin-top: 24px">
            <el-form-item label="用户名">
              <el-input :model-value="authStore.user?.username" disabled />
            </el-form-item>
            <el-form-item label="昵称">
              <el-input v-model="form.nickname" />
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="form.email" />
            </el-form-item>
            <el-form-item label="头像URL">
              <el-input v-model="form.avatarUrl" placeholder="输入头像图片链接" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="saving" @click="saveProfile">保存</el-button>
            </el-form-item>
          </el-form>
        </div>

        <div class="card game-config-card">
          <h2>游戏配置</h2>
          <p class="config-tip">设置每天的游戏次数上限，帮助合理安排游戏时间。</p>
          <el-form label-width="140px">
            <el-form-item label="每日数独次数">
              <el-input-number v-model="gameConfig.sudokuDailyLimit" :min="1" :max="100" />
            </el-form-item>
            <el-form-item label="每日动物园照顾次数">
              <el-input-number v-model="gameConfig.zooDailyCareLimit" :min="1" :max="1000" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="savingGameConfig" @click="saveGameConfig">保存配置</el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>

      <div class="card stats-card">
        <h2>游戏数据</h2>
        <div class="stat-grid">
          <div class="stat"><span class="val">{{ authStore.user?.totalPoints }}</span><span class="lbl">总积分</span></div>
          <div class="stat"><span class="val">{{ authStore.user?.totalClears }}</span><span class="lbl">通关次数</span></div>
          <div class="stat"><span class="val">{{ authStore.user?.loginStreak }}</span><span class="lbl">连续登录</span></div>
        </div>

        <h3 style="margin-top: 24px">最近积分记录</h3>
        <div v-if="transactions.length === 0" class="empty">暂无记录</div>
        <div v-for="tx in transactions" :key="tx.id" class="tx-item">
          <span>{{ tx.description }}</span>
          <span :class="tx.amount > 0 ? 'positive' : 'negative'">
            {{ tx.amount > 0 ? '+' : '' }}{{ tx.amount }}
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.profile-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 24px; }
.profile-column { display: flex; flex-direction: column; gap: 24px; }
h2 { margin-bottom: 16px; }
.config-tip { margin: -4px 0 16px; color: #999; font-size: 14px; line-height: 1.6; }
.avatar-section { display: flex; align-items: center; gap: 16px; }
.avatar-large {
  width: 64px; height: 64px; border-radius: 50%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff; display: flex; align-items: center; justify-content: center;
  font-size: 28px; font-weight: 700;
}
.level-badge { background: #667eea; color: #fff; padding: 4px 12px; border-radius: 12px; font-size: 14px; }
.stat-grid { display: flex; gap: 24px; }
.stat { display: flex; flex-direction: column; align-items: center; }
.val { font-size: 24px; font-weight: 700; color: #667eea; }
.lbl { font-size: 13px; color: #999; }
.tx-item { display: flex; justify-content: space-between; padding: 8px 0; border-bottom: 1px solid #f0f0f0; font-size: 14px; }
.positive { color: #52c41a; font-weight: 600; }
.negative { color: #f5222d; font-weight: 600; }
.empty { color: #999; text-align: center; padding: 16px; }
@media (max-width: 768px) { .profile-grid { grid-template-columns: 1fr; } }
</style>
