import request from './request'

// 定义账目管理、分类管理和仪表盘相关的所有 API 接口
// - `getAccountPage(params)` - 分页查询账目记录（第 3 行）
// - `addAccount(data)` - 新增账目记录（第 6 行）
// - `updateAccount(data)` - 更新账目记录（第 9 行）
// - `delAccount(id)` - 删除账目记录（第 12 行）
// - `getCategoryList(params)` - 查询分类列表（第 15 行）
// - `addCategory(data)` - 新增分类（第 18 行）
// - `delCategory(id)` - 删除分类（第 21 行）
export const getAccountPage = (params: any) => {
    return request({ url: '/account/page', method: 'get', params })
}
export const addAccount = (data: any) => {
    return request({ url: '/account', method: 'post', data })
}
export const updateAccount = (data: any) => {
    return request({ url: '/account', method: 'put', data })
}
export const delAccount = (id: number) => {
    return request({ url: `/account/${id}`, method: 'delete' })
}
export const getCategoryList = (params?: any) => {
    return request({ url: '/category/list', method: 'get', params })
}
export const addCategory = (data: any) => {
    return request({ url: '/category', method: 'post', data })
}
export const updateCategory = (data: any) => {
    return request({ url: '/category', method: 'put', data })
}
export const delCategory = (id: number) => {
    return request({ url: `/category/${id}`, method: 'delete' })
}
export const getDashboardStats = () => {
    return request({ url: '/dashboard/stats', method: 'get' })
}
export const getDashboardChart = (params: any) => {
    return request({ url: '/dashboard/chart', method: 'get', params })
}
