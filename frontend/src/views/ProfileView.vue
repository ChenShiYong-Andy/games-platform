<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { getData, postData, putData } from '@/api'
import type { PointTransaction, UserProfile } from '@/types'
import { ElMessage } from 'element-plus'

interface AdminStatus {
  adminPasswordSet: boolean
}

const authStore = useAuthStore()
const form = ref({ nickname: '', email: '', avatarUrl: '' })
const adminPasswordSet = ref(false)
const adminPanel = ref<'game' | 'points' | ''>('')
const setPasswordVisible = ref(false)
const adminPasswordForm = ref({ password: '' })
const gameConfig = ref({ sudokuDailyLimit: 5, adminPassword: '' })
const pointsForm = ref({ amount: 0, description: '', adminPassword: '' })
const transactions = ref<PointTransaction[]>([])
const saving = ref(false)
const settingPassword = ref(false)
const savingGameConfig = ref(false)
const adjustingPoints = ref(false)

onMounted(async () => {
  const user = authStore.user
  if (user) {
    form.value.nickname = user.nickname
    form.value.email = user.email || ''
    form.value.avatarUrl = user.avatarUrl || ''
    gameConfig.value.sudokuDailyLimit = user.sudokuDailyLimit || 5
    adminPasswordSet.value = !!user.adminPasswordSet
  }
  await loadAdminStatus()
  await loadTransactions()
})

async function loadAdminStatus() {
  try {
    const status = await getData<AdminStatus>('/admin/config/status')
    adminPasswordSet.value = status.adminPasswordSet
  } catch {
    adminPasswordSet.value = !!authStore.user?.adminPasswordSet
  }
}

async function loadTransactions() {
  try {
    transactions.value = await getData<PointTransaction[]>('/points/transactions', { limit: 10 })
  } catch { /* ignore */ }
}

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

async function setAdminPassword() {
  if (!adminPasswordForm.value.password) {
    ElMessage.info('请输入管理密码')
    return
  }
  settingPassword.value = true
  try {
    await postData<AdminStatus>('/admin/config/password', {
      password: adminPasswordForm.value.password
    })
    adminPasswordSet.value = true
    setPasswordVisible.value = false
    adminPasswordForm.value.password = ''
    await authStore.refreshProfile()
    ElMessage.success('管理密码已设置')
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : '设置失败')
  } finally {
    settingPassword.value = false
  }
}

async function saveGameConfig() {
  if (!gameConfig.value.adminPassword) {
    ElMessage.info('请输入管理密码')
    return
  }
  savingGameConfig.value = true
  try {
    const profile = await putData<UserProfile>('/admin/config/game', gameConfig.value)
    authStore.user = profile
    localStorage.setItem('user', JSON.stringify(profile))
    gameConfig.value.adminPassword = ''
    ElMessage.success('游戏配置已更新')
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : '更新失败')
  } finally {
    savingGameConfig.value = false
  }
}

async function adjustPoints() {
  if (!pointsForm.value.adminPassword) {
    ElMessage.info('请输入管理密码')
    return
  }
  if (!pointsForm.value.amount) {
    ElMessage.info('请输入非 0 的积分调整值')
    return
  }
  adjustingPoints.value = true
  try {
    const profile = await postData<UserProfile>('/admin/config/points', pointsForm.value)
    authStore.user = profile
    localStorage.setItem('user', JSON.stringify(profile))
    pointsForm.value = { amount: 0, description: '', adminPassword: '' }
    await loadTransactions()
    ElMessage.success('积分已调整')
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : '调整失败')
  } finally {
    adjustingPoints.value = false
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

        <div class="card admin-card">
          <h2>管理员配置</h2>
          <div v-if="!adminPasswordSet" class="admin-empty">
            <el-button type="primary" @click="setPasswordVisible = true">设置管理密码</el-button>
          </div>
          <template v-else>
            <div class="admin-entry-grid">
              <button class="admin-entry" :class="{ active: adminPanel === 'game' }" @click="adminPanel = 'game'">
                <strong>游戏配置</strong>
                <span>配置每日游戏次数</span>
              </button>
              <button class="admin-entry" :class="{ active: adminPanel === 'points' }" @click="adminPanel = 'points'">
                <strong>积分调整</strong>
                <span>手动增加或扣减积分</span>
              </button>
            </div>

            <div v-if="adminPanel === 'game'" class="admin-panel">
              <el-form label-width="140px">
                <el-form-item label="每日数独次数">
                  <el-input-number v-model="gameConfig.sudokuDailyLimit" :min="1" :max="100" />
                </el-form-item>
                <el-form-item label="管理密码">
                  <el-input v-model="gameConfig.adminPassword" type="password" show-password />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" :loading="savingGameConfig" @click="saveGameConfig">保存配置</el-button>
                </el-form-item>
              </el-form>
            </div>

            <div v-if="adminPanel === 'points'" class="admin-panel">
              <el-form label-width="140px">
                <el-form-item label="调整积分">
                  <el-input-number v-model="pointsForm.amount" :min="-100000" :max="100000" />
                </el-form-item>
                <el-form-item label="调整说明">
                  <el-input v-model="pointsForm.description" placeholder="例如：活动奖励、误发扣回" />
                </el-form-item>
                <el-form-item label="管理密码">
                  <el-input v-model="pointsForm.adminPassword" type="password" show-password />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" :loading="adjustingPoints" @click="adjustPoints">提交调整</el-button>
                </el-form-item>
              </el-form>
            </div>
          </template>
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

    <el-dialog v-model="setPasswordVisible" title="设置管理密码" width="420px">
      <el-form label-width="90px">
        <el-form-item label="管理密码">
          <el-input v-model="adminPasswordForm.password" type="password" show-password placeholder="至少 6 位" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="setPasswordVisible = false">取消</el-button>
        <el-button type="primary" :loading="settingPassword" @click="setAdminPassword">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.profile-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 24px; }
.profile-column { display: flex; flex-direction: column; gap: 24px; }
h2 { margin-bottom: 16px; }
.avatar-section { display: flex; align-items: center; gap: 16px; }
.avatar-large {
  width: 64px; height: 64px; border-radius: 50%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff; display: flex; align-items: center; justify-content: center;
  font-size: 28px; font-weight: 700;
}
.level-badge { background: #667eea; color: #fff; padding: 4px 12px; border-radius: 12px; font-size: 14px; }
.admin-empty { display: flex; justify-content: flex-start; padding: 8px 0; }
.admin-entry-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px; }
.admin-entry {
  border: 1px solid #e7e9f2;
  background: #fff;
  border-radius: 10px;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  text-align: left;
  cursor: pointer;
}
.admin-entry.active { border-color: #667eea; background: #f5f7ff; }
.admin-entry strong { color: #333; font-size: 16px; }
.admin-entry span { color: #888; font-size: 13px; }
.admin-panel { margin-top: 18px; }
.stat-grid { display: flex; gap: 24px; }
.stat { display: flex; flex-direction: column; align-items: center; }
.val { font-size: 24px; font-weight: 700; color: #667eea; }
.lbl { font-size: 13px; color: #999; }
.tx-item { display: flex; justify-content: space-between; padding: 8px 0; border-bottom: 1px solid #f0f0f0; font-size: 14px; }
.positive { color: #52c41a; font-weight: 600; }
.negative { color: #f5222d; font-weight: 600; }
.empty { color: #999; text-align: center; padding: 16px; }
@media (max-width: 768px) {
  .profile-grid { grid-template-columns: 1fr; }
  .admin-entry-grid { grid-template-columns: 1fr; }
}
</style>
