<template>
  <div class="category-page">
    <div class="page-header">
      <div>
        <h2>📂 分类管理</h2>
        <p class="subtitle">管理收入与支出分类</p>
      </div>
    </div>

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
    
    <el-card class="list-card">
      <el-tabs v-model="activeTab" @tab-click="load" class="custom-tabs">
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

      <el-table :data="list" stripe style="width: 100%">
        <el-table-column type="index" label="序号" width="80" />
        <el-table-column prop="name" label="分类名称" />
        <el-table-column label="操作" width="150">
          <template #default="scope">
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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getCategoryList, addCategory, delCategory } from '@/api/account'
import { ElMessage } from 'element-plus'

const activeTab = ref('2')
const list = ref([])
const newCat = ref('')
const newType = ref(2)

const load = async () => {
  const res: any = await getCategoryList({ type: activeTab.value })
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
  if (newType.value.toString() === activeTab.value) load()
}

const handleDel = async (id: number) => {
  await delCategory(id)
  ElMessage.success('删除成功')
  load()
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
</style>
