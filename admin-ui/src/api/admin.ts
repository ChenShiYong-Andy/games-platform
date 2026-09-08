import axios from 'axios'
import type { ApiResponse } from '@/types'
import router from '@/router'

const ADMIN_TOKEN_KEY = 'adminToken'
const ADMIN_PROFILE_KEY = 'adminProfile'

const adminApi = axios.create({ baseURL: '/api/admin', timeout: 10000 })

adminApi.interceptors.request.use(config => {
  const token = sessionStorage.getItem(ADMIN_TOKEN_KEY)
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

adminApi.interceptors.response.use(
  response => {
    const data = response.data as ApiResponse<unknown>
    return data.code === 200 ? response : Promise.reject(new Error(data.message))
  },
  error => {
    if ((error.response?.status === 401 || error.response?.status === 403)
      && !String(error.config?.url || '').startsWith('/auth/')) {
      sessionStorage.removeItem(ADMIN_TOKEN_KEY)
      sessionStorage.removeItem(ADMIN_PROFILE_KEY)
      void router.push('/admin/login')
    }
    return Promise.reject(new Error(error.response?.data?.message || error.message || '请求失败'))
  }
)

export async function getAdminData<T>(url: string): Promise<T> {
  const response = await adminApi.get<ApiResponse<T>>(url)
  return response.data.data
}

export async function postAdminData<T>(url: string, data?: unknown): Promise<T> {
  const response = await adminApi.post<ApiResponse<T>>(url, data)
  return response.data.data
}

export async function putAdminData<T>(url: string, data?: unknown): Promise<T> {
  const response = await adminApi.put<ApiResponse<T>>(url, data)
  return response.data.data
}

export async function deleteAdminData<T>(url: string): Promise<T> {
  const response = await adminApi.delete<ApiResponse<T>>(url)
  return response.data.data
}
