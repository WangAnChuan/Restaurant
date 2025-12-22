import request from './request'

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
export const delCategory = (id: number) => {
    return request({ url: `/category/${id}`, method: 'delete' })
}
export const getDashboardStats = () => {
    return request({ url: '/dashboard/stats', method: 'get' })
}
export const getDashboardChart = (params: any) => {
    return request({ url: '/dashboard/chart', method: 'get', params })
}
