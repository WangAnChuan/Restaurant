<template>
  <div class="account-page">
    <div class="page-header">
      <div>
        <h2>💰 财务管理</h2>
        <p class="subtitle">记录收入与支出明细</p>
      </div>
      <el-button type="primary" size="large" @click="openDialog">
        <el-icon><Plus /></el-icon>
        新增记录
      </el-button>
    </div>
    
    <el-card class="table-card">
      <el-table :data="tableData" stripe style="width: 100%">
        <el-table-column prop="transactionDate" label="日期" width="120" />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.type === 1 ? 'success' : 'danger'" effect="dark" size="small">
              {{ scope.row.type === 1 ? '收入' : '支出' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="categoryName" label="分类" />
        <el-table-column prop="paymentMethod" label="支付方式" />
        <el-table-column prop="amount" label="金额 (¥)">
          <template #default="scope">
            <span :class="scope.row.type === 1 ? 'amount-income' : 'amount-expense'">
              {{ scope.row.type === 1 ? '+' : '-' }} ¥{{ scope.row.amount }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" show-overflow-tooltip />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="scope">
            <el-popconfirm title="确定删除此记录？" @confirm="handleDel(scope.row.id)">
              <template #reference>
                <el-button size="small" type="danger" link>删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="tableData.length === 0" description="暂无收支记录" />
    </el-card>

    <!-- 居中弹窗代替侧边栏 -->
    <el-dialog v-model="dialogVisible" title="新增收支记录" width="500px" center>
      <el-form :model="form" label-width="80px" size="large">
        <el-form-item label="类型">
          <el-radio-group v-model="form.type" @change="loadCategories" style="width: 100%;">
            <el-radio-button :label="1" style="width: 50%;">
              💰 收入
            </el-radio-button>
            <el-radio-button :label="2" style="width: 50%;">
              📉 支出
            </el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="金额">
          <el-input-number v-model="form.amount" :precision="2" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker v-model="form.transactionDate" type="date" placeholder="选择日期" 
            style="width: 100%" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="支付方式" v-if="form.type === 1">
          <el-select v-model="form.paymentMethod" placeholder="请选择支付方式" style="width: 100%">
            <el-option label="微信" value="微信" />
            <el-option label="支付宝" value="支付宝" />
            <el-option label="现金" value="现金" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getAccountPage, addAccount, delAccount, getCategoryList } from '@/api/account'
import { ElMessage } from 'element-plus'

const tableData = ref([])
const dialogVisible = ref(false)
const categories = ref<any[]>([])

const form = reactive({
  type: 1,
  categoryId: null,
  categoryName: '',
  amount: 0,
  transactionDate: '',
  paymentMethod: '',
  remark: ''
})

const loadData = async () => {
  const res: any = await getAccountPage({ current: 1, size: 20 })
  tableData.value = res.records
}

const loadCategories = async () => {
  const res: any = await getCategoryList({ type: form.type })
  categories.value = res
}

const openDialog = () => {
  form.amount = 0
  form.remark = ''
  form.categoryId = null
  form.transactionDate = new Date().toISOString().split('T')[0]
  form.paymentMethod = ''
  dialogVisible.value = true
  loadCategories()
}

const submit = async () => {
  if (!form.categoryId) {
    ElMessage.warning('请选择分类')
    return
  }
  if (form.amount <= 0) {
    ElMessage.warning('请输入金额')
    return
  }
  const cat = categories.value.find(c => c.id === form.categoryId)
  if (cat) form.categoryName = cat.name
  
  await addAccount(form)
  ElMessage.success('保存成功')
  dialogVisible.value = false
  loadData()
}

const handleDel = async (id: number) => {
  await delAccount(id)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(() => loadData())
</script>

<style scoped>
.account-page {
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

.table-card {
  border-radius: 15px;
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.05);
}

.amount-income {
  font-weight: 600;
  color: #10b981;
}

.amount-expense {
  font-weight: 600;
  color: #ef4444;
}
</style>
