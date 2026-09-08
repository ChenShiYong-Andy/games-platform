<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useAdminAuthStore } from '@/stores/adminAuth'

const router = useRouter()
const adminStore = useAdminAuthStore()

function logout() {
  adminStore.logout()
  void router.push('/admin/login')
}
</script>

<template>
  <div class="admin-layout">
    <header>
      <div class="header-inner">
        <router-link to="/admin" class="brand">🛡️ 管理后台</router-link>
        <div class="account">
          <span>{{ adminStore.admin?.displayName }}</span>
          <small>@{{ adminStore.admin?.username }}</small>
          <el-button size="small" @click="logout">退出</el-button>
        </div>
      </div>
    </header>
    <main><router-view /></main>
  </div>
</template>

<style scoped>
.admin-layout{min-height:100vh;background:#f4f6fb}.admin-layout header{height:64px;background:#27344d;color:#fff}.header-inner{height:100%;max-width:1320px;margin:auto;padding:0 24px;display:flex;align-items:center;justify-content:space-between}.brand{color:#fff;font-size:20px;font-weight:800}.account{display:flex;align-items:center;gap:10px}.account small{color:#aeb8cb}.admin-layout main{padding:24px}
</style>
