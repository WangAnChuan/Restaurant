import request from './request'

export const getPurchaseList = (params: any) => {
    return request({ url: '/purchase/list', method: 'get', params })
}
export const addPurchase = (data: any) => {
    return request({ url: '/purchase', method: 'post', data })
}
export const updatePurchase = (data: any) => {
    return request({ url: '/purchase', method: 'put', data })
}
export const delPurchase = (id: number) => {
    return request({ url: `/purchase/${id}`, method: 'delete' })
}
