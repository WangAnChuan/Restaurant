<template>
  <div class="menu-page">
    <div class="background-decor"></div>
    
    <header class="page-header">
      <div class="header-content">
        <h1 class="title">今日菜单</h1>
        <div class="divider"></div>
        <p class="subtitle">精选食材 · 用心烹饪 · 每日新鲜供应</p>
      </div>
    </header>

    <main class="menu-container">
      <div v-if="loading" class="loading-state">
        <div class="loader"></div>
      </div>

      <div v-else-if="list.length > 0" class="menu-grid">
        <div 
          v-for="(dish, index) in list" 
          :key="dish.id" 
          class="dish-card"
          :style="{ '--delay': `${index * 0.1}s` }"
        >
          <div class="card-image-wrapper">
            <div 
              class="dish-image" 
              :style="getDishStyle(dish)"
            >
              <span v-if="!dish.imageUrl" class="dish-emoji">{{ getDishEmoji(dish.name) }}</span>
            </div>
            <div class="status-badge">
              <span class="dot"></span>
              在售
            </div>
          </div>
          
          <div class="card-content">
            <div class="card-main">
              <h3 class="dish-name">{{ dish.name }}</h3>
              <div class="ingredients-box">
                <i class="icon-leaf">🌿</i>
                <span class="ingredients-text">{{ dish.ingredients || '主厨秘制配方' }}</span>
              </div>
            </div>
            
            <div class="card-footer">
              <div class="price-block">
                <span class="currency">¥</span>
                <span class="amount">{{ dish.price }}</span>
              </div>
              <button class="order-btn" title="点击详情">
                <span class="btn-icon">➜</span>
              </button>
            </div>
          </div>
        </div>
      </div>

      <el-empty v-else description="今日暂无供应" class="empty-state" />
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getPublicDishList } from '@/api/dish'

const list = ref<any[]>([])
const loading = ref(true)

const dishEmojis: Record<string, string> = {
  '鱼': '🐟', '肉': '🥩', '鸡': '🍗', '牛': '🥩', '虾': '🦐', '蟹': '🦀',
  '面': '🍜', '饭': '🍚', '汤': '🥣', '菜': '🥬', '蛋': '🥚', '豆': '🫘',
  '酒': '🍷', '茶': '🍵', '果': '🍎', '甜': '🍰', '辣': '🌶️'
}

// Pastel colors for emoji backgrounds
const bgColors = [
  '#FF9A9E', '#FECFEF', '#A18CD1', '#FBC2EB', '#8FD3F4',
  '#84FAB0', '#E0C3FC', '#FFD1FF', '#E2EBf0', '#CFD9DF'
]

const getDishEmoji = (name: string) => {
  for (const key in dishEmojis) {
    if (name.includes(key)) return dishEmojis[key]
  }
  return '🍽️'
}

const getImageUrl = (url: string) => {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return 'http://localhost:9095' + url
}

const getDishStyle = (dish: any) => {
  if (dish.imageUrl) {
    return { backgroundImage: `url(${getImageUrl(dish.imageUrl)})` }
  }
  // Generate a consistent random color based on name length/char code
  const index = dish.name.length % bgColors.length
  return { 
    backgroundColor: bgColors[index],
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center'
  }
}

const load = async () => {
  try {
    loading.value = true
    const res: any = await getPublicDishList()
    list.value = res
  } finally {
    loading.value = false
  }
}

onMounted(() => load())
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Noto+Sans+SC:wght@300;400;500;700&display=swap');

.menu-page {
  min-height: 100vh;
  background-color: #f7f8fa;
  font-family: 'Noto Sans SC', system-ui, -apple-system, sans-serif;
  position: relative;
  overflow-x: hidden;
  padding-bottom: 60px;
}

/* Abstract Background Decor */
.background-decor {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 500px;
  background: linear-gradient(180deg, #eef2f6 0%, rgba(247, 248, 250, 0) 100%);
  z-index: 0;
  clip-path: ellipse(150% 100% at 50% 0%);
}

.page-header {
  position: relative;
  z-index: 1;
  text-align: center;
  padding: 80px 20px 60px;
}

.title {
  font-size: 3.5rem;
  font-weight: 800;
  color: #2c3e50;
  margin: 0;
  letter-spacing: -1px;
  background: linear-gradient(45deg, #2c3e50, #3498db);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.divider {
  width: 60px;
  height: 4px;
  background: #3498db;
  margin: 20px auto;
  border-radius: 2px;
}

.subtitle {
  font-size: 1.1rem;
  color: #7f8c8d;
  font-weight: 400;
  margin-top: 10px;
  letter-spacing: 2px;
}

.menu-container {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 24px;
  position: relative;
  z-index: 1;
}

.menu-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 32px;
}

.dish-card {
  background: white;
  border-radius: 24px;
  overflow: hidden;
  box-shadow: 0 10px 40px -10px rgba(0, 0, 0, 0.05);
  transition: all 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
  animation: slideUp 0.8s cubic-bezier(0.2, 0.8, 0.2, 1) backwards;
  animation-delay: var(--delay);
  display: flex;
  flex-direction: column;
}

.dish-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 20px 50px -12px rgba(0, 0, 0, 0.12);
}

.card-image-wrapper {
  height: 240px;
  position: relative;
  overflow: hidden;
}

.dish-image {
  width: 100%;
  height: 100%;
  background-size: cover;
  background-position: center;
  transition: transform 0.6s ease;
}

.dish-card:hover .dish-image {
  transform: scale(1.08);
}

.dish-emoji {
  font-size: 80px;
  filter: drop-shadow(0 4px 12px rgba(0,0,0,0.1));
}

.status-badge {
  position: absolute;
  top: 20px;
  right: 20px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(4px);
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: 600;
  color: #27ae60;
  display: flex;
  align-items: center;
  gap: 6px;
  box-shadow: 0 4px 15px rgba(0,0,0,0.05);
}

.dot {
  width: 6px;
  height: 6px;
  background-color: #27ae60;
  border-radius: 50%;
  animation: pulse 2s infinite;
}

.card-content {
  padding: 28px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.card-main {
  flex: 1;
}

.dish-name {
  font-size: 1.5rem;
  font-weight: 700;
  color: #2c3e50;
  margin: 0 0 12px;
  line-height: 1.3;
}

.ingredients-box {
  display: flex;
  align-items: start;
  gap: 8px;
  margin-bottom: 24px;
}

.icon-leaf {
  font-style: normal;
  font-size: 1rem;
}

.ingredients-text {
  font-size: 0.95rem;
  color: #95a5a6;
  line-height: 1.5;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 20px;
  border-top: 1px solid #f0f2f5;
}

.price-block {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.currency {
  font-size: 1rem;
  font-weight: 600;
  color: #e74c3c;
}

.amount {
  font-size: 2rem;
  font-weight: 800;
  color: #2c3e50;
  letter-spacing: -0.5px;
}

.order-btn {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  border: none;
  background: #2c3e50;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
}

.order-btn:hover {
  background: #34495e;
  transform: scale(1.1) rotate(90deg);
}

.booking-btn {
  font-size: 1.1rem;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes pulse {
  0% { box-shadow: 0 0 0 0 rgba(39, 174, 96, 0.4); }
  70% { box-shadow: 0 0 0 6px rgba(39, 174, 96, 0); }
  100% { box-shadow: 0 0 0 0 rgba(39, 174, 96, 0); }
}

/* Loading State */
.loading-state {
  display: flex;
  justify-content: center;
  padding: 100px 0;
}

.loader {
  width: 40px;
  height: 40px;
  border: 3px solid #eee;
  border-top-color: #3498db;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

@media (max-width: 768px) {
  .title { font-size: 2.5rem; }
  .menu-grid { grid-template-columns: 1fr; }
  .dish-image { height: 200px; }
}
</style>
