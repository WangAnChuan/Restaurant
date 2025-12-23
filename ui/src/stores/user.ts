import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login, getInfo } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
    // 从localStorage恢复用户信息
    const token = ref(localStorage.getItem('token') || '')
    const roles = ref<string[]>(JSON.parse(localStorage.getItem('roles') || '[]'))
    const username = ref(localStorage.getItem('username') || '')

    const loginAction = async (userInfo: any) => {
        const res: any = await login(userInfo)
        token.value = res.token
        localStorage.setItem('token', res.token)
    }

    const getInfoAction = async () => {
        const res: any = await getInfo()
        username.value = res.sysUser.username
        roles.value = res.permissions // e.g., ["ROLE_BOSS"]

        // 持久化用户信息
        localStorage.setItem('username', username.value)
        localStorage.setItem('roles', JSON.stringify(roles.value))

        return res
    }

    const logout = () => {
        token.value = ''
        roles.value = []
        username.value = ''

        // 清除所有持久化数据
        localStorage.removeItem('token')
        localStorage.removeItem('username')
        localStorage.removeItem('roles')
    }

    return { token, roles, username, loginAction, getInfoAction, logout }
})
