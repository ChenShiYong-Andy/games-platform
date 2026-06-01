import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

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
      path: '/',
      component: () => import('@/layouts/MainLayout.vue'),
      meta: { requiresAuth: true },
      children: [
        { path: '', name: 'Home', component: () => import('@/views/HomeView.vue') },
        { path: 'games/sudoku', name: 'Sudoku', component: () => import('@/views/games/SudokuView.vue') },
        { path: 'games/sudoku/play', name: 'SudokuPlay', component: () => import('@/views/games/SudokuPlayView.vue') },
        { path: 'games/zoo', name: 'ZooKeeper', component: () => import('@/views/games/ZooKeeperView.vue') },
        { path: 'profile', name: 'Profile', component: () => import('@/views/ProfileView.vue') },
        { path: 'ranking', name: 'Ranking', component: () => import('@/views/RankingView.vue') },
        { path: 'achievements', name: 'Achievements', component: () => import('@/views/AchievementsView.vue') },
        // 旧路由兼容
        { path: 'game', redirect: '/games/sudoku' },
        { path: 'game/play', redirect: to => ({ path: '/games/sudoku/play', query: to.query }) }
      ]
    }
  ]
})

router.beforeEach((to, _from, next) => {
  const authStore = useAuthStore()
  if (to.meta.requiresAuth && !authStore.isLoggedIn) {
    next('/login')
  } else if (to.meta.guest && authStore.isLoggedIn) {
    next('/')
  } else {
    next()
  }
})

export default router
