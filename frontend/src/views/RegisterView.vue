<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'

const router = useRouter()
const authStore = useAuthStore()

const form = ref({ username: '', password: '', nickname: '' })
const loading = ref(false)

async function handleRegister() {
  if (!form.value.username || !form.value.password || !form.value.nickname) {
    ElMessage.warning('请填写所有字段')
    return
  }
  loading.value = true
  try {
    await authStore.register(form.value.username, form.value.password, form.value.nickname)
    ElMessage.success('注册成功')
    router.push('/')
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : '注册失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-page">
    <div class="auth-card">
      <h1>创建账号</h1>
      <p class="subtitle">加入 Games Platform</p>
      <el-form @submit.prevent="handleRegister">
        <el-form-item>
          <el-input v-model="form.username" placeholder="用户名（3-50字符）" size="large" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.nickname" placeholder="昵称" size="large" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" type="password" placeholder="密码（6-50字符）" size="large" show-password />
        </el-form-item>
        <el-button type="primary" size="large" :loading="loading" @click="handleRegister" style="width: 100%">
          注册
        </el-button>
      </el-form>
      <p class="footer-link">
        已有账号？<router-link to="/login">立即登录</router-link>
      </p>
    </div>
  </div>
</template>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.auth-card {
  background: #fff;
  border-radius: 16px;
  padding: 48px 40px;
  width: 400px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
}

h1 {
  text-align: center;
  font-size: 28px;
  margin-bottom: 8px;
}

.subtitle {
  text-align: center;
  color: #999;
  margin-bottom: 32px;
}

.footer-link {
  text-align: center;
  margin-top: 24px;
  color: #666;
}

.footer-link a {
  color: #667eea;
  font-weight: 500;
}
</style>
