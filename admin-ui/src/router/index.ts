import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useAdminAuthStore } from '@/stores/adminAuth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/LoginView.vue'),
      meta: { guest: true }
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('@/views/RegisterView.vue'),
      meta: { guest: true }
    },
    {
      path: '/admin/login',
      name: 'AdminLogin',
      component: () => import('@/views/admin/AdminLoginView.vue'),
      meta: { adminGuest: true }
    },
    {
      path: '/admin/register',
      name: 'AdminRegister',
      component: () => import('@/views/admin/AdminRegisterView.vue'),
      meta: { adminGuest: true }
    },
    {
      path: '/admin',
      component: () => import('@/layouts/AdminLayout.vue'),
      meta: { requiresAdminAuth: true },
      children: [
        { path: '', name: 'Admin', component: () => import('@/views/AdminView.vue') }
      ]
    },
    {
      path: '/',
      component: () => import('@/layouts/MainLayout.vue'),
      meta: { requiresAuth: true },
      children: [
        { path: '', name: 'Home', component: () => import('@/views/HomeView.vue') },
        { path: 'games/sudoku', name: 'Sudoku', component: () => import('@/views/games/SudokuView.vue') },
        { path: 'games/sudoku/play', name: 'SudokuPlay', component: () => import('@/views/games/SudokuPlayView.vue') },
        { path: 'games/pet', name: 'Pet', component: () => import('@/views/games/PetView.vue') },
        { path: 'daily-english', name: 'DailyEnglish', component: () => import('@/views/DailyEnglishView.vue') },
        { path: 'games/gomoku', name: 'Gomoku', component: () => import('@/views/games/GomokuView.vue') },
        { path: 'games/chess', name: 'ChineseChess', component: () => import('@/views/games/ChineseChessView.vue') },
        { path: 'profile', name: 'Profile', component: () => import('@/views/ProfileView.vue') },
        { path: 'ranking', name: 'Ranking', component: () => import('@/views/RankingView.vue') },
        // 旧路由兼容
        { path: 'game', redirect: '/games/sudoku' },
        { path: 'game/play', redirect: to => ({ path: '/games/sudoku/play', query: to.query }) }
      ]
    }
  ]
})

router.beforeEach((to, _from, next) => {
  const authStore = useAuthStore()
  const adminAuthStore = useAdminAuthStore()
  if (to.meta.requiresAdminAuth && !adminAuthStore.isLoggedIn) {
    next('/admin/login')
  } else if (to.meta.adminGuest && adminAuthStore.isLoggedIn) {
    next('/admin')
  } else if (to.meta.requiresAuth && !authStore.isLoggedIn) {
    next('/login')
  } else if (to.meta.guest && authStore.isLoggedIn) {
    next('/')
  } else {
    next()
  }
})

export default router
