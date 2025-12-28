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

    <!-- 快捷时间筛选 -->
    <div class="date-filter-section">
      <div class="filter-label">时间范围：</div>
      <div class="filter-buttons">
        <button 
          v-for="filter in dateFilters" 
          :key="filter.value"
          @click="selectDateFilter(filter.value)"
          :class="['filter-btn', { active: selectedDateFilter === filter.value }]"
        >
          {{ filter.label }}
        </button>
      </div>
      <div class="custom-date-picker">
        <el-date-picker
          v-model="customDateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          size="default"
          @change="handleCustomDateChange"
        />
      </div>
    </div>

    <!-- 业务概览区域 -->
    <div class="business-overview">
      <!-- 待办事项/采购提醒 -->
      <div class="todo-card">
        <div class="card-header">
          <div class="header-icon">📋</div>
          <h3 class="card-title">今日待办</h3>
        </div>
        <div class="todo-content" v-loading="todoLoading">
          <div v-if="todayPurchaseList.length > 0" class="todo-list">
            <div class="todo-summary">
              <span class="summary-text">今日待采购项目：</span>
              <span class="summary-count">{{ todayPurchaseList.length }} 件</span>
            </div>
            <div class="todo-items">
              <div 
                v-for="item in todayPurchaseList.slice(0, 3)" 
                :key="item.id" 
                class="todo-item"
                :class="{ completed: item.status === 1 }"
              >
                <div class="item-checkbox">
                  <span v-if="item.status === 1" class="check-icon">✓</span>
                  <span v-else class="uncheck-icon">○</span>
                </div>
                <div class="item-content">
                  <span class="item-name">{{ item.itemName }}</span>
                  <span class="item-quantity">{{ item.quantity }}</span>
                </div>
              </div>
              <div v-if="todayPurchaseList.length > 3" class="more-items">
                还有 {{ todayPurchaseList.length - 3 }} 项...
              </div>
            </div>
            <button class="view-all-btn" @click="goToPurchasePage">
              查看全部 →
            </button>
          </div>
          <div v-else class="empty-todo">
            <span class="empty-icon">✅</span>
            <p class="empty-text">今日暂无待采购项目</p>
          </div>
        </div>
      </div>

      <!-- 热门菜品 TOP 5 -->
      <div class="top-dishes-card">
        <div class="card-header">
          <div class="header-icon">🏆</div>
          <h3 class="card-title">热门菜品</h3>
        </div>
        <div class="dishes-content" v-loading="dishesLoading">
          <div v-if="topDishes.length > 0" class="dishes-list">
            <div 
              v-for="(dish, index) in topDishes" 
              :key="dish.id" 
              class="dish-item"
            >
              <div class="dish-rank" :class="getRankClass(index)">
                {{ index + 1 }}
              </div>
              <div class="dish-info">
                <div class="dish-name">{{ dish.name }}</div>
                <div class="dish-meta">
                  <span class="dish-price">¥{{ dish.price }}</span>
                  <span class="dish-category">{{ dish.categoryName || '其他' }}</span>
                </div>
              </div>
              <div class="dish-badge" v-if="index === 0">
                <span class="badge-icon">🔥</span>
              </div>
            </div>
          </div>
          <div v-else class="empty-dishes">
            <span class="empty-icon">🍽️</span>
            <p class="empty-text">暂无菜品数据</p>
          </div>
        </div>
      </div>
    </div>

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
          <div class="stat-indicators">
            <span class="indicator-item">
              利润率: <strong :class="profitMargin >= 0 ? 'text-positive' : 'text-negative'">{{ profitMargin }}%</strong>
            </span>
            <span v-if="incomeComparison" class="indicator-item comparison" :class="incomeComparison.trend">
              {{ incomeComparison.label }} {{ incomeComparison.icon }} {{ incomeComparison.percent }}
            </span>
          </div>
        </div>
      </div>

      <!-- Total Expense -->
      <div class="stat-card" :class="{ 'budget-warning': isBudgetWarning, 'budget-danger': isBudgetDanger }">
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
          <div class="stat-indicators">
            <span class="indicator-item">
              预算: ¥{{ monthlyBudget.toFixed(2) }}
            </span>
            <div v-if="budgetUsagePercent > 0" class="budget-progress">
              <div class="budget-bar-bg">
                <div 
                  class="budget-bar-fill" 
                  :class="{ warning: budgetUsagePercent >= 80, danger: budgetUsagePercent >= 100 }"
                  :style="{ width: Math.min(budgetUsagePercent, 100) + '%' }"
                ></div>
              </div>
              <span class="budget-text" :class="{ warning: budgetUsagePercent >= 80, danger: budgetUsagePercent >= 100 }">
                {{ budgetUsagePercent.toFixed(1) }}%
              </span>
            </div>
          </div>
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
        <div class="header-actions">
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
          <button class="action-btn secondary" @click="toggleRecordsView">
            <span class="btn-icon">📋</span> {{ showRecordsView ? '隐藏记录' : '详情记录' }}
          </button>
          <button class="action-btn secondary" @click="toggleChartAnalysis">
            <span class="btn-icon">📊</span> {{ showChartAnalysis ? '隐藏图表' : '图表分析' }}
          </button>
          <button class="action-btn secondary" @click="toggleTrendAnalysis">
            <span class="btn-icon">📈</span> {{ showTrendAnalysis ? '隐藏趋势' : '趋势分析' }}
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

      <!-- Records View Section -->
      <transition name="fade">
        <div v-if="showRecordsView" class="records-view-section">
          <div class="records-header">
            <h3 class="section-subtitle">交易记录详情</h3>
            <button class="action-btn export-btn" @click="exportToExcel">
              <span class="btn-icon">📥</span> 导出 Excel
            </button>
          </div>
          <el-table 
            :data="recordsList" 
            style="width: 100%"
            v-loading="recordsLoading"
            @row-click="handleRowClick"
            :row-style="{ cursor: 'pointer' }"
            stripe
          >
            <el-table-column prop="transactionDate" label="日期" width="120" />
            <el-table-column prop="type" label="类型" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.type === 1 ? 'success' : 'danger'" size="small">
                  {{ scope.row.type === 1 ? '收入' : '支出' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="categoryName" label="分类" width="130" />
            <el-table-column prop="paymentMethod" label="支付方式" width="120" />
            <el-table-column prop="amount" label="金额 (¥)" width="130" align="right">
              <template #default="scope">
                <span :style="{ color: scope.row.type === 1 ? '#059669' : '#dc2626', fontWeight: '600' }">
                  {{ scope.row.type === 1 ? '+' : '-' }}{{ scope.row.amount.toFixed(2) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="占比" width="100" align="right">
              <template #default="scope">
                <span class="percentage-text">{{ getRecordPercentage(scope.row.amount) }}%</span>
              </template>
            </el-table-column>
            <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
            <el-table-column label="操作" width="180" fixed="right">
              <template #default="scope">
                <el-button type="warning" size="small" @click.stop="handleEdit(scope.row)">修改</el-button>
                <el-button type="danger" size="small" @click.stop="handleDelete(scope.row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          
          <div class="pagination-wrapper">
            <el-pagination
              v-model:current-page="pagination.current"
              v-model:page-size="pagination.size"
              :page-sizes="[5, 10, 20, 50]"
              :total="pagination.total"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="loadRecords"
              @current-change="loadRecords"
            />
          </div>
        </div>
      </transition>

      <!-- Chart Analysis Section -->
      <transition name="fade">
        <div v-if="showChartAnalysis" class="chart-analysis-section">
          <h3 class="chart-section-title">分类占比分析</h3>
          <div class="charts-wrapper">
            <div class="chart-item">
              <div class="chart-header">
                <h4 class="chart-title">收入分类占比</h4>
                <p class="chart-subtitle">本月总收入: ¥{{ stats.totalIncome.toFixed(2) }}</p>
              </div>
              <div ref="incomeChartRef" class="pie-chart"></div>
            </div>
            <div class="chart-item">
              <div class="chart-header">
                <h4 class="chart-title">支出分类占比</h4>
                <p class="chart-subtitle">本月总支出: ¥{{ stats.totalExpense.toFixed(2) }}</p>
              </div>
              <div ref="expenseChartRef" class="pie-chart"></div>
            </div>
          </div>
        </div>
      </transition>

      <!-- Trend Analysis Section -->
      <transition name="fade">
        <div v-if="showTrendAnalysis" class="trend-analysis-section">
          <div class="trend-header">
            <h3 class="chart-section-title">收支趋势分析</h3>
            <div class="trend-tabs">
              <button 
                v-for="trendTab in trendTabs" 
                :key="trendTab.value"
                @click="selectTrendPeriod(trendTab.value)"
                :class="['trend-tab-item', { active: trendPeriod === trendTab.value }]"
              >
                {{ trendTab.label }}
              </button>
            </div>
          </div>
          <div class="trend-chart-container">
            <div ref="trendChartRef" class="trend-chart"></div>
          </div>
        </div>
      </transition>
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

    <!-- Detail View Dialog (Read-only) -->
    <el-dialog
      v-model="detailDialogVisible"
      title="记录详情"
      width="500px"
      :close-on-click-modal="true"
    >
      <el-descriptions :column="1" border v-if="currentRecord">
        <el-descriptions-item label="日期">{{ currentRecord.transactionDate }}</el-descriptions-item>
        <el-descriptions-item label="类型">
          <el-tag :type="currentRecord.type === 1 ? 'success' : 'danger'">
            {{ currentRecord.type === 1 ? '收入' : '支出' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="分类">{{ currentRecord.categoryName }}</el-descriptions-item>
        <el-descriptions-item label="金额">
          <span :style="{ color: currentRecord.type === 1 ? '#059669' : '#dc2626', fontWeight: '600', fontSize: '18px' }">
            ¥ {{ currentRecord.amount?.toFixed(2) }}
          </span>
        </el-descriptions-item>
        <el-descriptions-item label="占比">
          <span class="percentage-text" style="font-size: 16px; font-weight: 600;">
            {{ getRecordPercentage(currentRecord.amount) }}%
          </span>
        </el-descriptions-item>
        <el-descriptions-item label="支付方式">{{ currentRecord.paymentMethod || '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注">{{ currentRecord.remark || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentRecord.createTime }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ currentRecord.updateTime }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- Edit Dialog -->
    <el-dialog
      v-model="editDialogVisible"
      title="修改记录"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="editForm" label-width="80px" :rules="rules" ref="editFormRef">
        <el-form-item label="类型" prop="type">
          <el-radio-group v-model="editForm.type" @change="handleEditTypeChange">
            <el-radio-button :label="1">收入</el-radio-button>
            <el-radio-button :label="2">支出</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="金额" prop="amount">
          <el-input-number v-model="editForm.amount" :min="0" :precision="2" :step="10" style="width: 100%" />
        </el-form-item>
        <el-form-item label="分类" prop="categoryName">
           <el-autocomplete
              v-model="editForm.categoryName"
              :fetch-suggestions="querySearchCategory"
              placeholder="请输入或选择分类"
              style="width: 100%"
              @select="handleEditCategorySelect"
           />
        </el-form-item>
        <el-form-item label="日期" prop="transactionDate">
          <el-date-picker
            v-model="editForm.transactionDate"
            type="date"
            placeholder="选择日期"
            style="width: 100%"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="支付方式" prop="paymentMethod" v-if="editForm.type === 1">
           <el-select v-model="editForm.paymentMethod" placeholder="请选择支付方式" style="width: 100%">
              <el-option label="微信" value="WeChat" />
              <el-option label="支付宝" value="Alipay" />
              <el-option label="现金" value="Cash" />
              <el-option label="银行卡" value="Card" />
           </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="editForm.remark" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmEdit" :loading="editSubmitting">
            保存
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, watch } from 'vue'
import { getDashboardStats, getDashboardChart, getAccountPage } from '@/api/account'
import * as echarts from 'echarts'
import type { EChartsOption } from 'echarts'
import * as XLSX from 'xlsx'

const stats = ref({ totalIncome: 0, totalExpense: 0, netIncome: 0 })
const groupBy = ref('category')
const chartData = ref<{name: string, value: number}[]>([])
const currentDate = new Date().toLocaleDateString('zh-CN', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' })

// 日期筛选相关
const selectedDateFilter = ref('month')
const customDateRange = ref<[string, string] | null>(null)
const dateFilters = [
  { label: '今日', value: 'today' },
  { label: '昨日', value: 'yesterday' },
  { label: '近7日', value: 'week' },
  { label: '本月', value: 'month' },
  { label: '本年', value: 'year' }
]

// 获取日期范围
const getDateRange = (filter: string): [string, string] => {
  const today = new Date()
  const year = today.getFullYear()
  const month = today.getMonth()
  const date = today.getDate()

  switch (filter) {
    case 'today':
      return [
        formatDate(today),
        formatDate(today)
      ]
    case 'yesterday':
      const yesterday = new Date(year, month, date - 1)
      return [
        formatDate(yesterday),
        formatDate(yesterday)
      ]
    case 'week':
      const weekAgo = new Date(year, month, date - 6)
      return [
        formatDate(weekAgo),
        formatDate(today)
      ]
    case 'month':
      const monthStart = new Date(year, month, 1)
      return [
        formatDate(monthStart),
        formatDate(today)
      ]
    case 'year':
      const yearStart = new Date(year, 0, 1)
      return [
        formatDate(yearStart),
        formatDate(today)
      ]
    default:
      return [
        formatDate(new Date(year, month, 1)),
        formatDate(today)
      ]
  }
}

const formatDate = (date: Date): string => {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

const selectDateFilter = (filter: string) => {
  selectedDateFilter.value = filter
  customDateRange.value = null
  loadDataWithDateFilter()
}

const handleCustomDateChange = (dates: [string, string] | null) => {
  if (dates) {
    selectedDateFilter.value = 'custom'
    loadDataWithDateFilter()
  }
}

const loadDataWithDateFilter = async () => {
  const [startDate, endDate] = selectedDateFilter.value === 'custom' && customDateRange.value
    ? customDateRange.value
    : getDateRange(selectedDateFilter.value)
  
  // 重新加载统计数据和图表数据，传入日期范围
  await Promise.all([
    loadStatsWithDate(startDate, endDate),
    loadChartWithDate(startDate, endDate)
  ])
  
  // 如果详情记录展开，也需要刷新
  if (showRecordsView.value) {
    loadRecordsWithDate(startDate, endDate)
  }
  
  // 如果图表分析展开，也需要刷新
  if (showChartAnalysis.value) {
    loadChartAnalysisWithDate(startDate, endDate)
  }
}

const loadStatsWithDate = async (startDate: string, endDate: string) => {
  try {
    // 由于后端getDashboardStats没有日期参数，我们需要从记录中计算
    const res: any = await getAccountPage({ 
      current: 1, 
      size: 10000,
      startDate,
      endDate
    })
    const records = res.records || []
    
    let totalIncome = 0
    let totalExpense = 0
    
    records.forEach((record: any) => {
      const amount = parseFloat(record.amount || 0)
      if (record.type === 1) {
        totalIncome += amount
      } else if (record.type === 2) {
        totalExpense += amount
      }
    })
    
    stats.value = {
      totalIncome,
      totalExpense,
      netIncome: totalIncome - totalExpense
    }
  } catch (error: any) {
    console.error('加载统计数据失败:', error)
  }
}

const loadChartWithDate = async (startDate: string, endDate: string) => {
  try {
    // 由于后端没有日期筛选参数，我们从记录中计算
    const res: any = await getAccountPage({ 
      current: 1, 
      size: 10000,
      startDate,
      endDate
    })
    const records = res.records || []
    
    const dataMap = new Map<string, number>()
    
    records.forEach((record: any) => {
      let key = ''
      if (groupBy.value === 'category') {
        key = record.categoryName || '未分类'
      } else if (groupBy.value === 'payment') {
        key = record.paymentMethod || '未知'
      } else {
        key = record.transactionDate || '未知'
      }
      
      const amount = parseFloat(record.amount || 0)
      dataMap.set(key, (dataMap.get(key) || 0) + amount)
    })
    
    chartData.value = Array.from(dataMap.entries()).map(([name, value]) => ({
      name,
      value
    }))
  } catch (error: any) {
    console.error('加载图表数据失败:', error)
  }
}

const loadRecordsWithDate = (startDate: string, endDate: string) => {
  // 在loadRecords中添加日期参数
  loadRecordsWithDateParams(startDate, endDate)
}

const loadChartAnalysisWithDate = async (startDate: string, endDate: string) => {
  try {
    const res: any = await getAccountPage({ 
      current: 1, 
      size: 1000,
      startDate,
      endDate
    })
    const records = res.records || []
    
    const incomeMap = new Map<string, number>()
    const expenseMap = new Map<string, number>()
    
    records.forEach((record: any) => {
      const categoryName = record.categoryName || '未分类'
      const amount = parseFloat(record.amount || 0)
      
      if (record.type === 1) {
        incomeMap.set(categoryName, (incomeMap.get(categoryName) || 0) + amount)
      } else if (record.type === 2) {
        expenseMap.set(categoryName, (expenseMap.get(categoryName) || 0) + amount)
      }
    })
    
    renderPieChart(incomeChartRef.value!, incomeMap, 'incomeChart', '#059669')
    renderPieChart(expenseChartRef.value!, expenseMap, 'expenseChart', '#dc2626')
  } catch (error: any) {
    console.error('加载图表分析失败:', error)
  }
}

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
  const [startDate, endDate] = selectedDateFilter.value === 'custom' && customDateRange.value
    ? customDateRange.value
    : getDateRange(selectedDateFilter.value)
  loadChartWithDate(startDate, endDate)
}

// Helper for progress bar
// 计算百分比：当前值 / (总收入 + 总支出) * 100
const getPercentage = (val: number) => {
  const total = stats.value.totalIncome + stats.value.totalExpense
  if (total === 0) return 0
  return ((val / total) * 100).toFixed(1)
}

// 业务概览数据
const todoLoading = ref(false)
const dishesLoading = ref(false)
const todayPurchaseList = ref<any[]>([])
const topDishes = ref<any[]>([])

// 加载今日待采购项目
const loadTodayPurchase = async () => {
  todoLoading.value = true
  try {
    const today = formatDate(new Date())
    const res: any = await getPurchaseList({ date: today })
    // 筛选未完成的项目
    todayPurchaseList.value = (res || []).filter((item: any) => item.status === 0)
  } catch (error: any) {
    console.error('加载采购清单失败:', error)
  } finally {
    todoLoading.value = false
  }
}

// 加载热门菜品
const loadTopDishes = async () => {
  dishesLoading.value = true
  try {
    const res: any = await getPublicDishList()
    // 由于没有销量数据，这里按价格排序取前5个作为"热门"菜品
    // 实际应用中应该根据订单数据统计销量
    topDishes.value = (res || [])
      .sort((a: any, b: any) => parseFloat(b.price) - parseFloat(a.price))
      .slice(0, 5)
  } catch (error: any) {
    console.error('加载热门菜品失败:', error)
  } finally {
    dishesLoading.value = false
  }
}

// 获取排名样式
const getRankClass = (index: number) => {
  if (index === 0) return 'rank-gold'
  if (index === 1) return 'rank-silver'
  if (index === 2) return 'rank-bronze'
  return 'rank-normal'
}

// 跳转到采购页面
const goToPurchasePage = () => {
  router.push('/purchase')
}

// 经营预警与指标
const monthlyBudget = ref(5000) // 每月预算，可配置
const budgetUsagePercent = ref(0)
const isBudgetWarning = ref(false)
const isBudgetDanger = ref(false)
const profitMargin = ref(0)
const incomeComparison = ref<any>(null)

// 计算经营指标
const calculateIndicators = async () => {
  // 计算预算使用率
  budgetUsagePercent.value = monthlyBudget.value > 0 
    ? (stats.value.totalExpense / monthlyBudget.value) * 100 
    : 0
  
  // 判断预算预警
  isBudgetWarning.value = budgetUsagePercent.value >= 80 && budgetUsagePercent.value < 100
  isBudgetDanger.value = budgetUsagePercent.value >= 100
  
  // 计算利润率
  profitMargin.value = stats.value.totalIncome > 0
    ? ((stats.value.netIncome / stats.value.totalIncome) * 100)
    : 0
  profitMargin.value = parseFloat(profitMargin.value.toFixed(1))
  
  // 计算环比（较上月）
  await calculateComparison()
}

const calculateComparison = async () => {
  try {
    const today = new Date()
    const year = today.getFullYear()
    const month = today.getMonth()
    
    // 获取上月同期数据
    const lastMonthStart = new Date(year, month - 1, 1)
    const lastMonthEnd = new Date(year, month, 0) // 上月最后一天
    
    const res: any = await getAccountPage({ 
      current: 1, 
      size: 10000,
      startDate: formatDate(lastMonthStart),
      endDate: formatDate(lastMonthEnd)
    })
    
    const records = res.records || []
    let lastMonthIncome = 0
    
    records.forEach((record: any) => {
      if (record.type === 1) {
        lastMonthIncome += parseFloat(record.amount || 0)
      }
    })
    
    // 计算环比
    if (lastMonthIncome > 0) {
      const change = stats.value.totalIncome - lastMonthIncome
      const changePercent = (change / lastMonthIncome) * 100
      
      incomeComparison.value = {
        label: '较上月',
        icon: change >= 0 ? '↑' : '↓',
        percent: Math.abs(changePercent).toFixed(1) + '%',
        trend: change >= 0 ? 'positive' : 'negative'
      }
    }
  } catch (error: any) {
    console.error('计算环比失败:', error)
  }
}

// 趋势分析
const showTrendAnalysis = ref(false)
const trendChartRef = ref<HTMLElement>()
let trendChartInstance: echarts.ECharts | null = null
const trendPeriod = ref('week') // week or month
const trendTabs = [
  { label: '近7日', value: 'week' },
  { label: '本月', value: 'month' }
]

const toggleTrendAnalysis = () => {
  showTrendAnalysis.value = !showTrendAnalysis.value
  if (showTrendAnalysis.value) {
    nextTick(() => {
      loadTrendAnalysis()
    })
  }
}

const selectTrendPeriod = (period: string) => {
  trendPeriod.value = period
  loadTrendAnalysis()
}

const loadTrendAnalysis = async () => {
  try {
    // 计算日期范围
    const today = new Date()
    const startDate = trendPeriod.value === 'week'
      ? new Date(today.getFullYear(), today.getMonth(), today.getDate() - 6)
      : new Date(today.getFullYear(), today.getMonth(), 1)
    
    const res: any = await getAccountPage({ 
      current: 1, 
      size: 10000,
      startDate: formatDate(startDate),
      endDate: formatDate(today)
    })
    
    const records = res.records || []
    
    // 按日期分组统计
    const dateMap = new Map<string, { income: number, expense: number }>()
    
    // 初始化所有日期
    const currentDate = new Date(startDate)
    while (currentDate <= today) {
      const dateStr = formatDate(currentDate)
      dateMap.set(dateStr, { income: 0, expense: 0 })
      currentDate.setDate(currentDate.getDate() + 1)
    }
    
    // 统计数据
    records.forEach((record: any) => {
      const date = record.transactionDate
      const amount = parseFloat(record.amount || 0)
      
      if (dateMap.has(date)) {
        const data = dateMap.get(date)!
        if (record.type === 1) {
          data.income += amount
        } else if (record.type === 2) {
          data.expense += amount
        }
      }
    })
    
    // 准备图表数据
    const dates: string[] = []
    const incomeData: number[] = []
    const expenseData: number[] = []
    const profitData: number[] = []
    
    dateMap.forEach((data, date) => {
      dates.push(date)
      incomeData.push(data.income)
      expenseData.push(data.expense)
      profitData.push(data.income - data.expense)
    })
    
    renderTrendChart(dates, incomeData, expenseData, profitData)
  } catch (error: any) {
    ElMessage.error(error.message || '加载趋势数据失败')
  }
}

const renderTrendChart = (
  dates: string[], 
  incomeData: number[], 
  expenseData: number[], 
  profitData: number[]
) => {
  if (!trendChartRef.value) return
  
  // 销毁旧实例
  if (trendChartInstance) {
    trendChartInstance.dispose()
  }
  
  // 创建新实例
  trendChartInstance = echarts.init(trendChartRef.value)
  
  const option: EChartsOption = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross',
        label: {
          backgroundColor: '#6a7985'
        }
      },
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#e5e7eb',
      borderWidth: 1,
      textStyle: {
        color: '#374151'
      },
      formatter: (params: any) => {
        let result = `<div style="font-weight: 600; margin-bottom: 8px;">${params[0].axisValue}</div>`
        params.forEach((param: any) => {
          result += `<div style="margin: 4px 0;">
            ${param.marker} ${param.seriesName}: <strong>¥${param.value.toFixed(2)}</strong>
          </div>`
        })
        return result
      }
    },
    legend: {
      data: ['收入', '支出', '净利润'],
      top: '3%',
      textStyle: {
        color: '#6b7280',
        fontSize: 13
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dates,
      axisLine: {
        lineStyle: {
          color: '#e5e7eb'
        }
      },
      axisLabel: {
        color: '#6b7280',
        fontSize: 12
      }
    },
    yAxis: {
      type: 'value',
      axisLine: {
        lineStyle: {
          color: '#e5e7eb'
        }
      },
      axisLabel: {
        color: '#6b7280',
        fontSize: 12,
        formatter: '¥{value}'
      },
      splitLine: {
        lineStyle: {
          color: '#f3f4f6'
        }
      }
    },
    series: [
      {
        name: '收入',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        lineStyle: {
          color: '#10b981',
          width: 3
        },
        itemStyle: {
          color: '#10b981',
          borderColor: '#fff',
          borderWidth: 2
        },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(16, 185, 129, 0.3)' },
              { offset: 1, color: 'rgba(16, 185, 129, 0.05)' }
            ]
          }
        },
        data: incomeData
      },
      {
        name: '支出',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        lineStyle: {
          color: '#ef4444',
          width: 3
        },
        itemStyle: {
          color: '#ef4444',
          borderColor: '#fff',
          borderWidth: 2
        },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(239, 68, 68, 0.3)' },
              { offset: 1, color: 'rgba(239, 68, 68, 0.05)' }
            ]
          }
        },
        data: expenseData
      },
      {
        name: '净利润',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        lineStyle: {
          color: '#3b82f6',
          width: 3,
          type: 'dashed'
        },
        itemStyle: {
          color: '#3b82f6',
          borderColor: '#fff',
          borderWidth: 2
        },
        data: profitData
      }
    ]
  }
  
  trendChartInstance.setOption(option)
  
  // 响应式调整
  window.addEventListener('resize', () => {
    if (trendChartInstance) {
      trendChartInstance.resize()
    }
  })
}

onMounted(() => {
  loadDataWithDateFilter()
  loadCategories()
  loadTodayPurchase()
  loadTopDishes()
})

// 监听统计数据变化，计算指标
watch(() => stats.value, () => {
  calculateIndicators()
}, { deep: true, immediate: true })

// Dialog Logic
import { addAccount, getCategoryList, getAccountPage, updateAccount, delAccount } from '@/api/account'
import { getPublicDishList } from '@/api/dish'
import { getPurchaseList } from '@/api/purchase'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'

const router = useRouter()

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
        loadDataWithDateFilter()
        if (showChartAnalysis.value) {
          loadChartAnalysis()
        }
        if (showTrendAnalysis.value) {
          loadTrendAnalysis()
        }
      } catch (error: any) {
        ElMessage.error(error.message || '添加失败')
      } finally {
        submitting.value = false
      }
    }
  })
}

// Records View Logic
const showRecordsView = ref(false)
const recordsLoading = ref(false)
const recordsList = ref<any[]>([])
const pagination = ref({
  current: 1,
  size: 10,
  total: 0
})

const toggleRecordsView = () => {
  showRecordsView.value = !showRecordsView.value
  if (showRecordsView.value) {
    nextTick(() => {
      loadRecords()
    })
  }
}

const loadRecords = async () => {
  const [startDate, endDate] = selectedDateFilter.value === 'custom' && customDateRange.value
    ? customDateRange.value
    : getDateRange(selectedDateFilter.value)
  
  await loadRecordsWithDateParams(startDate, endDate)
}

const loadRecordsWithDateParams = async (startDate: string, endDate: string) => {
  recordsLoading.value = true
  try {
    const res: any = await getAccountPage({
      current: pagination.value.current,
      size: pagination.value.size,
      startDate,
      endDate
    })
    recordsList.value = res.records || []
    pagination.value.total = res.total || 0
  } catch (error: any) {
    ElMessage.error(error.message || '加载记录失败')
  } finally {
    recordsLoading.value = false
  }
}

// 导出Excel功能
const exportToExcel = async () => {
  try {
    // 获取所有数据（不分页）
    const [startDate, endDate] = selectedDateFilter.value === 'custom' && customDateRange.value
      ? customDateRange.value
      : getDateRange(selectedDateFilter.value)
    
    const res: any = await getAccountPage({
      current: 1,
      size: 10000,
      startDate,
      endDate
    })
    
    const allRecords = res.records || []
    
    if (allRecords.length === 0) {
      ElMessage.warning('没有数据可导出')
      return
    }
    
    // 准备Excel数据
    const excelData = allRecords.map((record: any) => ({
      '日期': record.transactionDate,
      '类型': record.type === 1 ? '收入' : '支出',
      '分类': record.categoryName,
      '支付方式': record.paymentMethod || '-',
      '金额': record.amount,
      '占比': getRecordPercentage(record.amount) + '%',
      '备注': record.remark || '-',
      '创建时间': record.createTime
    }))
    
    // 创建工作簿
    const wb = XLSX.utils.book_new()
    const ws = XLSX.utils.json_to_sheet(excelData)
    
    // 设置列宽
    ws['!cols'] = [
      { wch: 12 },  // 日期
      { wch: 8 },   // 类型
      { wch: 15 },  // 分类
      { wch: 12 },  // 支付方式
      { wch: 10 },  // 金额
      { wch: 8 },   // 占比
      { wch: 30 },  // 备注
      { wch: 20 }   // 创建时间
    ]
    
    XLSX.utils.book_append_sheet(wb, ws, '交易记录')
    
    // 生成文件名
    const fileName = `财务报表_${startDate}_${endDate}.xlsx`
    
    // 导出文件
    XLSX.writeFile(wb, fileName)
    
    ElMessage.success('导出成功！')
  } catch (error: any) {
    console.error('导出失败:', error)
    ElMessage.error(error.message || '导出失败')
  }
}

// Detail View Logic
const detailDialogVisible = ref(false)
const currentRecord = ref<any>(null)

const handleViewDetail = (row: any) => {
  currentRecord.value = { ...row }
  detailDialogVisible.value = true
}

// 行点击事件
const handleRowClick = (row: any) => {
  handleViewDetail(row)
}

// 计算单条记录的百分比
const getRecordPercentage = (amount: number) => {
  const total = stats.value.totalIncome + stats.value.totalExpense
  if (total === 0) return '0.0'
  return ((amount / total) * 100).toFixed(1)
}

// Edit Logic
const editDialogVisible = ref(false)
const editSubmitting = ref(false)
const editFormRef = ref()
const editForm = ref<any>({
  id: null,
  type: 1,
  amount: 0,
  categoryId: null,
  categoryName: '',
  transactionDate: '',
  remark: '',
  paymentMethod: 'WeChat'
})

const handleEdit = (row: any) => {
  editForm.value = { ...row }
  editDialogVisible.value = true
  loadCategories()
}

const handleEditTypeChange = () => {
  editForm.value.categoryName = ''
  editForm.value.categoryId = null
  loadCategories()
}

const handleEditCategorySelect = (item: any) => {
  editForm.value.categoryId = item.id
  editForm.value.categoryName = item.value
}

const confirmEdit = async () => {
  if (!editFormRef.value) return
  await editFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      // 确认修改
      try {
        await ElMessageBox.confirm(
          '确认要修改这条记录吗？',
          '确认修改',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning',
          }
        )
        
        // Try to match category name to ID if ID is missing
        if (!editForm.value.categoryId) {
          const match = categoryList.value.find(c => c.value === editForm.value.categoryName)
          if (match) {
            editForm.value.categoryId = match.id
          } else {
            ElMessage.warning('请选择已存在的分类')
            return
          }
        }

        editSubmitting.value = true
        try {
          await updateAccount(editForm.value)
          ElMessage.success('修改成功')
          editDialogVisible.value = false
          // Refresh data
          loadRecords()
          loadDataWithDateFilter()
          if (showChartAnalysis.value) {
            loadChartAnalysis()
          }
          if (showTrendAnalysis.value) {
            loadTrendAnalysis()
          }
        } catch (error: any) {
          ElMessage.error(error.message || '修改失败')
        } finally {
          editSubmitting.value = false
        }
      } catch {
        // 用户取消
      }
    }
  })
}

// Delete Logic
const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm(
      '确认要删除这条记录吗？',
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    
    try {
      await delAccount(id)
      ElMessage.success('删除成功')
      // Refresh data
      loadRecords()
      loadDataWithDateFilter()
      if (showChartAnalysis.value) {
        loadChartAnalysis()
      }
      if (showTrendAnalysis.value) {
        loadTrendAnalysis()
      }
    } catch (error: any) {
      ElMessage.error(error.message || '删除失败')
    }
  } catch {
    // 用户取消
  }
}

// Chart Analysis Logic
const showChartAnalysis = ref(false)
const incomeChartRef = ref<HTMLElement>()
const expenseChartRef = ref<HTMLElement>()
let incomeChartInstance: echarts.ECharts | null = null
let expenseChartInstance: echarts.ECharts | null = null

const toggleChartAnalysis = () => {
  showChartAnalysis.value = !showChartAnalysis.value
  if (showChartAnalysis.value) {
    nextTick(() => {
      loadChartAnalysis()
    })
  }
}

const loadChartAnalysis = async () => {
  try {
    // 获取所有记录进行分类统计
    const res: any = await getAccountPage({ current: 1, size: 1000 })
    const records = res.records || []
    
    // 分别统计收入和支出
    const incomeMap = new Map<string, number>()
    const expenseMap = new Map<string, number>()
    
    records.forEach((record: any) => {
      const categoryName = record.categoryName || '未分类'
      const amount = parseFloat(record.amount || 0)
      
      if (record.type === 1) {
        // 收入
        incomeMap.set(categoryName, (incomeMap.get(categoryName) || 0) + amount)
      } else if (record.type === 2) {
        // 支出
        expenseMap.set(categoryName, (expenseMap.get(categoryName) || 0) + amount)
      }
    })
    
    // 绘制收入饼图
    renderPieChart(incomeChartRef.value!, incomeMap, 'incomeChart', '#059669')
    // 绘制支出饼图
    renderPieChart(expenseChartRef.value!, expenseMap, 'expenseChart', '#dc2626')
  } catch (error: any) {
    ElMessage.error(error.message || '加载图表数据失败')
  }
}

const renderPieChart = (
  container: HTMLElement, 
  dataMap: Map<string, number>, 
  chartType: string,
  mainColor: string
) => {
  // 销毁旧实例
  if (chartType === 'incomeChart' && incomeChartInstance) {
    incomeChartInstance.dispose()
  }
  if (chartType === 'expenseChart' && expenseChartInstance) {
    expenseChartInstance.dispose()
  }
  
  // 创建新实例
  const chartInstance = echarts.init(container)
  if (chartType === 'incomeChart') {
    incomeChartInstance = chartInstance
  } else {
    expenseChartInstance = chartInstance
  }
  
  // 准备数据
  const chartData = Array.from(dataMap.entries()).map(([name, value]) => ({
    name,
    value
  })).sort((a, b) => b.value - a.value)
  
  // 计算总额
  const total = chartData.reduce((sum, item) => sum + item.value, 0)
  
  // 配色方案
  const colors = chartType === 'incomeChart' 
    ? ['#10b981', '#34d399', '#6ee7b7', '#a7f3d0', '#d1fae5']
    : ['#ef4444', '#f87171', '#fca5a5', '#fecaca', '#fee2e2']
  
  const option: EChartsOption = {
    color: colors,
    tooltip: {
      trigger: 'item',
      formatter: (params: any) => {
        const percent = ((params.value / total) * 100).toFixed(1)
        return `${params.name}<br/>金额: ¥${params.value.toFixed(2)}<br/>占比: ${percent}%`
      },
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#e5e7eb',
      borderWidth: 1,
      textStyle: {
        color: '#374151',
        fontSize: 14
      },
      padding: 12
    },
    legend: {
      orient: 'vertical',
      right: '5%',
      top: 'center',
      itemWidth: 14,
      itemHeight: 14,
      itemGap: 16,
      textStyle: {
        color: '#6b7280',
        fontSize: 13,
        fontWeight: 500
      },
      formatter: (name: string) => {
        const item = chartData.find(d => d.name === name)
        if (item) {
          const percent = ((item.value / total) * 100).toFixed(1)
          return `${name}  ${percent}%`
        }
        return name
      }
    },
    series: [
      {
        name: chartType === 'incomeChart' ? '收入' : '支出',
        type: 'pie',
        radius: ['45%', '70%'],
        center: ['35%', '50%'],
        avoidLabelOverlap: true,
        itemStyle: {
          borderRadius: 8,
          borderColor: '#fff',
          borderWidth: 3
        },
        label: {
          show: false
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 16,
            fontWeight: 'bold',
            formatter: (params: any) => {
              const percent = ((params.value / total) * 100).toFixed(1)
              return `${params.name}\n¥${params.value.toFixed(2)}\n${percent}%`
            }
          },
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.2)'
          }
        },
        data: chartData.length > 0 ? chartData : [{ name: '暂无数据', value: 1 }]
      }
    ]
  }
  
  if (chartData.length === 0) {
    option.color = ['#e5e7eb']
  }
  
  chartInstance.setOption(option)
  
  // 响应式调整
  window.addEventListener('resize', () => {
    chartInstance.resize()
  })
}

// 监听统计数据变化，自动刷新图表
watch(() => stats.value, () => {
  if (showChartAnalysis.value) {
    nextTick(() => {
      loadChartAnalysis()
    })
  }
}, { deep: true })
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
  margin-bottom: 24px;
}

/* Date Filter Section */
.date-filter-section {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 32px;
  padding: 16px 24px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.04);
}

.filter-label {
  font-weight: 600;
  color: #374151;
  font-size: 14px;
  white-space: nowrap;
}

.filter-buttons {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.filter-btn {
  padding: 6px 16px;
  border: 1px solid #e5e7eb;
  background: white;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 500;
  color: #6b7280;
  cursor: pointer;
  transition: all 0.2s;
}

.filter-btn:hover {
  border-color: #3b82f6;
  color: #3b82f6;
  background: #eff6ff;
}

.filter-btn.active {
  background: #3b82f6;
  color: white;
  border-color: #3b82f6;
}

.custom-date-picker {
  margin-left: auto;
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

/* Business Overview */
.business-overview {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 24px;
  margin-bottom: 32px;
}

.todo-card,
.top-dishes-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
  border: 1px solid rgba(0, 0, 0, 0.03);
  transition: transform 0.2s, box-shadow 0.2s;
}

.todo-card:hover,
.top-dishes-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.08);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 2px solid #f3f4f6;
}

.header-icon {
  font-size: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.card-title {
  font-size: 18px;
  font-weight: 700;
  color: #111827;
  margin: 0;
}

/* Todo Card Styles */
.todo-summary {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  background: #f9fafb;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  margin-bottom: 16px;
}

.summary-text {
  color: #374151;
  font-size: 14px;
  font-weight: 500;
}

.summary-count {
  color: #111827;
  font-size: 24px;
  font-weight: 700;
}

.todo-items {
  margin-bottom: 16px;
}

.todo-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  margin-bottom: 8px;
  background: #f9fafb;
  border-radius: 8px;
  transition: all 0.2s;
}

.todo-item:hover {
  background: #f3f4f6;
}

.todo-item.completed {
  opacity: 0.6;
}

.todo-item.completed .item-name {
  text-decoration: line-through;
  color: #9ca3af;
}

.item-checkbox {
  font-size: 18px;
  color: #667eea;
}

.check-icon {
  color: #10b981;
  font-weight: bold;
}

.uncheck-icon {
  color: #d1d5db;
}

.item-content {
  flex: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.item-name {
  font-size: 14px;
  font-weight: 500;
  color: #374151;
}

.item-quantity {
  font-size: 13px;
  color: #6b7280;
  background: white;
  padding: 2px 8px;
  border-radius: 4px;
}

.more-items {
  text-align: center;
  padding: 8px;
  color: #6b7280;
  font-size: 13px;
}

.view-all-btn {
  width: 100%;
  padding: 10px;
  background: #f3f4f6;
  color: #374151;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.view-all-btn:hover {
  background: #e5e7eb;
  border-color: #d1d5db;
  color: #111827;
}

.empty-todo {
  text-align: center;
  padding: 32px 16px;
}

.empty-icon {
  font-size: 48px;
  display: block;
  margin-bottom: 12px;
}

.empty-text {
  color: #9ca3af;
  font-size: 14px;
  margin: 0;
}

/* Top Dishes Card Styles */
.dishes-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.dish-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px;
  background: #f9fafb;
  border-radius: 12px;
  transition: all 0.2s;
  position: relative;
}

.dish-item:hover {
  background: #f3f4f6;
  transform: translateX(4px);
}

.dish-rank {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 700;
  flex-shrink: 0;
}

.rank-gold {
  background: linear-gradient(135deg, #ffd700 0%, #ffed4e 100%);
  color: #b45309;
  box-shadow: 0 2px 8px rgba(255, 215, 0, 0.3);
}

.rank-silver {
  background: linear-gradient(135deg, #c0c0c0 0%, #e8e8e8 100%);
  color: #6b7280;
  box-shadow: 0 2px 8px rgba(192, 192, 192, 0.3);
}

.rank-bronze {
  background: linear-gradient(135deg, #cd7f32 0%, #e9a66d 100%);
  color: #7c2d12;
  box-shadow: 0 2px 8px rgba(205, 127, 50, 0.3);
}

.rank-normal {
  background: #e5e7eb;
  color: #9ca3af;
}

.dish-info {
  flex: 1;
  min-width: 0;
}

.dish-name {
  font-size: 15px;
  font-weight: 600;
  color: #111827;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dish-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.dish-price {
  font-size: 14px;
  font-weight: 600;
  color: #dc2626;
}

.dish-category {
  font-size: 12px;
  color: #6b7280;
  background: white;
  padding: 2px 8px;
  border-radius: 4px;
}

.dish-badge {
  position: absolute;
  top: -4px;
  right: -4px;
  font-size: 20px;
}

.empty-dishes {
  text-align: center;
  padding: 32px 16px;
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
  margin: 0 0 12px 0;
  color: #111827;
  letter-spacing: -0.5px;
}

.text-positive { color: #059669; }
.text-negative { color: #dc2626; }

/* Stat Indicators */
.stat-indicators {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 12px;
}

.indicator-item {
  font-size: 13px;
  color: #6b7280;
  display: flex;
  align-items: center;
  gap: 6px;
}

.indicator-item strong {
  font-weight: 600;
}

.indicator-item.comparison {
  font-size: 12px;
  padding: 4px 8px;
  background: #f9fafb;
  border-radius: 4px;
  width: fit-content;
}

.indicator-item.comparison.positive {
  color: #059669;
  background: #ecfdf5;
}

.indicator-item.comparison.negative {
  color: #dc2626;
  background: #fef2f2;
}

/* Budget Warning */
.stat-card.budget-warning {
  border: 2px solid #f59e0b;
  background: linear-gradient(to bottom, #ffffff 0%, #fffbeb 100%);
}

.stat-card.budget-danger {
  border: 2px solid #ef4444;
  background: linear-gradient(to bottom, #ffffff 0%, #fef2f2 100%);
}

.budget-progress {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 4px;
}

.budget-bar-bg {
  flex: 1;
  height: 8px;
  background: #f3f4f6;
  border-radius: 4px;
  overflow: hidden;
}

.budget-bar-fill {
  height: 100%;
  background: #3b82f6;
  border-radius: 4px;
  transition: all 0.3s ease;
}

.budget-bar-fill.warning {
  background: #f59e0b;
}

.budget-bar-fill.danger {
  background: #ef4444;
}

.budget-text {
  font-size: 13px;
  font-weight: 600;
  color: #6b7280;
  min-width: 45px;
  text-align: right;
}

.budget-text.warning {
  color: #f59e0b;
}

.budget-text.danger {
  color: #ef4444;
}

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

.header-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.action-btn.secondary {
  background-color: #f3f4f6;
  color: #374151;
  font-size: 14px;
  padding: 8px 16px;
}

.action-btn.secondary:hover {
  background-color: #e5e7eb;
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

:deep(.el-table__row) {
  transition: all 0.2s ease;
}

:deep(.el-table__row:hover) {
  background-color: #f8f9fa !important;
}

/* Pagination */
.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 24px;
  padding: 16px 0;
}

/* Records View Section */
.records-view-section {
  margin-top: 32px;
  padding-top: 32px;
  border-top: 2px solid #f3f4f6;
  animation: slideDown 0.3s ease-out;
}

.records-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.section-subtitle {
  font-size: 18px;
  font-weight: 700;
  color: #111827;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.section-subtitle::before {
  content: '';
  width: 4px;
  height: 20px;
  background: linear-gradient(to bottom, #3b82f6, #2563eb);
  border-radius: 2px;
}

.export-btn {
  background-color: #10b981;
  color: white;
  font-size: 14px;
  padding: 8px 16px;
}

.export-btn:hover {
  background-color: #059669;
}

.percentage-text {
  color: #6b7280;
  font-weight: 500;
  font-size: 13px;
}

/* Chart Analysis Section */
.chart-analysis-section {
  margin-top: 32px;
  padding-top: 32px;
  border-top: 2px solid #f3f4f6;
  animation: slideDown 0.3s ease-out;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from, .fade-leave-to {
  opacity: 0;
}

.chart-section-title {
  font-size: 18px;
  font-weight: 700;
  color: #111827;
  margin: 0 0 24px 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.chart-section-title::before {
  content: '';
  width: 4px;
  height: 20px;
  background: linear-gradient(to bottom, #3b82f6, #2563eb);
  border-radius: 2px;
}

.charts-wrapper {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(450px, 1fr));
  gap: 32px;
  margin-top: 24px;
}

.chart-item {
  background: #ffffff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
  border: 1px solid rgba(0, 0, 0, 0.03);
  transition: all 0.3s ease;
}

.chart-item:hover {
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.chart-header {
  margin-bottom: 16px;
}

.chart-title {
  font-size: 16px;
  font-weight: 600;
  color: #111827;
  margin: 0 0 6px 0;
}

.chart-subtitle {
  font-size: 13px;
  color: #6b7280;
  margin: 0;
  font-weight: 500;
}

.pie-chart {
  width: 100%;
  height: 380px;
  min-height: 350px;
}

/* Trend Analysis Section */
.trend-analysis-section {
  margin-top: 32px;
  padding-top: 32px;
  border-top: 2px solid #f3f4f6;
  animation: slideDown 0.3s ease-out;
}

.trend-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.trend-tabs {
  display: flex;
  background: #f3f4f6;
  padding: 4px;
  border-radius: 8px;
  gap: 4px;
}

.trend-tab-item {
  padding: 6px 16px;
  border: none;
  background: transparent;
  font-size: 13px;
  font-weight: 500;
  color: #6b7280;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.trend-tab-item:hover {
  background: #e5e7eb;
  color: #374151;
}

.trend-tab-item.active {
  background: white;
  color: #111827;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

.trend-chart-container {
  background: #ffffff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
  border: 1px solid rgba(0, 0, 0, 0.03);
}

.trend-chart {
  width: 100%;
  height: 400px;
  min-height: 350px;
}

@media (max-width: 768px) {
  .dashboard-container { padding: 20px; }
  .dashboard-header { flex-direction: column; align-items: flex-start; gap: 20px; }
  .header-right { width: 100%; display: flex; justify-content: flex-end; }
  .header-actions { flex-direction: column; align-items: flex-end; gap: 12px; }
  .pagination-wrapper { justify-content: center; }
  .charts-wrapper { 
    grid-template-columns: 1fr; 
    gap: 24px;
  }
  .chart-item { padding: 16px; }
  .pie-chart { height: 320px; min-height: 280px; }
  
  .date-filter-section {
    flex-direction: column;
    align-items: flex-start;
    padding: 16px;
  }
  
  .filter-buttons {
    width: 100%;
  }
  
  .filter-btn {
    flex: 1;
    min-width: 60px;
  }
  
  .custom-date-picker {
    margin-left: 0;
    width: 100%;
  }
  
  .records-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .export-btn {
    width: 100%;
  }
  
  .business-overview {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .todo-card,
  .top-dishes-card {
    padding: 16px;
  }
  
  .trend-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .trend-tabs {
    width: 100%;
  }
  
  .trend-tab-item {
    flex: 1;
  }
  
  .trend-chart-container {
    padding: 16px;
  }
  
  .trend-chart {
    height: 320px;
    min-height: 280px;
  }
}
</style>
