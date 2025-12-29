import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

// 配置 Axios 实例，统一处理所有 HTTP 请求
// Axios 实例配置、请求/响应拦截器
const request = axios.create({
    baseURL: 'http://localhost:9095/api',
    timeout: 30000 // 增加到30秒
})

request.interceptors.request.use(
    config => {
        const userStore = useUserStore()
        // 对于注册、登录、重置密码等公开接口，不需要发送 token
        const publicPaths = ['/auth/login', '/auth/register', '/auth/reset-password']
        const isPublicPath = publicPaths.some(path => config.url?.includes(path))
        
        if (userStore.token && !isPublicPath) {
            config.headers['Authorization'] = `Bearer ${userStore.token}`
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
        } else if (error.code === 'ECONNABORTED' || error.message.includes('timeout')) {
            // 处理超时错误
            ElMessage.error('请求超时，请检查网络连接或稍后重试')
        } else if (error.request) {
            // 请求已发出但没有收到响应
            ElMessage.error('网络连接失败，请检查网络或服务器状态')
        } else {
            ElMessage.error(error.message || 'Network Error')
        }
        return Promise.reject(error)
    }
)

export default request
