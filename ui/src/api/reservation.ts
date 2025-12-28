import request from './request'

// Table APIs
export const getTableList = () => {
    return request({ url: '/table/list', method: 'get' })
}

export const getTablePage = (params: any) => {
    return request({ url: '/table/page', method: 'get', params })
}

export const addTable = (data: any) => {
    return request({ url: '/table', method: 'post', data })
}

export const updateTable = (data: any) => {
    return request({ url: '/table', method: 'put', data })
}

// Reservation APIs
export const getAvailableTables = (params: any) => {
    return request({ url: '/reservation/available-tables', method: 'get', params })
}

export const createReservation = (data: any) => {
    return request({ url: '/reservation', method: 'post', data })
}

export const getMyReservations = () => {
    return request({ url: '/reservation/my', method: 'get' })
}

export const getReservationPage = (params: any) => {
    return request({ url: '/reservation/page', method: 'get', params })
}

export const updateReservationStatus = (id: number, status: number) => {
    return request({ url: `/reservation/${id}/status`, method: 'put', params: { status } })
}

export const cancelReservation = (id: number) => {
    return request({ url: `/reservation/${id}`, method: 'delete' })
}
