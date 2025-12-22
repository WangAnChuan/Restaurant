import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login, getInfo } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
    const token = ref(localStorage.getItem('token') || '')
    const roles = ref<string[]>([])
    const username = ref('')

    const loginAction = async (userInfo: any) => {
        const res: any = await login(userInfo)
        token.value = res.token
        localStorage.setItem('token', res.token)
    }

    const getInfoAction = async () => {
        const res: any = await getInfo()
        username.value = res.sysUser.username
        roles.value = res.permissions // e.g., ["ROLE_BOSS"]
        return res
    }

    const logout = () => {
        token.value = ''
        roles.value = []
        localStorage.removeItem('token')
    }

    return { token, roles, username, loginAction, getInfoAction, logout }
})
