<template>
  <div class="dish-page">
    <div class="page-header">
      <div>
        <h2>🍽️ 菜品管理</h2>
        <p class="subtitle">管理餐厅菜品信息</p >
      </div>
      <el-button type="primary" size="large" @click="openDialog()">
        <el-icon><Plus /></el-icon>
        添加菜品
      </el-button>
    </div>
    
    <div class="category-filter">
      <el-tabs v-model="activeCategory" class="custom-tabs">
        <el-tab-pane label="全部" name="" />
        <el-tab-pane v-for="cat in categoryList" :key="cat.id" :label="getCategoryName(cat.name)" :name="String(cat.id)" />
      </el-tabs>
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
            <p class="dish-desc" :title="dish.description">{{ dish.description || '暂无描述' }}</p>
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
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="80px" size="large">
        <el-form-item label="菜品名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入菜品名称" />
        </el-form-item>
        <el-form-item label="菜品分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择菜品分类" style="width: 100%">
            <el-option v-for="item in categoryList" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
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
                < img :src="getImageUrl(form.imageUrl)" alt="菜品图片" />
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
        <el-form-item label="价格 (¥)" prop="price">
          <el-input-number v-model="form.price" :precision="2" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="配料">
          <el-input v-model="form.ingredients" type="textarea" placeholder="请输入配料说明" :rows="2" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" placeholder="请输入菜品详细描述" :rows="3" />
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
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { getDishPage, addDish, updateDish, delDish, getDishCategoryList } from '@/api/dish'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

interface Dish {
  id: number
  name: string
  price: number
  ingredients: string
  status: number

  imageUrl: string
  categoryId?: number
  description?: string
}

const userStore = useUserStore()
const uploadUrl = 'http://localhost:9095/api/common/upload'
const uploadHeaders = computed(() => ({
  'Authorization': `Bearer ${userStore.token}`
}))
const list = ref<Dish[]>([])
const categoryList = ref<any[]>([]) 
const activeCategory = ref('') // Current active tab
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()
const form = reactive({ 
  id: 0, 
  name: '', 
  categoryId: undefined as number | undefined, 
  price: 0, 
  ingredients: '', 
  description: '', // Add description
  status: 1, 
  imageUrl: '' 
})

// 表单验证规则
const formRules = {
  name: [
    { required: true, message: '请输入菜品名称', trigger: 'blur' },
    { min: 1, max: 50, message: '菜品名称长度在1到50个字符', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择菜品分类', trigger: 'change' }
  ],
  price: [
    { required: true, message: '请输入价格', trigger: 'blur' },
    { type: 'number', min: 0.01, message: '价格必须大于0', trigger: 'blur' }
  ]
}

const dishEmojis: Record<string, string> = {
  '鱼': '🐟', '肉': '🍖', '鸡': '🍗', '牛': '🥩', '虾': '🦐', '蟹': '🦀',
  '面': '🍜', '饭': '🍚', '汤': '🍲', '菜': '🥬', '蛋': '🥚', '豆': '🫘',
  '酒': '🍺', '茶': '🍵', '果': '🍎', '甜': '🍰', '辣': '🌶️'
}

// Data Mapping for Localization
const categoryNameMap: Record<string, string> = {
  'Hot Dishes': '热菜',
  'Cold Dishes': '凉菜',
  'Soup': '汤品',
  'Beverages': '饮料',
  'Main Course': '主食'
}

const getCategoryName = (name: string) => categoryNameMap[name] || name

const getDishEmoji = (name: string) => {
  for (const key in dishEmojis) {
    if (name.includes(key)) return dishEmojis[key]
  }
  return '🍽️'
}

// 获取图片URL
// 注意：Vite已配置proxy，会自动将/uploads请求转发到后端
const getImageUrl = (url: string) => {
  if (!url) return ''
  // 如果已经是完整URL（http/https开头），直接返回
  if (url.startsWith('http')) return url
  // 否则直接返回相对路径，让Vite proxy处理
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
  const [res, catRes]: any = await Promise.all([
    getDishPage({ 
      current: 1, 
      size: 100,
      categoryId: activeCategory.value ? Number(activeCategory.value) : undefined
    }),
    getDishCategoryList() 
  ])
  list.value = res.records
  categoryList.value = catRes
}

const openDialog = (row?: any) => {
  if (row) {
    isEdit.value = true
    Object.assign(form, row)
  } else {
    isEdit.value = false
    Object.assign(form, { 
      id: 0, 
      name: '', 
      categoryId: activeCategory.value ? Number(activeCategory.value) : undefined, // Pre-select current tab category
      price: 0, 
      ingredients: '', 
      description: '', 
      status: 1, 
      imageUrl: '' 
    })
  }
  dialogVisible.value = true

  // 清除之前的验证错误
  setTimeout(() => {
    formRef.value?.clearValidate()
  }, 0)
}

const submit = async () => {
  // 使用表单验证
  if (!formRef.value) return

  try {
    await formRef.value.validate()

    // 验证通过，提交数据
    if (isEdit.value) {
      await updateDish(form)
    } else {
      await addDish(form)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    load()
  } catch (error: any) {
    // 验证失败或保存失败
    if (error?.message) {
      console.error('保存失败:', error)
      ElMessage.error('保存失败，请重试')
    }
  }
}

const handleDel = async (id: number) => {
  await delDish(id)
  ElMessage.success('删除成功')
  load()
}

// Watch activeCategory to reload list automatically
watch(activeCategory, () => {
    getDishPage({ 
      current: 1, 
      size: 100,
      categoryId: activeCategory.value ? Number(activeCategory.value) : undefined
    }).then((res: any) => {
      list.value = res.records
    })
})



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

.category-filter {
  margin-bottom: 25px;
  background: #f5f7fa;
  padding: 5px 5px 0;
  border-radius: 8px;
}

.custom-tabs :deep(.el-tabs__header) {
  margin: 0;
}

.custom-tabs :deep(.el-tabs__nav-wrap::after) {
  display: none; /* Remove bottom line */
}

.custom-tabs :deep(.el-tabs__item) {
  font-size: 15px;
  font-weight: 500;
  padding: 0 25px;
  color: #606266;
}

.custom-tabs :deep(.el-tabs__item.is-active) {
  color: #409EFF;
  font-weight: 600;
}

.custom-tabs :deep(.el-tabs__active-bar) {
  height: 3px;
  border-radius: 2px;
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

.dish-desc {
  font-size: 13px;
  color: #666;
  margin: 0 0 8px;
  line-height: 1.4;
  height: 36px; /* 2 lines */
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
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