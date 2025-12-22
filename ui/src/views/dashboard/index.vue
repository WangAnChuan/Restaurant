<template>
  <div class="dashboard-container">
    <header class="dashboard-header">
      <div class="header-left">
        <h1 class="page-title">财务概览</h1>
        <p class="current-date">{{ currentDate }}</p>
      </div>
      <div class="header-right">
        <button class="action-btn primary" @click="handleOpenDialog">
          <span class="btn-icon">+</span> 记一笔
        </button>
      </div>
    </header>

    <div class="stats-grid">
      <!-- Total Income -->
      <div class="stat-card">
        <div class="card-top">
          <div class="icon-wrapper income-bg">
            <svg class="icon income-icon" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
          </div>
          <span class="trend positive">
            <svg fill="none" viewBox="0 0 24 24" stroke="currentColor" class="trend-icon"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6" /></svg>
            12.5%
          </span>
        </div>
        <div class="stat-content">
          <p class="stat-label">本月收入</p>
          <h3 class="stat-value">¥{{ stats.totalIncome.toFixed(2) }}</h3>
        </div>
      </div>

      <!-- Total Expense -->
      <div class="stat-card">
        <div class="card-top">
          <div class="icon-wrapper expense-bg">
            <svg class="icon expense-icon" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 17h8m0 0V9m0 8l-8-8-4 4-6-6" />
            </svg>
          </div>
          <span class="trend negative">
            <svg fill="none" viewBox="0 0 24 24" stroke="currentColor" class="trend-icon"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 17h8m0 0V9m0 8l-8-8-4 4-6-6" /></svg>
            3.2%
          </span>
        </div>
        <div class="stat-content">
          <p class="stat-label">本月支出</p>
          <h3 class="stat-value">¥{{ stats.totalExpense.toFixed(2) }}</h3>
        </div>
      </div>

      <!-- Net Income -->
      <div class="stat-card">
        <div class="card-top">
          <div class="icon-wrapper profit-bg">
            <svg class="icon profit-icon" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 002 2h2a2 2 0 002-2z" />
            </svg>
          </div>
        </div>
        <div class="stat-content">
          <p class="stat-label">净收入</p>
          <h3 class="stat-value" :class="stats.netIncome >= 0 ? 'text-positive' : 'text-negative'">
            ¥{{ stats.netIncome.toFixed(2) }}
          </h3>
        </div>
      </div>
    </div>

    <section class="analysis-section">
      <div class="section-header">
        <h2 class="section-title">统计分析</h2>
        <div class="tabs">
          <button 
            v-for="tab in tabs" 
            :key="tab.val"
            @click="switchTab(tab.val)"
            :class="['tab-item', { active: groupBy === tab.val }]"
          >
            {{ tab.label }}
          </button>
        </div>
      </div>

      <div class="table-container">
        <el-table 
          :data="chartData" 
          style="width: 100%" 
          :header-cell-style="{ background: '#f8f9fa', color: '#6c757d', fontWeight: '600' }"
          :cell-style="{ padding: '16px 0' }"
        >
          <el-table-column prop="name" label="类别/分组">
            <template #default="scope">
              <div class="category-cell">
                <span class="category-dot"></span>
                {{ scope.row.name }}
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="value" label="金额" align="right">
            <template #default="scope">
              <span class="amount-cell">¥ {{ scope.row.value.toFixed(2) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="占比" align="right" width="200">
            <template #default="scope">
               <div class="progress-bar-wrapper">
                 <div class="progress-bar" :style="{ width: getPercentage(scope.row.value) + '%' }"></div>
                 <span class="percentage">{{ getPercentage(scope.row.value) }}%</span>
               </div>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="chartData.length === 0" description="暂无数据" />
      </div>
    </section>

    <!-- Transaction Dialog -->
    <el-dialog
      v-model="dialogVisible"
      title="记一笔"
      width="500px"
      :close-on-click-modal="false"
      destroy-on-close
      class="transaction-dialog"
    >
      <el-form :model="form" label-width="80px" :rules="rules" ref="formRef">
        <el-form-item label="类型" prop="type">
          <el-radio-group v-model="form.type" @change="handleTypeChange">
            <el-radio-button :label="1">收入</el-radio-button>
            <el-radio-button :label="2">支出</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="金额" prop="amount">
          <el-input-number v-model="form.amount" :min="0" :precision="2" :step="10" style="width: 100%" />
        </el-form-item>
        <el-form-item label="分类" prop="categoryName">
           <el-autocomplete
              v-model="form.categoryName"
              :fetch-suggestions="querySearchCategory"
              placeholder="请输入或选择分类"
              style="width: 100%"
              @select="handleCategorySelect"
           />
        </el-form-item>
        <el-form-item label="日期" prop="transactionDate">
          <el-date-picker
            v-model="form.transactionDate"
            type="date"
            placeholder="选择日期"
            style="width: 100%"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="支付方式" prop="paymentMethod" v-if="form.type === 1">
           <el-select v-model="form.paymentMethod" placeholder="请选择支付方式" style="width: 100%">
              <el-option label="微信" value="WeChat" />
              <el-option label="支付宝" value="Alipay" />
              <el-option label="现金" value="Cash" />
              <el-option label="银行卡" value="Card" />
           </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm" :loading="submitting">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getDashboardStats, getDashboardChart } from '@/api/account'

const stats = ref({ totalIncome: 0, totalExpense: 0, netIncome: 0 })
const groupBy = ref('category')
const chartData = ref<{name: string, value: number}[]>([])
const currentDate = new Date().toLocaleDateString('zh-CN', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' })

const tabs = [
  { label: '按分类', val: 'category' },
  { label: '按日期', val: 'date' },
  { label: '按支付方式', val: 'payment' }
]

const loadStats = async () => {
  const res: any = await getDashboardStats()
  stats.value = res
}

const loadChart = async () => {
  const res: any = await getDashboardChart({ groupBy: groupBy.value })
  chartData.value = Object.keys(res).map(key => ({ name: key, value: res[key] }))
}

const switchTab = (val: string) => {
  groupBy.value = val
  loadChart()
}

// Helper for progress bar
// const maxVal = computed(() => Math.max(...chartData.value.map(d => d.value), 1))
const getPercentage = (val: number) => {
  if (stats.value.totalIncome === 0) return 0
  return ((val / stats.value.totalIncome) * 100).toFixed(1)
}

onMounted(() => {
  loadStats()
  loadChart()
  loadCategories()
})

// Dialog Logic
import { addAccount, getCategoryList } from '@/api/account'
import { ElMessage } from 'element-plus'

const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref()
const categoryList = ref<{value: string, id: number}[]>([])

const form = ref({
  type: 1,
  amount: 0,
  categoryId: null as number | null,
  categoryName: '',
  transactionDate: new Date().toISOString().split('T')[0],
  remark: '',
  paymentMethod: 'WeChat' // Default
})

const rules = {
  amount: [{ required: true, message: '请输入金额', trigger: 'blur' }],
  categoryName: [{ required: true, message: '请输入或选择分类', trigger: 'blur' }],
  transactionDate: [{ required: true, message: '请选择日期', trigger: 'change' }]
}

const handleOpenDialog = () => {
  form.value = {
    type: 1,
    amount: 0,
    categoryId: null,
    categoryName: '',
    transactionDate: new Date().toISOString().split('T')[0],
    remark: '',
    paymentMethod: 'WeChat'
  }
  dialogVisible.value = true
  loadCategories() // Reload in case type affects it (if we add type filtering later)
}

const handleTypeChange = (_val: string | number | undefined) => {
   // Reset category if switching type might imply different categories (optional logic)
   form.value.categoryName = ''
   form.value.categoryId = null
}

const loadCategories = async () => {
  try {
     const res: any = await getCategoryList({ type: form.value.type }) // Filter by type if backend supports
     if (Array.isArray(res)) {
       categoryList.value = res.map((c: any) => ({ value: c.name, id: c.id }))
     }
  } catch(e) {
    console.error(e)
  }
}

const querySearchCategory = (queryString: string, cb: any) => {
  const results = queryString
    ? categoryList.value.filter(createFilter(queryString))
    : categoryList.value
  cb(results)
}
const createFilter = (queryString: string) => {
  return (item: { value: string }) => {
    return (item.value.toLowerCase().indexOf(queryString.toLowerCase()) === 0)
  }
}

const handleCategorySelect = (item: any) => {
  form.value.categoryId = item.id
  form.value.categoryName = item.value
}

const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid: boolean) => {
    if (valid) {
      // Try to match category name to ID if ID is missing (user typed manually)
      if (!form.value.categoryId) {
        const match = categoryList.value.find(c => c.value === form.value.categoryName)
        if (match) {
          form.value.categoryId = match.id
        } else {
             ElMessage.warning('请选择已存在的分类')
             return
        }
      }

      submitting.value = true
      try {
        await addAccount(form.value)
        ElMessage.success('记录添加成功')
        dialogVisible.value = false
        // Refresh data
        loadStats()
        loadChart()
        loadCategories()
      } catch (error: any) {
        ElMessage.error(error.message || '添加失败')
      } finally {
        submitting.value = false
      }
    }
  })
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700&family=Noto+Sans+SC:wght@400;500;700&display=swap');

.dashboard-container {
  padding: 40px;
  background-color: #f8f9fa;
  min-height: 100vh;
  font-family: 'Inter', 'Noto Sans SC', sans-serif;
  color: #1f2937;
}

.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 40px;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 8px 0;
  color: #111827;
}

.current-date {
  color: #6b7280;
  font-size: 14px;
  margin: 0;
}

.action-btn {
  padding: 10px 20px;
  border-radius: 8px;
  border: none;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 8px;
}

.action-btn.primary {
  background-color: #111827;
  color: white;
}

.action-btn.primary:hover {
  background-color: #374151;
}

/* Stats Section */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 24px;
  margin-bottom: 40px;
}

.stat-card {
  background: white;
  padding: 24px;
  border-radius: 16px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05), 0 2px 4px -1px rgba(0, 0, 0, 0.03);
  transition: transform 0.2s, box-shadow 0.2s;
  border: 1px solid rgba(0,0,0,0.03);
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.08);
}

.card-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}

.icon-wrapper {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon {
  width: 24px;
  height: 24px;
}

/* Specific colors */
.income-bg { background-color: #ecfdf5; }
.income-icon { color: #059669; }
.expense-bg { background-color: #fef2f2; }
.expense-icon { color: #dc2626; }
.profit-bg { background-color: #eff6ff; }
.profit-icon { color: #2563eb; }

.trend {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  font-weight: 500;
  padding: 4px 8px;
  border-radius: 20px;
}

.trend.positive { background: #ecfdf5; color: #059669; }
.trend.negative { background: #fef2f2; color: #dc2626; }
.trend-icon { width: 14px; height: 14px; }

.stat-label {
  color: #6b7280;
  font-size: 13px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin: 0 0 6px 0;
  font-weight: 600;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  margin: 0;
  color: #111827;
  letter-spacing: -0.5px;
}

.text-positive { color: #059669; }
.text-negative { color: #dc2626; }

/* Analysis Section */
.analysis-section {
  background: white;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
  border: 1px solid rgba(0,0,0,0.03);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.section-title {
  font-size: 20px;
  font-weight: 700;
  margin: 0;
  color: #111827;
}

.tabs {
  display: flex;
  background: #f3f4f6;
  padding: 4px;
  border-radius: 8px;
}

.tab-item {
  padding: 8px 16px;
  border: none;
  background: transparent;
  font-size: 14px;
  font-weight: 500;
  color: #6b7280;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.tab-item.active {
  background: white;
  color: #111827;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

/* Table overrides */
.category-cell {
  display: flex;
  align-items: center;
  gap: 12px;
  font-weight: 500;
  color: #374151;
}

.category-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: #3b82f6; /* Default blue for now */
}

.amount-cell {
  font-family: 'Inter', sans-serif;
  font-weight: 600;
  color: #111827;
}

.progress-bar-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
}

.progress-bar {
  height: 6px;
  background: #3b82f6;
  border-radius: 3px;
  min-width: 2px;
}

.percentage {
  font-size: 13px;
  color: #6b7280;
  width: 45px;
  text-align: right;
  font-family: 'Inter', sans-serif;
}

/* Table styles */
:deep(.el-table) {
  --el-table-border-color: #f3f4f6;
  --el-table-header-bg-color: #f9fafb;
}

:deep(.el-table th.el-table__cell) {
  background-color: #f9fafb !important;
}

@media (max-width: 768px) {
  .dashboard-container { padding: 20px; }
  .dashboard-header { flex-direction: column; align-items: flex-start; gap: 20px; }
  .header-right { width: 100%; display: flex; justify-content: flex-end; }
}
</style>
