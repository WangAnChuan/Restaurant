import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

// 配置 Axios 实例，统一处理所有 HTTP 请求
// Axios 实例配置、请求/响应拦截器
const request = axios.create({
    baseURL: 'http://localhost:9095/api',
    timeout: 5000
})

request.interceptors.request.use(
    config => {
        // 公开接口不需要token
        const publicPaths = ['/auth/login', '/auth/register', '/auth/reset-password']
        const isPublicPath = config.url ? publicPaths.some(path => config.url!.includes(path)) : false
        
        if (!isPublicPath) {
            const userStore = useUserStore()
            if (userStore.token) {
                config.headers['Authorization'] = `Bearer ${userStore.token}`
            }
        }
        return config
    },
    error => {
        return Promise.reject(error)
    }
)

request.interceptors.response.use(
    response => {
        const res = response.data
        if (res.code === 200) {
            return res.data
        } else {
            ElMessage.error(res.message || 'Error')
            return Promise.reject(new Error(res.message || 'Error'))
        }
    },
    error => {
        // 处理HTTP错误状态码
        if (error.response) {
            const status = error.response.status

            if (status === 401) {
                // 未授权：清除用户信息并跳转到登录页
                ElMessage.error('登录已过期，请重新登录')
                const userStore = useUserStore()
                userStore.logout()

                // 跳转到登录页
                if (window.location.pathname !== '/login') {
                    window.location.href = '/login'
                }
            } else if (status === 403) {
                ElMessage.error('没有权限访问该资源')
            } else if (status === 404) {
                ElMessage.error('请求的资源不存在')
            } else if (status === 500) {
                ElMessage.error('服务器错误，请稍后重试')
            } else {
                ElMessage.error(error.response.data?.message || error.message || 'Network Error')
            }
        } else {
            ElMessage.error(error.message || 'Network Error')
        }
        return Promise.reject(error)
    }
)

export default request
