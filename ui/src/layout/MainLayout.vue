<template>
  <div class="common-layout">
    <el-container>
      <el-aside width="260px" class="sidebar">
        <div class="logo-section">
          <div class="logo-circle">
            <span class="logo-icon">R</span>
          </div>
          <div class="logo-text">
            <span class="brand-name">RestManage</span>
            <span class="brand-tag">餐饮管理系统</span>
          </div>
        </div>
        
        <el-menu
          :default-active="$route.path"
          class="side-menu"
          background-color="transparent"
          text-color="#9ca3af"
          active-text-color="#ffffff"
          router
          :border="false"
        >
          <div class="menu-section-label" v-if="hasRole('Boss')">概览</div>
          <template v-if="hasRole('Boss')">
            <el-menu-item index="/dashboard">
              <el-icon><Odometer /></el-icon>
              <span>数据看板</span>
            </el-menu-item>
          </template>

          <div class="menu-section-label" v-if="hasRole('Boss')">管理</div>
          <template v-if="hasRole('Boss')">
            <el-menu-item index="/account">
              <el-icon><Money /></el-icon>
              <span>财务管理</span>
            </el-menu-item>
            <el-menu-item index="/category">
              <el-icon><Files /></el-icon>
              <span>分类管理</span>
            </el-menu-item>
            <el-menu-item index="/dish">
              <el-icon><Food /></el-icon>
              <span>菜品管理</span>
            </el-menu-item>
            <el-menu-item index="/reservation-manage">
              <el-icon><Calendar /></el-icon>
              <span>预定管理</span>
            </el-menu-item>
          </template>

          <div class="menu-section-label" v-if="hasRole('Purchaser') || hasRole('Boss')">运营</div>
          <template v-if="hasRole('Purchaser') || hasRole('Boss')">
            <el-menu-item index="/purchase">
              <el-icon><ShoppingCart /></el-icon>
              <span>采购清单</span>
            </el-menu-item>
          </template>

          <div class="menu-section-label" v-if="hasRole('Visitor')">前台</div>
          <template v-if="hasRole('Visitor')">
            <el-menu-item index="/menu">
              <el-icon><Menu /></el-icon>
              <span>今日菜单</span>
            </el-menu-item>
            <el-menu-item index="/reservation">
              <el-icon><Calendar /></el-icon>
              <span>在线预定</span>
            </el-menu-item>
          </template>
        </el-menu>

        <div class="sidebar-footer">
          <div class="footer-item">
            <el-icon><Setting /></el-icon>
            <span>设置</span>
          </div>
          <div class="footer-item">
             <el-icon><QuestionFilled /></el-icon>
             <span>帮助</span>
          </div>
        </div>
      </el-aside>
      
      <el-container class="main-container">
        <el-header class="main-header">
          <div class="header-left">
            <h2 class="page-current-title">{{ currentRouteName }}</h2>
          </div>
          <div class="header-right">
            <div class="action-icons">
              <button class="icon-btn"><el-icon><Bell /></el-icon></button>
              <button class="icon-btn"><el-icon><Search /></el-icon></button>
            </div>
            <div class="divider-vertical"></div>
            <el-dropdown trigger="click" placement="bottom-end">
              <div class="user-profile">
                <el-avatar :size="32" class="avatar-img" src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" />
                <div class="user-info-text">
                  <span class="username">{{ userStore.username }}</span>
                  <span class="role-badge">{{ userRoleText }}</span>
                </div>
                <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu class="user-dropdown">
                  <el-dropdown-item>
                     <el-icon><User /></el-icon> 个人中心
                  </el-dropdown-item>
                  <el-dropdown-item divided @click="handleLogout">
                    <el-icon><SwitchButton /></el-icon>
                    <span class="text-danger">退出登录</span>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>
        <el-main class="main-content">
          <router-view v-slot="{ Component }">
             <transition name="fade" mode="out-in">
                <component :is="Component" />
             </transition>
          </router-view>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { useUserStore } from '@/stores/user'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { computed } from 'vue'

const userStore = useUserStore()
const router = useRouter()
const route = useRoute()

const hasRole = (role: string) => {
  return userStore.roles.includes('ROLE_' + role.toUpperCase())
}

const userRoleText = computed(() => {
  if (hasRole('Boss')) return '管理员'
  if (hasRole('Purchaser')) return '采购'
  return '访客'
})

const currentRouteName = computed(() => {
  const nameMap: Record<string, string> = {
    '/dashboard': '数据看板',
    '/account': '财务管理',
    '/category': '分类管理',
    '/dish': '菜品管理',
    '/purchase': '采购清单',
    '/menu': '今日菜单',
    '/reservation': '在线预定',
    '/reservation-manage': '预定管理'
  }
  return nameMap[route.path] || '餐饮管理系统'
})

const handleLogout = () => {
  userStore.logout()
  ElMessage.success('已退出登录')
  router.push('/login')
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap');

.common-layout, .el-container {
  height: 100%;
  font-family: 'Inter', system-ui, sans-serif;
}

/* Sidebar Styling */
.sidebar {
  background-color: #111827; /* Dark slate */
  display: flex;
  flex-direction: column;
  border-right: 1px solid #1f2937;
  transition: width 0.3s ease;
  z-index: 10;
}

.logo-section {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 32px 24px;
  margin-bottom: 20px;
}

.logo-circle {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 6px -1px rgba(59, 130, 246, 0.5);
}

.logo-icon {
  color: white;
  font-weight: 700;
  font-size: 20px;
}

.logo-text {
  display: flex;
  flex-direction: column;
}

.brand-name {
  color: white;
  font-weight: 700;
  font-size: 18px;
  letter-spacing: -0.5px;
}

.brand-tag {
  color: #6b7280;
  font-size: 11px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.side-menu {
  flex: 1;
  padding: 0 16px;
  border-right: none !important;
}

.menu-section-label {
  padding: 24px 12px 10px 12px;
  color: #4b5563;
  font-size: 11px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.side-menu :deep(.el-menu-item) {
  margin: 4px 0;
  border-radius: 8px;
  height: 48px;
  line-height: 48px;
  font-weight: 500;
  color: #9ca3af;
  border-left: 3px solid transparent;
}

.side-menu :deep(.el-menu-item:hover) {
  background-color: #1f2937 !important;
  color: #f3f4f6 !important;
}

.side-menu :deep(.el-menu-item.is-active) {
  background-color: rgba(59, 130, 246, 0.1) !important;
  color: #60a5fa !important;
  border-left-color: #3b82f6;
  font-weight: 600;
}

.side-menu :deep(.el-menu-item .el-icon) {
  font-size: 18px;
  margin-right: 12px;
}

.sidebar-footer {
  padding: 24px;
  border-top: 1px solid #1f2937;
  display: flex;
  justify-content: space-around;
}

.footer-item {
  color: #6b7280;
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  padding: 8px;
  border-radius: 6px;
  transition: all 0.2s;
}

.footer-item:hover {
  background-color: #1f2937;
  color: #9ca3af;
}

/* Header Styling */
.main-header {
  background: white;
  border-bottom: 1px solid #f3f4f6;
  height: 72px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 40px;
}

.page-current-title {
  font-size: 18px;
  font-weight: 600;
  color: #111827;
  margin: 0;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.action-icons {
  display: flex;
  gap: 8px;
}

.icon-btn {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: 1px solid #e5e7eb;
  background: white;
  color: #6b7280;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
}

.icon-btn:hover {
  background: #f9fafb;
  color: #111827;
  border-color: #d1d5db;
}

.divider-vertical {
  width: 1px;
  height: 24px;
  background-color: #e5e7eb;
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 20px;
  transition: background 0.2s;
}

.user-profile:hover {
  background-color: #f3f4f6;
}

.avatar-img {
  border: 2px solid white;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.user-info-text {
  display: flex;
  flex-direction: column;
  line-height: 1.2;
}

.username {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
}

.role-badge {
  font-size: 11px;
  color: #6b7280;
}

.dropdown-icon {
  margin-left: 4px;
  color: #9ca3af;
  font-size: 12px;
}

.text-danger {
  color: #ef4444;
}

/* Main Content */
.main-content {
  background-color: #f8f9fa;
  padding: 0;
}

/* Transitions */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
