import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const request = axios.create({
    baseURL: 'http://localhost:9095/api',
    timeout: 5000
})

request.interceptors.request.use(
    config => {
        const userStore = useUserStore()
        if (userStore.token) {
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
        ElMessage.error(error.message || 'Network Error')
        return Promise.reject(error)
    }
)

export default request
