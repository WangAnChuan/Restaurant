<template>
  <div class="account-page">
    <!-- 页面头部 -->
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

    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchParams" class="demo-form-inline">
        <el-form-item label="类型">
          <el-select v-model="searchParams.type" placeholder="全部类型" clearable style="width: 120px" @change="handleSearchTypeChange">
            <el-option label="💰 收入" :value="1" />
            <el-option label="📉 支出" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="searchParams.categoryId" placeholder="全部分类" clearable style="width: 140px">
            <el-option v-for="c in searchCategories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="支付方式">
          <el-select v-model="searchParams.paymentMethod" placeholder="全部支付方式" clearable style="width: 140px">
            <el-option label="微信" value="微信" />
            <el-option label="支付宝" value="支付宝" />
            <el-option label="现金" value="现金" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card class="table-card">
      <el-table :data="tableData" stripe style="width: 100%">
        <el-table-column prop="transactionDate" label="日期" width="120">
          <template #default="scope">
            {{ formatTime(scope.row.transactionDate) }}
          </template>
        </el-table-column>
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
        <el-table-column prop="createTime" label="创建时间" width="170">
          <template #default="scope">
            {{ formatTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" width="170">
         <template #default="scope">
            {{ formatTime(scope.row.updateTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-button size="small" type="primary" link @click="handleEdit(scope.row)">编辑</el-button>
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


    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑收支记录' : '新增收支记录'" width="500px" center>
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
        <el-form-item label="支付方式">
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
import { getAccountPage, addAccount, updateAccount, delAccount, getCategoryList } from '@/api/account'
import { ElMessage } from 'element-plus'

// 响应式数据
const tableData = ref([])           // 表格数据
const dialogVisible = ref(false)    // 弹窗显示状态
const categories = ref<any[]>([])   // 分类列表
const searchCategories = ref<any[]>([]) // 搜索用的分类列表

// 搜索参数
const searchParams = reactive({
  type: undefined,
  categoryId: undefined,
  paymentMethod: undefined
})

// 表单数据
const form = reactive({
  id: undefined,          // ID
  type: 1,                // 类型：1=收入，2=支出
  categoryId: null,       // 分类ID
  categoryName: '',       // 分类名称
  amount: 0,              // 金额
  transactionDate: '',    // 交易日期
  paymentMethod: '',      // 支付方式
  remark: ''              // 备注
})

// 加载账目记录列表
const loadData = async () => {
  const params = {
    current: 1, 
    size: 20,
    ...searchParams
  }
  const res: any = await getAccountPage(params)
  tableData.value = res.records
}

const resetSearch = () => {
  searchParams.type = undefined
  searchParams.categoryId = undefined
  searchParams.paymentMethod = undefined
  loadData()
}

// 加载分类列表（根据收入/支出类型）
const loadCategories = async () => {
  const res: any = await getCategoryList({ type: form.type })
  categories.value = res
}

// 加载搜索用的分类列表
const loadSearchCategories = async () => {
  const res: any = await getCategoryList({ type: searchParams.type })
  searchCategories.value = res
}

const handleSearchTypeChange = () => {
  searchParams.categoryId = undefined
  loadSearchCategories()
}

// 打开新增弹窗
const openDialog = () => {
  form.id = undefined
  form.amount = 0
  form.remark = ''
  form.categoryId = null
  form.transactionDate = new Date().toISOString().split('T')[0]
  form.paymentMethod = ''
  dialogVisible.value = true
  loadCategories()
}

const handleEdit = (row: any) => {
  form.id = row.id
  form.type = row.type
  form.categoryId = row.categoryId
  form.categoryName = row.categoryName
  form.amount = row.amount
  form.transactionDate = row.transactionDate
  form.paymentMethod = row.paymentMethod
  form.remark = row.remark
  dialogVisible.value = true
  loadCategories()
}

// 提交表单
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
  
  if (form.id) {
    await updateAccount(form)
  } else {
    await addAccount(form)
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  loadData()
}

const handleDel = async (id: number) => {
  await delAccount(id)
  ElMessage.success('删除成功')
  loadData()
}

const formatTime = (time: string) => {
  if (!time) return ''
  return time.replace('T', ' ')
}

onMounted(() => {
  loadData()
  loadSearchCategories()
})
</script>

<style scoped>
.search-card {
  margin-bottom: 20px;
  border-radius: 15px;
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.05);
}
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
