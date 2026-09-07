<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'

const authStore = useAuthStore()
const form = ref({ nickname: '', email: '', avatarUrl: '' })
const saving = ref(false)

onMounted(async () => {
  const user = authStore.user
  if (user) {
    form.value.nickname = user.nickname
    form.value.email = user.email || ''
    form.value.avatarUrl = user.avatarUrl || ''
  }
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

</script>

<template>
  <div class="page-container">
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
  </div>
</template>

<style scoped>
.profile-card { max-width:720px;margin:0 auto; }
h2 { margin-bottom: 16px; }
.avatar-section { display: flex; align-items: center; gap: 16px; }
.avatar-large {
  width: 64px; height: 64px; border-radius: 50%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff; display: flex; align-items: center; justify-content: center;
  font-size: 28px; font-weight: 700;
}
.level-badge { background: #667eea; color: #fff; padding: 4px 12px; border-radius: 12px; font-size: 14px; }
</style>
