<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAdminAuthStore } from '@/stores/adminAuth'

const router = useRouter()
const store = useAdminAuthStore()
const form = ref({ username: '', password: '' })
const loading = ref(false)

async function submit() {
  if (!form.value.username || !form.value.password) return ElMessage.warning('请填写管理员账号和密码')
  loading.value = true
  try {
    await store.login(form.value.username, form.value.password)
    await router.push('/admin')
  } catch (error: unknown) {
    ElMessage.error(error instanceof Error ? error.message : '登录失败')
  } finally { loading.value = false }
}
</script>

<template>
  <div class="admin-auth-page">
    <section class="admin-auth-card">
      <div class="shield">🛡️</div><h1>管理员登录</h1><p>使用独立管理员账号进入管理工作台</p>
      <el-input v-model="form.username" size="large" placeholder="管理员账号" />
      <el-input v-model="form.password" size="large" type="password" show-password placeholder="管理员密码" @keyup.enter="submit" />
      <el-button type="primary" size="large" :loading="loading" @click="submit">登录管理后台</el-button>
      <div class="switch">还没有管理员账号？<router-link to="/admin/register">创建账号</router-link></div>
      <router-link class="home" to="/">返回用户端</router-link>
    </section>
  </div>
</template>

<style scoped>
.admin-auth-page{min-height:100vh;display:grid;place-items:center;background:linear-gradient(145deg,#1f293b,#344766)}.admin-auth-card{width:min(420px,calc(100% - 32px));padding:42px;display:flex;flex-direction:column;gap:16px;border-radius:20px;background:#fff;box-shadow:0 24px 70px #10172588;text-align:center}.shield{font-size:46px}.admin-auth-card h1{font-size:28px}.admin-auth-card p,.switch{color:#7d8594}.switch a{color:#409eff;font-weight:700}.home{color:#8b94a6;font-size:13px}
</style>
