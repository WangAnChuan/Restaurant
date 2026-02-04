import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

// https://vitejs.dev/config/
export default defineConfig({
    plugins: [vue()],
    resolve: {
        alias: {
            '@': path.resolve(__dirname, 'src')
        }
    },
    build: {
        chunkSizeWarningLimit: 1500,
        rollupOptions: {
            output: {
                manualChunks(id) {
                    if (id.includes('node_modules')) {
                        if (id.includes('element-plus')) {
                            return 'element-plus'
                        }
                        if (id.includes('echarts')) {
                            return 'echarts'
                        }
                        if (id.includes('xlsx')) {
                            return 'xlsx'
                        }
                        return 'vendor'
                    }
                }
            }
        }
    },
    server: {
        port: 3000,
        proxy: {
            '/api': {
                target: 'http://localhost:9095',
                changeOrigin: true
            },
            '/uploads': {
                target: 'http://localhost:9095',
                changeOrigin: true
            }
        }
    }
})
