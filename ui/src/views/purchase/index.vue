<template>
  <div class="purchase-page">
    <div class="page-header">
      <div class="header-text">
        <h2>🛒 采购清单</h2>
        <p class="subtitle">记录每日采购物品</p>
      </div>
      <el-button type="primary" size="large" class="large-btn" @click="dialogVisible = true">
        <el-icon :size="24" style="margin-right: 8px"><Plus /></el-icon>
        <span style="font-size: 18px; font-weight: bold;">添加物品</span>
      </el-button>
    </div>

    <!-- 筛选区域：大号日期选择 -->
    <div class="filter-section">
      <div class="date-picker-wrapper">
        <span class="filter-label">📅 选择日期：</span>
        <el-date-picker 
          v-model="targetDate" 
          type="date" 
          value-format="YYYY-MM-DD" 
          @change="load"
          size="large"
          class="large-date-picker"
          :clearable="false"
          placeholder="选择日期"
        />
      </div>
    </div>

    <!-- 列表区域：改为卡片列表 -->
    <div class="list-container" v-loading="loading">
      <div v-if="list.length > 0" class="card-list">
        <div v-for="(item, index) in list" :key="item.id" class="item-card">
          <div class="card-main">
            <div class="item-index">{{ index + 1 }}</div>
            <div class="item-info">
              <div class="item-name">{{ item.itemName }}</div>
              <div class="item-remark" v-if="item.remark">备注：{{ item.remark }}</div>
            </div>
            <div class="item-quantity">
              {{ item.quantity }}
            </div>
          </div>
          <div class="card-actions">
            <el-popconfirm 
              title="确定删除此物品？" 
              confirm-button-text="删除"
              cancel-button-text="取消"
              confirm-button-type="danger"
              width="260"
              @confirm="handleDel(item.id)"
            >
              <template #reference>
                <el-button type="danger" size="large" class="delete-btn">
                  <el-icon><Delete /></el-icon> 删除
                </el-button>
              </template>
            </el-popconfirm>
          </div>
        </div>
      </div>
      <div v-else class="empty-state">
        <el-empty description="暂无采购记录" :image-size="200">
          <template #description>
             <span class="empty-text">今天还没有添加采购物品哦 ~</span>
          </template>
        </el-empty>
      </div>
    </div>

    <!-- 添加弹窗：大字体输入 -->
    <el-dialog v-model="dialogVisible" title="添加采购物品" width="90%" :max-width="600" custom-class="large-dialog">
      <el-form :model="form" label-width="100px" size="large" class="large-form">
        <el-form-item label="物品名称">
          <el-input v-model="form.itemName" placeholder="买什么？(如: 土豆)" class="large-input" />
        </el-form-item>
        <el-form-item label="数量">
          <el-input v-model="form.quantity" placeholder="买多少？(如: 5斤)" class="large-input" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" placeholder="有什么要求？" :rows="3" class="large-input" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button size="large" class="footer-btn" @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" size="large" class="footer-btn confirm-btn" @click="submit">确定添加</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getPurchaseList, addPurchase, delPurchase } from '@/api/purchase'
import { ElMessage } from 'element-plus'
import { Plus, Delete } from '@element-plus/icons-vue'

const targetDate = ref(new Date().toISOString().split('T')[0])
const list = ref<any[]>([])
const dialogVisible = ref(false)
const loading = ref(false)
const form = reactive({ itemName: '', quantity: '', remark: '' })

const load = async () => {
  loading.value = true
  try {
    const res: any = await getPurchaseList({ date: targetDate.value })
    list.value = res || []
  } finally {
    loading.value = false
  }
}

const submit = async () => {
  if (!form.itemName) {
    ElMessage.warning('请填写物品名称')
    return
  }
  if (!form.quantity) {
    ElMessage.warning('请填写数量')
    return
  }
  await addPurchase({ ...form, targetDate: targetDate.value })
  ElMessage.success({
    message: '添加成功！',
    duration: 2000,
    showClose: true,
    grouping: true,
  })
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
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 84px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  background-color: white;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.page-header h2 {
  margin: 0;
  font-size: 28px;
  color: #333;
  margin-bottom: 8px;
}

.subtitle {
  margin: 0;
  color: #666;
  font-size: 16px;
}

.large-btn {
  padding: 12px 24px;
  height: auto;
}

.filter-section {
  background: white;
  padding: 20px;
  border-radius: 12px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.date-picker-wrapper {
  display: flex;
  align-items: center;
}

.filter-label {
  font-size: 18px;
  font-weight: bold;
  color: #333;
  margin-right: 15px;
}

/* Card List Styles */
.card-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.item-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
  border: 1px solid #eee;
  transition: transform 0.2s;
}

.item-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.card-main {
  display: flex;
  align-items: center;
  flex: 1;
}

.item-index {
  font-size: 20px;
  font-weight: bold;
  color: #999;
  width: 40px;
  flex-shrink: 0;
}

.item-info {
  flex: 1;
  padding-right: 20px;
}

.item-name {
  font-size: 24px;
  font-weight: bold;
  color: #1a1a1a;
  margin-bottom: 4px;
}

.item-remark {
  font-size: 16px;
  color: #666;
}

.item-quantity {
  font-size: 26px;
  color: #409EFF;
  font-weight: bold;
  padding: 0 30px;
  min-width: 120px;
  text-align: right;
  border-right: 2px solid #f0f0f0;
  margin-right: 20px;
}

.card-actions {
  flex-shrink: 0;
}

.delete-btn {
  font-size: 16px;
  padding: 12px 24px;
}

.empty-text {
  font-size: 18px;
  color: #909399;
}

/* Dialog & Form Styles */
.large-form .el-form-item__label {
  font-size: 18px !important;
}

.large-input {
  font-size: 18px;
}

:deep(.el-input__inner) {
  height: 50px;
  font-size: 18px;
}

:deep(.el-textarea__inner) {
  font-size: 18px;
}

.dialog-footer {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 10px;
}

.footer-btn {
  width: 140px;
  height: 50px;
  font-size: 18px;
}

.confirm-btn {
  font-weight: bold;
}

/* Responsive adjustments */
@media (max-width: 768px) {
  .item-card {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .card-main {
    width: 100%;
    margin-bottom: 15px;
    flex-wrap: wrap;
  }
  
  .item-quantity {
    border-right: none;
    text-align: left;
    padding-left: 0;
    margin-top: 10px;
    width: 100%;
  }

  .card-actions {
    width: 100%;
    text-align: right;
  }
}
</style>
