<template>
  <div class="reservation-manage-page">
    <div class="page-header">
      <div>
        <h2>📋 预定管理</h2>
        <p class="subtitle">查看和管理所有预定</p>
      </div>
    </div>

    <!-- Statistics Cards -->
    <el-row :gutter="20" style="margin-bottom: 20px">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #ecf5ff; color: #409eff">📅</div>
            <div>
              <div class="stat-value">{{ stats.todayCount }}</div>
              <div class="stat-label">今日预定</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #fef0f0; color: #f56c6c">⏰</div>
            <div>
              <div class="stat-value">{{ stats.pendingCount }}</div>
              <div class="stat-label">待确认</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #f0f9ff; color: #67c23a">✅</div>
            <div>
              <div class="stat-value">{{ stats.confirmedCount }}</div>
              <div class="stat-label">已确认</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #fdf6ec; color: #e6a23c">🍽️</div>
            <div>
              <div class="stat-value">8</div>
              <div class="stat-label">总餐桌数</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Filters -->
    <el-card shadow="never" style="margin-bottom: 20px">
      <el-form :inline="true">
        <el-form-item label="日期筛选">
          <el-date-picker
            v-model="filterDate"
            type="date"
            placeholder="选择日期"
            style="width: 200px"
            clearable
            @change="loadReservations"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="filterStatus"
            placeholder="全部状态"
            style="width: 150px"
            clearable
            @change="loadReservations"
          >
            <el-option label="待确认" :value="0" />
            <el-option label="已确认" :value="1" />
            <el-option label="已完成" :value="2" />
            <el-option label="已取消" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadReservations">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- Reservations Table -->
    <el-card shadow="never">
      <el-table :data="reservations" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column label="餐桌" width="80">
          <template #default="{ row }">
            <el-tag size="small">{{ getTableNumber(row.tableId) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reservationDate" label="日期" width="120" />
        <el-table-column prop="reservationTime" label="时间" width="80" />
        <el-table-column prop="guestCount" label="人数" width="60" />
        <el-table-column prop="customerName" label="顾客" width="100" />
        <el-table-column prop="customerPhone" label="电话" width="120" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" show-overflow-tooltip />
        <el-table-column label="创建时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 0"
              type="success"
              text
              size="small"
              @click="updateStatus(row.id, 1)"
            >
              确认
            </el-button>
            <el-button
              v-if="row.status === 1"
              type="primary"
              text
              size="small"
              @click="updateStatus(row.id, 2)"
            >
              完成
            </el-button>
            <el-button
              v-if="row.status === 0 || row.status === 1"
              type="danger"
              text
              size="small"
              @click="updateStatus(row.id, 3)"
            >
              取消
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- Pagination -->
      <div style="margin-top: 20px; text-align: right">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadReservations"
          @current-change="loadReservations"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { getReservationPage, updateReservationStatus, getTableList } from '@/api/reservation'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'

interface Reservation {
  id: number
  tableId: number
  customerName: string
  customerPhone: string
  reservationDate: string
  reservationTime: string
  guestCount: number
  status: number
  remark: string
  createTime: string
}

const loading = ref(false)
const filterDate = ref('')
const filterStatus = ref<number | undefined>(undefined)
const reservations = ref<Reservation[]>([])
const tables = ref<any[]>([])

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const stats = computed(() => {
  const today = new Date().toISOString().split('T')[0]
  const todayReservations = reservations.value.filter(r => r.reservationDate === today)
  
  return {
    todayCount: todayReservations.length,
    pendingCount: reservations.value.filter(r => r.status === 0).length,
    confirmedCount: reservations.value.filter(r => r.status === 1).length
  }
})

const getStatusType = (status: number) => {
  const types: Record<number, any> = {
    0: 'warning',
    1: 'success',
    2: 'info',
    3: 'danger'
  }
  return types[status] || 'info'
}

const getStatusText = (status: number) => {
  const texts: Record<number, string> = {
    0: '待确认',
    1: '已确认',
    2: '已完成',
    3: '已取消'
  }
  return texts[status] || '未知'
}

const getTableNumber = (tableId: number) => {
  const table = tables.value.find(t => t.id === tableId)
  return table ? table.tableNumber : '-'
}

const formatDateTime = (datetime: string) => {
  if (!datetime) return '-'
  return datetime.replace('T', ' ').substring(0, 16)
}

// 格式化日期为 yyyy-MM-dd 格式
const formatDate = (date: Date | string): string => {
  if (!date) return ''
  const d = typeof date === 'string' ? new Date(date) : date
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

const loadReservations = async () => {
  loading.value = true
  try {
    const params: any = {
      current: pagination.current,
      size: pagination.size
    }

    if (filterDate.value) {
      // 将日期格式化为 yyyy-MM-dd 格式发送给后端
      params.date = formatDate(filterDate.value)
    }
    if (filterStatus.value !== undefined) {
      params.status = filterStatus.value
    }

    const res: any = await getReservationPage(params)
    reservations.value = res.records
    pagination.total = res.total
  } catch (error) {
    console.error('加载预定列表失败:', error)
  } finally {
    loading.value = false
  }
}

const loadTables = async () => {
  try {
    const res: any = await getTableList()
    tables.value = res
  } catch (error) {
    console.error('加载餐桌列表失败:', error)
  }
}

const updateStatus = async (id: number, status: number) => {
  try {
    await updateReservationStatus(id, status)
    ElMessage.success('状态更新成功')
    loadReservations()
  } catch (error) {
    console.error('更新状态失败:', error)
  }
}

const resetFilters = () => {
  filterDate.value = ''
  filterStatus.value = undefined
  pagination.current = 1
  loadReservations()
}

onMounted(() => {
  loadTables()
  loadReservations()
})
</script>

<style scoped>
.reservation-manage-page {
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

.stat-card {
  border-radius: 12px;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 15px;
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #333;
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  color: #888;
  margin-top: 3px;
}
</style>
