<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'
import { getData, postData } from '@/api'
import { copyText } from '@/utils/clipboard'
import type { AdminBindingCode, AdminBindingStatus } from '@/types'

const authStore = useAuthStore()
const form = ref({ nickname: '', email: '', avatarUrl: '' })
const saving = ref(false)
const bindingStatus = ref<AdminBindingStatus | null>(null)
const bindingCode = ref<AdminBindingCode | null>(null)

onMounted(async () => {
  const user = authStore.user
  if (user) {
    form.value.nickname = user.nickname
    form.value.email = user.email || ''
    form.value.avatarUrl = user.avatarUrl || ''
  }
  try {
    bindingStatus.value = await getData<AdminBindingStatus>('/user/admin-binding')
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : '管理员关联状态加载失败')
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

async function generateBindingCode() {
  try {
    bindingCode.value = await postData<AdminBindingCode>('/user/admin-binding/code')
    ElMessage.success('一次性绑定码已生成，10 分钟内有效')
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : '生成失败')
  }
}

async function copyBindingCode() {
  if (!bindingCode.value) return
  await copyText(bindingCode.value.code)
  ElMessage.success('绑定码已复制')
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
    <div class="card binding-card">
      <div><h2>管理员关联</h2><p>关联后，管理员可以为你配置每日英语并管理积分、宠物数据。</p></div>
      <div v-if="bindingStatus?.bound" class="bound-info">
        <span>已关联</span><strong>{{ bindingStatus.adminDisplayName }} (@{{ bindingStatus.adminUsername }})</strong>
        <small>普通用户不能主动解除关联，如需解除请联系管理员。</small>
      </div>
      <div v-else class="code-area">
        <el-button type="primary" plain @click="generateBindingCode">生成一次性绑定码</el-button>
        <button v-if="bindingCode" class="code" @click="copyBindingCode">{{ bindingCode.code }} <small>点击复制</small></button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.profile-card { max-width:720px;margin:0 auto; }
.binding-card{max-width:720px;margin:20px auto 0;padding:24px;display:flex;justify-content:space-between;align-items:center;gap:24px}.binding-card p{color:#8991a1;font-size:13px}.bound-info,.code-area{display:flex;flex-direction:column;align-items:flex-end;gap:7px}.bound-info span{color:#2cb56f;font-weight:800}.bound-info small{color:#9aa1af}.code{border:1px dashed #667eea;border-radius:10px;background:#f1f3ff;color:#5265d7;font-size:21px;font-weight:900;letter-spacing:3px;padding:9px 13px;cursor:pointer}.code small{font-size:11px;letter-spacing:0;font-weight:500}@media(max-width:650px){.binding-card{align-items:flex-start;flex-direction:column}.bound-info,.code-area{align-items:flex-start}}
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
