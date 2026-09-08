<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAdminAuthStore } from '@/stores/adminAuth'

const router = useRouter()
const store = useAdminAuthStore()
const form = ref({ username: '', displayName: '', password: '', confirmPassword: '' })
const loading = ref(false)

async function submit() {
  if (!form.value.username || !form.value.displayName || form.value.password.length < 6) {
    return ElMessage.warning('请完整填写信息，密码至少 6 位')
  }
  if (form.value.password !== form.value.confirmPassword) return ElMessage.warning('两次密码不一致')
  loading.value = true
  try {
    await store.register(form.value.username, form.value.password, form.value.displayName)
    await router.push('/admin')
  } catch (error: unknown) {
    ElMessage.error(error instanceof Error ? error.message : '创建失败')
  } finally { loading.value = false }
}
</script>

<template>
  <div class="admin-auth-page">
    <section class="admin-auth-card">
      <div class="shield">🛡️</div><h1>创建管理员账号</h1><p>管理员账号与普通用户账号相互独立</p>
      <el-input v-model="form.username" size="large" placeholder="管理员账号" />
      <el-input v-model="form.displayName" size="large" placeholder="显示名称" />
      <el-input v-model="form.password" size="large" type="password" show-password placeholder="密码（至少 6 位）" />
      <el-input v-model="form.confirmPassword" size="large" type="password" show-password placeholder="确认密码" @keyup.enter="submit" />
      <el-button type="primary" size="large" :loading="loading" @click="submit">创建并进入</el-button>
      <div class="switch">已有管理员账号？<router-link to="/admin/login">返回登录</router-link></div>
    </section>
  </div>
</template>

<style scoped>
.admin-auth-page{min-height:100vh;display:grid;place-items:center;background:linear-gradient(145deg,#1f293b,#344766)}.admin-auth-card{width:min(420px,calc(100% - 32px));padding:38px 42px;display:flex;flex-direction:column;gap:14px;border-radius:20px;background:#fff;box-shadow:0 24px 70px #10172588;text-align:center}.shield{font-size:44px}.admin-auth-card h1{font-size:27px}.admin-auth-card p,.switch{color:#7d8594}.switch a{color:#409eff;font-weight:700}
</style>
