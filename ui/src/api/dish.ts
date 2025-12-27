import request from './request'

export const getDishPage = (params: any) => {
    return request({ url: '/dish/page', method: 'get', params })
}
export const getPublicDishList = () => {
    return request({ url: '/dish/public/list', method: 'get' })
}
export const addDish = (data: any) => {
    return request({ url: '/dish', method: 'post', data })
}
export const updateDish = (data: any) => {
    return request({ url: '/dish', method: 'put', data })
}
export const delDish = (id: number) => {
    return request({ url: `/dish/${id}`, method: 'delete' })
}

export const getDishCategoryList = (params?: any) => {
    return request({ url: '/dish/category/list', method: 'get', params })
}
