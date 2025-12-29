<template>
  <div class="login-wrapper">
    <!-- Left Side: Brand & Visuals -->
    <div class="left-panel">
      <div class="bg-overlay"></div>
      <div class="brand-content">
        <h1 class="brand-name">友家兄弟<br>土菜馆</h1>
        <div class="brand-divider"></div>

      </div>
    </div>

    <!-- Right Side: Forgot Password Form -->
    <div class="right-panel">
      <div class="login-card-clean">
        <div class="form-header">
          <h2 class="form-title">忘记密码</h2>
          <p class="form-subtitle">Reset your password</p>
        </div>

        <el-form :model="form" class="clean-form" size="large" @keyup.enter="handleSubmit">
          <div class="form-group">
            <label class="form-label">账号 / Account</label>
            <el-input 
              v-model="form.username" 
              placeholder="请输入账号" 
              class="clean-input"
            />
          </div>
          
          <div class="form-group">
            <label class="form-label">新密码 / New Password</label>
            <el-input 
              v-model="form.password" 
              type="password" 
              placeholder="请输入新密码" 
              show-password 
              class="clean-input"
            />
          </div>

          <div class="form-group">
            <label class="form-label">确认密码 / Confirm Password</label>
            <el-input 
              v-model="form.confirmPassword" 
              type="password" 
              placeholder="请再次输入新密码" 
              show-password 
              class="clean-input"
            />
          </div>

          <button class="submit-btn" @click.prevent="handleSubmit" :disabled="loading">
            <span v-if="!loading">修改密码</span>
            <span v-else>处理中...</span>
          </button>

          <div class="toggle-box">
            <a href="#" class="toggle-link" @click.prevent="goToLogin">
              返回登录
            </a>
          </div>
        </el-form>
      </div>
      
      <div class="copyright">
        © 2024 餐饮管理系统 | Powered by Vue3 + Element Plus
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { resetPassword } from '@/api/auth'
import { ElMessage } from 'element-plus'

const router = useRouter()
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
  confirmPassword: ''
})

const handleSubmit = async () => {
  if (!form.username || !form.password || !form.confirmPassword) {
    ElMessage.warning('请填写完整信息')
    return
  }

  if (form.password !== form.confirmPassword) {
    ElMessage.warning('两次输入的密码不一致')
    return
  }

  if (form.password.length < 6) {
    ElMessage.warning('密码长度至少为6位')
    return
  }

  loading.value = true
  try {
    await resetPassword({
      username: form.username,
      password: form.password
    })
    ElMessage.success('密码修改成功，请使用新密码登录')
    router.push('/login')
  } catch (e: any) {
    ElMessage.error(e.message || '密码修改失败')
  } finally {
    loading.value = false
  }
}

const goToLogin = () => {
  router.push('/login')
}
</script>

<style scoped>
.login-wrapper {
  display: flex;
  min-height: 100vh;
  width: 100vw;
  overflow: hidden;
  font-family: 'Inter', 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', Arial, sans-serif;
}

/* Left Panel */
.left-panel {
  flex: 0 0 45%;
  position: relative;
  background: url('@/assets/login-bg.png') no-repeat center center;
  background-size: cover;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.bg-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(16, 24, 60, 0.85) 0%, rgba(30, 41, 59, 0.95) 100%);
  z-index: 1;
}

.brand-content {
  position: relative;
  z-index: 2;
  text-align: center;
  padding: 40px;
}

.brand-name {
  font-family: 'Ma Shan Zheng', 'STKaiti', serif;
  font-size: 64px;
  line-height: 1.2;
  margin: 0 0 20px;
  background: linear-gradient(to right, #fff, #e2e8f0);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  text-shadow: 0 4px 12px rgba(0,0,0,0.3);
}

.brand-divider {
  width: 60px;
  height: 4px;
  background: #e64a19;
  margin: 0 auto 20px;
  border-radius: 2px;
}

.brand-slogan {
  font-size: 24px;
  font-weight: 300;
  margin: 0 0 10px;
  letter-spacing: 2px;
}

.brand-eng {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.6);
  text-transform: uppercase;
  letter-spacing: 1px;
}

/* Right Panel */
.right-panel {
  flex: 1;
  background: #f8fafc;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  position: relative;
}

.login-card-clean {
  width: 100%;
  max-width: 440px;
  padding: 40px;
  background: transparent;
}

.form-header {
  margin-bottom: 40px;
}

.form-title {
  font-size: 32px;
  font-weight: 700;
  color: #0f172a;
  margin: 0 0 10px;
}

.form-subtitle {
  font-size: 16px;
  color: #64748b;
  margin: 0;
}

.clean-form {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-label {
  font-size: 14px;
  font-weight: 500;
  color: #334155;
  margin-left: 2px;
}

.clean-input :deep(.el-input__wrapper) {
  padding: 0 16px;
  box-shadow: 0 0 0 1px #cbd5e1 inset;
  border-radius: 12px;
  background: #fff;
  transition: all 0.2s;
  height: 42px;
  display: flex;
  align-items: center;
}

.clean-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px #e64a19 inset !important;
}

/* Override browser autofill blue background */
.clean-input :deep(input:-webkit-autofill),
.clean-input :deep(input:-webkit-autofill:hover), 
.clean-input :deep(input:-webkit-autofill:focus), 
.clean-input :deep(input:-webkit-autofill:active) {
  -webkit-box-shadow: 0 0 0 1000px white inset !important;
  -webkit-text-fill-color: #333 !important;
  transition: background-color 5000s ease-in-out 0s;
}


.submit-btn {
  height: 42px;
  margin-top: 10px;
  background: #e64a19;
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 6px -1px rgba(230, 74, 25, 0.3);
}

.submit-btn:hover {
  background: #d84315;
  transform: translateY(-1px);
  box-shadow: 0 8px 12px -2px rgba(230, 74, 25, 0.4);
}

.submit-btn:disabled {
  background: #cbd5e1;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.toggle-box {
  text-align: center;
  font-size: 14px;
  color: #64748b;
  margin-top: 10px;
}

.toggle-link {
  color: #e64a19;
  font-weight: 600;
  text-decoration: none;
  cursor: pointer;
}

.toggle-link:hover {
  text-decoration: underline;
}

.copyright {
  position: absolute;
  bottom: 20px;
  font-size: 12px;
  color: #94a3b8;
}

@media (max-width: 900px) {
  .left-panel {
    display: none;
  }
  .right-panel {
    flex: 1;
    background: #fff;
  }
}
</style>

