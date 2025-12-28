<template>
  <div class="reservation-page">
    <div class="page-header">
      <div>
        <h2>📅 在线预定</h2>
        <p class="subtitle">选择您喜欢的时间和座位</p>
      </div>
    </div>

    <el-card class="form-card" shadow="never">
      <el-form :model="form" :rules="formRules" ref="formRef" label-width="100px" size="large">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="预定日期" prop="reservationDate">
              <el-date-picker
                v-model="form.reservationDate"
                type="date"
                placeholder="选择日期"
                style="width: 100%"
                :disabled-date="disabledDate"
                value-format="YYYY-MM-DD"
                @change="handleDateTimeChange"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="预定时间" prop="reservationTime">
              <el-select
                v-model="form.reservationTime"
                placeholder="选择时间段"
                style="width: 100%"
                @change="handleDateTimeChange"
              >
                <el-option
                  v-for="time in timeSlots"
                  :key="time"
                  :label="time"
                  :value="time"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="就餐人数" prop="guestCount">
              <el-input-number
                v-model="form.guestCount"
                :min="1"
                :max="20"
                style="width: 100%"
                @change="handleDateTimeChange"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">可用餐桌</el-divider>

        <el-form-item prop="tableId">
          <div v-if="availableTables.length > 0" class="table-grid">
            <div
              v-for="table in availableTables"
              :key="table.id"
              class="table-card"
              :class="{ selected: form.tableId === table.id }"
              @click="selectTable(table)"
            >
              <div class="table-icon">
                {{ table.tableType === 'ROUND_TABLE' ? '⭕' : '⬜' }}
              </div>
              <div class="table-info">
                <div class="table-number">{{ table.tableNumber }}号桌</div>
                <div class="table-type">{{ getTableTypeName(table.tableType) }}</div>
                <div class="table-capacity">{{ table.capacity }}人座</div>
              </div>
            </div>
          </div>

          <el-empty v-else-if="searchTriggered" description="暂无可用餐桌，请选择其他时间" />
          <el-alert v-else type="info" :closable="false" style="margin-bottom: 20px">
            <template #default>
              请先选择日期、时间和人数，系统将为您推荐合适的餐桌
            </template>
          </el-alert>
        </el-form-item>

        <el-divider content-position="left">联系信息</el-divider>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="您的姓名" prop="customerName">
              <el-input v-model="form.customerName" placeholder="请输入姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="customerPhone">
              <el-input v-model="form.customerPhone" placeholder="请输入手机号" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="备注">
          <el-input
            v-model="form.remark"
            type="textarea"
            :rows="3"
            placeholder="特殊需求或其他说明（选填）"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" size="large" @click="submitReservation" :loading="submitting">
            提交预定
          </el-button>
          <el-button size="large" @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="my-reservations" shadow="never" style="margin-top: 20px">
      <template #header>
        <div class="card-header">
          <span>我的预定</span>
          <el-button text @click="loadMyReservations">
            <el-icon><Refresh /></el-icon>
          </el-button>
        </div>
      </template>

      <el-table :data="myReservations" stripe>
        <el-table-column prop="reservationDate" label="日期" width="120" />
        <el-table-column prop="reservationTime" label="时间" width="100" />
        <el-table-column label="桌号" width="100">
          <template #default="{ row }">
            {{ row.tableNumber || '桌' + row.tableId }}
          </template>
        </el-table-column>
        <el-table-column prop="guestCount" label="人数" width="80" />
        <el-table-column prop="customerName" label="姓名" width="100" />
        <el-table-column prop="customerPhone" label="电话" width="120" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" show-overflow-tooltip />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 0 || row.status === 1"
              type="danger"
              text
              size="small"
              @click="cancelMyReservation(row.id)"
            >
              取消
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="myReservations.length === 0" description="暂无预定记录" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getAvailableTables, createReservation, getMyReservations, cancelReservation } from '@/api/reservation'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'

interface RestaurantTable {
  id: number
  tableNumber: string
  tableType: string
  capacity: number
  status: number
}

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
}

const formRef = ref()
const submitting = ref(false)
const searchTriggered = ref(false)
const availableTables = ref<RestaurantTable[]>([])
const myReservations = ref<Reservation[]>([])

const form = reactive({
  reservationDate: '',
  reservationTime: '',
  guestCount: 2,
  tableId: null as number | null,
  customerName: '',
  customerPhone: '',
  remark: ''
})

// Time slots: 10:00 to 21:00 (营业时间 10:00-22:00, 21:00是最后一个可预定时段)
const timeSlots = [
  '10:00', '11:00', '12:00', '13:00', '14:00', '15:00',
  '16:00', '17:00', '18:00', '19:00', '20:00', '21:00'
]

const formRules = {
  reservationDate: [{ required: true, message: '请选择预定日期', trigger: 'change' }],
  reservationTime: [{ required: true, message: '请选择预定时间', trigger: 'change' }],
  guestCount: [{ required: true, message: '请输入就餐人数', trigger: 'blur' }],
  tableId: [{ required: true, message: '请选择餐桌', trigger: 'change' }],
  customerName: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度在2到20个字符', trigger: 'blur' }
  ],
  customerPhone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入有效的手机号', trigger: 'blur' }
  ]
}

// Disable past dates
const disabledDate = (time: Date) => {
  return time.getTime() < Date.now() - 8.64e7 // Yesterday
}

const getTableTypeName = (type: string) => {
  return type === 'ROUND_TABLE' ? '大圆桌' : '四人座'
}

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

const handleDateTimeChange = async () => {
  if (form.reservationDate && form.reservationTime && form.guestCount) {
    searchTriggered.value = true
    try {
      const res: any = await getAvailableTables({
        date: form.reservationDate,
        time: form.reservationTime,
        guestCount: form.guestCount
      })
      availableTables.value = res
      form.tableId = null // Reset table selection
    } catch (error) {
      console.error('查询可用餐桌失败:', error)
      availableTables.value = []
    }
  }
}

const selectTable = (table: RestaurantTable) => {
  form.tableId = table.id
}

const submitReservation = async () => {
  if (!formRef.value) return

  try {
    // Validate form
    await formRef.value.validate()

    submitting.value = true
    await createReservation(form)
    ElMessage.success('预定成功！请等待确认')
    resetForm()
    loadMyReservations()
  } catch (error: any) {
    // Form validation failed or API call failed
    if (error?.response?.data?.message) {
      // API error
      ElMessage.error(error.response.data.message)
    } else if (error?.message) {
      // Other errors
      ElMessage.error(error.message)
    } else {
      // Form validation failed - errors already shown by el-form
      console.log('表单验证失败:', error)
    }
  } finally {
    submitting.value = false
  }
}

const resetForm = () => {
  formRef.value?.resetFields()
  availableTables.value = []
  searchTriggered.value = false
  form.tableId = null
}

const loadMyReservations = async () => {
  try {
    const res: any = await getMyReservations()
    myReservations.value = res
  } catch (error) {
    console.error('加载预定记录失败:', error)
  }
}

const cancelMyReservation = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要取消此预定吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await cancelReservation(id)
    ElMessage.success('已取消预定')
    loadMyReservations()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('取消预定失败:', error)
    }
  }
}

onMounted(() => {
  loadMyReservations()
})
</script>

<style scoped>
.reservation-page {
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

.form-card {
  border-radius: 12px;
}

.table-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  margin-bottom: 20px;
  overflow-x: auto;
}

.table-card {
  border: 2px solid #e4e7ed;
  border-radius: 12px;
  padding: 20px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
  background: #fff;
}

.table-card:hover {
  border-color: #409eff;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
  transform: translateY(-2px);
}

.table-card.selected {
  border-color: #409eff;
  background: linear-gradient(135deg, #e6f7ff 0%, #f0f9ff 100%);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.table-icon {
  font-size: 48px;
  margin-bottom: 10px;
}

.table-number {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 5px;
}

.table-type {
  font-size: 13px;
  color: #666;
  margin-bottom: 3px;
}

.table-capacity {
  font-size: 12px;
  color: #999;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.my-reservations {
  border-radius: 12px;
}
</style>
