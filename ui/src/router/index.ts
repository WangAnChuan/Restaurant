import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import MainLayout from '@/layout/MainLayout.vue'

const router = createRouter({
    history: createWebHistory(),
    routes: [
        {
            path: '/login',
            component: () => import('@/views/login/index.vue')
        },
        {
            path: '/',
            component: MainLayout,
            redirect: '/login', // Redirect to login initially
            children: [
                {
                    path: 'dashboard',
                    component: () => import('@/views/dashboard/index.vue'),
                    meta: { roles: ['ROLE_BOSS'] }
                },
                {
                    path: 'account',
                    component: () => import('@/views/account/index.vue'),
                    meta: { roles: ['ROLE_BOSS'] }
                },
                {
                    path: 'category',
                    component: () => import('@/views/category/index.vue'),
                    meta: { roles: ['ROLE_BOSS'] }
                },
                {
                    path: 'dish',
                    component: () => import('@/views/dish/index.vue'),
                    meta: { roles: ['ROLE_BOSS'] }
                },
                {
                    path: 'purchase',
                    component: () => import('@/views/purchase/index.vue'),
                    meta: { roles: ['ROLE_BOSS', 'ROLE_PURCHASER'] }
                },
                {
                    path: 'menu',
                    component: () => import('@/views/menu/index.vue'),
                    meta: { roles: ['ROLE_VISITOR'] }
                },
                {
                    path: 'reservation',
                    component: () => import('@/views/reservation/index.vue'),
                    meta: { roles: ['ROLE_VISITOR'] }
                },
                {
                    path: 'reservation-manage',
                    component: () => import('@/views/reservation/manage.vue'),
                    meta: { roles: ['ROLE_BOSS'] }
                }
            ]
        }
    ]
})

router.beforeEach(async (to, _from, next) => {
    const userStore = useUserStore()
    const token = userStore.token

    if (to.path === '/login') {
        next()
    } else {
        if (!token) {
            next('/login')
        } else {
            if (userStore.roles.length === 0) {
                await userStore.getInfoAction()
            }
            const roles = userStore.roles
            if (to.meta.roles && !(to.meta.roles as string[]).some(r => roles.includes(r))) {
                // Simple redirect logic based on Role
                if (roles.includes('ROLE_VISITOR')) next('/menu')
                else if (roles.includes('ROLE_PURCHASER')) next('/purchase')
                else next('/dashboard') // Fallback/Error
            } else {
                next()
            }
        }
    }
})

export default router
