<template>
  <div class="purchase-page">
    <div class="page-header">
      <div>
        <h2>🛒 采购清单</h2>
        <p class="subtitle">记录每日采购物品</p>
      </div>
      <el-button type="primary" size="large" @click="dialogVisible = true">
        <el-icon><Plus /></el-icon>
        添加物品
      </el-button>
    </div>

    <el-card class="filter-card">
      <div class="filter-form">
        <span class="filter-label">📅 选择日期：</span>
        <el-date-picker 
          v-model="targetDate" 
          type="date" 
          value-format="YYYY-MM-DD" 
          @change="load"
          size="large"
          placeholder="选择日期"
        />
      </div>
    </el-card>

    <el-card class="table-card">
      <el-table :data="list" stripe style="width: 100%">
        <el-table-column type="index" label="序号" width="80" />
        <el-table-column prop="itemName" label="物品名称" min-width="150" />
        <el-table-column prop="quantity" label="数量" width="120" />
        <el-table-column prop="remark" label="备注" show-overflow-tooltip />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="scope">
            <el-popconfirm title="确定删除此物品？" @confirm="handleDel(scope.row.id)">
              <template #reference>
                <el-button link type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="list.length === 0" description="暂无采购记录" />
    </el-card>

    <el-dialog v-model="dialogVisible" title="添加采购物品" width="450px">
      <el-form :model="form" label-width="80px" size="large">
        <el-form-item label="物品名称">
          <el-input v-model="form.itemName" placeholder="请输入物品名称" />
        </el-form-item>
        <el-form-item label="数量">
          <el-input v-model="form.quantity" placeholder="如：5斤、2袋" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" :rows="3" />
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
import { ref, reactive, onMounted } from 'vue'
import { getPurchaseList, addPurchase, delPurchase } from '@/api/purchase'
import { ElMessage } from 'element-plus'

const targetDate = ref(new Date().toISOString().split('T')[0])
const list = ref([])
const dialogVisible = ref(false)
const form = reactive({ itemName: '', quantity: '', remark: '' })

const load = async () => {
  const res: any = await getPurchaseList({ date: targetDate.value })
  list.value = res
}

const submit = async () => {
  if (!form.itemName) {
    ElMessage.warning('请输入物品名称')
    return
  }
  await addPurchase({ ...form, targetDate: targetDate.value })
  ElMessage.success('添加成功')
  dialogVisible.value = false
  Object.assign(form, { itemName: '', quantity: '', remark: '' })
  load()
}

const handleDel = async (id: number) => {
  await delPurchase(id)
  ElMessage.success('删除成功')
  load()
}

onMounted(() => {
  const d = new Date()
  d.setDate(d.getDate() + 1)
  targetDate.value = d.toISOString().split('T')[0]
  load()
})
</script>

<style scoped>
.purchase-page {
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

.filter-card {
  margin-bottom: 20px;
  border-radius: 15px;
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.05);
}

.filter-form {
  display: flex;
  align-items: center;
}

.filter-label {
  font-size: 15px;
  color: #666;
  margin-right: 10px;
}

.table-card {
  border-radius: 15px;
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.05);
}
</style>
