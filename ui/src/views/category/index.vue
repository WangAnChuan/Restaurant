<template>
  <div class="category-page">
    <div class="page-header">
      <div>
        <h2>📂 分类管理</h2>
        <p class="subtitle">管理收入与支出分类</p>
      </div>
    </div>
、
    <!--  表单  -->
    <el-card class="add-card">
      <div class="add-form">
        <el-input v-model="newCat" placeholder="输入新分类名称" size="large" style="width: 250px; margin-right: 15px;" />
        <el-select v-model="newType" placeholder="类型" size="large" style="width: 140px; margin-right: 15px;">
          <el-option label="💰 收入" :value="1" />
          <el-option label="📉 支出" :value="2" />
        </el-select>
        <el-button type="primary" size="large" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          添加分类
        </el-button>
      </div>
    </el-card>

    <!--  表格  -->
    <el-card class="list-card">
      <el-tabs v-model="activeTab" class="custom-tabs">
        <el-tab-pane name="1">
          <template #label>
            <span class="tab-label">💰 收入分类</span>
          </template>
        </el-tab-pane>
        <el-tab-pane name="2">
          <template #label>
            <span class="tab-label">📉 支出分类</span>
          </template>
        </el-tab-pane>
      </el-tabs>
      
      <div class="search-bar">
        <el-input v-model="searchName" placeholder="搜索分类名称" style="width: 200px; margin-right: 10px;" clearable @clear="load" @keyup.enter="load">
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" @click="load">搜索</el-button>
      </div>

      <el-table :data="list" stripe style="width: 100%">
        <el-table-column type="index" label="序号" width="80" />
        <el-table-column prop="name" label="分类名称" />
        <el-table-column prop="createTime" label="最后更新时间" width="180">
          <template #default="scope">
            {{ formatTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button link type="primary" @click="handleEdit(scope.row)">编辑</el-button>
            <el-popconfirm title="确定删除此分类？" @confirm="handleDel(scope.row.id)">
              <template #reference>
                <el-button link type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="list.length === 0" description="暂无分类数据" />
    </el-card>

    <!-- 编辑弹窗 -->
    <el-dialog v-model="dialogVisible" title="编辑分类" width="400px">
      <el-form :model="editForm">
        <el-form-item label="分类名称" label-width="80px">
          <el-input v-model="editForm.name" />
        </el-form-item>
        <el-form-item label="分类类型" label-width="80px">
          <el-radio-group v-model="editForm.type">
            <el-radio :label="1">💰 收入</el-radio>
            <el-radio :label="2">📉 支出</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { getCategoryList, addCategory, updateCategory, delCategory } from '@/api/account'
import { ElMessage } from 'element-plus'

const activeTab = ref('1')
const list = ref([])
const newCat = ref('')
const newType = ref(1)
const searchName = ref('')

const dialogVisible = ref(false)
const editForm = ref({
  id: 0,
  name: '',
  type: 1
})

// 监听activeTab变化，同步更新newType并刷新列表
watch(activeTab, (newVal) => {
  newType.value = Number(newVal)
  load()  // 切换tab时重新加载对应类型的分类列表
})

// 加载分类列表
// 注意：activeTab是字符串类型('1' 或 '2')，需要转换为数字传给后端
const load = async () => {
  const res: any = await getCategoryList({ 
    type: Number(activeTab.value),
    name: searchName.value
  })
  list.value = res
}

const handleAdd = async () => {
  if (!newCat.value) {
    ElMessage.warning('请输入分类名称')
    return
  }
  await addCategory({ name: newCat.value, type: newType.value })
  ElMessage.success('添加成功')
  newCat.value = ''
  // 切换到新添加分类的tab页并刷新
  activeTab.value = newType.value.toString()
  load()
}

const handleDel = async (id: number) => {
  await delCategory(id)
  ElMessage.success('删除成功')
  load()
}

const handleEdit = (row: any) => {
  editForm.value = { ...row }
  dialogVisible.value = true
}

const submitEdit = async () => {
    if (!editForm.value.name) {
      ElMessage.warning('请输入分类名称')
      return
    }
    await updateCategory(editForm.value)
    ElMessage.success('修改成功')
    dialogVisible.value = false
    load()
}

const formatTime = (time: string) => {
  if (!time) return ''
  return time.replace('T', ' ')
}

onMounted(() => load())
</script>

<style scoped>
.category-page {
  padding: 10px;
}

.page-header {
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

.add-card {
  margin-bottom: 20px;
  border-radius: 15px;
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.05);
}

.add-form {
  display: flex;
  align-items: center;
}

.list-card {
  border-radius: 15px;
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.05);
}

.tab-label {
  font-size: 15px;
}

.custom-tabs :deep(.el-tabs__item.is-active) {
  font-weight: 600;
}

.search-bar {
  padding: 10px 0;
  display: flex;
  justify-content: flex-end; /* Align to right */
}
</style>
