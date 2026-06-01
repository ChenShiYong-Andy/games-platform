<script setup lang="ts">
import { useRouter, useRoute, RouterLink } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { computed } from 'vue'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const activeMenu = computed(() => route.path)

function handleLogout() {
  authStore.logout()
  router.push('/login')
}
</script>

<template>
  <div class="layout">
    <header class="header">
      <div class="header-inner">
        <RouterLink to="/" class="logo">
          <span class="logo-icon">🎮</span>
          Games Platform
        </RouterLink>
        <nav class="nav">
          <RouterLink to="/" :class="{ active: activeMenu === '/' }">游戏大厅</RouterLink>
          <RouterLink to="/ranking" :class="{ active: activeMenu === '/ranking' }">排行榜</RouterLink>
          <RouterLink to="/achievements" :class="{ active: activeMenu === '/achievements' }">成就</RouterLink>
        </nav>
        <div class="user-area">
          <RouterLink to="/profile" class="user-info">
            <span class="avatar">{{ authStore.user?.nickname?.charAt(0) || '?' }}</span>
            <span>{{ authStore.user?.nickname }}</span>
            <span class="level">Lv.{{ authStore.user?.level }}</span>
          </RouterLink>
          <el-button size="small" @click="handleLogout">退出</el-button>
        </div>
      </div>
    </header>
    <main class="main">
      <router-view />
    </main>
  </div>
</template>

<style scoped>
.layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.header-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.logo {
  font-size: 20px;
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #fff;
}

.logo-icon {
  font-size: 24px;
}

.nav {
  display: flex;
  gap: 24px;
}

.nav a {
  color: rgba(255, 255, 255, 0.85);
  font-size: 15px;
  padding: 4px 0;
  border-bottom: 2px solid transparent;
  transition: all 0.2s;
}

.nav a:hover,
.nav a.active {
  color: #fff;
  border-bottom-color: #fff;
}

.user-area {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #fff;
}

.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
}

.level {
  background: rgba(255, 255, 255, 0.2);
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
}

.main {
  flex: 1;
  padding: 24px;
}
</style>
