<template>
  <div class="dish-page">
    <div class="page-header">
      <div>
        <h2>🍽️ 菜品管理</h2>
        <p class="subtitle">管理餐厅菜品信息</p>
      </div>
      <el-button type="primary" size="large" @click="openDialog()">
        <el-icon><Plus /></el-icon>
        添加菜品
      </el-button>
    </div>
    
    <el-row :gutter="20">
      <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="dish in list" :key="dish.id" style="margin-bottom: 20px;">
        <el-card class="dish-card" shadow="hover">
          <div class="dish-image" :style="{ backgroundImage: dish.imageUrl ? `url(${getImageUrl(dish.imageUrl)})` : 'none' }">
            <span v-if="!dish.imageUrl" class="dish-emoji">{{ getDishEmoji(dish.name) }}</span>
            <el-tag class="status-tag" :type="dish.status === 1 ? 'success' : 'info'" effect="dark" size="small">
              {{ dish.status === 1 ? '在售' : '停售' }}
            </el-tag>
          </div>
          <div class="dish-info">
            <h3 class="dish-name">{{ dish.name }}</h3>
            <p class="dish-ingredients">{{ dish.ingredients || '厨师秘制' }}</p>
            <div class="dish-footer">
              <span class="dish-price">¥ {{ dish.price }}</span>
              <div class="dish-actions">
                <el-button size="small" type="primary" text @click="openDialog(dish)">编辑</el-button>
                <el-popconfirm title="确定删除此菜品？" @confirm="handleDel(dish.id)">
                  <template #reference>
                    <el-button size="small" type="danger" text>删除</el-button>
                  </template>
                </el-popconfirm>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-empty v-if="list.length === 0" description="暂无菜品数据" />

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑菜品' : '添加菜品'" width="520px" center>
      <el-form :model="form" label-width="80px" size="large">
        <el-form-item label="菜品名称">
          <el-input v-model="form.name" placeholder="请输入菜品名称" />
        </el-form-item>
        <el-form-item label="菜品图片">
          <div class="upload-area">
            <el-upload
              class="image-uploader"
              :action="uploadUrl"
              :headers="uploadHeaders"
              :show-file-list="false"
              :on-success="handleUploadSuccess"
              :on-error="handleUploadError"
              :before-upload="beforeUpload"
              accept="image/*"
            >
              <div v-if="form.imageUrl" class="image-preview">
                <img :src="getImageUrl(form.imageUrl)" alt="菜品图片" />
                <div class="image-mask">
                  <span>点击更换</span>
                </div>
              </div>
              <div v-else class="upload-placeholder">
                <el-icon class="upload-icon"><Plus /></el-icon>
                <span>上传图片</span>
              </div>
            </el-upload>
            <el-button v-if="form.imageUrl" type="danger" text size="small" @click="form.imageUrl = ''">
              删除图片
            </el-button>
          </div>
        </el-form-item>
        <el-form-item label="价格 (¥)">
          <el-input-number v-model="form.price" :precision="2" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="配料">
          <el-input v-model="form.ingredients" type="textarea" placeholder="请输入配料说明" :rows="3" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" 
            active-text="在售" inactive-text="停售" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { getDishPage, addDish, updateDish, delDish } from '@/api/dish'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

interface Dish {
  id: number
  name: string
  price: number
  ingredients: string
  status: number
  imageUrl: string
}

const userStore = useUserStore()
const uploadUrl = 'http://localhost:9095/api/common/upload'
const uploadHeaders = computed(() => ({
  'Authorization': `Bearer ${userStore.token}`
}))
const list = ref<Dish[]>([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const form = reactive({ id: 0, name: '', price: 0, ingredients: '', status: 1, imageUrl: '' })

const dishEmojis: Record<string, string> = {
  '鱼': '🐟', '肉': '🍖', '鸡': '🍗', '牛': '🥩', '虾': '🦐', '蟹': '🦀',
  '面': '🍜', '饭': '🍚', '汤': '🍲', '菜': '🥬', '蛋': '🥚', '豆': '🫘',
  '酒': '🍺', '茶': '🍵', '果': '🍎', '甜': '🍰', '辣': '🌶️'
}

const getDishEmoji = (name: string) => {
  for (const key in dishEmojis) {
    if (name.includes(key)) return dishEmojis[key]
  }
  return '🍽️'
}

const getImageUrl = (url: string) => {
  if (!url) return ''
  // Vite proxy将自动转发 /uploads 请求到后端
  return url
}

const beforeUpload = (file: File) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5
  
  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB!')
    return false
  }
  return true
}

const handleUploadSuccess = (response: any) => {
  if (response.code === 200) {
    form.imageUrl = response.data.url
    ElMessage.success('上传成功')
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

const handleUploadError = (error: any) => {
  console.error('上传错误:', error)
  ElMessage.error('上传失败，请重试')
}

const load = async () => {
  const res: any = await getDishPage({ current: 1, size: 100 })
  list.value = res.records
}

const openDialog = (row?: any) => {
  if (row) {
    isEdit.value = true
    Object.assign(form, row)
  } else {
    isEdit.value = false
    Object.assign(form, { id: 0, name: '', price: 0, ingredients: '', status: 1, imageUrl: '' })
  }
  dialogVisible.value = true
}

const submit = async () => {
  if (!form.name) {
    ElMessage.warning('请输入菜品名称')
    return
  }
  try {
    if (isEdit.value) {
      await updateDish(form)
    } else {
      await addDish(form)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    load()
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败，请重试')
  }
}

const handleDel = async (id: number) => {
  await delDish(id)
  ElMessage.success('删除成功')
  load()
}

onMounted(() => load())
</script>

<style scoped>
.dish-page {
  padding: 10px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
}

.page-header h2 {
  margin: 0;
  font-size: 24px;
  color: #333;
}

.page-header .subtitle {
  margin: 5px 0 0;
  color: #888;
  font-size: 14px;
}

.dish-card {
  border-radius: 15px;
  overflow: hidden;
  transition: all 0.3s ease;
}

.dish-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.12);
}

.dish-image {
  height: 160px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  background-size: cover;
  background-position: center;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.dish-emoji {
  font-size: 60px;
}

.status-tag {
  position: absolute;
  top: 10px;
  right: 10px;
}

.dish-info {
  padding: 15px;
}

.dish-name {
  margin: 0 0 8px;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.dish-ingredients {
  margin: 0 0 12px;
  font-size: 12px;
  color: #888;
  height: 36px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.dish-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.dish-price {
  font-size: 20px;
  font-weight: 700;
  color: #E6A23C;
}

.dish-actions {
  display: flex;
  gap: 5px;
}

/* Upload styles */
.upload-area {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 10px;
}

.image-uploader {
  width: 150px;
  height: 150px;
}

.image-uploader :deep(.el-upload) {
  width: 100%;
  height: 100%;
  border: 2px dashed #d9d9d9;
  border-radius: 10px;
  cursor: pointer;
  overflow: hidden;
  transition: border-color 0.3s;
}

.image-uploader :deep(.el-upload:hover) {
  border-color: #667eea;
}

.upload-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #999;
  gap: 8px;
}

.upload-icon {
  font-size: 30px;
  color: #667eea;
}

.image-preview {
  width: 100%;
  height: 100%;
  position: relative;
}

.image-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-mask {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  opacity: 0;
  transition: opacity 0.3s;
}

.image-preview:hover .image-mask {
  opacity: 1;
}
</style>
