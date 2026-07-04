import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getData, postData, putData } from '@/api'
import type { AuthResponse, UserProfile } from '@/types'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(localStorage.getItem('token'))
  const user = ref<UserProfile | null>(
    localStorage.getItem('user') ? JSON.parse(localStorage.getItem('user')!) : null
  )

  const isLoggedIn = computed(() => !!token.value)

  function setAuth(data: AuthResponse) {
    token.value = data.token
    user.value = data.user
    localStorage.setItem('token', data.token)
    localStorage.setItem('user', JSON.stringify(data.user))
  }

  function logout() {
    token.value = null
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  async function register(username: string, password: string, nickname: string) {
    const data = await postData<AuthResponse>('/auth/register', { username, password, nickname })
    setAuth(data)
    return data
  }

  async function login(username: string, password: string) {
    const data = await postData<AuthResponse>('/auth/login', { username, password })
    setAuth(data)
    return data
  }

  async function refreshProfile() {
    const profile = await getData<UserProfile>('/user/profile')
    user.value = profile
    localStorage.setItem('user', JSON.stringify(profile))
    return profile
  }

  async function updateProfile(data: {
    nickname?: string
    email?: string
    avatarUrl?: string
    sudokuDailyLimit?: number
  }) {
    const profile = await putData<UserProfile>('/user/profile', data)
    user.value = profile
    localStorage.setItem('user', JSON.stringify(profile))
    return profile
  }

  return { token, user, isLoggedIn, register, login, logout, refreshProfile, updateProfile }
})
