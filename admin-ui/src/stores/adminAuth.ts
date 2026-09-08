import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { getAdminData, postAdminData } from '@/api/admin'
import type { AdminAuthResponse, AdminProfile } from '@/types'

export const useAdminAuthStore = defineStore('adminAuth', () => {
  const token = ref<string | null>(sessionStorage.getItem('adminToken'))
  const admin = ref<AdminProfile | null>(
    sessionStorage.getItem('adminProfile')
      ? JSON.parse(sessionStorage.getItem('adminProfile')!) : null
  )
  const isLoggedIn = computed(() => Boolean(token.value))

  function setAuth(data: AdminAuthResponse) {
    token.value = data.token
    admin.value = data.admin
    sessionStorage.setItem('adminToken', data.token)
    sessionStorage.setItem('adminProfile', JSON.stringify(data.admin))
  }

  async function login(username: string, password: string) {
    const data = await postAdminData<AdminAuthResponse>('/auth/login', { username, password })
    setAuth(data)
  }

  async function register(username: string, password: string, displayName: string) {
    const data = await postAdminData<AdminAuthResponse>('/auth/register', {
      username, password, displayName
    })
    setAuth(data)
  }

  async function refreshProfile() {
    admin.value = await getAdminData<AdminProfile>('/auth/profile')
    sessionStorage.setItem('adminProfile', JSON.stringify(admin.value))
  }

  function logout() {
    token.value = null
    admin.value = null
    sessionStorage.removeItem('adminToken')
    sessionStorage.removeItem('adminProfile')
  }

  return { token, admin, isLoggedIn, login, register, refreshProfile, logout }
})
